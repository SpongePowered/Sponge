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
package org.spongepowered.server.mixin.core.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(World.class)
public abstract class WorldMixin_Vanilla {

    @Shadow protected WorldInfo worldInfo;

    @Shadow public abstract long getTotalWorldTime();

    @ModifyArg(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V"))
    int vanillaImpl$updateRainTimeStart(final int newRainTime) {
        return newRainTime;
    }

    @ModifyArg(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V"))
    int vanillaImpl$updateThunderTimeStart(final int newThunderTime) {
        return newThunderTime;
    }

    // This would be in common, but Forge rewrites isBlockLoaded(BlockPos) to isBlockLoaded(BlockPos,boolean)....
    @Redirect(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;isInvalid()Z", ordinal = 0),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z")
        )
    )
    @Group(name = "isBlockLoadedTargetingUpdateEntities", min = 1)
    private boolean impl$useTileActiveChunk(final World world, final BlockPos pos) {
        return true; // If we got to here, we already have the method `bridge$shouldTick()` passing
    }
}
