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

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderSettings;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.EmptyObject;
import org.spongepowered.api.util.weighted.SeededVariableAmount;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.populator.BigMushroom;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.api.world.gen.populator.DeadBush;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.populator.Reed;
import org.spongepowered.api.world.gen.populator.SeaFloor;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.populator.WaterLily;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.world.biome.IMixinBiome;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.WrappedBiomeDecorator;

@NonnullByDefault
@Mixin(Biome.class)
public abstract class MixinBiome implements BiomeType, IMixinBiome {

    @Shadow @Final public String biomeName;
    @Shadow @Final public float temperature;
    @Shadow @Final public float rainfall;
    @Shadow public IBlockState topBlock;
    @Shadow public IBlockState fillerBlock;
    @Shadow public BiomeDecorator theBiomeDecorator;

    private String id;
    private String modId;

    @Override
    public BiomeGenerationSettings createDefaultGenerationSettings(org.spongepowered.api.world.World world) {
        SpongeBiomeGenerationSettings gensettings = new SpongeBiomeGenerationSettings();
        gensettings.getPopulators().clear();
        gensettings.getGenerationPopulators().clear();
        gensettings.getGroundCoverLayers().clear();
        buildPopulators((World) world, gensettings);
        if (!getClass().getName().startsWith("net.minecraft")) {
            gensettings.getPopulators().add(new WrappedBiomeDecorator((Biome) (Object) this));
        } else if (!this.theBiomeDecorator.getClass().getName().startsWith("net.minecraft")) {
            gensettings.getPopulators().add(new WrappedBiomeDecorator(this.theBiomeDecorator));
        }
        return gensettings;
    }

