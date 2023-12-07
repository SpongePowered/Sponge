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
package org.spongepowered.common.mixin.api.minecraft.world.scores;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(DisplaySlot.class)
public abstract class DisplaySlotMixin_API implements org.spongepowered.api.scoreboard.displayslot.DisplaySlot {

    @Override
    public Optional<NamedTextColor> teamColor() {
        return Optional.ofNullable(switch ((DisplaySlot) (Object)this) {
            case LIST, SIDEBAR, BELOW_NAME -> null;
            case TEAM_BLACK -> NamedTextColor.BLACK;
            case TEAM_DARK_BLUE -> NamedTextColor.DARK_BLUE;
            case TEAM_DARK_GREEN -> NamedTextColor.DARK_GREEN;
            case TEAM_DARK_AQUA -> NamedTextColor.DARK_AQUA;
            case TEAM_DARK_RED -> NamedTextColor.DARK_RED;
            case TEAM_DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
            case TEAM_GOLD -> NamedTextColor.GOLD;
            case TEAM_GRAY -> NamedTextColor.GRAY;
            case TEAM_DARK_GRAY -> NamedTextColor.DARK_GRAY;
            case TEAM_BLUE -> NamedTextColor.BLUE;
            case TEAM_GREEN -> NamedTextColor.GREEN;
            case TEAM_AQUA -> NamedTextColor.AQUA;
            case TEAM_RED -> NamedTextColor.RED;
            case TEAM_LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case TEAM_YELLOW -> NamedTextColor.YELLOW;
            case TEAM_WHITE -> NamedTextColor.WHITE;
        });
    }
}
