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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.entity.SkinData;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.manipulator.entity.SpongeSkinData;
import org.spongepowered.common.interfaces.IMixinEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*
 * Notes
 *
 * The label above the player's head is always visible unless the human is in
 * a team with invisible labels set to true. Could we leverage this at all?
 *
 * Hostile mobs don't attack the human, should this be default behaviour?
 */

public class HumanEntity extends EntityCreature /* implements Human (MixinHuman) */{

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

    // A queue of packets waiting to send to players tracking this human
    private final Map<UUID, List<Packet[]>> playerPacketMap = new HashMap<UUID, List<Packet[]>>();

    private GameProfile fakeProfile;
    private UUID skinUuid;

    public HumanEntity(World worldIn) {
        super(worldIn);
        this.fakeProfile = new GameProfile(this.entityUniqueID, "");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.1D);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
        this.dataWatcher.addObject(17, Float.valueOf(0.0F));
        this.dataWatcher.addObject(18, Integer.valueOf(0));
        // Enables all skin features
        this.dataWatcher.addObject(10, Byte.valueOf((byte) 0xFF));
    }

    @Override
    protected boolean canDespawn() {
        return false; // Humans shouldn't despawn naturally
        // Could this be configurable? (persistenceRequired)
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

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        String skinUuidString = ((IMixinEntity) this).getSpongeData().getString("skinUuid");
        if (!skinUuidString.isEmpty()) {
            this.updateFakeProfileWithSkin(UUID.fromString(skinUuidString));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        if (this.skinUuid != null) {
            ((IMixinEntity) this).getSpongeData().setString("skinUuid", this.skinUuid.toString());
        }
    }

    @Override
    public int getMaxInPortalTime() {
        return 80;
    }

    @Override
    protected String getSwimSound() {
        return "game.player.swim";
    }

    @Override
    protected String getSplashSound() {
        return "game.player.swim.splash";
    }

    @Override
    public int getPortalCooldown() {
        return 10;
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.1D;
        if (cause != null) {
            this.motionX = (double) (-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float) Math.PI / 180.0F) * 0.1F);
            this.motionZ = (double) (-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float) Math.PI / 180.0F) * 0.1F);
        } else {
            this.motionX = this.motionZ = 0.0D;
        }
    }

    @Override
    protected String getHurtSound() {
        return "game.player.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "game.player.die";
    }

    @Override
    public double getYOffset() {
        return -0.35D;
    }

    @Override
    public float getAIMoveSpeed() {
        return (float) this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }

    @Override
    protected String getFallSoundString(int damageValue) {
        return damageValue > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small";
    }

    @Override
    public float getEyeHeight() {
        return 1.62f;
    }

    @Override
    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }
        this.getDataWatcher().updateObject(17, Float.valueOf(amount));
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataWatcher().getWatchableObjectFloat(17);
    }

    private void renameProfile(String newName) {
        PropertyMap props = this.fakeProfile.getProperties();
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName);
        this.fakeProfile.getProperties().putAll(props);
    }

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

    private boolean isInWorld() {
        return this.worldObj.getEntityByID(this.getEntityId()) == this && !this.isDead;
    }

    private void respawnOnClient() {
        this.pushPackets(new S13PacketDestroyEntities(this.getEntityId()), this.createPlayerListPacket(S38PacketPlayerListItem.Action.ADD_PLAYER));
        this.pushPackets(this.createSpawnPacket());
        this.pushPackets(this.createPlayerListPacket(S38PacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    /**
     * Can the fake profile be removed from the tab list immediately (i.e. as
     * soon as the human has spawned).
     *
     * @return Whether it can be removed with 0 ticks delay
     */
    public boolean canRemoveFromListImmediately() {
        return !this.fakeProfile.getProperties().containsKey("textures");
    }

    /**
     * Called when a player stops tracking this human.
     *
     * Removes the player from the packet queue and sends them a REMOVE_PLAYER
     * tab list packet to make sure the human is not on it.
     *
     * @param player The player that has stopped tracking this human
     */
    public void onRemovedFrom(EntityPlayerMP player) {
        this.playerPacketMap.remove(player.getUniqueID());
        player.playerNetServerHandler.sendPacket(this.createPlayerListPacket(S38PacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    /**
     * Creates a {@link S0CPacketSpawnPlayer} packet.
     *
     * Copied directly from the constructor of the packet, because that can't be
     * used as we're not an EntityPlayer.
     *
     * @return A new spawn packet
     */
    public S0CPacketSpawnPlayer createSpawnPacket() {
        S0CPacketSpawnPlayer packet = new S0CPacketSpawnPlayer();
        packet.entityId = this.getEntityId();
        packet.playerId = this.fakeProfile.getId();
        packet.x = MathHelper.floor_double(this.posX * 32.0D);
        packet.y = MathHelper.floor_double(this.posY * 32.0D);
        packet.z = MathHelper.floor_double(this.posZ * 32.0D);
        packet.yaw = (byte) ((int) (this.rotationYaw * 256.0F / 360.0F));
        packet.pitch = (byte) ((int) (this.rotationPitch * 256.0F / 360.0F));
        ItemStack itemstack = (ItemStack) ((ArmorEquipable) this).getItemInHand().orNull();
        packet.currentItem = itemstack == null ? 0 : Item.getIdFromItem(itemstack.getItem());
        packet.watcher = this.getDataWatcher();
        return packet;
    }

    /**
     * Creates a {@link S38PacketPlayerListItem} packet with the given action.
     *
     * @param action The action to apply on the tab list
     * @return A new tab list packet
     */
    @SuppressWarnings("unchecked")
    public S38PacketPlayerListItem createPlayerListPacket(S38PacketPlayerListItem.Action action) {
        S38PacketPlayerListItem packet = new S38PacketPlayerListItem(action);
        packet.field_179769_b.add(packet.new AddPlayerData(this.fakeProfile, 0, WorldSettings.GameType.NOT_SET, this.getDisplayName()));
        return packet;
    }

    /**
     * Push the given packets to all players tracking this human.
     *
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(Packet... packets) {
        this.pushPackets(null, packets); // null = all players
    }

    /**
     * Push the given packets to the given player (who must be tracking this
     * human).
     *
     * @param player The player tracking this human
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(EntityPlayerMP player, Packet... packets) {
        List<Packet[]> queue = this.playerPacketMap.get(player);
        if (queue == null) {
            this.playerPacketMap.put(player == null ? null : player.getUniqueID(), queue = new ArrayList<Packet[]>());
        }
        queue.add(packets);
    }

    /**
     * (Internal) Pops the packets off the queue for the given player.
     *
     * @param player The player to get packets for (or null for all players)
     * @return An array of packets to send in a single tick
     */
    public Packet[] popQueuedPackets(EntityPlayerMP player) {
        List<Packet[]> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

}
