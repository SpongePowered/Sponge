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
package org.spongepowered.vanilla.mixin.core.server.network;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.PlayerChatFormatter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.network.protocol.game.ServerboundCustomPayloadPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;
import org.spongepowered.vanilla.chat.ChatFormatter;

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_Vanilla implements ServerGamePacketListener {

    @Shadow public net.minecraft.server.level.ServerPlayer player;
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "handleCustomPayload", at = @At(value = "HEAD"))
    private void onHandleCustomPayload(final ServerboundCustomPayloadPacket packet, final CallbackInfo ci) {
        // For some reason, "ServerboundCustomPayloadPacket" is released in the processPacket
        // method of its class, only applicable to this packet, so just retain here.
        ((ServerboundCustomPayloadPacketAccessor) packet).accessor$data().retain();

        final SpongeChannelRegistry channelRegistry = (SpongeChannelRegistry) Sponge.channelRegistry();
        this.server.execute(() -> channelRegistry.handlePlayPayload((EngineConnection) this, packet));
    }

    @Inject(method = "handleChat(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void vanilla$onProcessChatMessage(String var1, CallbackInfo ci, net.minecraft.network.chat.Component component) {
        ChatFormatter.formatChatComponent((net.minecraft.network.chat.TranslatableComponent) component);
        final ServerPlayer player = (ServerPlayer) this.player;
        final PlayerChatFormatter chatFormatter = player.chatFormatter();
        final TextComponent rawMessage = Component.text(var1);

        try (CauseStackManager.StackFrame frame = PhaseTracker.SERVER.pushCauseFrame()) {
            frame.pushCause(this.player);
            final Audience audience = (Audience) this.server;
            final PlayerChatEvent event = SpongeEventFactory.createPlayerChatEvent(frame.currentCause(), audience, Optional.of(audience), chatFormatter, Optional.of(chatFormatter), rawMessage, rawMessage);
            if (SpongeCommon.postEvent(event)) {
                ci.cancel();
            } else {
                event.chatFormatter().ifPresent(formatter ->
                    event.audience().map(SpongeAdventure::unpackAudiences).ifPresent(targets -> {
                        for (Audience target : targets) {
                            formatter.format(player, target, event.message(), event.originalMessage()).ifPresent(formattedMessage ->
                                target.sendMessage(player, formattedMessage));
                        }
                    })
                );
            }
        }
    }

    @Redirect(method = "handleChat(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V") )
    private void vanilla$cancelSendChatMsgImpl(PlayerList playerList, net.minecraft.network.chat.Component p_232641_1_, ChatType p_232641_2_, UUID p_232641_3_) {
        // Do nothing
    }
}
