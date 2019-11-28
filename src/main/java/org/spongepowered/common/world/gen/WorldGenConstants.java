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
package org.spongepowered.common.world.gen;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.Blocks;
import net.minecraft.block.SandBlock;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.ExtremeHillsBiome;
import net.minecraft.world.biome.ForestBiome;
import net.minecraft.world.biome.TaigaBiome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.EndChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.NetherChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.EmptyObject;
import org.spongepowered.api.util.weighted.SeededVariableAmount;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.populator.BigMushroom;
import org.spongepowered.api.world.gen.populator.Cactus;
import org.spongepowered.api.world.gen.populator.DeadBush;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Fossil;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.api.world.gen.populator.Melon;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.populator.NetherFire;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.Pumpkin;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.populator.Reed;
import org.spongepowered.api.world.gen.populator.SeaFloor;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.populator.Vine;
import org.spongepowered.api.world.gen.populator.WaterLily;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.common.mixin.core.world.biome.BiomeDecoratorAccessor;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.populators.FlowerForestSupplier;
import org.spongepowered.common.world.gen.populators.HellMushroomPopulator;
import org.spongepowered.common.world.gen.populators.RoofedForestPopulator;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Predicate;

public final class WorldGenConstants {

    public static final String VILLAGE_FLAG = "VILLAGE";

    private static final Class<?>[] MIXINED_CHUNK_PROVIDERS =
            new Class<?>[] {OverworldChunkGenerator.class, FlatChunkGenerator.class, NetherChunkGenerator.class, EndChunkGenerator.class};

