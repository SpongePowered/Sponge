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
package org.spongepowered.neoforge.mixin.core.commands;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.neoforged.neoforge.server.command.CommandHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * On NeoForge the client-bound command tree is built by
 * {@link CommandHelper#mergeCommandNode}, so the workaround in
 * {@code CommandsMixin#impl$dontAskServerIfSiblingAlreadyDoes} never runs. That workaround exists because the
 * client sends one custom suggestion request per argument node that carries a suggestion provider; when a parent
 * has more than one such child, a later request cancels the earlier future, the combined future completes
 * exceptionally and the suggestion window never opens.
 *
 * <p>The same rule is applied here: only the first argument node in the tree keeps its
 * {@code ASK_SERVER} suggestions, any further node is replaced with an equivalent node that has no suggestions.
 * The server still computes completions for every node from its own tree, so no candidates are lost.</p>
 */
@Mixin(value = CommandHelper.class, remap = false)
public abstract class CommandHelperMixin_Neo {

    private static final ThreadLocal<Integer> impl$mergeDepth = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> impl$customSuggestionsEmitted = ThreadLocal.withInitial(() -> false);

    @Inject(method = "mergeCommandNode", at = @At("HEAD"))
    private static void impl$beginClientTree(final CallbackInfo ci) {
        if (impl$mergeDepth.get() == 0) {
            impl$customSuggestionsEmitted.set(false);
        }
        impl$mergeDepth.set(impl$mergeDepth.get() + 1);
    }

    @Inject(method = "mergeCommandNode", at = @At("RETURN"))
    private static void impl$endClientTree(final CallbackInfo ci) {
        final int remaining = impl$mergeDepth.get() - 1;
        if (remaining <= 0) {
            impl$mergeDepth.remove();
            impl$customSuggestionsEmitted.remove();
        } else {
            impl$mergeDepth.set(remaining);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "toResult", at = @At("RETURN"), cancellable = true)
    private static void impl$askTheServerOnlyOnce(final CallbackInfoReturnable<CommandNode<?>> cir) {
        if (impl$mergeDepth.get() == 0) {
            return;
        }
        final CommandNode<?> result = cir.getReturnValue();
        if (!(result instanceof ArgumentCommandNode<?, ?> argument)) {
            return;
        }
        if (argument.getCustomSuggestions() != SuggestionProviders.ASK_SERVER) {
            return;
        }
        if (!impl$customSuggestionsEmitted.get()) {
            impl$customSuggestionsEmitted.set(true);
            return;
        }
        final RequiredArgumentBuilder<?, ?> builder = argument.createBuilder();
        builder.suggests(null);
        for (final CommandNode<?> child : argument.getChildren()) {
            ((RequiredArgumentBuilder) builder).then(child);
        }
        cir.setReturnValue(builder.build());
    }
}
