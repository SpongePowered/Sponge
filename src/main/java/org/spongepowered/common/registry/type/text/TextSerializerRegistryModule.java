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

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.text.serializer.JsonTextSerializer;
import org.spongepowered.common.text.serializer.PlainTextSerializer;
import org.spongepowered.common.text.serializer.SpongeFormattingCodeTextSerializer;

@RegisterCatalog(TextSerializers.class)
public final class TextSerializerRegistryModule extends AbstractCatalogRegistryModule<TextSerializer> implements AdditionalCatalogRegistryModule<TextSerializer> {

    @Override
    public void registerDefaults() {
        this.registerAdditionalCatalog(new PlainTextSerializer());
        this.registerAdditionalCatalog(new JsonTextSerializer());
        this.registerAdditionalCatalog(new SpongeFormattingCodeTextSerializer("sponge:formatting_code", "Formatting Codes", '&'));
        this.registerAdditionalCatalog(new SpongeFormattingCodeTextSerializer("minecraft:legacy_formatting_code", "Legacy Formatting Codes", COLOR_CHAR));
    }

    @Override
    public void registerAdditionalCatalog(TextSerializer serializer) {
        this.map.put(serializer.getId(), serializer);
    }
}
