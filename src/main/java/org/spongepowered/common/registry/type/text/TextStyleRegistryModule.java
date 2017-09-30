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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.text.format.SpongeTextStyle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TextStyleRegistryModule implements AlternateCatalogRegistryModule<TextStyle.Base> {

    @RegisterCatalog(value = TextStyles.class, ignoredFields = "NONE")
    public static final ImmutableMap<String, TextStyle.Base> textStyleMappings = ImmutableMap.<String, TextStyle.Base>builder()
        .put("minecraft:bold", SpongeTextStyle.of(TextFormatting.BOLD))
        .put("minecraft:italic", SpongeTextStyle.of(TextFormatting.ITALIC))
        .put("minecraft:underline", SpongeTextStyle.of(TextFormatting.UNDERLINE))
        .put("minecraft:strikethrough", SpongeTextStyle.of(TextFormatting.STRIKETHROUGH))
        .put("minecraft:obfuscated", SpongeTextStyle.of(TextFormatting.OBFUSCATED))
        .put("minecraft:reset", SpongeTextStyle.of(TextFormatting.RESET))
        .put("none", (TextStyle.Base) TextStyles.NONE)
        .build();

    @Override
    public Optional<TextStyle.Base> getById(String id) {
        if (id.equals("NONE")) {
            return Optional.of((TextStyle.Base) TextStyles.NONE);
        }
        return Optional.ofNullable(textStyleMappings.get(Preconditions.checkNotNull(id)));
    }

    @Override
    public Collection<TextStyle.Base> getAll() {
        return ImmutableList.copyOf(textStyleMappings.values());
    }

    @Override
    public Map<String, TextStyle.Base> provideCatalogMap() {
        final HashMap<String, TextStyle.Base> map = new HashMap<>();
        for (Map.Entry<String, TextStyle.Base> entry : textStyleMappings.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return map;
    }
}
