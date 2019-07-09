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

import com.google.common.collect.Lists;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.bridge.world.gen.FlaggedPopulatorBridge;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class FilteredPopulator implements FlaggedPopulatorBridge, Populator {

    private final Populator wrapped;
    private final List<String> requiredFlags = Lists.newArrayList();
    private final Predicate<Extent> check;

    public FilteredPopulator(Populator w) {
        this(w, (c) -> true);
    }

    public FilteredPopulator(Populator w, Predicate<Extent> c) {
        this.wrapped = w;
        this.check = c;
    }

    public void setRequiredFlags(String... flags) {
        this.requiredFlags.clear();
        for (String f : flags) {
            this.requiredFlags.add(f);
        }
    }

    @Override
    public PopulatorType getType() {
        return this.wrapped.getType();
    }

    @Override
    public void populate(World world, Extent volume, Random random) {
        this.wrapped.populate(world, volume, random);
    }

    @Override
    public void populate(org.spongepowered.api.world.World world, Extent extent, Random rand, ImmutableBiomeVolume virtualBiomes) {
        this.wrapped.populate(world, extent, rand, virtualBiomes);
    }

    @Override
    public void bridge$populate(World world, Extent extent, Random rand, List<String> flags) {
        if (!this.check.test(extent)) {
            return;
        }
        if (!flags.containsAll(this.requiredFlags)) {
            return;
        }
        this.wrapped.populate(world, extent, rand);
    }

    @Override
    public void bridge$populate(org.spongepowered.api.world.World world, Extent extent, Random rand, ImmutableBiomeVolume virtualBiomes, List<String> flags) {
        if (!this.check.test(extent)) {
            return;
        }
        if (!flags.containsAll(this.requiredFlags)) {
            return;
        }
        this.wrapped.populate(world, extent, rand, virtualBiomes);
    }

}
