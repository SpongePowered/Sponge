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

import net.minecraft.util.BlockPos;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.common.SpongeCatalogType;

import java.util.Random;

public class WrappedPopulatorObject extends SpongeCatalogType.Named implements PopulatorObject {

    private final WorldGenerator gen;

    public WrappedPopulatorObject(String id, String name, WorldGenerator gen) {
        super(id, name);
        this.gen = checkNotNull(gen, "gen");
    }

    @Override
    public boolean canPlaceAt(World world, int x, int y, int z) {
        return true;
    }

    @Override
    public void placeObject(World world, Random random, int x, int y, int z) {
        this.gen.generate((net.minecraft.world.World) world, random, new BlockPos(x, y, z));
    }

}
