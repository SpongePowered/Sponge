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
package org.spongepowered.common.datapack;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.datapack.DataPackEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class DataPackSerializer<E, T extends DataPackEntry<T>> {

    protected DataPackEncoder<E, T> encoder;
    protected DataPackDecoder<E, T> decoder;

    public DataPackSerializer(final DataPackEncoder<E, T> encoder, final DataPackDecoder<E, T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public boolean serialize(final SpongeDataPack<E, T> pack, final Path packDir, final List<T> packEntries) throws IOException {
        if (packEntries.isEmpty()) {
            return false;
        }

        // Write our objects
        for (final T packEntry : packEntries) {
            this.serializeObject(pack, packDir, packEntry);
            this.serializeAdditional(pack, packDir, packEntry);
        }
        return true;
    }

    public Path packEntryFile(final SpongeDataPackType<?, T> packType, final ResourceKey key, final Path packDir) {
        final ResourceLocation loc = this.location(packType, key);
        return packDir.resolve("data")
                .resolve(loc.getNamespace())
                .resolve(loc.getPath());
    }

    public ResourceLocation location(final SpongeDataPackType<?, T> packType, final ResourceKey key) {
        return ResourceLocation.fromNamespaceAndPath(key.namespace(), packType.dir() + "/" + key.value() + this.fileEnding());
    }

    public abstract String fileEnding();

    public abstract void serializeObject(final SpongeDataPack<E, T> pack, final Path packDir, final T packEntry) throws IOException;

    protected abstract void serializeAdditional(final SpongeDataPack<E, T> type, Path packDir, T entry) throws IOException;

    public abstract T deserialize(final SpongeDataPack<E, T> pack, final Path file, final ResourceKey key) throws IOException;

    public abstract T deserialize(final SpongeDataPack<E, T> pack, final InputStream is, final ResourceKey key) throws IOException;



    public static void writePackMetadata(final SpongeDataPack<?, ?> pack, final Path packDir, final boolean replace) throws IOException {
        // Write our pack metadata
        final Path packMeta = packDir.resolve("pack.mcmeta");
        if (replace || !Files.exists(packMeta)) {
            final JsonObject packDataRoot = new JsonObject();
            final JsonObject packData = new JsonObject();
            packDataRoot.add("pack", packData);
            packData.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
            packData.addProperty("description", pack.description());

            JsonDataPackSerializer.writeFile(packMeta, packDataRoot);
        }
    }

}
