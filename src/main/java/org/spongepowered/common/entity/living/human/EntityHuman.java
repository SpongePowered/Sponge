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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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
import net.minecraft.world.World;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.player.tab.TabListEntryAdapter;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.util.TextureUtil;

import java.util.List;
import java.util.UUID;

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

    // A queue of packets waiting to send to players tracking this human
    private final ListMultimap<UUID, Packet<?>[]> playerPacketMap = ArrayListMultimap.create();

    private GameProfile profile;
    private boolean aiDisabled = false;

    public EntityHuman(World worldIn) {
        super(worldIn);
        this.profile = new GameProfile(this.entityUniqueID, "");
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

    public GameProfile getProfile() {
        return this.profile;
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
        return Text.of(this.profile.getName());
    }

    @Override
    public Team getTeam() {
        return this.world.getScoreboard().getPlayersTeam(this.profile.getName());
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
    public void readEntityFromNBT(NBTTagCompound rootCompound) {
        super.readEntityFromNBT(rootCompound);
        final NBTTagCompound spongeCompound = ((IMixinEntity) this).getSpongeData();

        @Nullable ProfileProperty textures = TextureUtil.read(spongeCompound);
        if (textures != null) {
            TextureUtil.toPropertyMap(this.profile.getProperties(), textures);
        } else if (spongeCompound.hasKey(NbtDataUtil.HUMANOID_TEXTURES_UNIQUE_ID, NbtDataUtil.TAG_STRING)) {
            String skinUniqueId = ((IMixinEntity) this).getSpongeData().getString(NbtDataUtil.HUMANOID_TEXTURES_UNIQUE_ID);
            if (!skinUniqueId.isEmpty()) {
                TextureUtil.migrateLegacyTextureUniqueId(this.profile, UUID.fromString(skinUniqueId));
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound rootCompound) {
        super.writeEntityToNBT(rootCompound);
        final NBTTagCompound spongeCompound = ((IMixinEntity) this).getSpongeData();

        TextureUtil.write(spongeCompound, TextureUtil.fromProfile(this.profile));
        spongeCompound.removeTag(NbtDataUtil.HUMANOID_TEXTURES_UNIQUE_ID); // Remove legacy StringTag
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
        PropertyMap properties = this.profile.getProperties();
        this.profile = new GameProfile(this.profile.getId(), newName);
        this.profile.getProperties().putAll(properties);
    }

    public void removeFromTabListDelayed(@Nullable EntityPlayerMP player, SPacketPlayerListItem removePacket) {
        int delay = SpongeImpl.getGlobalConfig().getConfig().getEntity().getHumanPlayerListRemoveDelay();
        Runnable removeTask = () -> this.pushPackets(player, removePacket);
        if (delay == 0) {
            removeTask.run();
        } else {
            SpongeImpl.getGame().getScheduler().createTaskBuilder()
                    .execute(removeTask)
                    .delayTicks(delay)
                    .submit(SpongeImpl.getPlugin());
        }
    }

    private boolean isAliveAndInWorld() {
        return this.world.getEntityByID(this.getEntityId()) == this && !this.isDead;
    }

    public void respawnOnClient() {
        this.pushPackets(new SPacketDestroyEntities(this.getEntityId()), TabListEntryAdapter.human(this, null, SPacketPlayerListItem.Action.ADD_PLAYER));
        this.pushPackets(this.createSpawnPacket());
        removeFromTabListDelayed(null, TabListEntryAdapter.human(this, null, SPacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    /**
     * Can the fake profile be removed from the tab list immediately (i.e. as
     * soon as the human has spawned).
     *
     * @return Whether it can be removed with 0 ticks delay
     */
    public boolean canRemoveFromListImmediately() {
        return !this.profile.getProperties().containsKey("textures");
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
        this.playerPacketMap.removeAll(player.getUniqueID());
        player.connection.sendPacket(TabListEntryAdapter.human(this, player, SPacketPlayerListItem.Action.REMOVE_PLAYER));
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
        packet.uniqueId = this.profile.getId();
        packet.x = this.posX;
        packet.y = this.posY;
        packet.z = this.posZ;
        packet.yaw = (byte) ((int) (this.rotationYaw * 256.0F / 360.0F));
        packet.pitch = (byte) ((int) (this.rotationPitch * 256.0F / 360.0F));
        packet.watcher = this.getDataManager();
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
            this.playerPacketMap.put(null, packets);
        } else {
            this.playerPacketMap.put(player.getUniqueID(), packets);
        }
    }

    /**
     * (Internal) Pops the packets off the queue for the given player.
     *
     * @param player The player to get packets for (or null for all players)
     * @return An array of packets to send in a single tick
     */
    @Nullable
    public Packet<?>[] popQueuedPackets(@Nullable EntityPlayerMP player) {
        List<Packet<?>[]> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
        // Borrowed from Skeleton
        // TODO Figure out how to API this out
        final EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entitytippedarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        entitytippedarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));
        // These names are wrong
        int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
        int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this);
        entitytippedarrow.setDamage((double)(p_82196_2_ * 2.0F) + this.rand.nextGaussian() * 0.25D + (double)((float)this.world.getDifficulty().getDifficultyId() * 0.11F));

        if (i > 0) {
            entitytippedarrow.setDamage(entitytippedarrow.getDamage() + (double)i * 0.5D + 0.5D);
        }

        if (j > 0) {
            entitytippedarrow.setKnockbackStrength(j);
        }

        final ItemStack itemstack = this.getHeldItem(EnumHand.OFF_HAND);

        if (itemstack.getItem() == Items.TIPPED_ARROW) {
            entitytippedarrow.setPotionEffect(itemstack);
        }

        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entitytippedarrow);
    }
}
