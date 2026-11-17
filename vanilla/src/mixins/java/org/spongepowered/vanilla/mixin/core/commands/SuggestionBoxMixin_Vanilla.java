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
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SuggestionBox;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.command.brigadier.tree.ClientCommandTreeConverter;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.resolver.SpongeSuggestionTreeResolver;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(SuggestionBox.class)
public abstract class SuggestionBoxMixin_Vanilla {

    // @formatter:off
    @Shadow private static <S> void shadow$fillUsableCommands(final CommandNode<S> source, final CommandNode<S> target, final S commandFilter, final Map<CommandNode<S>, CommandNode<S>> converted) {
        throw new UnsupportedOperationException("Shadowed fillUsableCommands");
    }
    // @formatter:on

    // The tree a player is sent is the one Sponge resolves for them, non Brigadier commands included
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "toPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SuggestionBox;fillUsableCommands(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;Ljava/util/Map;)V"))
    private <S> void vanilla$fillFromSpongeTree(
        final CommandNode<S> dispatcherRoot,
        final CommandNode<S> packetRoot,
        final S commandFilter,
        final Map<CommandNode<S>, CommandNode<S>> converted,
        final ServerPlayer player) {
        final @Nullable SpongeCommandManager manager = SpongeCommandManager.get(player.level().getServer());
        if (manager == null) {
            SuggestionBoxMixin_Vanilla.shadow$fillUsableCommands(dispatcherRoot, packetRoot, commandFilter, converted);
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.SUBJECT, (Subject) player);
            final CommandCause sourceToUse = ((CommandSourceStackBridge) player.createCommandSourceStack()).bridge$withCurrentCause();

            final RootCommandNode<CommandSourceStack> spongeRoot = manager.getDispatcher().getRoot();
            final RootCommandNode<CommandSourceStack> rootSuggestion = new RootCommandNode<>();
            // We use this because the redirects should be a 1:1 mapping (which is what this map is for).
            final IdentityHashMap<CommandNode<CommandSourceStack>, CommandNode<CommandSourceStack>> idMap = new IdentityHashMap<>();
            idMap.put(spongeRoot, rootSuggestion);
            new SpongeSuggestionTreeResolver((CommandSourceStack) sourceToUse, idMap, new IdentityHashMap<>(), manager)
                .fillSuggestions(spongeRoot, rootSuggestion);

            for (final CommandNode<CommandSourceStack> node : manager.getNonBrigadierSuggestions(sourceToUse)) {
                rootSuggestion.addChild(node);
            }

            for (final CommandNode<SuggestionContext> node : new ClientCommandTreeConverter().convert(rootSuggestion).getChildren()) {
                packetRoot.addChild((CommandNode) node);
            }
        } catch (final RuntimeException e) {
            // A tree that cannot be sent costs the player their completions, not their connection
            SpongeCommon.logger().error("Could not build the command tree for {}", player.getGameProfile().name(), e);
        }
    }
}
