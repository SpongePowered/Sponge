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
package org.spongepowered.common.mixin.core.world.ticks;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.ticks.LevelChunkTicksBridge;
import org.spongepowered.common.bridge.world.ticks.LevelTicksBridge;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Iterator;

@Mixin(LevelChunkTicks.class)
public abstract class LevelChunkTicksMixin<T> implements LevelChunkTicksBridge<T> {

    private @MonotonicNonNull LevelTicks<T> impl$tickList;

    @Override
    public void bridge$setTickList(final LevelTicks<T> tickList) {
        this.impl$tickList = tickList;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "scheduleUnchecked", at = @At("HEAD"))
    private void impl$onScheduleUnchecked(final ScheduledTick<T> tick, final CallbackInfo ci) {
        final ServerLevel level = ((LevelTicksBridge<?>) this.impl$tickList).bridge$level();
        final PhaseContext<?> context = PhaseTracker.getWorldInstance(level).getPhaseContext();
        context.associateScheduledTickUpdate(level, tick);
        context.applyOwnerIfAvailable(owner -> ((CreatorTrackedBridge) (Object) tick).tracker$setTrackedUUID(PlayerTracker.Type.CREATOR, owner));
        context.applyNotifierIfAvailable(notified -> ((CreatorTrackedBridge) (Object) tick).tracker$setTrackedUUID(PlayerTracker.Type.NOTIFIER, notified));
        ((TickNextTickDataBridge<T>) (Object) tick).bridge$createdByList(level, (LevelChunkTicks) (Object) this);
    }

    @ModifyExpressionValue(method = "pack(J)Ljava/util/List;",
        at = @At(value = "INVOKE", target = "Ljava/util/Queue;iterator()Ljava/util/Iterator;"))
    private Iterator<ScheduledTick<T>> impl$onSaveSkipCancelled(final Iterator<ScheduledTick<T>> original) {
        return Iterators.filter(original, t -> ((TickNextTickDataBridge<T>) (Object) t).bridge$internalState() != ScheduledUpdate.State.CANCELLED);
    }

    @WrapOperation(method = "unpack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ticks/SavedTick;unpack(JJ)Lnet/minecraft/world/ticks/ScheduledTick;"))
    private ScheduledTick<T> impl$onUnpackTransferSpongeData(final SavedTick<T> instance, final long $$0, final long $$1, final Operation<ScheduledTick<T>> original) {
        final ScheduledTick<T> tick = original.call(instance, $$0, $$1);
        ((SpongeDataHolderBridge) (Object) tick).bridge$mergeDeserialized(((SpongeDataHolderBridge) (Object) instance).bridge$getManipulator());
        return tick;
    }
}