    public static boolean isValid(final ChunkGenerator cp, final Class<?> api_type) {
        if (api_type.isInstance(cp)) {
            for (final Class<?> mixind : MIXINED_CHUNK_PROVIDERS) {
                if (cp.getClass().equals(mixind)) {
                    return true;
                }
                // If our chunk provider is an instance of one of our mixed in classes but is not the class
                // then its a custom chunk provider which is extending one of the vanilla classes but if we
                // use it as a generation populator directly then we would lose the custom logic of the
                // extending class so we wrap it instead so that the provideChunk method is called.
                if(mixind.isInstance(cp)) {
                    // This checks that if the custom generator in fact does directly implement our interface,
                    // with the assurance (according to Reflection API) that the target class is actually
                    // extending the api type interface. This allows for mods that use our api and extend
                    // a mixed in type target to still implement our interface.
                    for (final Class<?> anInterface : cp.getClass().getInterfaces()) {
                        if (api_type.equals(anInterface)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static final SeededVariableAmount<Double> GROUND_COVER_DEPTH = new SeededVariableAmount<Double>() {

        @Override
        public double getAmount(final Random rand, final Double seed) {
            return (int) (seed / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        }

    };

    public static final Predicate<BlockState> DIRT_OR_GRASS = (input) -> {
        return input.getType().equals(BlockTypes.DIRT) || input.getType().equals(BlockTypes.GRASS);
    };

    public static final Predicate<BlockState> DIRT = (input) -> {
        return input.getType().equals(BlockTypes.DIRT);
    };

    public static final Predicate<BlockState> STONE = (input) -> {
        return input.getType().equals(BlockTypes.STONE);
    };

    public static final Predicate<Location<World>> STONE_LOCATION = (input) -> {
        return input.getBlock().getType().equals(BlockTypes.STONE);
    };

    public static final Predicate<Location<World>> CAVE_LIQUIDS = (input) -> {
        if (input.getBlockY() <= 0 || input.getBlockY() >= 255) {
            return false;
        }
        if (input.add(0, 1, 0).getBlock().getType() != BlockTypes.STONE || input.add(0, -1, 0).getBlock().getType() != BlockTypes.STONE
                || (input.getBlock().getType() != BlockTypes.STONE && input.getBlock().getType() != BlockTypes.AIR)) {
            return false;
        }
        int air = 0;
        int stone = 0;
        if (input.add(1, 0, 0).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(-1, 0, 0).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(0, 0, 1).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(0, 0, -1).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(1, 0, 0).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (input.add(-1, 0, 0).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (input.add(0, 0, 1).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (input.add(0, 0, -1).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (air == 1 && stone == 3) {
            return true;
        }
        return false;
    };

    public static final Predicate<Location<World>> HELL_LAVA = (input) -> {
        if (input.add(0, 1, 0).getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        } else if (input.getBlockType() != BlockTypes.AIR && input.getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        }
        int i = 0;

        if (input.add(-1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, -1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, 1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, -1, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        int j = 0;

        if (input.add(-1, 0, 0).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(1, 0, 0).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(0, 0, -1).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(0, 0, 1).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(0, -1, 0).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (i == 4 && j == 1) {
            return true;
        }

        return false;
    };

    public static final Predicate<Location<World>> HELL_LAVA_ENCLOSED = (input) -> {
        if (input.add(0, 1, 0).getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        } else if (input.getBlockType() != BlockTypes.AIR && input.getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        }
        int i = 0;

        if (input.add(-1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, -1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, 1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, -1, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (i == 5) {
            return true;
        }
        return false;
    };

    public static boolean lightingEnabled = true;

    public static void disableLighting() {
        lightingEnabled = false;
    }

    public static void enableLighting() {
        lightingEnabled = true;
    }

    private WorldGenConstants() {

    }

    // Temporary while Mixins issue gets fixed for referencing accessor mixins within another mixin
    public static void buildPopulators(final net.minecraft.world.World world, final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator, final net.minecraft.block.BlockState topBlock,
        final net.minecraft.block.BlockState fillerBlock) {

        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((BlockState) topBlock, SeededVariableAmount.fixed(1)));
        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((BlockState) fillerBlock, GROUND_COVER_DEPTH));
        if (fillerBlock.getBlock() == Blocks.SAND) {
            final BlockType type;
            if (fillerBlock.get(SandBlock.VARIANT) == SandBlock.EnumType.RED_SAND) {
                type = BlockTypes.RED_SANDSTONE;
            } else {
                type = BlockTypes.SANDSTONE;
            }
            gensettings.getGroundCoverLayers().add(new SandstoneGroundCoverLayer(type.getDefaultState()));
        }

        final String s = world.getWorldInfo().getGeneratorOptions();
        final ChunkGeneratorSettings settings = ChunkGeneratorSettings.Factory.jsonToFactory(s).build();

        final Ore dirt = Ore.builder()
                .ore((BlockState) Blocks.DIRT.getDefaultState())
                .size(settings.dirtSize)
                .perChunk(settings.dirtCount)
                .height(VariableAmount.baseWithRandomAddition(settings.dirtMinHeight, settings.dirtMaxHeight - settings.dirtMinHeight))
                .build();
        gensettings.getPopulators().add(dirt);

        final Ore gravel = Ore.builder()
                .ore((BlockState) Blocks.GRAVEL.getDefaultState())
                .size(settings.gravelSize)
                .perChunk(settings.gravelCount)
                .height(VariableAmount.baseWithRandomAddition(settings.gravelMinHeight, settings.gravelMaxHeight - settings.gravelMinHeight))
                .build();
        gensettings.getPopulators().add(gravel);

        final Ore diorite = Ore.builder()
                .ore((BlockState) Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE))
                .size(settings.dioriteSize)
                .perChunk(settings.dioriteCount)
                .height(VariableAmount.baseWithRandomAddition(settings.dioriteMinHeight, settings.dioriteMaxHeight - settings.dioriteMinHeight))
                .build();
        gensettings.getPopulators().add(diorite);

        final Ore granite = Ore.builder()
                .ore((BlockState) Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE))
                .size(settings.graniteSize)
                .perChunk(settings.graniteCount)
                .height(VariableAmount.baseWithRandomAddition(settings.graniteMinHeight, settings.graniteMaxHeight - settings.graniteMinHeight))
                .build();
        gensettings.getPopulators().add(granite);

        final Ore andesite = Ore.builder()
                .ore((BlockState) Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE))
                .size(settings.andesiteSize)
                .perChunk(settings.andesiteCount)
                .height(VariableAmount.baseWithRandomAddition(settings.andesiteMinHeight, settings.andesiteMaxHeight - settings.andesiteMinHeight))
                .build();
        gensettings.getPopulators().add(andesite);

        final Ore coal = Ore.builder()
                .ore((BlockState) Blocks.COAL_ORE.getDefaultState())
                .size(settings.coalSize)
                .perChunk(settings.coalCount)
                .height(VariableAmount.baseWithRandomAddition(settings.coalMinHeight, settings.coalMaxHeight - settings.coalMinHeight))
                .build();
        gensettings.getPopulators().add(coal);

        final Ore iron = Ore.builder()
                .ore((BlockState) Blocks.IRON_ORE.getDefaultState())
                .size(settings.ironSize)
                .perChunk(settings.ironCount)
                .height(VariableAmount.baseWithRandomAddition(settings.ironMinHeight, settings.ironMaxHeight - settings.ironMinHeight))
                .build();
        gensettings.getPopulators().add(iron);

        final Ore gold = Ore.builder()
                .ore((BlockState) Blocks.GOLD_ORE.getDefaultState())
                .size(settings.goldSize)
                .perChunk(settings.goldCount)
                .height(VariableAmount.baseWithRandomAddition(settings.goldMinHeight, settings.goldMaxHeight - settings.goldMinHeight))
                .build();
        gensettings.getPopulators().add(gold);

        final Ore redstone = Ore.builder()
                .ore((BlockState) Blocks.REDSTONE_ORE.getDefaultState())
                .size(settings.redstoneSize)
                .perChunk(settings.redstoneCount)
                .height(VariableAmount.baseWithRandomAddition(settings.redstoneMinHeight, settings.redstoneMaxHeight - settings.redstoneMinHeight))
                .build();
        gensettings.getPopulators().add(redstone);

        final Ore diamond = Ore.builder()
                .ore((BlockState) Blocks.DIAMOND_ORE.getDefaultState())
                .size(settings.diamondSize)
                .perChunk(settings.diamondCount)
                .height(VariableAmount.baseWithRandomAddition(settings.diamondMinHeight, settings.diamondMaxHeight - settings.diamondMinHeight))
                .build();
        gensettings.getPopulators().add(diamond);

        final Ore lapis = Ore.builder()
                .ore((BlockState) Blocks.LAPIS_ORE.getDefaultState())
                .size(settings.lapisSize)
                .perChunk(settings.lapisCount)
                .height(VariableAmount.baseWithVariance(settings.lapisCenterHeight, settings.lapisSpread))
                .build();
        gensettings.getPopulators().add(lapis);

        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        if (accessor.accessor$getSandPerChunk() > 0) {
            final SeaFloor sand = SeaFloor.builder()
                    .block((BlockState) Blocks.SAND.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(2, 5))
                    .depth(2)
                    .perChunk(accessor.accessor$getSandPerChunk())
                    .replace(DIRT_OR_GRASS)
                    .build();
            gensettings.getPopulators().add(sand);
        }
        if (accessor.accessor$getClayPerChunk() > 0) {
            final SeaFloor clay = SeaFloor.builder()
                    .block((BlockState) Blocks.CLAY.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(2, 2))
                    .depth(1)
                    .perChunk(accessor.accessor$getClayPerChunk())
                    .replace(DIRT)
                    .build();
            gensettings.getPopulators().add(clay);
        }
        if (accessor.accessor$getGravelPerChunk() > 0) {
            final SeaFloor gravelSeaFloor = SeaFloor.builder()
                    .block((BlockState) Blocks.GRAVEL.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(2, 4))
                    .depth(2)
                    .perChunk(accessor.accessor$getGravelPerChunk())
                    .replace(DIRT_OR_GRASS)
                    .build();
            gensettings.getPopulators().add(gravelSeaFloor);
        }
        final Forest forest = Forest.builder()
                .type(BiomeTreeTypes.OAK.getPopulatorObject(), 9)
                .type(BiomeTreeTypes.OAK.getLargePopulatorObject().get(), 1)
                .perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 1, 0.1))
                .build();
        gensettings.getPopulators().add(forest);

        if (accessor.accessor$getBigMushroomsPerChunk() > 0) {
            final BigMushroom mushroom = BigMushroom.builder()
                    .mushroomsPerChunk(accessor.accessor$getBigMushroomsPerChunk())
                    .type(MushroomTypes.BROWN.getPopulatorObject(), 1)
                    .type(MushroomTypes.RED.getPopulatorObject(), 1)
                    .build();
            gensettings.getPopulators().add(mushroom);
        }
        if (accessor.accessor$getFlowersPerChunk() > 0) {
            final Flower flower = Flower.builder()
                    .perChunk(accessor.accessor$getFlowersPerChunk() * 64)
                    .type(PlantTypes.DANDELION, 2)
                    .type(PlantTypes.POPPY, 1)
                    .build();
            gensettings.getPopulators().add(flower);
        }
        if (accessor.accessor$getGrassPerChunk() > 0) {
            final Shrub grass = Shrub.builder()
                    .perChunk(accessor.accessor$getGrassPerChunk() * 128)
                    .type(ShrubTypes.TALL_GRASS, 1)
                    .build();
            gensettings.getPopulators().add(grass);
        }
        if (accessor.accessor$getDeadBushPerChunk() > 0) {
            final DeadBush deadBush = DeadBush.builder()
                    .perChunk(accessor.accessor$getDeadBushPerChunk())
                    .build();
            gensettings.getPopulators().add(deadBush);
        }
        if (accessor.accessor$getWaterLilyPerChunk() > 0) {
            final WaterLily waterLily = WaterLily.builder()
                    .perChunk(accessor.accessor$getWaterLilyPerChunk() * 10)
                    .build();
            gensettings.getPopulators().add(waterLily);
        }
        final ChanceTable<MushroomType> types = new ChanceTable<>();
        types.add(new WeightedObject<>(MushroomTypes.BROWN, 2));
        types.add(new WeightedObject<>(MushroomTypes.RED, 1));
        types.add(new EmptyObject<>(5));
        final Mushroom smallMushroom = Mushroom.builder()
                .types(types)
                .mushroomsPerChunk(accessor.accessor$getMushroomsPerChunk() + 1)
                .build();
        gensettings.getPopulators().add(smallMushroom);
        final Reed reed = Reed.builder()
                .perChunk(accessor.accessor$getReedsPerChunk() + 10)
                .reedHeight(VariableAmount.baseWithRandomAddition(2, VariableAmount.baseWithRandomAddition(1, 3)))
                .build();
        gensettings.getPopulators().add(reed);
        final Pumpkin pumpkin = Pumpkin.builder()
                .perChunk(64)
                .chance(1 / 32d)
                .build();
        gensettings.getPopulators().add(pumpkin);
        if (accessor.accessor$getCactiPerChunk() > 0) {
            final Cactus cactus = Cactus.builder()
                    .cactiPerChunk(VariableAmount.baseWithOptionalAddition(0,
                            VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithOptionalAddition(2, 3, 0.5)), 0.8))
                    .build();
            gensettings.getPopulators().add(cactus);
        }
        if (decorator.generateFalls) {
            final RandomBlock water = RandomBlock.builder()
                    .block((BlockState) Blocks.FLOWING_WATER.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0, VariableAmount.baseWithRandomAddition(8, 248)))
                    .perChunk(50)
                    .placementTarget(CAVE_LIQUIDS)
                    .build();
            gensettings.getPopulators().add(water);
            final RandomBlock lava = RandomBlock.builder()
                    .block((BlockState) Blocks.FLOWING_LAVA.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0,
                            VariableAmount.baseWithRandomAddition(8, VariableAmount.baseWithRandomAddition(8, 240))))
                    .perChunk(20)
                    .placementTarget(CAVE_LIQUIDS)
                    .build();
            gensettings.getPopulators().add(lava);
        }
    }

    public static void resetGrassAndFlowers(final BiomeDecorator decorator) {
        // set flowers and grass to zero as they are handles by the plains grass
        // populator
        ((BiomeDecoratorAccessor) decorator).accessor$setFlowersPerChunk(0);
        ((BiomeDecoratorAccessor) decorator).accessor$setGrassPerChunk(0);
    }

    public static void buildHellPopulators(final net.minecraft.world.World world, final SpongeBiomeGenerationSettings gensettings) {
        final RandomBlock lava1 = RandomBlock.builder()
                .block((BlockState) Blocks.FLOWING_LAVA.getDefaultState())
                .perChunk(8)
                .height(VariableAmount.baseWithRandomAddition(4, 120))
                .placementTarget(HELL_LAVA)
                .build();
        gensettings.getPopulators().add(lava1);

        final NetherFire fire = NetherFire.builder()
                .perChunk(VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithRandomAddition(1, 10)))
                .perCluster(64)
                .build();
        gensettings.getPopulators().add(fire);

        final Glowstone glowstone1 = Glowstone.builder()
                .blocksPerCluster(1500)
                .clusterHeight(VariableAmount.baseWithRandomAddition(-11, 12))
                .perChunk(VariableAmount.baseWithRandomAddition(0, VariableAmount.baseWithRandomAddition(1, 10)))
                .height(VariableAmount.baseWithRandomAddition(4, 120))
                .build();
        gensettings.getPopulators().add(glowstone1);

        final Glowstone glowstone2 = Glowstone.builder()
                .blocksPerCluster(1500)
                .clusterHeight(VariableAmount.baseWithRandomAddition(0, 12))
                .perChunk(10)
                .height(VariableAmount.baseWithRandomAddition(0, 128))
                .build();
        gensettings.getPopulators().add(glowstone2);

        final ChanceTable<MushroomType> types = new ChanceTable<>();
        types.add(new WeightedObject<>(MushroomTypes.BROWN, 1));
        types.add(new EmptyObject<>(1));
        final HellMushroomPopulator smallMushroom = new HellMushroomPopulator();
        smallMushroom.setMushroomsPerChunk(1);
        smallMushroom.getTypes().addAll(types);
        gensettings.getPopulators().add(smallMushroom);

        final ChanceTable<MushroomType> types2 = new ChanceTable<>();
        types2.add(new WeightedObject<>(MushroomTypes.RED, 1));
        types2.add(new EmptyObject<>(1));
        final HellMushroomPopulator smallMushroom2 = new HellMushroomPopulator();
        smallMushroom2.setMushroomsPerChunk(1);
        smallMushroom2.getTypes().addAll(types2);
        gensettings.getPopulators().add(smallMushroom2);

        final Ore quartz = Ore.builder()
                .height(VariableAmount.baseWithRandomAddition(10, 108))
                .ore(BlockTypes.QUARTZ_ORE.getDefaultState())
                .perChunk(16)
                .placementCondition((o) -> o != null && o.getType() == BlockTypes.NETHERRACK)
                .size(14)
                .build();
        gensettings.getPopulators().add(quartz);

        final int halfSeaLevel = world.getSeaLevel() / 2 + 1;
        final Ore magma = Ore.builder()
                .height(VariableAmount.baseWithRandomAddition(halfSeaLevel - 5, 10))
                .ore(BlockTypes.MAGMA.getDefaultState())
                .perChunk(4)
                .placementCondition((o) -> o != null && o.getType() == BlockTypes.NETHERRACK)
                .size(33)
                .build();
        gensettings.getPopulators().add(magma);

        final RandomBlock lava2 = RandomBlock.builder()
                .block((BlockState) Blocks.FLOWING_LAVA.getDefaultState())
                .perChunk(16)
                .height(VariableAmount.baseWithRandomAddition(10, 108))
                .placementTarget(HELL_LAVA_ENCLOSED)
                .build();
        gensettings.getPopulators().add(lava2);
    }

    public static void buildForestPopulators(final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator, final ForestBiome.Type type) {
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        if (type == ForestBiome.Type.ROOFED) {
            final RoofedForestPopulator forest = new RoofedForestPopulator();
            gensettings.getPopulators().add(0, forest);
        } else {
            final Forest.Builder forest = Forest.builder();
            forest.perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 1, 0.1));
            if (type == ForestBiome.Type.BIRCH) {
                forest.type(BiomeTreeTypes.BIRCH.getPopulatorObject(), 1);
            } else {
                forest.type(BiomeTreeTypes.OAK.getPopulatorObject(), 4);
                forest.type(BiomeTreeTypes.BIRCH.getPopulatorObject(), 1);
            }
            gensettings.getPopulators().add(0, forest.build());
        }
        if (type == ForestBiome.Type.FLOWER) {
            gensettings.getPopulators().removeAll(gensettings.getPopulators(Flower.class));
            final Flower flower = Flower.builder()
                    .perChunk(accessor.accessor$getFlowersPerChunk() * 64)
                    .supplier(new FlowerForestSupplier())
                    .build();
            gensettings.getPopulators().add(flower);
        }
    }

    public static void buildHillsPopulator(final SpongeBiomeGenerationSettings gensettings, final ExtremeHillsBiome.Type type, final BiomeDecorator decorator) {
        gensettings.getGroundCoverLayers().clear();
        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((stoneNoise) -> {
            net.minecraft.block.BlockState result = Blocks.GRASS.getDefaultState();
            if ((stoneNoise < -1.0D || stoneNoise > 2.0D) && type == ExtremeHillsBiome.Type.MUTATED) {
                result = Blocks.GRAVEL.getDefaultState();
            } else if (stoneNoise > 1.0D && type != ExtremeHillsBiome.Type.EXTRA_TREES) {
                result = Blocks.STONE.getDefaultState();
            }
            return (BlockState) result;
        } , SeededVariableAmount.fixed(1)));
        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((stoneNoise) -> {
            net.minecraft.block.BlockState result = Blocks.DIRT.getDefaultState();
            if ((stoneNoise < -1.0D || stoneNoise > 2.0D) && type == ExtremeHillsBiome.Type.MUTATED) {
                result = Blocks.GRAVEL.getDefaultState();
            } else if (stoneNoise > 1.0D && type != ExtremeHillsBiome.Type.EXTRA_TREES) {
                result = Blocks.STONE.getDefaultState();
            }
            return (BlockState) result;
        } , GROUND_COVER_DEPTH));

        final RandomBlock emerald = RandomBlock.builder()
                .block((BlockState) Blocks.EMERALD_ORE.getDefaultState())
                .placementTarget(STONE_LOCATION)
                .perChunk(VariableAmount.baseWithRandomAddition(3, 6))
                .height(VariableAmount.baseWithRandomAddition(4, 28))
                .build();
        gensettings.getPopulators().add(emerald);

        final Ore silverfish = Ore.builder()
                .ore((BlockState) Blocks.MONSTER_EGG.getDefaultState().withProperty(SilverfishBlock.VARIANT, SilverfishBlock.EnumType.STONE))
                .perChunk(7)
                .height(VariableAmount.baseWithRandomAddition(0, 64))
                .size(9)
                .build();
        gensettings.getPopulators().add(silverfish);

        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(((BiomeDecoratorAccessor) decorator).accessor$getTreesPerChunk(), 1, 0.1));
        forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 20);
        forest.type(BiomeTreeTypes.OAK.getPopulatorObject(), 9);
        forest.type(BiomeTreeTypes.OAK.getLargePopulatorObject().get(), 1);
        gensettings.getPopulators().add(0, forest.build());
    }

    public static void buildJunglePopulators(final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator, final boolean isEdge) {
        for (final Iterator<Populator> it = gensettings.getPopulators().iterator(); it.hasNext();) {
            final Populator next = it.next();
            if (next instanceof Shrub) {
                final Shrub s = (Shrub) next;
                if (s.getTypes().size() == 1) {
                    final TableEntry<ShrubType> entry = s.getTypes().getEntries().get(0);
                    if (entry instanceof WeightedObject && ((WeightedObject<ShrubType>) entry).get() == ShrubTypes.TALL_GRASS) {
                        it.remove();
                    }
                }
            }
        }
        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        final Shrub grass = Shrub.builder()
                .perChunk(accessor.accessor$getGrassPerChunk() * 128)
                .type(ShrubTypes.FERN, 1)
                .type(ShrubTypes.TALL_GRASS, 3)
                .build();
        gensettings.getPopulators().add(grass);
        final Melon melon = Melon.builder()
                .perChunk(64)
                .build();
        gensettings.getPopulators().add(melon);

        final Vine vine = Vine.builder()
                .perChunk(50)
                .build();
        gensettings.getPopulators().add(vine);

        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 1, 0.1));
        forest.type(BiomeTreeTypes.OAK.getLargePopulatorObject().get(), 1);
        forest.type(BiomeTreeTypes.JUNGLE_BUSH.getPopulatorObject(), 4.5);
        if (!isEdge) {
            forest.type(BiomeTreeTypes.JUNGLE.getLargePopulatorObject().get(), 1.2);
            forest.type(BiomeTreeTypes.JUNGLE.getPopulatorObject(), 3);
        } else {
            forest.type(BiomeTreeTypes.JUNGLE.getPopulatorObject(), 4.5);
        }
        gensettings.getPopulators().add(0, forest.build());
    }

    public static void buildMesaPopulators(
        final net.minecraft.world.World world, final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator) {
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

        gensettings.getGroundCoverLayers().clear();
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest forest = Forest.builder()
          .perChunk(VariableAmount.baseWithOptionalAddition(((BiomeDecoratorAccessor) decorator).accessor$getTreesPerChunk(), 1, 0.1))
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

    public static void buildSavanaPopulators(final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator) {
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest.Builder forest = Forest.builder();
        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        forest.perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 1, 0.1));
        forest.type(BiomeTreeTypes.OAK.getPopulatorObject(), 1);
        forest.type(BiomeTreeTypes.SAVANNA.getPopulatorObject(), 4);
        gensettings.getPopulators().add(0, forest.build());
    }

    public static void buildSnowPopulators(final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator) {
        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 2, 0.1));
        forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 1);
        gensettings.getPopulators().add(0, forest.build());
    }

    public static void buildSwampPopulators(final SpongeBiomeGenerationSettings gensettings, final BiomeDecorator decorator) {
        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 1, 0.1));
        forest.type(BiomeTreeTypes.SWAMP.getPopulatorObject(), 1);
        gensettings.getPopulators().add(0, forest.build());
        gensettings.getPopulators().add(Fossil.builder().probability(1 / 64.0).build());
    }

    public static void buildTaigaPopulators(final SpongeBiomeGenerationSettings gensettings, final TaigaBiome.Type type, final net.minecraft.block.BlockState fillerBlock,
        final BiomeDecorator decorator) {
        if (type == TaigaBiome.Type.MEGA || type == TaigaBiome.Type.MEGA_SPRUCE) {
            gensettings.getGroundCoverLayers().clear();
            gensettings.getGroundCoverLayers().add(new GroundCoverLayer((Double seed) -> {
                if (seed > 1.75D) {
                    return (BlockState) Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
                } else if (seed > -0.95D) {
                    return (BlockState) Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
                }
                return (BlockState) Blocks.GRASS.getDefaultState();

            } , GROUND_COVER_DEPTH));
            gensettings.getGroundCoverLayers().add(new GroundCoverLayer((BlockState) fillerBlock, GROUND_COVER_DEPTH));

        }
        final BiomeDecoratorAccessor accessor = (BiomeDecoratorAccessor) decorator;
        for (final Iterator<Shrub> it = gensettings.getPopulators(Shrub.class).iterator(); it.hasNext();) {
            final Shrub next = it.next();
            if (next.getTypes().size() == 1) {
                final TableEntry<ShrubType> entry = next.getTypes().getEntries().get(0);
                if (entry instanceof WeightedObject && ((WeightedObject<ShrubType>) entry).get() == ShrubTypes.TALL_GRASS) {
                    it.remove();
                }
            }
        }
        final Shrub grass = Shrub.builder()
                .perChunk(accessor.accessor$getGrassPerChunk() * 128)
                .type(ShrubTypes.FERN, 4)
                .type(ShrubTypes.TALL_GRASS, 1)
                .build();
        gensettings.getPopulators().add(grass);
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        final Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(accessor.accessor$getTreesPerChunk(), 1, 0.1));
        if (type == TaigaBiome.Type.MEGA || type == TaigaBiome.Type.MEGA_SPRUCE) {
            if (type == TaigaBiome.Type.MEGA) {
                forest.type(BiomeTreeTypes.POINTY_TAIGA.getLargePopulatorObject().get(), 1);
                forest.type(BiomeTreeTypes.TALL_TAIGA.getLargePopulatorObject().get(), 12);
            } else {
                forest.type(BiomeTreeTypes.TALL_TAIGA.getLargePopulatorObject().get(), 13);
            }
            forest.type(BiomeTreeTypes.POINTY_TAIGA.getPopulatorObject(), 26 / 3d);
            forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 52 / 3d);
        } else {
            forest.type(BiomeTreeTypes.POINTY_TAIGA.getPopulatorObject(), 1);
            forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 2);
        }
        gensettings.getPopulators().add(0, forest.build());
    }
}
