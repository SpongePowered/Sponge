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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.kyori.adventure.text.Component;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.accessor.entity.player.PlayerEntityAccessor;
import org.spongepowered.common.accessor.network.play.server.SPlayerListItemPacketAccessor;
import org.spongepowered.common.accessor.network.play.server.SSpawnPlayerPacketAccessor;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class HumanEntity extends CreatureEntity implements TeamMember, IRangedAttackMob {

    // A queue of packets waiting to send to players tracking this human
    private final Map<UUID, List<Stream<IPacket<?>>>> playerPacketMap = new HashMap<>();

    private GameProfile fakeProfile;
    private boolean aiDisabled = false, leftHanded = false;

    public HumanEntity(final EntityType<? extends HumanEntity> type, final World worldIn) {
        super(type, worldIn);
        this.fakeProfile = new GameProfile(this.entityUniqueID, "");
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();

        // PlayerEntity
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23F);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.LUCK);
    }

    @Override
    protected void registerData() {
        // LivingEntity
        this.dataManager.register(LivingEntityAccessor.accessor$getLivingFlags(), (byte) 0);
        this.dataManager.register(LivingEntityAccessor.accessor$getHealth(), 1.0F);
        this.dataManager.register(LivingEntityAccessor.accessor$getPotionEffects(), 0);
        this.dataManager.register(LivingEntityAccessor.accessor$getHideParticles(), Boolean.FALSE);
        this.dataManager.register(LivingEntityAccessor.accessor$getArrowCountInEntity(), 0);
        this.dataManager.register(LivingEntityAccessor.accessor$getBeeStringCount(), 0);
        this.dataManager.register(LivingEntityAccessor.accessor$getBedPosition(), Optional.empty());

        // PlayerEntity
        this.dataManager.register(PlayerEntityAccessor.accessor$getAbsorption(), 0.0F);
        this.dataManager.register(PlayerEntityAccessor.accessor$getPlayerScore(), 0);
        this.dataManager.register(PlayerEntityAccessor.accessor$getPlayerModelFlag(), (byte) 0);
        this.dataManager.register(PlayerEntityAccessor.accessor$getMainHand(), (byte) 1);
        this.dataManager.register(PlayerEntityAccessor.accessor$getLeftShoulderEntity(), new CompoundNBT());
        this.dataManager.register(PlayerEntityAccessor.accessor$getRightShoulderEntity(), new CompoundNBT());
    }

    @Override
    protected void registerGoals() {
        // Just some defaults, so they do something by default. Plugins can fully cancel this and make them do whatever
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(3, new LookAtGoal(this, MobEntity.class, 8.0F));
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
    public Component getTeamRepresentation() {
        return Component.text(this.fakeProfile.getName());
    }

    @Override
    public Team getTeam() {
        return this.world.getScoreboard().getPlayersTeam(this.fakeProfile.getName());
    }

    @Override
    public void setCustomName(@Nullable final ITextComponent name) {
        final ITextComponent customName = this.getCustomName();
        if (customName == null && name == null || customName != null && customName.equals(name)) {
            return;
        }
        super.setCustomName(name);
        this.setProfileName(name);
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
    }

    @Override
    public void readAdditional(final CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("profile")) {
            this.fakeProfile = NBTUtil.readGameProfile(compound.getCompound("profile"));
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        final CompoundNBT profileCompound = new CompoundNBT();
        NBTUtil.writeGameProfile(profileCompound, this.fakeProfile);
        compound.put("profile", profileCompound);
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
    public void setAggroed(boolean hasAggro) {
        // NOOP, we handle the arm swing manually...
    }

    @Override
    public int getMaxInPortalTime() {
        return 1;
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
        this.recenterBoundingBox();
        if (cause != null) {
            this.setMotion(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * ((float)Math.PI / 180F)) * 0.1F, 0.1F,
                    -MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * ((float)Math.PI / 180F)) * 0.1F);
        } else {
            this.setMotion(0.0D, 0.1D, 0.0D);
        }

        this.extinguish();
        this.setFlag(0, false);
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
        return Constants.Entity.Player.PLAYER_Y_OFFSET;
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
    public float getAbsorptionAmount() {
        return this.getDataManager().get(PlayerEntityAccessor.accessor$getAbsorption());
    }

    @Override
    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }
        this.getDataManager().set(PlayerEntityAccessor.accessor$getAbsorption(), amount);
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
        this.swingArm(this.getActiveHand());
        float f = (float) this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
        int i = 0;

        f += EnchantmentHelper.getModifierForCreature(this.getHeldItem(Hand.MAIN_HAND), this.getCreatureAttribute());
        i += EnchantmentHelper.getKnockbackModifier(this);

        final boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag) {
            if (i > 0) {
                entityIn.addVelocity(-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * i * 0.5F, 0.1D,
                        MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * i * 0.5F);
                this.setMotion(this.getMotion().mul(0.6, 1.0, 0.6));
            }

            final int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0) {
                entityIn.setFire(j * 4);
            }

            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    private void setProfileName(@Nullable final ITextComponent newName) {
        final PropertyMap props = this.fakeProfile.getProperties();
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName == null ? "" : newName.getString());
        this.fakeProfile.getProperties().putAll(props);
    }

    public boolean getOrLoadSkin(final UUID minecraftAccount) {
        GameProfile gameProfile = SpongeCommon.getServer().getPlayerProfileCache().getProfileByUUID(minecraftAccount);
        if (gameProfile == null) {
            gameProfile =
                    SpongeCommon.getServer().getMinecraftSessionService().fillProfileProperties(new GameProfile(minecraftAccount, ""), true);
            if (gameProfile == null) {
                return false;
            }

            // TODO Should we put profile cache entries with UUIDs that don't have their names?

            SpongeCommon.getServer().getPlayerProfileCache().addEntry(gameProfile);
        }

        this.fakeProfile.getProperties().replaceValues(ProfileProperty.TEXTURES, gameProfile.getProperties().get(ProfileProperty.TEXTURES));
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }

        return true;
    }

    public boolean getOrLoadSkin(final String minecraftAccount) {
        Objects.requireNonNull(minecraftAccount);
        GameProfile gameProfile = SpongeCommon.getServer().getPlayerProfileCache().getGameProfileForUsername(minecraftAccount);
        if (gameProfile == null) {
            return false;
        }

        if (gameProfile.getProperties().isEmpty()) {
            gameProfile = SpongeCommon.getServer().getMinecraftSessionService().fillProfileProperties(gameProfile, true);
            SpongeCommon.getServer().getPlayerProfileCache().addEntry(gameProfile);
        }

        this.fakeProfile.getProperties().clear();
        this.fakeProfile.getProperties().putAll(gameProfile.getProperties());
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }

        return true;
    }

    public void removeFromTabListDelayed(@Nullable final ServerPlayerEntity player, final SPlayerListItemPacket removePacket) {
        final int delay = SpongeGameConfigs.getForWorld(this.world).get().getEntity().getHumanPlayerListRemoveDelay();
        final Runnable removeTask = () -> this.pushPackets(player, removePacket);
        if (delay == 0) {
            removeTask.run();
        } else {
            Sponge.getServer().getScheduler().submit(Task.builder()
                    .execute(removeTask)
                    .delay(new SpongeTicks(delay))
                    .plugin(Launch.getInstance().getCommonPlugin())
                    .build());
        }
    }

    public Property getSkinProperty() {
        return this.fakeProfile.getProperties().get(ProfileProperty.TEXTURES).iterator().next();
    }

    public void setSkinProperty(final Property property) {
        this.fakeProfile.getProperties().replaceValues(ProfileProperty.TEXTURES, Collections.singletonList(property));

        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
    }

    private boolean isAliveAndInWorld() {
        return this.world.getEntityByID(this.getEntityId()) == this && !this.removed;
    }

    private void respawnOnClient() {
        this.pushPackets(new SDestroyEntitiesPacket(this.getEntityId()), this.createPlayerListPacket(SPlayerListItemPacket.Action.ADD_PLAYER));
        this.pushPackets(this.createSpawnPacket());
        this.removeFromTabListDelayed(null, this.createPlayerListPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER));
    }

    /**
     * Can the fake profile be removed from the tab list immediately (i.e. as
     * soon as the human has spawned).
     *
     * @return Whether it can be removed with 0 ticks delay
     */
    public boolean canRemoveFromListImmediately() {
        return !this.fakeProfile.getProperties().containsKey(ProfileProperty.TEXTURES);
    }

    /**
     * Called when a player stops tracking this human.
     *
     * Removes the player from the packet queue and sends them a REMOVE_PLAYER
     * tab list packet to make sure the human is not on it.
     *
     * @param player The player that has stopped tracking this human
     */
    public void untrackFrom(final ServerPlayerEntity player) {
        this.playerPacketMap.remove(player.getUniqueID());
        player.connection.sendPacket(this.createPlayerListPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER));
    }

    /**
     * Creates a {@link SSpawnPlayerPacket} packet.
     *
     * Copied directly from the constructor of the packet, because that can't be
     * used as we're not a PlayerEntity.
     *
     * @return A new spawn packet
     */
    @SuppressWarnings("ConstantConditions")
    public SSpawnPlayerPacket createSpawnPacket() {
        final SSpawnPlayerPacket packet = new SSpawnPlayerPacket();
        final SSpawnPlayerPacketAccessor accessor = (SSpawnPlayerPacketAccessor) packet;
        accessor.accessor$setEntityId(this.getEntityId());
        accessor.accessor$setUniqueId(this.fakeProfile.getId());
        accessor.accessor$setX(this.getPosX());
        accessor.accessor$setY(this.getPosY());
        accessor.accessor$setZ(this.getPosZ());
        accessor.accessor$setYaw((byte) ((int) (this.rotationYaw * 256.0F / 360.0F)));
        accessor.accessor$setPitch((byte) ((int) (this.rotationPitch * 256.0F / 360.0F)));
        return packet;
    }

    /**
     * Creates a {@link SPlayerListItemPacket} packet with the given action.
     *
     * @param action The action to apply on the tab list
     * @return A new tab list packet
     */
    @SuppressWarnings("ConstantConditions")
    public SPlayerListItemPacket createPlayerListPacket(final SPlayerListItemPacket.Action action) {
        final SPlayerListItemPacket packet = new SPlayerListItemPacket(action);
        ((SPlayerListItemPacketAccessor) packet).accessor$getPlayers()
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
        final List<Stream<IPacket<?>>> queue;
        if (player == null) {
            queue = this.playerPacketMap.computeIfAbsent(null, k -> new ArrayList<>());
        } else {
            queue = this.playerPacketMap.computeIfAbsent(player.getUniqueID(), k -> new ArrayList<>());
        }
        queue.add(Stream.of(packets));
    }

    /**
     * (Internal) Pops the packets off the queue for the given player.
     *
     * @param player The player to get packets for (or null for all players)
     * @return An array of packets to send in a single tick
     */
    public Stream<IPacket<?>> popQueuedPackets(@Nullable final ServerPlayerEntity player) {
        final List<Stream<IPacket<?>>> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? Stream.empty() : queue.remove(0);
    }

    @Override
    public void attackEntityWithRangedAttack(final LivingEntity target, final float distanceFactor) {
        // Borrowed from Skeleton
        final ArrowEntity entitytippedarrow = new ArrowEntity(this.world, this);
        final double d0 = target.getPosX() - this.getPosX();
        final double d1 = target.getBoundingBox().minY + target.getHeight() / 3.0F - entitytippedarrow.getPosY();
        final double d2 = target.getPosZ() - this.getPosZ();
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
        this.world.addEntity(entitytippedarrow);
    }
}
