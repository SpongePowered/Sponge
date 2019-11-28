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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(WorldGenMegaJungle.class)
public abstract class WorldGenMegaJungleMixin_API extends WorldGenHugeTrees implements PopulatorObject {

    public WorldGenMegaJungleMixin_API(final boolean notify, final int baseHeightIn, final int extraRandomHeightIn, final IBlockState woodMetadataIn,
        final IBlockState leavesMetadataIn) {
        super(notify, baseHeightIn, extraRandomHeightIn, woodMetadataIn, leavesMetadataIn);
    }

    @Override
    public String getId() {
        return "minecraft:mega_jungle";
    }

    @Override
    public String getName() {
        return "Mega jungle tree";
    }

    @Override
    public boolean canPlaceAt(final World world, final int x, final int y, final int z) {
        return this.func_175929_a((net.minecraft.world.World) world, null, new BlockPos(x, y, z), this.field_76522_a);
    }

    @Override
    public void placeObject(final World world, final Random random, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        func_175904_e();
        if (func_180709_b((net.minecraft.world.World) world, random, pos)) {
            func_180711_a((net.minecraft.world.World) world, random, pos);
        }
    }

}
