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
package org.spongepowered.common.resource.pack;

import net.minecraft.resources.ResourcePackType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpongeFileSystemPack extends AbstractSpongePack {

    @Nullable
    private FileSystem fileSystem;

    protected SpongeFileSystemPack(Path rootPath) {
        super(rootPath);
    }

    /**
     * Creates a new filesystem from the root path lazily.
     *
     * @return The filesystem
     * @throws IOException If the filesystem could not be created
     */
    private FileSystem getFileSystem() throws IOException {
        if (fileSystem == null) {
            if (Files.isRegularFile(rootPath)) {
                fileSystem = FileSystems.newFileSystem(URI.create("jar:" + rootPath.toUri()), Collections.emptyMap());
            } else if (Files.isDirectory(rootPath)) {
                fileSystem = FileSystems.newFileSystem(rootPath.toUri(), Collections.emptyMap());
            } else {
                throw new IOException("Unsupported filesystem: " + rootPath);
            }
        }
        return fileSystem;
    }

    private Path getPackRoot(PackType type) throws IOException {
        return getFileSystem().getPath(((ResourcePackType) (Object) type).getDirectoryName());
    }

    private Path getFilePath(String path) throws IOException {
        return getFileSystem().getPath(path);
    }

    @Override
    protected InputStream openStream(String path) throws IOException {
        return Files.newInputStream(getFilePath(path));
    }

    @Override
    protected boolean exists(String path) {
        try {
            return Files.exists(getFilePath(path));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        try {
            return Files.list(getPackRoot(type))
                    .map(Path::getFileName)
                    .map(Object::toString)
                    .filter(s -> {
                        if (s.equals(s.toLowerCase(Locale.ROOT))) {
                            return true;
                        } else {
                            SpongeCommon.getLogger().warn("Pack: ignored non-lowercased namespace: {} in {}", s, this.rootPath);
                            return false;
                        }
                    })
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public void close() throws IOException {
        if (fileSystem != null) {
            fileSystem.close();
        }
    }

    @Override
    public Collection<ResourcePath> find(PackType type, String namespace, String prefix, int depth, Predicate<String> filter) {
        try {
            Path root = getPackRoot(type);
            return Files.walk(root.resolve(namespace).resolve(prefix), depth)
                    .filter(s -> !s.getFileName().toString().endsWith(".mcmeta"))
                    // todo is the path relative to start?
                    .map(Object::toString)
                    .filter(filter)
                    .map(s -> ResourcePath.of(namespace, s))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return "file:" + this.rootPath.getFileName().toString();
    }
}
