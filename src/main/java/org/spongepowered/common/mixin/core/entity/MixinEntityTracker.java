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
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.living.human.EntityHuman;

@Mixin(EntityTracker.class)
public abstract class MixinEntityTracker {

    @Shadow
    public abstract void trackEntity(Entity entityIn, int trackingRange, int updateFrequency);

    @Inject(method = "trackEntity", at = @At("HEAD"), cancellable = true)
    public void onTrackEntity(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            this.trackEntity(entityIn, 512, 2);
            ci.cancel();
        }
    }

    @Inject(method = "addEntityToTracker", at = @At("HEAD"))
    public void onAddEntityToTracker(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates, CallbackInfo ci) {
        if (!MinecraftServer.getServer().isCallingFromMinecraftThread() ) {
            throw new IllegalStateException("Detected attempt to add entity "' + entityIn + '" to tracker asynchronously.\n"
                    + " This is very bad as it can cause ConcurrentModificationException's during server tick.");
        }
    }

    @Inject(method = "untrackEntity", at = @At("HEAD"))
    public void onUntrackEntity(Entity entityIn, CallbackInfo ci) {
        if (!MinecraftServer.getServer().isCallingFromMinecraftThread() ) {
            throw new IllegalStateException("Detected attempt to untrack entity "' + entityIn + '" asynchronously.\n"
                    + "This is very bad as it can cause ConcurrentModificationException's during server tick.");
        }
    }
}
