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
import static org.spongepowered.api.data.DataTransactionBuilder.failResult;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.source.world.WorldDecayBlockEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.interfaces.block.IMixinBlockTree;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

@NonnullByDefault
@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends MixinBlock implements IMixinBlockTree {

    @Inject(method = "updateTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockLeaves;destroy(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V"), cancellable = true)
    public void callLeafDecay(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        Location<org.spongepowered.api.world.World> location =
            new Location<org.spongepowered.api.world.World>((org.spongepowered.api.world.World) worldIn, VecHelper.toVector(pos));
        BlockSnapshot blockOriginal = location.getBlockSnapshot();
        BlockSnapshot blockReplacement = blockOriginal.setState(BlockTypes.AIR.getDefaultState());
        ImmutableList<BlockTransaction> transactions = new ImmutableList.Builder<BlockTransaction>().add(new BlockTransaction(blockOriginal, blockReplacement)).build();
        final WorldDecayBlockEvent event = SpongeEventFactory.createWorldDecayBlock(Sponge.getGame(), Cause.of(worldIn), transactions);
        Sponge.getGame().getEventManager().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public TreeData getTreeData(IBlockState blockState) {
        BlockPlanks.EnumType type = null;
        if (blockState.getBlock() instanceof BlockOldLeaf) {
            type = (BlockPlanks.EnumType) blockState.getValue(BlockOldLeaf.VARIANT);
        } else if (blockState.getBlock() instanceof BlockNewLeaf) {
            type = (BlockPlanks.EnumType) blockState.getValue(BlockNewLeaf.VARIANT);
        }

        TreeType treeType = null;

        // TODO Sponge defaults to TreeTypes.OAK if type isn't found.
        switch (type) {
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
            default:
                treeType = TreeTypes.OAK;
        }

        return new SpongeTreeData(treeType);
    }

    public DataTransactionResult setTreeData(TreeData treeData, World world, BlockPos blockPos) {
        final TreeData data = getTreeData(checkNotNull(world).getBlockState(checkNotNull(blockPos)));
        return failResult(treeData.type().asImmutable());
    }

    @Override
    public BlockState resetTreeData(BlockState blockState) {
        if (blockState.getType() == BlockTypes.LEAVES) {
            return ((BlockState) ((IBlockState) blockState).withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK));
        } else if (blockState.getType() == BlockTypes.LEAVES2) {
            return ((BlockState) ((IBlockState) blockState).withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA));
        } else {
            return blockState;
        }
    }

}
