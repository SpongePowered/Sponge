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
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.block.LayeredData;
import org.spongepowered.api.data.manipulator.block.TreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.LeafDecayEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.block.SpongeTreeData;
import org.spongepowered.common.interfaces.block.IMixinBlockTree;
import org.spongepowered.common.util.VecHelper;
import sun.reflect.generics.tree.Tree;

import java.util.Random;

@NonnullByDefault
@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends MixinBlock implements IMixinBlockTree {

    @Inject(method = "updateTick", at = @At(value = "INVOKE", target = "net/minecraft/block/BlockLeaves;destroy (Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callLeafDecay(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci, byte b0, int i, int j, int k, int l, byte b1, int i1, int j1, int k1) {
        Location block = new Location((Extent) worldIn, VecHelper.toVector(pos));
        BlockSnapshot postChange = block.getSnapshot();
        postChange.setBlockState(BlockTypes.AIR.getDefaultState());
        final LeafDecayEvent event = SpongeEventFactory.createLeafDecay(Sponge.getGame(), null, block, postChange); //TODO Fix null cause
        Sponge.getGame().getEventManager().post(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public TreeData getTreeData(IBlockState blockState) {
        BlockPlanks.EnumType type = null;
        if(blockState.getBlock() instanceof BlockOldLeaf) {
            type = (BlockPlanks.EnumType) blockState.getValue(BlockOldLeaf.VARIANT);
        } else if(blockState.getBlock() instanceof BlockNewLeaf) {
            type = (BlockPlanks.EnumType) blockState.getValue(BlockNewLeaf.VARIANT);
        }

        TreeType treeType = null;

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

        return new SpongeTreeData().setValue(treeType);
    }

    @Override
    public DataTransactionResult setTreeData(TreeData treeData, World world, BlockPos blockPos, DataPriority priority) {
        final TreeData data = getTreeData(checkNotNull(world).getBlockState(checkNotNull(blockPos)));
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                BlockPlanks.EnumType treeType = null;

                if(treeData.getValue() == TreeTypes.OAK) {
                    treeType = BlockPlanks.EnumType.OAK;
                } else if(treeData.getValue() == TreeTypes.SPRUCE) {
                    treeType = BlockPlanks.EnumType.SPRUCE;
                } else if(treeData.getValue() == TreeTypes.BIRCH) {
                    treeType = BlockPlanks.EnumType.BIRCH;
                } else if(treeData.getValue() == TreeTypes.JUNGLE) {
                    treeType = BlockPlanks.EnumType.JUNGLE;
                } else if(treeData.getValue() == TreeTypes.ACACIA) {
                    treeType = BlockPlanks.EnumType.ACACIA;
                } else if(treeData.getValue() == TreeTypes.DARK_OAK) {
                    treeType = BlockPlanks.EnumType.DARK_OAK;
                }

                IBlockState blockState = world.getBlockState(blockPos);

                if(blockState.getBlock() instanceof BlockOldLeaf) {
                    world.setBlockState(blockPos, blockState.withProperty(BlockOldLeaf.VARIANT, checkNotNull(treeType)));
                } else if(blockState.getBlock() instanceof BlockNewLeaf) {
                    world.setBlockState(blockPos, blockState.withProperty(BlockOldLeaf.VARIANT, checkNotNull(treeType)));
                }
                return successReplaceData(data);
            default:
                return successNoData();
        }
    }

    @Override
    public BlockState resetTreeData(BlockState blockState) {
        if(blockState.getType() == BlockTypes.LEAVES) {
            return ((BlockState) ((IBlockState) blockState).withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK));
        } else if(blockState.getType() == BlockTypes.LEAVES2) {
            return ((BlockState) ((IBlockState) blockState).withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA));
        } else {
            return blockState;
        }
    }

}
