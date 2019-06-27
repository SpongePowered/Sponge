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
package org.spongepowered.common.mixin.core.world.biome;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeSnow;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.IcePath;
import org.spongepowered.api.world.gen.populator.IceSpike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;

@Mixin(BiomeSnow.class)
public abstract class BiomeSnowMixin extends BiomeMixin {

    @Shadow @Final private boolean superIcy;

    @Override
    public void bridge$buildPopulators(final World world, final SpongeBiomeGenerationSettings gensettings) {
        if (this.superIcy) {
            final IceSpike spike = IceSpike.builder()
                    .spikesPerChunk(3)
                    .build();
            gensettings.getPopulators().add(spike);
            final IcePath path = IcePath.builder()
                    .perChunk(2)
                    .radius(VariableAmount.baseWithRandomAddition(2, 2))
                    .build();
            gensettings.getPopulators().add(path);
        }
        super.bridge$buildPopulators(world, gensettings);
        WorldGenConstants.buildSnowPopulators(gensettings, this.decorator);
    }

}
