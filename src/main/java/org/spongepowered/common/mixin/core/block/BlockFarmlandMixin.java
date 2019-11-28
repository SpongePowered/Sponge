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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableMoistureData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeMoistureData;

import java.util.Optional;

@Mixin(FarmlandBlock.class)
public abstract class BlockFarmlandMixin extends BlockMixin {

    @Shadow protected static void turnToDirt(final World world, final BlockPos pos) {}

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getMoistureData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableMoistureData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableMoistureData) {
            int moisture = ((ImmutableMoistureData) manipulator).moisture().get();
            if (moisture > 7) {
                moisture = 7;
            }
            return Optional.of((BlockState) blockState.withProperty(FarmlandBlock.MOISTURE, moisture));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.MOISTURE)) {
            int moisture = (Integer) value;
            if (moisture > 7) {
                moisture = 7;
            }
            return Optional.of((BlockState) blockState.withProperty(FarmlandBlock.MOISTURE, moisture));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableMoistureData impl$getMoistureData(final net.minecraft.block.BlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeMoistureData.class, blockState.get(FarmlandBlock.MOISTURE), 0, 7);
    }

    @Redirect(method = "onFallenUpon",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockFarmland;turnToDirt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
        )
    )
    private void impl$CheckIfGrieferCanGrief(final World world, final BlockPos pos, final World worldIn, final BlockPos samePos,
        final Entity entityIn, final float fallDistance) {
        if (entityIn instanceof GrieferBridge && ((GrieferBridge) entityIn).bridge$CanGrief()) {
            turnToDirt(world, pos);
        }
    }
}
