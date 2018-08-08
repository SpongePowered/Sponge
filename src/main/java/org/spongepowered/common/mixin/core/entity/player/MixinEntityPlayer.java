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
package org.spongepowered.common.mixin.core.entity.player;

import com.flowpowered.math.vector.Vector3d;
import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.ModifierFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.interfaces.ITargetedLocation;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements IMixinEntityPlayer, ITargetedLocation {

    private static final String WORLD_PLAY_SOUND_AT =
            "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V";
    private static final String PLAYER_COLLIDE_ENTITY = "Lnet/minecraft/entity/Entity;onCollideWithPlayer(Lnet/minecraft/entity/player/EntityPlayer;)V";

    @Shadow public Container inventoryContainer;
    @Shadow public Container openContainer;
    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public PlayerCapabilities capabilities;
    @Shadow public InventoryPlayer inventory;
    @Shadow private BlockPos spawnPos;
    @Shadow private BlockPos bedLocation;
    @Shadow protected FoodStats foodStats;
    @Shadow public InventoryEnderChest enderChest;

    @Shadow public abstract boolean canOpen(LockCode code);
    @Shadow public abstract boolean isPlayerSleeping();
    @Shadow public abstract boolean isSpectator();
    @Shadow public abstract int xpBarCap();
    @Shadow public abstract float getCooledAttackStrength(float adjustTicks);
    @Shadow public abstract float getAIMoveSpeed();
    @Shadow public abstract void onCriticalHit(net.minecraft.entity.Entity entityHit);
    @Shadow public abstract void onEnchantmentCritical(net.minecraft.entity.Entity entityHit); // onEnchantmentCritical
    @Shadow public abstract void addExhaustion(float p_71020_1_);
    @Shadow public abstract void addStat(@Nullable StatBase stat, int amount);
    @Shadow public abstract void addStat(StatBase stat);
    @Shadow public abstract void resetCooldown();
    @Shadow public abstract void spawnSweepParticles(); //spawnSweepParticles()
    @Shadow public abstract void takeStat(StatBase stat);
    @Shadow protected abstract void destroyVanishingCursedItems(); // Filter vanishing curse enchanted items
    @Shadow public abstract void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn);
    @Shadow public abstract EntityItem dropItem(boolean dropAll);
    @Shadow public abstract FoodStats getFoodStats();
    @Shadow public abstract GameProfile getGameProfile();
    @Shadow public abstract Scoreboard getWorldScoreboard();
    @Shadow public abstract String getName();
    @Shadow @Nullable public abstract Team getTeam();
    @Shadow public abstract void addExperienceLevel(int levels);
    @Shadow public abstract void addScore(int scoreIn);
    @Shadow public abstract CooldownTracker shadow$getCooldownTracker();

    @Shadow protected abstract void spawnShoulderEntities();
    @Shadow public abstract boolean isCreative();

    private boolean affectsSpawning = true;
    private UUID collidingEntityUuid = null;
    private Vector3d targetedLocation;
    private boolean dontRecalculateExperience;
    private boolean shouldRestoreInventory = false;
    protected final boolean isFake = SpongeImplHooks.isFakePlayer((EntityPlayer) (Object) this);

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lcom/mojang/authlib/GameProfile;)V", at = @At("RETURN"))
    public void construct(World worldIn, GameProfile gameProfileIn, CallbackInfo ci) {
        this.targetedLocation = VecHelper.toVector3d(worldIn.getSpawnPoint());
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    public void onGetDisplayName(CallbackInfoReturnable<ITextComponent> ci) {
        ci.setReturnValue(LegacyTexts.parseComponent((TextComponentString) ci.getReturnValue(), SpongeTexts.COLOR_CHAR));
    }

    @Override
    public int getExperienceSinceLevel() {
        return this.experienceTotal - ExperienceHolderUtils.xpAtLevel(this.experienceLevel);
    }

    @Override
    public void setExperienceSinceLevel(int experience) {
        this.experienceTotal = ExperienceHolderUtils.xpAtLevel(this.experienceLevel) + experience;
        this.experience = (float) experience / this.xpBarCap();
    }

    /**
     * {@link EntityPlayer#addExperienceLevel(int)} doesn't update the total
     * experience. This recalculates it for plugins to properly make use of it.
     */
    private void recalculateTotalExperience() {
        if (!this.dontRecalculateExperience) {
            int newExperienceInLevel = (int) (this.experience * this.xpBarCap());
            this.experienceTotal = ExperienceHolderUtils.xpAtLevel(this.experienceLevel) + newExperienceInLevel;
            this.experience = (float) newExperienceInLevel / this.xpBarCap();
        }
    }

    @Inject(method = "addExperienceLevel", at = @At("RETURN"))
    private void onAddExperienceLevels(int levels, CallbackInfo ci) {
        recalculateTotalExperience();
    }

    @Inject(method = "onEnchant", at = @At("RETURN"))
    private void onEnchantChangeExperienceLevels(ItemStack item, int levels, CallbackInfo ci) {
        recalculateTotalExperience();
    }

    /**
     * @author JBYoshi - May 17, 2017
     * @reason This makes the experience updating more accurate and disables
     * the totalExperience recalculation above for this method, which would
     * otherwise have weird intermediate states.
     */
    @Overwrite
    public void addExperience(int amount) {
        this.addScore(amount);
        int i = Integer.MAX_VALUE - this.experienceTotal;

        if (amount > i) {
            amount = i;
        }

        // Sponge start - completely rewritten for integer-based calculations
        // this.experience += (float)amount / (float)this.xpBarCap();

        // for (this.experienceTotal += amount; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap()) {
            // this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
            // this.addExperienceLevel(1);
        // }

        int finalExperience = this.experienceTotal + amount;
        int finalLevel = ExperienceHolderUtils.getLevelForExp(finalExperience);
        if (finalLevel != this.experienceLevel) {
            this.dontRecalculateExperience = true;
            try {
                addExperienceLevel(finalLevel - this.experienceLevel);
            } finally {
                this.dontRecalculateExperience = false;
            }
        }
        this.experience = (float) (finalExperience - ExperienceHolderUtils.xpAtLevel(finalLevel))
                / ExperienceHolderUtils.getExpBetweenLevels(finalLevel);
        this.experienceTotal = finalExperience;
        this.experienceLevel = finalLevel;
        // Sponge end
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void recalculateXpOnLoad(NBTTagCompound compound, CallbackInfo ci) {
        // Fix the mistakes of /xp commands past.
        recalculateTotalExperience();
    }

    public boolean isFlying() {
        return this.capabilities.isFlying;
    }

    public void setFlying(boolean flying) {
        this.capabilities.isFlying = flying;
    }

    /**
     * @author blood - May 12th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Overwrite
    @Override
    public void onDeath(DamageSource cause) {
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        Optional<DestructEntityEvent.Death>
                event = SpongeCommonEventFactory.callDestructEntityEventDeath((EntityPlayer) (Object) this, cause, isMainThread);
        if (event.map(Cancellable::isCancelled).orElse(true)) {
            return;
        }
        super.onDeath(cause);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.10000000149011612D;

        if (this.getName().equals("Notch")) {
            this.dropItem(new ItemStack(Items.APPLE, 1), true, false);
        }

        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            this.destroyVanishingCursedItems();
            this.inventory.dropAllItems();
        }

        if (cause != null) {
            this.motionX = (double) (-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
            this.motionZ = (double) (-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
        } else {
            this.motionX = this.motionZ = 0.0D;
        }

        this.addStat(StatList.DEATHS);
        this.takeStat(StatList.TIME_SINCE_DEATH);
        this.extinguish();
        this.setFlag(0, false);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isPlayerSleeping()Z"))
    public boolean onIsPlayerSleeping(EntityPlayer self) {
        if (self.isPlayerSleeping()) {
            if (!((IMixinWorld) this.world).isFake()) {
                Sponge.getCauseStackManager().pushCause(this);
                SpongeImpl.postEvent(SpongeEventFactory.
                        createSleepingEventTick(Sponge.getCauseStackManager().getCurrentCause(),
                                                this.getWorld().createSnapshot(VecHelper.toVector3i(this.bedLocation)), this));
                Sponge.getCauseStackManager().popCause();
            }
            return true;
        }
        return false;
    }

    /**
     * @author gabizou - January 4th, 2016
     *
     * This prevents sounds from being sent to the server by players who are vanish.
     */
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = WORLD_PLAY_SOUND_AT))
    public void playSound(World world, EntityPlayer player, double d1, double d2, double d3, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (!this.isVanished()) {
            this.world.playSound(player, d1, d2, d3, sound, category, volume, pitch);
        }
    }

    @Override
    public boolean affectsSpawning() {
        return this.affectsSpawning && !this.isSpectator();
    }

    @Override
    public void setAffectsSpawning(boolean affectsSpawning) {
        this.affectsSpawning = affectsSpawning;
    }

    @Override
    public Vector3d getTargetedLocation() {
        return this.targetedLocation;
    }

    @Override
    public void setTargetedLocation(@Nullable Vector3d vec) {
        this.targetedLocation = vec != null ? vec : VecHelper.toVector3d(this.world.getSpawnPoint());
        if (!((Object) this instanceof EntityPlayerMP)) {
            this.world.setSpawnPoint(VecHelper.toBlockPos(this.targetedLocation));
        }
    }

    /**
     * @author gabizou - June 13th, 2016
     * @reason Reverts the method to flow through our systems, Forge patches
     * this to throw an ItemTossEvent, but we'll be throwing it regardless in
     * SpongeForge's handling.
     *
     * @param itemStackIn
     * @param unused
     * @return
     */
    @Overwrite
    @Nullable
    public EntityItem dropItem(ItemStack itemStackIn, boolean unused) {
        return this.dropItem(itemStackIn, false, false);
    }

    /**
     * @author gabizou - June 4th, 2016
     * @reason When a player drops an item, all methods flow through here instead of {@link Entity#dropItem(Item, int)}
     * because of the idea of {@code dropAround} and {@code traceItem}.
     *
     * @param droppedItem The item to drop
     * @param dropAround If true, the item is dropped around the player, otherwise thrown in front of the player
     * @param traceItem If true, the item is thrown as the player
     * @return The entity, if spawned correctly and not captured in any way
     */
    @Nullable
    @Overwrite
    public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        if (droppedItem.isEmpty()) {
            return null;
        }
        // Sponge Start - redirect to our handling to capture and throw events.
        if (!((IMixinWorld) this.world).isFake()) {
            return EntityUtil.playerDropItem(this, droppedItem, dropAround, traceItem);
        }
        // Sponge end
        double d0 = this.posY - 0.30000001192092896D + (double) this.getEyeHeight();
        EntityItem entityitem = new EntityItem(this.world, this.posX, d0, this.posZ, droppedItem);
        entityitem.setPickupDelay(40);

        if (traceItem) {
            entityitem.setThrower(this.getName());
        }

        if (dropAround) {
            float f = this.rand.nextFloat() * 0.5F;
            float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
            entityitem.motionX = (double) (-MathHelper.sin(f1) * f);
            entityitem.motionZ = (double) (MathHelper.cos(f1) * f);
            entityitem.motionY = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            entityitem.motionX =
                (double) (-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f2);
            entityitem.motionZ =
                (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f2);
            entityitem.motionY = (double) (-MathHelper.sin(this.rotationPitch * 0.017453292F) * f2 + 0.1F);
            float f3 = this.rand.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * this.rand.nextFloat();
            entityitem.motionX += Math.cos((double) f3) * (double) f2;
            entityitem.motionY += (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            entityitem.motionZ += Math.sin((double) f3) * (double) f2;
        }

        ItemStack itemstack = this.dropItemAndGetStack(entityitem);

        if (traceItem) {
            if (itemstack != null && !itemstack.isEmpty()) { // Sponge - add null check
                this.addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.getCount());
            }

            this.addStat(StatList.DROP);
        }

        return entityitem;
    }

    /**
     * @author gabizou - June 4th, 2016
     * @reason Overwrites the original logic to simply pass through to the
     * PhaseTracker.
     *
     * @param entity The entity item to spawn
     * @return The itemstack
     */
    @SuppressWarnings("OverwriteModifiers") // This is a MinecraftDev thing, since forge elevates the modifier to public
    @Overwrite
    @Nullable
    public ItemStack dropItemAndGetStack(EntityItem entity) {
        this.world.spawnEntity(entity);
        return entity.getItem();
    }

    @Redirect(method = "collideWithPlayer", at = @At(value = "INVOKE", target = PLAYER_COLLIDE_ENTITY)) // collideWithPlayer
    private void onPlayerCollideEntity(net.minecraft.entity.Entity entity, EntityPlayer player) {
        this.collidingEntityUuid = entity.getUniqueID();
        entity.onCollideWithPlayer(player);
        this.collidingEntityUuid = null;
    }


    @Override
    public UUID getCollidingEntityUuid() {
        return this.collidingEntityUuid;
    }

    /**
     * @author dualspiral - October 7th, 2016
     *
     * @reason When setting {@link SpongeHealthData#setHealth(double)} to 0, {@link #onDeath(DamageSource)} was
     * not being called. This check bypasses some of the checks that prevent the superclass method being called
     * when the {@link DamageSourceRegistryModule#IGNORED_DAMAGE_SOURCE} is being used.
     */
    @Inject(method = "attackEntityFrom", cancellable = true, at = @At(value = "HEAD"))
    private void onAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source == DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE) {
            // Taken from the original method, wake the player up if they are about to die.
            if (this.isPlayerSleeping() && !this.world.isRemote) {
                this.wakeUpPlayer(true, true, false);
            }

            // We just throw it to the superclass method so that we can potentially get the
            // onDeath method.
            cir.setReturnValue(super.attackEntityFrom(source, amount));
        }
    }

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 - This enitre method was rewritten
     *
     *
     * @reason Rewrites the attackTargetEntityWithCurrentItem to throw an {@link AttackEntityEvent} prior
     * to the ensuing {@link DamageEntityEvent}. This should cover all cases where players are
     * attacking entities and those entities override {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}
     * and effectively bypass our damage event hooks.
     *
     * LVT Rename Table:
     * float f        | damage               |
     * float f1       | enchantmentDamage    |
     * float f2       | attackStrength       |
     * boolean flag   | isStrongAttack       |
     * boolean flag1  | isSprintingAttack    |
     * boolean flag2  | isCriticalAttack     | Whether critical particles will spawn and of course, multiply the output damage
     * boolean flag3  | isSweapingAttack     | Whether the player is sweaping an attack and will deal AoE damage
     * int i          | knockbackModifier    | The knockback modifier, must be set from the event after it has been thrown
     * float f4       | targetOriginalHealth | This is initially set as the entity original health
     * boolean flag4  | litEntityOnFire      | This is an internal flag to where if the attack failed, the entity is no longer set on fire
     * int j          | fireAspectModifier   | Literally just to check that the weapon used has fire aspect enchantments
     * double d0      | distanceWalkedDelta  | This checks that the distance walked delta is more than the normal walking speed to evaluate if you're making a sweaping attack
     * double d1      | targetMotionX        | Current target entity motion x vector
     * double d2      | targetMotionY        | Current target entity motion y vector
     * double d3      | targetMotionZ        | Current target entity motion z vector
     * boolean flag5  | attackSucceeded      | Whether the attack event succeeded
     *
     * @param targetEntity The target entity
     */
    @Overwrite
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        // Sponge Start - Add SpongeImpl hook to override in forge as necessary
        if (!SpongeImplHooks.checkAttackEntity((EntityPlayer) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity((EntityPlayer) (Object) this)) {
                // Sponge Start - Prepare our event values
                // float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                final double originalBaseDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float damage = (float) originalBaseDamage;
                // Sponge End
                float enchantmentDamage = 0.0F;

                // Spogne Start - Redirect getting enchantments for our damage event handlers
                // if (targetEntity instanceof EntityLivingBase) {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
                // } else {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                // }
                float attackStrength = this.getCooledAttackStrength(0.5F);

                final List<ModifierFunction<DamageModifier>> originalFunctions = new ArrayList<>();

                final EnumCreatureAttribute creatureAttribute = targetEntity instanceof EntityLivingBase
                                                                ? ((EntityLivingBase) targetEntity).getCreatureAttribute()
                                                                : EnumCreatureAttribute.UNDEFINED;
                final List<DamageFunction> enchantmentModifierFunctions = DamageEventHandler.createAttackEnchantmentFunction(this.getHeldItemMainhand(), creatureAttribute, attackStrength);
                // This is kept for the post-damage event handling
                final List<DamageModifier> enchantmentModifiers = enchantmentModifierFunctions.stream().map(ModifierFunction::getModifier).collect(Collectors.toList());

                enchantmentDamage = (float) enchantmentModifierFunctions.stream()
                        .map(ModifierFunction::getFunction)
                        .mapToDouble(function -> function.applyAsDouble(originalBaseDamage))
                        .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                originalFunctions.add(DamageEventHandler.provideCooldownAttackStrengthFunction((EntityPlayer) (Object) this, attackStrength));
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                enchantmentDamage = enchantmentDamage * attackStrength;
                this.resetCooldown();

                if (damage > 0.0F || enchantmentDamage > 0.0F) {
                    boolean isStrongAttack = attackStrength > 0.9F;
                    boolean isSprintingAttack = false;
                    boolean isCriticalAttack = false;
                    boolean isSweapingAttack = false;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier((EntityPlayer) (Object) this);

                    if (this.isSprinting() && isStrongAttack) {
                        // Sponge - Only play sound after the event has be thrown and not cancelled.
                        // this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.entity_player_attack_knockback, this.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        isSprintingAttack = true;
                    }

                    isCriticalAttack = isStrongAttack && this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(MobEffects.BLINDNESS) && !this.isRiding() && targetEntity instanceof EntityLivingBase;
                    isCriticalAttack = isCriticalAttack && !this.isSprinting();

                    if (isCriticalAttack) {
                        // Sponge Start - add critical attack tuple
                        // damage *= 1.5F; // Sponge - This is handled in the event
                        originalFunctions.add(DamageEventHandler.provideCriticalAttackTuple((EntityPlayer) (Object) this));
                        // Sponge End
                    }

                    // damage = damage + enchantmentDamage; // Sponge - We don't need this since our event will re-assign the damage to deal
                    double distanceWalkedDelta = (double) (this.distanceWalkedModified - this.prevDistanceWalkedModified);

                    final ItemStack heldItem = this.getHeldItem(EnumHand.MAIN_HAND);
                    if (isStrongAttack && !isCriticalAttack && !isSprintingAttack && this.onGround && distanceWalkedDelta < (double) this.getAIMoveSpeed()) {
                        ItemStack itemstack = heldItem;

                        if (itemstack.getItem() instanceof ItemSword) {
                            isSweapingAttack = true;
                        }
                    }

                    // Sponge Start - Create the event and throw it
                    final DamageSource damageSource = DamageSource.causePlayerDamage((EntityPlayer) (Object) this);
                    final boolean isMainthread = !this.world.isRemote;
                    if (isMainthread) {
                        Sponge.getCauseStackManager().pushCause(damageSource);
                    }
                    final Cause currentCause = isMainthread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), damageSource);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(currentCause, originalFunctions, EntityUtil.fromNative(targetEntity), knockbackModifier, originalBaseDamage);
                    SpongeImpl.postEvent(event);
                    if (isMainthread) {
                        Sponge.getCauseStackManager().popCause();
                    }
                    if (event.isCancelled()) {
                        return;
                    }

                    damage = (float) event.getFinalOutputDamage();
                    // sponge - need final for later events
                    final double attackDamage = damage;
                    knockbackModifier = event.getKnockbackModifier();
                    enchantmentDamage = (float) enchantmentModifiers.stream()
                            .mapToDouble(event::getOutputDamage)
                            .sum();
                    // Sponge End

                    float targetOriginalHealth = 0.0F;
                    boolean litEntityOnFire = false;
                    int fireAspectModifier = EnchantmentHelper.getFireAspectModifier((EntityPlayer) (Object) this);

                    if (targetEntity instanceof EntityLivingBase) {
                        targetOriginalHealth = ((EntityLivingBase) targetEntity).getHealth();

                        if (fireAspectModifier > 0 && !targetEntity.isBurning()) {
                            litEntityOnFire = true;
                            targetEntity.setFire(1);
                        }
                    }

                    double targetMotionX = targetEntity.motionX;
                    double targetMotionY = targetEntity.motionY;
                    double targetMotionZ = targetEntity.motionZ;
                    boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) (Object) this), damage);

                    if (attackSucceeded) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof EntityLivingBase) {
                                ((EntityLivingBase) targetEntity).knockBack((EntityPlayer) (Object) this, (float) knockbackModifier * 0.5F, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                            } else {
                                targetEntity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F));
                            }

                            this.motionX *= 0.6D;
                            this.motionZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        if (isSweapingAttack) {
                            for (EntityLivingBase entitylivingbase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, targetEntity.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                                if (entitylivingbase != (EntityPlayer) (Object) this && entitylivingbase != targetEntity && !this.isOnSameTeam(entitylivingbase) && this.getDistanceSq(entitylivingbase) < 9.0D) {
                                    // Sponge Start - Do a small event for these entities
                                    // entitylivingbase.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                                    // entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                                    final EntityDamageSource sweepingAttackSource = EntityDamageSource.builder().entity(this).type(DamageTypes.SWEEPING_ATTACK).build();
                                    try (final StackFrame frame = isMainthread ? Sponge.getCauseStackManager().pushCauseFrame() : null) {
                                        if (isMainthread) {
                                            frame.pushCause(sweepingAttackSource);
                                        }
                                        final ItemStackSnapshot heldSnapshot = ItemStackUtil.snapshotOf(heldItem);
                                        if (isMainthread) {
                                            frame.addContext(EventContextKeys.WEAPON, heldSnapshot);
                                        }
                                        final DamageFunction sweapingFunction = DamageFunction.of(DamageModifier.builder()
                                                .cause(Cause.of(EventContext.empty(), heldSnapshot))
                                                .item(heldSnapshot)
                                                .type(DamageModifierTypes.SWEEPING)
                                                .build(),
                                            (incoming) -> EnchantmentHelper.getSweepingDamageRatio((EntityPlayer) (Object) this) * attackDamage);
                                        final List<DamageFunction> sweapingFunctions = new ArrayList<>();
                                        sweapingFunctions.add(sweapingFunction);
                                        AttackEntityEvent sweepingAttackEvent = SpongeEventFactory.createAttackEntityEvent(
                                            currentCause,
                                            sweapingFunctions, EntityUtil.fromNative(entitylivingbase), 1, 1.0D);
                                        SpongeImpl.postEvent(sweepingAttackEvent);
                                        if (!sweepingAttackEvent.isCancelled()) {
                                            entitylivingbase
                                                .knockBack((EntityPlayer) (Object) this, sweepingAttackEvent.getKnockbackModifier() * 0.4F,
                                                    (double) MathHelper.sin(this.rotationYaw * 0.017453292F),
                                                    (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                                            entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) (Object) this),
                                                (float) sweepingAttackEvent.getFinalOutputDamage());
                                        }
                                    }
                                    // Sponge End
                                }
                            }

                            this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                            this.spawnSweepParticles();
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = targetMotionX;
                            targetEntity.motionY = targetMotionY;
                            targetEntity.motionZ = targetMotionZ;
                        }

                        if (isCriticalAttack) {
                            this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                            this.onCriticalHit(targetEntity);
                        }

                        if (!isCriticalAttack && !isSweapingAttack) {
                            if (isStrongAttack) {
                                this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK , this.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantmentDamage > 0.0F) {
                            this.onEnchantmentCritical(targetEntity);
                        }

                        this.setLastAttackedEntity(targetEntity);

                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, (EntityPlayer) (Object) this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments((EntityPlayer) (Object) this, targetEntity);
                        ItemStack itemstack1 = this.getHeldItemMainhand();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof MultiPartEntityPart) {
                            IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;

                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if(!itemstack1.isEmpty() && targetEntity instanceof EntityLivingBase) {
                            itemstack1.hitEntity((EntityLivingBase)targetEntity, (EntityPlayer) (Object) this);
                            if(itemstack1.isEmpty()) {
                                this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            float f5 = targetOriginalHealth - ((EntityLivingBase) targetEntity).getHealth();
                            this.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspectModifier > 0) {
                                targetEntity.setFire(fireAspectModifier * 4);
                            }

                            if (this.world instanceof WorldServer && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        this.addExhaustion(0.3F);
                    } else {
                        this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);

                        if (litEntityOnFire) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "setItemStackToSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private void onSetItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack, CallbackInfo ci)
    {
        if (((IMixinInventoryPlayer) this.inventory).capturesTransactions()) {
            if (slotIn == EntityEquipmentSlot.MAINHAND) {
                ItemStack orig = this.inventory.mainInventory.get(this.inventory.currentItem);
                Slot slot = ((PlayerInventory) this.inventory).getPrimary().getHotbar().getSlot(SlotIndex.of(this.inventory.currentItem)).get();
                ((IMixinInventoryPlayer) this.inventory).getCapturedTransactions().add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
                ItemStack orig = this.inventory.offHandInventory.get(0);
                Slot slot = ((PlayerInventory) this.inventory).getOffhand();
                ((IMixinInventoryPlayer) this.inventory).getCapturedTransactions().add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                ItemStack orig = this.inventory.armorInventory.get(slotIn.getIndex());
                Slot slot = ((PlayerInventory) this.inventory).getEquipment().getSlot(SlotIndex.of(slotIn.getIndex())).get();
                ((IMixinInventoryPlayer) this.inventory).getCapturedTransactions().add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            }
        }
    }

    @Redirect(method = "setDead", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/inventory/Container;onContainerClosed(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void onOnContainerClosed(Container container, EntityPlayer player) {
        // Corner case where the server is shutting down on the client, the enitty player mp is also being killed off.
        if (Sponge.isServerAvailable() && SpongeImplHooks.isClientAvailable() && Sponge.getGame().getState() == GameState.SERVER_STOPPING) {
            container.onContainerClosed(player);
            return;
        }
        if (player instanceof EntityPlayerMP ) {
            final EntityPlayerMP serverPlayer = (EntityPlayerMP) player;


            try (PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext()
                    .source(serverPlayer)
                    .packetPlayer(serverPlayer)
                    .openContainer(container)) {
                // intentionally missing the lastCursor to not double throw close event
                ctx.buildAndSwitch();
                final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(this.inventory.getItemStack());
                container.onContainerClosed(player);
                SpongeCommonEventFactory.callInteractInventoryCloseEvent(this.openContainer, serverPlayer, cursor, ItemStackSnapshot.NONE, false);
            }
        } else {
            // Proceed as normal with client code
            container.onContainerClosed(player);
        }
    }

    @Override
    public void shouldRestoreInventory(boolean restore) {
        this.shouldRestoreInventory = restore;
    }

    @Override
    public boolean shouldRestoreInventory() {
        return this.shouldRestoreInventory;
    }

    @Override
    public boolean isImmuneToFireForIgniteEvent() {
        return this.isSpectator() || this.isCreative();
    }
}
