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
import net.minecraft.entity.EntityTracker;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

@Mixin(EntityTracker.class)
public abstract class MixinEntityTracker {

    @Shadow @Final private WorldServer theWorld;

    @Shadow
    public abstract void trackEntity(Entity entityIn, int trackingRange, int updateFrequency);

    @Inject(method = "trackEntity", at = @At("HEAD"), cancellable = true)
    public void onTrackEntity(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            this.trackEntity(entityIn, 512, 2);
            ci.cancel();
        }
    }


    @Redirect(method = "addEntityToTracker", at = @At(value = "NEW", args = "class=java/lang/IllegalStateException"))
    private IllegalStateException reportEntityAlreadyTrackedWithWorld(String string, Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates) {
        IllegalStateException exception = new IllegalStateException(String.format("Entity %s is already tracked for world: %s", entityIn, ((World) this.theWorld).getName()));;
        if (CauseTracker.ENABLED && CauseTracker.getInstance().verboseErrors) {
            CauseTracker.getInstance().printMessageWithCaughtException("Exception tracking entity", "An entity that was already tracked was added to the tracker!", exception);
        }
        return exception;
    }

    @Inject(method = "addEntityToTracker", at = @At("HEAD"), cancellable = true)
    public void onAddEntityToTracker(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates, CallbackInfo ci) {
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread() ) {
            Thread.dumpStack();
            SpongeImpl.getLogger().error("Detected attempt to add entity '" + entityIn + "' to tracker asynchronously.\n"
                    + " This is very bad as it can cause ConcurrentModificationException's during a server tick.\n"
                    + " Skipping...");
            ci.cancel();
        }
    }

    @Inject(method = "untrackEntity", at = @At("HEAD"), cancellable = true)
    public void onUntrackEntity(Entity entityIn, CallbackInfo ci) {
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread() ) {
            Thread.dumpStack();
            SpongeImpl.getLogger().error("Detected attempt to untrack entity '" + entityIn + "' asynchronously.\n"
                    + "This is very bad as it can cause ConcurrentModificationException's during a server tick.\n"
                    + " Skipping...");
            ci.cancel();
        }
    }
}
