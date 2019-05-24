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
package org.spongepowered.common.world.gen;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.SeededVariableAmount;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.biome.GroundCoverLayer;

public final class SandstoneGroundCoverLayer extends GroundCoverLayer {

    private static final int MAX_HEIGHT = 4;

    public SandstoneGroundCoverLayer(BlockState state) {
        super(state, SeededVariableAmount.wrapped(VariableAmount.range(0, MAX_HEIGHT)));
    }

    @Override
    public SeededVariableAmount<Double> getDepth(int topYCoordinate) {
        // Based on the code in Biome.generateBiomeTerrain().
        // This code uses -62 instead of -63 because the variable in vanilla
        // points to the block above.
        return (rand, seed) -> this.getDepth().getFlooredAmount(rand, seed) + Math.max(topYCoordinate - 62, 0);
    }
}
