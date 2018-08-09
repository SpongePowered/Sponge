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
import org.spongepowered.common.text.format.SpongeTextStyle;

@RegisterCatalog(value = TextStyles.class, ignoredFields = "NONE")
public final class TextStyleRegistryModule extends AbstractCatalogRegistryModule<TextStyle.Base>
    implements AlternateCatalogRegistryModule<TextStyle.Base> {

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("bold"), SpongeTextStyle.of(TextFormatting.BOLD));
        register(CatalogKey.minecraft("italic"), SpongeTextStyle.of(TextFormatting.ITALIC));
        register(CatalogKey.minecraft("underline"), SpongeTextStyle.of(TextFormatting.UNDERLINE));
        register(CatalogKey.minecraft("strikethrough"), SpongeTextStyle.of(TextFormatting.STRIKETHROUGH));
        register(CatalogKey.minecraft("obfuscated"), SpongeTextStyle.of(TextFormatting.OBFUSCATED));
        register(CatalogKey.minecraft("reset"), SpongeTextStyle.of(TextFormatting.RESET));
        register(CatalogKey.minecraft("none"), (TextStyle.Base) TextStyles.NONE);
    }

}
