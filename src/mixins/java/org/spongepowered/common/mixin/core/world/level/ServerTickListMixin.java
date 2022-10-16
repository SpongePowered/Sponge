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
package org.spongepowered.common.mixin.core.world.level;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.world.level.TickNextTickDataBridge;

import java.util.Iterator;
import java.util.Queue;

@Mixin(ServerTickList.class)
public abstract class ServerTickListMixin<T> {

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Inject(
        method = "addTickData",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;add(Ljava/lang/Object;)Z",
            remap = false
        )
    )
    private void impl$associateScheduledTickData(TickNextTickData<T> param0, CallbackInfo ci) {
        ((TickNextTickDataBridge<T>) param0).bridge$createdByList((ServerTickList<T>) (Object) this);
    }


    /*
    DO NOT REORDER THIS INJECT AFTER THE REDIRECT ON QUEUE;ADD DUE TO MIXIN BUG
    https://github.com/SpongePowered/Mixin/issues/493
     */
    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
            remap = false,
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void impl$markDataAsCompleted(CallbackInfo ci, int var0, ServerChunkCache var1, Iterator var2, TickNextTickData var4) {
        ((TickNextTickDataBridge<T>) var4).bridge$setState(ScheduledUpdate.State.FINISHED);
    }

    /**
     * A more streamlined solution to filtering without having to necessarily
     * deal with removing the data from both sets individually and potentially
     * cause an invariant. Since this means that the data can be cancelled
     * without removing from the set, we effectively need to just filter it here.
     * But the scheduled update will still end up being removed from the schedule
     *
     * @param queue The currentlyTicking queue to be ticked
     * @param data The next tick data to tick
     * @return False if the data was marked as cancelled
     */
    @SuppressWarnings("unchecked")
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z",
            remap = false
        )
    )
    private boolean impl$validateHasNextUncancelled(Queue<TickNextTickData<T>> queue, Object data) {
        final ScheduledUpdate.State state = ((TickNextTickDataBridge<T>) data).bridge$internalState();
        if (state == ScheduledUpdate.State.CANCELLED) {
            return false;
        }
        return queue.add((TickNextTickData<T>) data);
    }

    //endregion

}
