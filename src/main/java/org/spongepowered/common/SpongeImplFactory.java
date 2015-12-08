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
package org.spongepowered.common;


import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.world.World;

import java.util.Iterator;

/**
 * Utility that fires events that normally Forge fires at (in spots). Typically
 * our penultimate goal is to not remove spots where events occur but sometimes
 * it happens (in @Overwrites typically). Normally events that are in Forge are
 * called themselves in SpongeVanilla but when it can't really occur, we fix
 * this issue with Sponge by overwriting this class
 */
public class SpongeImplFactory {

    public static LoadWorldEvent createLoadWorldEvent(Game game, World world) {
        return SpongeEventFactory.createLoadWorldEvent(game, Cause.of(NamedCause.source(SpongeImpl.getGame().getServer())), world);
    }

    public static ClientConnectionEvent.Join createClientConnectionEventJoin(Game game, Cause cause, Text originalMessage, Text message, MessageSink originalSink, MessageSink sink, Player targetEntity) {
        return SpongeEventFactory.createClientConnectionEventJoin(game, cause, originalMessage, message, originalSink, sink, targetEntity);
    }

    public static RespawnPlayerEvent createRespawnPlayerEvent(Game game, Cause cause, Transform<World> fromTransform, Transform<World> toTransform, Player targetEntity, boolean bedSpawn) {
        return SpongeEventFactory.createRespawnPlayerEvent(game, cause, fromTransform, toTransform, targetEntity, bedSpawn);
    }

    public static ClientConnectionEvent.Disconnect createClientConnectionEventDisconnect(Game game, Cause cause, Text originalMessage, Text message, MessageSink originalSink, MessageSink sink, Player targetEntity) {
        return SpongeEventFactory.createClientConnectionEventDisconnect(game, cause, originalMessage, message, originalSink, sink, targetEntity);
    }

    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block instanceof BlockContainer;
    }

    public static int getBlockLightValue(Block block, BlockPos pos, IBlockAccess world) {
        return block.getLightValue();
    }

    public static int getBlockLightOpacity(Block block, IBlockAccess world, BlockPos pos) {
        return block.getLightOpacity();
    }

    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return true;
    }

    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider)block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    // TODO - Determine if this is still needed
    @SuppressWarnings("unchecked")
    public static void updateComparatorOutputLevel(net.minecraft.world.World world, BlockPos pos, Block blockIn) {
        Iterator<EnumFacing> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumFacing enumfacing = iterator.next();
            BlockPos blockpos1 = pos.offset(enumfacing);

            if (world.isBlockLoaded(blockpos1)) {
                IBlockState iblockstate = world.getBlockState(blockpos1);

                if (Blocks.unpowered_comparator.isAssociated(iblockstate.getBlock())) {
                    iblockstate.getBlock().onNeighborBlockChange(world, blockpos1, iblockstate, blockIn);
                } else if (iblockstate.getBlock().isNormalCube()) {
                    blockpos1 = blockpos1.offset(enumfacing);
                    iblockstate = world.getBlockState(blockpos1);

                    if (Blocks.unpowered_comparator.isAssociated(iblockstate.getBlock())) {
                        iblockstate.getBlock().onNeighborBlockChange(world, blockpos1, iblockstate, blockIn);
                    }
                }
            }
        }
    }
}
