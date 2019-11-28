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
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableLogAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.data.value.BaseValue;
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
        if(blockState.func_177230_c() instanceof BlockOldLog) {
            type = blockState.func_177229_b(BlockOldLog.field_176301_b);
        } else if(blockState.func_177230_c() instanceof BlockNewLog) {
            type = blockState.func_177229_b(BlockNewLog.field_176300_b);
        } else {
            type = BlockPlanks.EnumType.OAK;
        }

        final TreeType treeType = TreeTypeRegistryModule.getFor(type);

        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeTreeData.class, treeType);
    }

    @SuppressWarnings("ConstantConditions")
    private ImmutableLogAxisData getLogAxisData(final net.minecraft.block.BlockState blockState) {
        final LogAxis logAxis = (LogAxis) (Object) blockState.func_177229_b(LogBlock.field_176299_a);
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeLogAxisData.class, logAxis);
    }


    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableTreeData.class.isAssignableFrom(immutable) || ImmutableLogAxisData.class.isAssignableFrom(immutable);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<BlockState> bridge$getStateWithData(final net.minecraft.block.BlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableTreeData) {
            final TreeType treeType = ((ImmutableTreeData) manipulator).type().get();
            final BlockPlanks.EnumType type = TreeTypeRegistryModule.getFor(treeType);
            return impl$processLogType(blockState, type, treeType);
        }
        if (manipulator instanceof ImmutableLogAxisData) {
            final LogAxis logAxis = ((ImmutableLogAxisData) manipulator).type().get();
            return Optional.of((BlockState) blockState.func_177226_a(LogBlock.field_176299_a, (LogBlock.EnumAxis) (Object) logAxis));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final net.minecraft.block.BlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.TREE_TYPE)) {
            final TreeType treeType = (TreeType) value;
            final BlockPlanks.EnumType type = TreeTypeRegistryModule.getFor(treeType);
            return impl$processLogType(blockState, type, treeType);
        } else if (key.equals(Keys.LOG_AXIS)) {
            return Optional.of((BlockState) blockState.func_177226_a(LogBlock.field_176299_a, (LogBlock.EnumAxis) value));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private Optional<BlockState> impl$processLogType(final net.minecraft.block.BlockState blockState, final BlockPlanks.EnumType type, final TreeType treeType) {
        if (blockState.func_177230_c() instanceof BlockOldLog) {
            if (treeType.equals(TreeTypes.OAK) ||
                treeType.equals(TreeTypes.BIRCH) ||
                treeType.equals(TreeTypes.SPRUCE) ||
                treeType.equals(TreeTypes.JUNGLE)) {
                return Optional.of((BlockState) blockState.func_177226_a(BlockOldLog.field_176301_b, type));
            }
        } else if (blockState.func_177230_c() instanceof BlockNewLog) {
            if (treeType.equals(TreeTypes.ACACIA) || treeType.equals(TreeTypes.DARK_OAK)) {
                return Optional.of((BlockState) blockState.func_177226_a(BlockNewLog.field_176300_b, type));
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public List<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final net.minecraft.block.BlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getTreeData(blockState), getLogAxisData(blockState));
    }
}
