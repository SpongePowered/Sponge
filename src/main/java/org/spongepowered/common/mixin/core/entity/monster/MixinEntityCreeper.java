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

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityCreeper.class)
public abstract class MixinEntityCreeper extends MixinEntityMob implements Creeper {

    @Shadow private int timeSinceIgnited;
    @Shadow private int fuseTime;
    @Shadow public abstract void explode();
    @Override
    @Shadow public abstract void ignite();
    // Context
    @Nullable private Entity igniter;
    @Nullable private ItemStack stack;
    private boolean wasEventCancelled = false;

    public void creeper$detonate() {
        this.explode();
    }

    public void creeper$ignite() {
        this.ignite();
    }

    public void creeper$ignite(int fuseTicks) {
        this.timeSinceIgnited = 0;
        this.fuseTime = fuseTicks;
        this.ignite();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntityCreeper;playSound(Ljava/lang/String;FF)V"))
    public void processUpdateCancellation(EntityCreeper creeper, String name, float volume, float pitch) {
        List<Object> cause = Lists.newArrayList();

        if (this.igniter != null) {
            cause.add(NamedCause.igniter(this.igniter));
        }

        if (this.stack != null) {
            cause.add(NamedCause.igniter(this.stack));
        }

        cause.add(NamedCause.source(this));

        // Reset state
        this.igniter = null;
        this.stack = null;

        ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (World) this.worldObj);
        this.wasEventCancelled = SpongeImpl.postEvent(event);
        if (!this.wasEventCancelled) {
            creeper.playSound(name, volume, pitch);
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/monster/EntityCreeper;timeSinceIgnited:I",
            shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD, ordinal = 0))
    public void resetTimeSinceIgnited(CallbackInfo ci) {
        if (this.wasEventCancelled) {
            this.timeSinceIgnited = 0;
            this.wasEventCancelled = false;
        }
    }

    @Inject(method = "interact(Lnet/minecraft/entity/player/EntityPlayer;)Z", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/entity/monster/EntityCreeper;ignite()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void captureExplosionPrimer(EntityPlayer player, CallbackInfoReturnable<Boolean> cir, ItemStack stack) {
        this.igniter = player;
        this.stack = stack;
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)"
            + "Lnet/minecraft/world/Explosion;", shift = At.Shift.BEFORE), cancellable = true)
    public void processExplosion(CallbackInfo ci) {
        List<Object> cause = Lists.newArrayList();

        if (this.igniter != null) {
            cause.add(NamedCause.igniter(this.igniter));
        }

        if (this.stack != null) {
            cause.add(NamedCause.igniter(this.stack));
        }

        cause.add(NamedCause.source(this));

        // Reset state
        this.igniter = null;
        this.stack = null;

        ExplosionEvent.Pre event = SpongeEventFactory.createExplosionEventPre(Cause.of(cause), (World) this.worldObj);
        if (SpongeImpl.postEvent(event)) {
            this.timeSinceIgnited = 0;
            ci.cancel();
        }
    }

}
