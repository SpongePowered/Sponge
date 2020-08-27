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
package org.spongepowered.common.mixin.core.server.management;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.math.vector.Vector3d;

import java.net.SocketAddress;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract ITextComponent canPlayerLogin(SocketAddress socketAddress, com.mojang.authlib.GameProfile gameProfile);

    private void disconnectClient(final NetworkManager netManager, final @Nullable Component disconnectMessage, final @Nullable GameProfile profile) {
        final ITextComponent reason;
        if (disconnectMessage != null) {
            reason = SpongeAdventure.asVanilla(disconnectMessage);
        } else {
            reason = new TranslationTextComponent("disconnect.disconnected");
        }

        try {
            LOGGER.info("Disconnecting " + (profile != null ? profile.toString() + " (" + netManager.getRemoteAddress().toString() + ")" :
                    netManager.getRemoteAddress() + ": " + reason.getUnformattedComponentText()));
            netManager.sendPacket(new SDisconnectPacket(reason));
            netManager.closeChannel(reason);
        } catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    @Inject(method = "initializeConnectionToPlayer", at = @At(value = "HEAD"))
    private void impl$onInitPlayer_head(final NetworkManager networkManager, final ServerPlayerEntity mcPlayer, final CallbackInfo ci) {
        final GameProfile previousGameProfile = (GameProfile) this.server.getPlayerProfileCache().getProfileByUUID(mcPlayer.getGameProfile().getId());
        ((ServerPlayerEntityBridge) mcPlayer).bridge$setPreviousGameProfile(previousGameProfile);
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/world/server/ServerWorld;"))
    private net.minecraft.world.server.ServerWorld impl$onInitPlayer_getWorld(
            final MinecraftServer server, final net.minecraft.world.dimension.DimensionType type,
            final NetworkManager networkManager, final ServerPlayerEntity mcPlayer) {
        @Nullable ITextComponent kickReason = this.canPlayerLogin(networkManager.getRemoteAddress(), mcPlayer.getGameProfile());
        Component disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeAdventure.asAdventure(kickReason);
        } else {
            disconnectMessage = TextComponent.of("You are not allowed to log in to this server.");
        }

        net.minecraft.world.server.ServerWorld mcWorld = server.getWorld(mcPlayer.dimension);

        //noinspection ConstantConditions
        if (mcWorld == null) {
            SpongeCommon.getLogger().warn("The player '{}' was located in a world that isn't loaded or doesn't exist. This is not safe so "
                            + "the player will be moved to the spawn of the default world.", mcPlayer.getGameProfile().getName());
            mcPlayer.dimension = DimensionType.OVERWORLD;
            mcWorld = server.getWorld(mcPlayer.dimension);
            final BlockPos spawnPoint = mcWorld.getSpawnPoint();
            mcPlayer.setPosition(spawnPoint.getX() + 0.5, spawnPoint.getY() + 0.5, spawnPoint.getZ() + 0.5);
        }

        mcPlayer.setWorld(mcWorld);

        final ServerPlayer player = (ServerPlayer) mcPlayer;
        final ServerLocation location = player.getServerLocation();
        final Vector3d rotation = player.getRotation();
        // player.getConnection() cannot be used here, because it's still be null at this point
        final ServerSideConnection connection = (ServerSideConnection) networkManager.getNetHandler();
        final User user = player.getUser();

        final Cause cause = Cause.of(EventContext.empty(), connection, user);
        final ServerSideConnectionEvent.Login event = SpongeEventFactory.createServerSideConnectionEventLogin(cause, disconnectMessage,
                disconnectMessage, location, location, rotation, rotation, connection, user, false);
        if (kickReason != null) {
            event.setCancelled(true);
        }
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            final Component message = event.isMessageCancelled() ? null : event.getMessage();
            this.disconnectClient(networkManager, message, player.getProfile());
            return null;
        }

        final ServerLocation toLocation = event.getToLocation();
        final Vector3d toRotation = event.getToRotation();
        mcPlayer.setPositionAndRotation(toLocation.getX(), toLocation.getY(), toLocation.getZ(),
                (float) toRotation.getY(), (float) toRotation.getX());
        return (net.minecraft.world.server.ServerWorld) toLocation.getWorld();
    }

    @Inject(method = "initializeConnectionToPlayer", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/world/server/ServerWorld;"))
    private void impl$onInitPlayer_afterGetWorld(final NetworkManager networkManager, final ServerPlayerEntity mcPlayer, final CallbackInfo ci) {
        if (mcPlayer.world == null) {
            ci.cancel();
        }
    }

    @Redirect(method = "initializeConnectionToPlayer",
        at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false
        )
    )
    private void impl$onInitPlayer_printPlayerWorldInJoinFeedback(Logger logger, String message, Object p0, Object p1, Object p2, Object p3,
            Object p4, Object p5, NetworkManager manager, ServerPlayerEntity entity) {
        logger.info("{}[{}] logged in to world '{}' with entity id {} at ({}, {}, {})", p0, p1, ((org.spongepowered.api.world.server.ServerWorld) entity.getServerWorld()).getKey(), p2, p3, p4, p5);
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "NEW", target = "net/minecraft/network/play/server/SJoinGamePacket"))
    private SJoinGamePacket impl$onInitPlayer_sendFakeDimensionTypeForVanillaClient(final int entityId, final GameType gameType, final long seed,
            final boolean hardcoreMode, final DimensionType dimensionType, final int maxPlayers, final WorldType worldType, final int viewDistance,
            final boolean reducedDebugInfo, final boolean enableRespawnScreen, final NetworkManager manager, final ServerPlayerEntity entity) {
        ((ServerPlayerEntityBridge) entity).bridge$sendDimensionData(manager, dimensionType);
        return PlatformHooks.getInstance().getPacketHooks().createSJoinGamePacket(entity, gameType, seed, hardcoreMode, dimensionType, maxPlayers,
                worldType, viewDistance, reducedDebugInfo, enableRespawnScreen);
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    private void impl$onInitPlayer_delaySendMessage(final PlayerList playerList, final ITextComponent message) {
        // Don't send here, will be done later
    }

    @Inject(method = "initializeConnectionToPlayer", at = @At(value = "RETURN"))
    private void impl$onInitPlayer_join(final NetworkManager networkManager, final ServerPlayerEntity mcPlayer, final CallbackInfo ci) {
        final GameProfile previousGameProfile = ((ServerPlayerEntityBridge) mcPlayer).bridge$getPreviousGameProfile();
        final String previousName = previousGameProfile == null ? "" : previousGameProfile.getName().orElse(null);

        final ITextComponent joinMessage;
        if (mcPlayer.getGameProfile().getName().equalsIgnoreCase(previousName)) {
            joinMessage = new TranslationTextComponent("multiplayer.player.joined", mcPlayer.getDisplayName());
        } else {
            joinMessage = new TranslationTextComponent("multiplayer.player.joined.renamed", mcPlayer.getDisplayName(), previousName);
        }
        joinMessage.getStyle().setColor(TextFormatting.YELLOW);

        final ServerPlayer player = (ServerPlayer) mcPlayer;
        final ServerSideConnection connection = player.getConnection();
        final Cause cause = Cause.of(EventContext.empty(), connection, player);
        final Audience audience = Audiences.onlinePlayers();
        final Component joinComponent = SpongeAdventure.asAdventure(joinMessage);

        final ServerSideConnectionEvent.Join event = SpongeEventFactory.createServerSideConnectionEventJoin(cause, audience,
                Optional.of(audience), joinComponent, joinComponent, connection, player, false);
        SpongeCommon.postEvent(event);
        if (!event.isMessageCancelled()) {
            event.getAudience().ifPresent(audience1 -> audience1.sendMessage(event.getMessage()));
        }

        ((ServerPlayerEntityBridge) mcPlayer).bridge$setPreviousGameProfile(null);
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private CustomServerBossInfoManager impl$getPerWorldBossBarManager(MinecraftServer minecraftServer, NetworkManager netManager, ServerPlayerEntity playerIn) {
        return ((ServerWorldBridge) playerIn.getServerWorld()).bridge$getBossBarManager();
    }

    @Redirect(method = "playerLoggedOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private CustomServerBossInfoManager impl$getPerWorldBossBarManager(MinecraftServer minecraftServer, ServerPlayerEntity playerIn) {
        return ((ServerWorldBridge) playerIn.getServerWorld()).bridge$getBossBarManager();
    }

    @Redirect(method = "func_212504_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;getSaveHandler()Lnet/minecraft/world/storage/SaveHandler;"))
    private SaveHandler impl$onlyUseOverworldForPlayerData(ServerWorld serverWorld) {
        if (serverWorld.dimension.getType() == DimensionType.OVERWORLD) {
            return serverWorld.getSaveHandler();
        }

        return SpongeCommon.getServer().getWorld(DimensionType.OVERWORLD).getSaveHandler();
    }
}
