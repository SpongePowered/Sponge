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
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenForest;
import net.minecraft.world.biome.BiomeGenMutated;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.populators.FlowerForestSupplier;
import org.spongepowered.common.world.gen.populators.RoofedForestPopulator;

@Mixin(BiomeGenForest.class)
public abstract class MixinBiomeGenForest extends MixinBiomeGenBase {

    @Shadow private int field_150632_aF;

    @Override
    public void buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        BiomeDecorator theBiomeDecorator = this.theBiomeDecorator;
        if (BiomeGenMutated.class.isAssignableFrom(this.getClass())) {
            theBiomeDecorator = ((BiomeGenMutated) (Object) this).baseBiome.theBiomeDecorator;
        }
        int base = -3;
        if (this.field_150632_aF == 1) {
            base = -1;
        }
        DoublePlant plant = DoublePlant.builder()
                .perChunk(VariableAmount.baseWithRandomAddition(base * 5, 5 * 5))
                .type(DoublePlantTypes.SYRINGA, 1)
                .type(DoublePlantTypes.ROSE, 1)
                .type(DoublePlantTypes.PAEONIA, 1)
                .build();
        gensettings.getPopulators().add(plant);
        super.buildPopulators(world, gensettings);
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        if (this.field_150632_aF == 3) {
            RoofedForestPopulator forest = new RoofedForestPopulator();
            gensettings.getPopulators().add(0, forest);
        } else {
            Forest.Builder forest = Forest.builder();
            forest.perChunk(VariableAmount.baseWithOptionalAddition(theBiomeDecorator.treesPerChunk, 1, 0.1));
            if (this.field_150632_aF == 2) {
                forest.type(BiomeTreeTypes.BIRCH.getPopulatorObject(), 1);
            } else {
                forest.type(BiomeTreeTypes.OAK.getPopulatorObject(), 4);
                forest.type(BiomeTreeTypes.BIRCH.getPopulatorObject(), 1);
            }
            gensettings.getPopulators().add(0, forest.build());
        }
        if (this.field_150632_aF == 1) {
            gensettings.getPopulators().removeAll(gensettings.getPopulators(Flower.class));
            Flower flower = Flower.builder()
                    .perChunk(theBiomeDecorator.flowersPerChunk * 64)
                    .supplier(new FlowerForestSupplier())
                    .build();
            gensettings.getPopulators().add(flower);
        }
    }
}
