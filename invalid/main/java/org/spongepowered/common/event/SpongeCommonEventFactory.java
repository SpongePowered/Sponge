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
package org.spongepowered.common.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.state.PistonBlockStructureHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpongeCommonEventFactory {

    /**
     * This simulates the blocks a piston moves and calls the event for saner
     * debugging.
     *
     * @return if the event was cancelled
     */
    public static boolean handlePistonEvent(
            final ServerWorldBridge world, final ServerWorld.ServerBlockEventList list, final Object obj, final BlockPos pos, final Block blockIn,
            final int eventId, final int eventParam) {
        final boolean extending = (eventId == 0);
        final net.minecraft.block.BlockState blockstate = ((net.minecraft.world.World) world).getBlockState(pos);
        final net.minecraft.util.Direction direction = blockstate.get(DirectionalBlock.FACING);
        final LocatableBlock locatable = new SpongeLocatableBlockBuilder().world((World) world).state((BlockState) blockstate).position(pos.getX(), pos.getY(), pos.getZ()).build();

        // Sets toss out duplicate values (even though there shouldn't be any)
        final HashSet<Location> locations = new HashSet<>();
        locations.add(Location.of((World) world, pos.getX(), pos.getY(), pos.getZ()));

        final PistonBlockStructureHelper movedBlocks = new PistonBlockStructureHelper((ServerWorld) world, pos, direction, extending);
        movedBlocks.canMove(); // calculates blocks to be moved

        Stream.concat(movedBlocks.getBlocksToMove().stream(), movedBlocks.getBlocksToDestroy().stream())
                .map(block -> Location.of((World) world, block.getX(), block.getY(), block.getZ()))
                .collect(Collectors.toCollection(() -> locations)); // SUPER
                                                                    // efficient
                                                                    // code!

        // If the piston is extending and there are no blocks to destroy, add the offset location for protection purposes
        if (extending && movedBlocks.getBlocksToDestroy().isEmpty()) {
            final List<BlockPos> movedPositions = movedBlocks.getBlocksToMove();
            final BlockPos offsetPos;
            // If there are no blocks to move, add the offset of piston
            if (movedPositions.isEmpty()) {
                offsetPos = pos.offset(direction);
            } else {
                // Add the offset of last block set to move
                offsetPos = movedPositions.get(movedPositions.size() - 1).offset(direction);
            }
            locations.add(Location.of((World) world, offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()));
        }

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (extending) {
                frame.addContext(EventContextKeys.PISTON_EXTEND, (World) world);
            } else {
                frame.addContext(EventContextKeys.PISTON_RETRACT, (World) world);
            }
            return SpongeCommonEventFactory.callChangeBlockEventPre(world, ImmutableList.copyOf(locations), locatable)
                .isCancelled();
        }
    }

}
