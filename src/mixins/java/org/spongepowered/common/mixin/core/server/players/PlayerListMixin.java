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
package org.spongepowered.common.mixin.core.server.players;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.network.protocol.game.ClientboundRespawnPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.management.PlayerListBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.server.PerWorldBorderListener;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int viewDistance;

    @Shadow public abstract net.minecraft.network.chat.Component shadow$canPlayerLogin(SocketAddress socketAddress, com.mojang.authlib.GameProfile gameProfile);
    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow @Nullable public abstract CompoundTag shadow$load(net.minecraft.server.level.ServerPlayer playerIn);
    // @formatter:on

    private boolean impl$isGameMechanicRespawn = false;
    ResourceKey<Level> impl$newDestination = null;
    ResourceKey<Level> impl$originalDestination = null;

    @Override
    public void bridge$setOriginalDestinationDimension(final ResourceKey<Level> dimension) {
        this.impl$originalDestination = dimension;
    }

    @Override
    public void bridge$setNewDestinationDimension(final ResourceKey<Level> dimension) {
        this.impl$newDestination = dimension;
    }

    @Redirect(method = "placeNewPlayer",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/nbt/CompoundTag;"
        )
    )
    private CompoundTag impl$setPlayerDataForNewPlayers(final PlayerList playerList, final net.minecraft.server.level.ServerPlayer playerIn) {
        final SpongeUser user = (SpongeUser) ((ServerPlayerEntityBridge) playerIn).bridge$getUserObject();
        if (user != null) {
            if (SpongeUser.dirtyUsers.contains(user)) {
                user.save();
            }
            user.invalidate();
        }

        final CompoundTag compound = this.shadow$load(playerIn);
        if (compound == null) {
            ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().setPlayerInfo(playerIn.getUUID(), Instant.now(), Instant.now());
        }
        return compound;
    }
    
    @Redirect(method = "placeNewPlayer",
        at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
        )
    )
    private net.minecraft.server.level.ServerLevel impl$onInitPlayer_getWorld(final MinecraftServer minecraftServer,
        final ResourceKey<Level> dimension, final Connection networkManager, final net.minecraft.server.level.ServerPlayer mcPlayer
    ) {
        @Nullable final net.minecraft.network.chat.Component kickReason = this.shadow$canPlayerLogin(networkManager.getRemoteAddress(), mcPlayer.getGameProfile());
        final Component disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeAdventure.asAdventure(kickReason);
        } else {
            disconnectMessage = Component.text("You are not allowed to log in to this server.");
        }

        net.minecraft.server.level.ServerLevel mcWorld = minecraftServer.getLevel(dimension);

        if (mcWorld == null) {
            SpongeCommon.getLogger().warn("The player '{}' was located in a world that isn't loaded or doesn't exist. This is not safe so "
                            + "the player will be moved to the spawn of the default world.", mcPlayer.getGameProfile().getName());
            mcWorld = minecraftServer.overworld();
            final BlockPos spawnPoint = mcWorld.getSharedSpawnPos();
            mcPlayer.setPos(spawnPoint.getX() + 0.5, spawnPoint.getY() + 0.5, spawnPoint.getZ() + 0.5);
        }

        mcPlayer.setLevel(mcWorld);

        final ServerPlayer player = (ServerPlayer) mcPlayer;
        final ServerLocation location = player.getServerLocation();
        final Vector3d rotation = player.getRotation();
        // player.getConnection() cannot be used here, because it's still be null at this point
        final ServerSideConnection connection = (ServerSideConnection) networkManager.getPacketListener();
        final User user = player.getUser();

        final Cause cause = Cause.of(EventContext.empty(), connection, user);
        final ServerSideConnectionEvent.Login event = SpongeEventFactory.createServerSideConnectionEventLogin(cause, disconnectMessage,
                disconnectMessage, location, location, rotation, rotation, connection, user);
        if (kickReason != null) {
            event.setCancelled(true);
        }
        if (SpongeCommon.postEvent(event)) {
            this.impl$disconnectClient(networkManager, event.getMessage(), player.profile());
            return null;
        }

        final ServerLocation toLocation = event.getToLocation();
        final Vector3d toRotation = event.getToRotation();
        mcPlayer.absMoveTo(toLocation.getX(), toLocation.getY(), toLocation.getZ(),
                (float) toRotation.getY(), (float) toRotation.getX());
        return (net.minecraft.server.level.ServerLevel) toLocation.getWorld();
    }

    @Inject(method = "placeNewPlayer",
        cancellable = true,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;",
            shift = At.Shift.AFTER
        )
    )
    private void impl$onInitPlayer_BeforeSetWorld(final Connection p_72355_1_, final net.minecraft.server.level.ServerPlayer p_72355_2_, final CallbackInfo ci) {
        if (p_72355_2_.level == null) {
            ci.cancel();
        }
    }

    @Redirect(method = "placeNewPlayer",
        at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false
        )
    )
    private void impl$onInitPlayer_printPlayerWorldInJoinFeedback(
            final Logger logger, final String message, final Object p0, final Object p1, final Object p2, final Object p3,
            final Object p4, final Object p5, final Connection manager, final net.minecraft.server.level.ServerPlayer entity) {
        logger.info("{}[{}] logged in to world '{}' with entity id {} at ({}, {}, {})", p0, p1, ((org.spongepowered.api.world.server.ServerWorld) entity.getLevel()).getKey(), p2, p3, p4, p5);
    }

    @Redirect(method = "placeNewPlayer",
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/MinecraftServer;invalidateStatus()V"),
            to = @At(
                value = "FIELD",
                opcode = Opcodes.GETSTATIC,
                target = "Lnet/minecraft/ChatFormatting;YELLOW:Lnet/minecraft/ChatFormatting;"
            )
        ),
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;equalsIgnoreCase(Ljava/lang/String;)Z"
        )
    )
    private boolean impl$onInitPlayer_dontClassSpongeNameAsModified(final String currentName, final String originalName) {
        if (originalName.equals(Constants.GameProfile.DUMMY_NAME)) {
            return true;
        }
        return currentName.equalsIgnoreCase(originalName);
    }

    @Redirect(method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
        )
    )
    private void impl$onInitPlayer_delaySendMessage(
        final PlayerList playerList,
        final net.minecraft.network.chat.Component message,
        final ChatType p_232641_2_,
        final UUID p_232641_3_,
        final Connection manager,
        final net.minecraft.server.level.ServerPlayer playerIn
    ) {
        // Don't send here, will be done later. We cache the expected message.
        ((ServerPlayerEntityBridge) playerIn).bridge$setConnectionMessageToSend(message);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundLoginPacket"))
    private ClientboundLoginPacket impl$usePerWorldViewDistance(final int p_i242082_1_, final GameType p_i242082_2_, final GameType p_i242082_3_,
            final long p_i242082_4_, final boolean p_i242082_6_, final Set<ResourceKey<Level>> p_i242082_7_,
            final RegistryAccess.RegistryHolder p_i242082_8_, final DimensionType p_i242082_9_, final ResourceKey<Level> p_i242082_10_,
            final int p_i242082_11_, final int p_i242082_12_, final boolean p_i242082_13_, final boolean p_i242082_14_,
            final boolean p_i242082_15_, final boolean p_i242082_16_, Connection p_72355_1_, net.minecraft.server.level.ServerPlayer player) {

        return new ClientboundLoginPacket(p_i242082_1_, p_i242082_2_, p_i242082_3_, p_i242082_4_, p_i242082_6_, p_i242082_7_, p_i242082_8_, p_i242082_9_,
                p_i242082_10_, p_i242082_11_, ((ServerWorldInfoBridge) player.getLevel().getLevelData()).bridge$viewDistance().orElse(this.viewDistance),
                p_i242082_13_, p_i242082_14_, p_i242082_15_, p_i242082_16_);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/bossevents/CustomBossEvents;"))
    private CustomBossEvents impl$getPerWorldBossBarManager(
            final MinecraftServer minecraftServer, final Connection netManager, final net.minecraft.server.level.ServerPlayer playerIn) {
        return ((ServerWorldBridge) playerIn.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;updateEntireScoreboard(Lnet/minecraft/server/ServerScoreboard;Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void impl$sendScoreboard(final PlayerList playerList, final ServerScoreboard scoreboardIn, final net.minecraft.server.level.ServerPlayer playerIn) {
        ((ServerPlayerEntityBridge)playerIn).bridge$initScoreboard();
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
    private void impl$onInitPlayer_join(final Connection networkManager, final net.minecraft.server.level.ServerPlayer mcPlayer, final CallbackInfo ci) {
        final ServerPlayer player = (ServerPlayer) mcPlayer;
        final ServerSideConnection connection = player.getConnection();
        final Cause cause = Cause.of(EventContext.empty(), connection, player);
        final Audience audience = Audiences.onlinePlayers();
        final Component joinComponent = SpongeAdventure.asAdventure(((ServerPlayerEntityBridge) mcPlayer).bridge$getConnectionMessageToSend());

        final ServerSideConnectionEvent.Join event = SpongeEventFactory.createServerSideConnectionEventJoin(cause, audience,
                Optional.of(audience), joinComponent, joinComponent, connection, player, false);
        SpongeCommon.postEvent(event);
        if (!event.isMessageCancelled()) {
            event.getAudience().ifPresent(audience1 -> audience1.sendMessage(Identity.nil(), event.getMessage()));
        }

        ((ServerPlayerEntityBridge) mcPlayer).bridge$setConnectionMessageToSend(null);
    }

    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/bossevents/CustomBossEvents;"))
    private CustomBossEvents impl$getPerWorldBossBarManager(final MinecraftServer minecraftServer, final net.minecraft.server.level.ServerPlayer playerIn) {
        return ((ServerWorldBridge) playerIn.getLevel()).bridge$getBossBarManager();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void impl$RemovePlayerReferenceFromScoreboard(final net.minecraft.server.level.ServerPlayer player, final CallbackInfo ci) {
        ((ServerScoreboardBridge) ((ServerPlayer) player).getScoreboard()).bridge$removePlayer(player, false);
    }

    @Redirect(method = "setLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V"
        )
    )
    private void impl$usePerWorldBorderListener(final WorldBorder worldBorder, final BorderChangeListener listener, final ServerLevel serverWorld) {
        worldBorder.addListener(new PerWorldBorderListener(serverWorld));
    }

    @Redirect(method = "load",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;load(Lnet/minecraft/nbt/CompoundTag;)V"
        )
    )
    private void impl$setSpongePlayerDataForSinglePlayer(final net.minecraft.server.level.ServerPlayer entity, final CompoundTag compound) {
        entity.load(compound);

        ((SpongeServer) this.shadow$getServer()).getPlayerDataManager().readPlayerData(compound, entity.getUUID(), null);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Redirect(
        method = "respawn",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Optional;isPresent()Z",
            remap = false
        ),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;", remap = false),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isDemo()Z")
        )
    )
    private boolean impl$flagIfRespawnLocationIsGameMechanic(final Optional<?> optional) {
        this.impl$isGameMechanicRespawn = optional.isPresent();
        return this.impl$isGameMechanicRespawn;
    }

    @Redirect(method = "respawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 1
        )
    )
    private void impl$callRespawnPlayerRecreateEvent(
        final ServerGamePacketListenerImpl serverPlayNetHandler, final Packet<?> packetIn, final net.minecraft.server.level.ServerPlayer originalPlayer, final boolean keepAllPlayerData) {
        final net.minecraft.server.level.ServerPlayer recreatedPlayer = serverPlayNetHandler.player;

        final Vector3d originalPosition = VecHelper.toVector3d(originalPlayer.position());
        final Vector3d destinationPosition = VecHelper.toVector3d(recreatedPlayer.position());
        final org.spongepowered.api.world.server.ServerWorld originalWorld = (org.spongepowered.api.world.server.ServerWorld) originalPlayer.level;
        final org.spongepowered.api.world.server.ServerWorld originalDestinationWorld = (org.spongepowered.api.world.server.ServerWorld) this.server.getLevel(this.impl$originalDestination == null ? Level.OVERWORLD : this.impl$originalDestination);
        final org.spongepowered.api.world.server.ServerWorld destinationWorld = (org.spongepowered.api.world.server.ServerWorld) this.server.getLevel(this.impl$newDestination == null ? Level.OVERWORLD : this.impl$newDestination);

        final RespawnPlayerEvent.Recreate event = SpongeEventFactory.createRespawnPlayerEventRecreate(PhaseTracker.getCauseStackManager().getCurrentCause(), destinationPosition, originalWorld, originalPosition, destinationWorld, originalDestinationWorld, destinationPosition, (ServerPlayer) originalPlayer, (ServerPlayer) recreatedPlayer, this.impl$isGameMechanicRespawn, !keepAllPlayerData);
        SpongeCommon.postEvent(event);
        recreatedPlayer.setPos(event.getDestinationPosition().getX(), event.getDestinationPosition().getY(), event.getDestinationPosition().getZ());
        this.impl$isGameMechanicRespawn = false;
        this.impl$originalDestination = null;
        this.impl$newDestination = null;

        final ServerLevel targetWorld = (ServerLevel) event.getDestinationWorld();
        ((ServerPlayerEntityBridge) recreatedPlayer).bridge$sendChangeDimension(
            targetWorld.dimensionType(),
            ((ClientboundRespawnPacketAccessor) packetIn).accessor$dimension(),
            ((ClientboundRespawnPacketAccessor) packetIn).accessor$seed(),
            recreatedPlayer.gameMode.getGameModeForPlayer(),
            recreatedPlayer.gameMode.getPreviousGameModeForPlayer(),
            targetWorld.isDebug(),
            targetWorld.isFlat(),
            keepAllPlayerData
        );
    }

    @Redirect(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel impl$usePerWorldWorldBorder(final MinecraftServer minecraftServer, final net.minecraft.server.level.ServerPlayer playerIn,
            final ServerLevel worldIn) {
        return worldIn;
    }

    private void impl$disconnectClient(final Connection netManager, final Component disconnectMessage, final @Nullable GameProfile profile) {
        final net.minecraft.network.chat.Component reason = SpongeAdventure.asVanilla(disconnectMessage);

        try {
            PlayerListMixin.LOGGER.info("Disconnecting " + (profile != null ? profile.toString() + " (" + netManager.getRemoteAddress().toString() + ")" :
                    netManager.getRemoteAddress() + ": " + reason.getString()));
            netManager.send(new ClientboundDisconnectPacket(reason));
            netManager.disconnect(reason);
        } catch (final Exception exception) {
            PlayerListMixin.LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    @Inject(method = "saveAll()V", at = @At("RETURN"))
    private void impl$saveOrInvalidateUserDuringSaveAll(final CallbackInfo ci) {
        for (final SpongeUser user : SpongeUser.initializedUsers) {
            if (SpongeUser.dirtyUsers.contains(user)) {
                user.save();
            } else {
                user.invalidate();
            }
        }
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;save(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void impl$invalidateUserOnSave(final net.minecraft.server.level.ServerPlayer player, final CallbackInfo ci) {
        // Just to be safe ignore dirty users that exist on online player
        final User user = ((ServerPlayerEntityBridge) player).bridge$getUser();
        SpongeUser.dirtyUsers.remove(user);
        ((SpongeUser) user).invalidate();
    }

}
