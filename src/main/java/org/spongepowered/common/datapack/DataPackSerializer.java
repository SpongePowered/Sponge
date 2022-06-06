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
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.common.SpongeCommon;

public class DataPackSerializer<T extends DataPackEntry<T>> {

    protected boolean serialize(final SpongeDataPackType<T> type, final Path packDir, final List<T> packEntries) throws IOException {
        if (!type.persistent()) { // TODO persistence - reloadable types can now be saved at any time - which would delete all others...
            FileUtils.deleteDirectory(packDir.toFile());
        }

        if (packEntries.isEmpty()) {
            return false;
        }

        // Write our objects
        for (final T packEntry : packEntries) {
            this.serializeObject(type, packDir, packEntry);
            this.serializeAdditional(type, packDir, packEntry);
        }

        DataPackSerializer.writePackMetadata(type.name(), packDir);
        return true;
    }

    public Path packEntryFile(final SpongeDataPackType<T> type, final T packEntry, final Path packDir) {
        return packDir.resolve("data")
                .resolve(packEntry.key().namespace())
                .resolve(type.dir())
                .resolve(packEntry.key().value() + ".json");
    }

    protected void serializeObject(final SpongeDataPackType<T> type, final Path packDir, final T packEntry) throws IOException {
        final JsonElement serialized = type.encoder().encode(packEntry, SpongeCommon.server().registryAccess());
        final Path file = this.packEntryFile(type, packEntry, packDir);
        final JsonElement finalJson = this.transformSerialized(file, packEntry, serialized);
        Files.createDirectories(file.getParent());
        DataPackSerializer.writeFile(file, finalJson);
    }

    protected JsonElement transformSerialized(final Path file, final T entry, final JsonElement serialized) throws IOException {
        return serialized;
    }

    protected void serializeAdditional(final SpongeDataPackType<T> type, Path packDir, T entry) throws IOException {
    }

    public static void writePackMetadata(final String token, final Path directory) throws IOException {
        // Write our pack metadata
        final Path packMeta = directory.resolve("pack.mcmeta");
        Files.deleteIfExists(packMeta);
        final JsonObject packDataRoot = new JsonObject();
        final JsonObject packData = new JsonObject();
        packDataRoot.add("pack", packData);
        packData.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion());
        packData.addProperty("description", "Sponge plugin provided " + token);

        DataPackSerializer.writeFile(packMeta, packDataRoot);
    }

    public static void writeFile(final Path file, final JsonElement object) throws IOException {
        Files.deleteIfExists(file);

        try (BufferedWriter bufferedwriter = Files.newBufferedWriter(file)) {
            bufferedwriter.write(object.toString());
        }
    }

}
