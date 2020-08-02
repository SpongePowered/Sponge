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
package org.spongepowered.common.resource.meta;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.resource.meta.MetaSection;

import java.io.IOException;

/**
 * A fallback metadata section serializer to get a specific section without a
 * backing object.
 */
public class SpongeMetadataSectionSerializer<T> implements IMetadataSectionSerializer<T> {
    private static final Gson gson = new Gson();
    private final MetaSection<T> section;

    public SpongeMetadataSectionSerializer(MetaSection<T> section) {
        this.section = section;
    }

    @Override
    public String getSectionName() {
        return section.getQuery().toString();
    }

    @Override
    public T deserialize(JsonObject json) {
        try {
            DataView data = DataFormats.JSON.get().read(gson.toJson(json));
            return section.deserialize(data);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }
}
