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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.common.SpongeCommon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DataPackSerializer<T extends DataPackEntry<T>> {

    private DataPackEncoder<T> encoder;
    private DataPackDecoder<T> decoder;

    public DataPackSerializer(final DataPackEncoder<T> encoder, final DataPackDecoder<T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public boolean serialize(final SpongeDataPack<T> pack, final Path packDir, final List<T> packEntries) throws IOException {
        if (!pack.persistent()) { // TODO persistence - reloadable types can now be saved at any time - which would delete all others...
            FileUtils.deleteDirectory(packDir.toFile());
        }

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

    public Path packEntryFile(final SpongeDataPack<T> pack, final ResourceKey key, final Path packDir) {
        return packDir.resolve("data")
                .resolve(key.namespace())
                .resolve(pack.dir())
                .resolve(key.value() + ".json");
    }

    protected void serializeObject(final SpongeDataPack<T> pack, final Path packDir, final T packEntry) throws IOException {
        final JsonElement serialized = this.encoder.encode(packEntry, SpongeCommon.server().registryAccess());
        final Path file = this.packEntryFile(pack, packEntry.key(), packDir);
        final JsonElement finalJson = this.transformSerialized(file, packEntry, serialized);
        Files.createDirectories(file.getParent());
        DataPackSerializer.writeFile(file, finalJson);
    }

    protected JsonElement transformSerialized(final Path file, final T entry, final JsonElement serialized) throws IOException {
        return serialized;
    }

    protected void serializeAdditional(final SpongeDataPack<T> type, Path packDir, T entry) throws IOException {
    }

    public T deserialize(final SpongeDataPack<T> pack, final Path file, final ResourceKey key) throws IOException {
        try (final InputStream stream = Files.newInputStream(file); final InputStreamReader reader = new InputStreamReader(stream)) {
            final JsonElement element = JsonParser.parseReader(reader);

            if (this.decoder != null) {
                // TODO this is actually blocking
                return this.decoder.decode(pack, key, element, SpongeCommon.server().registryAccess());
            }
        }
        return null;
    }

    public static void writeFile(final Path file, final JsonElement object) throws IOException {
        Files.deleteIfExists(file);
        try (BufferedWriter bufferedwriter = Files.newBufferedWriter(file)) {
            bufferedwriter.write(object.toString());
        }
    }

    public static void writePackMetadata(final SpongeDataPack<?> pack, final Path packDir, final boolean replace) throws IOException {
        // Write our pack metadata
        final Path packMeta = packDir.resolve("pack.mcmeta");
        if (replace || !Files.exists(packMeta)) {
            final JsonObject packDataRoot = new JsonObject();
            final JsonObject packData = new JsonObject();
            packDataRoot.add("pack", packData);
            packData.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion());
            packData.addProperty("description", pack.description());

            writeFile(packMeta, packDataRoot);
        }
    }

}
