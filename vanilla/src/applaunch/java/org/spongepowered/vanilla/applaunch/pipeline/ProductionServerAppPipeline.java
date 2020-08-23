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
package org.spongepowered.vanilla.applaunch.pipeline;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.jar.asm.JarEntryRemappingTransformer;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.asm.LorenzRemapper;
import org.cadixdev.lorenz.io.MappingFormats;
import org.spongepowered.vanilla.applaunch.VanillaCommandLine;
import org.spongepowered.vanilla.applaunch.pipeline.model.Version;
import org.spongepowered.vanilla.applaunch.pipeline.model.VersionManifest;

import java.io.IOException;
import java.io.InputStreamReader;
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

public final class ProductionServerAppPipeline extends AppPipeline {

    public static final String MINECRAFT_VERSION_TARGET = "1.14.4";
    public static final String MINECRAFT_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String MINECRAFT_PATH_PREFIX = "net/minecraft/server";
    public static final String MINECRAFT_SERVER_JAR_NAME = "minecraft_server";
    public static final String MCP_CONFIG_NAME = "mcp_config";
    public static final String MCP_CONFIG_PREFIX_URL = "https://files.minecraftforge.net/maven/de/oceanlabs/mcp/" + MCP_CONFIG_NAME;
    public static final String MCP_CONFIG_PATH_PREFIX = "de/oceanlabs/mcp/" + MCP_CONFIG_NAME;
    public static final String MCP_JOINED_PATH = "config/joined.tsrg";

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    @Override
    public void prepare() throws Exception {
        super.prepare();

        this.downloadMinecraft(VanillaCommandLine.LIBRARIES_DIRECTORY);
        final Path srgZip = this.downloadSRG(VanillaCommandLine.LIBRARIES_DIRECTORY);
        this.remapMinecraft(VanillaCommandLine.LIBRARIES_DIRECTORY, srgZip);
    }

    private void downloadMinecraft(final Path librariesDirectory) throws IOException, NoSuchAlgorithmException {
        this.logger.info("Downloading the versions manifest...");

        VersionManifest.Version foundVersionManifest = null;

        final Gson gson = new Gson();
        try (final JsonReader reader = new JsonReader(new InputStreamReader(new URL(ProductionServerAppPipeline.MINECRAFT_MANIFEST_URL)
                .openStream()))) {
            final VersionManifest manifest = gson.fromJson(reader, VersionManifest.class);
            for (final VersionManifest.Version version : manifest.versions) {
                if (ProductionServerAppPipeline.MINECRAFT_VERSION_TARGET.equals(version.id)) {
                    foundVersionManifest = version;
                    break;
                }
            }
        }

        if (foundVersionManifest == null) {
            throw new IOException(String.format("Failed to find version manifest for '%s'!",
                    ProductionServerAppPipeline.MINECRAFT_VERSION_TARGET));
        }

        Version version;

        try (final JsonReader reader = new JsonReader(new InputStreamReader(foundVersionManifest.url.openStream()))) {
            version = gson.fromJson(reader, Version.class);
        }

        if (version == null) {
            throw new IOException(String.format("Failed to download version information for '%s'!",
                    ProductionServerAppPipeline.MINECRAFT_VERSION_TARGET));
        }

        final Path downloadTarget = librariesDirectory.resolve(ProductionServerAppPipeline.MINECRAFT_PATH_PREFIX).resolve(
                ProductionServerAppPipeline.MINECRAFT_VERSION_TARGET).resolve(ProductionServerAppPipeline.MINECRAFT_SERVER_JAR_NAME + ".jar");

        if (Files.notExists(downloadTarget)) {
            this.downloadCheckHash(version.downloads.server.url, downloadTarget, version.downloads.server.sha1);
        } else {
            this.logger.info("Detected existing Minecraft Server jar, verifying hashes...");
            final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            // Pipe the download stream into the file and compute the SHA-1
            final byte[] bytes = Files.readAllBytes(downloadTarget);
            final String fileSha1 = this.toHexString(sha1.digest(bytes));

            if (version.downloads.server.sha1.equals(fileSha1)) {
                this.logger.info("Minecraft Server jar verified!");
            } else {
                Files.delete(downloadTarget);
                this.logger.error("Checksum verification failed: Expected {}, {}. Deleting cached Minecraft Server jar...",
                        version.downloads.server.sha1, fileSha1);
            }
        }
    }

