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
import com.flowpowered.math.vector.Vector3i;
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
import org.spongepowered.api.data.manipulator.entity.RespawnLocationData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerRespawnEvent;
import org.spongepowered.api.scoreboard.Scoreboard;
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
import org.spongepowered.common.interfaces.IMixinServerConfigurationManager;
import org.spongepowered.common.interfaces.IMixinWorldProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.border.PlayerBorderListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager implements IMixinServerConfigurationManager {

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

    private Scoreboard scoreboard;

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

    @SuppressWarnings("rawtypes")
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
        ((Player) playerIn).setLocation(event.getLocation());

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

    @SuppressWarnings("unchecked")
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int targetDimension, boolean conqueredEnd) {
        this.scoreboard = ((Player) playerIn).getScoreboard();

        // Phase 1 - check if the player is allowed to respawn in same dimension
        Dimension prevDimension = (Dimension) this.mcServer.worldServerForDimension(playerIn.dimension).provider;
        if (prevDimension.allowsPlayerRespawns()) {
            targetDimension = playerIn.dimension;
        } else {
            WorldServer requestedWorld = this.mcServer.worldServerForDimension(targetDimension);
            if (requestedWorld == null) {
                targetDimension = 0;
            } else if (!requestedWorld.provider.canRespawnHere()) {
                targetDimension = ((IMixinWorldProvider) requestedWorld.provider).getRespawnDimension(playerIn);
            }
        }

        // Phase 2 - handle return from End
        if (conqueredEnd) {
            WorldServer exitWorld = this.mcServer.worldServerForDimension(targetDimension);
            Optional<Location> exit = Optional.absent();
            // use bed if available, otherwise default spawn
            if (((Player) playerIn).getData(RespawnLocationData.class).isPresent()) {
                exit = Optional.of(((Player) playerIn).getData(RespawnLocationData.class).get().getRespawnLocation());
            }
            if (!exit.isPresent() || ((net.minecraft.world.World) exit.get().getExtent()).provider.getDimensionId() != 0) {
                Vector3i pos = ((World) exitWorld).getProperties().getSpawnPosition();
                exit = Optional.of(new Location((World) exitWorld, new Vector3d(pos.getX(), pos.getY(), pos.getZ())));
            }
        }

        // Phase 3 - remove current player from current dimension
        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerForPlayer().getEntityTracker().untrackEntity(playerIn);
        playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);

        // Phase 4 - handle bed spawn
        BlockPos bedSpawnChunkCoords = ((IMixinEntityPlayer) playerIn).getBedLocation(targetDimension);
        boolean spawnForced = ((IMixinEntityPlayer) playerIn).isSpawnForced(targetDimension);
        playerIn.dimension = targetDimension;
        WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
        // make sure to update reference for bed spawn logic
        playerIn.setWorld(worldserver);
        playerIn.theItemInWorldManager.setWorld(worldserver);
        playerIn.playerConqueredTheEnd = false;
        BlockPos bedSpawnLocation;
        boolean isBedSpawn = false;
        World toWorld = ((World) playerIn.worldObj);
        Location location = null;

        if (bedSpawnChunkCoords != null) { // if player has a bed
            bedSpawnLocation =
                    EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(playerIn.dimension), bedSpawnChunkCoords, spawnForced);

            if (bedSpawnLocation != null) {
                isBedSpawn = true;
                playerIn.setLocationAndAngles(bedSpawnLocation.getX() + 0.5F,
                        bedSpawnLocation.getY() + 0.1F, bedSpawnLocation.getZ() + 0.5F, 0.0F, 0.0F);
                playerIn.setSpawnPoint(bedSpawnChunkCoords, spawnForced);
                location =
                        new Location(toWorld, new Vector3d(bedSpawnChunkCoords.getX() + 0.5, bedSpawnChunkCoords.getY(),
                                bedSpawnChunkCoords.getZ() + 0.5));
            } else { // bed was not found (broken)
                playerIn.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0));
                // use the spawnpoint as location
                location =
                        new Location(toWorld, new Vector3d(toWorld.getProperties().getSpawnPosition().getX(), toWorld.getProperties()
                                .getSpawnPosition().getY(), toWorld.getProperties().getSpawnPosition().getZ()));
            }
        }

        if (location == null) {
            // use the world spawnpoint as default location
            location = new Location(toWorld, toWorld.getProperties().getSpawnPosition());
        }

        if (!conqueredEnd) { // don't reset player if returning from end
            ((IMixinEntityPlayerMP) playerIn).reset();
        }

        final PlayerRespawnEvent event = SpongeImplEventFactory.createPlayerRespawn(Sponge.getGame(), (Player) playerIn, isBedSpawn, location);
        Sponge.getGame().getEventManager().post(event);
        location = event.getNewRespawnLocation();

        final WorldServer targetWorld = (WorldServer) location.getExtent();
        targetWorld.theChunkProviderServer.loadChunk((int) location.getX() >> 4, (int) location.getZ() >> 4);

        // Phase 5 - Respawn player in new world
        // Support vanilla clients logging into custom dimensions
        int dimension = DimensionManager.getClientDimensionToSend(targetWorld.provider.getDimensionId(), targetWorld, playerIn);
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            DimensionManager.sendDimensionRegistration(targetWorld, playerIn, dimension);
        }

        playerIn.playerNetServerHandler.sendPacket(new S07PacketRespawn(dimension, targetWorld.getDifficulty(), targetWorld
                .getWorldInfo().getTerrainType(), playerIn.theItemInWorldManager.getGameType()));
        playerIn.setWorld(targetWorld); // in case plugin changed it
        playerIn.theItemInWorldManager.setWorld(targetWorld);
        playerIn.isDead = false;
        playerIn.playerNetServerHandler.setPlayerLocation(location.getX(), location.getY(), location.getZ(),
                playerIn.rotationYaw, playerIn.rotationPitch);

        // Keep players out of blocks
        while (!targetWorld.getCollidingBoundingBoxes(playerIn, playerIn.getEntityBoundingBox()).isEmpty()) {
            playerIn.setPosition(playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ);
        }

        playerIn.setSneaking(false);
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

        ((Player) playerIn).setScoreboard(this.scoreboard);
        ((Player) this.uuidToPlayerMap.get(playerIn.getUniqueID())).setScoreboard(this.scoreboard);

        return playerIn;
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
