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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.DamageEventHandler;
import org.spongepowered.common.mixin.core.entity.MixinEntityCreature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(EntityMob.class)
public abstract class MixinEntityMob extends MixinEntityCreature implements Monster {

    /**
     * @author gabizou - April 8th, 2016
     * @reason Rewrite this to throw an {@link AttackEntityEvent}
     * and process correctly.
     *
     * @param targetEntity The entity to attack
     * @return True if the attack was successful
     */
    @Overwrite
    public boolean attackEntityAsMob(Entity targetEntity) {
        // Sponge Start - Prepare our event values
        // float baseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        final double originalBaseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        final List<Tuple<DamageModifier, Function<? super Double, Double>>> originalFunctions = new ArrayList<>();
        // Sponge End
        int knockbackModifier = 0;

        if (targetEntity instanceof EntityLivingBase) {
            // Sponge Start - Gather modifiers
            originalFunctions.addAll(DamageEventHandler.createAttackEnchamntmentFunction(this.getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute()));
            // baseDamage += EnchantmentHelper.getModifierForCreature(this.getHeldItem(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
            knockbackModifier += EnchantmentHelper.getKnockbackModifier((EntityMob) (Object) this);
        }

        // Sponge Start - Throw our event and handle appropriately
        final DamageSource damageSource = DamageSource.causeMobDamage((EntityMob) (Object) this);
        final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(Cause.source(damageSource).build(), originalFunctions,
                EntityUtil.fromNative(targetEntity), knockbackModifier, originalBaseDamage);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        knockbackModifier = event.getKnockbackModifier();
        // boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causeMobDamage(this), baseDamage);
        boolean attackSucceeded = targetEntity.attackEntityFrom(damageSource, (float) event.getFinalOutputDamage());
        // Sponge End
        if (attackSucceeded) {
            if (knockbackModifier > 0) {
                targetEntity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) knockbackModifier * 0.5F), 0.1D,
                        (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) knockbackModifier * 0.5F));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier((EntityMob) (Object) this);

            if (j > 0) {
                targetEntity.setFire(j * 4);
            }

            this.applyEnchantments((EntityMob) (Object) this, targetEntity);
        }

        return attackSucceeded;
    }

}
