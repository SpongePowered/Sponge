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
package org.spongepowered.common.mixin.api.minecraft.commands.synchronization;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.SpongeCommandCompletion;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mixin(SuggestionProviders.Wrapper.class)
public abstract class SuggestionProviders_WrapperMixin_API implements CommandCompletionProvider {

    // @formatter:off
    @Shadow public abstract CompletableFuture<Suggestions> shadow$getSuggestions(
            com.mojang.brigadier.context.CommandContext<SharedSuggestionProvider> var1, SuggestionsBuilder var2) throws CommandSyntaxException;
    // @formatter:on

    @SuppressWarnings("unchecked")
    @Override
    public List<CommandCompletion> complete(final @NonNull CommandContext context, final @NonNull String currentInput) {
        try {
            return this.shadow$getSuggestions((com.mojang.brigadier.context.CommandContext<SharedSuggestionProvider>) context,
                    new SuggestionsBuilder(currentInput, 0)).join().getList().stream()
                        .map(SpongeCommandCompletion::from).collect(Collectors.toList());
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

}
