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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class DisplaySlotStreamGenerator {

    private DisplaySlotStreamGenerator() {
    }

    public static Stream<Tuple<DisplaySlot, Integer>> stream() {
        final Stream.Builder<Tuple<DisplaySlot, Integer>> builder = Stream.builder();
        builder.add(new Tuple<>(new SpongeDisplaySlot(ResourceKey.sponge("below_name"), 2), 0));
        builder.add(new Tuple<>(new SpongeDisplaySlot(ResourceKey.sponge("list"), 0), 1));

        final Map<TextFormatting, SpongeDisplaySlot> sidebarByColor = new HashMap<>();
        final Function<TextFormatting, DisplaySlot> sidebarWithColor = sidebarByColor::get;

        sidebarByColor.put(TextFormatting.RESET, new SpongeDisplaySlot(ResourceKey.sponge("sidebar_team_no_color"), 1, null, sidebarWithColor));
        for (final TextFormatting formatting : TextFormatting.values()) {
            if (formatting.isColor() && formatting != TextFormatting.RESET) {
                sidebarByColor.put(formatting, new SpongeDisplaySlot(ResourceKey.sponge("sidebar_team_" + formatting.getName()),
                        3 + formatting.getId(), formatting, sidebarWithColor));
            }
        }

        sidebarByColor.values().forEach(slot -> builder.add(new Tuple<>(slot, slot.getIndex())));
        return builder.build();
    }
}
