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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.ticks.LevelChunkTicksBridge;
import org.spongepowered.common.bridge.world.ticks.TickNextTickDataBridge;

@Mixin(LevelChunkTicks.class)
public abstract class LevelChunkTicksMixin<T> implements LevelChunkTicksBridge<T> {

    private LevelTicks<T> impl$tickList;

    @Override
    public void bridge$setTickList(final LevelTicks<T> tickList) {
        this.impl$tickList = tickList;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "scheduleUnchecked", at = @At("HEAD"))
    private void impl$onScheduleUnchecked(final ScheduledTick<T> $$0, final CallbackInfo ci) {
        ((TickNextTickDataBridge<T>) (Object) $$0).bridge$createdByList(this.impl$tickList);
    }

    @SuppressWarnings("unchecked")
    @ModifyArg(method = "save(JLjava/util/function/Function;)Lnet/minecraft/nbt/ListTag;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ticks/SavedTick;saveTick(Lnet/minecraft/world/ticks/ScheduledTick;Ljava/util/function/Function;J)Lnet/minecraft/nbt/CompoundTag;"))
    private ScheduledTick<T> impl$onSaveSkipCancelled(final ScheduledTick<T> $$0) {
        final ScheduledUpdate.State state = ((TickNextTickDataBridge<T>) (Object) $$0).bridge$internalState();
        if (state != ScheduledUpdate.State.CANCELLED) {
            return $$0;
        }
        return null;
    }

    @Redirect(method = "save(JLjava/util/function/Function;)Lnet/minecraft/nbt/ListTag;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;add(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean impl$onSaveSkipCancelled2(final ListTag instance, final Object o) {
        if (o != null) {
            return instance.add((CompoundTag) o);
        }
        return false;
    }
}
