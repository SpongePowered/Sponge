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
import net.minecraft.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.common.datapack.tag.TagSerializedObject;
import org.spongepowered.common.tag.SpongeTagType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TagDataPackSerializer extends DataPackSerializer<TagSerializedObject> {
    public TagDataPackSerializer(String token, String typeDirectoryName) {
        super(token, typeDirectoryName);
    }

    @Override
    protected boolean serialize(final SpongeDataPackType<@NonNull ?, TagSerializedObject> type, final Path datapacksDir, final List<TagSerializedObject> objects, int count) throws IOException {
        final Path datapackDir = datapacksDir.resolve(this.getPackName());

        if (!type.persistent()) {
            FileUtils.deleteDirectory(datapackDir.toFile());
        }

        if (objects.isEmpty()) {
            return false;
        }

        // Write our objects
        for (final TagSerializedObject object : objects) {
            final Path namespacedDataDirectory = datapackDir.resolve("data").resolve(object.getKey().namespace());
            final String filename = object.getKey().value() + ".json";
            final Path objectFile = namespacedDataDirectory.resolve(this.typeDirectoryName).resolve(((SpongeTagType<?>) object.getTagType()).internalId()).resolve(filename);
            Files.createDirectories(objectFile.getParent());


            JsonObject toWrite = object.getObject();
            if (Files.exists(objectFile) && !object.getObject().getAsJsonPrimitive("replace").getAsBoolean()) {
                // Merge, baby merge.

                final JsonObject jsonObject;
                try (BufferedReader bufferedReader = Files.newBufferedReader(objectFile)) {
                    final JsonElement jsonElement = new JsonParser().parse(bufferedReader);
                    jsonObject = jsonElement.getAsJsonObject();
                }
                toWrite = Tag.Builder.tag().addFromJson(jsonObject, filename).addFromJson(object.getObject(), filename).serializeToJson();
            }
            DataPackSerializer.writeFile(objectFile, toWrite);

            this.serializeAdditional(namespacedDataDirectory, object);
        }

        DataPackSerializer.writePackMetadata(this.name, datapackDir);
        return true;
    }
}
