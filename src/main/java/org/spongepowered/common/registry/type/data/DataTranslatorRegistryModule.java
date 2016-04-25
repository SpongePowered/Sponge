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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DataTranslatorRegistryModule implements SpongeAdditionalCatalogRegistryModule<DataTranslator<?>> {

    public static DataTranslatorRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(DataTranslators.class)
    private final Map<String, DataTranslator<?>> dataFormatMappings = new HashMap<>();

    @Override
    public Optional<DataTranslator<?>> getById(String id) {
        return Optional.ofNullable(this.dataFormatMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<DataTranslator<?>> getAll() {
        return ImmutableList.copyOf(this.dataFormatMappings.values());
    }

    @Override
    public void registerAdditionalCatalog(DataTranslator<?> extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getId().isEmpty(), "Id cannot be empty");
        checkArgument(!this.dataFormatMappings.containsKey(extraCatalog.getId()), "Duplicate Id");
        this.dataFormatMappings.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
    }

    DataTranslatorRegistryModule() {
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    private static final class Holder {

        static final DataTranslatorRegistryModule INSTANCE = new DataTranslatorRegistryModule();
    }
}