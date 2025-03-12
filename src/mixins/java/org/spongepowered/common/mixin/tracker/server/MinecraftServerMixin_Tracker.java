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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.SystemReport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.server.TickTaskBridge;
import org.spongepowered.common.event.tracking.CauseTrackerCrashHandler;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.mixin.tracker.util.thread.BlockableEventLoopMixin_Tracker;

import java.util.function.BooleanSupplier;

@SuppressWarnings("rawtypes")
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Tracker extends BlockableEventLoopMixin_Tracker {

    // @formatter:off
    @Shadow public abstract boolean shadow$isStopped();
    @Shadow public abstract void shadow$tickChildren(BooleanSupplier hasTimeLeft);
    // @formatter:on

    @Inject(method = "fillSystemReport", at = @At("RETURN"), cancellable = true)
    private void tracker$addPhaseTrackerToCrashReport(final SystemReport category, final CallbackInfoReturnable<SystemReport> cir) {
        category.setDetail("Sponge PhaseTracker", CauseTrackerCrashHandler.INSTANCE::call);
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void tracker$ensurePhaseTrackerEmpty(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        PhaseTracker.getServerInstanceExplicitly().ensureEmpty();
    }

    @Redirect(
        method = "tickServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;tickChildren(Ljava/util/function/BooleanSupplier;)V"
        )
    )
    private void tracker$wrapUpdateTimeLightAndEntities(final MinecraftServer minecraftServer, final BooleanSupplier hasTimeLeft) {
        try (
            final PhaseContext<@NonNull ?> context = TickPhase.Tick.SERVER_TICK
                .createPhaseContext(PhaseTracker.getServerInstanceExplicitly())
                .server(minecraftServer)
        ) {
            context.buildAndSwitch();
            this.shadow$tickChildren(hasTimeLeft);
        }
    }

    @Redirect(
        method = "tickChildren",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;tick(Ljava/util/function/BooleanSupplier;)V"
        )
    )
    private void tracker$wrapWorldTick(final ServerLevel serverWorld, final BooleanSupplier hasTimeLeft) {
        try (
            final PhaseContext<@NonNull ?> context = TickPhase.Tick.WORLD_TICK
                .createPhaseContext(PhaseTracker.getWorldInstance(serverWorld))
                .world(serverWorld)
        ) {
            context.buildAndSwitch();
            serverWorld.tick(hasTimeLeft);
        }
    }

    @Inject(method = "wrapRunnable(Ljava/lang/Runnable;)Lnet/minecraft/server/TickTask;", at = @At("RETURN"))
    private void tracker$associatePhaseContextWithWrappedTask(final Runnable runnable, final CallbackInfoReturnable<TickTask> cir) {        final TickTask returnValue = cir.getReturnValue();
        if (!PhaseTracker.getServerInstanceExplicitly().onSidedThread()) {
            final PhaseContext<@NonNull ?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
            if (phaseContext.isEmpty()) {
                return;
            }
            phaseContext.foldContextForThread(((TickTaskBridge) returnValue));
        }
    }

    @WrapMethod(method = "doRunTask(Lnet/minecraft/server/TickTask;)V")
    private void tracker$wrapAndPerformContextSwitch(final TickTask task, final Operation<Void> original) {
        try (final PhaseContext<@NonNull ?> context = PluginPhase.State.DELAYED_TASK.createPhaseContext(PhaseTracker.getServerInstanceExplicitly())
            .source(task)
            .setDelayedContextPopulator(((TickTaskBridge) task).bridge$getFrameModifier().orElse(null))
        ) {
            context.buildAndSwitch();
            original.call(task);
        }
    }

    @Override
    protected boolean tracker$isServerAndIsServerStopped() {
        return this.shadow$isStopped();
    }

}
