/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.installer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.asm.jar.JarEntryRemappingTransformer;
import org.cadixdev.bombe.jar.JarClassEntry;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.asm.LorenzRemapper;
import org.cadixdev.lorenz.io.proguard.ProGuardReader;
import org.spongepowered.vanilla.installer.model.mojang.Version;
import org.spongepowered.vanilla.installer.model.mojang.VersionManifest;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public final class InstallerMain {

    private final Installer installer;

    public InstallerMain(final String[] args) throws Exception {
        LauncherCommandLine.configure(args);
        this.installer = new Installer(LauncherCommandLine.installerDirectory);
    }

    public static void main(final String[] args) throws Exception {
        new InstallerMain(args).run();
    }

    public void run() throws Exception {
        final Version mcVersion = this.downloadMinecraftManifest();
        final CompletableFuture<Path> mappingsFuture = this.downloadMappings(mcVersion, LauncherCommandLine.librariesDirectory);
        final CompletableFuture<Path> originalMcFuture = this.downloadMinecraft(mcVersion, LauncherCommandLine.librariesDirectory);
        final CompletableFuture<Path> remappedMinecraftJarFuture = mappingsFuture.thenCombineAsync(originalMcFuture, (mappings, minecraft) -> {
            try {
                return this.remapMinecraft(LauncherCommandLine.librariesDirectory, minecraft, mappings, this.installer.getLibraryManager().preparationWorker());
            } catch (final IOException ex) {
                return AsyncUtils.sneakyThrow(ex);
            }
        }, this.installer.getLibraryManager().preparationWorker());
        this.installer.getLibraryManager().validate();

        final Path remappedMinecraftJar = remappedMinecraftJarFuture.join();
        this.installer.getLibraryManager().addLibrary(new LibraryManager.Library("minecraft", remappedMinecraftJar));
        this.installer.getLibraryManager().finishedProcessing();

        Logger.info("Environment has been verified.");

        this.installer.getLibraryManager().getAll().values().stream()
            .map(LibraryManager.Library::getFile)
            .forEach(path -> {
                Logger.debug("Adding jar {} to classpath", path);
                Agent.addJarToClasspath(path);
            });

        final List<String> gameArgs = new ArrayList<>(LauncherCommandLine.remainingArgs);
        Collections.addAll(gameArgs, this.installer.getLauncherConfig().args.split(" "));

        // Suppress illegal reflection warnings on newer java
        Agent.crackModules();

        final String className = "org.spongepowered.vanilla.applaunch.Main";
        InstallerMain.invokeMain(className, gameArgs.toArray(new String[0]));
    }

    private static void invokeMain(final String className, final String[] args) {
        try {
            Class.forName(className)
                .getMethod("main", String[].class)
                .invoke(null, (Object) args);
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            Logger.error(ex, "Failed to invoke main class {} due to an error", className);
        }
    }

    private Version downloadMinecraftManifest() throws IOException {
        Logger.info("Downloading the Minecraft versions manifest...");

        VersionManifest.Version foundVersionManifest = null;

        final Gson gson = new Gson();
        try (final JsonReader reader = new JsonReader(new InputStreamReader(new URL(Constants.Libraries.MINECRAFT_MANIFEST_URL)
                .openStream()))) {
            final VersionManifest manifest = gson.fromJson(reader, VersionManifest.class);
            for (final VersionManifest.Version version : manifest.versions) {
                if (Constants.Libraries.MINECRAFT_VERSION_TARGET.equals(version.id)) {
                    foundVersionManifest = version;
                    break;
                }
            }
        }

        if (foundVersionManifest == null) {
            throw new IOException(String.format("Failed to find version manifest for '%s'!", Constants.Libraries.MINECRAFT_VERSION_TARGET));
        }

        final Version version;

        try (final JsonReader reader = new JsonReader(new InputStreamReader(foundVersionManifest.url.openStream()))) {
            version = gson.fromJson(reader, Version.class);
        }

        if (version == null) {
            throw new IOException(String.format("Failed to download version information for '%s'!",
                    Constants.Libraries.MINECRAFT_VERSION_TARGET));
        }

        return version;
    }

    private CompletableFuture<Path> downloadMinecraft(final Version version, final Path librariesDirectory) throws Exception {
        return AsyncUtils.asyncFailableFuture(() -> {
            final Path downloadTarget = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX)
                    .resolve(version.id)
                    .resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + ".jar");

            if (Files.notExists(downloadTarget)) {
                if (!this.installer.getLauncherConfig().autoDownloadLibraries) {
                    throw new IOException(
                            String.format("The Minecraft jar is not located at '%s' and downloading it has been turned off.", downloadTarget));
                }
                InstallerUtils
                        .downloadCheckHash(version.downloads.server.url, downloadTarget, MessageDigest.getInstance("SHA-1"),
                                version.downloads.server.sha1, false);
            } else {
                if (this.installer.getLauncherConfig().checkLibraryHashes) {
                    Logger.info("Detected existing Minecraft Server jar, verifying hashes...");
                    final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

                    // Pipe the download stream into the file and compute the SHA-1
                    final byte[] bytes = Files.readAllBytes(downloadTarget);
                    final String fileSha1 = InstallerUtils.toHexString(sha1.digest(bytes));

                    if (version.downloads.server.sha1.equals(fileSha1)) {
                        Logger.info("Minecraft Server jar verified!");
                    } else {
                        Logger.error("Checksum verification failed: Expected {}, {}. Deleting cached Minecraft Server jar...",
                                version.downloads.server.sha1, fileSha1);
                        Files.delete(downloadTarget);
                        InstallerUtils.downloadCheckHash(version.downloads.server.url, downloadTarget,
                                MessageDigest.getInstance("SHA-1"), version.downloads.server.sha1, false);
                    }
                } else {
                    Logger.info("Detected existing Minecraft jar. Skipping hash check as that is turned off...");
                }
            }
            return downloadTarget;
        }, this.installer.getLibraryManager().preparationWorker());
    }

    private CompletableFuture<Path> downloadMappings(final Version version, final Path librariesDirectory) {
        return AsyncUtils.asyncFailableFuture(() -> {
            Logger.info("Setting up names for Minecraft {}", Constants.Libraries.MINECRAFT_VERSION_TARGET);
            final Path downloadTarget = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_MAPPINGS_PREFIX)
                    .resolve(Constants.Libraries.MINECRAFT_VERSION_TARGET)
                    .resolve(Constants.Libraries.MINECRAFT_MAPPINGS_NAME);
            if (Files.notExists(downloadTarget)) {
                final Version.Downloads.Download mappings = version.downloads.server_mappings;
                if (mappings == null) {
                    throw new IOException(String.format("Mappings were not included in version manifest for %s", Constants.Libraries.MINECRAFT_VERSION_TARGET));
                }
                // TODO Figure out how to sha1 check the zip file
                if (this.installer.getLauncherConfig().autoDownloadLibraries) {
                    InstallerUtils.download(mappings.url, downloadTarget, false);
                } else {
                    throw new IOException(String.format("Mappings were not located at '%s' and downloading them has been turned off.", downloadTarget));
                }
            } else {
                Logger.info("Detected existing mappings, verifying hashes...");
                // TODO Figure out how to sha1 check the zip file
                Logger.info("mappings verified!");
            }

            return downloadTarget;
        }, this.installer.getLibraryManager().preparationWorker());
    }

    private Path remapMinecraft(final Path librariesDirectory, final Path inputJar, final Path serverMappings, final ExecutorService service) throws IOException {
        Logger.info("Checking if we need to remap Minecraft...");
        final Path outputJar = inputJar.resolveSibling(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + "_remapped.jar");

        if (Files.exists(outputJar)) {
            Logger.info("Remapped Minecraft detected, skipping...");
            return outputJar;
        }

        Logger.info("Remapping Minecraft. This may take a while...");
        final MappingSet mappings = MappingSet.create();
        try (final BufferedReader reader = Files.newBufferedReader(serverMappings, StandardCharsets.UTF_8)) {
            new ProGuardReader(reader).read().reverse(mappings);
        }

        try (final Atlas atlas = new Atlas(service)) {
            atlas.install(ctx -> new JarEntryRemappingTransformer(new LorenzRemapper(mappings, ctx.inheritanceProvider())) {
                @Override
                public JarClassEntry transform(final JarClassEntry entry) {
                    // Skip shaded classes that we know are non-obf
                    final String name = entry.getName();
                    if (name.startsWith("it/unimi")
                        || name.startsWith("com/google")
                        || name.startsWith("com/mojang/datafixers")
                        || name.startsWith("org/apache")) {
                        return entry;
                    }

                    return super.transform(entry);
                }
            });
            atlas.run(inputJar, outputJar);
        }

        return outputJar;
    }
}
