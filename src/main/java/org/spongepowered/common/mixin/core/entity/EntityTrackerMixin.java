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
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(EntityTracker.class)
public abstract class EntityTrackerMixin {

    @Shadow @Final private ServerWorld world;

    @Shadow
    public abstract void track(Entity entityIn, int trackingRange, int updateFrequency);

    @Inject(method = "track(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void impl$onTrackEntity(final Entity entityIn, final CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            this.track(entityIn, 512, 2);
            ci.cancel();
        }
    }


    @Redirect(method = "track(Lnet/minecraft/entity/Entity;IIZ)V",
        at = @At(value = "NEW", args = "class=java/lang/IllegalStateException", remap = false))
    private IllegalStateException impl$reportEntityAlreadyTrackedWithWorld(final String string, final Entity entityIn, final int trackingRange
        , final int updateFrequency, final boolean sendVelocityUpdates) {
        final IllegalStateException exception = new IllegalStateException(String.format("Entity %s is already tracked for world: %s", entityIn, ((World) this.world).getName()));;
        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().verboseErrors()) {
            PhaseTracker.getInstance().printMessageWithCaughtException("Exception tracking entity", "An entity that was already tracked was added to the tracker!", exception);
        }
        return exception;
    }

    @Inject(method = "track(Lnet/minecraft/entity/Entity;IIZ)V", at = @At("HEAD"), cancellable = true)
    private void onAddEntityToTracker(final Entity entityIn, final int trackingRange, final int updateFrequency, final boolean sendVelocityUpdates,
        final CallbackInfo ci) {
        if (!SpongeImpl.getServer().isServerStopped() && !SpongeImpl.getServer().func_152345_ab() ) {
            Thread.dumpStack();
            SpongeImpl.getLogger().error("Detected attempt to add entity '" + entityIn + "' to tracker asynchronously.\n"
                    + " This is very bad as it can cause ConcurrentModificationException's during a server tick.\n"
                    + " Skipping...");
            ci.cancel();
        }
    }

    @Inject(method = "untrack", at = @At("HEAD"), cancellable = true)
    private void impl$onUntrackEntity(final Entity entityIn, final CallbackInfo ci) {
        if (!SpongeImpl.getServer().isServerStopped() && !SpongeImpl.getServer().func_152345_ab() ) {
            Thread.dumpStack();
            SpongeImpl.getLogger().error("Detected attempt to untrack entity '" + entityIn + "' asynchronously.\n"
                    + "This is very bad as it can cause ConcurrentModificationException's during a server tick.\n"
                    + " Skipping...");
            ci.cancel();
        }
    }

}
