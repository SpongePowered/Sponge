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

import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourcePackType;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.meta.MetaParseException;
import org.spongepowered.api.resource.meta.MetaSection;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.api.resource.pack.PackType;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public abstract class AbstractSpongePack implements Pack {

    protected final Path rootPath;

    protected AbstractSpongePack(Path rootPath) {
        this.rootPath = rootPath;
    }

    private String resolvePath(PackType type, ResourcePath path) {
        String directoryName = ((ResourcePackType) (Object) type).getDirectoryName();
        return String.format("%s/%s/%s", directoryName, path.getNamespace(), path.getPath());
    }

    @Override
    public InputStream openStream(PackType type, ResourcePath path) throws IOException {
        return openStream(resolvePath(type, path));
    }

    protected abstract InputStream openStream(String path) throws IOException;

    @Override
    public boolean exists(PackType type, ResourcePath path) {
        return exists(resolvePath(type, path));
    }

    protected abstract boolean exists(String path);

    @Override
    public <T> Optional<T> getMetadata(MetaSection<T> section) throws IOException, MetaParseException {
        try (InputStream inputstream = this.openStream("pack.mcmeta")) {
            return readMetadata(section, inputstream);
        }
    }

    public static <T> Optional<T> readMetadata(MetaSection<T> section, InputStream inputStream) {
        DataView view;
        try {
            // metadata is always json
            view = DataFormats.JSON.get().readFrom(inputStream);
        } catch (IOException e) {
            SpongeCommon.getLogger().error("Couldn't read {} metadata", section.getClass().getName(), e);
            return Optional.empty();
        }

        if (!view.contains(section.getQuery())) {
            return Optional.empty();
        }

        try {
            return view.getView(section.getQuery()).map(section::deserialize);
        } catch (JsonParseException | MetaParseException e) {
            SpongeCommon.getLogger().error("Couldn't load {} metadata", section.getClass().getName(), e);
            return Optional.empty();
        }
    }

}
