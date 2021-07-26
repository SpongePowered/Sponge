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
package org.spongepowered.fabric.installer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.spongepowered.fabric.installer.model.sponge.Libraries;
import org.spongepowered.fabric.installer.model.sponge.SonatypeResponse;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public final class LibraryManager {

    private final Installer installer;
    private final Path rootDirectory;
    private final Map<String, Library> libraries;
    private final ExecutorService preparationWorker;

    public LibraryManager(final Installer installer, final Path rootDirectory) {
        this.installer = installer;
        this.rootDirectory = rootDirectory;

        this.libraries = new LinkedHashMap<>();
        final int availableCpus = Runtime.getRuntime().availableProcessors();
        // We'll be performing mostly IO-blocking operations, so more threads will help us for now
        // It might make sense to make this overridable eventually
        this.preparationWorker = new ThreadPoolExecutor(
            Math.min(Math.max(4, availableCpus * 2), 64), Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>() // this is the number of tasks allowed to be waiting before the pool will spawn off a new thread (unbounded)
        );
    }

    public Path getRootDirectory() {
        return this.rootDirectory;
    }

    public Map<String, Library> getAll() {
        return Collections.unmodifiableMap(this.libraries);
    }

    protected void addLibrary(final Library library) {
        this.libraries.put(library.getName(), library);
    }

    public void validate() throws Exception {
        Logger.info("Scanning and verifying libraries in '{}'. Please wait, this may take a moment...",
            LauncherCommandLine.librariesDirectory.toAbsolutePath());

        final Gson gson = new Gson();

        final Libraries dependencies;
        try (final JsonReader reader = new JsonReader(new InputStreamReader(this.getClass().getResourceAsStream("/libraries.json")))) {
            dependencies = gson.fromJson(reader, Libraries.class);
        }

        final Set<Library> downloadedDeps = ConcurrentHashMap.newKeySet();
        final List<CompletableFuture<?>> operations = new ArrayList<>(dependencies.dependencies.size());
        final Set<String> failures = ConcurrentHashMap.newKeySet();

        for (final Libraries.Dependency dependency : dependencies.dependencies) {
            operations.add(AsyncUtils.asyncFailableFuture(() -> {
                final String groupPath = dependency.group.replace(".", "/");
                final Path depDirectory =
                    this.rootDirectory.resolve(groupPath).resolve(dependency.module).resolve(dependency.version);
                Files.createDirectories(depDirectory);
                final Path depFile = depDirectory.resolve(dependency.module + "-" + dependency.version + ".jar");
                final MessageDigest md5 = MessageDigest.getInstance("MD5");

                final boolean checkHashes = this.installer.getLauncherConfig().checkLibraryHashes;

                if (Files.exists(depFile)) {
                    if (!checkHashes) {
                        Logger.info("Detected existing '{}', skipping hash checks...", depFile);
                        downloadedDeps.add(new Library(dependency.group + "-" + dependency.module, depFile));
                        return null;
                    }

                    // Pipe the download stream into the file and compute the SHA-1
                    final byte[] bytes = Files.readAllBytes(depFile);
                    final String fileMd5 = InstallerUtils.toHexString(md5.digest(bytes));

                    if (dependency.md5.equals(fileMd5)) {
                        Logger.debug("'{}' verified!", depFile);
                    } else {
                        Logger.error("Checksum verification failed: Expected {}, {}. Deleting cached '{}'...",
                            dependency.md5, fileMd5, depFile);
                        Files.delete(depFile);

                        final SonatypeResponse response = this.getResponseFor(gson, dependency);

                        if (response.items.isEmpty()) {
                            failures.add("No data received from '" + new URL(String.format(Constants.Libraries.SPONGE_NEXUS_DOWNLOAD_URL,
                                dependency.md5, dependency.group,
                                dependency.module, dependency.version)) + "'!");
                            return null;
                        }
                        final SonatypeResponse.Item item = response.items.get(0);
                        final URL url = item.downloadUrl;

                        InstallerUtils.downloadCheckHash(url, depFile, md5, item.checksum.md5, true);
                    }
                } else {
                    final SonatypeResponse response = this.getResponseFor(gson, dependency);

                    if (response.items.isEmpty()) {
                        failures.add("No data received from '" + new URL(String.format(Constants.Libraries.SPONGE_NEXUS_DOWNLOAD_URL,
                            dependency.md5, dependency.group,
                            dependency.module, dependency.version)) + "'!");
                        return null;
                    }

                    final SonatypeResponse.Item item = response.items.get(0);
                    final URL url = item.downloadUrl;

                    if (checkHashes) {
                        InstallerUtils.downloadCheckHash(url, depFile, md5, item.checksum.md5, true);
                    } else {
                        InstallerUtils.download(url, depFile, true);
                    }
                }

                downloadedDeps.add(new Library(dependency.group + "-" + dependency.module, depFile));
                return null;
            }, this.preparationWorker));
        }


        CompletableFuture.allOf(operations.toArray(new CompletableFuture<?>[0])).handle((result, err) -> {
            if (err != null) {
                failures.add(err.getMessage());
                Logger.error(err, "Failed to download library");
            }
            return result;
        }).join();

        if (!failures.isEmpty()) {
            Logger.error("Failed to download some libraries:");
            for (final String message : failures) {
                Logger.error(message);
            }
            System.exit(-1);
        }

        for (final Library library : downloadedDeps) {
            this.libraries.put(library.getName(), library);
        }
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

    public ExecutorService preparationWorker() {
        return this.preparationWorker;
    }

    public void finishedProcessing() {
        if (this.preparationWorker.isTerminated()) {
            return;
        }

        this.preparationWorker.shutdown();
        boolean successful;
        try {
            successful = this.preparationWorker.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            successful = false;
        }

        if (!successful) {
            Logger.warn("Failed to shut down library preparation pool in 10 seconds, forcing shutdown now.");
            this.preparationWorker.shutdownNow();
        }
    }

    public static class Library {

        private final String name;
        private final Path file;

        public Library(final String name, final Path file) {
            this.name = name;
            this.file = file;
        }

        public String getName() {
            return this.name;
        }

        public Path getFile() {
            return this.file;
        }
    }
}
