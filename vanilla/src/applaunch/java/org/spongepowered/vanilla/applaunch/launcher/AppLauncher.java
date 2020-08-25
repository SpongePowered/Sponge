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
package org.spongepowered.vanilla.applaunch.launcher;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.cadixdev.atlas.Atlas;
import org.cadixdev.bombe.jar.asm.JarEntryRemappingTransformer;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.asm.LorenzRemapper;
import org.cadixdev.lorenz.io.MappingFormats;
import org.spongepowered.vanilla.applaunch.Constants;
import org.spongepowered.vanilla.applaunch.VanillaCommandLine;
import org.spongepowered.vanilla.applaunch.launcher.model.Version;
import org.spongepowered.vanilla.applaunch.launcher.model.VersionManifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
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

public final class AppLauncher {

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public static void main(final String[] args) throws Exception {
        new AppLauncher().run(args);
    }

    public void run(final String[] args) throws Exception {
        VanillaCommandLine.configure(args);

        System.out.println("Checking libraries, please wait...");
        this.downloadMinecraft(VanillaCommandLine.librariesDirectory);
        final Path srgZip = this.downloadSRG(VanillaCommandLine.librariesDirectory);
        this.remapMinecraft(VanillaCommandLine.librariesDirectory, srgZip);

        final String javaHome = System.getProperty("java.home");
        final String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        final Path srgJar = VanillaCommandLine.librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX)
                .resolve(Constants.Libraries.MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME +
                        "_remapped.jar").toAbsolutePath();
        final Path mixinJar = VanillaCommandLine.librariesDirectory.resolve("org/spongepowered/mixin/mixin.jar");
        final Path accessTransformersJar = VanillaCommandLine.librariesDirectory.resolve("net/minecraftforge/accesstransformer/accesstransformers"
                + ".jar");
        classpath = classpath + ";" + srgJar.toString() + ";" + mixinJar.toAbsolutePath().toString() + ";" + accessTransformersJar.toAbsolutePath()
                        .toString();

        final String className = "org.spongepowered.vanilla.applaunch.Main";
        final List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5100");
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(Arrays.asList(VanillaCommandLine.RAW_ARGS));
        final ProcessBuilder builder = new ProcessBuilder(command);
        final Process process = builder.inheritIO().start();
        process.waitFor();
    }

    private void downloadMinecraft(final Path librariesDirectory) throws IOException, NoSuchAlgorithmException {
        System.out.println("Downloading the versions manifest...");

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
            this.downloadCheckHash(version.downloads.server.url, downloadTarget, version.downloads.server.sha1);
        } else {
            if (VanillaCommandLine.checkMinecraftJarHash) {
                System.out.println("Detected existing Minecraft Server jar, verifying hashes...");
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

                // Pipe the download stream into the file and compute the SHA-1
                final byte[] bytes = Files.readAllBytes(downloadTarget);
                final String fileSha1 = this.toHexString(sha1.digest(bytes));

                if (version.downloads.server.sha1.equals(fileSha1)) {
                    System.out.println("Minecraft Server jar verified!");
                } else {
                    Files.delete(downloadTarget);
                    System.err.println(String.format("Checksum verification failed: Expected %s, %s. Deleting cached Minecraft Server jar...",
                            version.downloads.server.sha1, fileSha1));
                }
            } else {
                System.out.println("Detected existing Minecraft Server jar. Skipping hash check as that is turned off...");
            }
        }
    }

    private Path downloadSRG(final Path librariesDirectory) throws IOException {
        System.out.println(String.format("Setting up MCP config for Minecraft %s", Constants.Libraries.MINECRAFT_VERSION_TARGET));
        final Path downloadTarget = librariesDirectory.resolve(Constants.Libraries.MCP_CONFIG_PATH_PREFIX).resolve(Constants.Libraries
                .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MCP_CONFIG_NAME + "-" + Constants.Libraries
                .MINECRAFT_VERSION_TARGET + ".zip");
        if (Files.notExists(downloadTarget)) {
            final URL mcpConfigUrl = new URL(Constants.Libraries.MCP_CONFIG_PREFIX_URL + "/" + Constants.Libraries
                    .MINECRAFT_VERSION_TARGET + "/" + Constants.Libraries.MCP_CONFIG_NAME + "-" + Constants.Libraries
                    .MINECRAFT_VERSION_TARGET + ".zip");
            // TODO Figure out how to sha1 check the zip file
            if (VanillaCommandLine.downloadSrgMappings) {
                this.download(mcpConfigUrl, downloadTarget);
            } else {
                throw new IOException(String.format("MCP config was not located at '%s' and downloading it has been turned off.", downloadTarget));
            }
        } else {
            System.out.println("Detected existing MCP mappings, verifying hashes...");
            // TODO Figure out how to sha1 check the zip file
            System.out.println("MCP mappings verified!");
        }

        return downloadTarget;
    }

    private void remapMinecraft(final Path librariesDirectory, final Path srgZip) throws IOException {
        System.out.println("Checking if we need to remap Minecraft...");
        final Path inputJar = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(Constants.Libraries
                .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + ".jar");
        final Path outputJar = librariesDirectory.resolve(Constants.Libraries.MINECRAFT_PATH_PREFIX).resolve(Constants.Libraries
                .MINECRAFT_VERSION_TARGET).resolve(Constants.Libraries.MINECRAFT_SERVER_JAR_NAME + "_" + "remapped.jar");

        if (Files.exists(outputJar)) {
            System.out.println("Remapped Minecraft detected, skipping...");
            return;
        }

        System.out.println("Remapping Minecraft to SRG. This may take a while...");
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
    private void download(final URL url, final Path path) throws IOException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        System.out.println("Downloading " + name + ". This could take a while...");
        System.out.println(String.format("URL -> <%s>", url));

        // Pipe the download stream into the file and compute the SHA-1
        try (final ReadableByteChannel in = Channels.newChannel(url.openStream()); final FileChannel out = FileChannel.open(path,
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

        System.out.println("Downloading " + name + ". This could take a while...");
        System.out.println(String.format("URL -> <%s>", url));

        final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        // Pipe the download stream into the file and compute the SHA-1
        try (final DigestInputStream stream = new DigestInputStream(url.openStream(), sha1); final ReadableByteChannel in = Channels
                .newChannel(stream); final FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            out.transferFrom(in, 0, Long.MAX_VALUE);
        }

        final String fileSha1 = this.toHexString(sha1.digest());

        if (VanillaCommandLine.checkMinecraftJarHash) {
            if (expected.equals(fileSha1)) {
                System.out.println("Successfully downloaded " + name + " and verified checksum!");
            } else {
                Files.delete(path);
                throw new IOException("Checksum verification failed: Expected " + expected + ", got " + fileSha1);
            }
        } else {
            System.out.println("Skipping hash check as that is turned off...");
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
