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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.mixin.core.entity.EntityLivingMixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(MonsterEntity.class)
public abstract class EntityMobMixin extends EntityLivingMixin {

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 additions
     * @author Aaro1011 - November 12, 2016 - Update for 1.11
     *
     * @reason Rewrite this to throw an {@link AttackEntityEvent} and process correctly.
     *
     * float f        | baseDamage
     * int i          | knockbackModifier
     * boolean flag   | attackSucceeded
     *
     * @param targetEntity The entity to attack
     * @return True if the attack was successful
     */
    @Overwrite
    public boolean attackEntityAsMob(Entity targetEntity) {
        // Sponge Start - Prepare our event values
        // float baseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        final double originalBaseDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
        final List<DamageFunction> originalFunctions = new ArrayList<>();
        // Sponge End
        int knockbackModifier = 0;

        if (targetEntity instanceof LivingEntity) {
            // Sponge Start - Gather modifiers
            originalFunctions.addAll(DamageEventHandler.createAttackEnchantmentFunction(this.getHeldItemMainhand(), ((LivingEntity) targetEntity).getCreatureAttribute(), 1.0F)); // 1.0F is for full attack strength since mobs don't have the concept
            // baseDamage += EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
            knockbackModifier += EnchantmentHelper.getKnockbackModifier((MonsterEntity) (Object) this);
        }

        // Sponge Start - Throw our event and handle appropriately
        final DamageSource damageSource = DamageSource.causeMobDamage((MonsterEntity) (Object) this);
        Sponge.getCauseStackManager().pushCause(damageSource);
        final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), originalFunctions,
            (org.spongepowered.api.entity.Entity) targetEntity, knockbackModifier, originalBaseDamage);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            return false;
        }
        knockbackModifier = event.getKnockbackModifier();
        // boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causeMobDamage(this), baseDamage);
        boolean attackSucceeded = targetEntity.attackEntityFrom(damageSource, (float) event.getFinalOutputDamage());
        // Sponge End
        if (attackSucceeded) {
            if (knockbackModifier > 0 && targetEntity instanceof LivingEntity) {
                ((LivingEntity) targetEntity).knockBack((MonsterEntity) (Object) this, (float) knockbackModifier * 0.5F, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier((MonsterEntity) (Object) this);

            if (j > 0) {
                targetEntity.setFire(j * 4);
            }

            if (targetEntity instanceof PlayerEntity) {
                PlayerEntity entityplayer = (PlayerEntity) targetEntity;
                ItemStack itemstack = this.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem() instanceof AxeItem && itemstack1.getItem() == Items.SHIELD) {
                    float f1 = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier((MonsterEntity) (Object) this) * 0.05F;

                    if (this.rand.nextFloat() < f1) {
                        entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                        this.world.setEntityState(entityplayer, (byte) 30);
                    }
                }
            }

            this.applyEnchantments((MonsterEntity) (Object) this, targetEntity);
        }

        return attackSucceeded;
    }

    /**
     * @author aikar - February 20th, 2017 - Optimizes light level check.
     * @author blood - February 20th, 2017 - Avoids checking unloaded chunks and chunks with pending light updates.
     *
     * @reason Avoids checking unloaded chunks and chunks with pending light updates.
     *
     * @return Whether current position has a valid light level for spawning
     */
    @Overwrite
    protected boolean isValidLightLevel()
    {
        final BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        final Chunk chunk = ((ChunkProviderBridge) this.world.getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(blockpos.getX() >> 4, blockpos.getZ() >> 4);
        if (chunk == null || !((ChunkBridge) chunk).bridge$isActive()) {
            return false;
        }

        if (this.world.getLightFor(LightType.SKY, blockpos) > this.rand.nextInt(32))
        {
            return false;
        } 
        else 
        {
            //int i = this.worldObj.getLightFromNeighbors(blockpos);
            boolean passes; // Sponge
            if (this.world.isThundering()) {
                int j = this.world.getSkylightSubtracted();;
                this.world.setSkylightSubtracted(10);
                passes = !((WorldServerBridge) this.world).bridge$isLightLevel(chunk, blockpos, this.rand.nextInt(9));
                this.world.setSkylightSubtracted(j);
            } else { 
                passes = !((WorldServerBridge) this.world).bridge$isLightLevel(chunk, blockpos, this.rand.nextInt(9));
            }

            return passes;
        }
    }
}
