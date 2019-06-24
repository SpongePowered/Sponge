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
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.populators.MesaBiomeGenerationPopulator;

import java.util.Random;

@Mixin(BiomeMesa.class)
public abstract class BiomeMesaMixin extends BiomeMixin {

    @Shadow @Final private boolean brycePillars;
    @Shadow @Final private boolean hasForest;

    @Override
    public void bridge$buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        gensettings.getGenerationPopulators().add(new MesaBiomeGenerationPopulator(this.brycePillars, this.hasForest));
        super.bridge$buildPopulators(world, gensettings);
        final String s = world.getWorldInfo().getGeneratorOptions();
        final ChunkGeneratorSettings settings = ChunkGeneratorSettings.Factory.jsonToFactory(s).build();

        // Extra gold is generated in mesa biomes
        final Ore gold = Ore.builder()
                .ore((BlockState) Blocks.GOLD_ORE.getDefaultState())
                .size(settings.goldSize)
                .perChunk(20)
                .height(VariableAmount.baseWithRandomAddition(32, 80 - 32))
                .build();
        gensettings.getPopulators().add(gold);

        final BiomeDecorator theBiomeDecorator = this.decorator;
        gensettings.getGroundCoverLayers().clear();
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest forest = Forest.builder()
          .perChunk(VariableAmount.baseWithOptionalAddition(theBiomeDecorator.treesPerChunk, 1, 0.1))
          .type(BiomeTreeTypes.OAK.getPopulatorObject(), 1)
          .build();
        gensettings.getPopulators().add(0, forest);

        gensettings.getPopulators().removeAll(gensettings.getPopulators(Cactus.class));
        final Cactus cactus = Cactus.builder()
                .cactiPerChunk(VariableAmount.baseWithOptionalAddition(0,
                        VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithOptionalAddition(2, 3, 0.25)), 0.4))
                .build();
        gensettings.getPopulators().add(cactus);
    }

    /**
     * Cancel the call to place the terrain blocks as this is instead handled
     * through our custom genpop.
     */
    @Inject(method = "genTerrainBlocks(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/world/chunk/ChunkPrimer;IID)V", at = @At("HEAD") ,
      cancellable = true)
    private void genTerrainBlocks(World world, Random rand, ChunkPrimer chunk, int x, int z, double stoneNoise, CallbackInfo ci) {
        final SpongeChunkGenerator generator = (SpongeChunkGenerator) ((org.spongepowered.api.world.World) world).getWorldGenerator();
        final BiomeGenerationSettings biomeSettings = generator.getBiomeSettings((BiomeType) this);
        // If trhis isn't our settings type then this isn't our generation, allow this to continue
        if (!(biomeSettings instanceof SpongeBiomeGenerationSettings)) {
            ci.cancel();
        }
    }
}
