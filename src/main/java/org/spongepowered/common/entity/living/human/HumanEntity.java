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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.accessor.entity.player.PlayerEntityAccessor;
import org.spongepowered.common.accessor.network.play.server.SPlayerListItemPacketAccessor;
import org.spongepowered.common.accessor.network.play.server.SSpawnPlayerPacketAccessor;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/*
 * Notes
 *
 * The label above the player's head is always visible unless the human is in
 * a team with invisible labels set to true. Could we leverage this at all?
 *
 * Hostile mobs don't attack the human, should this be default behaviour?
 */
@SuppressWarnings("EntityConstructor") // MCDev needs an update for 1.14
public class HumanEntity extends CreatureEntity implements TeamMember, IRangedAttackMob {

    // According to http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape
    // you can access this data once per minute, lets cache for 2 minutes
    private static final LoadingCache<UUID, PropertyMap> PROPERTIES_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build((uuid) -> SpongeCommon.getServer().getMinecraftSessionService()
                    .fillProfileProperties(new GameProfile(uuid, ""), true)
                    .getProperties());

    // A queue of packets waiting to send to players tracking this human
    private final Map<UUID, List<Stream<IPacket<?>>>> playerPacketMap = Maps.newHashMap();

    private GameProfile fakeProfile;
    @Nullable private UUID skinUuid;
    private boolean aiDisabled = false, leftHanded = false;

    public HumanEntity(final EntityType<? extends HumanEntity> type, final World worldIn) {
        super(type, worldIn);
        this.fakeProfile = new GameProfile(this.entityUniqueID, "");
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
        this.dataManager.register(LivingEntityAccessor.accessor$getLivingFlags(), Byte.valueOf((byte)0));
        this.dataManager.register(LivingEntityAccessor.accessor$getPotionEffects(), Integer.valueOf(0));
        this.dataManager.register(LivingEntityAccessor.accessor$getHideParticles(), Boolean.valueOf(false));
        this.dataManager.register(LivingEntityAccessor.accessor$getArrowCountInEntity(), Integer.valueOf(0));
        this.dataManager.register(LivingEntityAccessor.accessor$getHealth(), Float.valueOf(1.0F));
        // EntityPlayer
        this.dataManager.register(PlayerEntityAccessor.accessor$getAbsorption(), Float.valueOf(0.0F));
        this.dataManager.register(PlayerEntityAccessor.accessor$getPlayerScore(), Integer.valueOf(0));
        this.dataManager.register(PlayerEntityAccessor.accessor$getPlayerModelFlag(), Byte.valueOf((byte)0));
        this.dataManager.register(PlayerEntityAccessor.accessor$getMainHand(), Byte.valueOf((byte)1));
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
        return TextComponent.of(this.fakeProfile.getName());
    }

    @Override
    public Team getTeam() {
        return this.world.getScoreboard().getPlayersTeam(this.fakeProfile.getName());
    }

    @Override
    public void setCustomName(@Nullable ITextComponent name) {
        // TODO - figure out whether these restrictions still exist
//        if (name.length() > 16) {
//            // Vanilla restriction
//            name = name.substring(0, 16);
//        }
        final ITextComponent customName = this.getCustomName();
        if (customName == null && name == null || customName != null && customName.equals(name)) {
            return;
        }
        super.setCustomName(name);
        this.renameProfile(name);
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
    }

    @Override
    public void readAdditional(final CompoundNBT tagCompund) {
        super.readAdditional(tagCompund);
        final String skinUuidString = ((DataCompoundHolder) this).data$getSpongeData().getString(Constants.Entity.Human.SKIN_UUID_KEY);
        if (!skinUuidString.isEmpty()) {
            this.updateFakeProfileWithSkin(UUID.fromString(skinUuidString));
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeData();
        if (this.skinUuid != null) {
            spongeData.putString(Constants.Entity.Human.SKIN_UUID_KEY, this.skinUuid.toString());
        } else {
            spongeData.remove(Constants.Entity.Human.SKIN_UUID_KEY);
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
        this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
        double motionX = 0.0D;
        double motionY = 0.1D;
        double motionZ = 0.0D;
        if (cause != null) {
            motionX = -MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float) Math.PI / 180.0F) * 0.1F;
            motionZ = -MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float) Math.PI / 180.0F) * 0.1F;
        }
        this.setMotion(motionX, motionY, motionZ);
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

    private void renameProfile(final ITextComponent newName) {
        final PropertyMap props = this.fakeProfile.getProperties();
        // TODO - is this right? calling getString on the name?
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName.getString());
        this.fakeProfile.getProperties().putAll(props);
    }

    private boolean updateFakeProfileWithSkin(final UUID skin) {
        final PropertyMap properties = PROPERTIES_CACHE.get(skin);
        if (properties == null || properties.isEmpty()) {
            return false;
        }
        this.fakeProfile.getProperties().replaceValues(ProfileProperty.TEXTURES, properties.get(ProfileProperty.TEXTURES));
        this.skinUuid = skin;
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
                    .delayTicks(delay)
                    .plugin(SpongeCommon.getPlugin())
                    .build());
        }
    }

    public boolean setSkinUuid(final UUID skin) {
        if (!SpongeCommon.getServer().isServerInOnlineMode()) {
            // Skins only work when online-mode = true
            return false;
        }
        if (skin.equals(this.skinUuid)) {
            return true;
        }
        if (!this.updateFakeProfileWithSkin(skin)) {
            return false;
        }
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
        return true;
    }

    public Property getSkinProperty() {
        return this.fakeProfile.getProperties().get(ProfileProperty.TEXTURES).iterator().next();
    }

    public void setSkinProperty(Property property) {
        this.fakeProfile.getProperties().replaceValues(ProfileProperty.TEXTURES, Collections.singletonList(property));
    }

    @Nullable
    public UUID getSkinUuid() {
        return this.skinUuid;
    }

    public DataTransactionResult removeSkin() {
        if (this.skinUuid == null) {
            return DataTransactionResult.successNoData();
        }
        this.fakeProfile.getProperties().removeAll(ProfileProperty.TEXTURES);
        // TODO - figure out this new bit of mess.
//        final Immutable<?> oldValue = new ImmutableSpongeValue<>(Keys.SKIN_UNIQUE_ID, this.skinUuid);
//        this.skinUuid = null;
//        if (this.isAliveAndInWorld()) {
//            this.respawnOnClient();
//        }
//        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).replace(oldValue).build();
        return DataTransactionResult.failNoData();
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
    public void onRemovedFrom(final ServerPlayerEntity player) {
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
        if (player == null) {
            List<Stream<IPacket<?>>> queue = this.playerPacketMap.get(null);
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(null, queue);
            }
            queue.add(Stream.of(packets));
        } else {
            List<Stream<IPacket<?>>> queue = this.playerPacketMap.get(player.getUniqueID());
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(player.getUniqueID(), queue);
            }
            queue.add(Stream.of(packets));
        }
    }

    /**
     * (Internal) Pops the packets off the queue for the given player.
     *
     * @param player The player to get packets for (or null for all players)
     * @return An array of packets to send in a single tick
     */
    public Stream<IPacket<?>> popQueuedPackets(@Nullable final ServerPlayerEntity player) {
        final List<Stream<IPacket<?>>> queue = this.playerPacketMap.get(player == null ? null : player.getUniqueID());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

    @Override
    public void attackEntityWithRangedAttack(final LivingEntity target, final float distanceFactor) {
        // Borrowed from Skeleton
        // TODO Figure out how to API this out
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
