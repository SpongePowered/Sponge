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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinServerConfigurationManager;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.play.server.IMixinS44PacketWorldBorder;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.net.SocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager implements IMixinServerConfigurationManager {

    private static final String WRITE_PLAYER_DATA =
            "Lnet/minecraft/world/storage/IPlayerFileData;writePlayerData(Lnet/minecraft/entity/player/EntityPlayer;)V";

    @Shadow @Final private static Logger logger;
    @Shadow @Final private MinecraftServer mcServer;
    @Shadow @Final public Map<UUID, EntityPlayerMP> uuidToPlayerMap;
    @Shadow @Final public List<EntityPlayerMP> playerEntityList;
    @Shadow private IPlayerFileData playerNBTManagerObj;
    @Shadow public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn);
    @Shadow public abstract void setPlayerGameTypeBasedOnOther(EntityPlayerMP playerIn, @Nullable EntityPlayerMP other, net.minecraft.world.World worldIn);
    @Shadow public abstract MinecraftServer getServerInstance();
    @Shadow public abstract int getMaxPlayers();
    @Shadow public abstract void sendChatMsg(IChatComponent component);
    @Shadow public abstract void sendPacketToAllPlayers(Packet<?> packetIn);
    @Shadow public abstract void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn);
    @Shadow public abstract void playerLoggedIn(EntityPlayerMP playerIn);
    @Shadow public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn);
    @Shadow public abstract void syncPlayerInventory(EntityPlayerMP playerIn);
    @Nullable @Shadow public abstract String allowUserToConnect(SocketAddress address, GameProfile profile);

    /**
     * Bridge methods to proxy modified method in Vanilla, nothing in Forge
     */
    public void func_72355_a(NetworkManager netManager, EntityPlayerMP playerIn) {
        initializeConnectionToPlayer(netManager, playerIn, null);
    }

    /**
     * Bridge methods to proxy modified method in Vanilla, nothing in Forge
     */
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn) {
        initializeConnectionToPlayer(netManager, playerIn, null);
    }

    private void disconnectClient(NetworkManager netManager, Optional<Text> disconnectMessage, @Nullable GameProfile profile) {
        IChatComponent reason;
        if (disconnectMessage.isPresent()) {
            reason = SpongeTexts.toComponent(disconnectMessage.get());
        } else {
            reason = new ChatComponentTranslation("disconnect.disconnected");
        }

        try {
            logger.info("Disconnecting " + (profile != null ? profile.toString() + " (" + netManager.getRemoteAddress().toString() + ")" : String.valueOf(netManager.getRemoteAddress() + ": " + reason.getUnformattedText())));
            netManager.sendPacket(new S40PacketDisconnect(reason));
            netManager.closeChannel(reason);
        } catch (Exception exception) {
            logger.error("Error whilst disconnecting player", exception);
        }
    }

    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, @Nullable NetHandlerPlayServer handler) {
        GameProfile gameprofile = playerIn.getGameProfile();
        PlayerProfileCache playerprofilecache = this.mcServer.getPlayerProfileCache();
        GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.addEntry(gameprofile);
        // Sponge - save changes to offline User before reading player data
        SpongeUser user = (SpongeUser) ((IMixinEntityPlayerMP) playerIn).getUserObject();
        if (SpongeUser.dirtyUsers.contains(user)) {
            user.save();
        }
        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
        WorldServer worldserver = DimensionManager.getWorldFromDimId(playerIn.dimension);

        if (worldserver == null) {
            SpongeImpl.getLogger().warn("Player [{}] has attempted to login to unloaded dimension [{}]. This is not safe so we have moved them to "
                    + "the default world's spawn point.", playerIn.getName(), playerIn.dimension);
            playerIn.dimension = 0;
            worldserver = this.mcServer.worldServerForDimension(0);
            BlockPos spawnPoint = ((IMixinWorldProvider) worldserver.provider).getRandomizedSpawnPoint();
            playerIn.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }

        // Sponge start - fire login event
        @Nullable String kickReason = allowUserToConnect(netManager.getRemoteAddress(), gameprofile);
        Text disconnectMessage;
        if (kickReason != null) {
            disconnectMessage = SpongeTexts.fromLegacy(kickReason);
        } else {
            disconnectMessage = Text.of("You are not allowed to log in to this server.");
        }

        Player player = (Player) playerIn;
        Transform<World> fromTransform = player.getTransform().setExtent((World) worldserver);

        ClientConnectionEvent.Login loginEvent = SpongeEventFactory.createClientConnectionEventLogin(
                Cause.of(NamedCause.source(player)), fromTransform, fromTransform, (RemoteConnection) netManager,
                new MessageEvent.MessageFormatter(disconnectMessage), (org.spongepowered.api.profile.GameProfile) gameprofile, player, false
        );

        if (kickReason != null) {
            loginEvent.setCancelled(true);
        }

        SpongeImpl.postEvent(loginEvent);
        if (loginEvent.isCancelled()) {
            disconnectClient(netManager, loginEvent.isMessageCancelled() ? Optional.empty() : Optional.of(loginEvent.getMessage()), gameprofile);
            return;
        }

        // Join data
        Optional<Instant> firstJoined = SpongePlayerDataHandler.getFirstJoined(playerIn.getUniqueID());
        Instant lastJoined = Instant.now();
        SpongePlayerDataHandler.setPlayerInfo(playerIn.getUniqueID(), firstJoined.orElse(lastJoined), lastJoined);

        double x = loginEvent.getToTransform().getPosition().getX();
        double y = loginEvent.getToTransform().getPosition().getY();
        double z = loginEvent.getToTransform().getPosition().getZ();
        float pitch = (float) loginEvent.getToTransform().getPitch();
        float yaw = (float) loginEvent.getToTransform().getYaw();
        if (worldserver != loginEvent.getToTransform().getExtent()) {
            worldserver = (net.minecraft.world.WorldServer) loginEvent.getToTransform().getExtent();
        }

        playerIn.setPositionAndRotation(x, y, z, yaw, pitch);
        playerIn.dimension = worldserver.provider.getDimensionId();
        // make sure the chunk is loaded for login
        boolean chunkLoadOverride = worldserver.theChunkProviderServer.chunkLoadOverride;
        worldserver.theChunkProviderServer.chunkLoadOverride = true;
        worldserver.theChunkProviderServer.loadChunk(loginEvent.getToTransform().getLocation().getChunkPosition().getX(), loginEvent.getToTransform().getLocation().getChunkPosition().getZ());
        worldserver.theChunkProviderServer.chunkLoadOverride = chunkLoadOverride;
        // Sponge end

        playerIn.setWorld(worldserver);
        playerIn.theItemInWorldManager.setWorld((WorldServer) playerIn.worldObj);
        String s1 = "local";

        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        logger.info(playerIn.getName() + "[" + s1 + "] logged in with entity id " + playerIn.getEntityId() + " in "
                + worldserver.getWorldInfo().getWorldName() + "(" + worldserver.provider.getDimensionId()
                + ") at (" + playerIn.posX + ", " + playerIn.posY + ", " + playerIn.posZ + ")");
        WorldInfo worldinfo = worldserver.getWorldInfo();
        BlockPos blockpos = worldserver.getSpawnPoint();
        this.setPlayerGameTypeBasedOnOther(playerIn, null, worldserver);

        if (handler == null) {
            // Create the handler here (so the player's gets set)
            handler = new NetHandlerPlayServer(this.mcServer, netManager, playerIn);
        }
        playerIn.playerNetServerHandler = handler;

        // Support vanilla clients logging into custom dimensions
        int dimension = DimensionManager.getClientDimensionToSend(worldserver.provider.getDimensionId(), worldserver, playerIn);
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            DimensionManager.sendDimensionRegistration(worldserver, playerIn, dimension);
        }

        handler.sendPacket(new S01PacketJoinGame(playerIn.getEntityId(), playerIn.theItemInWorldManager.getGameType(), worldinfo
                .isHardcoreModeEnabled(), dimension, worldserver.getDifficulty(), this.getMaxPlayers(), worldinfo
                .getTerrainType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
        handler.sendPacket(new S3FPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this
                .getServerInstance().getServerModName())));
        handler.sendPacket(new S41PacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        handler.sendPacket(new S05PacketSpawnPosition(blockpos));
        handler.sendPacket(new S39PacketPlayerAbilities(playerIn.capabilities));
        handler.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
        playerIn.getStatFile().func_150877_d();
        playerIn.getStatFile().sendAchievements(playerIn);
        this.mcServer.refreshStatusNextTick();

        this.playerLoggedIn(playerIn);
        handler.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        this.updateTimeAndWeatherForPlayer(playerIn, worldserver);

        // Sponge Start - Use the server's ResourcePack object
        Optional<ResourcePack> pack = ((Server)this.mcServer).getDefaultResourcePack();
        if (pack.isPresent()) {
            ((Player)playerIn).sendResourcePack(pack.get());
        }
        // Sponge End

        playerIn.addSelfToInternalCraftingInventory();


        // Sponge Start

        // Move logic for creating join message up here
        //
        // This sends the objective/score creation packets
        // to the player, without attempting to remove them from their
        // previous scoreboard (which is set in a field initializer).
        // This allows #getWorldScoreboard to function
        // as normal, without causing issues when it is initialized on the client.

        ((IMixinEntityPlayerMP) playerIn).initScoreboard();

        ChatComponentTranslation chatcomponenttranslation;

        if (!playerIn.getName().equalsIgnoreCase(s))
        {
            chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined.renamed", playerIn.getDisplayName(), s);
        }
        else
        {
            chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined", playerIn.getDisplayName());
        }

        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);

        for (Object o : playerIn.getActivePotionEffects()) {
            PotionEffect potioneffect = (PotionEffect) o;
            handler.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        // Fire PlayerJoinEvent
        Text originalMessage = SpongeTexts.toText(chatcomponenttranslation);
        MessageChannel originalChannel = player.getMessageChannel();
        final ClientConnectionEvent.Join event = SpongeEventFactory.createClientConnectionEventJoin(
                Cause.of(NamedCause.source(player)), originalChannel, Optional.of(originalChannel),
                new MessageEvent.MessageFormatter(originalMessage), player, false
        );
        SpongeImpl.postEvent(event);
        // Send to the channel
        if (!event.isMessageCancelled()) {
            event.getChannel().ifPresent(channel -> channel.send(player, event.getMessage()));
        }
        // Sponge end

        if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10)) {
            Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldserver);

            if (entity != null) {
                entity.forceSpawn = true;
                worldserver.spawnEntityInWorld(entity);
                playerIn.mountEntity(entity);
                entity.forceSpawn = false;
            }
        }
    }

    // A temporary variable to transfer the 'isBedSpawn' variable between
    // getPlayerRespawnLocation and recreatePlayerEntity
    private boolean tempIsBedSpawn = false;

    /**
     * @author Zidane - June 13th, 2015
     * @author simon816 - June 24th, 2015
     *
     * @reason - Direct respawning players to use Sponge events
     * and process appropriately.
     *
     * @param playerIn The player being respawned/created
     * @param targetDimension The target dimension
     * @param conqueredEnd Whether the end was conquered
     * @return The new player
     */
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int targetDimension, boolean conqueredEnd) {

        // ### PHASE 1 ### Get the location to spawn

        // Vanilla will always use overworld, set to the world the player was in
        // UNLESS comming back from the end.
        if (!conqueredEnd && targetDimension == 0) {
            targetDimension = playerIn.dimension;
        }

        Player player = (Player) playerIn;
        Transform<World> fromTransform = player.getTransform();
        Transform<World> toTransform = new Transform<>(this.getPlayerRespawnLocation(playerIn, targetDimension), Vector3d.ZERO, Vector3d.ZERO);
        Location<World> location = toTransform.getLocation();

        // If coming from end, fire a teleport event for plugins
        if (conqueredEnd) {
            // When leaving the end, players are never placed inside the teleporter but instead "respawned" in the target world
            DisplaceEntityEvent.Teleport teleportEvent = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(playerIn, location);
            if (teleportEvent.isCancelled()) {
                playerIn.playerConqueredTheEnd = false;
                return playerIn;
            }

            toTransform = teleportEvent.getToTransform();
            location = toTransform.getLocation();
        }
        // Keep players out of blocks
        Vector3d tempPos = player.getLocation().getPosition();
        playerIn.setPosition(location.getX(), location.getY(), location.getZ());
        while (!((WorldServer) location.getExtent()).getCollidingBoundingBoxes(playerIn, playerIn.getEntityBoundingBox()).isEmpty()) {
            playerIn.setPosition(playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ);
            location = location.add(0, 1, 0);
        }
        playerIn.setPosition(tempPos.getX(), tempPos.getY(), tempPos.getZ());

        // ### PHASE 2 ### Remove player from current dimension
        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);

        // ### PHASE 3 ### Reset player (if applicable)
        playerIn.playerConqueredTheEnd = false;
        if (!conqueredEnd) { // don't reset player if returning from end
            ((IMixinEntityPlayerMP) playerIn).reset();
        }
        playerIn.setSneaking(false);
        // update to safe location
        toTransform = toTransform.setLocation(location);

        ((IMixinEntityPlayerMP) playerIn).resetAttributeMap();

        // ### PHASE 4 ### Fire event and set new location on the player
        final RespawnPlayerEvent event =
                SpongeEventFactory.createRespawnPlayerEvent(Cause.of(NamedCause.source(playerIn)), fromTransform, toTransform,
                    (Player) playerIn, this.tempIsBedSpawn);
        this.tempIsBedSpawn = false;
        SpongeImpl.postEvent(event);
        ((IMixinEntity) (Object) player).setLocationAndAngles(event.getToTransform());
        toTransform = event.getToTransform();
        location = toTransform.getLocation();

        if (!(location.getExtent() instanceof WorldServer)) {
            SpongeImpl.getLogger().warn("Location set in PlayerRespawnEvent was invalid, using original location instead");
            location = event.getFromTransform().getLocation();
        }
        final WorldServer targetWorld = (WorldServer) location.getExtent();

        playerIn.dimension = targetWorld.provider.getDimensionId();
        playerIn.setWorld(targetWorld);
        playerIn.theItemInWorldManager.setWorld(targetWorld);

        // make sure the chunk is loaded for respawn
        boolean chunkLoadOverride = targetWorld.theChunkProviderServer.chunkLoadOverride;
        targetWorld.theChunkProviderServer.chunkLoadOverride = true;
        targetWorld.theChunkProviderServer.loadChunk((int) location.getX() >> 4, (int) location.getZ() >> 4);
        targetWorld.theChunkProviderServer.chunkLoadOverride = chunkLoadOverride;

        // ### PHASE 5 ### Respawn player in new world

        // Support vanilla clients logging into custom dimensions
        int dimension = DimensionManager.getClientDimensionToSend(targetWorld.provider.getDimensionId(), targetWorld, playerIn);
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            DimensionManager.sendDimensionRegistration(targetWorld, playerIn, dimension);
        }

        playerIn.playerNetServerHandler.sendPacket(new S07PacketRespawn(dimension, targetWorld.getDifficulty(), targetWorld
                .getWorldInfo().getTerrainType(), playerIn.theItemInWorldManager.getGameType()));
        playerIn.isDead = false;
        playerIn.playerNetServerHandler.setPlayerLocation(location.getX(), location.getY(), location.getZ(),
                (float) toTransform.getYaw(), (float) toTransform.getPitch());

        final BlockPos spawnLocation = targetWorld.getSpawnPoint();
        playerIn.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(spawnLocation));
        playerIn.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(playerIn.experience, playerIn.experienceTotal,
                playerIn.experienceLevel));
        this.updateTimeAndWeatherForPlayer(playerIn, targetWorld);
        targetWorld.getPlayerManager().addPlayer(playerIn);
        org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) player;
        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity(spongeEntity)
                .type(SpawnTypes.PLACEMENT)
                .build();
        ((org.spongepowered.api.world.World) targetWorld).spawnEntity(spongeEntity, Cause.of(NamedCause.source(spawnCause)));
        this.playerEntityList.add(playerIn);
        playerIn.addSelfToInternalCraftingInventory();

        // Reset the health.
        final MutableBoundedValue<Double> maxHealth = ((Player) playerIn).maxHealth();
        final MutableBoundedValue<Integer> food = ((Player) playerIn).foodLevel();
        final MutableBoundedValue<Double> saturation = ((Player) playerIn).saturation();

        playerIn.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(1.0F);
        playerIn.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth.get().floatValue());
        playerIn.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(maxHealth.get().floatValue(), food.get(), saturation.get().floatValue()));

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            playerIn.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        return playerIn;
    }

    // Internal. Note: Has side-effects
    private Location<World> getPlayerRespawnLocation(EntityPlayerMP playerIn, int targetDimension) {
        Location<World> location = ((World) playerIn.worldObj).getSpawnLocation();
        this.tempIsBedSpawn = false;
        WorldServer targetWorld = this.mcServer.worldServerForDimension(targetDimension);
        if (targetWorld == null) { // Target world doesn't exist? Use global
            return location;
        }

        Dimension targetDim = (Dimension) targetWorld.provider;
        // Cannot respawn in requested world, use the fallback dimension for
        // that world. (Usually overworld unless a mod says otherwise).
        if (!targetDim.allowsPlayerRespawns()) {
            targetDimension = ((IMixinWorldProvider) targetDim).getRespawnDimension(playerIn);
            targetWorld = this.mcServer.worldServerForDimension(targetDimension);
            targetDim = (Dimension) targetWorld.provider;
        }
        Vector3d spawnPos = VecHelper.toVector3d(targetWorld.getSpawnPoint());
        BlockPos bedLoc = ((IMixinEntityPlayer) playerIn).getBedLocation(targetDimension);
        if (bedLoc != null) { // Player has a bed
            boolean forceBedSpawn = ((IMixinEntityPlayer) playerIn).isSpawnForced(targetDimension);
            BlockPos bedSpawnLoc = EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(targetDimension), bedLoc, forceBedSpawn);
            if (bedSpawnLoc != null) { // The bed exists and is not obstructed
                this.tempIsBedSpawn = true;
                playerIn.setLocationAndAngles(bedSpawnLoc.getX() + 0.5D, bedSpawnLoc.getY() + 0.1D, bedSpawnLoc.getZ() + 0.5D, 0.0F, 0.0F);
                spawnPos = new Vector3d(bedSpawnLoc.getX() + 0.5D, bedSpawnLoc.getY() + 0.1D, bedSpawnLoc.getZ() + 0.5D);
            } else { // Bed invalid
                playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
                // Vanilla behaviour - Delete the known bed location if invalid
                bedLoc = null; // null = remove location
            }
            // Set the new bed location for the new dimension
            int prevDim = playerIn.dimension; // Temporarily for setSpawnPoint
            playerIn.dimension = targetDimension;
            playerIn.setSpawnPoint(bedLoc, forceBedSpawn);
            playerIn.dimension = prevDim;
        }
        return new Location<>((World) targetWorld, spawnPos);
    }

    /**
     * @author blood - May 21st, 2016
     *
     * @reason - adjusted to support {@link DisplaceEntityEvent.Teleport.Portal}
     *
     * @param playerIn The player teleporting to another dimension
     * @param targetDimensionId The id of target dimension.
     * @param teleporter The teleporter used to transport and create the portal
     */
    @Override
    public void transferPlayerToDimension(EntityPlayerMP playerIn, int targetDimensionId, net.minecraft.world.Teleporter teleporter) {
        DisplaceEntityEvent.Teleport.Portal event = SpongeCommonEventFactory.handleDisplaceEntityPortalEvent(playerIn, targetDimensionId, teleporter);
        if (event == null || event.isCancelled()) {
            return;
        }

        WorldServer fromWorld = (WorldServer) event.getFromTransform().getExtent();
        WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();
        playerIn.dimension = toWorld.provider.getDimensionId();
        // make sure the target chunk is loaded
        boolean chunkLoadOverride = toWorld.theChunkProviderServer.chunkLoadOverride;
        toWorld.theChunkProviderServer.chunkLoadOverride = true;
        toWorld.theChunkProviderServer.loadChunk(event.getToTransform().getLocation().getChunkPosition().getX(), event.getToTransform().getLocation().getChunkPosition().getZ());
        toWorld.theChunkProviderServer.chunkLoadOverride = chunkLoadOverride;
        // Support vanilla clients teleporting to custom dimensions
        int dimension = DimensionManager.getClientDimensionToSend(toWorld.provider.getDimensionId(), toWorld, playerIn);
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            DimensionManager.sendDimensionRegistration(toWorld, playerIn, dimension);
        }
        playerIn.playerNetServerHandler.sendPacket(new S07PacketRespawn(playerIn.dimension, fromWorld.getDifficulty(), fromWorld.getWorldInfo().getTerrainType(), playerIn.theItemInWorldManager.getGameType()));
        fromWorld.removePlayerEntityDangerously(playerIn);
        playerIn.isDead = false;
        // we do not need to call transferEntityToWorld as we already have the correct transform and created the portal in handleDisplaceEntityPortalEvent
        ((IMixinEntity) playerIn).setLocationAndAngles(event.getToTransform());
        toWorld.spawnEntityInWorld(playerIn);
        toWorld.updateEntityWithOptionalForce(playerIn, false);
        playerIn.setWorld(toWorld);
        this.preparePlayer(playerIn, fromWorld);
        playerIn.playerNetServerHandler.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.theItemInWorldManager.setWorld(toWorld);
        this.updateTimeAndWeatherForPlayer(playerIn, toWorld);
        this.syncPlayerInventory(playerIn);

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            playerIn.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }
        ((IMixinEntityPlayerMP) playerIn).refreshXpHealthAndFood();
    }

    // copy of transferEntityToWorld but only contains code to apply the location on entity before being placed into a portal
    @Override
    public void prepareEntityForPortal(Entity entityIn, WorldServer oldWorldIn, WorldServer toWorldIn) {
        oldWorldIn.theProfiler.startSection("moving");
        WorldProvider pOld = oldWorldIn.provider;
        WorldProvider pNew = toWorldIn.provider;
        double moveFactor = getMovementFactor(pOld) / getMovementFactor(pNew);
        double x = entityIn.posX * moveFactor;
        double y = entityIn.posY;
        double z = entityIn.posZ * moveFactor;

        if (!(pNew instanceof WorldProviderEnd)) {
            x = MathHelper.clamp_double(x, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
            z = MathHelper.clamp_double(z, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
            entityIn.setLocationAndAngles(x, entityIn.posY, z, entityIn.rotationYaw, entityIn.rotationPitch);
        }

        if (pNew instanceof WorldProviderEnd) {
            BlockPos blockpos;

            if (pOld instanceof WorldProviderEnd) {
                blockpos = toWorldIn.getSpawnPoint();
            } else {
                blockpos = toWorldIn.getSpawnCoordinate();
            }

            x = (double)blockpos.getX();
            y = (double)blockpos.getY();
            z = (double)blockpos.getZ();
            entityIn.setLocationAndAngles(x, y, z, 90.0F, 0.0F);
        }

        if (!(pOld instanceof WorldProviderEnd)) {
            x = (double)MathHelper.clamp_int((int)x, -29999872, 29999872);
            z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);

            if (entityIn.isEntityAlive()) {
                entityIn.setLocationAndAngles(x, y, z, entityIn.rotationYaw, entityIn.rotationPitch);
            }
        }

        if (entityIn.isEntityAlive()) {
            oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
        }

        oldWorldIn.theProfiler.endSection();
    }

    /**
     * @author blood - May 21st, 2016
     *
     * @reason - overwritten to redirect to our method that accepts a teleporter
     */
    @Overwrite
    public void transferEntityToWorld(Entity entityIn, int p_82448_2_, WorldServer oldWorldIn, WorldServer toWorldIn) {
        transferEntityToWorld(entityIn, p_82448_2_, oldWorldIn, toWorldIn, toWorldIn.getDefaultTeleporter());
    }

    /**
     * @author blood - May 21st, 2016
     *
     * @reason - rewritten to capture a plugin or mod that attempts to call this method directly.
     *
     * @param entityIn The entity being teleported
     * @param fromDimensionId The origin dimension id
     * @param fromWorld The origin world
     * @param toWorld The destination world
     * @param teleporter The teleporter being used to transport the entity
     */
    @Override
    public void transferEntityToWorld(Entity entityIn, int fromDimensionId, WorldServer fromWorld, WorldServer toWorld, net.minecraft.world.Teleporter teleporter) {
        // rewritten completely to handle our portal event
        DisplaceEntityEvent.Teleport.Portal event = SpongeCommonEventFactory.handleDisplaceEntityPortalEvent(entityIn, toWorld.provider.getDimensionId(), teleporter);
        if (event == null || event.isCancelled()) {
            return;
        }

        entityIn.setLocationAndAngles(event.getToTransform().getPosition().getX(), event.getToTransform().getPosition().getY(), event.getToTransform().getPosition().getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
        toWorld.spawnEntityInWorld(entityIn);
        toWorld.updateEntityWithOptionalForce(entityIn, false);
        entityIn.setWorld(toWorld);
    }

    // forge utility method
    private double getMovementFactor(WorldProvider provider) {
        if (provider instanceof WorldProviderHell) {
            return 8.0;
        }
        return 1.0;
    }

    @Inject(method = "setPlayerManager", at = @At("HEAD"), cancellable = true)
    private void onSetPlayerManager(WorldServer[] worldServers, CallbackInfo callbackInfo) {
        if (this.playerNBTManagerObj == null) {
            this.playerNBTManagerObj = worldServers[0].getSaveHandler().getPlayerNBTManager();
            // This is already added in our world constructor
            //worldServers[0].getWorldBorder().addListener(new PlayerBorderListener(0));
        }
        callbackInfo.cancel();
    }

    @Redirect(method = "updateTimeAndWeatherForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private WorldBorder onUpdateTimeGetWorldBorder(WorldServer worldServer, EntityPlayerMP entityPlayerMP, WorldServer worldServerIn) {
        return worldServerIn.getWorldBorder();
    }

    @Redirect(method = "updateTimeAndWeatherForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket"
            + "(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    public void onWorldBorderInitializePacket(NetHandlerPlayServer invoker, Packet packet, EntityPlayerMP playerMP, WorldServer worldServer) {
        if (worldServer.provider instanceof WorldProviderHell) {
            ((IMixinS44PacketWorldBorder) packet).netherifyCenterCoordinates();
        }

        invoker.sendPacket(packet);
    }

    @Inject(method = "playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V", at = @At("HEAD"))
    private void onPlayerLogOut(EntityPlayerMP player, CallbackInfo ci) {
        // Synchronise with user object
        NBTTagCompound nbt = new NBTTagCompound();
        player.writeToNBT(nbt);
        ((SpongeUser) ((IMixinEntityPlayerMP) player).getUserObject()).readFromNbt(nbt);

        // Remove player reference from scoreboard
        ((IMixinServerScoreboard) ((Player) player).getScoreboard()).removePlayer(player, false);
    }

    @Inject(method = "saveAllPlayerData()V", at = @At("RETURN"))
    private void onSaveAllPlayerData(CallbackInfo ci) {
        for (SpongeUser user : SpongeUser.dirtyUsers) {
            user.save();
        }
    }

    @Inject(method = "playerLoggedIn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/ServerConfigurationManager;"
            + "sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void playerLoggedIn2(EntityPlayerMP player, CallbackInfo ci) {
        // Create a packet to be used for players without context data
        S38PacketPlayerListItem noSpecificViewerPacket = new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, player);

        for (EntityPlayerMP viewer : this.playerEntityList) {
            if (((Player) viewer).canSee((Player) player)) {
                viewer.playerNetServerHandler.sendPacket(noSpecificViewerPacket);
            }

            if (((Player) player).canSee((Player) viewer)) {
                player.playerNetServerHandler.sendPacket(new S38PacketPlayerListItem(S38PacketPlayerListItem.Action.ADD_PLAYER, viewer));
            }
        }

        // Spawn player into level
        WorldServer level = this.mcServer.worldServerForDimension(player.dimension);
        org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) player;
        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity(spongeEntity)
                .type(SpawnTypes.PLACEMENT)
                .build();
        ((org.spongepowered.api.world.World) level).spawnEntity(spongeEntity, Cause.of(NamedCause.source(spawnCause)));
        this.preparePlayer(player, null);

        // We always want to cancel.
        ci.cancel();
    }

    @Inject(method = "writePlayerData", at = @At(target = WRITE_PLAYER_DATA, value = "INVOKE"))
    private void onWritePlayerFile(EntityPlayerMP playerMP, CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(playerMP.getUniqueID());
    }

}
