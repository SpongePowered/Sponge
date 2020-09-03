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
import org.apache.logging.log4j.Logger;
import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.jar.asm.JarEntryRemappingTransformer;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.asm.LorenzRemapper;
import org.cadixdev.lorenz.io.MappingFormats;
import org.spongepowered.vanilla.installer.model.mojang.Version;
import org.spongepowered.vanilla.installer.model.mojang.VersionManifest;
import org.spongepowered.vanilla.installer.model.sponge.Libraries;
import org.spongepowered.vanilla.installer.model.sponge.SonatypeResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class InstallerMain {

    static {
        System.setProperty("log4j.configurationFile", "log4j2_installer.xml");
    }

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private final Logger logger;

    public static void main(final String[] args) throws Exception {
        new InstallerMain().run(args);
    }

    public InstallerMain() {
        this.logger = LogManager.getLogger("Installer");
    }

    public void run(final String[] args) throws Exception {
        VanillaCommandLine.configure(args);

        this.logger.info("Scanning and verifying libraries in '{}'. Please wait, this may take a moment...", VanillaCommandLine.librariesDirectory);
        final List<Path> dependencies = this.downloadDependencies(VanillaCommandLine.librariesDirectory);
        final Path gameJar = this.downloadMinecraft(VanillaCommandLine.librariesDirectory);
        final Path srgZip = this.downloadSRG(VanillaCommandLine.librariesDirectory);
        this.remapMinecraft(VanillaCommandLine.librariesDirectory, srgZip);

        this.logger.info("Environment has been verified.");

        final String javaHome = System.getProperty("java.home");
        final String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        final StringBuilder builder = new StringBuilder(System.getProperty("java.class.path"));

        builder.append(";");

        for (final Path depFile : dependencies) {
            builder.append(depFile.toString()).append(";");
        }

        builder.append(gameJar).append(";");

        final String className = "org.spongepowered.vanilla.applaunch.Main";
        final List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(builder.toString());
        command.add(className);
        command.addAll(Arrays.asList(VanillaCommandLine.RAW_ARGS));

        this.logger.debug("Launching JVM with flags: '{}'", command);
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Process process = processBuilder.inheritIO().start();
        process.waitFor();
    }

    private List<Path> downloadDependencies(final Path librariesDirectory) throws IOException, NoSuchAlgorithmException {
        this.logger.info("Checking dependencies, please wait...");
        final Gson gson = new Gson();

        Libraries dependencies;
        try (final JsonReader reader = new JsonReader(new InputStreamReader(this.getClass().getResourceAsStream("/libraries.json")))) {
            dependencies = gson.fromJson(reader, Libraries.class);
        }

        final List<Path> downloadedDeps = new ArrayList<>();

        for (final Libraries.Dependency dependency : dependencies.dependencies) {
            final String groupPath = dependency.group.replace(".", "/");
            final Path depDirectory =
                    librariesDirectory.resolve(groupPath).resolve(dependency.module).resolve(dependency.version);
            Files.createDirectories(depDirectory);
            final Path depFile = depDirectory.resolve(dependency.module + "-" + dependency.version + ".jar");
            if (Files.exists(depFile)) {
                this.logger.info("Detected existing '{}', verifying hashes...", depFile);

                final MessageDigest md5 = MessageDigest.getInstance("MD5");

                // Pipe the download stream into the file and compute the SHA-1
                final byte[] bytes = Files.readAllBytes(depFile);
                final String fileMd5 = this.toHexString(md5.digest(bytes));

                if (dependency.md5.equals(fileMd5)) {
                    this.logger.info("'{}' verified!", depFile);
                } else {
                    this.logger.error("Checksum verification failed: Expected {}, {}. Deleting cached '{}'...",
                            dependency.md5, fileMd5, depFile);
                    Files.delete(depFile);

                    final SonatypeResponse response = this.getResponseFor(gson, dependency);

                    if (response.items.isEmpty()) {
                        this.logger.error("No data received from '{}'!", new URL(String.format(Constants.Libraries.SPONGE_NEXUS_DOWNLOAD_URL, dependency.md5, dependency.group,
                                dependency.module, dependency.version)));
                        continue;
                    }
                    final SonatypeResponse.Item item = response.items.get(0);
                    final URL url = item.downloadUrl;

                    this.downloadCheckHash(url, depFile, MessageDigest.getInstance("MD5"), item.checksum.md5, true);
                }
            } else {
                final SonatypeResponse response = this.getResponseFor(gson, dependency);

                if (response.items.isEmpty()) {
                    this.logger.error("No data received from '{}'!", new URL(String.format(Constants.Libraries.SPONGE_NEXUS_DOWNLOAD_URL, dependency.md5, dependency.group,
                            dependency.module, dependency.version)));
                    continue;
                }

                final SonatypeResponse.Item item = response.items.get(0);
                final URL url = item.downloadUrl;

                this.downloadCheckHash(url, depFile, MessageDigest.getInstance("MD5"), item.checksum.md5, true);
            }

            downloadedDeps.add(depFile);
        }

        return downloadedDeps;
    }

    private Path downloadMinecraft(final Path librariesDirectory) throws IOException, NoSuchAlgorithmException {
        this.logger.info("Downloading the Minecraft versions manifest...");

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
            if (!VanillaCommandLine.downloadMinecraftJar) {
                throw new IOException(
                        String.format("The Minecraft jar is not located at '%s' and downloading it has been turned off.", downloadTarget));
            }
            this.downloadCheckHash(version.downloads.server.url, downloadTarget, MessageDigest.getInstance("SHA-1"), version.downloads.server.sha1,
                    false);
        } else {
            if (VanillaCommandLine.checkMinecraftJarHash) {
                this.logger.info("Detected existing Minecraft Server jar, verifying hashes...");
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

                // Pipe the download stream into the file and compute the SHA-1
                final byte[] bytes = Files.readAllBytes(downloadTarget);
                final String fileSha1 = this.toHexString(sha1.digest(bytes));

                if (version.downloads.server.sha1.equals(fileSha1)) {
                    this.logger.info("Minecraft Server jar verified!");
                } else {
                    this.logger.error("Checksum verification failed: Expected {}, {}. Deleting cached Minecraft Server jar...",
                            version.downloads.server.sha1, fileSha1);
                    Files.delete(downloadTarget);
                    this.downloadCheckHash(version.downloads.server.url, downloadTarget, MessageDigest.getInstance("SHA-1"),
                            version.downloads.server.sha1, false);
                }
            } else {
                this.logger.info("Detected existing Minecraft Server jar. Skipping hash check as that is turned off...");
            }
        }

        return downloadTarget;
    }

    private Path downloadSRG(final Path librariesDirectory) throws IOException {
        this.logger.info("Setting up MCP config for Minecraft {}", Constants.Libraries.MINECRAFT_VERSION_TARGET);
        final Path downloadTarget = librariesDirectory.resolve(Constants.Libraries.MCP_CONFIG_PATH_PREFIX).resolve(Constants.Libraries
                .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MCP_CONFIG_NAME + "-" + Constants.Libraries
                .MINECRAFT_VERSION_TARGET + ".zip");
        if (Files.notExists(downloadTarget)) {
            final URL mcpConfigUrl = new URL(Constants.Libraries.MCP_CONFIG_PREFIX_URL + "/" + Constants.Libraries
                    .MINECRAFT_VERSION_TARGET + "/" + Constants.Libraries.MCP_CONFIG_NAME + "-" + Constants.Libraries
                    .MINECRAFT_VERSION_TARGET + ".zip");
            // TODO Figure out how to sha1 check the zip file
            if (VanillaCommandLine.downloadSrgMappings) {
                this.download(mcpConfigUrl, downloadTarget, false);
            } else {
                throw new IOException(String.format("MCP config was not located at '%s' and downloading it has been turned off.", downloadTarget));
            }
        } else {
            this.logger.info("Detected existing MCP mappings, verifying hashes...");
            // TODO Figure out how to sha1 check the zip file
            this.logger.info("MCP mappings verified!");
        }

        return downloadTarget;
    }

    private SonatypeResponse getResponseFor(final Gson gson, final Libraries.Dependency dependency) throws IOException {
        final URL requestUrl = new URL(String.format(Constants.Libraries.SPONGE_NEXUS_DOWNLOAD_URL, dependency.md5, dependency.group,
                dependency.module, dependency.version));

        final HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Sponge-Downloader");

        connection.connect();

        try (final JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()))) {
            return gson.fromJson(reader, SonatypeResponse.class);
        }
    }

    private void remapMinecraft(final Path librariesDirectory, final Path srgZip) throws IOException {
        this.logger.info("Checking if we need to remap Minecraft...");
        final Path inputJar = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(Constants.Libraries
                .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + ".jar");
        final Path outputJar = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(Constants.Libraries
                .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + "_" + "remapped.jar");

        if (Files.exists(outputJar)) {
            this.logger.info("Remapped Minecraft detected, skipping...");
            return;
        }

        this.logger.info("Remapping Minecraft to SRG. This may take a while...");
        try (final FileSystem fileSystem = FileSystems.newFileSystem(srgZip, null)) {
            final Path srgFile = fileSystem.getPath(Constants.Libraries.MCP_JOINED_PATH);
            final MappingSet mappings = new MappingSet();
            MappingFormats.TSRG.read(mappings, srgFile);
            final Atlas atlas = new Atlas();
            atlas.install(ctx -> new JarEntryRemappingTransformer(
                    new LorenzRemapper(mappings, ctx.inheritanceProvider())
            ));
            atlas.run(inputJar, outputJar);
        }
    }

    /**
     * Downloads a file.
     *
     * @param url The file URL
     * @param path The local path
     * @throws IOException If there is a problem while downloading the file
     */
    private void download(final URL url, final Path path, final boolean requiresRequest) throws IOException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        this.logger.info("Downloading {}. This may take a while...", name);
        this.logger.info("URL -> <{}>", url);

        if (!requiresRequest) {
            try (final ReadableByteChannel in = Channels.newChannel(url.openStream()); final FileChannel out = FileChannel.open(path,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        } else {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Sponge-Downloader");

            connection.connect();

            try (final ReadableByteChannel in = Channels.newChannel(connection.getInputStream()); final FileChannel out = FileChannel.open(path,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        }
    }

    /**
     * Downloads a file and verify its digest.
     *
     * @param url The file URL
     * @param path The local path
     * @param expected The SHA-1 expected digest
     * @throws IOException If there is a problem while downloading the file
     */
    private void downloadCheckHash(final URL url, final Path path, final MessageDigest digest, final String expected, boolean requiresRequest) throws IOException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        this.logger.info("Downloading {}. This may take a while...", name);
        this.logger.info("URL -> <{}>", url);

        if (!requiresRequest) {
            // Pipe the download stream into the file and compute the hash
            try (final DigestInputStream stream = new DigestInputStream(url.openStream(), digest); final ReadableByteChannel in = Channels
                    .newChannel(stream); final FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        } else {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Sponge-Downloader");

            connection.connect();

            // Pipe the download stream into the file and compute the hash
            try (final DigestInputStream stream = new DigestInputStream(connection.getInputStream(), digest); final ReadableByteChannel in = Channels
                    .newChannel(stream); final FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        }

        final String fileSha1 = this.toHexString(digest.digest());

        if (VanillaCommandLine.checkMinecraftJarHash) {
            if (expected.equals(fileSha1)) {
                this.logger.info("Successfully downloaded {} and verified checksum!", name);
            } else {
                Files.delete(path);
                throw new IOException(String.format("Checksum verification failed: Expected '%s', got '%s'.", expected, fileSha1));
            }
        } else {
            this.logger.info("Skipping hash check as that is turned off...");
        }
    }

    private String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
