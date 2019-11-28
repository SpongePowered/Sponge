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
package org.spongepowered.common.mixin.core.server;

import com.flowpowered.math.vector.Vector3d;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListIPBans;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
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
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.packet.SPacketWorldBorderBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.management.PlayerListBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.player.PlayerPhase;
import org.spongepowered.common.service.ban.SpongeIPBanList;
import org.spongepowered.common.service.ban.SpongeUserListBans;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.whitelist.SpongeUserListWhitelist;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.io.File;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private Map<UUID, EntityPlayerMP> uuidToPlayerMap;
    @Shadow @Final private List<EntityPlayerMP> playerEntityList;
    @Shadow @Final private Map<UUID, PlayerAdvancements> advancements;
    @Shadow private IPlayerFileData playerDataManager;
    @Shadow public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn);
    @Shadow public abstract MinecraftServer getServerInstance();
    @Shadow public abstract int getMaxPlayers();
    @Shadow public abstract void sendPacketToAllPlayers(Packet<?> packetIn);
    @Shadow public abstract void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn);
    @Shadow public abstract void playerLoggedIn(EntityPlayerMP playerIn);
    @Shadow public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn);
    @Shadow public abstract void updatePermissionLevel(EntityPlayerMP p_187243_1_);
    @Shadow public abstract void syncPlayerInventory(EntityPlayerMP playerIn);
    @Nullable @Shadow public abstract String allowUserToConnect(SocketAddress address, GameProfile profile);
    @Shadow private void setPlayerGameTypeBasedOnOther(final EntityPlayerMP playerIn, @Nullable final EntityPlayerMP other, final net.minecraft.world.World worldIn) {
        // Shadowed
    }

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Redirect ban list constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link BanService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListBans"))
    private UserListBans createBanList(final File file) {
        return new SpongeUserListBans(file);
    }

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Redirect IP ban list constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link BanService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListIPBans"))
    private UserListIPBans createIPBanList(final File file) {
        return new SpongeIPBanList(file);
    }

    /**
     * @author Minecrell - December 4th, 2016
     * @reason Redirect whitelist constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link WhitelistService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListWhitelist"))
    private UserListWhitelist createWhitelist(final File file) {
        return new SpongeUserListWhitelist(file);
    }

    /**
     * @author Zidane - June 13th, 2015
     * @author simon816 - June 24th, 2015
     * @author Zidane - March 29th, 2016
     * @author gabizou - June 5th, 2016 - Update for teleportation changes to keep the same player.
     *
     * @reason - Direct respawning players to use Sponge events
     * and process appropriately.
     *
     * @param playerIn The player being respawned/created
     * @param targetDimension The target dimension
     * @param conqueredEnd Whether the end was conquered
     * @return The new player
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(final EntityPlayerMP playerIn, int targetDimension, final boolean conqueredEnd) {
        // ### PHASE 1 ### Get the location to spawn

        // Vanilla will always use overworld, set to the world the player was in
        // UNLESS comming back from the end.
        if (!conqueredEnd && targetDimension == 0) {
            targetDimension = playerIn.field_71093_bK;
        }

        if (playerIn.func_184207_aI()) {
            playerIn.func_184226_ay();
        }

        if (playerIn.func_184218_aH()) {
            playerIn.func_184210_p();
        }

        final Player player = (Player) playerIn;
        final Transform<World> fromTransform = player.getTransform();
        WorldServer worldServer = this.server.func_71218_a(targetDimension);
        final Location<World> toLocation;
        final Location<World> temp = ((World) playerIn.field_70170_p).getSpawnLocation();
        boolean tempIsBedSpawn = false;
        if (worldServer == null) { // Target world doesn't exist? Use global
            toLocation = temp;
        } else {
            final Dimension toDimension = (Dimension) worldServer.field_73011_w;
            int toDimensionId = ((WorldServerBridge) worldServer).bridge$getDimensionId();
            // Cannot respawn in requested world, use the fallback dimension for
            // that world. (Usually overworld unless a mod says otherwise).
            if (!toDimension.allowsPlayerRespawns()) {
                toDimensionId = SpongeImplHooks.getRespawnDimension((WorldProvider) toDimension, playerIn);
                worldServer = worldServer.func_73046_m().func_71218_a(toDimensionId);
            }

            Vector3d targetSpawnVec = VecHelper.toVector3d(SpongeImplHooks.getRandomizedSpawnPoint(worldServer));
            final BlockPos bedPos = SpongeImplHooks.getBedLocation(playerIn, toDimensionId);
            if (bedPos != null) { // Player has a bed
                final boolean forceBedSpawn = SpongeImplHooks.isSpawnForced(playerIn, toDimensionId);
                final BlockPos bedSpawnLoc = EntityPlayer.func_180467_a(worldServer, bedPos, forceBedSpawn);
                if (bedSpawnLoc != null) { // The bed exists and is not obstructed
                    tempIsBedSpawn = true;
                    targetSpawnVec = new Vector3d(bedSpawnLoc.func_177958_n() + 0.5D, bedSpawnLoc.func_177956_o() + 0.1D, bedSpawnLoc.func_177952_p() + 0.5D);
                } else { // Bed invalid
                    playerIn.field_71135_a.func_147359_a(new SPacketChangeGameState(0, 0.0F));
                }
            }
            toLocation = new Location<>((World) worldServer, targetSpawnVec);
        }

        Transform<World> toTransform = new Transform<>(toLocation, Vector3d.ZERO, Vector3d.ZERO);
        targetDimension = ((WorldServerBridge) toTransform.getExtent()).bridge$getDimensionId();
        Location<World> location = toTransform.getLocation();

        // If coming from end, fire a teleport event for plugins
        if (conqueredEnd) {
            // When leaving the end, players are never placed inside the teleporter but instead "respawned" in the target world
            final MoveEntityEvent.Teleport teleportEvent = EntityUtil.handleDisplaceEntityTeleportEvent(playerIn, location);
            if (teleportEvent.isCancelled()) {
                playerIn.field_71136_j = false;
                return playerIn;
            }

            toTransform = teleportEvent.getToTransform();
            location = toTransform.getLocation();
        }
        // Keep players out of blocks
        final Vector3d tempPos = player.getLocation().getPosition();
        playerIn.func_70107_b(location.getX(), location.getY(), location.getZ());
        while (!((WorldServer) location.getExtent()).func_184144_a(playerIn, playerIn.func_174813_aQ()).isEmpty() && location.getPosition().getY() < 256.0D) {
            playerIn.func_70107_b(playerIn.field_70165_t, playerIn.field_70163_u + 1.0D, playerIn.field_70161_v);
            location = location.add(0, 1, 0);
        }
        playerIn.func_70107_b(tempPos.getX(), tempPos.getY(), tempPos.getZ());

        // ### PHASE 2 ### Remove player from current dimension
        playerIn.func_71121_q().func_73039_n().func_72787_a(playerIn);
        playerIn.func_71121_q().func_73039_n().func_72790_b(playerIn);
        playerIn.func_71121_q().func_184164_w().func_72695_c(playerIn);
        this.playerEntityList.remove(playerIn);
        this.server.func_71218_a(playerIn.field_71093_bK).func_72973_f(playerIn);
        final BlockPos bedPos = SpongeImplHooks.getBedLocation(playerIn, targetDimension);

        // ### PHASE 3 ### Reset player (if applicable)
        // Recreate the player object in order to support Forge's PlayerEvent.Clone
        final PlayerInteractionManager playerinteractionmanager;

        if (this.server.func_71242_L()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(this.server.func_71218_a(targetDimension));
        } else {
            playerinteractionmanager = new PlayerInteractionManager(this.server.func_71218_a(targetDimension));
        }

        final EntityPlayerMP newPlayer = new EntityPlayerMP(SpongeImpl.getServer(), worldServer, playerIn.func_146103_bH(), playerinteractionmanager);
        newPlayer.field_71135_a = playerIn.field_71135_a;
        newPlayer.func_193104_a(playerIn, conqueredEnd);
        // set player dimension for RespawnPlayerEvent
        newPlayer.field_71093_bK = targetDimension;
        newPlayer.func_145769_d(playerIn.func_145782_y());
        newPlayer.func_174817_o(playerIn);
        newPlayer.func_184819_a(playerIn.func_184591_cq());

        // Sponge - Vanilla does this before recreating the player entity. However, we need to determine the bed location
        // before respawning the player, so we know what dimension to spawn them into. This means that the bed location must be copied
        // over to the new player
        if (bedPos != null && tempIsBedSpawn) {
            newPlayer.func_180473_a(bedPos, playerIn.func_82245_bX());
        }

        ((EntityPlayerMPBridge) newPlayer).bridge$setScoreboardOnRespawn(((Player) playerIn).getScoreboard());
        ((EntityPlayerMPBridge) playerIn).bridge$removeScoreboardOnRespawn();

        for (final String s : playerIn.func_184216_O()) {
            newPlayer.func_184211_a(s);
        }

        this.setPlayerGameTypeBasedOnOther(newPlayer, playerIn, worldServer);
        newPlayer.func_70095_a(false);

        ((EntityPlayerMPBridge) playerIn).bridge$setDelegateAfterRespawn(newPlayer);

        // update to safe location
        toTransform = toTransform.setLocation(location);

        // ### PHASE 4 ### Fire event and set new location on the player
        Sponge.getCauseStackManager().pushCause(newPlayer);
        final RespawnPlayerEvent event = SpongeEventFactory.createRespawnPlayerEvent(Sponge.getCauseStackManager().getCurrentCause(), fromTransform,
                toTransform, (Player) playerIn, (Player) newPlayer, tempIsBedSpawn, !conqueredEnd);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        ((EntityBridge) player).bridge$setLocationAndAngles(event.getToTransform());
        toTransform = event.getToTransform();
        location = toTransform.getLocation();

        if (!(location.getExtent() instanceof WorldServer)) {
            SpongeImpl.getLogger().warn("LocationBridge set in PlayerRespawnEvent was invalid, using original location instead");
            location = event.getFromTransform().getLocation();
        }
        worldServer = (WorldServer) location.getExtent();

        final WorldServerBridge mixinWorldServer = (WorldServerBridge) worldServer;
        // Set the dimension again in case a plugin changed the target world during RespawnPlayerEvent
        newPlayer.field_71093_bK = mixinWorldServer.bridge$getDimensionId();
        newPlayer.func_70029_a(worldServer);
        newPlayer.field_71134_c.func_73080_a(worldServer);

        worldServer.func_72863_F().func_186028_c((int) location.getX() >> 4, (int) location.getZ() >> 4);

        // ### PHASE 5 ### Respawn player in new world

        // Support vanilla clients logging into custom dimensions
        final int dimensionId = WorldManager.getClientDimensionId(newPlayer, worldServer);

        // Send dimension registration
        if (((EntityPlayerMPBridge) newPlayer).bridge$usesCustomClient()) {
            WorldManager.sendDimensionRegistration(newPlayer, worldServer.field_73011_w);
        } else {
            // Force vanilla client to refresh its chunk cache if same dimension type
            if (fromTransform.getExtent().getUniqueId() != ((World) worldServer).getUniqueId() && fromTransform.getExtent().getDimension().getType() ==
              toTransform.getExtent().getDimension().getType()) {
                newPlayer.field_71135_a.func_147359_a(new SPacketRespawn((dimensionId >= 0 ? -1 : 0), worldServer.func_175659_aa(), worldServer
                        .func_72912_H().func_76067_t(), newPlayer.field_71134_c.func_73081_b()));
            }
        }
        newPlayer.field_71135_a.func_147359_a(new SPacketRespawn(dimensionId, worldServer.func_175659_aa(), worldServer
                .func_72912_H().func_76067_t(), newPlayer.field_71134_c.func_73081_b()));
        newPlayer.field_71135_a.func_147359_a(new SPacketServerDifficulty(worldServer.func_175659_aa(), worldServer.func_72912_H().func_176123_z()));
        newPlayer.field_71135_a.func_147364_a(location.getX(), location.getY(), location.getZ(),
                (float) toTransform.getYaw(), (float) toTransform.getPitch());

        final BlockPos spawnLocation = worldServer.func_175694_M();
        newPlayer.field_71135_a.func_147359_a(new SPacketSpawnPosition(spawnLocation));
        newPlayer.field_71135_a.func_147359_a(new SPacketSetExperience(newPlayer.field_71106_cc, newPlayer.field_71067_cb,
                newPlayer.field_71068_ca));
        this.updateTimeAndWeatherForPlayer(newPlayer, worldServer);
        this.updatePermissionLevel(newPlayer);
        worldServer.func_184164_w().func_72683_a(newPlayer);
        final org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) newPlayer;
        ((org.spongepowered.api.world.World) worldServer).spawnEntity(spongeEntity);
        this.playerEntityList.add(newPlayer);
        newPlayer.field_71135_a.func_147359_a(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_GAME_MODE, newPlayer));
        for (DataManipulator<?, ?> container : ((Player) playerIn).getContainers()) {
            ((Player) newPlayer).offer(container);
        }
        this.uuidToPlayerMap.put(newPlayer.func_110124_au(), newPlayer);
        newPlayer.func_71116_b();

        // Update reducedDebugInfo game rule
        newPlayer.field_71135_a.func_147359_a(new SPacketEntityStatus(newPlayer,
                worldServer.func_82736_K().func_82766_b(DefaultGameRules.REDUCED_DEBUG_INFO) ? (byte) 22 : 23));

        for (final PotionEffect potioneffect : newPlayer.func_70651_bq()) {
            newPlayer.field_71135_a.func_147359_a(new SPacketEntityEffect(newPlayer.func_145782_y(), potioneffect));
        }
        ((EntityPlayerMPBridge) newPlayer).bridge$refreshScaledHealth();
        newPlayer.field_71135_a.func_147359_a(new SPacketHeldItemChange(playerIn.field_71071_by.field_70461_c));
        SpongeCommonEventFactory.callPostPlayerRespawnEvent(newPlayer, conqueredEnd);

        return newPlayer;
    }

    /**
     * @author Zidane
     * @reason Re-route to the common hook
     */
    @Overwrite
    public void transferEntityToWorld(final Entity entityIn, final int lastDimension, final WorldServer oldWorldIn, final WorldServer toWorldIn) {
        EntityUtil.transferEntityToWorld(entityIn, null, toWorldIn, (ForgeITeleporterBridge) toWorldIn.func_85176_s(), false);
    }

    /**
     * @author Zidane
     * @reason Re-route to the common hook
     */
    @Overwrite
    public void changePlayerDimension(final EntityPlayerMP player, final int dimension) {
        final WorldServer toWorld = this.server.func_71218_a(dimension);

        EntityUtil.transferPlayerToWorld(player, null, toWorld, (ForgeITeleporterBridge) toWorld.func_85176_s());
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setPlayerManager", at = @At("HEAD"), cancellable = true)
    private void onSetPlayerManager(final WorldServer[] worlds, final CallbackInfo callbackInfo) {
        if (this.playerDataManager == null) {
            this.playerDataManager = worlds[0].func_72860_G().func_75756_e();
            // This is already added in our world constructor
            //worlds[0].getWorldBorder().addListener(new PlayerBorderListener(0));
        }
        callbackInfo.cancel();
    }

    @Redirect(method = "updateTimeAndWeatherForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private WorldBorder onUpdateTimeGetWorldBorder(final WorldServer worldServer, final EntityPlayerMP entityPlayerMP, final WorldServer worldServerIn) {
        return worldServerIn.func_175723_af();
    }

    @Redirect(method = "updateTimeAndWeatherForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket"
            + "(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    private void onWorldBorderInitializePacket(
        final NetHandlerPlayServer invoker, final Packet<?> packet, final EntityPlayerMP playerMP, final WorldServer worldServer) {
        if (worldServer.field_73011_w instanceof WorldProviderHell) {
            ((SPacketWorldBorderBridge) packet).bridge$changeCoordinatesForNether();
        }

        invoker.func_147359_a(packet);
    }

    @Inject(method = "playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V", at = @At("HEAD"))
    private void onPlayerLogOut(final EntityPlayerMP player, final CallbackInfo ci) {
        // Remove player reference from scoreboard
        ((ServerScoreboardBridge) ((Player) player).getScoreboard()).bridge$removePlayer(player, false);
    }

    @Redirect(method = "playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;removeEntity(Lnet/minecraft/entity/Entity;)V"))
    private void onPlayerRemoveFromWorldFromDisconnect(final WorldServer world, final Entity player, final EntityPlayerMP playerMP) {
        try (final GeneralizedContext context = PlayerPhase.State.PLAYER_LOGOUT.createPhaseContext().source(playerMP)) {
            context.buildAndSwitch();
            world.func_72900_e(player);
        }
    }

    @Inject(method = "saveAllPlayerData()V", at = @At("RETURN"))
    private void onSaveAllPlayerData(final CallbackInfo ci) {
        for (final SpongeUser user : SpongeUser.dirtyUsers) {
            user.save();
        }
    }

    @Inject(method = "playerLoggedIn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void impl$sendAddPlayerListItemPacketAndPreparePlayer(final EntityPlayerMP player, final CallbackInfo ci) {
        // Create a packet to be used for players without context data
        final SPacketPlayerListItem noSpecificViewerPacket = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, player);

        for (final EntityPlayerMP viewer : this.playerEntityList) {
            if (((Player) viewer).canSee((Player) player)) {
                viewer.field_71135_a.func_147359_a(noSpecificViewerPacket);
            }

            if (player == viewer || ((Player) player).canSee((Player) viewer)) {
                player.field_71135_a.func_147359_a(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, viewer));
            }
        }

        // Spawn player into level
        final WorldServer level = this.server.func_71218_a(player.field_71093_bK);
        // TODO direct this appropriately
        level.func_72838_d(player);
        this.preparePlayer(player, null);

        // We always want to cancel.
        ci.cancel();
    }

    @Inject(method = "writePlayerData", at = @At(target = "Lnet/minecraft/world/storage/IPlayerFileData;writePlayerData(Lnet/minecraft/entity/player/EntityPlayer;)V", value = "INVOKE"))
    private void impl$saveSpongePlayerDataAfterSavingPlayerData(final EntityPlayerMP playerMP, final CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(playerMP.func_110124_au());
    }

    @ModifyVariable(method = "sendPlayerPermissionLevel", at = @At("HEAD"), argsOnly = true)
    private int impl$UpdatePermLevel(final int permLevel) {
        // If a non-default permission service is being used, then the op level will always be 0.
        // We force it to be 4 to ensure that the client is able to open command blocks (
        if (!(Sponge.getServiceManager().provideUnchecked(PermissionService.class) instanceof SpongePermissionService)) {
            return 4;
        }
        return permLevel;
    }

    @Redirect(method = "updatePermissionLevel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/WorldServer;getWorldInfo()Lnet/minecraft/world/storage/WorldInfo;"))
    private WorldInfo onGetWorldInfo(final WorldServer overworld, final EntityPlayerMP player) {
        // TODO: This applies only to singleplayer, on the server canSendCommands is called with the game profile
        // We can't get the world from the game profile

        // Check the world info of the current world instead of overworld world info
        return player.field_70170_p.func_72912_H();
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
    public void sendMessage(final ITextComponent component, final boolean isSystem) {
        ChatUtil.sendMessage(component, MessageChannel.TO_ALL, (CommandSource) this.server, !isSystem);
    }

    @Override
    public void bridge$reloadAdvancementProgress() {
        for (final PlayerAdvancements playerAdvancements : this.advancements.values()) {
            ((PlayerAdvancementsBridge) playerAdvancements).bridge$reloadAdvancementProgress();
        }
    }
}
