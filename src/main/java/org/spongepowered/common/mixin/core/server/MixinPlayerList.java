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
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketTagsList;
import net.minecraft.network.play.server.SPacketUpdateRecipes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListIPBans;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorld;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.player.PlayerPhase;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.play.server.IMixinSPacketWorldBorder;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;
import org.spongepowered.common.registry.type.world.dimension.GlobalDimensionType;
import org.spongepowered.common.service.ban.SpongeIPBanList;
import org.spongepowered.common.service.ban.SpongeUserListBans;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.whitelist.SpongeUserListWhitelist;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.io.File;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements IMixinPlayerList {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private Map<UUID, EntityPlayerMP> uuidToPlayerMap;
    @Shadow @Final private Map<UUID, PlayerAdvancements> advancements;
    @Shadow private IPlayerFileData playerDataManager;
    @Shadow public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn);
    @Shadow public abstract int getMaxPlayers();
    @Shadow public abstract void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn);
    @Shadow public abstract void playerLoggedIn(EntityPlayerMP playerIn);
    @Shadow public abstract void updatePermissionLevel(EntityPlayerMP p_187243_1_);
    @Shadow public abstract void sendWorldInfo(EntityPlayerMP p_72354_1_, WorldServer p_72354_2_);
    @Shadow public abstract MinecraftServer getServer();
    @Shadow @Nullable public abstract ITextComponent canPlayerLogin(SocketAddress p_206258_1_, GameProfile p_206258_2_);
    @Shadow @Final private List<EntityPlayerMP> players;
    @Shadow protected abstract void setPlayerGameTypeBasedOnOther(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, IWorld p_72381_3_);

    /**
     * Bridge methods to proxy modified method in Vanilla, nothing in Forge
     */
    public void func_72355_a(NetworkManager netManager, EntityPlayerMP playerIn) {
        this.initializeConnectionToPlayer(netManager, playerIn, null);
    }

    /**
     * Bridge methods to proxy modified method in Vanilla, nothing in Forge
     */
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn) {
        initializeConnectionToPlayer(netManager, playerIn, null);
    }

    /**
     * @author Aaron1011 - February 6th, 2018 - Version 1.13.2 - Initial Update
     */
    private void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, @Nullable NetHandlerPlayServer handler) {
        final GameProfile gameprofile = playerIn.getGameProfile();
        final PlayerProfileCache playerprofilecache = this.server.getPlayerProfileCache();
        final GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
        final String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.addEntry(gameprofile);

        // Sponge start - save changes to offline User before reading player data
        SpongeUser user = (SpongeUser) ((IMixinEntityPlayerMP) playerIn).getUserObject();
        if (SpongeUser.dirtyUsers.contains(user)) {
            user.save();
        }
        // Sponge end

        final NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
        final Optional<Instant> firstJoined = SpongePlayerDataHandler.getFirstJoined(playerIn.getUniqueID());
        final Instant lastJoined = Instant.now();

        SpongePlayerDataHandler.setPlayerInfo(playerIn.getUniqueID(), firstJoined.orElse(lastJoined), lastJoined);

        final ITextComponent kickReason = this.canPlayerLogin(netManager.getRemoteAddress(), gameprofile);
        Text disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeTexts.toText(kickReason);
        } else {
            disconnectMessage = Text.of("You are not allowed to log in to this server.");
        }

        // Somehow we have a dimension type with no world, go back to the overworld then.
        WorldServer toWorld = this.server.getWorld(playerIn.dimension);
        if (toWorld == null) {
            playerIn.dimension = DimensionType.OVERWORLD;
            toWorld = this.server.getWorld(DimensionType.OVERWORLD);
            playerIn.setWorld(toWorld);
            final BlockPos spawnPos = toWorld.getSpawnPoint();
            playerIn.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        }

        final Player player = (Player) playerIn;

        Transform toTransform = player.getTransform();

        final ClientConnectionEvent.Login event = SpongeEventFactory.createClientConnectionEventLogin(
            Sponge.getCauseStackManager().getCurrentCause(), toTransform, toTransform, (RemoteConnection) netManager,
            new MessageEvent.MessageFormatter(disconnectMessage), (org.spongepowered.api.profile.GameProfile) gameprofile, player, false
        );

        if (kickReason != null) {
            event.setCancelled(true);
        }

        if (SpongeImpl.postEvent(event)) {
            this.disconnectClient(netManager, event.isMessageCancelled() ? Optional.empty() : Optional.of(event.getMessage()), gameprofile);
            return;
        }

        toTransform = event.getToTransform();
        final Location toLocation = toTransform.getLocation();
        toWorld = (WorldServer) toLocation.getWorld();

        playerIn.dimension = toWorld.getDimension().getType();
        playerIn.setWorld(toWorld);
        playerIn.interactionManager.setWorld(toWorld);

        double x = toLocation.getX();
        double y = toLocation.getY();
        double z = toLocation.getY();
        float pitch = (float) toTransform.getPitch();
        float yaw = (float) toTransform.getYaw();
        playerIn.setPositionAndRotation(x, y, z, yaw, pitch);

        toWorld.getChunkProvider().getChunk((int)x >> 4, (int)z >> 4, true, true);

        String s1 = "local";

        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        LOGGER.info(playerIn.getName() + "[" + s1 + "] logged in with entity id " + playerIn.getEntityId() + " in " + toWorld.getWorldInfo()
            .getWorldName() + "(" + toWorld.getDimension().getType() + ") at (" + playerIn.posX + ", " + playerIn.posY + ", " + playerIn.posZ + ")");

        final WorldInfo info = toWorld.getWorldInfo();
        this.setPlayerGameTypeBasedOnOther(playerIn, null, toWorld);

        if (handler == null) {
            handler = new NetHandlerPlayServer(this.server, netManager, playerIn);
        }

        playerIn.connection = handler;
        SpongeImplHooks.fireServerConnectionEvent(netManager);

        handler.sendPacket(new SPacketJoinGame(playerIn.getEntityId(), playerIn.interactionManager.getGameType(),
            info.isHardcore(), toWorld.dimension.getType(), toWorld.getDifficulty(), this.getMaxPlayers(), info.getGenerator(),
            toWorld.getGameRules().getBoolean("reducedDebugInfo")));

        handler.sendPacket(new SPacketCustomPayload(SPacketCustomPayload.BRAND, (new PacketBuffer(Unpooled.buffer())).writeString(this.getServer().getServerModName())));
        handler.sendPacket(new SPacketServerDifficulty(info.getDifficulty(), info.isDifficultyLocked()));
        handler.sendPacket(new SPacketPlayerAbilities(playerIn.abilities));
        handler.sendPacket(new SPacketHeldItemChange(playerIn.inventory.currentItem));
        handler.sendPacket(new SPacketUpdateRecipes(this.server.getRecipeManager().getRecipes()));
        handler.sendPacket(new SPacketTagsList(this.server.getNetworkTagManager()));
        this.updatePermissionLevel(playerIn);
        playerIn.getStats().markAllDirty();
        playerIn.getRecipeBook().init(playerIn);

        // Sponge start
        // This sends the objective/score creation packets
        // to the player, without attempting to remove them from their
        // previous scoreboard (which is set in a field initializer).
        // This allows #getWorldScoreboard to function
        // as normal, without causing issues when it is initialized on the client.

        ((IMixinEntityPlayerMP) playerIn).initScoreboard();
        // Sponge end

        this.server.refreshStatusNextTick();
        this.playerLoggedIn(playerIn);
        handler.setPlayerLocation(x, y, z, yaw, pitch);
        this.sendWorldInfo(playerIn, toWorld);

        // Sponge Start - Use the server's ResourcePack object
        Optional<ResourcePack> pack = ((Server) this.server).getDefaultResourcePack();
        pack.ifPresent(((Player) playerIn)::sendResourcePack);
        // Sponge End

        for(PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            handler.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        if (nbttagcompound != null && nbttagcompound.contains("RootVehicle", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("RootVehicle");
            Entity entity1 = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompound("Entity"), toWorld, true);
            if (entity1 != null) {
                UUID uuid = nbttagcompound1.getUniqueId("Attach");
                if (entity1.getUniqueID().equals(uuid)) {
                    playerIn.startRiding(entity1, true);
                } else {
                    for(Entity entity : entity1.getRecursivePassengers()) {
                        if (entity.getUniqueID().equals(uuid)) {
                            playerIn.startRiding(entity, true);
                            break;
                        }
                    }
                }

                if (!playerIn.isPassenger()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    toWorld.removeEntityDangerously(entity1);

                    for(Entity entity2 : entity1.getRecursivePassengers()) {
                        toWorld.removeEntityDangerously(entity2);
                    }
                }
            }
        }

        playerIn.addSelfToInternalCraftingInventory();

        // Sponge start - move join message creation to end of the method,
        // when we fire the event
        ITextComponent itextcomponent;
        if (playerIn.getGameProfile().getName().equalsIgnoreCase(s)) {
            itextcomponent = new TextComponentTranslation("multiplayer.player.joined", new Object[]{playerIn.getDisplayName()});
        } else {
            itextcomponent = new TextComponentTranslation("multiplayer.player.joined.renamed", new Object[]{playerIn.getDisplayName(), s});
        }

        final Text originalMessage = SpongeTexts.toText(itextcomponent);
        final MessageChannel originalChannel = player.getMessageChannel();
        final ClientConnectionEvent.Join joinEvent = SpongeEventFactory.createClientConnectionEventJoin(
            Sponge.getCauseStackManager().getCurrentCause(), originalChannel, Optional.of(originalChannel),
            new MessageEvent.MessageFormatter(originalMessage), player, false
        );
        SpongeImpl.postEvent(joinEvent);
        // Send to the channel
        if (!joinEvent.isMessageCancelled()) {
            joinEvent.getChannel().ifPresent(channel -> channel.send(player, event.getMessage()));
        }
        // Sponge end
    }

    /**
     * @author Zidane - June 13th, 2015
     * @author simon816 - June 24th, 2015
     * @author Zidane - March 29th, 2016
     * @author gabizou - June 5th, 2016 - Update for teleportation changes to keep the same player.
     * @author Zidane - February 26th, 2019 - Version 1.13.2 - Initial Update
     *
     * @reason Direct respawning players to use Sponge events and process appropriately.
     */
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, DimensionType toDimension, boolean conqueredEnd) {
        // Vanilla will always use overworld, set to the world the player was in
        // UNLESS comming back from the end.
        if (!conqueredEnd && toDimension == DimensionType.OVERWORLD) {
            toDimension = playerIn.dimension;
        }

        if (playerIn.isBeingRidden()) {
            playerIn.removePassengers();
        }

        if (playerIn.ridingEntity != null) {
            playerIn.stopRiding();
        }

        Player player = (Player) playerIn;
        final Transform fromTransform = player.getTransform();
        final WorldServer fromWorld = this.server.getWorld(playerIn.dimension);

        WorldServer toWorld = this.server.getWorld(toDimension);
        Transform toTransform = new Transform(EntityUtil.getPlayerRespawnLocation(playerIn, toWorld), Vector3d.ZERO, Vector3d.ZERO);
        Location toLocation = toTransform.getLocation();

        // If coming from end, fire a teleport event for plugins
        if (conqueredEnd) {
            // When leaving the end, players are never placed inside the teleporter but instead "respawned" in the target world
            final MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent(playerIn, toLocation);
            if (event.isCancelled()) {
                playerIn.queuedEndExit = false;
                return playerIn;
            }

            toTransform = event.getToTransform();
            toLocation = toTransform.getLocation();
        }

        toWorld = (WorldServer) toTransform.getWorld();
        toDimension = toWorld.getDimension().getType();

        playerIn.getServerWorld().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerWorld().getEntityTracker().untrack(playerIn);
        playerIn.getServerWorld().getPlayerChunkMap().removePlayer(playerIn);
        this.players.remove(playerIn);
        fromWorld.removeEntityDangerously(playerIn);

        PlayerInteractionManager playerinteractionmanager;

        if (this.server.isDemo()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(toWorld);
        } else {
            playerinteractionmanager = new PlayerInteractionManager(toWorld);
        }

        final EntityPlayerMP newPlayer = new EntityPlayerMP(this.server, toWorld, playerIn.getGameProfile(), playerinteractionmanager);
        player = (Player) newPlayer;

        ((IMixinEntityPlayerMP) newPlayer).setRespawning(true);
        newPlayer.connection = playerIn.connection;
        newPlayer.copyFrom(playerIn, conqueredEnd);
        // set player dimension for RespawnPlayerEvent
        newPlayer.dimension = toDimension;
        newPlayer.setEntityId(playerIn.getEntityId());
        newPlayer.setPrimaryHand(playerIn.getPrimaryHand());

        ((IMixinEntityPlayerMP) newPlayer).setScoreboardOnRespawn(((Player) playerIn).getScoreboard());
        ((IMixinEntityPlayerMP) playerIn).removeScoreboardOnRespawn();

        for (String s : playerIn.getTags()) {
            newPlayer.addTag(s);
        }

        toTransform = toTransform.setLocation(toLocation);

        final RespawnPlayerEvent event = SpongeEventFactory.createRespawnPlayerEvent(Sponge.getCauseStackManager().getCurrentCause(),
            (Player) playerIn, (Player) newPlayer, fromTransform, toTransform, EntityUtil.tempIsBedSpawn, !conqueredEnd);

        EntityUtil.tempIsBedSpawn = false;

        SpongeImpl.postEvent(event);

        final boolean pluginChangedLocation = event.getToTransform().getLocation() != toLocation;

        toTransform = event.getToTransform();
        toLocation = toTransform.getLocation();
        toWorld = (WorldServer) toLocation.getWorld();
        toDimension = toWorld.getDimension().getType();

        toWorld.getChunkProvider().getChunk((int)toLocation.getX() >> 4, (int)toLocation.getZ() >> 4, true, true);

        newPlayer.dimension = toWorld.getDimension().getType();
        newPlayer.interactionManager.world = toWorld;

        final BlockPos bedPos = SpongeImplHooks.getBedLocation(playerIn, toDimension);

        if (bedPos != null) {
            newPlayer.setSpawnPoint(bedPos, playerIn.isSpawnForced());
        }

        this.setPlayerGameTypeBasedOnOther(newPlayer, playerIn, toWorld);

        newPlayer.setPosition(toLocation.getX(), toLocation.getY(), toLocation.getZ());

        if (!pluginChangedLocation) {
            while (toWorld.isCollisionBoxesEmpty(newPlayer, newPlayer.getBoundingBox()) && toLocation.getPosition().getY() < 256.0D) {
                newPlayer.setPosition(playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ);
                toLocation = toLocation.add(0, 1, 0);
            }
        }

        final DimensionType clientDimension = ((IMixinDimensionType) toWorld.getDimension().getType()).asClientDimensionType();

        if (!((IMixinEntityPlayerMP) newPlayer).usesCustomClient()) {
            final GlobalDimensionType fromGlobalDimension = ((IMixinDimensionType) fromWorld.getDimension().getType()).getGlobalDimensionType();
            final GlobalDimensionType toGlobalDimension = ((IMixinDimensionType) clientDimension).getGlobalDimensionType();

            if (fromGlobalDimension != toGlobalDimension) {
                newPlayer.connection.sendPacket(new SPacketRespawn((clientDimension.getId() >= DimensionType.OVERWORLD.getId() ? DimensionType.NETHER :
                    DimensionType.OVERWORLD), toWorld.getDifficulty(), toWorld.getWorldInfo().getGenerator(), newPlayer.interactionManager
                    .getGameType()));
            }
        }

        ((IMixinEntityPlayerMP) newPlayer).refreshSkinOnRespawn();

        newPlayer.connection.sendPacket(new SPacketRespawn(clientDimension, toWorld.getDifficulty(), toWorld
            .getWorldInfo().getGenerator(), newPlayer.interactionManager.getGameType()));

        newPlayer.connection.sendPacket(new SPacketServerDifficulty(toWorld.getDifficulty(), toWorld.getWorldInfo().isDifficultyLocked()));

        newPlayer.connection.setPlayerLocation(toLocation.getX(), toLocation.getY(), toLocation.getZ(),
            (float) toTransform.getYaw(), (float) toTransform.getPitch());

        final BlockPos spawnLocation = toWorld.getSpawnPoint();
        newPlayer.connection.sendPacket(new SPacketSpawnPosition(spawnLocation));

        newPlayer.connection.sendPacket(new SPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));

        this.sendWorldInfo(playerIn, toWorld);
        this.updatePermissionLevel(newPlayer);

        toWorld.getPlayerChunkMap().addPlayer(newPlayer);

        ((World) toWorld).spawnEntity(player);

        this.players.add(newPlayer);

        newPlayer.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_GAME_MODE, newPlayer));
        this.uuidToPlayerMap.put(newPlayer.getUniqueID(), newPlayer);
        newPlayer.addSelfToInternalCraftingInventory();

        newPlayer.connection.sendPacket(new SPacketEntityStatus(newPlayer,
            toWorld.getGameRules().getBoolean("reducedDebugInfo") ? DataConstants.REDUCED_DEBUG_INFO_ENABLE : DataConstants.REDUCED_DEBUG_INFO_DISABLE));

        for (PotionEffect potioneffect : newPlayer.getActivePotionEffects()) {
            newPlayer.connection.sendPacket(new SPacketEntityEffect(newPlayer.getEntityId(), potioneffect));
        }
        ((IMixinEntityPlayerMP) newPlayer).refreshScaledHealth();

        ((IMixinEntityPlayerMP) newPlayer).setRespawning(false);
        ((IMixinEntityPlayerMP) playerIn).removeTabList();

        SpongeCommonEventFactory.callPostPlayerRespawnEvent(newPlayer, conqueredEnd);

        return newPlayer;
    }

    /**
     * @author blood - May 21st, 2016
     * @author Zidane - Feburary 26th, 2019 - Version 1.13.2
     *
     * @reason Overwritten to redirect to our method that accepts a {@link Teleporter}
     */
    @Overwrite
    public void transferEntityToWorld(Entity entityIn, DimensionType dimensionType, WorldServer oldWorldIn, WorldServer toWorldIn) {
        this.transferEntityToWorld(entityIn, oldWorldIn, toWorldIn, toWorldIn.getDefaultTeleporter());
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
        ChatUtil.sendMessage(component, MessageChannel.TO_ALL, (CommandSource) this.server, !isSystem);
    }

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Redirect ban list constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link BanService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListBans"))
    private UserListBans createBanList(File file) {
        return new SpongeUserListBans(file);
    }

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Redirect IP ban list constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link BanService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListIPBans"))
    private UserListIPBans createIPBanList(File file) {
        return new SpongeIPBanList(file);
    }

    /**
     * @author Minecrell - December 4th, 2016
     * @reason Redirect whitelist constructor and use our custom implementation
     *     instead. Redirects all methods to the {@link WhitelistService}.
     */
    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListWhitelist"))
    private UserListWhitelist createWhitelist(File file) {
        return new SpongeUserListWhitelist(file);
    }

    @Inject(method = "func_212504_a", at = @At("HEAD"), cancellable = true)
    private void onSetPlayerManager(WorldServer world, CallbackInfo callbackInfo) {
        if (this.playerDataManager == null) {
            this.playerDataManager = SpongeImpl.getServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getPlayerNBTManager();
            // This is already added in our world constructor
            //worlds[0].getWorldBorder().addListener(new PlayerBorderListener(0));
        }
        callbackInfo.cancel();
    }

    @Redirect(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private WorldBorder onUpdateTimeGetWorldBorder(WorldServer worldServer, EntityPlayerMP entityPlayerMP, WorldServer worldServerIn) {
        return worldServerIn.getWorldBorder();
    }

    @Redirect(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket"
            + "(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    private void onWorldBorderInitializePacket(NetHandlerPlayServer invoker, Packet<?> packet, EntityPlayerMP playerMP, WorldServer worldServer) {
        if (worldServer.dimension instanceof NetherDimension) {
            ((IMixinSPacketWorldBorder) packet).netherifyCenterCoordinates();
        }

        invoker.sendPacket(packet);
    }

    @Inject(method = "playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V", at = @At("HEAD"))
    private void onPlayerLogOut(EntityPlayerMP player, CallbackInfo ci) {
        // Synchronise with user object
        NBTTagCompound nbt = new NBTTagCompound();
        player.writeWithoutTypeId(nbt);
        ((SpongeUser) ((IMixinEntityPlayerMP) player).getUserObject()).readFromNbt(nbt);

        // Remove player reference from scoreboard
        ((IMixinServerScoreboard) ((Player) player).getScoreboard()).removePlayer(player, false);
    }

    @Redirect(method = "playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;removeEntity(Lnet/minecraft/entity/Entity;)V"))
    private void onPlayerRemoveFromWorldFromDisconnect(WorldServer world, Entity player, EntityPlayerMP playerMP) {
        try (final GeneralizedContext context = PlayerPhase.State.PLAYER_LOGOUT.createPhaseContext().source(playerMP).addCaptures()) {
            context.buildAndSwitch();
            world.removeEntity(player);
        }
    }

    @Inject(method = "saveAllPlayerData()V", at = @At("RETURN"))
    private void onSaveAllPlayerData(CallbackInfo ci) {
        for (SpongeUser user : SpongeUser.dirtyUsers) {
            user.save();
        }
    }

    @Inject(method = "playerLoggedIn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers"
        + "(Lnet/minecraft/network/Packet;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void playerLoggedIn2(EntityPlayerMP player, CallbackInfo ci) {
        // Create a packet to be used for players without context data
        SPacketPlayerListItem noSpecificViewerPacket = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, player);

        for (EntityPlayerMP viewer : this.players) {
            if (((Player) viewer).canSee((Player) player)) {
                viewer.connection.sendPacket(noSpecificViewerPacket);
            }

            if (((Player) player).canSee((Player) viewer)) {
                player.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, viewer));
            }
        }

        // Spawn player into level
        WorldServer level = this.server.getWorld(player.dimension);
        // TODO direct this appropriately
        level.spawnEntity(player);
        this.preparePlayer(player, null);

        // We always want to cancel.
        ci.cancel();
    }

    @Inject(method = "writePlayerData", at = @At(target = "Lnet/minecraft/world/storage/IPlayerFileData;writePlayerData(Lnet/minecraft/entity/player/EntityPlayer;)V", value = "INVOKE"))
    private void onWritePlayerFile(EntityPlayerMP playerMP, CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(playerMP.getUniqueID());
    }

    @ModifyVariable(method = "sendPlayerPermissionLevel", at = @At("HEAD"), argsOnly = true)
    private int fixPermLevel(int permLevel) {
        // If a non-default permission service is being used, then the op level will always be 0.
        // We force it to be 4 to ensure that the client is able to open command blocks (
        if (!(Sponge.getServiceManager().provideUnchecked(PermissionService.class) instanceof SpongePermissionService)) {
            return 4;
        }
        return permLevel;
    }

    @Inject(method = "createPlayerForUser", at = @At("RETURN"), cancellable = true)
    private void onCreatePlayerForUser(CallbackInfoReturnable<EntityPlayerMP> cir) {
        ((IMixinEntityPlayerMP) cir.getReturnValue()).forceRecreateUser();
    }

    /**
     * Handles transfer an {@link Entity} by {@link Teleporter}.
     *
     * @param entity The entity being teleported
     * @param fromWorld The origin world
     * @param toWorld The destination world
     * @param teleporter The teleporter being used to transport the entity
     */
    @Override
    public void transferEntityToWorld(Entity entity, WorldServer fromWorld, WorldServer toWorld, Teleporter teleporter) {
        // rewritten completely to handle our portal event
        final MoveEntityEvent.Teleport.Portal event = EntityUtil.handleDisplaceEntityPortalEvent(entity, toWorld.getDimension().getType(),
            (IMixinITeleporter) teleporter);
        if (event == null || event.isCancelled()) {
            return;
        }

        entity.setLocationAndAngles(event.getToTransform().getPosition().getX(), event.getToTransform().getPosition().getY(),
            event.getToTransform().getPosition().getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
        toWorld.spawnEntity(entity);
        entity.setWorld(toWorld);
        toWorld.tickEntity(entity, false);
    }

    /**
     * Handles transferring a {@link EntityPlayerMP} to a new {@link net.minecraft.world.World}.
     *
     * @param player The player
     * @param toDimensionType The destination dimension type
     * @param teleporter The teleporter
     */
    @Override
    public void transferPlayerToDimension(EntityPlayerMP player, DimensionType toDimensionType, Teleporter teleporter) {
        final MoveEntityEvent.Teleport.Portal event = EntityUtil.handleDisplaceEntityPortalEvent(player, toDimensionType,
            (IMixinITeleporter) teleporter);
        if (event == null || event.isCancelled()) {
            return;
        }

        EntityUtil.transferPlayerToDimension(event, player);
    }

    /**
     * Prepares an {@link Entity} for teleporting into a portal via location changes.
     *
     * @param entity The entity
     * @param fromWorld The previous world
     * @param toWorld The new world
     */
    @Override
    public void prepareEntityForPortal(Entity entity, WorldServer fromWorld, WorldServer toWorld) {
        fromWorld.profiler.startSection("moving");
        Dimension fromDimension = fromWorld.dimension;
        Dimension toDimension = toWorld.dimension;
        double moveFactor = getMovementFactor(fromDimension) / getMovementFactor(toDimension);
        double x = entity.posX * moveFactor;
        double y = entity.posY;
        double z = entity.posZ * moveFactor;

        if (toDimension instanceof EndDimension) {
            BlockPos blockpos;

            if (fromDimension instanceof EndDimension) {
                blockpos = toWorld.getSpawnPoint();
            } else {
                blockpos = toWorld.getSpawnCoordinate();
            }

            x = blockpos.getX();
            y = blockpos.getY();
            z = blockpos.getZ();
            entity.setLocationAndAngles(x, y, z, 90.0F, 0.0F);
        }

        if (!(fromDimension instanceof EndDimension)) {
            fromWorld.profiler.startSection("placing");
            x = MathHelper.clamp((int)x, -29999872, 29999872);
            z = MathHelper.clamp((int)z, -29999872, 29999872);

            if (entity.isAlive()) {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            fromWorld.profiler.endSection();
        }

        if (entity.isAlive()) {
            fromWorld.tickEntity(entity, false);
        }

        fromWorld.profiler.endSection();
    }

    @Override
    public double getMovementFactor(Dimension dimension) {
        if (dimension instanceof NetherDimension) {
            return 8.0;
        }
        return 1.0;
    }

    @Override
    public void reloadAdvancementProgress() {
        for (PlayerAdvancements playerAdvancements : this.advancements.values()) {
            ((IMixinPlayerAdvancements) playerAdvancements).reloadAdvancementProgress();
        }
    }

    private void disconnectClient(NetworkManager netManager, Optional<Text> disconnectMessage, @Nullable GameProfile profile) {
        ITextComponent reason;
        if (disconnectMessage.isPresent()) {
            reason = SpongeTexts.toComponent(disconnectMessage.get());
        } else {
            reason = new TextComponentTranslation("disconnect.disconnected");
        }

        try {
            LOGGER.info("Disconnecting " + (profile != null ? profile.toString() + " (" + netManager.getRemoteAddress().toString() + ")" : String.valueOf(netManager.getRemoteAddress() + ": " + reason.getUnformattedComponentText())));
            netManager.sendPacket(new SPacketDisconnect(reason));
            netManager.closeChannel(reason);
        } catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", exception);
        }
    }
}
