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
package org.spongepowered.common.mixin.core.entity.item;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.api.entity.vehicle.minecart.MinecartTNT;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityMinecartTNT.class)
public abstract class MixinEntityMinecartTNT extends MixinEntityMinecart implements MinecartTNT {

    @Nullable private Entity shooter;

    @Inject(method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/item/EntityMinecartTNT;explodeCart(D)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void processAttackEntityFromCauseCapture(CallbackInfo ci, EntityArrow arrow) {
        this.shooter = arrow;
    }

    @Inject(method = "explodeCart(D)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)"
            + "Lnet/minecraft/world/Explosion;", shift = At.Shift.BEFORE), cancellable = true)
    public void processExplosion(CallbackInfo ci) {
        List<Object> cause = Lists.newArrayList();

        if (this.shooter != null) {
            cause.add(NamedCause.igniter(this.shooter));
        }

        cause.add(NamedCause.source(this));

        // Reset state
        this.shooter = null;

        ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (World) this.worldObj);
        if (SpongeImpl.postEvent(event)) {
            ci.cancel();
        }
    }

}
