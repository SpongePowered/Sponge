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
package org.spongepowered.common.entity.living.human;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/*
 * Notes
 *
 * The label above the player's head is always visible unless the human is in
 * a team with invisible labels set to true. Could we leverage this at all?
 *
 * Hostile mobs don't attack the human, should this be default behaviour?
 */
public class EntityHuman extends EntityCreature implements TeamMember, IRangedAttackMob {

    // According to http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape
    // you can access this data once per minute, lets cache for 2 minutes
    private static final LoadingCache<UUID, PropertyMap> PROPERTIES_CACHE = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, PropertyMap>() {

                @Override
                public PropertyMap load(UUID uuid) throws Exception {
                    return SpongeImpl.getServer().getMinecraftSessionService().fillProfileProperties(new GameProfile(uuid, ""), true)
                            .getProperties();
                }
            });

    // A queue of packets waiting to send to players tracking this human
    private final Map<UUID, List<Packet<?>[]>> playerPacketMap = Maps.newHashMap();

    private GameProfile fakeProfile;
    @Nullable private UUID skinUuid;
    private boolean aiDisabled = false;

    public EntityHuman(World worldIn) {
        super(worldIn);
        this.fakeProfile = new GameProfile(this.entityUniqueID, "");
        this.setSize(0.6F, 1.8F);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(EntityLivingBase.HAND_STATES, Byte.valueOf((byte)0));
        this.dataManager.register(EntityLivingBase.POTION_EFFECTS, Integer.valueOf(0));
        this.dataManager.register(EntityLivingBase.HIDE_PARTICLES, Boolean.valueOf(false));
        this.dataManager.register(EntityLivingBase.ARROW_COUNT_IN_ENTITY, Integer.valueOf(0));
        this.dataManager.register(EntityLivingBase.HEALTH, Float.valueOf(1.0F));
        this.dataManager.register(EntityPlayer.ABSORPTION, 0.0F);
        this.dataManager.register(EntityPlayer.PLAYER_SCORE, 0);
        this.dataManager.register(EntityPlayer.MAIN_HAND, (byte) 1);
        this.dataManager.register(EntityPlayer.PLAYER_MODEL_FLAG, (byte) 0xFF);
    }

    @Override
    public boolean isLeftHanded() {
        return this.dataManager.get(EntityPlayer.MAIN_HAND) == 0;
    }

    @Override
    public boolean isAIDisabled() {
        return this.aiDisabled;
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.fakeProfile.getName());
    }

    @Override
    public Team getTeam() {
        return this.worldObj.getScoreboard().getPlayersTeam(this.fakeProfile.getName());
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
        if (this.isAliveAndInWorld()) {
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
        NBTTagCompound spongeData = ((IMixinEntity) this).getSpongeData();
        if (this.skinUuid != null) {
            spongeData.setString("skinUuid", this.skinUuid.toString());
        } else {
            spongeData.removeTag("skinUuid");
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.updateArmSwingProgress();
    }

    @Override
    public int getMaxInPortalTime() {
        return 80;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_PLAYER_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_PLAYER_SPLASH;
    }

    @Override
    public int getPortalCooldown() {
        return 10;
    }

    @Override
    public void onDeath(@Nullable DamageSource cause) {
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
    protected SoundEvent getHurtSound() {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    public double getYOffset() {
        return -0.35D;
    }

    @Override
    public float getAIMoveSpeed() {
        return (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
    }

    @Override
    protected SoundEvent getFallSound(int p_184588_1_) {
        return p_184588_1_ > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }
    @Override
    public float getEyeHeight() {
        return 1.62f;
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataManager().get(EntityPlayer.ABSORPTION);
    }

    @Override
    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }
        this.getDataManager().set(EntityPlayer.ABSORPTION, amount);
    }

    @Override
    protected float updateDistance(float p_110146_1_, float p_110146_2_) {
        float retValue = super.updateDistance(p_110146_1_, p_110146_2_);
        // Make the body rotation follow head rotation
        this.rotationYaw = this.rotationYawHead;
        return retValue;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        super.attackEntityAsMob(entityIn);
        this.swingArm(EnumHand.MAIN_HAND);
        float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;

        if (entityIn instanceof EntityLivingBase) {
            f += EnchantmentHelper.getModifierForCreature(this.getHeldItem(EnumHand.MAIN_HAND), ((EntityLivingBase) entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0) {
                entityIn.addVelocity((double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F), 0.1D,
                        (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                entityIn.setFire(j * 4);
            }

            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    private void renameProfile(String newName) {
        PropertyMap props = this.fakeProfile.getProperties();
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName);
        this.fakeProfile.getProperties().putAll(props);
    }

    private boolean updateFakeProfileWithSkin(UUID skin) {
        PropertyMap properties = PROPERTIES_CACHE.getUnchecked(skin);
        if (properties.isEmpty()) {
            return false;
        }
        this.fakeProfile.getProperties().replaceValues("textures", properties.get("textures"));
        this.skinUuid = skin;
        return true;
    }

    public void removeFromTabListDelayed(@Nullable EntityPlayerMP player, SPacketPlayerListItem removePacket) {
        int delay = SpongeImpl.getGlobalConfig().getConfig().getEntity().getHumanPlayerListRemoveDelay();
        Runnable removeTask = () -> this.pushPackets(player, removePacket);
        if (delay == 0) {
            removeTask.run();
        } else {
            SpongeImpl.getGame().getScheduler().createTaskBuilder().execute(removeTask).delayTicks(delay).submit(SpongeImpl.getPlugin());
        }
    }

    public boolean setSkinUuid(UUID skin) {
        if (!SpongeImpl.getServer().isServerInOnlineMode()) {
            // Skins only work when online-mode = true
            return false;
        }
        if (skin.equals(this.skinUuid)) {
            return true;
        }
        if (!updateFakeProfileWithSkin(skin)) {
            return false;
        }
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
        return true;
    }

    @Nullable
    public UUID getSkinUuid() {
        return this.skinUuid;
    }

    public DataTransactionResult removeSkin() {
        if (this.skinUuid == null) {
            return DataTransactionResult.successNoData();
        }
        this.fakeProfile.getProperties().removeAll("textures");
        ImmutableValue<?> oldValue = new ImmutableSpongeValue<>(Keys.SKIN_UNIQUE_ID, this.skinUuid);
        this.skinUuid = null;
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).replace(oldValue).build();
    }

    private boolean isAliveAndInWorld() {
        return this.worldObj.getEntityByID(this.getEntityId()) == this && !this.isDead;
    }

    private void respawnOnClient() {
        this.pushPackets(new SPacketDestroyEntities(this.getEntityId()), this.createPlayerListPacket(SPacketPlayerListItem.Action.ADD_PLAYER));
        this.pushPackets(this.createSpawnPacket());
        removeFromTabListDelayed(null, this.createPlayerListPacket(SPacketPlayerListItem.Action.REMOVE_PLAYER));
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
        player.connection.sendPacket(this.createPlayerListPacket(SPacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    /**
     * Creates a {@link SPacketSpawnPlayer} packet.
     *
     * Copied directly from the constructor of the packet, because that can't be
     * used as we're not an EntityPlayer.
     *
     * @return A new spawn packet
     */
    public SPacketSpawnPlayer createSpawnPacket() {
        SPacketSpawnPlayer packet = new SPacketSpawnPlayer();
        packet.entityId = this.getEntityId();
        packet.uniqueId = this.fakeProfile.getId();
        packet.x = this.posX;
        packet.y = this.posY;
        packet.z = this.posZ;
        packet.yaw = (byte) ((int) (this.rotationYaw * 256.0F / 360.0F));
        packet.pitch = (byte) ((int) (this.rotationPitch * 256.0F / 360.0F));
        packet.watcher = this.getDataManager();
        return packet;
    }

    /**
     * Creates a {@link SPacketPlayerListItem} packet with the given action.
     *
     * @param action The action to apply on the tab list
     * @return A new tab list packet
     */
    @SuppressWarnings("unchecked")
    public SPacketPlayerListItem createPlayerListPacket(SPacketPlayerListItem.Action action) {
        SPacketPlayerListItem packet = new SPacketPlayerListItem(action);
        packet.players.add(packet.new AddPlayerData(this.fakeProfile, 0, GameType.NOT_SET, this.getDisplayName()));
        return packet;
    }

    /**
     * Push the given packets to all players tracking this human.
     *
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(Packet<?>... packets) {
        this.pushPackets(null, packets); // null = all players
    }

    /**
     * Push the given packets to the given player (who must be tracking this
     * human).
     *
     * @param player The player tracking this human
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(@Nullable EntityPlayerMP player, Packet<?>... packets) {
        if (player == null) {
            List<Packet<?>[]> queue = this.playerPacketMap.get(null);
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(null, queue);
            }
            queue.add(packets);
        } else {
            List<Packet<?>[]> queue = this.playerPacketMap.get(player.getUniqueID());
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(player.getUniqueID(), queue);
            }
            queue.add(packets);
        }
    }

    /**
     * (Internal) Pops the packets off the queue for the given player.
     *
     * @param player The player to get packets for (or null for all players)
     * @return An array of packets to send in a single tick
     */
    public Packet<?>[] popQueuedPackets(@Nullable EntityPlayerMP player) {
        List<Packet<?>[]> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
        // Borrowed from Skeleton
        // TODO Figure out how to API this out
        final EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.worldObj, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entitytippedarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        entitytippedarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.worldObj.getDifficulty().getDifficultyId() * 4));
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
        int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
        entitytippedarrow.setDamage((double)(p_82196_2_ * 2.0F) + this.rand.nextGaussian() * 0.25D + (double)((float)this.worldObj.getDifficulty().getDifficultyId() * 0.11F));

        if (i > 0) {
            entitytippedarrow.setDamage(entitytippedarrow.getDamage() + (double)i * 0.5D + 0.5D);
        }

        if (j > 0) {
            entitytippedarrow.setKnockbackStrength(j);
        }

        final ItemStack itemstack = this.getHeldItem(EnumHand.OFF_HAND);

        if (itemstack != null && itemstack.getItem() == Items.TIPPED_ARROW) {
            entitytippedarrow.setPotionEffect(itemstack);
        }

        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.worldObj.spawnEntityInWorld(entitytippedarrow);
    }
}