    @Override
    public void buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        BiomeDecorator theBiomeDecorator = this.theBiomeDecorator;

        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((BlockState) this.topBlock, SeededVariableAmount.fixed(1)));
        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((BlockState) this.fillerBlock, WorldGenConstants.GROUND_COVER_DEPTH));

        String s = world.getWorldInfo().getGeneratorOptions();
        ChunkProviderSettings settings;
        if (s != null) {
            settings = ChunkProviderSettings.Factory.jsonToFactory(s).build();
        } else {
            settings = ChunkProviderSettings.Factory.jsonToFactory("").build();
        }

        Ore dirt = Ore.builder()
                .ore((BlockState) Blocks.DIRT.getDefaultState())
                .size(settings.dirtSize)
                .perChunk(settings.dirtCount)
                .height(VariableAmount.baseWithRandomAddition(settings.dirtMinHeight, settings.dirtMaxHeight))
                .build();
        gensettings.getPopulators().add(dirt);

        Ore gravel = Ore.builder()
                .ore((BlockState) Blocks.GRAVEL.getDefaultState())
                .size(settings.gravelSize)
                .perChunk(settings.gravelCount)
                .height(VariableAmount.baseWithRandomAddition(settings.gravelMinHeight, settings.gravelMaxHeight))
                .build();
        gensettings.getPopulators().add(gravel);

        Ore diorite = Ore.builder()
                .ore((BlockState) Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
                .size(settings.dioriteSize)
                .perChunk(settings.dioriteCount)
                .height(VariableAmount.baseWithRandomAddition(settings.dioriteMinHeight, settings.dioriteMaxHeight))
                .build();
        gensettings.getPopulators().add(diorite);

        Ore granite = Ore.builder()
                .ore((BlockState) Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
                .size(settings.graniteSize)
                .perChunk(settings.graniteCount)
                .height(VariableAmount.baseWithRandomAddition(settings.graniteMinHeight, settings.graniteMaxHeight))
                .build();
        gensettings.getPopulators().add(granite);

        Ore andesite = Ore.builder()
                .ore((BlockState) Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
                .size(settings.andesiteSize)
                .perChunk(settings.andesiteCount)
                .height(VariableAmount.baseWithRandomAddition(settings.andesiteMinHeight, settings.andesiteMaxHeight))
                .build();
        gensettings.getPopulators().add(andesite);

        Ore coal = Ore.builder()
                .ore((BlockState) Blocks.COAL_ORE.getDefaultState())
                .size(settings.coalSize)
                .perChunk(settings.coalCount)
                .height(VariableAmount.baseWithRandomAddition(settings.coalMinHeight, settings.coalMaxHeight))
                .build();
        gensettings.getPopulators().add(coal);

        Ore iron = Ore.builder()
                .ore((BlockState) Blocks.IRON_ORE.getDefaultState())
                .size(settings.ironSize)
                .perChunk(settings.ironCount)
                .height(VariableAmount.baseWithRandomAddition(settings.ironMinHeight, settings.ironMaxHeight))
                .build();
        gensettings.getPopulators().add(iron);

        Ore gold = Ore.builder()
                .ore((BlockState) Blocks.GOLD_ORE.getDefaultState())
                .size(settings.goldSize)
                .perChunk(settings.goldCount)
                .height(VariableAmount.baseWithRandomAddition(settings.goldMinHeight, settings.goldMaxHeight))
                .build();
        gensettings.getPopulators().add(gold);

        Ore redstone = Ore.builder()
                .ore((BlockState) Blocks.REDSTONE_ORE.getDefaultState())
                .size(settings.redstoneSize)
                .perChunk(settings.redstoneCount)
                .height(VariableAmount.baseWithRandomAddition(settings.redstoneMinHeight, settings.redstoneMaxHeight))
                .build();
        gensettings.getPopulators().add(redstone);

        Ore diamond = Ore.builder()
                .ore((BlockState) Blocks.DIAMOND_ORE.getDefaultState())
                .size(settings.diamondSize)
                .perChunk(settings.diamondCount)
                .height(VariableAmount.baseWithRandomAddition(settings.diamondMinHeight, settings.diamondMaxHeight))
                .build();
        gensettings.getPopulators().add(diamond);

        Ore lapis = Ore.builder()
                .ore((BlockState) Blocks.LAPIS_ORE.getDefaultState())
                .size(settings.lapisSize)
                .perChunk(settings.lapisCount)
                .height(VariableAmount.baseWithVariance(settings.lapisCenterHeight, settings.lapisSpread))
                .build();
        gensettings.getPopulators().add(lapis);

        if (theBiomeDecorator.sandPerChunk2 > 0) {
            SeaFloor sand = SeaFloor.builder()
                    .block((BlockState) Blocks.SAND.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(2, 5))
                    .depth(2)
                    .perChunk(theBiomeDecorator.sandPerChunk2)
                    .replace(WorldGenConstants.DIRT_OR_GRASS)
                    .build();
            gensettings.getPopulators().add(sand);
        }
        if (theBiomeDecorator.clayPerChunk > 0) {
            SeaFloor clay = SeaFloor.builder()
                    .block((BlockState) Blocks.CLAY.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(2, 2))
                    .depth(1)
                    .perChunk(theBiomeDecorator.clayPerChunk)
                    .replace(WorldGenConstants.DIRT)
                    .build();
            gensettings.getPopulators().add(clay);
        }
        if (theBiomeDecorator.sandPerChunk > 0) {
            SeaFloor gravelSeaFloor = SeaFloor.builder()
                    .block((BlockState) Blocks.GRAVEL.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(2, 4))
                    .depth(2)
                    .perChunk(theBiomeDecorator.sandPerChunk)
                    .replace(WorldGenConstants.DIRT_OR_GRASS)
                    .build();
            gensettings.getPopulators().add(gravelSeaFloor);
        }
        Forest forest = Forest.builder()
                .type(BiomeTreeTypes.OAK.getPopulatorObject(), 9)
                .type(BiomeTreeTypes.OAK.getLargePopulatorObject().get(), 1)
                .perChunk(VariableAmount.baseWithOptionalAddition(theBiomeDecorator.treesPerChunk, 1, 0.1))
                .build();
        gensettings.getPopulators().add(forest);

        if (theBiomeDecorator.bigMushroomsPerChunk > 0) {
            BigMushroom mushroom = BigMushroom.builder()
                    .mushroomsPerChunk(theBiomeDecorator.bigMushroomsPerChunk)
                    .type(MushroomTypes.BROWN.getPopulatorObject(), 1)
                    .type(MushroomTypes.RED.getPopulatorObject(), 1)
                    .build();
            gensettings.getPopulators().add(mushroom);
        }
        if (theBiomeDecorator.flowersPerChunk > 0) {
            Flower flower = Flower.builder()
                    .perChunk(theBiomeDecorator.flowersPerChunk * 64)
                    .type(PlantTypes.DANDELION, 2)
                    .type(PlantTypes.POPPY, 1)
                    .build();
            gensettings.getPopulators().add(flower);
        }
        if (theBiomeDecorator.grassPerChunk > 0) {
            Shrub grass = Shrub.builder()
                    .perChunk(theBiomeDecorator.grassPerChunk * 128)
                    .type(ShrubTypes.TALL_GRASS, 1)
                    .build();
            gensettings.getPopulators().add(grass);
        }
        if (theBiomeDecorator.deadBushPerChunk > 0) {
            DeadBush deadBush = DeadBush.builder()
                    .perChunk(theBiomeDecorator.deadBushPerChunk)
                    .build();
            gensettings.getPopulators().add(deadBush);
        }
        if (theBiomeDecorator.waterlilyPerChunk > 0) {
            WaterLily waterLily = WaterLily.builder()
                    .perChunk(theBiomeDecorator.waterlilyPerChunk * 10)
                    .build();
            gensettings.getPopulators().add(waterLily);
        }
        ChanceTable<MushroomType> types = new ChanceTable<MushroomType>();
        types.add(new WeightedObject<>(MushroomTypes.BROWN, 2));
        types.add(new WeightedObject<>(MushroomTypes.RED, 1));
        types.add(new EmptyObject<>(5));
        Mushroom smallMushroom = Mushroom.builder()
                .types(types)
                .mushroomsPerChunk(theBiomeDecorator.mushroomsPerChunk + 1)
                .build();
        gensettings.getPopulators().add(smallMushroom);
        Reed reed = Reed.builder()
                .perChunk(theBiomeDecorator.reedsPerChunk + 10)
                .reedHeight(VariableAmount.baseWithRandomAddition(2, VariableAmount.baseWithRandomAddition(1, 3)))
                .build();
        gensettings.getPopulators().add(reed);
        Pumpkin pumpkin = Pumpkin.builder()
                .perChunk(64)
                .chance(1 / 32d)
                .build();
        gensettings.getPopulators().add(pumpkin);
        if (theBiomeDecorator.cactiPerChunk > 0) {
            Cactus cactus = Cactus.builder()
                    .cactiPerChunk(VariableAmount.baseWithOptionalAddition(0,
                            VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithOptionalAddition(2, 3, 0.5)), 0.8))
                    .build();
            gensettings.getPopulators().add(cactus);
        }
        if (theBiomeDecorator.generateLakes) {
            RandomBlock water = RandomBlock.builder()
                    .block((BlockState) Blocks.FLOWING_WATER.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0, VariableAmount.baseWithRandomAddition(8, 248)))
                    .perChunk(50)
                    .placementTarget(WorldGenConstants.CAVE_LIQUIDS)
                    .build();
            gensettings.getPopulators().add(water);
            RandomBlock lava = RandomBlock.builder()
                    .block((BlockState) Blocks.FLOWING_LAVA.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0,
                            VariableAmount.baseWithRandomAddition(8, VariableAmount.baseWithRandomAddition(8, 240))))
                    .perChunk(20)
                    .placementTarget(WorldGenConstants.CAVE_LIQUIDS)
                    .build();
            gensettings.getPopulators().add(lava);
        }

    }

    @Inject(method = "registerBiome", at = @At("HEAD"))
    private static void onRegisterBiome(int id, String name, Biome biome, CallbackInfo ci) {
        final String modId = SpongeImplHooks.getModIdFromClass(biome.getClass());
        final String biomeName = name.toLowerCase().replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");

        ((IMixinBiome) biome).setModId(modId);
        ((IMixinBiome) biome).setId(modId + ":" + biomeName);
    }

    @Override
    public String getName() {
        return this.biomeName;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        checkState(this.id == null, "Attempt made to set ID!");

        this.id = id;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public void setModId(String modId) {
        checkState(this.modId == null, "Attempt made to set Mod ID!");

        this.modId = modId;
    }

    @Override
    public double getTemperature() {
        return this.temperature;
    }

    @Override
    public double getHumidity() {
        return this.rainfall;
    }
}
