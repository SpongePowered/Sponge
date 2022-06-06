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
import net.minecraft.nbt.Tag;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.common.tag.SpongeTagTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TagDataPackSerializer extends DataPackSerializer<TagTemplate> {

    @Override
    protected JsonElement transformSerialized(final Path file, final TagTemplate entry, final JsonElement serialized) throws IOException{
        JsonElement toWrite = serialized;
        /* TODO support merging
        if (Files.exists(file) && !((SpongeTagTemplate) entry).replace()) {
            final JsonObject jsonObject;
            try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
                final JsonElement jsonElement = JsonParser.parseReader(bufferedReader);
                jsonObject = jsonElement.getAsJsonObject();
            }

            toWrite = Tag.Builder.tag().addFromJson(jsonObject, filename).addFromJson(toWrite, filename).serializeToJson();
            // TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(builder.build(), this.replace)).getOrThrow(false, e -> {}).getAsJsonObject();
        }
        //*/
        return toWrite;
    }

    @Override
    public Path packEntryFile(final SpongeDataPackType<TagTemplate> type, final TagTemplate packEntry, final Path packDir) {
        final DefaultedRegistryType<?> registryType = ((SpongeTagTemplate) packEntry).registryType();
        return packDir.resolve("data")
                .resolve(packEntry.key().namespace())
                .resolve(type.dir())
                .resolve(registryType.location().toString())
                .resolve(packEntry.key().value() + ".json");
    }
}
