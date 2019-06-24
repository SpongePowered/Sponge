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
import net.minecraft.world.biome.BiomeJungle;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Melon;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.populator.Vine;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;

import java.util.Iterator;

@Mixin(BiomeJungle.class)
public abstract class BiomeJungleMixin extends BiomeMixin {

    @Shadow @Final private boolean isEdge;

    @Override
    public void bridge$buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        super.bridge$buildPopulators(world, gensettings);
        BiomeDecorator theBiomeDecorator = this.decorator;
        for (Iterator<Populator> it = gensettings.getPopulators().iterator(); it.hasNext();) {
            Populator next = it.next();
            if (next instanceof Shrub) {
                Shrub s = (Shrub) next;
                if (s.getTypes().size() == 1) {
                    TableEntry<ShrubType> entry = s.getTypes().getEntries().get(0);
                    if (entry instanceof WeightedObject && ((WeightedObject<ShrubType>) entry).get() == ShrubTypes.TALL_GRASS) {
                        it.remove();
                    }
                }
            }
        }
        Shrub grass = Shrub.builder()
                .perChunk(theBiomeDecorator.grassPerChunk * 128)
                .type(ShrubTypes.FERN, 1)
                .type(ShrubTypes.TALL_GRASS, 3)
                .build();
        gensettings.getPopulators().add(grass);
        Melon melon = Melon.builder()
                .perChunk(64)
                .build();
        gensettings.getPopulators().add(melon);

        Vine vine = Vine.builder()
                .perChunk(50)
                .build();
        gensettings.getPopulators().add(vine);

        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(theBiomeDecorator.treesPerChunk, 1, 0.1));
        forest.type(BiomeTreeTypes.OAK.getLargePopulatorObject().get(), 1);
        forest.type(BiomeTreeTypes.JUNGLE_BUSH.getPopulatorObject(), 4.5);
        if (!this.isEdge) {
            forest.type(BiomeTreeTypes.JUNGLE.getLargePopulatorObject().get(), 1.2);
            forest.type(BiomeTreeTypes.JUNGLE.getPopulatorObject(), 3);
        } else {
            forest.type(BiomeTreeTypes.JUNGLE.getPopulatorObject(), 4.5);
        }
        gensettings.getPopulators().add(0, forest.build());
    }
}