    private Path downloadSRG(final Path librariesDirectory) throws IOException {
        this.logger.info("Setting up MCP config for Minecraft {}", ProductionServerAppPipeline.MINECRAFT_VERSION_TARGET);
        final Path targetPath = librariesDirectory.resolve(ProductionServerAppPipeline.MCP_CONFIG_PATH_PREFIX).resolve(ProductionServerAppPipeline
                        .MINECRAFT_VERSION_TARGET).resolve(ProductionServerAppPipeline.MCP_CONFIG_NAME + "-" + ProductionServerAppPipeline
                        .MINECRAFT_VERSION_TARGET + ".zip");
        if (Files.notExists(targetPath)) {
            final URL mcpConfigUrl = new URL(ProductionServerAppPipeline.MCP_CONFIG_PREFIX_URL + "/" + ProductionServerAppPipeline
                    .MINECRAFT_VERSION_TARGET + "/" + ProductionServerAppPipeline.MCP_CONFIG_NAME + "-" + ProductionServerAppPipeline
                    .MINECRAFT_VERSION_TARGET + ".zip");
            // TODO Figure out how to sha1 check the zip file
            this.download(mcpConfigUrl, targetPath);
        } else {
            this.logger.info("Detected existing MCP mappings, verifying hashes...");
            // TODO Figure out how to sha1 check the zip file
            this.logger.info("MCP mappings verified!");
        }

        return targetPath;
    }

    private void remapMinecraft(final Path librariesDirectory, final Path srgZip) throws IOException {
        this.logger.info("Checking if we need to remap Minecraft...");
        final Path inputJar = librariesDirectory.resolve(ProductionServerAppPipeline.MINECRAFT_PATH_PREFIX).resolve(ProductionServerAppPipeline
                .MINECRAFT_VERSION_TARGET).resolve(ProductionServerAppPipeline.MINECRAFT_SERVER_JAR_NAME + ".jar");
        final Path outputJar = librariesDirectory.resolve(ProductionServerAppPipeline.MINECRAFT_PATH_PREFIX).resolve(ProductionServerAppPipeline
                .MINECRAFT_VERSION_TARGET).resolve(ProductionServerAppPipeline.MINECRAFT_SERVER_JAR_NAME + "_" + "remapped.jar");

        if (Files.exists(outputJar)) {
            this.logger.info("Remapped Minecraft detected, skipping...");
            return;
        }

        this.logger.info("Remapping Minecraft to SRG. This may take a while...");
        try (final FileSystem fileSystem = FileSystems.newFileSystem(srgZip, null)) {
            final Path srgFile = fileSystem.getPath(ProductionServerAppPipeline.MCP_JOINED_PATH);
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
     * @param remote The file URL
     * @param path The local path
     * @throws IOException If there is a problem while downloading the file
     */
    private void download(final URL remote, final Path path) throws IOException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        this.logger.info("Downloading " + name + ". This could take a while...");
        this.logger.info("URL -> <{}>", remote);

        // Pipe the download stream into the file and compute the SHA-1
        try (final ReadableByteChannel in = Channels.newChannel(remote.openStream()); final FileChannel out = FileChannel.open(path,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            out.transferFrom(in, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Downloads a file and verify its digest.
     *
     * @param url The file URL
     * @param path The local path
     * @param expected The SHA-1 expected digest
     * @throws IOException If there is a problem while downloading the file
     * @throws NoSuchAlgorithmException Never because the JVM is required to support SHA-1
     */
    private void downloadCheckHash(final URL url, final Path path, final String expected) throws IOException, NoSuchAlgorithmException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        this.logger.info("Downloading " + name + ". This could take a while...");
        this.logger.info("URL -> <{}>", url);

        final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        // Pipe the download stream into the file and compute the SHA-1
        try (final DigestInputStream stream = new DigestInputStream(url.openStream(), sha1); final ReadableByteChannel in = Channels
                .newChannel(stream); final FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            out.transferFrom(in, 0, Long.MAX_VALUE);
        }

        final String fileSha1 = this.toHexString(sha1.digest());

        if (expected.equals(fileSha1)) {
            this.logger.info("Successfully downloaded " + name + " and verified checksum!");
        } else {
            Files.delete(path);
            throw new IOException("Checksum verification failed: Expected " + expected + ", got " + fileSha1);
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
