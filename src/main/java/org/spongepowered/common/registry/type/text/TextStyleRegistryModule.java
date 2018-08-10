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

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.text.format.TextStyleImpl;

@RegisterCatalog(value = TextStyles.class)
public final class TextStyleRegistryModule extends AbstractCatalogRegistryModule<TextStyle.Base>
    implements AlternateCatalogRegistryModule<TextStyle.Base> {

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("bold"), TextStyleImpl.Real.of(TextFormatting.BOLD));
        register(CatalogKey.minecraft("italic"), TextStyleImpl.Real.of(TextFormatting.ITALIC));
        register(CatalogKey.minecraft("underline"), TextStyleImpl.Real.of(TextFormatting.UNDERLINE));
        register(CatalogKey.minecraft("strikethrough"), TextStyleImpl.Real.of(TextFormatting.STRIKETHROUGH));
        register(CatalogKey.minecraft("obfuscated"), TextStyleImpl.Real.of(TextFormatting.OBFUSCATED));
        register(CatalogKey.minecraft("reset"), TextStyleImpl.Real.of(TextFormatting.RESET));
        register(CatalogKey.sponge("none"), new TextStyleImpl.None());
    }

    @Override
    protected String marshalFieldKey(String key) {
        return key.replace("sponge:", "");
    }

    @Override
    protected boolean filterAll(TextStyle.Base element) {
        return element != TextStyles.NONE;
    }
}
