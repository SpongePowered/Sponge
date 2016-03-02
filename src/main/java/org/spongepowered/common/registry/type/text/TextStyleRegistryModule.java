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

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.text.format.SpongeTextStyle;

public final class TextStyleRegistryModule implements RegistryModule {

    @RegisterCatalog(TextStyles.class)
    public static final ImmutableMap<String, TextStyle> textStyleMappings = ImmutableMap.<String, TextStyle>builder()
        .put("bold", SpongeTextStyle.of(TextFormatting.BOLD))
        .put("italic", SpongeTextStyle.of(TextFormatting.ITALIC))
        .put("underline", SpongeTextStyle.of(TextFormatting.UNDERLINE))
        .put("strikethrough", SpongeTextStyle.of(TextFormatting.STRIKETHROUGH))
        .put("obfuscated", SpongeTextStyle.of(TextFormatting.OBFUSCATED))
        .put("reset", SpongeTextStyle.of(TextFormatting.RESET))
        .put("none", TextStyles.NONE)
        .build();

}
