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
package org.spongepowered.common.registry.type.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.persistence.HoconDataFormat;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.data.persistence.NbtDataFormat;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(DataFormats.class)
public class DataFormatRegistryModule extends AbstractCatalogRegistryModule<DataFormat> implements AdditionalCatalogRegistryModule<DataFormat> {

    @Override
    public void registerAdditionalCatalog(DataFormat extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getKey().getValue().isEmpty(), "Id cannot be empty");
        checkArgument(!this.map.containsKey(extraCatalog.getKey()), "Duplicate Id");
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        register(CatalogKey.sponge("nbt"), new NbtDataFormat("nbt"));
        register(CatalogKey.sponge("json"), new JsonDataFormat());
        register(CatalogKey.sponge("hocon"), new HoconDataFormat("hocon"));
    }
}
