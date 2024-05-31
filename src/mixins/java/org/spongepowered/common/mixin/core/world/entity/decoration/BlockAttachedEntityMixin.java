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

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.DamageEventUtil;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin extends EntityMixin {
    // @formatter:off
    @Shadow public abstract boolean shadow$survives();
    // @formatter:on

    private boolean impl$ignorePhysics = false; // TODO missing data key?

    /**
     * Called to update the entity's position/logic.
     */
    @Redirect(method = "tick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/decoration/BlockAttachedEntity;survives()Z"))
    private boolean impl$checkIfOnValidSurfaceAndIgnoresPhysics(final BlockAttachedEntity entityHanging) {
        return this.shadow$survives() && !this.impl$ignorePhysics;
    }

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/decoration/BlockAttachedEntity;kill()V"))
    private void attackImpl$postEventOnAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        if (DamageEventUtil.callOtherAttackEvent((Entity) (Object) this, source, amount).isCancelled()) {
            cir.setReturnValue(true);
        }
    }

}
