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
package org.spongepowered.common.world.gen.populators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;
import java.util.function.Predicate;

public class RandomBlockPopulator implements RandomBlock {

    private VariableAmount count;
    private VariableAmount height;
    private Predicate<Location<World>> check;
    private BlockState state;

    public RandomBlockPopulator(final BlockState block, final VariableAmount count, final VariableAmount height) {
        this.count = checkNotNull(count);
        this.state = checkNotNull(block);
        this.height = checkNotNull(height);
        this.check = (t) -> true;
    }

    public RandomBlockPopulator(final BlockState block, final VariableAmount count, final VariableAmount height, final Predicate<Location<World>> check) {
        this.count = checkNotNull(count);
        this.state = checkNotNull(block);
        this.height = checkNotNull(height);
        this.check = checkNotNull(check);
    }

    @Override
    public PopulatorType getType() {
        return PopulatorTypes.GENERIC_BLOCK;
    }

    @Override
    public void populate(final org.spongepowered.api.world.World world, final Extent extent, final Random random) {
        final Vector3i min = extent.getBlockMin();
        final Vector3i size = extent.getBlockSize();
        final int n = this.count.getFlooredAmount(random);
        final Location<World> chunkMin = new Location<World>(world, min.getX(), min.getY(),
                min.getZ());
        for (int i = 0; i < n; i++) {
            final Location<World> pos = chunkMin.add(random.nextInt(size.getX()), this.height.getFlooredAmount(random), random.nextInt(size.getZ()));
            if (this.check.test(pos)) {
                if (((WorldBridge) world).bridge$isFake()) {
                    world.setBlock(pos.getBlockPosition(), this.state, BlockChangeFlags.PHYSICS_OBSERVER);
                } else { // This is the most direct call to set a block state, due to neighboring updates we don't want to cause.
                    PhaseTracker.getInstance().setBlockState((WorldServerBridge) world, VecHelper.toBlockPos(pos), (IBlockState) this.state, BlockChangeFlags.PHYSICS_OBSERVER);
                }
                // Liquids force a block update tick so they may flow during world gen
                try {
                    ((WorldServer) world).func_189507_a(VecHelper.toBlockPos(pos), (IBlockState) this.state, random);
                } catch(IllegalArgumentException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public BlockState getBlock() {
        return this.state;
    }

    @Override
    public void setBlock(final BlockState block) {
        this.state = checkNotNull(block);
    }

    @Override
    public VariableAmount getAttemptsPerChunk() {
        return this.count;
    }

    @Override
    public void setAttemptsPerChunk(final VariableAmount count) {
        this.count = checkNotNull(count);
    }

    @Override
    public Predicate<Location<World>> getPlacementTarget() {
        return this.check;
    }

    @Override
    public void getPlacementTarget(final Predicate<Location<World>> target) {
        this.check = checkNotNull(target);
    }

    @Override
    public VariableAmount getHeightRange() {
        return this.height;
    }

    @Override
    public void setHeightRange(final VariableAmount height) {
        this.height = checkNotNull(height);
    }

}
