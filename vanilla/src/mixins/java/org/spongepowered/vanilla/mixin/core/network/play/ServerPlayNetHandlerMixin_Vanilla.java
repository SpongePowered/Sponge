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
package org.spongepowered.vanilla.mixin.core.network.play;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.PlayerChatRouter;
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
import org.spongepowered.common.accessor.network.play.client.CCustomPayloadPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;
import org.spongepowered.vanilla.chat.ChatFormatter;

import java.util.Optional;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin_Vanilla implements IServerPlayNetHandler {

    @Shadow public ServerPlayerEntity player;
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "processCustomPayload", at = @At(value = "HEAD"))
    private void onHandleCustomPayload(final CCustomPayloadPacket packet, final CallbackInfo ci) {
        // For some reason, "CCustomPayloadPacket" is released in the processPacket
        // method of its class, only applicable to this packet, so just retain here.
        ((CCustomPayloadPacketAccessor) packet).accessor$getPayload().retain();

        final SpongeChannelRegistry channelRegistry = (SpongeChannelRegistry) Sponge.getChannelRegistry();
        this.server.execute(() -> channelRegistry.handlePlayPayload((EngineConnection) this, packet));
    }

    @Inject(method = "processChatMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;Z)V"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void vanilla$onProcessChatMessage(CChatMessagePacket packet, CallbackInfo ci, String s, ITextComponent component) {
        ChatFormatter.formatChatComponent((TranslationTextComponent) component);
        final PlayerChatRouter chatRouter = ((ServerPlayer) this.player).getChatRouter();
        Component adventure = SpongeAdventure.asAdventure(component);
        adventure = ((TranslatableComponent) adventure).args().get(1);

        try (CauseStackManager.StackFrame frame = PhaseTracker.SERVER.pushCauseFrame()) {
            frame.pushCause(this.player);
            final PlayerChatEvent event = SpongeEventFactory
                    .createPlayerChatEvent(PhaseTracker.SERVER.getCurrentCause(), chatRouter, Optional.of(chatRouter), adventure, adventure);
            if (SpongeCommon.postEvent(event)) {
                ci.cancel();
            } else {
                event.getChatRouter().ifPresent(router -> router.chat((ServerPlayer) this.player, event.getMessage()));
            }
        }
    }

    @Redirect(method = "processChatMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;Z)V") )
    private void vanilla$cancelSendChatMsgImpl(PlayerList manager, ITextComponent component, boolean chat) {
        // Do nothing
    }
}
