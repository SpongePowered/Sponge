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
package org.spongepowered.common.mixin.tracker.server;

import net.minecraft.crash.CrashReport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.util.concurrent.TrackedTickDelayedTaskBridge;
import org.spongepowered.common.event.tracking.CauseTrackerCrashHandler;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.mixin.tracker.util.concurrent.ThreadTaskExecutorMixin_Tracker;

import java.util.function.BooleanSupplier;

@SuppressWarnings("rawtypes")
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Tracker extends ThreadTaskExecutorMixin_Tracker {

    @Shadow public abstract boolean shadow$isServerStopped();

    @Shadow protected abstract void shadow$updateTimeLightAndEntities(BooleanSupplier hasTimeLeft);

    @Inject(method = "addServerInfoToCrashReport", at = @At("RETURN"), cancellable = true)
    private void tracker$addPhaseTrackerToCrashReport(final CrashReport report, final CallbackInfoReturnable<CrashReport> cir) {
        report.makeCategory("Sponge PhaseTracker").addDetail("Phase Stack", CauseTrackerCrashHandler.INSTANCE);
        cir.setReturnValue(report);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tracker$ensurePhaseTrackerEmpty(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        PhaseTracker.SERVER.ensureEmpty();
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities(Ljava/util/function/BooleanSupplier;)V"
        )
    )
    private void tracker$wrapUpdateTimeLightAndEntities(final MinecraftServer minecraftServer, final BooleanSupplier hasTimeLeft) {
        try (
            final PhaseContext<@NonNull ?> context = TickPhase.Tick.SERVER_TICK
                .createPhaseContext(PhaseTracker.SERVER)
                .server(minecraftServer)
        ) {
            context.buildAndSwitch();
            this.shadow$updateTimeLightAndEntities(hasTimeLeft);
        }
    }

    @Redirect(
        method = "updateTimeLightAndEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/server/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"
        )
    )
    private void tracker$wrapWorldTick(final ServerWorld serverWorld, final BooleanSupplier hasTimeLeft) {
        try (
            final PhaseContext<@NonNull ?> context = TickPhase.Tick.WORLD_TICK
                .createPhaseContext(PhaseTracker.SERVER)
                .world(serverWorld)
        ) {
            context.buildAndSwitch();
            serverWorld.tick(hasTimeLeft);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "wrapTask", at = @At("RETURN"))
    private void tracker$associatePhaseContextWithWrappedTask(final Runnable runnable, final CallbackInfoReturnable<TickDelayedTask> cir) {
        final TickDelayedTask returnValue = cir.getReturnValue();
        if (!PhaseTracker.SERVER.onSidedThread()) {
            final PhaseContext<@NonNull ?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
            if (phaseContext.isEmpty()) {
                return;
            }
            phaseContext.foldContextForThread(((TrackedTickDelayedTaskBridge) returnValue));
        }
    }

    @Redirect(
        method = "run(Lnet/minecraft/util/concurrent/TickDelayedTask;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/concurrent/RecursiveEventLoop;run(Ljava/lang/Runnable;)V"
        )
    )
    @SuppressWarnings("unchecked")
    private void tracker$wrapAndPerformContextSwitch(final RecursiveEventLoop<?> thisServer, final Runnable runnable) {
        try (final PhaseContext<@NonNull ?> context = PluginPhase.State.DELAYED_TASK.createPhaseContext(PhaseTracker.SERVER)
            .source(runnable)
            .setDelayedContextPopulator(((TrackedTickDelayedTaskBridge) runnable).bridge$getFrameModifier().orElse(null))
        ) {
            context.buildAndSwitch();
            super.shadow$run(runnable);
        }
    }

    @Override
    protected boolean tracker$isServerAndIsServerStopped() {
        return this.shadow$isServerStopped();
    }

}
