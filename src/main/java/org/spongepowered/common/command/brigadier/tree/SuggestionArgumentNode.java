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
package org.spongepowered.common.command.brigadier.tree;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Objects;

// Used to differentiate between custom suggestions
public final class SuggestionArgumentNode<T> extends ArgumentCommandNode<SharedSuggestionProvider, T> {

    public SuggestionArgumentNode(final RequiredArgumentBuilder<SharedSuggestionProvider, T> builder) {
        super(builder.getName(),
                builder.getType(),
                builder.getCommand(),
                builder.getRequirement(),
                builder.getRedirect(),
                builder.getRedirectModifier(),
                builder.isFork(),
                builder.getSuggestionsProvider());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getCustomSuggestions());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof SuggestionArgumentNode && super.equals(o)) {
            final SuggestionArgumentNode<?> that = (SuggestionArgumentNode<?>) o;
            // This is intentional - we can end up with a stack overflow if we don't do this.
            // Same with hashCode, which is why we haven't put it in there.
            // We need the redirect to be part of the equality contract however, so an
            // identity is the best we have gotten here.
            return this.getRedirect() == that.getRedirect() &&
                    Objects.equals(this.getCustomSuggestions(), that.getCustomSuggestions());
        }
        return false;
    }
}
