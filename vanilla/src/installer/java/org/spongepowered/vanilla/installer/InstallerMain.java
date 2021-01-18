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
import org.apache.logging.log4j.LogManager;
import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.asm.jar.JarEntryRemappingTransformer;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.asm.LorenzRemapper;
import org.cadixdev.lorenz.io.MappingFormats;
import org.spongepowered.vanilla.installer.model.mojang.Version;
import org.spongepowered.vanilla.installer.model.mojang.VersionManifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class InstallerMain {

    private static final Pattern CLASSPATH_SPLITTER = Pattern.compile(";", Pattern.LITERAL);

    static {
        final String log4jConfig = System.getProperty("log4j.configurationFile");
        if (log4jConfig == null || log4jConfig.isEmpty()) {
            System.setProperty("log4j.configurationFile", "log4j2_launcher.xml");
        }
    }

    private final Installer installer;

    public InstallerMain(final String[] args) throws Exception {
        LauncherCommandLine.configure(args);
        this.installer = new Installer(LogManager.getLogger("Installer"), LauncherCommandLine.installerDirectory);
    }

    public static void main(final String[] args) throws Exception {
        new InstallerMain(args).run();
    }

    public void run() throws Exception {
        this.installer.getLibraryManager().validate();

        this.downloadMinecraft(LauncherCommandLine.librariesDirectory);
        final Path srgZip = this.downloadSRG(LauncherCommandLine.librariesDirectory);
        final Path minecraftJar = this.remapMinecraft(LauncherCommandLine.librariesDirectory, srgZip);

        this.installer.getLibraryManager().addLibrary(new LibraryManager.Library("minecraft", minecraftJar));

        this.installer.getLogger().info("Environment has been verified.");

        final String javaBin = this.installer.getLauncherConfig().jvmDirectory.replace("${JAVA_HOME}", System.getProperty("java.home")) +
            File.separator + "bin" + File.separator + "java";
        final List<String> jvmArgs;
        if (!this.installer.getLauncherConfig().jvmArgs.isEmpty()) {
            jvmArgs = Arrays.asList(this.installer.getLauncherConfig().jvmArgs.split(" "));
        } else {
            jvmArgs = null;
        }
        final String depsClasspath = this.installer.getLibraryManager().getAll().values().stream().map(LibraryManager.Library::getFile).
            map(Path::toAbsolutePath).map(Path::normalize).map(Path::toString).collect(Collectors.joining(File.pathSeparator));
        final String launchClasspath = CLASSPATH_SPLITTER.splitAsStream(System.getProperty("java.class.path"))
                .map(it -> Paths.get(it).toAbsolutePath().toString())
                .collect(Collectors.joining(File.pathSeparator));
        final String classpath = launchClasspath + File.pathSeparator + depsClasspath +
                File.pathSeparator + minecraftJar.toAbsolutePath().normalize().toString();
        final List<String> gameArgs = Arrays.asList(this.installer.getLauncherConfig().args.split(" "));

        this.installer.getLogger().debug("Setting classpath to: " + classpath);

        final String className = "org.spongepowered.vanilla.applaunch.Main";
        final List<String> command = new ArrayList<>();
        command.add(javaBin);
        if (jvmArgs != null) {
            command.addAll(jvmArgs);
        }
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(gameArgs);

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Process process = processBuilder.inheritIO().start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                installer.getLogger().error("Waiting for server termination failed!", e);
            }
        }));
        process.waitFor();
    }

    private void downloadMinecraft(final Path librariesDirectory) throws Exception {
        this.installer.getLogger().info("Downloading the Minecraft versions manifest...");

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

        Version version;

        try (final JsonReader reader = new JsonReader(new InputStreamReader(foundVersionManifest.url.openStream()))) {
            version = gson.fromJson(reader, Version.class);
        }

        if (version == null) {
            throw new IOException(String.format("Failed to download version information for '%s'!",
                Constants.Libraries.MINECRAFT_VERSION_TARGET));
        }

        final Path downloadTarget = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(
            Constants.Libraries.MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + ".jar");

        if (Files.notExists(downloadTarget)) {
            if (!this.installer.getLauncherConfig().autoDownloadLibraries) {
                throw new IOException(
                    String.format("The Minecraft jar is not located at '%s' and downloading it has been turned off.", downloadTarget));
            }
            InstallerUtils
                .downloadCheckHash(this.installer.getLogger(), version.downloads.server.url, downloadTarget, MessageDigest.getInstance("SHA-1"),
                    version.downloads.server.sha1, false);
        } else {
            if (this.installer.getLauncherConfig().checkLibraryHashes) {
                this.installer.getLogger().info("Detected existing Minecraft Server jar, verifying hashes...");
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

                // Pipe the download stream into the file and compute the SHA-1
                final byte[] bytes = Files.readAllBytes(downloadTarget);
                final String fileSha1 = InstallerUtils.toHexString(sha1.digest(bytes));

                if (version.downloads.server.sha1.equals(fileSha1)) {
                    this.installer.getLogger().info("Minecraft Server jar verified!");
                } else {
                    this.installer.getLogger().error("Checksum verification failed: Expected {}, {}. Deleting cached Minecraft Server jar...",
                        version.downloads.server.sha1, fileSha1);
                    Files.delete(downloadTarget);
                    InstallerUtils.downloadCheckHash(this.installer.getLogger(), version.downloads.server.url, downloadTarget,
                        MessageDigest.getInstance("SHA-1"), version.downloads.server.sha1, false);
                }
            } else {
                this.installer.getLogger().info("Detected existing Minecraft jar. Skipping hash check as that is turned off...");
            }
        }
    }

    private Path downloadSRG(final Path librariesDirectory) throws IOException {
        this.installer.getLogger().info("Setting up MCP config for Minecraft {}", Constants.Libraries.MINECRAFT_VERSION_TARGET);
        final Path downloadTarget = librariesDirectory.resolve(Constants.Libraries.MCP_CONFIG_PATH_PREFIX).resolve(Constants.Libraries
            .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MCP_CONFIG_NAME + "-" + Constants.Libraries
            .MINECRAFT_VERSION_TARGET + ".zip");
        if (Files.notExists(downloadTarget)) {
            final URL mcpConfigUrl = new URL(Constants.Libraries.MCP_CONFIG_PREFIX_URL + "/" + Constants.Libraries.MINECRAFT_VERSION_TARGET
                + "/" + Constants.Libraries.MCP_CONFIG_NAME + "-" + Constants.Libraries.MINECRAFT_VERSION_TARGET + ".zip");
            // TODO Figure out how to sha1 check the zip file
            if (this.installer.getLauncherConfig().autoDownloadLibraries) {
                InstallerUtils.download(this.installer.getLogger(), mcpConfigUrl, downloadTarget, false);
            } else {
                throw new IOException(String.format("MCP config was not located at '%s' and downloading it has been turned off.", downloadTarget));
            }
        } else {
            this.installer.getLogger().info("Detected existing MCP mappings, verifying hashes...");
            // TODO Figure out how to sha1 check the zip file
            this.installer.getLogger().info("MCP mappings verified!");
        }

        return downloadTarget;
    }

    private Path remapMinecraft(final Path librariesDirectory, final Path srgZip) throws IOException {
        this.installer.getLogger().info("Checking if we need to remap Minecraft...");
        final Path inputJar = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(Constants.Libraries
            .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + ".jar");
        final Path outputJar = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(Constants.Libraries
            .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + "_" + "remapped.jar");

        if (Files.exists(outputJar)) {
            this.installer.getLogger().info("Remapped Minecraft detected, skipping...");
            return outputJar;
        }

        this.installer.getLogger().info("Remapping Minecraft to SRG. This may take a while...");
        try (final FileSystem fileSystem = FileSystems.newFileSystem(srgZip, null)) {
            final Path srgFile = fileSystem.getPath(Constants.Libraries.MCP_JOINED_PATH);
            final MappingSet mappings = MappingSet.create();
            MappingFormats.TSRG.read(mappings, srgFile);
            final Atlas atlas = new Atlas();
            atlas.install(ctx -> new JarEntryRemappingTransformer(
                new LorenzRemapper(mappings, ctx.inheritanceProvider())
            ));
            atlas.run(inputJar, outputJar);
        }

        return outputJar;
    }
}
