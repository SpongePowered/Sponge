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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.datapack.tag.TagSerializedObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class TagDataPackSerializer extends DataPackSerializer<TagSerializedObject> {

    private final static class Holder {
        static final Map<String, String> REGISTRY_TYPES_TO_DIRECTORY =
            Map.ofEntries(
                    Map.entry(Registry.BLOCK_REGISTRY.location().getPath(), "blocks"),
                    Map.entry(Registry.ENTITY_TYPE_REGISTRY.location().getPath(), "entity_types"),
                    Map.entry(Registry.FLUID_REGISTRY.location().getPath(), "fluids"),
                    Map.entry(Registry.GAME_EVENT_REGISTRY.location().getPath(), "game_events"),
                    Map.entry(Registry.ITEM_REGISTRY.location().getPath(), "items")
                    // TODO: Functions don't seem to be in a registry?
            );
    }

    public TagDataPackSerializer(final String token, final String typeDirectoryName) {
        super(token, typeDirectoryName);
    }

    private String tagTypeName(final RegistryType<?> registryType) {
        return TagDataPackSerializer.Holder.REGISTRY_TYPES_TO_DIRECTORY
                .getOrDefault(registryType.location().asString(), ((ResourceLocation) (Object) registryType.location()).toDebugFileName());
    }

    @Override
    protected boolean serialize(final SpongeDataPackType<@NonNull ?, TagSerializedObject> type, final Path datapacksDir, final List<TagSerializedObject> objects, final int count) throws IOException {
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
            final Path objectFile = namespacedDataDirectory.resolve(this.typeDirectoryName).resolve(this.tagTypeName(object.getRegistryType())).resolve(filename);
            Files.createDirectories(objectFile.getParent());


            JsonObject toWrite = object.getObject();
            if (Files.exists(objectFile) && !object.getObject().getAsJsonPrimitive("replace").getAsBoolean()) {
                // Merge, baby merge.

                final JsonObject jsonObject;
                try (final BufferedReader bufferedReader = Files.newBufferedReader(objectFile)) {
                    final JsonElement jsonElement = JsonParser.parseReader(bufferedReader);
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
