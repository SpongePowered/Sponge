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
import net.minecraft.block.BlockCactus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableGrowthData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeGrowthData;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;

import java.util.Optional;

@Mixin(BlockCactus.class)
public abstract class BlockCactusMixin extends BlockMixin {

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "onEntityCollision",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean impl$reAssignForBlockDamageSource(final Entity entity, final DamageSource source, final float damage,
        final net.minecraft.world.World world, final BlockPos pos, final IBlockState state, final Entity entityIn) {
        if (world.field_72995_K) {
            return entity.func_70097_a(source, damage);
        }
        try {
            final Location<World> location = new Location<>((World) world, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
            final MinecraftBlockDamageSource cactus = new MinecraftBlockDamageSource("cactus", location);
            ((DamageSourceBridge) cactus).bridge$setCactusSource();
            return entity.func_70097_a(DamageSource.field_76367_g, damage);
        } finally {
            ((DamageSourceBridge) source).bridge$setCactusSource();
        }
    }

    @SuppressWarnings("RedundantTypeArguments")
    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getGrowthData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableGrowthData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableGrowthData) {
            final int growth = ((ImmutableGrowthData) manipulator).growthStage().get();
            return Optional.of((BlockState) blockState.func_177226_a(BlockCactus.field_176587_a, growth));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.GROWTH_STAGE)) {
            final int growth = (Integer) value;
            return Optional.of((BlockState) blockState.func_177226_a(BlockCactus.field_176587_a, growth));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableGrowthData impl$getGrowthData(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeGrowthData.class, blockState.func_177229_b(BlockCactus.field_176587_a), 0, 15);
    }

}
