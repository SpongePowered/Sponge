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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.SharedConstants;

public class DataPackSerializer<T extends DataPackSerializedObject> {

    protected final String name;
    protected final String typeDirectoryName;

    public DataPackSerializer(final String token, final String typeDirectoryName) {
        this.name = token;
        this.typeDirectoryName = typeDirectoryName;
    }

    protected boolean serialize(final SpongeDataPackType<@NonNull ?, T> type, final Path datapacksDir, final List<T> objects, int count) throws IOException {
        final Path datapackDir = datapacksDir.resolve(this.getPackName());

        if (!type.persistent()) {
            FileUtils.deleteDirectory(datapackDir.toFile());
        }

        if (count == 0) {
            return false;
        }

        // Write our objects
        for (final T object : objects) {
            final Path namespacedDataDirectory = datapackDir.resolve("data").resolve(object.getKey().namespace());
            final Path objectFile = namespacedDataDirectory.resolve(this.typeDirectoryName).resolve(object.getKey().value() + ".json");
            Files.createDirectories(objectFile.getParent());

            DataPackSerializer.writeFile(objectFile, object.getObject());

            this.serializeAdditional(namespacedDataDirectory, object);
        }

        DataPackSerializer.writePackMetadata(this.name, datapackDir);
        return true;
    }

    protected void serializeAdditional(Path dataDirectory, T object) throws IOException {
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

    public String getPackName() {
        return "plugin_" + this.typeDirectoryName;
    }
}
