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
import org.spongepowered.bootstrap.forge.VanillaBootstrap;
import org.spongepowered.libs.LibraryManager;
import org.spongepowered.libs.LibraryUtils;
import org.spongepowered.vanilla.installer.library.TinyLogger;
import org.spongepowered.vanilla.installer.model.GroupArtifactVersion;
import org.spongepowered.vanilla.installer.model.mojang.BundleElement;
import org.spongepowered.vanilla.installer.model.mojang.BundlerMetadata;
import org.spongepowered.vanilla.installer.model.mojang.FormatVersion;
import org.spongepowered.vanilla.installer.model.mojang.Version;
import org.spongepowered.vanilla.installer.model.mojang.VersionManifest;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public final class InstallerMain {

    private static final String COLLECTION_BOOTSTRAP = "bootstrap"; // boot layer
    private static final String COLLECTION_MAIN = "main"; // game layer

    private final Installer installer;
    private final boolean isolated;

    public InstallerMain(final String[] args, final boolean isolated) throws Exception {
        LauncherCommandLine.configure(args);
        this.installer = new Installer(LauncherCommandLine.installerDirectory);
        this.isolated = isolated;
    }

    public static void main(final String[] args) throws Exception {
        new InstallerMain(args, true).run();
    }

    public void run() throws Exception {
        try  {
            this.downloadAndRun();
        } catch (final Exception ex) {
            Logger.error(ex, "Failed to download Sponge libraries and/or Minecraft");
            throw ex;
        } finally {
            this.installer.getLibraryManager().finishedProcessing();
        }
    }

    private void downloadAndRun() throws Exception {
        ServerAndLibraries extractedMinecraftJar = null;
        Version mcVersion = null;
        try {
            mcVersion = this.downloadMinecraftManifest();
        } catch (final IOException ex) {
            extractedMinecraftJar = this.recoverFromMinecraftDownloadError(ex);
        }

        final LibraryManager libraryManager = this.installer.getLibraryManager();
        try {
            if (mcVersion != null) {
                extractedMinecraftJar = this.downloadMinecraft(mcVersion).thenApplyAsync(this::extractBundle, libraryManager.preparationWorker()).get();
            }
        } catch (final ExecutionException ex) {
            final /* @Nullable */ Throwable cause = ex.getCause();
            extractedMinecraftJar = this.recoverFromMinecraftDownloadError(cause instanceof Exception ? (Exception) cause : ex);
        }
        assert extractedMinecraftJar != null; // always assigned or thrown

        libraryManager.validate();

        // Minecraft itself is on the main layer
        libraryManager.addLibrary(InstallerMain.COLLECTION_MAIN, new LibraryManager.Library("minecraft", extractedMinecraftJar.server()));

        // Other libs are on the bootstrap layer
        for (final Map.Entry<GroupArtifactVersion, Path> entry : extractedMinecraftJar.libraries().entrySet()) {
            final GroupArtifactVersion artifact = entry.getKey();
            final Path path = entry.getValue();

            libraryManager.addLibrary(InstallerMain.COLLECTION_BOOTSTRAP, new LibraryManager.Library(artifact.toString(), path));
        }

        if (!this.isolated) {
            // JaCoCo core is provided by the user because its version must match the version of the JaCoCo agent
            Path jacocoJar = null;
            try {
                final Class<?> jacocoClass = getClass().getClassLoader().loadClass("org.jacoco.core.JaCoCo");
                jacocoJar = Path.of(jacocoClass.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (final Exception ignored) {}

            if (jacocoJar != null && jacocoJar.getFileName().toString().endsWith(".jar")) {
                Logger.info("JaCoCo core has been detected. Custom instrumentation will be enabled.");
                libraryManager.addLibrary(InstallerMain.COLLECTION_BOOTSTRAP, new LibraryManager.Library("jacoco-core", jacocoJar));
            }
        }

        libraryManager.finishedProcessing();

        Logger.info("Environment has been verified.");

        final Set<String> seenLibs = new HashSet<>();
        final Path[] bootLibs = libraryManager.getAll(InstallerMain.COLLECTION_BOOTSTRAP).stream()
            .peek(lib -> seenLibs.add(lib.name()))
            .map(LibraryManager.Library::file)
            .toArray(Path[]::new);

        final Path[] gameLibs = libraryManager.getAll(InstallerMain.COLLECTION_MAIN).stream()
            .filter(lib -> !seenLibs.contains(lib.name()))
            .map(LibraryManager.Library::file)
            .toArray(Path[]::new);

        final URL rootJar = InstallerMain.class.getProtectionDomain().getCodeSource().getLocation();
        final URI fsURI = new URI("jar:" + rootJar);
        System.setProperty("sponge.rootJarFS", fsURI.toString());

        final FileSystem fs = FileSystems.newFileSystem(fsURI, Map.of());
        final Path spongeBoot = newJarInJar(fs.getPath("jars", "spongevanilla-boot.jar"));

        String launchTarget = LauncherCommandLine.launchTarget;
        if (launchTarget == null) {
            final Path manifestFile = fs.getPath("META-INF", "MANIFEST.MF");
            try (final InputStream stream = Files.newInputStream(manifestFile)) {
                final Manifest manifest = new Manifest(stream);
                launchTarget = manifest.getMainAttributes().getValue(Constants.ManifestAttributes.LAUNCH_TARGET);
            }
        }

        final StringJoiner resourcesEnv = new StringJoiner(File.pathSeparator);
        for (final Path lib : gameLibs) {
            resourcesEnv.add(lib.toAbsolutePath().toString());
        }
        // Kept after ours: what the launch already asked for, like a plugin's libraries on a run without its jar
        final String launchResources = System.getProperty("sponge.resources");
        if (launchResources != null && !launchResources.isBlank()) {
            resourcesEnv.add(launchResources);
        }
        System.setProperty("sponge.resources", resourcesEnv.toString());

        final List<String> gameArgs = new ArrayList<>(LauncherCommandLine.remainingArgs);
        gameArgs.add("--launchTarget");
        gameArgs.add(launchTarget);
        Collections.addAll(gameArgs, this.installer.getConfig().args().split(" "));

        this.bootstrap(bootLibs, spongeBoot, gameArgs.toArray(new String[0]));
    }

    private static Path newJarInJar(final Path jar) {
        try {
            URI jij = new URI("jij:" + jar.toAbsolutePath().toUri().getRawSchemeSpecificPart()).normalize();
            final Map<String, ?> env = Map.of("packagePath", jar);
            FileSystem jijFS = FileSystems.newFileSystem(jij, env);
            return jijFS.getPath("/"); // root of the archive to load
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Throwable> ServerAndLibraries recoverFromMinecraftDownloadError(final T ex) throws T {
        final Path expectedUnpacked = this.expectedMinecraftLocation(Constants.Libraries.MINECRAFT_VERSION_TARGET);
        // Re-read bundler metadata (needs original bundled location)
        if (Files.exists(expectedUnpacked)) {
            Logger.warn(ex, "Failed to download Minecraft. An existing jar exists, so we will attempt to use that instead.");
            return this.extractBundle(this.expectedBundleLocation(expectedUnpacked));
        } else {
            throw ex;
        }
    }

    private void bootstrap(final Path[] bootLibs, final Path spongeBoot, final String[] args) throws Exception {
        final List<Path[]> classpath = new ArrayList<>();
        for (final Path lib : bootLibs) {
            classpath.add(new Path[] { lib });
        }
        classpath.add(new Path[] { spongeBoot });

        try {
            new VanillaBootstrap(args).boot(classpath, this.isolated);
        } catch (final Exception ex) {
            Logger.error(ex, "Failed to invoke bootstrap due to an error");
            throw ex;
        }
    }

    private Version downloadMinecraftManifest() throws Exception {
        Logger.info("Downloading the Minecraft versions manifest...");

        VersionManifest.Version foundVersionManifest = null;

        final Gson gson = new Gson();
        final URLConnection conn = new URI(Constants.Libraries.MINECRAFT_MANIFEST_URL).toURL().openConnection();
        conn.setConnectTimeout(5 /* seconds */ * 1000);
        try (final JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()))) {
            final VersionManifest manifest = gson.fromJson(reader, VersionManifest.class);
            for (final VersionManifest.Version version : manifest.versions()) {
                if (Constants.Libraries.MINECRAFT_VERSION_TARGET.equals(version.id())) {
                    foundVersionManifest = version;
                    break;
                }
            }
        }

        if (foundVersionManifest == null) {
            throw new IOException(String.format("Failed to find version manifest for '%s'!", Constants.Libraries.MINECRAFT_VERSION_TARGET));
        }

        final Version version;
        try (final JsonReader reader = new JsonReader(new InputStreamReader(foundVersionManifest.url().openStream()))) {
            version = gson.fromJson(reader, Version.class);
        }

        if (version == null) {
            throw new IOException(String.format("Failed to download version information for '%s'!", Constants.Libraries.MINECRAFT_VERSION_TARGET));
        }

        return version;
    }

    private Path expectedMinecraftLocation(final String version) {
        return this.installer.getLibraryManager().getRootDirectory().resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX)
            .resolve(version)
            .resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + ".jar");
    }

    private Path expectedBundleLocation(final Path originalLocation) {
        return originalLocation.resolveSibling(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + "-bundle.jar");
    }

    private CompletableFuture<Path> downloadMinecraft(final Version version) {
        return LibraryUtils.asyncFailableFuture(() -> {
            final Path downloadTarget = this.expectedBundleLocation(this.expectedMinecraftLocation(version.id()));
            final Version.Downloads.Download server = version.downloads().server();

            if (Files.notExists(downloadTarget)) {
                if (!this.installer.getConfig().autoDownloadLibraries()) {
                    throw new IOException(String.format("The Minecraft jar is not located at '%s' and downloading it has been turned off.", downloadTarget));
                }
                LibraryUtils.downloadAndVerifyDigest(TinyLogger.INSTANCE, server.url(), downloadTarget, "SHA-1", server.sha1());
            } else {
                if (this.installer.getConfig().checkLibraryHashes()) {
                    Logger.info("Detected existing Minecraft Server jar, verifying hashes...");

                    // Pipe the download stream into the file and compute the SHA-1
                    if (LibraryUtils.validateDigest("SHA-1", server.sha1(), downloadTarget)) {
                        Logger.info("Minecraft Server jar verified!");
                    } else {
                        Logger.error("Checksum verification failed: Expected {}. Deleting cached Minecraft Server jar...", server.sha1());
                        Files.delete(downloadTarget);
                        LibraryUtils.downloadAndVerifyDigest(TinyLogger.INSTANCE, server.url(), downloadTarget, "SHA-1", server.sha1());
                    }
                } else {
                    Logger.info("Detected existing Minecraft jar. Skipping hash check as that is turned off...");
                }
            }
            return downloadTarget;
        }, this.installer.getLibraryManager().preparationWorker());
    }

    private ServerAndLibraries extractBundle(final Path bundleJar) {
        final Path serverDestination = this.expectedMinecraftLocation(Constants.Libraries.MINECRAFT_VERSION_TARGET);
        try (final JarFile bundle = new JarFile(bundleJar.toFile())) {
            final Optional<BundlerMetadata> metaOpt = BundlerMetadata.read(bundle);
            if (metaOpt.isEmpty()) {
                return new ServerAndLibraries(bundleJar, Map.of());
            }
            final BundlerMetadata md = metaOpt.get();
            // Check version
            if (!md.version().equals(new FormatVersion(1, 0))) {
                Logger.warn("Read bundler metadata from server jar with version {}, but we only support 1.0", md.version());
            }

            // Extract server
            boolean serverExtractionNeeded = true;
            final BundleElement server = md.server();
            if (Files.exists(serverDestination)) {
                if (LibraryUtils.validateDigest("SHA-256", server.sha256(), serverDestination)) {
                    // library is valid
                    serverExtractionNeeded = false;
                }
            }
            if (serverExtractionNeeded) {
                final ZipEntry serverEntry = bundle.getEntry(server.path());
                try (final InputStream in = bundle.getInputStream(serverEntry)) {
                    LibraryUtils.transferAndVerifyDigest(TinyLogger.INSTANCE, in, serverDestination, "SHA-256", server.sha256());
                }
            }

            // Extract libraries
            final Path libsDir = this.installer.getLibraryManager().getRootDirectory();
            final Map<GroupArtifactVersion, Path> libs = new HashMap<>();
            for (final BundleElement library : md.libraries()) {
                final GroupArtifactVersion gav = GroupArtifactVersion.parse(library.id());
                final Path destination = gav.resolve(libsDir).resolve(gav.artifact() + '-' + gav.version() + (gav.classifier() == null ? "" : '-' + gav.classifier()) +".jar");

                if (Files.exists(destination)) {
                    if (LibraryUtils.validateDigest("SHA-256", library.sha256(), destination)) {
                       // library is valid
                       libs.put(gav, destination);
                       continue;
                    }
                }

                final ZipEntry entry = bundle.getEntry(library.path());
                try (final InputStream in = bundle.getInputStream(entry)) {
                    LibraryUtils.transferAndVerifyDigest(TinyLogger.INSTANCE, in, destination, "SHA-256", library.sha256());
                    libs.put(gav, destination);
                }
            }

            return new ServerAndLibraries(serverDestination, libs);
        } catch (final IOException | NoSuchAlgorithmException ex) {
            Logger.error(ex, "Failed to extract bundle from {}", bundleJar);
            throw new RuntimeException(ex);
        }
    }

    record ServerAndLibraries(Path server, Map<GroupArtifactVersion, Path> libraries) {
        ServerAndLibraries {
            libraries = Map.copyOf(libraries);
        }
    }
}
