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
package org.spongepowered.common.mixin.core.network.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.server.STabCompletePacket;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.network.NetworkManagerHolderBridge;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.registrar.BrigadierBasedRegistrar;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Locale;
import java.util.Optional;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin implements NetworkManagerHolderBridge {

    private static final String[] impl$emptyCommandArray = new String[] { "" };

    @Shadow @Final public NetworkManager netManager;
    @Shadow public ServerPlayerEntity player;

    @Override
    public NetworkManager bridge$getNetworkManager() {
        return this.netManager;
    }

    @Inject(method = "processTabComplete", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/network/PacketThreadUtil;"
                    + "checkThreadAndEnqueue(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/world/server/ServerWorld;)V"),
            cancellable = true)
    private void impl$getSuggestionsFromNonBrigCommand(final CTabCompletePacket p_195518_1_, final CallbackInfo ci) {
        final String rawCommand = p_195518_1_.getCommand();
        final String[] command = this.impl$extractCommandString(rawCommand);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this.player);
            final CommandCause cause = CommandCause.create();
            final SpongeCommandManager manager = ((SpongeCommandManager) Sponge.getCommandManager());
            if (!rawCommand.contains(" ")) {
                final SuggestionsBuilder builder = new SuggestionsBuilder(command[0], 0);
                if (command[0].isEmpty()) {
                    manager.getAliasesForCause(cause).forEach(builder::suggest);
                } else {
                    manager.getAliasesThatStartWithForCause(cause, command[0]).forEach(builder::suggest);
                }
                this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), builder.build()));
                ci.cancel();
            } else {
                final Optional<CommandMapping> mappingOptional =
                        manager.getCommandMapping(command[0].toLowerCase(Locale.ROOT)).filter(x -> !(x.getRegistrar() instanceof BrigadierBasedRegistrar));
                if (mappingOptional.isPresent()) {
                    final CommandMapping mapping = mappingOptional.get();
                    if (mapping.getRegistrar().canExecute(cause, mapping)) {
                        try {
                            final SuggestionsBuilder builder = new SuggestionsBuilder(rawCommand, rawCommand.lastIndexOf(" ") + 1);
                            mapping.getRegistrar().suggestions(cause, mapping, command[0], command[1]).forEach(builder::suggest);
                            this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), builder.build()));
                        } catch (final CommandException e) {
                            cause.sendMessage(TextComponent.of("Unable to create suggestions for your tab completion"));
                            this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), Suggestions.empty().join()));
                        }
                    } else {
                        this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), Suggestions.empty().join()));
                    }
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(method = "processTabComplete", at = @At(value = "INVOKE",
            target = "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)"
                    + "Lcom/mojang/brigadier/ParseResults;"))
    private ParseResults<CommandSource> impl$informParserThisIsASuggestionCheck(final CommandDispatcher<CommandSource> commandDispatcher,
            final StringReader command,
            final Object source) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().parse(command, (CommandSource) source, true);
    }

    private String[] impl$extractCommandString(final String commandString) {
        if (commandString.isEmpty()) {
            return ServerPlayNetHandlerMixin.impl$emptyCommandArray;
        }
        if (commandString.startsWith("/")) {
            return commandString.substring(1).split(" ", 2);
        }
        return commandString.split(" ", 2);
    }

}
