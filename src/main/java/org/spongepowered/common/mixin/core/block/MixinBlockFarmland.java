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
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeMoistureData;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(BlockFarmland.class)
public abstract class MixinBlockFarmland extends MixinBlock {

    @Nullable private Entity currentGriefer;
    @Shadow private void turnToDirt(World world, BlockPos pos) {}

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getMoistureData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableMoistureData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableMoistureData) {
            int moisture = ((ImmutableMoistureData) manipulator).moisture().get();
            if (moisture > 7) {
                moisture = 7;
            }
            return Optional.of((BlockState) blockState.withProperty(BlockFarmland.MOISTURE, moisture));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.MOISTURE)) {
            int moisture = (Integer) value;
            if (moisture > 7) {
                moisture = 7;
            }
            return Optional.of((BlockState) blockState.withProperty(BlockFarmland.MOISTURE, moisture));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private ImmutableMoistureData getMoistureData(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeMoistureData.class, blockState.getValue(BlockFarmland.MOISTURE), 0, 7);
    }

    @Inject(method = "onFallenUpon", at = @At(value = "HEAD"))
    private void onFallenUponHead(World worldIn, BlockPos pos, Entity entityIn, float fallDistance, CallbackInfo ci) {
        this.currentGriefer = entityIn;
    }

    @Inject(method = "onFallenUpon", at = @At(value = "RETURN"))
    private void onEntityFallenUponReturn(World worldIn, BlockPos pos, Entity entityIn, float fallDistance, CallbackInfo ci) {
        this.currentGriefer = null;
    }

    @Redirect(method = "onFallenUpon", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockFarmland;turnToDirt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void beforeTurnToDirt(BlockFarmland block, World world, BlockPos pos) {
        if (this.currentGriefer instanceof IMixinGriefer && ((IMixinGriefer) this.currentGriefer).canGrief()) {
            this.turnToDirt(world, pos);
        }
    }
}
