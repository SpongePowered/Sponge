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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataTransactionBuilder.successNoData;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.interfaces.block.IMixinBlockTree;

@NonnullByDefault
@Mixin(BlockLog.class)
public abstract class MixinBlockLog extends MixinBlock implements IMixinBlockTree {

    @Override
    public TreeData getTreeData(IBlockState blockState) {
        BlockPlanks.EnumType type = null;
        if(blockState.getBlock() instanceof BlockOldLog) {
            type = (BlockPlanks.EnumType) blockState.getValue(BlockOldLog.VARIANT);
        } else if(blockState.getBlock() instanceof BlockNewLog) {
            type = (BlockPlanks.EnumType) blockState.getValue(BlockNewLog.VARIANT);
        }

        TreeType treeType = TreeTypes.OAK;

        switch(type) {
            case OAK:
                treeType = TreeTypes.OAK;
                break;
            case SPRUCE:
                treeType = TreeTypes.SPRUCE;
                break;
            case BIRCH:
                treeType = TreeTypes.BIRCH;
                break;
            case JUNGLE:
                treeType = TreeTypes.JUNGLE;
                break;
            case ACACIA:
                treeType = TreeTypes.ACACIA;
                break;
            case DARK_OAK:
                treeType = TreeTypes.DARK_OAK;
                break;
        }

        final TreeData data = new SpongeTreeData(treeType);

        return data;
    }

    public DataTransactionResult setTreeData(TreeData treeData, World world, BlockPos blockPos) {
        final TreeData data = getTreeData(checkNotNull(world).getBlockState(checkNotNull(blockPos)));
        return successNoData();
    }

    @Override
    public BlockState resetTreeData(BlockState blockState) {
        if(blockState.getType() == BlockTypes.LEAVES) {
            return ((BlockState) ((IBlockState) blockState).withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK));
        } else if(blockState.getType() == BlockTypes.LEAVES2) {
            return ((BlockState) ((IBlockState) blockState).withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA));
        } else {
            return blockState;
        }
    }

}
