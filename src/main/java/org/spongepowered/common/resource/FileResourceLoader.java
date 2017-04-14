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
package org.spongepowered.common.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourceLoader;
import org.spongepowered.api.resource.ResourceLocation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.persistence.JsonTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.annotation.Nullable;

public class FileResourceLoader implements ResourceLoader {

    private final FileSystem fs;
    private final Path base;

    public FileResourceLoader(Path base) {
        fs = FileSystems.getFileSystem(base.toUri());
        this.base = base;
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation location) {
        Path path = getPath(location);
        if (Files.exists(path)) {
            return Optional.of(new Resource() {

                @Nullable Optional<DataView> meta;

                @Override
                public ResourceLocation getResourceLocation() {
                    return location;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return Files.newInputStream(path, StandardOpenOption.READ);
                }

                @Override
                public ResourceLoader getResourceLoader() {
                    return FileResourceLoader.this;
                }

                @Override
                public Optional<DataView> getMetadata() {
                    if (meta == null) {
                        meta = Optional.empty();
                        Path metaPath = path.resolveSibling(path.getFileName() + ".mcmeta");
                        if (Files.exists(metaPath)) {
                            try (InputStream in = Files.newInputStream(metaPath, StandardOpenOption.READ)) {
                                JsonObject obj = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
                                meta = Optional.of(JsonTranslator.translateFrom(obj));
                                // TODO SpongeAPI#1363
                                // meta = DataFormats.JSON.readFrom(in);
                            } catch (IOException e) {
                                SpongeImpl.getLogger().warn("Unable to read metadata for {} from {}. ", location, base, e);
                            }
                        }
                    }
                    assert meta != null;
                    return meta;
                }
            });
        }
        return Optional.empty();
    }

    private Path getPath(ResourceLocation loc) {
        return fs.getPath("assets", loc.getDomain(), loc.getPath());
    }

}
