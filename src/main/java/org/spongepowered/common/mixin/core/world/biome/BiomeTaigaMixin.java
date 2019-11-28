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

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeTaiga;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;

@Mixin(BiomeTaiga.class)
public abstract class BiomeTaigaMixin extends BiomeMixin {

    @Shadow @Final private BiomeTaiga.Type type;

    @Override
    public void bridge$buildPopulators(final World world, final SpongeBiomeGenerationSettings gensettings) {
        if (this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) {
            final BlockBlob blob = BlockBlob.builder()
                    .blobCount(VariableAmount.baseWithRandomAddition(0, 3))
                    .block((BlockState) Blocks.field_150341_Y.func_176223_P())
                    .radius(VariableAmount.baseWithRandomAddition(0, 2))
                    .build();
            gensettings.getPopulators().add(blob);
        }

        final DoublePlant fern = DoublePlant.builder()
                .type(DoublePlantTypes.FERN, 1)
                .perChunk(7 * 5)
                .build();
        gensettings.getPopulators().add(fern);
        super.bridge$buildPopulators(world, gensettings);
        WorldGenConstants.buildTaigaPopulators(gensettings, this.type, this.fillerBlock, this.decorator);
    }

}
