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
import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.data.manipulator.mutable.entity.RespawnLocationData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerRespawnEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.event.SpongeImplEventFactory;
import org.spongepowered.common.interfaces.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.IMixinWorldProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.border.PlayerBorderListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager {

    @Shadow private static Logger logger;
    @Shadow private MinecraftServer mcServer;
    @Shadow private IPlayerFileData playerNBTManagerObj;
    @SuppressWarnings("rawtypes")
    @Shadow public List playerEntityList;
    @Shadow public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP playerIn);
    @Shadow public abstract void setPlayerGameTypeBasedOnOther(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, net.minecraft.world.World worldIn);
    @Shadow protected abstract void func_96456_a(ServerScoreboard scoreboardIn, EntityPlayerMP playerIn);
    @Shadow public abstract MinecraftServer getServerInstance();
    @Shadow public abstract int getMaxPlayers();
    @Shadow public abstract void sendChatMsg(IChatComponent component);
    @Shadow public abstract void playerLoggedIn(EntityPlayerMP playerIn);
    @Shadow public Map<UUID, EntityPlayerMP> uuidToPlayerMap;

    /**
     * Bridge methods to proxy modified method in Vanilla, nothing in Forge
     */
    public void func_72355_a (NetworkManager netManager, EntityPlayerMP playerIn) {
        initializeConnectionToPlayer(netManager, playerIn, null);
    }

    /**
     * Bridge methods to proxy modified method in Vanilla, nothing in Forge
     */
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn) {
        initializeConnectionToPlayer(netManager, playerIn, null);
    }

    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn, @Nullable NetHandlerPlayServer handler) {
        GameProfile gameprofile = playerIn.getGameProfile();
        PlayerProfileCache playerprofilecache = this.mcServer.getPlayerProfileCache();
        GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.addEntry(gameprofile);
        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
        WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);

        if (worldserver == null) {
            playerIn.dimension = 0;
            worldserver = this.mcServer.worldServerForDimension(0);
            BlockPos spawnPoint = ((IMixinWorldProvider) worldserver.provider).getRandomizedSpawnPoint();
            playerIn.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }

        playerIn.setWorld(worldserver);
        playerIn.theItemInWorldManager.setWorld((WorldServer) playerIn.worldObj);
        String s1 = "local";

        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        WorldInfo worldinfo = worldserver.getWorldInfo();
        BlockPos blockpos = worldserver.getSpawnPoint();
        this.setPlayerGameTypeBasedOnOther(playerIn, null, worldserver);

        // Sponge Start

        if (handler == null) {
            // Create the handler here (so the player's gets set)
            handler = new NetHandlerPlayServer(this.mcServer, netManager, playerIn);
        }

        playerIn.playerNetServerHandler = handler;

        // Sponge End

        // Support vanilla clients logging into custom dimensions
        int dimension = DimensionManager.getClientDimensionToSend(worldserver.provider.getDimensionId(), worldserver, playerIn);
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            DimensionManager.sendDimensionRegistration(worldserver, playerIn, dimension);
        }

        handler.sendPacket(new S01PacketJoinGame(playerIn.getEntityId(), playerIn.theItemInWorldManager.getGameType(), worldinfo
                .isHardcoreModeEnabled(), dimension, worldserver.getDifficulty(), this.getMaxPlayers(), worldinfo
                .getTerrainType(), worldserver.getGameRules().getGameRuleBooleanValue("reducedDebugInfo")));
        handler.sendPacket(new S3FPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this
                .getServerInstance().getServerModName())));
        handler.sendPacket(new S41PacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        handler.sendPacket(new S05PacketSpawnPosition(blockpos));
        handler.sendPacket(new S39PacketPlayerAbilities(playerIn.capabilities));
        handler.sendPacket(new S09PacketHeldItemChange(playerIn.inventory.currentItem));
        playerIn.getStatFile().func_150877_d();
        playerIn.getStatFile().func_150884_b(playerIn);
        this.mcServer.refreshStatusNextTick();

        this.playerLoggedIn(playerIn);
        handler.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        this.updateTimeAndWeatherForPlayer(playerIn, worldserver);

        if (this.mcServer.getResourcePackUrl().length() > 0) {
            playerIn.loadResourcePack(this.mcServer.getResourcePackUrl(), this.mcServer.getResourcePackHash());
        }

        playerIn.addSelfToInternalCraftingInventory();


        // Sponge Start

        // Move logic for creating join message up here
        ChatComponentTranslation chatcomponenttranslation;

        if (!playerIn.getCommandSenderName().equalsIgnoreCase(s))
        {
            chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined.renamed", new Object[] {playerIn.getDisplayName(), s});
        }
        else
        {
            chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined", new Object[] {playerIn.getDisplayName()});
        }

        chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);

        // Fire PlayerJoinEvent
        final PlayerJoinEvent event = SpongeImplEventFactory.createPlayerJoin(Sponge.getGame(), (Player) playerIn, ((Player) playerIn).getLocation(),
                SpongeTexts.toText(chatcomponenttranslation), ((Player) playerIn).getMessageSink());
        Sponge.getGame().getEventManager().post(event);
        // Set the resolved location of the event onto the player
        ((Player) playerIn).setLocation(event.getTransform().getLocation());

        logger.info(playerIn.getCommandSenderName() + "[" + s1 + "] logged in with entity id " + playerIn.getEntityId() + " at (" + playerIn.posX
                + ", " + playerIn.posY + ", " + playerIn.posZ + ")");

        // Sponge start -> Send to the sink
        event.getSink().sendMessage(event.getNewMessage());
        // Sponge end

        this.func_96456_a((ServerScoreboard) worldserver.getScoreboard(), playerIn);

        for (Object o : playerIn.getActivePotionEffects()) {
            PotionEffect potioneffect = (PotionEffect) o;
            handler.sendPacket(new S1DPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

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

    @SuppressWarnings("unchecked")
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int targetDimension, boolean conqueredEnd) {

        // ### PHASE 1 ### Get the location to spawn

        // Vanilla will always use overworld, set to the world the player was in
        // UNLESS comming back from the end.
        if (!conqueredEnd && targetDimension == 0) {
            targetDimension = playerIn.dimension;
        }
        Location<World> location = this.getPlayerRespawnLocation(playerIn, targetDimension);

        // Keep players out of blocks
        Vector3d tempPos = ((Player) playerIn).getLocation().getPosition();
        playerIn.setPosition(location.getX(), location.getY(), location.getZ());
        while (!((WorldServer) location.getExtent()).getCollidingBoundingBoxes(playerIn, playerIn.getEntityBoundingBox()).isEmpty()) {
            playerIn.setPosition(playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ);
            location = location.add(0, 1, 0);
        }
        playerIn.setPosition(tempPos.getX(), tempPos.getY(), tempPos.getZ());


        // ### PHASE 2 ### Remove player from current dimension
        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerForPlayer().getEntityTracker().untrackEntity(playerIn);
        playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);

        // ### PHASE 3 ### Reset player (if applicable)
        playerIn.playerConqueredTheEnd = false;
        if (!conqueredEnd) { // don't reset player if returning from end
            ((IMixinEntityPlayerMP) playerIn).reset();
        }
        playerIn.setSneaking(false);

        // ### PHASE 4 ### Fire event and set new location on the player
        final PlayerRespawnEvent event =
                SpongeImplEventFactory.createPlayerRespawn(Sponge.getGame(), (Player) playerIn, this.tempIsBedSpawn, location);
        this.tempIsBedSpawn = false;
        Sponge.getGame().getEventManager().post(event);
        location = event.getNewRespawnTransform().getLocation();

        if (!(location.getExtent() instanceof WorldServer)) {
            Sponge.getLogger().warn("Location set in PlayerRespawnEvent was invalid, using original location instead");
            location = event.getNewRespawnTransform().getLocation();
        }
        final WorldServer targetWorld = (WorldServer) location.getExtent();

        playerIn.dimension = targetWorld.provider.getDimensionId();
        playerIn.setWorld(targetWorld);
        playerIn.theItemInWorldManager.setWorld(targetWorld);

        targetWorld.theChunkProviderServer.loadChunk((int) location.getX() >> 4, (int) location.getZ() >> 4);

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
                playerIn.rotationYaw, playerIn.rotationPitch);

        final BlockPos spawnLocation = targetWorld.getSpawnPoint();
        playerIn.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(spawnLocation));
        playerIn.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(playerIn.experience, playerIn.experienceTotal,
                playerIn.experienceLevel));
        this.updateTimeAndWeatherForPlayer(playerIn, targetWorld);
        targetWorld.getPlayerManager().addPlayer(playerIn);
        targetWorld.spawnEntityInWorld(playerIn);
        this.playerEntityList.add(playerIn);
        playerIn.addSelfToInternalCraftingInventory();
        playerIn.setHealth(playerIn.getHealth());

        return playerIn;
    }

    // Internal. Note: Has side-effects
    private Location<World> getPlayerRespawnLocation(EntityPlayerMP playerIn, int targetDimension) {
        this.tempIsBedSpawn = false;
        WorldServer targetWorld = this.mcServer.worldServerForDimension(targetDimension);
        if (targetWorld == null) { // Target world doesn't exist? Use global
            return ((World) this.mcServer.getEntityWorld()).getSpawnLocation();
        }
        Dimension targetDim = (Dimension) targetWorld.provider;
        // Cannot respawn in requested world, use the fallback dimension for
        // that world. (Usually overworld unless a mod says otherwise).
        if (!targetDim.allowsPlayerRespawns()) {
            targetDimension = ((IMixinWorldProvider) targetDim).getRespawnDimension(playerIn);
            targetWorld = this.mcServer.worldServerForDimension(targetDimension);
            targetDim = (Dimension) targetWorld.provider;
        }
        // Use data attached to the player if possible
        Optional<RespawnLocationData> optRespawn = ((Player) playerIn).get(RespawnLocationData.class);
        if (optRespawn.isPresent()) {
            // API TODO: Make this support multiple world spawn points
            // TODO Make RespawnLocationData 'shadow' the bed location from below
            return null;
        }
        Vector3d spawnPos = null;
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
        if (spawnPos == null) {
            spawnPos = VecHelper.toVector(targetWorld.getSpawnPoint()).toDouble();
        }
        return new Location<World>((World) targetWorld, spawnPos);
    }

    @Overwrite
    public void setPlayerManager(WorldServer[] worldServers) {
        if (this.playerNBTManagerObj != null) {
            return;
        }
        this.playerNBTManagerObj = worldServers[0].getSaveHandler().getPlayerNBTManager();
        worldServers[0].getWorldBorder().addListener(new PlayerBorderListener());
    }

    @Overwrite
    public void updateTimeAndWeatherForPlayer(EntityPlayerMP playerIn, WorldServer worldIn) {
        net.minecraft.world.border.WorldBorder worldborder = worldIn.getWorldBorder();
        playerIn.playerNetServerHandler.sendPacket(new S44PacketWorldBorder(worldborder, S44PacketWorldBorder.Action.INITIALIZE));
        playerIn.playerNetServerHandler.sendPacket(new S03PacketTimeUpdate(worldIn.getTotalWorldTime(), worldIn.getWorldTime(), worldIn
                .getGameRules().getGameRuleBooleanValue("doDaylightCycle")));

        if (worldIn.isRaining()) {
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(1, 0.0F));
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(7, worldIn.getRainStrength(1.0F)));
            playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(8, worldIn.getThunderStrength(1.0F)));
        }
    }
}
