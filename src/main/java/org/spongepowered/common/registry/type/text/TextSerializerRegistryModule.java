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
package org.spongepowered.common.registry.type.text;

import static org.spongepowered.common.text.SpongeTexts.COLOR_CHAR;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.text.serializer.JsonTextSerializer;
import org.spongepowered.common.text.serializer.PlainTextSerializer;
import org.spongepowered.common.text.serializer.SpongeFormattingCodeTextSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TextSerializerRegistryModule implements AlternateCatalogRegistryModule<TextSerializer>, AdditionalCatalogRegistryModule<TextSerializer> {

    @RegisterCatalog(TextSerializers.class)
    private final Map<String, TextSerializer> textSerializerMappings = new HashMap<>();

    @Override
    public Optional<TextSerializer> getById(String id) {
        return Optional.ofNullable(this.textSerializerMappings.get(id));
    }

    @Override
    public Collection<TextSerializer> getAll() {
        return ImmutableList.copyOf(this.textSerializerMappings.values());
    }

    @Override
    public void registerDefaults() {
        registerSerializer(new PlainTextSerializer());
        registerSerializer(new JsonTextSerializer());
        registerSerializer(new SpongeFormattingCodeTextSerializer("sponge:formatting_code", "Formatting Codes", '&'));
        registerSerializer(new SpongeFormattingCodeTextSerializer("minecraft:legacy_formatting_code", "Legacy Formatting Codes", COLOR_CHAR));
    }

    private void registerSerializer(TextSerializer serializer) {
        this.textSerializerMappings.put(serializer.getId(), serializer);
    }

    @Override
    public void registerAdditionalCatalog(TextSerializer serializer) {
        this.textSerializerMappings.put(serializer.getId(), serializer);
    }

    @Override
    public Map<String, TextSerializer> provideCatalogMap() {
        final HashMap<String, TextSerializer> map = new HashMap<>();
        for (Map.Entry<String, TextSerializer> entry : this.textSerializerMappings.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", "").replace("sponge:", ""), entry.getValue());
        }
        return map;
    }
}
