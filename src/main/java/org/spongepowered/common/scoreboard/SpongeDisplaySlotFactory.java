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
package org.spongepowered.common.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public final class SpongeDisplaySlotFactory implements DisplaySlot.Factory {

    @Override
    @NonNull
    public Optional<DisplaySlot> findByTeamColor(final @NonNull NamedTextColor color) {
        final DefaultedRegistryReference<DisplaySlot> slot =
                SpongeDisplaySlotFactory.ColorMapping.COLOR_TO_DISPLAY_SLOT_MAP.get(SpongeAdventure.asVanilla(color));
        if (slot != null) {
            return slot.find();
        }
        return Optional.empty();
    }

    public static final class ColorMapping {
        public static final java.util.Map<ChatFormatting, DefaultedRegistryReference<DisplaySlot>> COLOR_TO_DISPLAY_SLOT_MAP;

        static {
            final java.util.Map<ChatFormatting, DefaultedRegistryReference<DisplaySlot>> map = new HashMap<>();
            map.put(ChatFormatting.AQUA, DisplaySlots.SIDEBAR_TEAM_AQUA);
            map.put(ChatFormatting.BLACK, DisplaySlots.SIDEBAR_TEAM_BLACK);
            map.put(ChatFormatting.BLUE, DisplaySlots.SIDEBAR_TEAM_BLUE);
            map.put(ChatFormatting.DARK_AQUA, DisplaySlots.SIDEBAR_TEAM_DARK_AQUA);
            map.put(ChatFormatting.DARK_BLUE, DisplaySlots.SIDEBAR_TEAM_DARK_BLUE);
            map.put(ChatFormatting.DARK_GRAY, DisplaySlots.SIDEBAR_TEAM_DARK_GRAY);
            map.put(ChatFormatting.DARK_GREEN, DisplaySlots.SIDEBAR_TEAM_DARK_GREEN);
            map.put(ChatFormatting.DARK_PURPLE, DisplaySlots.SIDEBAR_TEAM_DARK_PURPLE);
            map.put(ChatFormatting.DARK_RED, DisplaySlots.SIDEBAR_TEAM_DARK_RED);
            map.put(ChatFormatting.GOLD, DisplaySlots.SIDEBAR_TEAM_GOLD);
            map.put(ChatFormatting.GRAY, DisplaySlots.SIDEBAR_TEAM_GRAY);
            map.put(ChatFormatting.GREEN, DisplaySlots.SIDEBAR_TEAM_GREEN);
            map.put(ChatFormatting.LIGHT_PURPLE, DisplaySlots.SIDEBAR_TEAM_LIGHT_PURPLE);
            map.put(ChatFormatting.RED, DisplaySlots.SIDEBAR_TEAM_RED);
            map.put(ChatFormatting.WHITE, DisplaySlots.SIDEBAR_TEAM_WHITE);
            map.put(ChatFormatting.YELLOW, DisplaySlots.SIDEBAR_TEAM_YELLOW);
            COLOR_TO_DISPLAY_SLOT_MAP = Collections.unmodifiableMap(map);
        }

        private ColorMapping() {
        }
    }

}
