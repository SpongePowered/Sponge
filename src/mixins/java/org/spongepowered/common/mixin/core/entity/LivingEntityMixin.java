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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.MovementTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements LivingEntityBridge {

    // @formatter:off

    @Shadow @Final public int maxHurtResistantTime;
    @Shadow public int hurtTime;
    @Shadow public int maxHurtTime;
    @Shadow public float attackedAtYaw;
    @Shadow public float limbSwingAmount;
    @Shadow protected int idleTime;
    @Shadow protected int recentlyHit;
    @Shadow protected int activeItemStackUseCount;
    @Shadow protected float lastDamage;
    @Shadow @Nullable protected PlayerEntity attackingPlayer;
    @Shadow protected ItemStack activeItemStack;
    @Shadow private DamageSource lastDamageSource;
    @Shadow private long lastDamageStamp;
    @Shadow protected boolean dead;

    @Shadow public abstract IAttributeInstance shadow$getAttribute(IAttribute attribute);
    @Shadow public abstract void shadow$setHealth(float health);
    @Shadow public abstract void shadow$knockBack(Entity entityIn, float p_70653_2_, double p_70653_3_, double p_70653_5_);
    @Shadow public abstract void shadow$setRevengeTarget(LivingEntity livingBase);
    @Shadow public abstract void shadow$setAbsorptionAmount(float amount);
    @Shadow public abstract void shadow$setHeldItem(Hand hand, @Nullable ItemStack stack);
    @Shadow public abstract void shadow$resetActiveHand();
    @Shadow public abstract int shadow$getItemInUseCount();
    @Shadow public abstract float shadow$getAbsorptionAmount();
    @Shadow public abstract float shadow$getHealth();
    @Shadow public abstract boolean shadow$isPotionActive(Effect potion);
    @Shadow protected abstract boolean shadow$canBlockDamageSource(DamageSource p_184583_1_);
    @Shadow public abstract ItemStack shadow$getItemStackFromSlot(EquipmentSlotType slotIn);
    @Shadow public abstract ItemStack shadow$getHeldItemMainhand();
    @Shadow public abstract CombatTracker shadow$getCombatTracker();
    @Shadow public void shadow$onKillCommand() { }
    @Shadow public abstract Hand shadow$getActiveHand();
    @Shadow protected abstract void shadow$markVelocityChanged();
    @Shadow protected abstract void shadow$damageShield(float p_184590_1_);
    @Shadow protected abstract void shadow$playHurtSound(DamageSource p_184581_1_);
    @Shadow protected abstract void shadow$blockUsingShield(LivingEntity p_190629_1_);
    @Shadow protected abstract float shadow$getSoundVolume();
    @Shadow protected abstract float shadow$getSoundPitch();
    @Shadow protected abstract SoundEvent shadow$getDeathSound();
    @Shadow public abstract boolean shadow$isSleeping();
    @Shadow private boolean shadow$checkTotemDeathProtection(final DamageSource p_190628_1_) {
        return false; // SHADOWED
    }
    @Shadow public abstract void shadow$onDeath(DamageSource cause);
    @Shadow protected abstract void shadow$addItemParticles(ItemStack stack, int count);
    @Shadow public abstract void shadow$wakeUp();

    // @formatter:on

    @Shadow public int deathTime;
    @Shadow public float rotationYawHead;
    private int impl$maxAir = this.shadow$getMaxAir();
    @Nullable private ItemStack impl$activeItemStackCopy;
    @Nullable private Vector3d impl$preTeleportPosition;

/*    @Override
    public int bridge$getMaxAir() {
        return this.impl$maxAir;
    }

    @Override
    public void bridge$setMaxAir(final int air) {
        this.impl$maxAir = air;
        if (air != Constants.Sponge.Entity.DEFAULT_MAX_AIR) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
            spongeData.putInt(Constants.Sponge.Entity.MAX_AIR, air);
        } else {
            if (((DataCompoundHolder) this).data$hasSpongeDataCompound()) {
                ((DataCompoundHolder) this).data$getSpongeDataCompound().remove(Constants.Sponge.Entity.MAX_AIR);
            }
        }
    }

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Sponge.Entity.MAX_AIR)) {
            this.impl$maxAir = compound.getInt(Constants.Sponge.Entity.MAX_AIR);
        }
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        if (this.impl$maxAir != Constants.Sponge.Entity.DEFAULT_MAX_AIR) { // We don't need to set max air unless it's really necessary
            compound.putInt(Constants.Sponge.Entity.MAX_AIR, this.impl$maxAir);
        }
    }

    *//**
     * @param health The health
     * @param info The callback
     * @author gabizou - April 29th, 2018
     * @reason Due to cancelling death events, "healing" the entity is the only way to cancel the
     *     death, but we still want to reset the death event counter. This is the simplest way to get it working
     *     with forge mods who do not have access to Sponge's API.
     *//*
    @Inject(method = "setHealth",
        at = @At("HEAD"))
    private void onSetHealthResetEvents(final float health, final CallbackInfo info) {
        if (this.shadow$getHealth() <= 0 && health > 0) {
            this.bridge$resetDeathEventsPosted();
        }
    }

    @Redirect(method = "applyPotionDamageCalculations",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isPotionActive(Lnet/minecraft/potion/Effect;)Z"))
    private boolean impl$onIsPotionActive(final LivingEntity entityIn, final Effect potion) {
        return false; // handled in our bridge$damageEntityHook
    }

    *//**
     * @param entityIn The entity being damaged
     * @param damage The damage to deal
     * @author blood - Some time ago in 2015?
     * @reason Our damage hook handles armor modifiers and "replaying" damage to armor.
     *//*
    @Redirect(method = "applyArmorCalculations",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;damageArmor(F)V"))
    private void onDamageArmor(final LivingEntity entityIn, final float damage) {
        // do nothing as this is handled in our bridge$damageEntityHook
    }

    *//**
     * @author bloodmc - November 21, 2015
     * @reason This shouldn't be used internally but a mod may still call it so we simply reroute to our hook.
     *//*
    @Overwrite
    protected void damageEntity(final DamageSource damageSource, final float damage) {
        this.bridge$damageEntity(damageSource, damage);
    }

    *//**
     * @author bloodmc - November 22, 2015
     * @author gabizou - Updated April 11th, 2016 - Update for 1.9 changes
     * @author Aaron1011 - Updated Nov 11th, 2016 - Update for 1.11 changes
     * @reason Reroute damageEntity calls to our hook in order to prevent damage.
     *//*
    @SuppressWarnings("ConstantConditions")
    @Override
    @Overwrite
    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        // Sponge start - Add certain hooks for necessities
        this.lastDamageSource = source;
        if (source == null) {
            new PrettyPrinter(60).centre().add("Null DamageSource").hr()
                .addWrapped("Sponge has found a null damage source! This should NEVER happen "
                    + "as the DamageSource is used for all sorts of calculations. Usually"
                    + " this can be considered developer error. Please report the following"
                    + " stacktrace to the most appropriate mod/plugin available.")
                .add()
                .add(new IllegalArgumentException("Null DamageSource"))
                .log(SpongeCommon.getLogger(), Level.WARN);
            return false;
        }
        // Sponge - This hook is for forge use mainly
        if (!this.bridge$hookModAttack((LivingEntity) (Object) this, source, amount)) {
            return false;
        }
        // Sponge end
        if (this.shadow$isInvulnerableTo(source)) {
            return false;
        } else if (this.world.isRemote) {
            return false;
        } else if (this.shadow$getHealth() <= 0.0F && source != DamageTypeStreamGenerator.IGNORED_DAMAGE_SOURCE) {
            return false;
        } else if (source.isFireDamage() && this.shadow$isPotionActive(Effects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.shadow$isSleeping() && !this.world.isRemote) {
                this.shadow$wakeUp();
            }

            this.idleTime = 0;


            final float f = amount;

            // Sponge - ignore as this is handled in our damageEntityHook
//                if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
//                {
//                    this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this);
//                    amount *= 0.75F;
//                }
            // Sponge End

            // Sponge - set the 'shield blocking ran' flag to the proper value, since
            // we comment out the logic below
            final boolean flag = amount > 0.0F && this.shadow$canBlockDamageSource(source);

            // Sponge start - this is handled in our bridge$damageEntityHook
//                boolean flag = false;
//
//                if (amount > 0.0F && this.shadow$canBlockDamageSource(source))
//                {
//                    this.damageShield(amount);
//                    amount = 0.0F;
//
//                    if (!source.isProjectile())
//                    {
//                        Entity entity = source.getImmediateSource();
//
//                        if (entity instanceof EntityLivingBase)
//                        {
//                            this.blockUsingShield((EntityLivingBase)entity);
//                        }
//                    }
//
//                    flag = true;
//                }
            // Sponge end

            this.limbSwingAmount = 1.5F;
            boolean flag1 = true;

            if ((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F) {
                if (amount <= this.lastDamage) { // Technically, this is wrong since 'amount' won't be 0 if a shield is used. However, we need bridge$damageEntityHook so that we process the shield, so we leave it as-is
                    return false;
                }

                // Sponge start - reroute to our damage hook
                // only if the class is unmodded. If it's a modded class, then it should be calling our
                // damageEntity method, which would re-run our bridge$damageEntityHook.
                if (((EntityTypeBridge) this.shadow$getType()).bridge$overridesDamageEntity()) {
                    this.damageEntity(source, amount - this.lastDamage);
                } else {
                    if (!this.bridge$damageEntity(source, amount - this.lastDamage)) {
                        return false;
                    }
                }

                // this.damageEntity(source, amount - this.lastDamage); // handled above
                // Sponge end
                this.lastDamage = amount;
                flag1 = false;
            } else {
                // Sponge start - reroute to our damage hook
                if (((EntityTypeBridge) this.shadow$getType()).bridge$overridesDamageEntity()) {
                    this.damageEntity(source, amount);
                } else {
                    if (!this.bridge$damageEntity(source, amount)) {
                        return false;
                    }
                }
                this.lastDamage = amount;
                this.hurtResistantTime = 20;
                // this.damageEntity(source, amount); // handled above
                // Sponge end
                this.maxHurtTime = 10;
                this.hurtTime = this.maxHurtTime;
            }

            this.attackedAtYaw = 0.0F;
            final Entity entity = source.getTrueSource();

            if (entity instanceof LivingEntity) {
                this.shadow$setRevengeTarget((LivingEntity) entity);
            }

            if (entity instanceof PlayerEntity) {
                this.recentlyHit = 100;
                this.attackingPlayer = (PlayerEntity) entity;
                // Forge Start - Change WolfEntity check to TameableEntity check
                // } else if (entity1 instanceof WolfEntity) { - Vanilla
            } else if (entity instanceof TameableEntity) {
                final TameableEntity tameableEntity = (TameableEntity) entity;
                // Forge end
                if (tameableEntity.isTamed()) {
                    this.recentlyHit = 100;
                    final LivingEntity livingentity = tameableEntity.getOwner();
                    if (livingentity != null && livingentity.getType() == EntityType.PLAYER) {
                        this.attackingPlayer = (PlayerEntity) livingentity;
                    } else {
                        this.attackingPlayer = null;
                    }
                }
            }

            if (flag1) {
                if (flag) {
                    this.world.setEntityState((LivingEntity) (Object) this, (byte) 29);
                } else if (source instanceof EntityDamageSource && ((EntityDamageSource) source).getIsThornsDamage()) {
                    this.world.setEntityState((LivingEntity) (Object) this, (byte) 33);
                } else {
                    final byte b0;
                    if (source == DamageSource.DROWN) {
                        b0 = 36;
                    } else if (source.isFireDamage()) {
                        b0 = 37;
                    } else if (source == DamageSource.SWEET_BERRY_BUSH) {
                        b0 = 44;
                    } else {
                        b0 = 2;
                    }

                    this.world.setEntityState((LivingEntity) (Object) this, b0);
                }


                if (source != DamageSource.DROWN && !flag) { // Sponge - remove 'amount > 0.0F' - it's redundant in Vanilla, and breaks our handling of shields
                    this.shadow$markVelocityChanged();
                }

                if (entity != null) {
                    double d1 = entity.posX - this.posX;
                    double d0;

                    for (d0 = entity.posZ - this.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                        d1 = (Math.random() - Math.random()) * 0.01D;
                    }

                    this.attackedAtYaw = (float) (MathHelper.atan2(d0, d1) * 180.0D / Math.PI - (double) this.rotationYaw);
                    this.shadow$knockBack(entity, 0.4F, d1, d0);
                } else {
                    this.attackedAtYaw = (float) (Math.random() * 2.0D * 180);
                }
            }

            if (this.shadow$getHealth() <= 0.0F) {
                if (!this.shadow$checkTotemDeathProtection(source)) {
                    final SoundEvent soundevent = this.shadow$getDeathSound();

                    if (flag1 && soundevent != null) {
                        this.shadow$playSound(soundevent, this.shadow$getSoundVolume(), this.shadow$getSoundPitch());
                    }

                    this.shadow$onDeath(source); // Sponge tracker will redirect this call
                }
            } else if (flag1) {
                this.shadow$playHurtSound(source);
            }

            final boolean flag2 = !flag;// Sponge - remove 'amount > 0.0F' since it's handled in the event
            if (flag2) {
                this.lastDamageSource = source;
                this.lastDamageStamp = this.world.getGameTime();
            }

            if ((LivingEntity) (Object) this instanceof ServerPlayerEntity) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity) (Object) this, source, f, amount, flag);
            }

            if (entity instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity) entity, (Entity) (Object) this, source, f, amount, flag);
            }

            return flag2;

        }
    }

    *//**
     * @author gabizou - January 4th, 2016
     *     This is necessary for invisibility checks so that vanish players don't actually send the particle stuffs.
     *//*
    @Redirect(method = "updateItemUse",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;addItemParticles(Lnet/minecraft/item/ItemStack;I)V"))
    private void spawnItemParticle(final LivingEntity livingEntity, final ItemStack stack, final int count) {
        if (!this.bridge$isVanished()) {
            this.shadow$addItemParticles(stack, count);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean bridge$damageEntity(final DamageSource damageSource, float damage) {
        if (!this.shadow$isInvulnerableTo(damageSource)) {
            final boolean isHuman = (LivingEntity) (Object) this instanceof PlayerEntity;
            // apply forge damage hook
            damage = this.bridge$applyModDamage((LivingEntity) (Object) this, damageSource, damage);
            final float originalDamage = damage; // set after forge hook.
            if (damage <= 0) {
                return false;
            }

            final List<DamageFunction> originalFunctions = new ArrayList<>();
            final Optional<DamageFunction> hardHatFunction =
                DamageEventHandler.createHardHatModifier((LivingEntity) (Object) this, damageSource);
            final Optional<List<DamageFunction>> armorFunction =
                this.bridge$provideArmorModifiers((LivingEntity) (Object) this, damageSource, damage);
            final Optional<DamageFunction> resistanceFunction =
                DamageEventHandler.createResistanceModifier((LivingEntity) (Object) this, damageSource);
            final Optional<List<DamageFunction>> armorEnchantments =
                DamageEventHandler.createEnchantmentModifiers((LivingEntity) (Object) this, damageSource);
            final Optional<DamageFunction> absorptionFunction =
                DamageEventHandler.createAbsorptionModifier((LivingEntity) (Object) this, damageSource);
            final Optional<DamageFunction> shieldFunction =
                DamageEventHandler.createShieldFunction((LivingEntity) (Object) this, damageSource, damage);

            hardHatFunction.ifPresent(originalFunctions::add);

            shieldFunction.ifPresent(originalFunctions::add);

            armorFunction.ifPresent(originalFunctions::addAll);

            resistanceFunction.ifPresent(originalFunctions::add);

            armorEnchantments.ifPresent(originalFunctions::addAll);

            absorptionFunction.ifPresent(originalFunctions::add);
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                DamageEventHandler.generateCauseFor(damageSource, frame);

                final DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(frame.getCurrentCause(), (org.spongepowered.api.entity.Entity) this, originalFunctions, originalDamage);
                if (damageSource != DamageTypeStreamGenerator.IGNORED_DAMAGE_SOURCE) { // Basically, don't throw an event if it's our own damage source
                    Sponge.getEventManager().post(event);
                }
                if (event.isCancelled()) {
                    return false;
                }

                damage = (float) event.getFinalDamage();

                damage = this.bridge$applyModDamagePost((LivingEntity) (Object) this, damageSource, damage);

                // Helmet
                final ItemStack helmet = this.shadow$getItemStackFromSlot(EquipmentSlotType.HEAD);
                // We still sanity check if a mod is calling to damage the entity with an anvil or falling block
                // without using our mixin redirects in EntityFallingBlockMixin.
                if ((damageSource instanceof FallingBlockDamageSource) || damageSource == DamageSource.ANVIL || damageSource == DamageSource.FALLING_BLOCK && !helmet.isEmpty()) {
                    helmet.damageItem((int) (event.getBaseDamage() * 4.0F + this.rand.nextFloat() * event.getBaseDamage() * 2.0F), (LivingEntity) (Object) this, (entity) -> {
                        entity.sendBreakAnimation(EquipmentSlotType.HEAD);
                    });
                }

                // Shield
                if (shieldFunction.isPresent()) {
                    this.shadow$damageShield((float) event.getBaseDamage());
                    if (!damageSource.isProjectile()) {
                        final Entity entity = damageSource.getImmediateSource();

                        if (entity instanceof LivingEntity) {
                            this.shadow$blockUsingShield((LivingEntity) entity);
                        }
                    }
                }

                // Armor
                if (!damageSource.isUnblockable()) {
                    for (final DamageFunction modifier : event.getModifiers()) {
                        this.bridge$applyArmorDamage((LivingEntity) (Object) this, damageSource, event, modifier.getModifier());
                    }
                }

                double absorptionModifier = absorptionFunction.map(function -> event.getDamage(function.getModifier())).orElse(0d);
                if (absorptionFunction.isPresent()) {
                    absorptionModifier = event.getDamage(absorptionFunction.get().getModifier());
                }

                this.shadow$setAbsorptionAmount(Math.max(this.shadow$getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
                if (damage != 0.0F) {
                    if (isHuman) {
                        ((PlayerEntity) (Object) this).addExhaustion(damageSource.getHungerDamage());
                    }
                    final float f2 = this.shadow$getHealth();

                    this.shadow$setHealth(f2 - damage);
                    this.shadow$getCombatTracker().trackDamage(damageSource, f2, damage);

                    if (isHuman) {
                        return true;
                    }

                    this.shadow$setAbsorptionAmount(this.shadow$getAbsorptionAmount() - damage);
                }
                return true;
            }
        }
        return false;
    }
/*
    @Override
    public float bridge$applyModDamage(final LivingEntity entityLivingBase, final DamageSource source, final float damage) {
        return damage;
    }

    @Override
    public Optional<List<DamageFunction>> bridge$provideArmorModifiers(final LivingEntity entityLivingBase,
        final DamageSource source, final double damage) {
        return DamageEventHandler.createArmorModifiers(entityLivingBase, source, damage);
    }

    @Override
    public void bridge$applyArmorDamage(
        final LivingEntity entityLivingBase, final DamageSource source, final DamageEntityEvent entityEvent, final DamageModifier modifier) {
        final Optional<DamageObject> optional = modifier.getCause().first(DamageObject.class);
        if (optional.isPresent()) {
            DamageEventHandler.acceptArmorModifier((LivingEntity) (Object) this, source, modifier, entityEvent.getDamage(modifier));
        }
    }

    @Override
    public float bridge$applyModDamagePost(final LivingEntity entityLivingBase, final DamageSource source, final float damage) {
        return damage;
    }


    @Override
    public boolean bridge$hookModAttack(final LivingEntity entityLivingBase, final DamageSource source, final float amount) {
        return true;
    }

    *//**
     * @author gabizou - January 4th, 2016
     * @reason This allows invisiblity to ignore entity collisions.
     *//*
    @Overwrite
    public boolean canBeCollidedWith() {
        return !(this.bridge$isVanished() && this.bridge$isUncollideable()) && !this.removed;
    }

    @Redirect(method = "updateFallState",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/server/ServerWorld;spawnParticle(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"))
    private int impl$vanishSpawnParticleForFallState(
        final ServerWorld serverWorld, final IParticleData particleTypes, final double xCoord, final double yCoord,
        final double zCoord, final int numberOfParticles, final double xOffset, final double yOffset,
        final double zOffset, final double particleSpeed) {
        if (!this.bridge$isVanished()) {
            return serverWorld.spawnParticle(particleTypes, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed);
        }
        return 0;
    }

    @Inject(method = "onItemUseFinish",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;resetActiveHand()V"))
    private void impl$updateHealthForUseFinish(final CallbackInfo ci) {
        if (this instanceof ServerPlayerEntityBridge) {
            ((ServerPlayerEntityBridge) this).bridge$refreshScaledHealth();
        }
    }

    // Data delegated methods

    // Start implementation of UseItemstackEvent

    @Inject(method = "setActiveHand",
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;activeItemStack:Lnet/minecraft/item/ItemStack;"))
    private void impl$onSetActiveItemStack(final Hand hand, final CallbackInfo ci, final ItemStack stack) {
        if (this.world.isRemote) {
            return;
        }

        final UseItemStackEvent.Start event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) hand;
            this.impl$addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventStart(PhaseTracker.getCauseStackManager().getCurrentCause(),
                stack.getUseDuration(), stack.getUseDuration(), snapshot);
        }

        if (SpongeCommon.postEvent(event)) {
            ci.cancel();
        } else {
            this.activeItemStackUseCount = event.getRemainingDuration();
        }
    }

    @Redirect(method = "setActiveHand",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;activeItemStackUseCount:I"))
    private void impl$getItemDuration(final LivingEntity this$0, final int count) {
        if (this.world.isRemote) {
            this.activeItemStackUseCount = count;
        }
        // If we're on the server, do nothing, since we already set this field on onSetActiveItemStack
    }

    // A helper method for firing UseItemStackEvent sub-events
    // This ensures that the cause and context for these events
    // always have OWNER and NOTIFIER set (if possible),
    // as well as USED_ITEM and USED_HAND
    private void impl$addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot, final HandType hand) {
        frame.addContext(EventContextKeys.USED_HAND, hand);
        this.impl$addSelfToFrame(frame, snapshot);
    }

    private void impl$addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot) {
        frame.pushCause(this);
        frame.addContext(EventContextKeys.USED_ITEM, snapshot);
        if (this instanceof User) {
            frame.addContext(EventContextKeys.CREATOR, (User) this);
            frame.addContext(EventContextKeys.NOTIFIER, (User) this);
        }
    }

    @Redirect(method = "updateActiveHand",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getItemInUseCount()I",
            ordinal = 0))
    private int impl$onGetRemainingItemDuration(final LivingEntity self) {
        if (this.world.isRemote) {
            return self.getItemInUseCount();
        }

        final UseItemStackEvent.Tick event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.activeItemStack);
            final HandType handType = (HandType) (Object) this.shadow$getActiveHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventTick(PhaseTracker.getCauseStackManager().getCurrentCause(),
                this.activeItemStackUseCount, this.activeItemStackUseCount, snapshot);
            SpongeCommon.postEvent(event);
        }
        // Because the item usage will only finish if activeItemStackUseCount == 0 and decrements it first, it should be >= 1
        this.activeItemStackUseCount = Math.max(event.getRemainingDuration(), 1);

        if (event.isCancelled()) {
            // Get prepared for some cool hacks: We're within the condition for updateItemUse
            // So if we don't want it to call the method we just pass a value that makes the
            // condition evaluate to false, so an integer >= 25
            return 26;
        }
        SpongeImplHooks.onUseItemTick((LivingEntity) (Object) this, this.activeItemStack, this.activeItemStackUseCount);


        return this.shadow$getItemInUseCount();
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onItemUseFinish",
        cancellable = true,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;updateItemUse(Lnet/minecraft/item/ItemStack;I)V"))
    private void impl$onUpdateItemUse(final CallbackInfo ci) {
        if (this.world.isRemote) {
            return;
        }


        final UseItemStackEvent.Finish event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.activeItemStack);
            final HandType handType = (HandType) (Object) this.shadow$getActiveHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventFinish(PhaseTracker.getCauseStackManager().getCurrentCause(),
                this.activeItemStackUseCount, this.activeItemStackUseCount, snapshot);
        }
        SpongeCommon.postEvent(event);
        if (event.getRemainingDuration() > 0) {
            this.activeItemStackUseCount = event.getRemainingDuration();
            ci.cancel();
        } else if (event.isCancelled()) {
            this.shadow$resetActiveHand();
            ci.cancel();
        } else {
            this.impl$activeItemStackCopy = this.activeItemStack.copy();
        }
    }

    @Redirect(method = "onItemUseFinish",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;setHeldItem(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"))
    private void impl$onSetHeldItem(final LivingEntity self, final Hand hand, final ItemStack stack) {
        if (this.world.isRemote) {
            self.setHeldItem(hand, stack);
            return;
        }

        // Unforunately, ItemFood calls ItemStack#shrink in Item#onItemUseFinish.
        // To ensure that we provide the original ItemStack in the event,
        // we make a copy of in our onUpdateItemUse redirect
        // If the event or transaction is cancelled, we make sure to explicitly
        // set the copy back in the player's hand, since it may have been already
        // modified if an ItemFood is being used.

        final ItemStackSnapshot activeItemStackSnapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy == null ? ItemStack.EMPTY : this.impl$activeItemStackCopy);


        final UseItemStackEvent.Replace event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack == null ? ItemStack.EMPTY : stack);
            final HandType handType = (HandType) (Object) hand;
            this.impl$addSelfToFrame(frame, activeItemStackSnapshot, handType);
            event = SpongeEventFactory.createUseItemStackEventReplace(PhaseTracker.getCauseStackManager().getCurrentCause(),
                this.activeItemStackUseCount, this.activeItemStackUseCount, activeItemStackSnapshot,
                new Transaction<>(ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy), snapshot));
        }

        if (SpongeCommon.postEvent(event)) {
            this.shadow$setHeldItem(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        if (!event.getItemStackResult().isValid()) {
            this.shadow$setHeldItem(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        this.shadow$setHeldItem(hand, ItemStackUtil.fromSnapshotToNative(event.getItemStackResult().getFinal()));
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "stopActiveHand",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;onPlayerStoppedUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"))
    // stopActiveHand
    private void impl$onStopPlayerUsing(final ItemStack stack, final World world, final LivingEntity self, final int duration) {
        if (this.world.isRemote) {
            stack.onPlayerStoppedUsing(world, self, duration);
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) this.shadow$getActiveHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            if (!SpongeCommon.postEvent(SpongeEventFactory.createUseItemStackEventStop(PhaseTracker.getCauseStackManager().getCurrentCause(),
                duration, duration, snapshot))) {
                stack.onPlayerStoppedUsing(world, self, duration);
            }
        }
    }

    @Inject(method = "resetActiveHand",
        at = @At("HEAD"))
    private void impl$onResetActiveHand(final CallbackInfo ci) {
        if (this.world.isRemote) {
            return;
        }

        // If we finished using an item, impl$activeItemStackCopy will be non-null
        // However, if a player stopped using an item early, impl$activeItemStackCopy will not be set
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy != null ? this.impl$activeItemStackCopy : this.activeItemStack);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            this.impl$addSelfToFrame(frame, snapshot);
            SpongeCommon.postEvent(SpongeEventFactory.createUseItemStackEventReset(PhaseTracker.getCauseStackManager().getCurrentCause(),
                this.activeItemStackUseCount, this.activeItemStackUseCount, snapshot));
        }
        this.impl$activeItemStackCopy = null;
    }*/

    // End implementation of UseItemStackEvent

    @Inject(method = "attemptTeleport", at = @At("HEAD"))
    private void impl$snapshotPositionBeforeVanillaTeleportLogic(double x, double y, double z, boolean changeState,
            CallbackInfoReturnable<Boolean> cir) {
        this.impl$preTeleportPosition = new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Inject(method = "attemptTeleport", at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.BY, by = 2), cancellable = true)
    private void impl$callMoveEntityEventForTeleport(double x, double y, double z, boolean changeState,
            CallbackInfoReturnable<Boolean> cir) {

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.ENTITY_TELEPORT);

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                    (org.spongepowered.api.entity.Entity) this, this.impl$preTeleportPosition, new Vector3d(this.posX, this.posY, this.posZ),
                    new Vector3d(x, y, z));

            if (SpongeCommon.postEvent(event)) {
                this.shadow$setPositionAndUpdate(this.impl$preTeleportPosition.getX(), this.impl$preTeleportPosition.getY(),
                        this.impl$preTeleportPosition.getZ());
                cir.setReturnValue(false);
            }

            this.shadow$setPositionAndUpdate(event.getDestinationPosition().getX(), event.getDestinationPosition().getY(),
                    event.getDestinationPosition().getZ());
        }
    }
}
