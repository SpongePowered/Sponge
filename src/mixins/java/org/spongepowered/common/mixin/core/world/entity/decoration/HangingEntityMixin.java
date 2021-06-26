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
package org.spongepowered.common.mixin.core.world.entity.decoration;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import java.util.ArrayList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.HangingEntity;

@Mixin(HangingEntity.class)
public abstract class HangingEntityMixin extends EntityMixin {

    // @formatter:off
    @Shadow public abstract boolean shadow$survives();
    // @formatter:on

    private boolean impl$ignorePhysics = false; // TODO missing data key?

    /**
     * Called to update the entity's position/logic.
     */
    @Redirect(method = "tick()V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/HangingEntity;survives()Z"))
    private boolean impl$checkIfOnValidSurfaceAndIgnoresPhysics(final HangingEntity entityHanging) {
        return this.shadow$survives() && !this.impl$ignorePhysics;
    }

    @Inject(method = "hurt",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/HangingEntity;remove()V"
        ),
        cancellable = true
    )
    private void impl$postEventOnAttackEntityFrom(final DamageSource source, final float amount,
        final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.currentCause(),
                (Entity) this, new ArrayList<>(), 0, amount);
            SpongeCommon.post(event);
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
    /*
    @Override
    @Overwrite
    public ItemEntity entityDropItem(final ItemStack stack, final float offsetY) {
        // Sponge Start - Check for client worlds,, don't care about them really. If it's server world, then we care.
        final double xOffset = ((float) this.facingDirection.getXOffset() * 0.15F);
        final double zOffset = ((float) this.facingDirection.getZOffset() * 0.15F);
        if (((WorldBridge) this.world).bridge$isFake()) {
            // Sponge End
            final ItemEntity entityitem = new ItemEntity(this.world, this.posX + xOffset, this.posY + (double) offsetY, this.posZ + zOffset, stack);
            entityitem.setDefaultPickupDelay();
            this.world.addEntity(entityitem);
            return entityitem;
        }
        // Sponge - redirect server sided logic to sponge to handle cause stacks and phase states
        return EntityUtil.entityOnDropItem((HangingEntity) (Object) this, stack, offsetY, this.posX + xOffset, this.posZ + zOffset);
    }
*/
}
