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
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;
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
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.network.play.server.SRespawnPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.management.PlayerListBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.server.PerWorldBorderListener;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract ITextComponent shadow$canPlayerLogin(SocketAddress socketAddress, com.mojang.authlib.GameProfile gameProfile);
    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow @Nullable public abstract CompoundNBT shadow$load(ServerPlayerEntity playerIn);
    // @formatter:on

    private boolean impl$isGameMechanicRespawn = false;
    RegistryKey<World> impl$newDestination = null;
    RegistryKey<World> impl$originalDestination = null;

    @Override
    public void bridge$setOriginalDestinationDimension(final RegistryKey<World> dimension) {
        this.impl$originalDestination = dimension;
    }

    @Override
    public void bridge$setNewDestinationDimension(final RegistryKey<World> dimension) {
        this.impl$newDestination = dimension;
    }

    @Redirect(method = "placeNewPlayer",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;load(Lnet/minecraft/entity/player/ServerPlayerEntity;)Lnet/minecraft/nbt/CompoundNBT;"
        )
    )
    private CompoundNBT impl$setPlayerDataForNewPlayers(final PlayerList playerList, final ServerPlayerEntity playerIn) {
        final CompoundNBT compound = this.shadow$load(playerIn);
        if (compound == null) {
            ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().setPlayerInfo(playerIn.getUUID(), Instant.now(), Instant.now());
        }
        return compound;
    }
    
    @Redirect(method = "placeNewPlayer",
        at = @At(value = "INVOKE", 
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/util/RegistryKey;)Lnet/minecraft/world/server/ServerWorld;"
        )
    )
    private net.minecraft.world.server.ServerWorld impl$onInitPlayer_getWorld(final MinecraftServer minecraftServer,
        final RegistryKey<World> dimension, final NetworkManager networkManager, final ServerPlayerEntity mcPlayer
    ) {
        @Nullable final ITextComponent kickReason = this.shadow$canPlayerLogin(networkManager.getRemoteAddress(), mcPlayer.getGameProfile());
        final Component disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeAdventure.asAdventure(kickReason);
        } else {
            disconnectMessage = Component.text("You are not allowed to log in to this server.");
        }

        net.minecraft.world.server.ServerWorld mcWorld = minecraftServer.getLevel(dimension);

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
            this.impl$disconnectClient(networkManager, event.getMessage(), player.getProfile());
            return null;
        }

        final ServerLocation toLocation = event.getToLocation();
        final Vector3d toRotation = event.getToRotation();
        mcPlayer.absMoveTo(toLocation.getX(), toLocation.getY(), toLocation.getZ(),
                (float) toRotation.getY(), (float) toRotation.getX());
        return (net.minecraft.world.server.ServerWorld) toLocation.getWorld();
    }

    @Inject(method = "placeNewPlayer",
        cancellable = true,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/util/RegistryKey;)Lnet/minecraft/world/server/ServerWorld;",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void impl$onInitPlayer_BeforeSetWorld(final NetworkManager networkManager, final ServerPlayerEntity mcPlayer, final CallbackInfo ci,
            com.mojang.authlib.GameProfile gameprofile, PlayerProfileCache playerprofilecache, com.mojang.authlib.GameProfile gameprofile1, String s,
            CompoundNBT compoundnbt, RegistryKey registrykey, MinecraftServer var23, RegistryKey var24) {
        if (mcPlayer.level == null) {
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
            final Object p4, final Object p5, final NetworkManager manager, final ServerPlayerEntity entity) {
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
                target = "Lnet/minecraft/util/text/TextFormatting;YELLOW:Lnet/minecraft/util/text/TextFormatting;"
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
            target = "Lnet/minecraft/server/management/PlayerList;broadcastMessage(Lnet/minecraft/util/text/ITextComponent;Lnet/minecraft/util/text/ChatType;Ljava/util/UUID;)V"
        )
    )
    private void impl$onInitPlayer_delaySendMessage(
        final PlayerList playerList,
        final ITextComponent message,
        final ChatType p_232641_2_,
        final UUID p_232641_3_,
        NetworkManager manager,
        ServerPlayerEntity playerIn
    ) {
        // Don't send here, will be done later. We cache the expected message.
        ((ServerPlayerEntityBridge) playerIn).bridge$setConnectionMessageToSend(message);
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
    private void impl$onInitPlayer_join(final NetworkManager networkManager, final ServerPlayerEntity mcPlayer, final CallbackInfo ci) {
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

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private CustomServerBossInfoManager impl$getPerWorldBossBarManager(
            final MinecraftServer minecraftServer, final NetworkManager netManager, final ServerPlayerEntity playerIn) {
        return ((ServerWorldBridge) playerIn.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/CustomServerBossInfoManager;"))
    private CustomServerBossInfoManager impl$getPerWorldBossBarManager(final MinecraftServer minecraftServer, final ServerPlayerEntity playerIn) {
        return ((ServerWorldBridge) playerIn.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;updateEntireScoreboard(Lnet/minecraft/scoreboard/ServerScoreboard;Lnet/minecraft/entity/player/ServerPlayerEntity;)V"))
    private void impl$sendScoreboard(final PlayerList playerList, final ServerScoreboard scoreboardIn, final ServerPlayerEntity playerIn) {
        ((ServerPlayerEntityBridge)playerIn).bridge$initScoreboard();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void impl$RemovePlayerReferenceFromScoreboard(final ServerPlayerEntity player, final CallbackInfo ci) {
        ((ServerScoreboardBridge) ((ServerPlayer) player).getScoreboard()).bridge$removePlayer(player, false);
    }

    @Redirect(method = "setLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/border/WorldBorder;addListener(Lnet/minecraft/world/border/IBorderListener;)V"
        )
    )
    private void impl$usePerWorldBorderListener(final WorldBorder worldBorder, final IBorderListener listener, final ServerWorld serverWorld) {
        worldBorder.addListener(new PerWorldBorderListener(serverWorld));
    }

    @Redirect(method = "load",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ServerPlayerEntity;load(Lnet/minecraft/nbt/CompoundNBT;)V"
        )
    )
    private void impl$setSpongePlayerDataForSinglePlayer(final ServerPlayerEntity entity, final CompoundNBT compound) {
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
            target = "Lnet/minecraft/network/play/ServerPlayNetHandler;send(Lnet/minecraft/network/IPacket;)V",
            ordinal = 1
        )
    )
    private void impl$callRespawnPlayerRecreateEvent(
        final ServerPlayNetHandler serverPlayNetHandler, final IPacket<?> packetIn, final ServerPlayerEntity originalPlayer, final boolean keepAllPlayerData) {
        final ServerPlayerEntity recreatedPlayer = serverPlayNetHandler.player;

        final Vector3d originalPosition = VecHelper.toVector3d(originalPlayer.position());
        final Vector3d destinationPosition = VecHelper.toVector3d(recreatedPlayer.position());
        final org.spongepowered.api.world.server.ServerWorld originalWorld = (org.spongepowered.api.world.server.ServerWorld) originalPlayer.level;
        final org.spongepowered.api.world.server.ServerWorld originalDestinationWorld = (org.spongepowered.api.world.server.ServerWorld) this.server.getLevel(this.impl$originalDestination == null ? World.OVERWORLD : this.impl$originalDestination);
        final org.spongepowered.api.world.server.ServerWorld destinationWorld = (org.spongepowered.api.world.server.ServerWorld) this.server.getLevel(this.impl$newDestination == null ? World.OVERWORLD : this.impl$newDestination);

        final RespawnPlayerEvent.Recreate event = SpongeEventFactory.createRespawnPlayerEventRecreate(PhaseTracker.getCauseStackManager().getCurrentCause(), destinationPosition, originalWorld, originalPosition, destinationWorld, originalDestinationWorld, destinationPosition, (ServerPlayer) originalPlayer, (ServerPlayer) recreatedPlayer, this.impl$isGameMechanicRespawn, !keepAllPlayerData);
        SpongeCommon.postEvent(event);
        recreatedPlayer.setPos(event.getDestinationPosition().getX(), event.getDestinationPosition().getY(), event.getDestinationPosition().getZ());
        this.impl$isGameMechanicRespawn = false;
        this.impl$originalDestination = null;
        this.impl$newDestination = null;

        final ServerWorld targetWorld = (ServerWorld) event.getDestinationWorld();
        ((ServerPlayerEntityBridge) recreatedPlayer).bridge$sendChangeDimension(
            targetWorld.dimensionType(),
            ((SRespawnPacketAccessor) packetIn).accessor$dimension(),
            ((SRespawnPacketAccessor) packetIn).accessor$seed(),
            recreatedPlayer.gameMode.getGameModeForPlayer(),
            recreatedPlayer.gameMode.getPreviousGameModeForPlayer(),
            targetWorld.isDebug(),
            targetWorld.isFlat(),
            keepAllPlayerData
        );
    }

    @Redirect(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/world/server/ServerWorld;"))
    private ServerWorld impl$usePerWorldWorldBorder(final MinecraftServer minecraftServer, final ServerPlayerEntity playerIn,
            final ServerWorld worldIn) {
        return worldIn;
    }

    private void impl$disconnectClient(final NetworkManager netManager, final Component disconnectMessage, final @Nullable GameProfile profile) {
        final ITextComponent reason = SpongeAdventure.asVanilla(disconnectMessage);

        try {
            PlayerListMixin.LOGGER.info("Disconnecting " + (profile != null ? profile.toString() + " (" + netManager.getRemoteAddress().toString() + ")" :
                    netManager.getRemoteAddress() + ": " + reason.getString()));
            netManager.send(new SDisconnectPacket(reason));
            netManager.disconnect(reason);
        } catch (final Exception exception) {
            PlayerListMixin.LOGGER.error("Error whilst disconnecting player", exception);
        }
    }
}
