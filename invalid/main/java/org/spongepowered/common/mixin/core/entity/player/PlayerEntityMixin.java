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
package org.spongepowered.common.mixin.invalid.core.entity.player;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
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
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.ChangeLevelEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.LocationTargetingBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.provider.entity.player.ExperienceHolderUtils;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.LivingEntityMixin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerEntityBridge, LocationTargetingBridge {

    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public net.minecraft.entity.player.PlayerInventory inventory;

    @Shadow public abstract int shadow$xpBarCap();
    @Shadow public abstract float shadow$getCooledAttackStrength(float adjustTicks);
    @Shadow public abstract float shadow$getAIMoveSpeed();
    @Shadow public abstract void shadow$onCriticalHit(Entity entityHit);
    @Shadow public abstract void shadow$onEnchantmentCritical(Entity entityHit);
    @Shadow public abstract void shadow$addExhaustion(float p_71020_1_);
    @Shadow public abstract void shadow$addStat(ResourceLocation stat, int amount);
    @Shadow public abstract void shadow$resetCooldown();
    @Shadow public abstract void shadow$spawnSweepParticles();
    @Shadow public void shadow$wakeUpPlayer(final boolean immediately, final boolean updateWorldFlag, final boolean setSpawn) {};
    @Shadow public abstract void shadow$addExperienceLevel(int levels);
    @Shadow public abstract void shadow$addScore(int scoreIn);
    @Shadow public abstract SoundCategory shadow$getSoundCategory();

    private boolean impl$dontRecalculateExperience;

    /**
     * @author JBYoshi - May 17, 2017
     * @author i509VCB - February 17th, 2020 - 1.14.4
     * @reason This makes the experience updating more accurate and disables
     * the totalExperience recalculation above for this method, which would
     * otherwise have weird intermediate states.
     */
    @Overwrite
    public void giveExperiencePoints(int amount) {
        this.shadow$addScore(amount);
        final int i = Integer.MAX_VALUE - this.experienceTotal;

        if (amount > i) {
            amount = i;
        }

        if (((WorldBridge) this.world).bridge$isFake()) {
            this.experience += (float)amount / (float)this.shadow$xpBarCap();

            for(this.experienceTotal += amount; this.experience >= 1.0F; this.experience /= (float)this.shadow$xpBarCap()) {
                this.experience = (this.experience - 1.0F) * (float)this.shadow$xpBarCap();
                this.shadow$addExperienceLevel(1);
            }
        } else {
            this.postEventAndUpdateExperience(this.experienceTotal + amount);
        }
    }

    private void postEventAndUpdateExperience(final int finalExperience) {
        final SpongeExperienceHolderData data = new SpongeExperienceHolderData();
        data.setTotalExp(finalExperience);
        final ImmutableSpongeExperienceHolderData
            immutable =
            new ImmutableSpongeExperienceHolderData(this.experienceLevel, this.experienceTotal, this.bridge$getExperienceSinceLevel());
        final ChangeEntityExperienceEvent event = SpongeEventFactory.createChangeEntityExperienceEvent(
                Sponge.getCauseStackManager().getCurrentCause(), immutable, data, (Player) this);
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            return;
        }
        int finalLevel = event.getFinalData().level().get();
        if (finalLevel != this.experienceLevel) {
            @SuppressWarnings("deprecation") final ChangeLevelEvent levelEvent = SpongeEventFactory.createChangeLevelEventTargetPlayer(
                    Sponge.getCauseStackManager().getCurrentCause(), this.experienceLevel, finalLevel, (Player) this);
            SpongeCommon.postEvent(levelEvent);
            if (levelEvent.isCancelled()) {
                return;
            }
            if (levelEvent.getLevel() != finalLevel) {
                finalLevel = levelEvent.getLevel();
                event.getFinalData().set(Keys.EXPERIENCE_LEVEL, finalLevel);
            }
            if (finalLevel != this.experienceLevel) {
                this.impl$dontRecalculateExperience = true;
                try {
                    this.shadow$addExperienceLevel(finalLevel - this.experienceLevel);
                } finally {
                    this.impl$dontRecalculateExperience = false;
                }
            }
        }
        this.experience = (float) event.getFinalData().experienceSinceLevel().get()
                / ExperienceHolderUtils.getExpBetweenLevels(finalLevel);
        this.experienceTotal = event.getFinalData().totalExperience().get();
        this.experienceLevel = finalLevel;
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
    public ItemStack dropItemAndGetStack(final ItemEntity entity) {
        this.world.addEntity0(entity);
        return entity.getItem();
    }

    /**
     * @author dualspiral - October 7th, 2016
     *
     * @reason When setting {@link SpongeHealthData#setHealth(double)} to 0, {@link #shadow$onDeath(DamageSource)} was
     * not being called. This check bypasses some of the checks that prevent the superclass method being called
     * when the {@link DamageSourceRegistryModule#IGNORED_DAMAGE_SOURCE} is being used.
     */
    @Inject(method = "attackEntityFrom", cancellable = true, at = @At(value = "HEAD"))
    private void onAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        if (source == DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE) {
            // Taken from the original method, wake the player up if they are about to die.
            if (this.shadow$isSleeping() && !this.world.isRemote) {
                this.shadow$wakeUpPlayer(true, true, false);
            }

            // We just throw it to the superclass method so that we can potentially get the
            // onDeath method.
            cir.setReturnValue(super.attackEntityFrom(source, amount));
        }
    }




}
