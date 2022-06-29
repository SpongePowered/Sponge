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
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.Taggable;

import java.io.IOException;
import java.nio.file.Path;

public final class TagDataPackSerializer<T extends Taggable<T>> extends JsonDataPackSerializer<TagTemplate<T>> {

    public TagDataPackSerializer(final DataPackEncoder<JsonElement, TagTemplate<T>> encoder, final DataPackDecoder<JsonElement, TagTemplate<T>> decoder) {
        super(encoder, decoder);
    }

    @Override
    protected JsonElement transformSerialized(final Path file, final TagTemplate<T> entry, final JsonElement serialized) throws IOException{
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

}
