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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * On NeoForge the client-bound command tree is built by
 * {@link CommandHelper#mergeCommandNode}, so the workaround in
 * {@code CommandsMixin#impl$dontAskServerIfSiblingAlreadyDoes} never runs. That workaround exists because the
 * client sends one custom suggestion request per argument node that carries a suggestion provider; when a parent
 * has more than one such child, a later request cancels the earlier future, the combined future completes
 * exceptionally and the suggestion window never opens.
 *
 * <p>The same rule is applied here, per parent: only the first argument node below a given parent keeps its
 * {@code ASK_SERVER} suggestions, and any further sibling below that parent is replaced with an equivalent node
 * that has no suggestions. Nodes below other parents keep their own first suggestion node. The server still
 * computes completions for every node from its own tree, so no candidates are lost.</p>
 */
@Mixin(value = CommandHelper.class, remap = false)
public abstract class CommandHelperMixin_Neo {

    private static final ThreadLocal<Deque<CommandNode<?>>> impl$mergeParents = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Set<CommandNode<?>>> impl$customSuggestionsEmitted = ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    @Inject(method = "mergeCommandNode", at = @At("HEAD"))
    private static void impl$beginMerge(final CommandNode<?> source, final CallbackInfo ci) {
        impl$mergeParents.get().push(source);
    }

    @Inject(method = "mergeCommandNode", at = @At("RETURN"))
    private static void impl$endMerge(final CommandNode<?> source, final CallbackInfo ci) {
        final Deque<CommandNode<?>> parents = impl$mergeParents.get();
        parents.pop();
        if (parents.isEmpty()) {
            impl$mergeParents.remove();
            impl$customSuggestionsEmitted.remove();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "toResult", at = @At("RETURN"), cancellable = true)
    private static void impl$askTheServerOnlyOncePerParent(final CallbackInfoReturnable<CommandNode<?>> cir) {
        final CommandNode<?> parent = impl$mergeParents.get().peek();
        if (parent == null) {
            return;
        }
        final CommandNode<?> result = cir.getReturnValue();
        if (!(result instanceof ArgumentCommandNode<?, ?> argument)) {
            return;
        }
        if (argument.getCustomSuggestions() != SuggestionProviders.ASK_SERVER) {
            return;
        }
        final Set<CommandNode<?>> emitted = impl$customSuggestionsEmitted.get();
        if (emitted.add(parent)) {
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
