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
package org.spongepowered.common.mixin.timings;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.TimingHistory;
import co.aikar.timings.WorldTimingsHandler;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(World.class)
public class MixinWorld {

    // ESS - endStartSection
    private static final String ESS = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V";

    @Shadow @Final public boolean isRemote;
    @Shadow @Final public List<Entity> loadedEntityList;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;

    protected WorldTimingsHandler timings;

    @Inject(method = "<init>", at = @At("RETURN") )
    private void onInit(CallbackInfo ci) {
        this.timings = new WorldTimingsHandler((World) (Object) this);
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE_STRING", target = ESS, args = "ldc=remove", shift = At.Shift.AFTER) )
    private void onEntityRemovalBegin(CallbackInfo ci) {
        if (!this.isRemote) {
            this.timings.entityRemoval.startTiming();
        }
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE_STRING", target = ESS, args = "ldc=regular", shift = At.Shift.AFTER) )
    private void onEntityRemovalEnd(CallbackInfo ci) {
        if (!this.isRemote) {
            this.timings.entityRemoval.stopTiming();
            TimingHistory.entityTicks += this.loadedEntityList.size();
            this.timings.entityTick.startTiming();
        }
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V") )
    private void onBeginEntityTick(CallbackInfo ci) {
        if (!this.isRemote) {
            SpongeTimings.tickEntityTimer.startTiming();
            // TODO entity.tickTimer.startTiming();
        }
    }

    @Inject(method = "updateEntities", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER),
            @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;", ordinal = 1)})
    private void onEndEntityTick(CallbackInfo ci) {
        if (!this.isRemote) {
            SpongeTimings.tickEntityTimer.stopTiming();
            // TODO entity.tickTimer.stopTiming();
        }
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE_STRING", target = ESS, args = "ldc=blockEntities", shift = At.Shift.AFTER) )
    private void onTileEntityTickBegin(CallbackInfo ci) {
        if (!this.isRemote) {
            this.timings.entityTick.stopTiming();
            // TODO timings.tileEntityTick.startTiming();
        }
    }

    @Inject(method = "updateEntities", at = @At("RETURN") )
    private void addTileEntityTicks(CallbackInfo ci) {
        if (!this.isRemote) {
            TimingHistory.tileEntityTicks += this.loadedTileEntityList.size();
        }
    }

    @Inject(method = "updateEntityWithOptionalForce", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;ticksExisted:I", opcode = Opcodes.GETFIELD) )
    private void incrementActivatedEntityTicks(CallbackInfo ci) {
        if (!this.isRemote) {
            TimingHistory.activatedEntityTicks++;
        }
    }
}
