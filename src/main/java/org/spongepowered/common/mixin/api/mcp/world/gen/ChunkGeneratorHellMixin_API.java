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
package org.spongepowered.common.mixin.api.mcp.world.gen;

import net.minecraft.world.gen.ChunkGeneratorNether;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.FortressStructure;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.PopulatorProviderBridge;

@Mixin(ChunkGeneratorNether.class)
public abstract class ChunkGeneratorHellMixin_API implements PopulatorProviderBridge {

    @Shadow @Final private boolean generateStructures;
    @Shadow @Final private FortressStructure genNetherBridge;
    @Shadow @Final private MapGenBase genNetherCaves;

    @Override
    public void bridge$addPopulators(final WorldGenerator generator) {
        generator.getGenerationPopulators().add((GenerationPopulator) this.genNetherCaves);

        if (this.generateStructures) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.genNetherBridge);
            generator.getPopulators().add((Populator) this.genNetherBridge);
        }
    }

}
