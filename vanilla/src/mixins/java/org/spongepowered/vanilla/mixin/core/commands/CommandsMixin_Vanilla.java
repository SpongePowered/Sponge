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
package org.spongepowered.vanilla.mixin.core.commands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(Commands.class)
public abstract class CommandsMixin_Vanilla {

    // @formatter:off
    @Shadow private void shadow$fillUsableCommands(final CommandNode<CommandSourceStack> rootCommandSource,
                                                   final CommandNode<SharedSuggestionProvider> rootSuggestion,
                                                   final CommandSourceStack source,
                                                   final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode) {
        throw new AssertionError("This shouldn't be callable");
    }
    // @formatter:on

    private CauseStackManager.StackFrame impl$initFrame = null;
    private WeakHashMap<ServerPlayer, Map<CommandNode<CommandSourceStack>, List<CommandNode<SharedSuggestionProvider>>>> impl$playerNodeCache;
    private SpongeCommandManager impl$commandManager;

    @Redirect(method = "sendCommands", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;fillUsableCommands(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Map;)V"))
    private void impl$addNonBrigSuggestions(
        final Commands commands,
        final CommandNode<CommandSourceStack> p_197052_1_,
        final CommandNode<SharedSuggestionProvider> p_197052_2_,
        final CommandSourceStack p_197052_3_,
        final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> p_197052_4_,
        final ServerPlayer playerEntity) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(playerEntity);
            frame.addContext(EventContextKeys.SUBJECT, (Subject) playerEntity);
            final CommandCause sourceToUse = ((CommandSourceStackBridge) p_197052_3_).bridge$withCurrentCause();
            try {
                this.impl$playerNodeCache.put(playerEntity, new IdentityHashMap<>());
                // We use this because the redirects should be a 1:1 mapping (which is what this map is for).
                final IdentityHashMap<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> idMap = new IdentityHashMap<>(p_197052_4_);
                this.shadow$fillUsableCommands(p_197052_1_, p_197052_2_, (CommandSourceStack) sourceToUse, idMap);
            } finally {
                this.impl$playerNodeCache.remove(playerEntity);
            }
            for (final CommandNode<SharedSuggestionProvider> node : this.impl$commandManager.getNonBrigadierSuggestions(sourceToUse)) {
                p_197052_2_.addChild(node);
            }
        }
    }
}
