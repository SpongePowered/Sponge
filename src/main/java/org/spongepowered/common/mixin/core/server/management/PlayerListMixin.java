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

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.IPBanList;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.WhiteList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IPlayerFileData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.dimension.Dimension;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.network.play.server.SWorldBorderPacketBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.management.PlayerListBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.player.PlayerPhase;
import org.spongepowered.common.server.PerWorldBorderListener;
import org.spongepowered.common.service.ban.SpongeIPBanList;
import org.spongepowered.common.service.ban.SpongeUserListBans;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.whitelist.SpongeUserListWhitelist;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;
import org.spongepowered.math.vector.Vector3d;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {

    @Shadow @Final private Map<UUID, ServerPlayerEntity> uuidToPlayerMap;
    @Shadow private IPlayerFileData playerDataManager;
    @Shadow @Final private Map<UUID, PlayerAdvancements> advancements;

    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow protected abstract void shadow$setPlayerGameTypeBasedOnOther(ServerPlayerEntity target, ServerPlayerEntity source, IWorld worldIn);
    @Shadow public abstract void shadow$sendWorldInfo(ServerPlayerEntity playerIn, ServerWorld worldIn);
    @Shadow public abstract void shadow$updatePermissionLevel(ServerPlayerEntity player);
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers();

    @Nullable private DimensionType impl$updatePermissionLevelDimensionType;

    /**
     * @author Minecrell - Minecraft 1.14.4
     * @reason Redirect ban list constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link BanService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListBans"))
    private BanList impl$createBanList(File file) {
        return new SpongeUserListBans(file);
    }

    /**
     * @author Minecrell - Minecraft 1.14.4
     * @reason Redirect IP ban list constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link BanService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListIPBans"))
    private IPBanList impl$createIPBanList(File file) {
        return new SpongeIPBanList(file);
    }

    /**
     * @author Minecrell - Minecraft 1.14.4
     * @reason Redirect whitelist constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link WhitelistService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListWhitelist"))
    private WhiteList impl$createWhitelist(File file) {
        return new SpongeUserListWhitelist(file);
    }

    /**
     * @author Zidane - Minecraft 1.14.4
     * @reason - Direct respawning players to use Sponge events and process appropriately.
     */
    @Overwrite
    public ServerPlayerEntity recreatePlayerEntity(ServerPlayerEntity player, DimensionType targetDimension, boolean conqueredEnd) {
        // Vanilla will always use overworld, set to the world the player was in
        // UNLESS coming back from the end.
        if (!conqueredEnd && targetDimension == DimensionType.OVERWORLD) {
            targetDimension = player.dimension;
        }

        final Transform fromTransform = ((Player) player).getTransform();
        final ServerWorld fromWorld = player.getServerWorld();
        ServerWorld toWorld = this.shadow$getServer().getWorld(targetDimension);
        Location toLocation;
        final Location temp = ((World) player.getServerWorld()).getSpawnLocation();
        boolean tempIsBedSpawn = false;
        if (toWorld == null) { // Target world doesn't exist? Use global
            toLocation = temp;
        } else {
            DimensionType toDimensionType = toWorld.getDimension().getType();
            // Cannot respawn in requested world, use the fallback dimension for
            // that world. (Usually overworld unless a mod says otherwise).
            if (!((Dimension) toWorld.getDimension()).allowsPlayerRespawns()) {
                toDimensionType = SpongeImplHooks.getRespawnDimensionType(toWorld.getDimension(), player);
                toWorld = toWorld.getServer().getWorld(toDimensionType);
            }

            Vector3d targetSpawnVec = VecHelper.toVector3d(SpongeImplHooks.getRandomizedSpawnPoint(toWorld));
            final BlockPos bedPos = SpongeImplHooks.getBedLocation(player, toDimensionType);
            if (bedPos != null) { // Player has a bed
                final boolean forceBedSpawn = SpongeImplHooks.isSpawnForced(player, toDimensionType);
                Vec3d bedSpawnLoc = PlayerEntity.func_213822_a(toWorld, bedPos, forceBedSpawn).orElse(null);
                if (bedSpawnLoc != null) { // The bed exists and is not obstructed
                    tempIsBedSpawn = true;
                    targetSpawnVec = new Vector3d(bedSpawnLoc.getX() + 0.5D, bedSpawnLoc.getY() + 0.1D, bedSpawnLoc.getZ() + 0.5D);
                } else { // Bed invalid
                    player.connection.sendPacket(new SChangeGameStatePacket(0, 0.0F));
                }
            }
            toLocation = Location.of((World) toWorld, targetSpawnVec);
        }

        Transform toTransform = Transform.of(toLocation.getPosition(), ((Player) player).getRotation(), Vector3d.ONE);
        targetDimension = ((ServerWorld) toLocation.getWorld()).getDimension().getType();

        // If coming from end, fire a teleport event for plugins
        if (conqueredEnd) {
            // When leaving the end, players are never placed inside the teleporter but instead "respawned" in the target world
            final MoveEntityEvent.Teleport teleportEvent = EntityUtil.handleDisplaceEntityTeleportEvent(player, toLocation);
            if (teleportEvent.isCancelled()) {
                player.queuedEndExit = false;
                return player;
            }

            toTransform = teleportEvent.getToTransform();
            toLocation = Location.of(teleportEvent.getToWorld(), toTransform.getPosition());
        }

        this.shadow$getPlayers().remove(player);
        player.getServerWorld().removePlayer(player);

        // Recreate the player object in order to support Forge's PlayerEvent.Clone
        final PlayerInteractionManager playerinteractionmanager;

        if (this.shadow$getServer().isDemo()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(this.shadow$getServer().getWorld(targetDimension));
        } else {
            playerinteractionmanager = new PlayerInteractionManager(this.shadow$getServer().getWorld(targetDimension));
        }

        final ServerPlayerEntity newPlayer = new ServerPlayerEntity(SpongeImpl.getServer(), toWorld, player.getGameProfile(), playerinteractionmanager);
        ((EntityBridge) newPlayer).bridge$setLocationAndAngles(toTransform);
        ((ServerPlayerEntityBridge) player).bridge$setDelegateAfterRespawn(newPlayer);

        newPlayer.connection = player.connection;
        newPlayer.copyFrom(player, conqueredEnd);
        newPlayer.setEntityId(player.getEntityId());
        newPlayer.setPrimaryHand(player.getPrimaryHand());

        for (final String s : player.getTags()) {
            newPlayer.addTag(s);
        }

        final BlockPos bedPos = SpongeImplHooks.getBedLocation(player, targetDimension);

        // Sponge - Vanilla does this before recreating the player entity. However, we need to determine the bed location
        // before respawning the player, so we know what dimension to spawn them into. This means that the bed location must be copied
        // over to the new player
        if (bedPos != null && tempIsBedSpawn) {
            newPlayer.setSpawnPoint(bedPos, player.isSpawnForced());
        }

        this.shadow$setPlayerGameTypeBasedOnOther(newPlayer, player, toWorld);

        ((ServerPlayerEntityBridge) newPlayer).bridge$setScoreboardOnRespawn(((Player) player).getScoreboard());
        ((ServerPlayerEntityBridge) player).bridge$removeScoreboardOnRespawn();

        // Keep players out of blocks
        newPlayer.setPosition(toLocation.getX(), toLocation.getY(), toLocation.getZ());
        while (!((ServerWorld) toLocation.getWorld()).areCollisionShapesEmpty(player) && player.posY < 256.0D) {
            newPlayer.setPosition(player.posX, player.posY + 1.0D, player.posZ);
        }

        Sponge.getCauseStackManager().pushCause(newPlayer);
        final RespawnPlayerEvent event = SpongeEventFactory.createRespawnPlayerEvent(Sponge.getCauseStackManager().getCurrentCause(), (Player) player,
            (Player) newPlayer, fromTransform, toTransform,  (World) fromWorld, (World) toWorld, tempIsBedSpawn, !conqueredEnd);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        ((EntityBridge) newPlayer).bridge$setLocationAndAngles(event.getToTransform());
        toLocation = Location.of(event.getToWorld(), event.getToTransform().getPosition());
        toWorld = (ServerWorld) toLocation.getWorld();

        // Set the dimension again in case a plugin changed the target world during RespawnPlayerEvent
        newPlayer.dimension = toWorld.getDimension().getType();
        newPlayer.setWorld(toWorld);
        newPlayer.interactionManager.setWorld(toWorld);

        // Send dimension registration
        if (((ServerPlayerEntityBridge) newPlayer).bridge$usesCustomClient()) {
            NetworkUtil.sendDimensionRegistration(newPlayer, toWorld.getDimension());
        } else {
            final SpongeDimensionType fromClientDimensionType = ((DimensionBridge) fromWorld.getDimension()).bridge$getClientDimensionType(newPlayer);
            final SpongeDimensionType toClientDimensionType = ((DimensionBridge) toWorld.getDimension()).bridge$getClientDimensionType(newPlayer);

            // Force vanilla client to refresh its chunk cache if same dimension type
            if (fromWorld != toWorld && fromClientDimensionType == toClientDimensionType) {
                newPlayer.connection.sendPacket(new SRespawnPacket(toClientDimensionType == DimensionTypes.OVERWORLD ? DimensionType.THE_NETHER :
                    toClientDimensionType == DimensionTypes.THE_END ? DimensionType.THE_NETHER : DimensionType.OVERWORLD,
                    toWorld.getWorldInfo().getGenerator(), newPlayer.interactionManager.getGameType()));
            }
        }

        newPlayer.connection.sendPacket(new SRespawnPacket(targetDimension, toWorld
                .getWorldInfo().getGenerator(), newPlayer.interactionManager.getGameType()));
        newPlayer.connection.setPlayerLocation(newPlayer.posX, newPlayer.posY, newPlayer.posZ, newPlayer.rotationYaw, newPlayer.rotationPitch);
        newPlayer.connection.sendPacket(new SServerDifficultyPacket(toWorld.getWorldInfo().getDifficulty(), toWorld.getWorldInfo().isDifficultyLocked()));
        newPlayer.connection.sendPacket(new SSetExperiencePacket(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));

        this.shadow$sendWorldInfo(newPlayer, toWorld);
        this.shadow$updatePermissionLevel(newPlayer);
        toWorld.addRespawnedPlayer(newPlayer);
        this.shadow$getPlayers().add(newPlayer);
        this.uuidToPlayerMap.put(newPlayer.getUniqueID(), newPlayer);
        newPlayer.addSelfToInternalCraftingInventory();

        // TODO 1.14 - Per-World Custom Boss Events
        // TODO 1.14 - Copy over data from previous player to current player

        for (final EffectInstance effect : newPlayer.getActivePotionEffects()) {
            newPlayer.connection.sendPacket(new SPlayEntityEffectPacket(newPlayer.getEntityId(), effect));
        }

        ((ServerPlayerEntityBridge) newPlayer).bridge$refreshScaledHealth();
        newPlayer.connection.sendPacket(new SHeldItemChangePacket(newPlayer.inventory.currentItem));
        SpongeCommonEventFactory.callPostPlayerRespawnEvent(newPlayer, conqueredEnd);

        return newPlayer;
    }

    @Inject(method = "func_212504_a", at = @At("HEAD"), cancellable = true)
    private void impl$usePlayerDataFromOverworldAndSetPerWorldBorderListener(ServerWorld world, CallbackInfo ci) {
        if (world.getDimension().getType().getId() == 0) {
            this.playerDataManager = world.getSaveHandler();
        }

        world.getWorldBorder().addListener(new PerWorldBorderListener(world));
    }

    @Redirect(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/world/server/ServerWorld;"))
    private ServerWorld impl$useProvidedWorldForWorldBorder(MinecraftServer minecraftServer, DimensionType dimension, ServerPlayerEntity player,
        ServerWorld world) {
        return world;
    }

    @Redirect(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/ServerPlayNetHandler;sendPacket(Lnet/minecraft/network/IPacket;)V", ordinal = 0))
    private void impl$adjustWorldBorderCoordinatesForMovementFactor(ServerPlayNetHandler handler, IPacket<?> packet, ServerPlayerEntity player, ServerWorld world) {
        if (((DimensionBridge) world.getDimension()).bridge$getMovementFactor() != 1.0f) {
            ((SWorldBorderPacketBridge) packet).bridge$changeCoordinatesForMovementFactor(((DimensionBridge) world.getDimension()).bridge$getMovementFactor());
        }

        handler.sendPacket(packet);
    }

    @Inject(method = "playerLoggedOut", at = @At("HEAD"))
    private void impl$RemovePlayerReferenceFromScoreboard(ServerPlayerEntity player, CallbackInfo ci) {
        ((ServerScoreboardBridge) ((Player) player).getScoreboard()).bridge$removePlayer(player, false);
    }

    @Redirect(method = "playerLoggedOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;removePlayer(Lnet/minecraft/entity/player/ServerPlayerEntity;)V"))
    private void impl$trackPlayerLogoutThroughPhaseTracker(ServerWorld world, ServerPlayerEntity player) {
        try (final GeneralizedContext context = PlayerPhase.State.PLAYER_LOGOUT.createPhaseContext().source(player)) {
            context.buildAndSwitch();
            world.removePlayer(player);
        }
    }

    @Inject(method = "saveAllPlayerData()V", at = @At("RETURN"))
    private void impl$saveAllSpongeUsers(CallbackInfo ci) {
        for (final SpongeUser user : SpongeUser.dirtyUsers) {
            user.save();
        }
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/IPacket;)V"))
    private void impl$sendJoiningPlayerToOthersIfVisibleByThem(PlayerList playerList, IPacket<?> packet, ServerPlayerEntity player) {
        final SPlayerListItemPacket noSpecificViewerPacket = new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, player);

        for (final ServerPlayerEntity otherPlayer : this.shadow$getPlayers()) {
            if (otherPlayer != player && ((Player) otherPlayer).canSee((Player) player)) {
                otherPlayer.connection.sendPacket(noSpecificViewerPacket);
            }

            if (otherPlayer == player || ((Player) player).canSee((Player) otherPlayer)) {
                player.connection.sendPacket(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, otherPlayer));
            }
        }
    }

    @Redirect(method = "initializeConnectionToPlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int impl$nullifyPlayerLoopToSendVisiblePlayers(List<ServerPlayerEntity> players) {
        // Looks odd, right? I don't want the original for statement to loop and redirects don't support locals so...nullify it as it is handled above
        return 0;
    }

    @Inject(method = "writePlayerData", at = @At(target = "Lnet/minecraft/world/storage/IPlayerFileData;writePlayerData(Lnet/minecraft/entity/player/PlayerEntity;)V", value = "INVOKE"))
    private void impl$saveSpongePlayerDataAfterSavingPlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        SpongePlayerDataHandler.savePlayer(player.getUniqueID());
    }

    @ModifyVariable(method = "sendPlayerPermissionLevel", at = @At("HEAD"), argsOnly = true)
    private int impl$updatePermLevel(int permLevel) {
        // If a non-default permission service is being used, then the op level will always be 0.
        // We force it to be 4 to ensure that the client is able to open command blocks (
        if (!(Sponge.getServiceManager().provideUnchecked(PermissionService.class) instanceof SpongePermissionService)) {
            return 4;
        }
        return permLevel;
    }

    @Redirect(method = "updatePermissionLevel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getPermissionLevel(Lcom/mojang/authlib/GameProfile;)I"))
    private int impl$setDimensionTypeThenQueryPermissionLevel(MinecraftServer server, GameProfile profile, ServerPlayerEntity player) {
        this.impl$updatePermissionLevelDimensionType = player.getServerWorld().getDimension().getType();
        final int permissionLevel = server.getPermissionLevel(player.getGameProfile());
        this.impl$updatePermissionLevelDimensionType = null;
        return permissionLevel;
    }

    @Redirect(method = "canSendCommands", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/world/server/ServerWorld;"))
    private ServerWorld impl$useDimensionTypeFromUpdatePermissionLevel(MinecraftServer server, DimensionType dimension) {
        if (this.impl$updatePermissionLevelDimensionType != null) {
            return server.getWorld(this.impl$updatePermissionLevelDimensionType);
        }
        return server.getWorld(dimension);
    }

    /**
     * @author simon816 - 14th November, 2016
     *
     * @reason Redirect chat broadcasts to fire an event for the message. Each receiver
     * (typically a player) will handle the actual sending of the message.
     *
     * @param component The message
     * @param isSystem Whether this is a system message
     */
    @Overwrite
    public void sendMessage(ITextComponent component, boolean isSystem) {
        ChatUtil.sendMessage(component, MessageChannel.toPlayersAndServer(), (CommandSource) this.server, !isSystem);
    }

    @Override
    public void bridge$reloadAdvancementProgress() {
        for (final PlayerAdvancements playerAdvancements : this.advancements.values()) {
            ((PlayerAdvancementsBridge) playerAdvancements).bridge$reloadAdvancementProgress();
        }
    }
}
