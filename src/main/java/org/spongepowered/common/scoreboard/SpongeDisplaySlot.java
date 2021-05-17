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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Optional;

public final class SpongeDisplaySlot implements DisplaySlot {

    private final int id;
    private final @Nullable ChatFormatting formatting;

    private @Nullable NamedTextColor color;

    public static int slotIdFromFormatting(final ChatFormatting formatting) {
        return formatting.getId() + 3;
    }

    public SpongeDisplaySlot(final int id) {
        this(id, null);
    }

    public SpongeDisplaySlot(final ChatFormatting color) {
        this(SpongeDisplaySlot.slotIdFromFormatting(color), color);
    }

    private SpongeDisplaySlot(final int id, final @Nullable ChatFormatting color) {
        this.id = id;
        this.formatting = color;
    }

    @Override
    @NonNull
    public Optional<NamedTextColor> teamColor() {
        if (this.color == null) {
            this.color = SpongeAdventure.asAdventureNamed(this.formatting);
        }
        return Optional.ofNullable(this.color);
    }

    public int index() {
        return this.id;
    }

}
