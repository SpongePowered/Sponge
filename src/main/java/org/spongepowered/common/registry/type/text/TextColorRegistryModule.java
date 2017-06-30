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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class TextColorRegistryModule implements CatalogRegistryModule<TextColor> {

    @RegisterCatalog(TextColors.class)
    public static final Map<String, TextColor> textColorMappings = Maps.newHashMap();
    public static final Map<TextFormatting, SpongeTextColor> enumChatColor = Maps.newEnumMap(TextFormatting.class);

    @Override
    public Optional<TextColor> getById(String id) {
        return Optional.ofNullable(textColorMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<TextColor> getAll() {
        return ImmutableList.copyOf(textColorMappings.values());
    }

    @Override
    public void registerDefaults() {
        addTextColor(TextFormatting.BLACK, Color.BLACK);
        addTextColor(TextFormatting.DARK_BLUE, Color.ofRgb(0x0000AA));
        addTextColor(TextFormatting.DARK_GREEN, Color.ofRgb(0x00AA00));
        addTextColor(TextFormatting.DARK_AQUA, Color.ofRgb(0x00AAAA));
        addTextColor(TextFormatting.DARK_RED, Color.ofRgb(0xAA0000));
        addTextColor(TextFormatting.DARK_PURPLE, Color.ofRgb(0xAA00AA));
        addTextColor(TextFormatting.GOLD, Color.ofRgb(0xFFAA00));
        addTextColor(TextFormatting.GRAY, Color.ofRgb(0xAAAAAA));
        addTextColor(TextFormatting.DARK_GRAY, Color.ofRgb(0x555555));
        addTextColor(TextFormatting.BLUE, Color.ofRgb(0x5555FF));
        addTextColor(TextFormatting.GREEN, Color.ofRgb(0x55FF55));
        addTextColor(TextFormatting.AQUA, Color.ofRgb(0x55FFFF));
        addTextColor(TextFormatting.RED, Color.ofRgb(0xFF5555));
        addTextColor(TextFormatting.LIGHT_PURPLE, Color.ofRgb(0xFF55FF));
        addTextColor(TextFormatting.YELLOW, Color.ofRgb(0xFFFF55));
        addTextColor(TextFormatting.WHITE, Color.WHITE);
        addTextColor(TextFormatting.RESET, Color.WHITE);

        textColorMappings.put("none", TextColors.NONE);
    }

    private static void addTextColor(TextFormatting handle, Color color) {
        SpongeTextColor spongeColor = new SpongeTextColor(handle, color);
        textColorMappings.put(handle.name().toLowerCase(Locale.ENGLISH), spongeColor);
        enumChatColor.put(handle, spongeColor);
    }
}
