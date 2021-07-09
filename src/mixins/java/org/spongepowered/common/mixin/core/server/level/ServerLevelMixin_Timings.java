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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.world.level.entity.EntityTickListAccessor;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.mixin.core.world.level.LevelMixin_Timings;
import co.aikar.timings.sponge.TimingHistory;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_Timings extends LevelMixin_Timings implements ServerLevelBridge {

    // @formatter:off
    @Shadow @Final private EntityTickList entityTickList;
    // @formatter:on

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void impl$startWorldTimings(BooleanSupplier var1, CallbackInfo ci) {
        this.bridge$getTimingsHandler().tick.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=blockEvents"))
    protected void impl$startScheduledBlockTimings(BooleanSupplier param0, CallbackInfo ci) {
        this.bridge$getTimingsHandler().scheduledBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=entities"))
    private void impl$startEntityGlobalTimings(BooleanSupplier var1, CallbackInfo ci) {
        this.bridge$getTimingsHandler().scheduledBlocks.stopTiming();
        this.bridge$getTimingsHandler().tickEntities.startTiming();
        TimingHistory.entityTicks += ((EntityTickListAccessor) this.entityTickList).accessor$active().size();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tickBlockEntities()V"))
    protected void impl$startBlockEntitiesTimings(BooleanSupplier param0, CallbackInfo ci) {
        this.bridge$getTimingsHandler().tickEntities.stopTiming();
        this.bridge$getTimingsHandler().blockEntityTick.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tickBlockEntities()V", shift = At.Shift.AFTER))
    private void impl$stopBlockEntitiesTimings(BooleanSupplier param0, CallbackInfo ci) {
        this.bridge$getTimingsHandler().blockEntityTick.stopTiming();
        TimingHistory.blockEntityTicks += this.blockEntityTickers.size();
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V"))
    protected void impl$startEntityRemovalTimings(final CallbackInfo ci) {
        this.bridge$getTimingsHandler().entityRemoval.startTiming();
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V", shift = At.Shift.AFTER))
    protected void impl$stopEntityRemovalTimings(final CallbackInfo ci) {
        this.bridge$getTimingsHandler().entityRemoval.stopTiming();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void impl$stopWorldTimings(BooleanSupplier var1, CallbackInfo ci) {
        this.bridge$getTimingsHandler().tick.stopTiming();
    }
}
