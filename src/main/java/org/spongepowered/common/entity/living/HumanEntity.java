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

package org.spongepowered.common.entity.living;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.entity.SkinData;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.manipulator.entity.SpongeSkinData;
import org.spongepowered.common.interfaces.IMixinEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*
 * Notes
 *
 * The label above the player's head is always visible unless the human is in
 * a team with invisible labels set to true. Could we leverage this at all?
 *
 * Hostile mobs attack the human by default, should this be default behaviour?
 */

public class HumanEntity extends EntityPlayer {

    // According to http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape
    // you can access this data once per minute, lets cache for 2 minutes
    private static final LoadingCache<UUID, PropertyMap> propertiesCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, PropertyMap>() {

                @Override
                public PropertyMap load(UUID uuid) throws Exception {
                    return MinecraftServer.getServer().getMinecraftSessionService().fillProfileProperties(new GameProfile(uuid, ""), true)
                            .getProperties();
                }
            });

    private GameProfile fakeProfile;

    private final Map<UUID, List<Packet[]>> playerPacketMap = new HashMap<UUID, List<Packet[]>>();

    public HumanEntity(World worldIn) {
        super(worldIn, HumanEntity.createRandomProfile());
        this.fakeProfile = new GameProfile(this.entityUniqueID, "");
        // Enables all skin features
        this.getDataWatcher().updateObject(10, (byte) 0xFF);
    }

    private static GameProfile createRandomProfile() {
        // This GameProfile should not be used anywhere, it's here for the super
        // constructor.
        // The profile to send to the client is fakeProfile.
        UUID uuid = MathHelper.getRandomUuid(new Random());
        return new GameProfile(uuid, "Human.{" + uuid.toString() + "}");
    }

    private void renameProfile(String newName) {
        PropertyMap props = this.fakeProfile.getProperties();
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName);
        this.fakeProfile.getProperties().putAll(props);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public String getCommandSenderName() {
        // Change back to Entity.getCommandSenderName, don't use the username
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        } else {
            return StatCollector.translateToLocal("entity.human.name");
        }
    }

    @Override
    public void setCustomNameTag(String name) {
        if (name.length() > 16) {
            // Vanilla restriction
            name = name.substring(0, 16);
        }
        if (this.getCustomNameTag().equals(name)) {
            return;
        }
        super.setCustomNameTag(name);
        this.renameProfile(name);
        if (this.isInWorld()) {
            this.respawnOnClient();
        }
    }

    private UUID skinUuid;

    private boolean updateFakeProfileWithSkin(UUID skin) {
        PropertyMap properties = propertiesCache.getUnchecked(skin);
        if (properties.isEmpty()) {
            return false;
        }
        this.fakeProfile.getProperties().replaceValues("textures", properties.get("textures"));
        this.skinUuid = skin;
        return true;
    }

    public DataTransactionResult setSkinData(SkinData skin) {
        if (!MinecraftServer.getServer().isServerInOnlineMode()) {
            // Skins only work when online-mode = true
            return DataTransactionBuilder.fail(skin);
        }
        if (skin.getValue().equals(this.skinUuid)) {
            return DataTransactionBuilder.successNoData();
        }
        if (!updateFakeProfileWithSkin(skin.getValue())) {
            return DataTransactionBuilder.fail(skin);
        }
        if (this.isInWorld()) {
            this.respawnOnClient();
        }
        return DataTransactionBuilder.successNoData();
    }

    public Optional<SkinData> getSkinData() {
        if (this.skinUuid != null) {
            return Optional.<SkinData>of(new SpongeSkinData(this.skinUuid));
        }
        return Optional.absent();
    }

    public SkinData createSkinData() {
        return this.getSkinData().or(new SpongeSkinData(this.entityUniqueID));
    }

    public boolean removeSkin() {
        if (this.skinUuid == null) {
            return false;
        }
        this.fakeProfile.getProperties().removeAll("textures");
        this.skinUuid = null;
        if (this.isInWorld()) {
            this.respawnOnClient();
        }
        return true;
    }

    public Optional<SkinData> fillSkinData(SkinData manipulator) {
        if (this.skinUuid == null) {
            return Optional.absent();
        }
        return Optional.of(manipulator.setValue(this.skinUuid));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        // The EntityPlayer.readEntityFromNBT changes the entityUniqueID to
        // that of the GameProfile (our fake one in the constructor) we need to
        // change it back
        super.readEntityFromNBT(tagCompund);
        if (tagCompund.hasKey("UUIDMost", 4) && tagCompund.hasKey("UUIDLeast", 4)) {
            this.entityUniqueID = new UUID(tagCompund.getLong("UUIDMost"), tagCompund.getLong("UUIDLeast"));
        } else if (tagCompund.hasKey("UUID", 8)) {
            this.entityUniqueID = UUID.fromString(tagCompund.getString("UUID"));
        }

        // Read the skin UUID if available
        String skinUuidString = ((IMixinEntity) this).getSpongeData().getString("skinUuid");
        if (!skinUuidString.isEmpty()) {
            this.updateFakeProfileWithSkin(UUID.fromString(skinUuidString));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        // Write the skin UUID if it's set
        if (this.skinUuid != null) {
            ((IMixinEntity) this).getSpongeData().setString("skinUuid", this.skinUuid.toString());
        }
    }

    @Override
    public Team getTeam() {
        // Change back to using the entity's UUID rather than the name
        return this.worldObj.getScoreboard().getPlayersTeam(this.getUniqueID().toString());
    }

    private boolean isInWorld() {
        return this.worldObj.getEntityByID(this.getEntityId()) == this && !this.isDead;
    }

    private void respawnOnClient() {
        this.pushPackets(new S13PacketDestroyEntities(this.getEntityId()), this.createPlayerListPacket(S38PacketPlayerListItem.Action.ADD_PLAYER));
        this.pushPackets(new S0CPacketSpawnPlayer(this));
        this.pushPackets(this.createPlayerListPacket(S38PacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    @SuppressWarnings("unchecked")
    public S38PacketPlayerListItem createPlayerListPacket(S38PacketPlayerListItem.Action action) {
        S38PacketPlayerListItem packet = new S38PacketPlayerListItem(action);
        packet.field_179769_b.add(packet.new AddPlayerData(this.fakeProfile, 0, WorldSettings.GameType.NOT_SET, this.getDisplayName()));
        return packet;
    }

    public void pushPackets(Packet... packets) {
        this.pushPackets(null, packets); // null = all players
    }

    public void pushPackets(EntityPlayerMP player, Packet... packets) {
        List<Packet[]> queue = this.playerPacketMap.get(player);
        if (queue == null) {
            this.playerPacketMap.put(player == null ? null : player.getUniqueID(), queue = new ArrayList<Packet[]>());
        }
        queue.add(packets);
    }

    public Packet[] popQueuedPackets(EntityPlayerMP player) {
        List<Packet[]> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

    public boolean canRemoveFromListImmediately() {
        return !this.fakeProfile.getProperties().containsKey("textures");
    }

    public void onRemovedFrom(EntityPlayerMP player) {
        this.playerPacketMap.remove(player.getUniqueID());
        player.playerNetServerHandler.sendPacket(this.createPlayerListPacket(S38PacketPlayerListItem.Action.REMOVE_PLAYER));
    }

}
