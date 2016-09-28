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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
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
import org.spongepowered.api.event.entity.MoveEntityEvent;
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
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.PlayerPhase;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.play.server.IMixinSPacketWorldBorder;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

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

    private static final String WRITE_PLAYER_DATA =
            "Lnet/minecraft/world/storage/IPlayerFileData;writePlayerData(Lnet/minecraft/entity/player/EntityPlayer;)V";
    private static final String
            SERVER_SEND_PACKET_TO_ALL_PLAYERS =
            "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V";
    private static final String NET_HANDLER_SEND_PACKET = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V";
    @Shadow @Final private static Logger LOG;
    @Shadow @Final private MinecraftServer mcServer;
    @Shadow @Final public Map<UUID, EntityPlayerMP> uuidToPlayerMap;
    @Shadow @Final public List<EntityPlayerMP> playerEntityList;
    @Shadow private IPlayerFileData playerNBTManagerObj;
    @Shadow public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn);
    @Shadow public abstract MinecraftServer getServerInstance();
    @Shadow public abstract int getMaxPlayers();
    @Shadow public abstract void sendChatMsg(ITextComponent component);
    @Shadow public abstract void sendPacketToAllPlayers(Packet<?> packetIn);
    @Shadow public abstract void preparePlayer(EntityPlayerMP playerIn, @Nullable WorldServer worldIn);
    @Shadow public abstract void playerLoggedIn(EntityPlayerMP playerIn);
    @Shadow public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn);
    @Shadow public abstract void updatePermissionLevel(EntityPlayerMP p_187243_1_);
    @Shadow public abstract void syncPlayerInventory(EntityPlayerMP playerIn);
    @Nullable @Shadow public abstract String allowUserToConnect(SocketAddress address, GameProfile profile);
    @Shadow private void setPlayerGameTypeBasedOnOther(EntityPlayerMP playerIn, @Nullable EntityPlayerMP other, net.minecraft.world.World worldIn) {
        // Shadowed
    }

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
        ITextComponent reason;
        if (disconnectMessage.isPresent()) {
            reason = SpongeTexts.toComponent(disconnectMessage.get());
        } else {
            reason = new TextComponentTranslation("disconnect.disconnected");
        }

        try {
            LOG.info("Disconnecting " + (profile != null ? profile.toString() + " (" + netManager.getRemoteAddress().toString() + ")" : String.valueOf(netManager.getRemoteAddress() + ": " + reason.getUnformattedText())));
            netManager.sendPacket(new SPacketDisconnect(reason));
            netManager.closeChannel(reason);
        } catch (Exception exception) {
            LOG.error("Error whilst disconnecting player", exception);
        }
    }

    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, @Nullable NetHandlerPlayServer handler) {
        GameProfile gameprofile = playerIn.getGameProfile();
        PlayerProfileCache playerprofilecache = this.mcServer.getPlayerProfileCache();
        GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.addEntry(gameprofile);

        // Sponge start - save changes to offline User before reading player data
        SpongeUser user = (SpongeUser) ((IMixinEntityPlayerMP) playerIn).getUserObject();
        if (SpongeUser.dirtyUsers.contains(user)) {
            user.save();
        }
        // Sponge end

        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
        WorldServer worldServer = this.mcServer.worldServerForDimension(playerIn.dimension);
        int actualDimensionId = ((IMixinWorldServer) worldServer).getDimensionId();
        BlockPos spawnPos = null;
        // Join data
        Optional<Instant> firstJoined = SpongePlayerDataHandler.getFirstJoined(playerIn.getUniqueID());
        Instant lastJoined = Instant.now();
        SpongePlayerDataHandler.setPlayerInfo(playerIn.getUniqueID(), firstJoined.orElse(lastJoined), lastJoined);

        if (actualDimensionId != playerIn.dimension) {
            SpongeImpl.getLogger().warn("Player [{}] has attempted to login to unloaded world [{}]. This is not safe so we have moved them to "
                    + "the default world's spawn point.", playerIn.getName(), playerIn.dimension);
            if (!firstJoined.isPresent()) {
                spawnPos = SpongeImplHooks.getRandomizedSpawnPoint(worldServer);
            } else {
                spawnPos = worldServer.getSpawnPoint();
            }
            playerIn.dimension = actualDimensionId;
            playerIn.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
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
        Transform<World> fromTransform = player.getTransform().setExtent((World) worldServer);

        ClientConnectionEvent.Login loginEvent = SpongeEventFactory.createClientConnectionEventLogin(
                Cause.of(NamedCause.source(player)), fromTransform, fromTransform, (RemoteConnection) netManager,
                new MessageEvent.MessageFormatter(disconnectMessage), (org.spongepowered.api.profile.GameProfile) gameprofile, player, false
        );

        if (kickReason != null) {
            loginEvent.setCancelled(true);
        }

        if (SpongeImpl.postEvent(loginEvent)) {
            disconnectClient(netManager, loginEvent.isMessageCancelled() ? Optional.empty() : Optional.of(loginEvent.getMessage()), gameprofile);
            return;
        }

        // Sponge end

        worldServer = (WorldServer) loginEvent.getToTransform().getExtent();
        double x = loginEvent.getToTransform().getPosition().getX();
        double y = loginEvent.getToTransform().getPosition().getY();
        double z = loginEvent.getToTransform().getPosition().getZ();
        float pitch = (float) loginEvent.getToTransform().getPitch();
        float yaw = (float) loginEvent.getToTransform().getYaw();

        playerIn.dimension = ((IMixinWorldServer) worldServer).getDimensionId();
        playerIn.setWorld(worldServer);
        playerIn.interactionManager.setWorld((WorldServer) playerIn.worldObj);
        playerIn.setPositionAndRotation(x, y, z, yaw, pitch);
        // make sure the chunk is loaded for login
        worldServer.getChunkProvider().loadChunk(loginEvent.getToTransform().getLocation().getChunkPosition().getX(), loginEvent.getToTransform().getLocation().getChunkPosition().getZ());
        // Sponge end

        String s1 = "local";

        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        final WorldInfo worldinfo = worldServer.getWorldInfo();
        final BlockPos spawnBlockPos = worldServer.getSpawnPoint();
        this.setPlayerGameTypeBasedOnOther(playerIn, null, worldServer);

        // Sponge start
        if (handler == null) {
            // Create the handler here (so the player's gets set)
            handler = new NetHandlerPlayServer(this.mcServer, netManager, playerIn);
        }
        playerIn.connection = handler;
        // Sponge end

        // Support vanilla clients logging into custom dimensions
        final int dimensionId = WorldManager.getClientDimensionId(playerIn, worldServer);

        // Send dimension registration
        WorldManager.sendDimensionRegistration(playerIn, worldServer.provider);

        handler.sendPacket(new SPacketJoinGame(playerIn.getEntityId(), playerIn.interactionManager.getGameType(), worldinfo
                .isHardcoreModeEnabled(), dimensionId, worldServer.getDifficulty(), this.getMaxPlayers(), worldinfo
                .getTerrainType(), worldServer.getGameRules().getBoolean("reducedDebugInfo")));
        handler.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this
                .getServerInstance().getServerModName())));
        handler.sendPacket(new SPacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        handler.sendPacket(new SPacketSpawnPosition(spawnBlockPos));
        handler.sendPacket(new SPacketPlayerAbilities(playerIn.capabilities));
        handler.sendPacket(new SPacketHeldItemChange(playerIn.inventory.currentItem));
        this.updatePermissionLevel(playerIn);
        playerIn.getStatFile().markAllDirty();
        playerIn.getStatFile().sendAchievements(playerIn);
        this.mcServer.refreshStatusNextTick();

        handler.setPlayerLocation(x, y, z, yaw, pitch);
        this.playerLoggedIn(playerIn);

        // Sponge start - add world name to message
        LOG.info(playerIn.getName() + "[" + s1 + "] logged in with entity id " + playerIn.getEntityId() + " in "
                + worldServer.getWorldInfo().getWorldName() + "(" + ((IMixinWorldServer) worldServer).getDimensionId()
                + ") at (" + playerIn.posX + ", " + playerIn.posY + ", " + playerIn.posZ + ")");
        // Sponge end

        this.updateTimeAndWeatherForPlayer(playerIn, worldServer);

        // Sponge Start - Use the server's ResourcePack object
        Optional<ResourcePack> pack = ((Server)this.mcServer).getDefaultResourcePack();
        if (pack.isPresent()) {
            ((Player)playerIn).sendResourcePack(pack.get());
        }
        // Sponge End

        // Sponge Start
        //
        // This sends the objective/score creation packets
        // to the player, without attempting to remove them from their
        // previous scoreboard (which is set in a field initializer).
        // This allows #getWorldScoreboard to function
        // as normal, without causing issues when it is initialized on the client.

        ((IMixinEntityPlayerMP) playerIn).initScoreboard();

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            handler.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        if (nbttagcompound != null) {
            if (nbttagcompound.hasKey("RootVehicle", 10)) {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("RootVehicle");
                Entity entity2 = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompoundTag("Entity"), worldServer, true);

                if (entity2 != null) {
                    UUID uuid = nbttagcompound1.getUniqueId("Attach");

                    if (entity2.getUniqueID().equals(uuid)) {
                        playerIn.startRiding(entity2, true);
                    } else {
                        for (Entity entity : entity2.getRecursivePassengers()) {
                            if (entity.getUniqueID().equals(uuid)) {
                                playerIn.startRiding(entity, true);
                                break;
                            }
                        }
                    }

                    if (!playerIn.isRiding()) {
                        LOG.warn("Couldn\'t reattach entity to player");
                        worldServer.removeEntityDangerously(entity2);

                        for (Entity entity3 : entity2.getRecursivePassengers()) {
                            worldServer.removeEntityDangerously(entity3);
                        }
                    }
                }
            } else if (nbttagcompound.hasKey("Riding", 10)) {
                Entity entity1 = AnvilChunkLoader.readWorldEntity(nbttagcompound.getCompoundTag("Riding"), worldServer, true);

                if (entity1 != null) {
                    playerIn.startRiding(entity1, true);
                }
            }
        }

        playerIn.addSelfToInternalCraftingInventory();

        TextComponentTranslation chatcomponenttranslation;

        if (!playerIn.getName().equalsIgnoreCase(s))
        {
            chatcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined.renamed", playerIn.getDisplayName(), s);
        }
        else
        {
            chatcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined", playerIn.getDisplayName());
        }

        chatcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);

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
    }

    // A temporary variable to transfer the 'isBedSpawn' variable between
    // getPlayerRespawnLocation and recreatePlayerEntity
    private boolean tempIsBedSpawn = false;

    /**
     * @author Zidane - June 13th, 2015
     * @author simon816 - June 24th, 2015
     * @author Zidane - March 29th, 2016
     * @author gabizou - June 5th, 2016 - Update for teleportation changes to keep the same player.
     *
     * @reason - Direct respawning players to use Sponge events
     * and process appropriately.
     *
     * @param entityPlayerMP The player being respawned/created
     * @param targetDimension The target dimension
     * @param conqueredEnd Whether the end was conquered
     * @return The new player
     */
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP entityPlayerMP, int targetDimension, boolean conqueredEnd) {
        // ### PHASE 1 ### Get the location to spawn

        // Vanilla will always use overworld, set to the world the player was in
        // UNLESS comming back from the end.
        if (!conqueredEnd && targetDimension == 0) {
            targetDimension = entityPlayerMP.dimension;
        }

        if (entityPlayerMP.isBeingRidden()) {
            entityPlayerMP.removePassengers();
        }

        if (entityPlayerMP.isRiding()) {
            entityPlayerMP.dismountRidingEntity();
        }

        final Player player = (Player) entityPlayerMP;
        final Transform<World> fromTransform = player.getTransform();
        WorldServer worldServer = this.mcServer.worldServerForDimension(targetDimension);
        Transform<World> toTransform = new Transform<>(this.getPlayerRespawnLocation(entityPlayerMP, worldServer), Vector3d.ZERO, Vector3d.ZERO);
        Location<World> location = toTransform.getLocation();

        // If coming from end, fire a teleport event for plugins
        if (conqueredEnd) {
            // When leaving the end, players are never placed inside the teleporter but instead "respawned" in the target world
            MoveEntityEvent.Teleport teleportEvent = EntityUtil.handleDisplaceEntityTeleportEvent(entityPlayerMP, location);
            if (teleportEvent.isCancelled()) {
                entityPlayerMP.playerConqueredTheEnd = false;
                return entityPlayerMP;
            }

            toTransform = teleportEvent.getToTransform();
            location = toTransform.getLocation();
        }
        // Keep players out of blocks
        Vector3d tempPos = player.getLocation().getPosition();
        entityPlayerMP.setPosition(location.getX(), location.getY(), location.getZ());
        while (!((WorldServer) location.getExtent()).getCollisionBoxes(entityPlayerMP, entityPlayerMP.getEntityBoundingBox()).isEmpty()) {
            entityPlayerMP.setPosition(entityPlayerMP.posX, entityPlayerMP.posY + 1.0D, entityPlayerMP.posZ);
            location = location.add(0, 1, 0);
        }
        entityPlayerMP.setPosition(tempPos.getX(), tempPos.getY(), tempPos.getZ());

        // ### PHASE 2 ### Remove player from current dimension
        entityPlayerMP.getServerWorld().getEntityTracker().removePlayerFromTrackers(entityPlayerMP);
        entityPlayerMP.getServerWorld().getPlayerChunkMap().removePlayer(entityPlayerMP);
        this.playerEntityList.remove(entityPlayerMP);
        this.mcServer.worldServerForDimension(entityPlayerMP.dimension).removeEntityDangerously(entityPlayerMP);

        // ### PHASE 3 ### Reset player (if applicable)
        entityPlayerMP.playerConqueredTheEnd = false;
        if (!conqueredEnd) { // don't reset player if returning from end
            ((IMixinEntityPlayerMP) entityPlayerMP).reset();
        }
        entityPlayerMP.setSneaking(false);
        // update to safe location
        toTransform = toTransform.setLocation(location);

        ((IMixinEntityPlayerMP) entityPlayerMP).resetAttributeMap();

        // ### PHASE 4 ### Fire event and set new location on the player
        final RespawnPlayerEvent event = SpongeImplHooks.createRespawnPlayerEvent(Cause.of(NamedCause.source(entityPlayerMP)), fromTransform,
                toTransform, (Player) entityPlayerMP, this.tempIsBedSpawn);
        this.tempIsBedSpawn = false;
        SpongeImpl.postEvent(event);
        ((IMixinEntity) (Object) player).setLocationAndAngles(event.getToTransform());
        toTransform = event.getToTransform();
        location = toTransform.getLocation();

        if (!(location.getExtent() instanceof WorldServer)) {
            SpongeImpl.getLogger().warn("Location set in PlayerRespawnEvent was invalid, using original location instead");
            location = event.getFromTransform().getLocation();
        }
        worldServer = (WorldServer) location.getExtent();

        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
        entityPlayerMP.dimension = mixinWorldServer.getDimensionId();
        entityPlayerMP.setWorld(worldServer);
        entityPlayerMP.interactionManager.setWorld(worldServer);

        worldServer.getChunkProvider().loadChunk((int) location.getX() >> 4, (int) location.getZ() >> 4);

        // ### PHASE 5 ### Respawn player in new world

        // Support vanilla clients logging into custom dimensions
        final int dimensionId = WorldManager.getClientDimensionId(entityPlayerMP, worldServer);

        // Send dimension registration
        if (((IMixinEntityPlayerMP) entityPlayerMP).usesCustomClient()) {
            WorldManager.sendDimensionRegistration(entityPlayerMP, worldServer.provider);
        } else {
            // Force vanilla client to refresh its chunk cache if same dimension type
            if (fromTransform.getExtent() != worldServer && fromTransform.getExtent().getDimension().getType() == toTransform.getExtent().getDimension().getType()) {
                entityPlayerMP.connection.sendPacket(new SPacketRespawn((dimensionId >= 0 ? -1 : 0), worldServer.getDifficulty(), worldServer
                        .getWorldInfo().getTerrainType(), entityPlayerMP.interactionManager.getGameType()));
            }
        }
        entityPlayerMP.connection.sendPacket(new SPacketRespawn(dimensionId, worldServer.getDifficulty(), worldServer
                .getWorldInfo().getTerrainType(), entityPlayerMP.interactionManager.getGameType()));
        entityPlayerMP.isDead = false;
        entityPlayerMP.connection.setPlayerLocation(location.getX(), location.getY(), location.getZ(),
                (float) toTransform.getYaw(), (float) toTransform.getPitch());

        final BlockPos spawnLocation = worldServer.getSpawnPoint();
        entityPlayerMP.connection.sendPacket(new SPacketSpawnPosition(spawnLocation));
        entityPlayerMP.connection.sendPacket(new SPacketSetExperience(entityPlayerMP.experience, entityPlayerMP.experienceTotal,
                entityPlayerMP.experienceLevel));
        this.updateTimeAndWeatherForPlayer(entityPlayerMP, worldServer);
        worldServer.getPlayerChunkMap().addPlayer(entityPlayerMP);
        org.spongepowered.api.entity.Entity spongeEntity = player;
        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity(spongeEntity)
                .type(SpawnTypes.PLACEMENT)
                .build();
        ((org.spongepowered.api.world.World) worldServer).spawnEntity(spongeEntity, Cause.of(NamedCause.source(spawnCause)));
        this.playerEntityList.add(entityPlayerMP);
        entityPlayerMP.addSelfToInternalCraftingInventory();

        // Reset the health.
        final MutableBoundedValue<Double> maxHealth = ((Player) entityPlayerMP).maxHealth();
        final MutableBoundedValue<Integer> food = ((Player) entityPlayerMP).foodLevel();
        final MutableBoundedValue<Double> saturation = ((Player) entityPlayerMP).saturation();

        entityPlayerMP.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0F);
        entityPlayerMP.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth.get().floatValue());
        entityPlayerMP.connection.sendPacket(new SPacketUpdateHealth(maxHealth.get().floatValue(), food.get(), saturation.get().floatValue()));
        ((IMixinEntityPlayerMP) entityPlayerMP).refreshXpHealthAndFood();

        for (PotionEffect potioneffect : entityPlayerMP.getActivePotionEffects()) {
            entityPlayerMP.connection.sendPacket(new SPacketEntityEffect(entityPlayerMP.getEntityId(), potioneffect));
        }

        entityPlayerMP.sendPlayerAbilities();

        return entityPlayerMP;
    }

    // Internal. Note: Has side-effects
    private Location<World> getPlayerRespawnLocation(EntityPlayerMP playerIn, @Nullable WorldServer targetWorld) {
        final Location<World> location = ((World) playerIn.worldObj).getSpawnLocation();
        this.tempIsBedSpawn = false;
        if (targetWorld == null) { // Target world doesn't exist? Use global
            return location;
        }

        final Dimension targetDimension = (Dimension) targetWorld.provider;
        int targetDimensionId = ((IMixinWorldServer) targetWorld).getDimensionId();
        // Cannot respawn in requested world, use the fallback dimension for
        // that world. (Usually overworld unless a mod says otherwise).
        if (!targetDimension.allowsPlayerRespawns()) {
            targetDimensionId = ((IMixinWorldProvider) targetDimension).getRespawnDimension(playerIn);
            targetWorld = this.mcServer.worldServerForDimension(targetDimensionId);
        }

        Vector3d targetSpawnVec = VecHelper.toVector3d(targetWorld.getSpawnPoint());
        BlockPos bedPos = ((IMixinEntityPlayer) playerIn).getBedLocation(targetDimensionId);
        if (bedPos != null) { // Player has a bed
            boolean forceBedSpawn = ((IMixinEntityPlayer) playerIn).isSpawnForced(targetDimensionId);
            BlockPos bedSpawnLoc = EntityPlayer.getBedSpawnLocation(targetWorld, bedPos, forceBedSpawn);
            if (bedSpawnLoc != null) { // The bed exists and is not obstructed
                this.tempIsBedSpawn = true;
                targetSpawnVec = new Vector3d(bedSpawnLoc.getX() + 0.5D, bedSpawnLoc.getY() + 0.1D, bedSpawnLoc.getZ() + 0.5D);
            } else { // Bed invalid
                playerIn.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
                // Vanilla behaviour - Delete the known bed location if invalid
                bedPos = null; // null = remove location
            }
            // Set the new bed location for the new dimension
            int prevDim = playerIn.dimension; // Temporarily for setSpawnPoint
            playerIn.dimension = targetDimensionId;
            playerIn.setSpawnPoint(bedPos, forceBedSpawn);
            playerIn.dimension = prevDim;
        }
        return new Location<>((World) targetWorld, targetSpawnVec);
    }

    /**
     * @author blood - May 21st, 2016
     * @author gabizou - June 2nd, 2016 - Update for 1.9.4 and cause tracker changes
     *
     * @reason - adjusted to support {@link MoveEntityEvent.Teleport.Portal}
     *
     * @param playerIn The player teleporting to another dimension
     * @param targetDimensionId The id of target dimension.
     * @param teleporter The teleporter used to transport and create the portal
     */
    @Override
    public void transferPlayerToDimension(EntityPlayerMP playerIn, int targetDimensionId, net.minecraft.world.Teleporter teleporter) {
        MoveEntityEvent.Teleport.Portal event = EntityUtil.handleDisplaceEntityPortalEvent(playerIn, targetDimensionId, teleporter);
        if (event == null || event.isCancelled()) {
            return;
        }

        WorldServer fromWorld = (WorldServer) event.getFromTransform().getExtent();
        WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();
        playerIn.dimension = WorldManager.getClientDimensionId(playerIn, toWorld);
        toWorld.getChunkProvider().loadChunk(event.getToTransform().getLocation().getChunkPosition().getX(), event.getToTransform().getLocation().getChunkPosition().getZ());
        // Support vanilla clients teleporting to custom dimensions
        final int dimensionId = playerIn.dimension;

        // Send dimension registration
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            WorldManager.sendDimensionRegistration(playerIn, toWorld.provider);
        } else {
            // Force vanilla client to refresh its chunk cache if same dimension type
            if (fromWorld != toWorld && fromWorld.provider.getDimensionType() == toWorld.provider.getDimensionType()) {
                playerIn.connection.sendPacket(new SPacketRespawn((dimensionId >= 0 ? -1 : 0), toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
            }
        }
        playerIn.connection.sendPacket(new SPacketRespawn(dimensionId, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
        fromWorld.removeEntityDangerously(playerIn);
        playerIn.isDead = false;
        // we do not need to call transferEntityToWorld as we already have the correct transform and created the portal in handleDisplaceEntityPortalEvent
        ((IMixinEntity) playerIn).setLocationAndAngles(event.getToTransform());
        playerIn.setWorld(toWorld);
        toWorld.spawnEntityInWorld(playerIn);
        toWorld.updateEntityWithOptionalForce(playerIn, false);
        this.preparePlayer(playerIn, fromWorld);
        playerIn.connection.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.interactionManager.setWorld(toWorld);
        this.updateTimeAndWeatherForPlayer(playerIn, toWorld);
        this.syncPlayerInventory(playerIn);

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            playerIn.connection.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }
        ((IMixinEntityPlayerMP) playerIn).refreshXpHealthAndFood();

        SpongeImplHooks.handlePostChangeDimensionEvent(playerIn, fromWorld, toWorld);
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

//        if (!(pNew instanceof WorldProviderEnd)) {
//            x = MathHelper.clamp_double(x, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
//            z = MathHelper.clamp_double(z, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
//            entityIn.setLocationAndAngles(x, entityIn.posY, z, entityIn.rotationYaw, entityIn.rotationPitch);
//        }

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
            oldWorldIn.theProfiler.startSection("placing");
            x = (double)MathHelper.clamp_int((int)x, -29999872, 29999872);
            z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);

            if (entityIn.isEntityAlive()) {
                entityIn.setLocationAndAngles(x, y, z, entityIn.rotationYaw, entityIn.rotationPitch);
            }
            oldWorldIn.theProfiler.endSection();
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
        MoveEntityEvent.Teleport.Portal event = EntityUtil
                .handleDisplaceEntityPortalEvent(entityIn, WorldManager.getDimensionId(toWorld), teleporter);
        if (event == null || event.isCancelled()) {
            return;
        }

        entityIn.setLocationAndAngles(event.getToTransform().getPosition().getX(), event.getToTransform().getPosition().getY(), event.getToTransform().getPosition().getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
        toWorld.spawnEntityInWorld(entityIn);
        toWorld.updateEntityWithOptionalForce(entityIn, false);
        entityIn.setWorld(toWorld);
    }

    // forge utility method
    @Override
    public double getMovementFactor(WorldProvider provider) {
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
    public void onWorldBorderInitializePacket(NetHandlerPlayServer invoker, Packet<?> packet, EntityPlayerMP playerMP, WorldServer worldServer) {
        if (worldServer.provider instanceof WorldProviderHell) {
            ((IMixinSPacketWorldBorder) packet).netherifyCenterCoordinates();
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

    @Redirect(method = "playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;removeEntity(Lnet/minecraft/entity/Entity;)V"))
    private void onPlayerRemoveFromWorldFromDisconnect(WorldServer world, Entity player, EntityPlayerMP playerMP) {
        final CauseTracker causeTracker = ((IMixinWorldServer) world).getCauseTracker();
        causeTracker.switchToPhase(PlayerPhase.State.PLAYER_LOGOUT, PhaseContext.start()
                .add(NamedCause.source(playerMP))
                .addCaptures()
                .complete()
        );
        world.removeEntity(player);
        causeTracker.completePhase();
    }

    @Inject(method = "saveAllPlayerData()V", at = @At("RETURN"))
    private void onSaveAllPlayerData(CallbackInfo ci) {
        for (SpongeUser user : SpongeUser.dirtyUsers) {
            user.save();
        }
    }

    @Inject(method = "playerLoggedIn", at = @At(value = "INVOKE", target = SERVER_SEND_PACKET_TO_ALL_PLAYERS, shift = At.Shift.BEFORE), cancellable = true)
    public void playerLoggedIn2(EntityPlayerMP player, CallbackInfo ci) {
        // Create a packet to be used for players without context data
        SPacketPlayerListItem noSpecificViewerPacket = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, player);

        for (EntityPlayerMP viewer : this.playerEntityList) {
            if (((Player) viewer).canSee((Player) player)) {
                viewer.connection.sendPacket(noSpecificViewerPacket);
            }

            if (((Player) player).canSee((Player) viewer)) {
                player.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, viewer));
            }
        }

        // Spawn player into level
        WorldServer level = this.mcServer.worldServerForDimension(player.dimension);
        // TODO direct this appropriately
        level.spawnEntityInWorld(player);
        this.preparePlayer(player, null);

        // We always want to cancel.
        ci.cancel();
    }

    @Inject(method = "writePlayerData", at = @At(target = WRITE_PLAYER_DATA, value = "INVOKE"))
    private void onWritePlayerFile(EntityPlayerMP playerMP, CallbackInfo callbackInfo) {
        SpongePlayerDataHandler.savePlayer(playerMP.getUniqueID());
    }

}
