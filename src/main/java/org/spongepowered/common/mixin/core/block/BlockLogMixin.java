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
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.LogBlock;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableLogAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.data.type.WoodTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeLogAxisData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;
import org.spongepowered.common.registry.type.block.TreeTypeRegistryModule;

import java.util.List;
import java.util.Optional;

@Mixin(LogBlock.class)
public abstract class BlockLogMixin extends BlockMixin {

    private ImmutableTreeData getTreeData(final net.minecraft.block.BlockState blockState) {
        final BlockPlanks.EnumType type;
        if(blockState.getBlock() instanceof BlockOldLog) {
            type = blockState.get(BlockOldLog.VARIANT);
        } else if(blockState.getBlock() instanceof BlockNewLog) {
            type = blockState.get(BlockNewLog.VARIANT);
        } else {
            type = BlockPlanks.EnumType.OAK;
        }

        final WoodType treeType = TreeTypeRegistryModule.getFor(type);

        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeTreeData.class, treeType);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableLogAxisData getLogAxisData(final net.minecraft.block.BlockState blockState) {
        final LogAxis logAxis = (LogAxis) (Object) blockState.get(LogBlock.LOG_AXIS);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeLogAxisData.class, logAxis);
    }


    @Override
    public boolean bridge$supports(final Class<? extends Immutable<?, ?>> immutable) {
        return ImmutableTreeData.class.isAssignableFrom(immutable) || ImmutableLogAxisData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final Immutable<?, ?> manipulator) {
        if (manipulator instanceof ImmutableTreeData) {
            final WoodType treeType = ((ImmutableTreeData) manipulator).type().get();
            final BlockPlanks.EnumType type = TreeTypeRegistryModule.getFor(treeType);
            return this.impl$processLogType(blockState, type, treeType);
        }
        if (manipulator instanceof ImmutableLogAxisData) {
            final LogAxis logAxis = ((ImmutableLogAxisData) manipulator).type().get();
            return Optional.of((BlockState) blockState.withProperty(LogBlock.LOG_AXIS, (LogBlock.EnumAxis) (Object) logAxis));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends Value<E>> key, final E value) {
        if (key.equals(Keys.TREE_TYPE)) {
            final WoodType treeType = (WoodType) value;
            final BlockPlanks.EnumType type = TreeTypeRegistryModule.getFor(treeType);
            return this.impl$processLogType(blockState, type, treeType);
        } else if (key.equals(Keys.LOG_AXIS)) {
            return Optional.of((BlockState) blockState.withProperty(LogBlock.LOG_AXIS, (LogBlock.EnumAxis) value));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private Optional<BlockState> impl$processLogType(final net.minecraft.block.BlockState blockState, final BlockPlanks.EnumType type, final WoodType treeType) {
        if (blockState.getBlock() instanceof BlockOldLog) {
            if (treeType.equals(WoodTypes.OAK) ||
                treeType.equals(WoodTypes.BIRCH) ||
                treeType.equals(WoodTypes.SPRUCE) ||
                treeType.equals(WoodTypes.JUNGLE)) {
                return Optional.of((BlockState) blockState.withProperty(BlockOldLog.VARIANT, type));
            }
        } else if (blockState.getBlock() instanceof BlockNewLog) {
            if (treeType.equals(WoodTypes.ACACIA) || treeType.equals(WoodTypes.DARK_OAK)) {
                return Optional.of((BlockState) blockState.withProperty(BlockNewLog.VARIANT, type));
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public List<Immutable<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<Immutable<?, ?>>of(this.getTreeData(blockState), this.getLogAxisData(blockState));
    }
}
