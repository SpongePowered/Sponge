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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.world.gen.feature.GlowStoneFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.api.world.gen.populator.Glowstone.Builder;


public class GlowstoneBuilder implements Glowstone.Builder {
    
    private VariableAmount count;
    private VariableAmount attempts;
    private VariableAmount clusterheight;
    private VariableAmount height;
    
    public GlowstoneBuilder() {
        reset();
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        this.count = checkNotNull(count, "count");
        return this;
    }

    @Override
    public Builder blocksPerCluster(VariableAmount attempts) {
        this.attempts = checkNotNull(attempts, "attempts");
        return this;
    }

    @Override
    public Builder clusterHeight(VariableAmount height) {
        this.clusterheight = checkNotNull(height, "clusterheight");
        return this;
    }

    @Override
    public Builder height(VariableAmount height) {
        this.height = checkNotNull(height, "height");
        return this;
    }

    @Override
    public Builder from(Glowstone value) {
        return perChunk(value.getClustersPerChunk())
            .blocksPerCluster(value.getAttemptsPerCluster())
            .clusterHeight(value.getClusterHeight())
            .height(value.getHeight());
    }

    @Override
    public Builder reset() {
        this.count = VariableAmount.baseWithRandomAddition(1, 10);
        this.attempts = VariableAmount.fixed(1500);
        this.clusterheight = VariableAmount.baseWithRandomAddition(0, 12);
        this.height = VariableAmount.baseWithRandomAddition(4, 120);
        return this;
    }

    @Override
    public Glowstone build() throws IllegalStateException {
        Glowstone pop = (Glowstone) new GlowStoneFeature();
        pop.setAttemptsPerCluster(this.attempts);
        pop.setClustersPerChunk(this.count);
        pop.setClusterHeight(this.clusterheight);
        pop.setHeight(this.height);
        return pop;
    }

}
