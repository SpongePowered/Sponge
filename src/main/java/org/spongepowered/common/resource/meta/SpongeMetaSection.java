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
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.resource.meta.MetaParseException;
import org.spongepowered.api.resource.meta.NamedMetaSection;

import java.io.IOException;

public class SpongeMetaSection<T> implements NamedMetaSection<T> {

    private static final Gson gson = new Gson();
    private final ResourceKey key;
    private final IMetadataSectionSerializer<T> sectionSerializer;

    public SpongeMetaSection(ResourceKey key, IMetadataSectionSerializer<T> sectionSerializer) {
        this.key = key;
        this.sectionSerializer = sectionSerializer;
    }

    @Override
    public ResourceKey getKey() {
        return key;
    }

    @Override
    public String getName() {
        return sectionSerializer.getClass().getName();
    }

    @Override
    public DataQuery getQuery() {
        return DataQuery.of(sectionSerializer.getSectionName());
    }

    @Override
    public T deserialize(DataView data) throws MetaParseException {
        try {
            String jsonString = DataFormats.JSON.get().write(data);
            JsonObject jsonObj = gson.fromJson(jsonString, JsonObject.class);
            return sectionSerializer.deserialize(jsonObj);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }
}
