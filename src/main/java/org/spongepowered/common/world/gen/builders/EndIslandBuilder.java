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
package org.spongepowered.common.world.gen.builders;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.world.gen.feature.EndIslandFeature;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.EndIsland;
import org.spongepowered.api.world.gen.populator.EndIsland.Builder;

public class EndIslandBuilder implements EndIsland.Builder {

    private VariableAmount initial;
    private VariableAmount decrement;
    private BlockState state;
    private int exclusion;

    public EndIslandBuilder() {
        reset();
    }

    @Override
    public Builder startingRadius(VariableAmount radius) {
        this.initial = checkNotNull(radius);
        return this;
    }

    @Override
    public Builder radiusDecrement(VariableAmount radius) {
        this.decrement = checkNotNull(radius);
        return this;
    }

    @Override
    public Builder islandBlock(BlockState state) {
        this.state = checkNotNull(state, "state");
        return this;
    }

    @Override
    public Builder exclusionRadius(int radius) {
        checkArgument(radius >= 0, "Exclusion radius must be postive or zero");
        this.exclusion = radius;
        return this;
    }

    @Override
    public Builder from(EndIsland value) {
        startingRadius(value.getStartingRadius());
        radiusDecrement(value.getRadiusDecrement());
        islandBlock(value.getIslandBlock());
        return this;
    }

    @Override
    public Builder reset() {
        this.initial = VariableAmount.baseWithRandomAddition(4, 3);
        this.decrement = VariableAmount.baseWithRandomAddition(0.5, 2);
        this.state = BlockTypes.END_STONE.getDefaultState();
        this.exclusion = 1024;
        return this;
    }

    @Override
    public EndIsland build() throws IllegalStateException {
        EndIsland island = (EndIsland) new EndIslandFeature();
        island.setIslandBlock(this.state);
        island.setStartingRadius(this.initial);
        island.setRadiusDecrement(this.decrement);
        island.setExclusionRadius(this.exclusion);
        return island;
    }

}
