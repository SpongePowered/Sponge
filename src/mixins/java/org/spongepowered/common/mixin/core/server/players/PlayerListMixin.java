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

import io.netty.channel.local.LocalAddress;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.network.protocol.game.ClientboundRespawnPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.entity.player.LoginPermissions;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.server.PerWorldBorderListener;
import org.spongepowered.common.service.server.ban.SpongeIPBanList;
import org.spongepowered.common.service.server.ban.SpongeUserBanList;
import org.spongepowered.common.service.server.whitelist.SpongeUserWhiteList;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static SimpleDateFormat BAN_DATE_FORMAT;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int viewDistance;
    @Shadow @Final @Mutable private UserBanList bans;
    @Shadow @Final @Mutable private IpBanList ipBans;
    @Shadow @Final @Mutable private UserWhiteList whitelist;
    @Shadow @Final private List<net.minecraft.server.level.ServerPlayer> players;
    @Shadow @Final protected int maxPlayers;

    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow @Nullable public abstract CompoundTag shadow$load(net.minecraft.server.level.ServerPlayer playerIn);
    @Shadow public abstract boolean shadow$canBypassPlayerLimit(com.mojang.authlib.GameProfile param0);
    // @formatter:on

    private boolean impl$isGameMechanicRespawn = false;
    ResourceKey<Level> impl$newDestination = null;
    ResourceKey<Level> impl$originalDestination = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setSpongeLists(final CallbackInfo callbackInfo) {
        this.bans = new SpongeUserBanList(PlayerList.USERBANLIST_FILE);
        this.ipBans = new SpongeIPBanList(PlayerList.IPBANLIST_FILE);
        this.whitelist = new SpongeUserWhiteList(PlayerList.WHITELIST_FILE);
    }

    @Override
    public void bridge$setOriginalDestinationDimension(final ResourceKey<Level> dimension) {
        this.impl$originalDestination = dimension;
    }

    @Override
    public void bridge$setNewDestinationDimension(final ResourceKey<Level> dimension) {
        this.impl$newDestination = dimension;
    }

    @Override
    public CompletableFuture<Optional<net.minecraft.network.chat.Component>> bridge$canPlayerLogin(final SocketAddress param0, final com.mojang.authlib.GameProfile param1) {
        final SpongeGameProfile profile = SpongeGameProfile.basicOf(param1);

        return Sponge.server().serviceProvider().banService().banFor(profile).<Optional<net.minecraft.network.chat.Component>>thenCompose(profileBanOpt -> {
            if (profileBanOpt.isPresent()) {
                final Ban.Profile var0 = profileBanOpt.get();
                final MutableComponent var1 = new TranslatableComponent("multiplayer.disconnect.banned.reason", var0.reason().orElse(Component.empty()));
                if (var0.expirationDate().isPresent()) {
                    var1.append(new TranslatableComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(var0.expirationDate().get())));
                }
                return CompletableFuture.completedFuture(Optional.of(var1));
            }

            if (param0 instanceof LocalAddress) { // don't bother looking up IP bans on local address
                return CompletableFuture.completedFuture(Optional.empty());
            }

            final InetAddress address;
            try {
                address = InetAddress.getByName(NetworkUtil.getHostString(param0));
            } catch (final UnknownHostException ex) {
                return CompletableFuture.completedFuture(Optional.of(new TextComponent(ex.getMessage()))); // no
            }
            return Sponge.server().serviceProvider().banService().banFor(address).thenCompose(ipBanOpt -> {
                if (ipBanOpt.isPresent()) {
                    final Ban.IP var2 = ipBanOpt.get();
                    final MutableComponent var3 = new TranslatableComponent("multiplayer.disconnect.banned_ip.reason", var2.reason().orElse(Component.empty()));
                    if (var2.expirationDate().isPresent()) {
                        var3.append(new TranslatableComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(var2.expirationDate().get())));
                    }
                    return CompletableFuture.completedFuture(Optional.of(var3));
                }

                return CompletableFuture.supplyAsync(() -> {
                    if (!Sponge.server().isWhitelistEnabled()) {
                        return true;
                    }
                    final PermissionService permissionService = Sponge.server().serviceProvider().permissionService();
                    Subject subject = permissionService.userSubjects().subject(param1.getId().toString()).orElse(null);
                    if (subject == null) {
                        subject = permissionService.defaults();
                    }
                    return subject.hasPermission(LoginPermissions.BYPASS_WHITELIST_PERMISSION);
                }, SpongeCommon.server()).thenCompose(w -> {
                    if (w) {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                    return Sponge.server().serviceProvider().whitelistService().isWhitelisted(profile).thenCompose(whitelisted -> {
                        if (!whitelisted) {
                            return CompletableFuture.completedFuture(Optional.of(new TranslatableComponent("multiplayer.disconnect.not_whitelisted")));
                        }
                        return CompletableFuture.completedFuture(Optional.empty());
                    });
                });
            });
        }).thenComposeAsync(componentOpt -> {
            if (componentOpt.isPresent()) {
                return CompletableFuture.completedFuture(componentOpt);
            }
            if (this.players.size() >= this.maxPlayers && !this.shadow$canBypassPlayerLimit(param1)) {
                return CompletableFuture.completedFuture(Optional.of(new TranslatableComponent("multiplayer.disconnect.server_full")));
            }
            return CompletableFuture.completedFuture(Optional.empty());
        }, SpongeCommon.server());
    }

    @Redirect(method = "placeNewPlayer",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/nbt/CompoundTag;"
        )
    )
    private CompoundTag impl$setPlayerDataForNewPlayers(final PlayerList playerList, final net.minecraft.server.level.ServerPlayer playerIn) {
        final SpongeUser user = (SpongeUser) ((ServerPlayerBridge) playerIn).bridge$getUserObject();
        if (user != null) {
            if (SpongeUser.dirtyUsers.contains(user)) {
                user.save();
            }
            user.invalidate();
        }

        final CompoundTag compound = this.shadow$load(playerIn);
        if (compound == null) {
            ((SpongeServer) SpongeCommon.server()).getPlayerDataManager().setPlayerInfo(playerIn.getUUID(), Instant.now(), Instant.now());
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
        @Nullable final net.minecraft.network.chat.Component kickReason = ((ConnectionBridge) networkManager).bridge$getKickReason();
        final Component disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeAdventure.asAdventure(kickReason);
        } else {
            disconnectMessage = Component.text("You are not allowed to log in to this server.");
        }

        net.minecraft.server.level.ServerLevel mcWorld = minecraftServer.getLevel(dimension);

        if (mcWorld == null) {
            SpongeCommon.logger().warn("The player '{}' was located in a world that isn't loaded or doesn't exist. This is not safe so "
                            + "the player will be moved to the spawn of the default world.", mcPlayer.getGameProfile().getName());
            mcWorld = minecraftServer.overworld();
            final BlockPos spawnPoint = mcWorld.getSharedSpawnPos();
            mcPlayer.setPos(spawnPoint.getX() + 0.5, spawnPoint.getY() + 0.5, spawnPoint.getZ() + 0.5);
        }

        mcPlayer.setLevel(mcWorld);

        final ServerPlayer player = (ServerPlayer) mcPlayer;
        final ServerLocation location = player.serverLocation();
        final Vector3d rotation = player.rotation();
        // player.connection() cannot be used here, because it's still be null at this point
        final ServerSideConnection connection = (ServerSideConnection) networkManager.getPacketListener();
        final User user = player.user();

        final Cause cause = Cause.of(EventContext.empty(), connection, user);
        final ServerSideConnectionEvent.Login event = SpongeEventFactory.createServerSideConnectionEventLogin(cause, disconnectMessage,
                disconnectMessage, location, location, rotation, rotation, connection, user);
        if (kickReason != null) {
            event.setCancelled(true);
        }
        if (SpongeCommon.post(event)) {
            this.impl$disconnectClient(networkManager, event.message(), player.profile());
            return null;
        }

        final ServerLocation toLocation = event.toLocation();
        final Vector3d toRotation = event.toRotation();
        mcPlayer.absMoveTo(toLocation.x(), toLocation.y(), toLocation.z(),
                (float) toRotation.y(), (float) toRotation.x());
        return (net.minecraft.server.level.ServerLevel) toLocation.world();
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
        if (!p_72355_1_.isConnected()) {
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
        logger.info("{}[{}] logged in to world '{}' with entity id {} at ({}, {}, {})", p0, p1, ((org.spongepowered.api.world.server.ServerWorld) entity.getLevel()).key(), p2, p3, p4, p5);
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
        ((ServerPlayerBridge) playerIn).bridge$setConnectionMessageToSend(message);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundLoginPacket"))
    private ClientboundLoginPacket impl$usePerWorldViewDistance(final int p_i242082_1_, final GameType p_i242082_2_, final GameType p_i242082_3_,
            final long p_i242082_4_, final boolean p_i242082_6_, final Set<ResourceKey<Level>> p_i242082_7_,
            final RegistryAccess.RegistryHolder p_i242082_8_, final DimensionType p_i242082_9_, final ResourceKey<Level> p_i242082_10_,
            final int p_i242082_11_, final int p_i242082_12_, final boolean p_i242082_13_, final boolean p_i242082_14_,
            final boolean p_i242082_15_, final boolean p_i242082_16_, Connection p_72355_1_, net.minecraft.server.level.ServerPlayer player) {

        return new ClientboundLoginPacket(p_i242082_1_, p_i242082_2_, p_i242082_3_, p_i242082_4_, p_i242082_6_, p_i242082_7_, p_i242082_8_, p_i242082_9_,
                p_i242082_10_, p_i242082_11_, ((PrimaryLevelDataBridge) player.getLevel().getLevelData()).bridge$viewDistance().orElse(this.viewDistance),
                p_i242082_13_, p_i242082_14_, p_i242082_15_, p_i242082_16_);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/bossevents/CustomBossEvents;"))
    private CustomBossEvents impl$getPerWorldBossBarManager(
            final MinecraftServer minecraftServer, final Connection netManager, final net.minecraft.server.level.ServerPlayer playerIn) {
        return ((ServerLevelBridge) playerIn.getLevel()).bridge$getBossBarManager();
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;updateEntireScoreboard(Lnet/minecraft/server/ServerScoreboard;Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void impl$sendScoreboard(final PlayerList playerList, final ServerScoreboard scoreboardIn, final net.minecraft.server.level.ServerPlayer playerIn) {
        ((ServerPlayerBridge)playerIn).bridge$initScoreboard();
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
    private void impl$onInitPlayer_join(final Connection networkManager, final net.minecraft.server.level.ServerPlayer mcPlayer, final CallbackInfo ci) {
        final ServerPlayer player = (ServerPlayer) mcPlayer;
        final ServerSideConnection connection = player.connection();
        final Cause cause = Cause.of(EventContext.empty(), connection, player);
        final Audience audience = Audiences.onlinePlayers();
        final Component joinComponent = SpongeAdventure.asAdventure(((ServerPlayerBridge) mcPlayer).bridge$getConnectionMessageToSend());

        final ServerSideConnectionEvent.Join event = SpongeEventFactory.createServerSideConnectionEventJoin(cause, audience,
                Optional.of(audience), joinComponent, joinComponent, connection, player, false);
        SpongeCommon.post(event);
        if (!event.isMessageCancelled()) {
            event.audience().ifPresent(audience1 -> audience1.sendMessage(Identity.nil(), event.message()));
        }

        ((ServerPlayerBridge) mcPlayer).bridge$setConnectionMessageToSend(null);
    }

    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCustomBossEvents()Lnet/minecraft/server/bossevents/CustomBossEvents;"))
    private CustomBossEvents impl$getPerWorldBossBarManager(final MinecraftServer minecraftServer, final net.minecraft.server.level.ServerPlayer playerIn) {
        return ((ServerLevelBridge) playerIn.getLevel()).bridge$getBossBarManager();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void impl$RemovePlayerReferenceFromScoreboard(final net.minecraft.server.level.ServerPlayer player, final CallbackInfo ci) {
        ((ServerScoreboardBridge) ((ServerPlayer) player).scoreboard()).bridge$removePlayer(player, false);
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

        final RespawnPlayerEvent.Recreate event = SpongeEventFactory.createRespawnPlayerEventRecreate(PhaseTracker.getCauseStackManager().currentCause(), destinationPosition, originalWorld, originalPosition, destinationWorld, originalDestinationWorld, destinationPosition, (ServerPlayer) originalPlayer, (ServerPlayer) recreatedPlayer, this.impl$isGameMechanicRespawn, !keepAllPlayerData);
        SpongeCommon.post(event);
        recreatedPlayer.setPos(event.destinationPosition().x(), event.destinationPosition().y(), event.destinationPosition().z());
        this.impl$isGameMechanicRespawn = false;
        this.impl$originalDestination = null;
        this.impl$newDestination = null;

        final ServerLevel targetWorld = (ServerLevel) event.destinationWorld();
        ((ServerPlayerBridge) recreatedPlayer).bridge$sendChangeDimension(
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

    @Inject(method = "respawn", at = @At("RETURN"))
    private void impl$callRespawnPlayerPostEvent(final net.minecraft.server.level.ServerPlayer player, final boolean keepAllPlayerData, final CallbackInfoReturnable<net.minecraft.server.level.ServerPlayer> cir) {
        final org.spongepowered.api.world.server.ServerWorld originalWorld = (org.spongepowered.api.world.server.ServerWorld) player.level;
        final org.spongepowered.api.world.server.ServerWorld originalDestinationWorld = (org.spongepowered.api.world.server.ServerWorld) this.server.getLevel(this.impl$originalDestination == null ? Level.OVERWORLD : this.impl$originalDestination);
        final org.spongepowered.api.world.server.ServerWorld destinationWorld = (org.spongepowered.api.world.server.ServerWorld) this.server.getLevel(this.impl$newDestination == null ? Level.OVERWORLD : this.impl$newDestination);

        final RespawnPlayerEvent.Post event = SpongeEventFactory.createRespawnPlayerEventPost(PhaseTracker.getCauseStackManager().currentCause(), destinationWorld, originalWorld, originalDestinationWorld, (ServerPlayer) cir.getReturnValue());
        SpongeCommon.post(event);
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
        final User user = ((ServerPlayerBridge) player).bridge$getUser();
        SpongeUser.dirtyUsers.remove(user);
        ((SpongeUser) user).invalidate();
    }

}
