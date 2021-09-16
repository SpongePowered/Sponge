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
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.PlayerChatFormatter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.vanilla.chat.ChatFormatter;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_Vanilla implements ServerGamePacketListener {

    @Shadow public net.minecraft.server.level.ServerPlayer player;
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "handleCustomPayload", at = @At(value = "HEAD"))
    private void vanilla$onHandleCustomPayload(final ServerboundCustomPayloadPacket packet, final CallbackInfo ci) {
        // For some reason, "ServerboundCustomPayloadPacket" is released in the processPacket
        // method of its class, only applicable to this packet, so just retain here.
        packet.getData().retain();

        final SpongeChannelManager channelRegistry = (SpongeChannelManager) Sponge.channelManager();
        this.server.execute(() -> channelRegistry.handlePlayPayload((EngineConnection) this, packet));
    }

    @Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void vanilla$onProcessChatMessage(final TextFilter.FilteredText message, final CallbackInfo ci, final String plain, final String filteredPlain, final net.minecraft.network.chat.Component filtered, final net.minecraft.network.chat.Component unfiltered) {
        // todo(zml): See where mojang takes these chat filtering changes
        ChatFormatter.formatChatComponent((net.minecraft.network.chat.TranslatableComponent) unfiltered);
        final ServerPlayer player = (ServerPlayer) this.player;
        final PlayerChatFormatter chatFormatter = player.chatFormatter();
        final TextComponent rawMessage = Component.text(plain);

        try (CauseStackManager.StackFrame frame = PhaseTracker.SERVER.pushCauseFrame()) {
            frame.pushCause(this.player);
            final Audience audience = (Audience) this.server;
            final PlayerChatEvent event = SpongeEventFactory.createPlayerChatEvent(frame.currentCause(), audience, Optional.of(audience), chatFormatter, Optional.of(chatFormatter), rawMessage, rawMessage);
            if (SpongeCommon.post(event)) {
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

    @Redirect(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V") )
    private void vanilla$cancelSendChatMsgImpl(final PlayerList playerList, final net.minecraft.network.chat.Component p_232641_1_, final Function<ServerPlayer, Component> messageProvider, final ChatType p_232641_2_, final UUID p_232641_3_) {
        // Do nothing
    }

    @SuppressWarnings({"UnresolvedMixinReference", "unchecked", "rawtypes"})
    @Redirect(method = "lambda$handlePlaceRecipe$10",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookMenu;handlePlacement(ZLnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void vanilla$onPlaceRecipe(final RecipeBookMenu recipeBookMenu, final boolean shift, final Recipe<?> recipe, final net.minecraft.server.level.ServerPlayer player) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();

        final Inventory craftInv = ((Inventory) player.containerMenu).query(QueryTypes.INVENTORY_TYPE.get().of(CraftingInventory.class));
        if (!(craftInv instanceof CraftingInventory)) {
            recipeBookMenu.handlePlacement(shift, recipe, player);
            SpongeCommon.logger().warn("Detected crafting without a InventoryCrafting!? Crafting Event will not fire.");
            return;
        }

        try (final EffectTransactor ignored = transactor.logPlaceRecipe(shift, recipe, player, (CraftingInventory) craftInv)) {
            recipeBookMenu.handlePlacement(shift, recipe, player);
            player.containerMenu.broadcastChanges();
        }
    }
}
