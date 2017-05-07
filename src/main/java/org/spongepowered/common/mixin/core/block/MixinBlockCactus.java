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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableGrowthData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeGrowthData;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(BlockCactus.class)
public abstract class MixinBlockCactus extends MixinBlock {

    private static final String CACTUS_DAMAGE_FIELD = "Lnet/minecraft/util/DamageSource;CACTUS:Lnet/minecraft/util/DamageSource;";

    @Nullable private DamageSource originalCactus;

    @Inject(method = "onEntityCollidedWithBlock", at = @At(value = "FIELD", target = CACTUS_DAMAGE_FIELD, opcode = Opcodes.GETSTATIC))
    public void preSetOnFire(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo callbackInfo) {
        if (!worldIn.isRemote) {
            this.originalCactus = DamageSource.CACTUS;
            Location<World> location = new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ());
            DamageSource.CACTUS = new MinecraftBlockDamageSource("cactus", location).setFireDamage();
        }
    }

    @Inject(method = "onEntityCollidedWithBlock", at = @At(value = "FIELD", target = CACTUS_DAMAGE_FIELD,
            opcode = Opcodes.GETSTATIC, shift = At.Shift.AFTER))
    public void postSetOnFire(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo callbackInfo) {
        if (!worldIn.isRemote) {
            if (this.originalCactus == null) {
                SpongeImpl.getLogger().error("Original cactus is null!");
                Thread.dumpStack();
            }
            DamageSource.CACTUS = this.originalCactus;
        }
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getGrowthData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableGrowthData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableGrowthData) {
            int growth = ((ImmutableGrowthData) manipulator).growthStage().get();
            return Optional.of((BlockState) blockState.withProperty(BlockCactus.AGE, growth));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.GROWTH_STAGE)) {
            int growth = (Integer) value;
            return Optional.of((BlockState) blockState.withProperty(BlockCactus.AGE, growth));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private ImmutableGrowthData getGrowthData(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeGrowthData.class, blockState.getValue(BlockCactus.AGE), 0, 15);
    }

}
