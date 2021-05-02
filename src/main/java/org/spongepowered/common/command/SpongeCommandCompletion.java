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
package org.spongepowered.common.command;

import com.mojang.brigadier.suggestion.Suggestion;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Optional;

public final class SpongeCommandCompletion implements CommandCompletion {

    public static SpongeCommandCompletion from(final @NonNull Suggestion suggestion) {
        @Nullable Component tooltip = null;
        if (suggestion.getTooltip() != null) {
            tooltip = SpongeAdventure.asAdventure(suggestion.getTooltip());
        }
        return new SpongeCommandCompletion(suggestion.getText(), tooltip);
    }

    final String completion;
    final @Nullable Component tooltip;

    public SpongeCommandCompletion(final String completion) {
        this(completion, null);
    }

    public SpongeCommandCompletion(final String completion, final @Nullable Component tooltip) {
        this.completion = completion;
        this.tooltip = tooltip;
    }

    @Override
    public String completion() {
        return this.completion;
    }

    @Override
    public Optional<Component> tooltip() {
        return Optional.ofNullable(this.tooltip);
    }

}
