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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.SharedSuggestionProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public final class ForcedRedirectArgumentSuggestionNode<T> extends ArgumentCommandNode<SharedSuggestionProvider, T> implements ForcedRedirectNode {

    private @Nullable CommandNode<SharedSuggestionProvider> forcedRedirect = null;

    public ForcedRedirectArgumentSuggestionNode(final String name,
            final ArgumentType<T> type,
            final Command<SharedSuggestionProvider> command,
            final SuggestionProvider<SharedSuggestionProvider> customSuggestions) {
        super(name, type, command, isp -> true, null, null, false, customSuggestions);
    }

    @Override
    public void setForcedRedirect(final @Nullable CommandNode<SharedSuggestionProvider> redirect) {
        this.forcedRedirect = redirect;
    }

    @Override
    public CommandNode<SharedSuggestionProvider> getRedirect() {
        return this.forcedRedirect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.forcedRedirect, this.getCustomSuggestions());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ForcedRedirectArgumentSuggestionNode<?> that = (ForcedRedirectArgumentSuggestionNode<?>) o;
        return Objects.equals(this.forcedRedirect, that.forcedRedirect)
                && Objects.equals(this.getCustomSuggestions(), that.getCustomSuggestions());
    }

}
