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
package org.spongepowered.common.mixin.core.world.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.EndIslandFeature;
import org.spongepowered.api.world.gen.populator.EndIsland;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(EndIslandFeature.class)
public abstract class WorldGenEndIslandMixin extends WorldGeneratorMixin {

    /**
     * @author Deamon
     * @reason Make it use the initial radius, radius decrement, and
     * block type fields
     */
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos position) {
        // int radius = rand.nextInt(3) + 4;
        double radius = ((EndIsland) this).getStartingRadius().getFlooredAmount(rand);

        for (int y = 0; radius > 0.5F; --y) {
            for (int x = MathHelper.func_76128_c(-radius); x <= MathHelper.func_76143_f(radius); ++x) {
                for (int z = MathHelper.func_76128_c(-radius); z <= MathHelper.func_76143_f(radius); ++z) {
                    if (x * x + z * z <= (radius + 1.0F) * (radius + 1.0F)) {
                        // this.setBlockAndNotifyAdequately(worldIn,
                        // position.add(k, j, l),
                        // Blocks.end_stone.getDefaultState());
                        this.setBlockAndNotifyAdequately(worldIn, position.func_177982_a(x, y, z), (BlockState) ((EndIsland) this).getIslandBlock());
                    }
                }
            }

            radius = (float) (radius - ((EndIsland) this).getRadiusDecrement().getAmount(rand));
            // radius = (float)(radius - (rand.nextInt(2) + 0.5D));
        }

        return true;
    }

}
