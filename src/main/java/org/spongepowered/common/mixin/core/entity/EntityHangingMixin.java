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

import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.EntityUtil;

import java.util.ArrayList;

import javax.annotation.Nullable;

@Mixin(HangingEntity.class)
public abstract class EntityHangingMixin extends EntityMixin {

    @Shadow @Nullable public Direction facingDirection;
    @Shadow public abstract boolean onValidSurface();

    private boolean ignorePhysics = false;

    /**
     * Called to update the entity's position/logic.
     */
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;onValidSurface()Z"))
    private boolean checkIfOnValidSurfaceAndIgnoresPhysics(HangingEntity entityHanging) {
        return this.onValidSurface() && !this.ignorePhysics;
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(CompoundNBT compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        compound.func_74757_a("ignorePhysics", this.ignorePhysics);
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(CompoundNBT compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.func_74764_b("ignorePhysics")) {
            this.ignorePhysics = compound.func_74767_n("ignorePhysics");
        }
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;setDead()V"), cancellable = true)
    private void onAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), new ArrayList<>(), (Entity) this, 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * @author gabizou - April 19th, 2018
     * @reason Redirect the flow of logic to sponge for events and captures. Forge's compatibility is built in
     * to the implementation.
     */
    @Override
    @Overwrite
    public ItemEntity entityDropItem(ItemStack stack, float offsetY) {
        // Sponge Start - Check for client worlds,, don't care about them really. If it's server world, then we care.
        final double xOffset = ((float) this.facingDirection.func_82601_c() * 0.15F);
        final double zOffset = ((float) this.facingDirection.func_82599_e() * 0.15F);
        if (((WorldBridge) this.world).bridge$isFake()) {
            // Sponge End
            ItemEntity entityitem = new ItemEntity(this.world, this.posX + xOffset, this.posY + (double) offsetY, this.posZ + zOffset, stack);
            entityitem.func_174869_p();
            this.world.func_72838_d(entityitem);
            return entityitem;
        }
        // Sponge - redirect server sided logic to sponge to handle cause stacks and phase states
        return EntityUtil.entityOnDropItem((HangingEntity) (Object) this, stack, offsetY, this.posX + xOffset, this.posZ + zOffset);
    }

}
