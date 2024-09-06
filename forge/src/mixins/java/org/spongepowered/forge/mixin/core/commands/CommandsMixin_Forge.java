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
package org.spongepowered.forge.mixin.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.server.command.CommandHelper;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

@Mixin(Commands.class)
public abstract class CommandsMixin_Forge {

    private WeakHashMap<ServerPlayer, Map<CommandNode<CommandSourceStack>, List<CommandNode<SharedSuggestionProvider>>>> impl$playerNodeCache;
    private SpongeCommandManager impl$commandManager;

    // The event fired by Forge is fired in ForgeCommandManager at the appropriate time.
    @Redirect(method = "performCommand",
        at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"))
    private boolean forge$redirectToSpongeCommandManager(IEventBus instance, Event event) {
        return false;
    }


    @SuppressWarnings("unchecked")
    @Redirect(method = "sendCommands", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraftforge/server/command/CommandHelper;mergeCommandNode(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Ljava/util/Map;Ljava/lang/Object;Lcom/mojang/brigadier/Command;Ljava/util/function/Function;)V",
        remap = false
    ))
    private <S, T> void impl$addNonBrigSuggestions(
        final CommandNode<S> sourceChild,
        final CommandNode<T> sourceNode,
        final Map<CommandNode<S>, CommandNode<T>> resultNode,
        final S sourceToResult,
        final Command<T> canUse,
        final Function<SuggestionProvider<S>, SuggestionProvider<T>> execute,
        final ServerPlayer player
    ) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.SUBJECT, (Subject) player);
            final CommandCause sourceToUse = ((CommandSourceStackBridge) sourceToResult).bridge$withCurrentCause();
            try {
                this.impl$playerNodeCache.put(player, new IdentityHashMap<>());
                // We use this because the redirects should be a 1:1 mapping (which is what this map is for).
                final IdentityHashMap<CommandNode<S>, CommandNode<T>> idMap = new IdentityHashMap<>(resultNode);
                CommandHelper.<S, T>mergeCommandNode(sourceChild, sourceNode, idMap, sourceToResult, canUse, execute);
            } finally {
                this.impl$playerNodeCache.remove(player);
            }
            for (final CommandNode<SharedSuggestionProvider> node : this.impl$commandManager.getNonBrigadierSuggestions(sourceToUse)) {
                sourceNode.addChild((CommandNode<T>) node);
            }
        }
    }

}
