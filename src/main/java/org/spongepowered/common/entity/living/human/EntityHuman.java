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
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket.AddPlayerData;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;
import org.spongepowered.common.mixin.core.entity.player.EntityPlayerAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketPlayerListItemAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketSpawnPlayerAccessor;

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
public class EntityHuman extends CreatureEntity implements TeamMember, IRangedAttackMob {

    // According to http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape
    // you can access this data once per minute, lets cache for 2 minutes
    private static final LoadingCache<UUID, PropertyMap> PROPERTIES_CACHE = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, PropertyMap>() {

                @Override
                public PropertyMap load(final UUID uuid) throws Exception {
                    return SpongeImpl.getServer().getMinecraftSessionService().fillProfileProperties(new GameProfile(uuid, ""), true)
                            .getProperties();
                }
            });

    // A queue of packets waiting to send to players tracking this human
    private final Map<UUID, List<IPacket<?>[]>> playerPacketMap = Maps.newHashMap();

    private GameProfile fakeProfile;
    @Nullable private UUID skinUuid;
    private boolean aiDisabled = false, leftHanded = false;

    public EntityHuman(final World worldIn) {
        super(worldIn);
        this.fakeProfile = new GameProfile(this.entityUniqueID, "");
        this.setSize(0.6F, 1.8F);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
    }

    @Override
    protected void registerData() {
        // EntityLivingBase
        this.dataManager.register(EntityLivingBaseAccessor.accessor$getHandStatesParameter(), Byte.valueOf((byte)0));
        this.dataManager.register(EntityLivingBaseAccessor.accessor$getPotionEffectsParameter(), Integer.valueOf(0));
        this.dataManager.register(EntityLivingBaseAccessor.accessor$getHideParticlesParameter(), Boolean.valueOf(false));
        this.dataManager.register(EntityLivingBaseAccessor.accessor$getArrowCountInEntityParameter(), Integer.valueOf(0));
        this.dataManager.register(EntityLivingBaseAccessor.accessor$getHealthParameter(), Float.valueOf(1.0F));
        // EntityPlayer
        this.dataManager.register(EntityPlayerAccessor.accessor$getAbsorptionParameter(), Float.valueOf(0.0F));
        this.dataManager.register(EntityPlayerAccessor.accessor$getPlayerScoreParameter(), Integer.valueOf(0));
        this.dataManager.register(EntityPlayerAccessor.accessor$getPlayerModelFlagParameter(), Byte.valueOf((byte)0));
        this.dataManager.register(EntityPlayerAccessor.accessor$getMainHandParameter(), Byte.valueOf((byte)1));
    }

    @Override
    public boolean isLeftHanded() {
        return this.leftHanded;
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
        return this.world.getScoreboard().getPlayersTeam(this.fakeProfile.getName());
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
    public void readAdditional(final CompoundNBT tagCompund) {
        super.readAdditional(tagCompund);
        final String skinUuidString = ((DataCompoundHolder) this).data$getSpongeCompound().getString("skinUuid");
        if (!skinUuidString.isEmpty()) {
            this.updateFakeProfileWithSkin(UUID.fromString(skinUuidString));
        }
    }

    @Override
    public void writeEntityToNBT(final CompoundNBT tagCompound) {
        super.writeEntityToNBT(tagCompound);
        final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
        if (this.skinUuid != null) {
            spongeData.putString("skinUuid", this.skinUuid.toString());
        } else {
            spongeData.remove("skinUuid");
        }
    }

    @Override
    public void livingTick() {
        super.livingTick();
        this.updateArmSwingProgress();
    }

    @Override
    public void setNoAI(final boolean disable) {
        this.aiDisabled = disable;
    }

    @Override
    public void setLeftHanded(final boolean leftHanded) {
        this.leftHanded = leftHanded;
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
    public void onDeath(@Nullable final DamageSource cause) {
        super.onDeath(cause);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.1D;
        if (cause != null) {
            this.motionX = -MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float) Math.PI / 180.0F) * 0.1F;
            this.motionZ = -MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float) Math.PI / 180.0F) * 0.1F;
        } else {
            this.motionX = this.motionZ = 0.0D;
        }
    }

    @Override
    protected SoundEvent getHurtSound(final DamageSource source) {
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
        return (float) this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
    }

    @Override
    protected SoundEvent getFallSound(final int height) {
        return height > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL;
    }
    @Override
    public float getEyeHeight() {
        return 1.62f;
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataManager().get(EntityPlayerAccessor.accessor$getAbsorptionParameter());
    }

    @Override
    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }
        this.getDataManager().set(EntityPlayerAccessor.accessor$getAbsorptionParameter(), amount);
    }

    @Override
    protected float updateDistance(final float p_110146_1_, final float p_110146_2_) {
        final float retValue = super.updateDistance(p_110146_1_, p_110146_2_);
        // Make the body rotation follow head rotation
        this.rotationYaw = this.rotationYawHead;
        return retValue;
    }

    @Override
    public boolean attackEntityAsMob(final Entity entityIn) {
        super.attackEntityAsMob(entityIn);
        this.swingArm(Hand.MAIN_HAND);
        float f = (float) this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
        int i = 0;

        if (entityIn instanceof LivingEntity) {
            f += EnchantmentHelper.getModifierForCreature(this.getHeldItem(Hand.MAIN_HAND), ((LivingEntity) entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        final boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0) {
                entityIn.addVelocity(-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * i * 0.5F, 0.1D,
                        MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * i * 0.5F);
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            final int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                entityIn.setFire(j * 4);
            }

            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    private void renameProfile(final String newName) {
        final PropertyMap props = this.fakeProfile.getProperties();
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName);
        this.fakeProfile.getProperties().putAll(props);
    }

    private boolean updateFakeProfileWithSkin(final UUID skin) {
        final PropertyMap properties = PROPERTIES_CACHE.getUnchecked(skin);
        if (properties.isEmpty()) {
            return false;
        }
        this.fakeProfile.getProperties().replaceValues("textures", properties.get("textures"));
        this.skinUuid = skin;
        return true;
    }

    public void removeFromTabListDelayed(@Nullable final ServerPlayerEntity player, final SPlayerListItemPacket removePacket) {
        final int delay = SpongeImpl.getGlobalConfigAdapter().getConfig().getEntity().getHumanPlayerListRemoveDelay();
        final Runnable removeTask = () -> this.pushPackets(player, removePacket);
        if (delay == 0) {
            removeTask.run();
        } else {
            SpongeImpl.getGame().getScheduler().createTaskBuilder()
                    .execute(removeTask)
                    .delayTicks(delay)
                    .submit(SpongeImpl.getPlugin());
        }
    }

    public boolean setSkinUuid(final UUID skin) {
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
        final ImmutableValue<?> oldValue = new ImmutableSpongeValue<>(Keys.SKIN_UNIQUE_ID, this.skinUuid);
        this.skinUuid = null;
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).replace(oldValue).build();
    }

    private boolean isAliveAndInWorld() {
        return this.world.getEntityByID(this.getEntityId()) == this && !this.removed;
    }

    private void respawnOnClient() {
        this.pushPackets(new SDestroyEntitiesPacket(this.getEntityId()), this.createPlayerListPacket(SPlayerListItemPacket.Action.ADD_PLAYER));
        this.pushPackets(this.createSpawnPacket());
        removeFromTabListDelayed(null, this.createPlayerListPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER));
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
    public void onRemovedFrom(final ServerPlayerEntity player) {
        this.playerPacketMap.remove(player.getUniqueID());
        player.connection.sendPacket(this.createPlayerListPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER));
    }

    /**
     * Creates a {@link SPacketSpawnPlayer} packet.
     *
     * Copied directly from the constructor of the packet, because that can't be
     * used as we're not an EntityPlayer.
     *
     * @return A new spawn packet
     */
    @SuppressWarnings("ConstantConditions")
    public SSpawnPlayerPacket createSpawnPacket() {
        final SSpawnPlayerPacket packet = new SSpawnPlayerPacket();
        final SPacketSpawnPlayerAccessor accessor = (SPacketSpawnPlayerAccessor) packet;
        accessor.accessor$setentityId(this.getEntityId());
        accessor.accessor$setuniqueId(this.fakeProfile.getId());
        accessor.accessor$setx(this.posX);
        accessor.accessor$sety(this.posY);
        accessor.accessor$setZ(this.posZ);
        accessor.accessor$setYaw((byte) ((int) (this.rotationYaw * 256.0F / 360.0F)));
        accessor.accessor$setPitch((byte) ((int) (this.rotationPitch * 256.0F / 360.0F)));
        accessor.accessor$setWatcher(this.getDataManager());
        return packet;
    }

    /**
     * Creates a {@link SPacketPlayerListItem} packet with the given action.
     *
     * @param action The action to apply on the tab list
     * @return A new tab list packet
     */
    @SuppressWarnings("ConstantConditions")
    public SPlayerListItemPacket createPlayerListPacket(final SPlayerListItemPacket.Action action) {
        final SPlayerListItemPacket packet = new SPlayerListItemPacket(action);
        ((SPacketPlayerListItemAccessor) packet).accessor$getPlayerDatas()
            .add(packet.new AddPlayerData(this.fakeProfile, 0, GameType.NOT_SET, this.getDisplayName()));
        return packet;
    }

    /**
     * Push the given packets to all players tracking this human.
     *
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(final IPacket<?>... packets) {
        this.pushPackets(null, packets); // null = all players
    }

    /**
     * Push the given packets to the given player (who must be tracking this
     * human).
     *
     * @param player The player tracking this human
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(@Nullable final ServerPlayerEntity player, final IPacket<?>... packets) {
        if (player == null) {
            List<IPacket<?>[]> queue = this.playerPacketMap.get(null);
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(null, queue);
            }
            queue.add(packets);
        } else {
            List<IPacket<?>[]> queue = this.playerPacketMap.get(player.getUniqueID());
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
    public IPacket<?>[] popQueuedPackets(@Nullable final ServerPlayerEntity player) {
        final List<IPacket<?>[]> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

    @Override
    public void attackEntityWithRangedAttack(final LivingEntity target, final float distanceFactor) {
        // Borrowed from Skeleton
        // TODO Figure out how to API this out
        final ArrowEntity entitytippedarrow = new ArrowEntity(this.world, this);
        final double d0 = target.posX - this.posX;
        final double d1 = target.getBoundingBox().minY + target.height / 3.0F - entitytippedarrow.posY;
        final double d2 = target.posZ - this.posZ;
        final double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        entitytippedarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 14 - this.world.getDifficulty().getId() * 4);
        // These names are wrong
        final int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
        final int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this);
        entitytippedarrow.setDamage(distanceFactor * 2.0F + this.rand.nextGaussian() * 0.25D + this.world.getDifficulty().getId() * 0.11F);

        if (i > 0) {
            entitytippedarrow.setDamage(entitytippedarrow.getDamage() + i * 0.5D + 0.5D);
        }

        if (j > 0) {
            entitytippedarrow.setKnockbackStrength(j);
        }

        final ItemStack itemstack = this.getHeldItem(Hand.OFF_HAND);

        if (itemstack.getItem() == Items.TIPPED_ARROW) {
            entitytippedarrow.setPotionEffect(itemstack);
        }

        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity0(entitytippedarrow);
    }

    @Override
    public void setSwingingArms(final boolean var1) {
        // TODO 1.12-pre2 Can we support this
    }
}
