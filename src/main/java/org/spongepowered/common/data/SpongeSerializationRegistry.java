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
package org.spongepowered.common.data;

import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.*;
import org.spongepowered.api.block.tileentity.carrier.*;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.*;
import org.spongepowered.api.data.manipulator.immutable.block.*;
import org.spongepowered.api.data.manipulator.immutable.entity.*;
import org.spongepowered.api.data.manipulator.immutable.item.*;
import org.spongepowered.api.data.manipulator.immutable.tileentity.*;
import org.spongepowered.api.data.manipulator.mutable.*;
import org.spongepowered.api.data.manipulator.mutable.block.*;
import org.spongepowered.api.data.manipulator.mutable.entity.*;
import org.spongepowered.api.data.manipulator.mutable.item.*;
import org.spongepowered.api.data.manipulator.mutable.tileentity.*;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.data.property.block.*;
import org.spongepowered.api.data.property.entity.*;
import org.spongepowered.api.data.property.item.*;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.SpongeFireworkEffectDataBuilder;
import org.spongepowered.common.data.builder.SpongeItemEnchantmentBuilder;
import org.spongepowered.common.data.builder.block.data.SpongePatternLayerBuilder;
import org.spongepowered.common.data.builder.block.tileentity.*;
import org.spongepowered.common.data.builder.item.SpongeItemStackDataBuilder;
import org.spongepowered.common.data.builder.item.SpongeItemStackSnapshotBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.block.ImmutableSpongeTreeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.item.ImmutableItemEnchantmentDataBuilder;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.manipulator.immutable.*;
import org.spongepowered.common.data.manipulator.immutable.block.*;
import org.spongepowered.common.data.manipulator.immutable.entity.*;
import org.spongepowered.common.data.manipulator.immutable.item.*;
import org.spongepowered.common.data.manipulator.immutable.tileentity.*;
import org.spongepowered.common.data.manipulator.mutable.*;
import org.spongepowered.common.data.manipulator.mutable.block.*;
import org.spongepowered.common.data.manipulator.mutable.entity.*;
import org.spongepowered.common.data.manipulator.mutable.item.*;
import org.spongepowered.common.data.manipulator.mutable.tileentity.*;
import org.spongepowered.common.data.processor.data.*;
import org.spongepowered.common.data.processor.data.block.*;
import org.spongepowered.common.data.processor.data.entity.*;
import org.spongepowered.common.data.processor.data.item.*;
import org.spongepowered.common.data.processor.data.tileentity.*;
import org.spongepowered.common.data.processor.value.*;
import org.spongepowered.common.data.processor.value.block.*;
import org.spongepowered.common.data.processor.value.entity.*;
import org.spongepowered.common.data.processor.value.item.*;
import org.spongepowered.common.data.processor.value.tileentity.*;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.property.store.block.*;
import org.spongepowered.common.data.property.store.entity.*;
import org.spongepowered.common.data.property.store.item.*;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.service.persistence.SpongeSerializationManager;

public class SpongeSerializationRegistry {

    public static void setupSerialization(Game game) {
        KeyRegistry.registerKeys();
        SpongeSerializationManager service = SpongeSerializationManager.getInstance();
        SpongeDataRegistry dataRegistry = SpongeDataRegistry.getInstance();
        // TileEntities
        service.registerBuilder(Banner.class, new SpongeBannerBuilder(game));
        service.registerBuilder(PatternLayer.class, new SpongePatternLayerBuilder());
        service.registerBuilder(BrewingStand.class, new SpongeBrewingStandBuilder(game));
        service.registerBuilder(Chest.class, new SpongeChestBuilder(game));
        service.registerBuilder(CommandBlock.class, new SpongeCommandBlockBuilder(game));
        service.registerBuilder(Comparator.class, new SpongeComparatorBuilder(game));
        service.registerBuilder(DaylightDetector.class, new SpongeDaylightBuilder(game));
        service.registerBuilder(Dispenser.class, new SpongeDispenserBuilder(game));
        service.registerBuilder(Dropper.class, new SpongeDropperBuilder(game));
        service.registerBuilder(EnchantmentTable.class, new SpongeEnchantmentTableBuilder(game));
        service.registerBuilder(EnderChest.class, new SpongeEnderChestBuilder(game));
        service.registerBuilder(EndPortal.class, new SpongeEndPortalBuilder(game));
        service.registerBuilder(Furnace.class, new SpongeFurnaceBuilder(game));
        service.registerBuilder(Hopper.class, new SpongeHopperBuilder(game));
        service.registerBuilder(MobSpawner.class, new SpongeMobSpawnerBuilder(game));
        service.registerBuilder(Note.class, new SpongeNoteBuilder(game));
        service.registerBuilder(Sign.class, new SpongeSignBuilder(game));
        service.registerBuilder(Skull.class, new SpongeSkullBuilder(game));

        // Block stuff
        service.registerBuilder(BlockSnapshot.class, new SpongeBlockSnapshotBuilder());
        service.registerBuilder(BlockState.class, new SpongeBlockStateBuilder());
        service.registerBuilderAndImpl(ImmutableTreeData.class, ImmutableSpongeTreeData.class, new ImmutableSpongeTreeDataBuilder());

        // Entity stuff
        service.registerBuilder(EntitySnapshot.class, new SpongeEntitySnapshotBuilder());

        // ItemStack stuff
        service.registerBuilder(ItemStack.class, new SpongeItemStackDataBuilder());
        service.registerBuilder(ItemStackSnapshot.class, new SpongeItemStackSnapshotBuilder());
        service.registerBuilder(ItemEnchantment.class, new SpongeItemEnchantmentBuilder());
        service.registerBuilderAndImpl(ImmutableEnchantmentData.class, ImmutableSpongeEnchantmentData.class,
                new ImmutableItemEnchantmentDataBuilder());
        service.registerBuilder(FireworkEffect.class, new SpongeFireworkEffectDataBuilder());

        // Data Manipulators

        final HealthDataProcessor healthProcessor = new HealthDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(HealthData.class, SpongeHealthData.class, ImmutableHealthData.class,
                ImmutableSpongeHealthData.class, healthProcessor);

        final IgniteableDataProcessor igniteableProcessor = new IgniteableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(IgniteableData.class, SpongeIgniteableData.class, ImmutableIgniteableData.class,
                ImmutableSpongeIgniteableData.class, igniteableProcessor);

        final DisplayNameDataProcessor displayNameDataProcessor = new DisplayNameDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DisplayNameData.class, SpongeDisplayNameData.class,
                ImmutableDisplayNameData.class, ImmutableSpongeDisplayNameData.class, displayNameDataProcessor);

        final ColoredDataProcessor coloredDataProcessor = new ColoredDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ColoredData.class, SpongeColoredData.class,
                ImmutableColoredData.class, ImmutableSpongeColoredData.class, coloredDataProcessor);

        final CareerDataProcessor careerDataProcessor = new CareerDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(CareerData.class, SpongeCareerData.class, ImmutableCareerData.class,
                ImmutableSpongeCareerData.class, careerDataProcessor);

        final SignDataProcessor signDataProcessor = new SignDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SignData.class, SpongeSignData.class,
                ImmutableSignData.class, ImmutableSpongeSignData.class, signDataProcessor);

        final FlyingDataProcessor flyingDataProcessor = new FlyingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FlyingData.class, SpongeFlyingData.class, ImmutableFlyingData.class,
                ImmutableSpongeFlyingData.class, flyingDataProcessor);

        final FlyingAbilityDataProcessor flyingAbilityDataProcessor = new FlyingAbilityDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FlyingAbilityData.class, SpongeFlyingAbilityData.class, ImmutableFlyingAbilityData.class,
                ImmutableSpongeFlyingAbilityData.class, flyingAbilityDataProcessor);

        final SkullDataProcessor skullDataProcessor = new SkullDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                ImmutableSpongeSkullData.class, skullDataProcessor);

        final VelocityDataProcessor velocityDataProcessor = new VelocityDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(VelocityData.class, SpongeVelocityData.class, ImmutableVelocityData.class,
                ImmutableSpongeVelocityData.class, velocityDataProcessor);

        final FoodDataProcessor foodDataProcessor = new FoodDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FoodData.class, SpongeFoodData.class, ImmutableFoodData.class,
                ImmutableSpongeFoodData.class, foodDataProcessor);

        final BreathingDataProcessor breathingDataProcessor = new BreathingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BreathingData.class, SpongeBreathingData.class, ImmutableBreathingData.class,
                ImmutableSpongeBreathingData.class, breathingDataProcessor);

        final GameModeDataProcessor gameModeDataProcessor = new GameModeDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(GameModeData.class, SpongeGameModeData.class, ImmutableGameModeData.class,
                ImmutableSpongeGameModeData.class, gameModeDataProcessor);

        final ScreamingDataProcessor screamingDataProcessor = new ScreamingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ScreamingData.class, SpongeScreamingData.class, ImmutableScreamingData.class,
                ImmutableSpongeScreamingData.class, screamingDataProcessor);

        final RepresentedItemDataProcessor representedItemDataProcessor = new RepresentedItemDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, representedItemDataProcessor);

        final ItemEnchantmentDataProcessor itemEnchantmentDataProcessor = new ItemEnchantmentDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(EnchantmentData.class, SpongeEnchantmentData.class, ImmutableEnchantmentData.class,
                ImmutableSpongeEnchantmentData.class, itemEnchantmentDataProcessor);

        final ItemLoreDataProcessor itemLoreDataProcessor = new ItemLoreDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(LoreData.class, SpongeLoreData.class, ImmutableLoreData.class, ImmutableSpongeLoreData.class,
                itemLoreDataProcessor);

        final ItemPagedDataProcessor itemPagedDataProcessor = new ItemPagedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PagedData.class, SpongePagedData.class, ImmutablePagedData.class, ImmutableSpongePagedData.class,
                itemPagedDataProcessor);

        final HorseDataProcessor horseDataProcessor = new HorseDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(HorseData.class, SpongeHorseData.class, ImmutableHorseData.class,
                ImmutableSpongeHorseData.class, horseDataProcessor);

        final SneakingDataProcessor sneakingDataProcessor = new SneakingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SneakingData.class, SpongeSneakingData.class, ImmutableSneakingData.class,
                ImmutableSpongeSneakingData.class, sneakingDataProcessor);

        final GoldenAppleDataProcessor goldenAppleDataProcessor = new GoldenAppleDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(GoldenAppleData.class, SpongeGoldenAppleData.class, ImmutableGoldenAppleData.class,
                ImmutableSpongeGoldenAppleData.class, goldenAppleDataProcessor);

        final ExperienceHolderDataProcessor experienceHolderDataProcessor = new ExperienceHolderDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ExperienceHolderData.class, SpongeExperienceHolderData.class, ImmutableExperienceHolderData.class,
                ImmutableSpongeExperienceHolderData.class, experienceHolderDataProcessor);

        final ItemAuthorDataProcessor itemAuthorDataProcessor = new ItemAuthorDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(AuthorData.class, SpongeAuthorData.class, ImmutableAuthorData.class,
                ImmutableSpongeAuthorData.class, itemAuthorDataProcessor);

        final BreakableDataProcessor breakableDataProcessor = new BreakableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BreakableData.class, SpongeBreakableData.class, ImmutableBreakableData.class,
                ImmutableSpongeBreakableData.class, breakableDataProcessor);

        final PlaceableDataProcessor placeableDataProcessor = new PlaceableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PlaceableData.class, SpongePlaceableData.class, ImmutablePlaceableData.class,
                ImmutableSpongePlaceableData.class, placeableDataProcessor);

        final MovementSpeedDataProcessor movementSpeedDataProcessor = new MovementSpeedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(MovementSpeedData.class, SpongeMovementSpeedData.class, ImmutableMovementSpeedData.class,
                ImmutableSpongeMovementSpeedData.class, movementSpeedDataProcessor);

        final SlimeDataProcessor slimeDataProcessor = new SlimeDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SlimeData.class, SpongeSlimeData.class, ImmutableSlimeData.class, ImmutableSpongeSlimeData.class,
                slimeDataProcessor);

        final VillagerZombieProcessor villagerZombieProcessor = new VillagerZombieProcessor();
        dataRegistry.registerDataProcessorAndImpl(VillagerZombieData.class, SpongeVillagerZombieData.class, ImmutableVillagerZombieData.class,
                ImmutableSpongeVillagerZombieData.class, villagerZombieProcessor);

        final PlayingDataProcessor playingDataProcessor = new PlayingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PlayingData.class, SpongePlayingData.class, ImmutablePlayingData.class,
                ImmutableSpongePlayingData.class, playingDataProcessor);

        final SittingDataProcessor sittingDataProcessor = new SittingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SittingData.class, SpongeSittingData.class, ImmutableSittingData.class,
                ImmutableSpongeSittingData.class, sittingDataProcessor);

        final ShearedDataProcessor shearedDataProcessor = new ShearedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ShearedData.class, SpongeShearedData.class, ImmutableShearedData.class,
                ImmutableSpongeShearedData.class, shearedDataProcessor);

        final PigSaddleDataProcessor pigSaddleDataProcessor = new PigSaddleDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PigSaddleData.class, SpongePigSaddleData.class, ImmutablePigSaddleData.class,
                ImmutableSpongePigSaddleData.class, pigSaddleDataProcessor);

        final TameableDataProcessor tameableDataProcessor = new TameableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(TameableData.class, SpongeTameableData.class, ImmutableTameableData.class,
                ImmutableSpongeTameableData.class, tameableDataProcessor);

        final WolfWetDataProcessor wolfWetDataProcessor = new WolfWetDataProcessor();
        final ItemWetDataProcessor itemWetDataProcessor = new ItemWetDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                wolfWetDataProcessor);
        dataRegistry.registerDataProcessorAndImpl(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                itemWetDataProcessor);

        final ElderDataProcessor elderDataProcessor = new ElderDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ElderData.class, SpongeElderData.class, ImmutableElderData.class, ImmutableSpongeElderData.class,
                elderDataProcessor);

        final CoalDataProcessor coalDataProcessor = new CoalDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(CoalData.class, SpongeCoalData.class, ImmutableCoalData.class,
                ImmutableSpongeCoalData.class, coalDataProcessor);

        final CookedFishDataProcessor cookedFishDataProcessor = new CookedFishDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(CookedFishData.class, SpongeCookedFishData.class, ImmutableCookedFishData.class,
                ImmutableSpongeCookedFishData.class, cookedFishDataProcessor);

        final FishDataProcessor fishDataProcessor = new FishDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FishData.class, SpongeFishData.class, ImmutableFishData.class,
                ImmutableSpongeFishData.class, fishDataProcessor);

        dataRegistry.registerDataProcessorAndImpl(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class,
                ImmutableRepresentedPlayerData.class, ImmutableSpongeRepresentedPlayerData.class,
                new SkullRepresentedPlayerDataProcessor());

        dataRegistry.registerDataProcessorAndImpl(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class,
                ImmutableRepresentedPlayerData.class, ImmutableSpongeRepresentedPlayerData.class,
                new ItemSkullRepresentedPlayerDataProcessor());

        final FurnaceDataProcessor furnaceDataProcessor = new FurnaceDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FurnaceData.class, SpongeFurnaceData.class,
                ImmutableFurnaceData.class, ImmutableSpongeFurnaceData.class, furnaceDataProcessor);

        final DirtDataProcessor dirtDataProcessor = new DirtDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DirtData.class, SpongeDirtData.class, ImmutableDirtData.class,
                ImmutableSpongeDirtData.class, dirtDataProcessor);

        final StoneDataProcessor stoneDataProcessor = new StoneDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(StoneData.class, SpongeStoneData.class, ImmutableStoneData.class,
                ImmutableSpongeStoneData.class, stoneDataProcessor);

        final PrismarineDataProcessor prismarineDataProcessor = new PrismarineDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PrismarineData.class, SpongePrismarineData.class, ImmutablePrismarineData.class,
                ImmutableSpongePrismarineData.class, prismarineDataProcessor);

        final BrickDataProcessor brickDataProcessor = new BrickDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BrickData.class, SpongeBrickData.class, ImmutableBrickData.class,
                ImmutableSpongeBrickData.class, brickDataProcessor);

        final QuartzDataProcessor quartzDataProcessor = new QuartzDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(QuartzData.class, SpongeQuartzData.class, ImmutableQuartzData.class,
                ImmutableSpongeQuartzData.class, quartzDataProcessor);

        final SandDataProcessor sandDataProcessor = new SandDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SandData.class, SpongeSandData.class, ImmutableSandData.class,
                ImmutableSpongeSandData.class, sandDataProcessor);

        final SlabDataProcessor slabDataProcessor = new SlabDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SlabData.class, SpongeSlabData.class, ImmutableSlabData.class,
                ImmutableSpongeSlabData.class, slabDataProcessor);

        final SandstoneDataProcessor sandstoneDataProcessor = new SandstoneDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SandstoneData.class, SpongeSandstoneData.class, ImmutableSandstoneData.class,
                ImmutableSpongeSandstoneData.class, sandstoneDataProcessor);

        final ComparatorDataProcessor comparatorDataProcessor = new ComparatorDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ComparatorData.class, SpongeComparatorData.class, ImmutableComparatorData.class,
                ImmutableSpongeComparatorData.class, comparatorDataProcessor);

        final TreeDataProcessor treeDataProcessor = new TreeDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(TreeData.class, SpongeTreeData.class, ImmutableTreeData.class,
                ImmutableSpongeTreeData.class, treeDataProcessor);

        final DisguisedBlockDataProcessor disguisedBlockDataProcessor = new DisguisedBlockDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DisguisedBlockData.class, SpongeDisguisedBlockData.class, ImmutableDisguisedBlockData.class,
                ImmutableSpongeDisguisedBlockData.class, disguisedBlockDataProcessor);

        final HingeDataProcessor hingeDataProcessor = new HingeDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(HingeData.class, SpongeHingeData.class, ImmutableHingeData.class,
                ImmutableSpongeHingeData.class, hingeDataProcessor);

        final PistonDataProcessor pistonDataProcessor = new PistonDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PistonData.class, SpongePistonData.class, ImmutablePistonData.class,
                ImmutableSpongePistonData.class, pistonDataProcessor);

        final ExpOrbDataProcessor expOrbDataProcessor = new ExpOrbDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ExpOrbData.class, SpongeExpOrbData.class, ImmutableExpOrbData.class,
                ImmutableSpongeExpOrbData.class, expOrbDataProcessor);

        final PortionDataProcessor portionDataProcessor = new PortionDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PortionData.class, SpongePortionData.class, ImmutablePortionData.class,
                ImmutableSpongePortionData.class, portionDataProcessor);

        final RailDirectionDataProcessor railDirectionDataProcessor = new RailDirectionDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(RailDirectionData.class, SpongeRailDirectionData.class, ImmutableRailDirectionData.class,
                ImmutableSpongeRailDirectionData.class, railDirectionDataProcessor);

        final StairShapeDataProcessor stairShapeDataProcessor = new StairShapeDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(StairShapeData.class, SpongeStairShapeData.class, ImmutableStairShapeData.class,
                ImmutableSpongeStairShapeData.class, stairShapeDataProcessor);

        final WallDataProcessor wallDataProcessor = new WallDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(WallData.class, SpongeWallData.class, ImmutableWallData.class,
                ImmutableSpongeWallData.class, wallDataProcessor);

        final ShrubDataProcessor shrubDataProcessor = new ShrubDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ShrubData.class, SpongeShrubData.class, ImmutableShrubData.class,
                ImmutableSpongeShrubData.class, shrubDataProcessor);

        final PlantDataProcessor plantDataProcessor = new PlantDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PlantData.class, SpongePlantData.class, ImmutablePlantData.class,
                ImmutableSpongePlantData.class, plantDataProcessor);

        final DoublePlantDataProcessor doublePlantDataProcessor = new DoublePlantDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DoublePlantData.class, SpongeDoublePlantData.class, ImmutableDoublePlantData.class,
                ImmutableSpongeDoublePlantData.class, doublePlantDataProcessor);

        final BigMushroomDataProcessor bigMushroomDataProcessor = new BigMushroomDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BigMushroomData.class, SpongeBigMushroomData.class, ImmutableBigMushroomData.class,
                ImmutableSpongeBigMushroomData.class, bigMushroomDataProcessor);


        final BrewingStandDataProcessor brewingStandDataProcessor = new BrewingStandDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BrewingStandData.class, SpongeBrewingStandData.class, ImmutableBrewingStandData.class,
                ImmutableSpongeBrewingStandData.class, brewingStandDataProcessor);

        final AttachedDataProcessor attachedDataProcessor = new AttachedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(AttachedData.class, SpongeAttachedData.class, ImmutableAttachedData.class,
                ImmutableSpongeAttachedData.class, attachedDataProcessor);

        final ConnectedDirectionDataProcessor connectedDirectionDataProcessor = new ConnectedDirectionDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ConnectedDirectionData.class, SpongeConnectedDirectionData.class, ImmutableConnectedDirectionData.class,
                ImmutableSpongeConnectedDirectionData.class, connectedDirectionDataProcessor);

        final DirectionalDataProcessor directionalDataProcessor = new DirectionalDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class,
                ImmutableSpongeDirectionalData.class, directionalDataProcessor);

        final DisarmedDataProcessor disarmedDataProcessor = new DisarmedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DisarmedData.class, SpongeDisarmedData.class, ImmutableDisarmedData.class,
                ImmutableSpongeDisarmedData.class, disarmedDataProcessor);

        final DropDataProcessor dropDataProcessor = new DropDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DropData.class, SpongeDropData.class, ImmutableDropData.class,
                ImmutableSpongeDropData.class, dropDataProcessor);

        final ExtendedDataProcessor extendedDataProcessor = new ExtendedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ExtendedData.class, SpongeExtendedData.class, ImmutableExtendedData.class,
                ImmutableSpongeExtendedData.class, extendedDataProcessor);

        final GrowthDataProcessor growthDataProcessor = new GrowthDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(GrowthData.class, SpongeGrowthData.class, ImmutableGrowthData.class,
                ImmutableSpongeGrowthData.class, growthDataProcessor);

        final OpenDataProcessor openDataProcessor = new OpenDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(OpenData.class, SpongeOpenData.class, ImmutableOpenData.class,
                ImmutableSpongeOpenData.class, openDataProcessor);

        final PoweredDataProcessor poweredDataProcessor = new PoweredDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PoweredData.class, SpongePoweredData.class, ImmutablePoweredData.class,
                ImmutableSpongePoweredData.class, poweredDataProcessor);

        final RedstonePoweredDataProcessor redstonePoweredDataProcessor = new RedstonePoweredDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(RedstonePoweredData.class, SpongeRedstonePoweredData.class,
                ImmutableRedstonePoweredData.class, ImmutableSpongeRedstonePoweredData.class,
                redstonePoweredDataProcessor);

        final SeamlessDataProcessor seamlessDataProcessor = new SeamlessDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SeamlessData.class, SpongeSeamlessData.class, ImmutableSeamlessData.class,
                ImmutableSpongeSeamlessData.class, seamlessDataProcessor);

        final SnowedDataProcessor snowedDataProcessor = new SnowedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SnowedData.class, SpongeSnowedData.class, ImmutableSnowedData.class,
                ImmutableSpongeSnowedData.class, snowedDataProcessor);

        final SuspendedDataProcessor suspendedDataProcessor = new SuspendedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SuspendedData.class, SpongeSuspendedData.class, ImmutableSuspendedData.class,
                ImmutableSpongeSuspendedData.class, suspendedDataProcessor);

        final OccupiedDataProcessor occupiedDataProcessor = new OccupiedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(OccupiedData.class, SpongeOccupiedData.class, ImmutableOccupiedData.class,
                ImmutableSpongeOccupiedData.class, occupiedDataProcessor);

        final InWallDataProcessor inWallDataProcessor = new InWallDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(InWallData.class, SpongeInWallData.class, ImmutableInWallData.class,
                ImmutableSpongeInWallData.class, inWallDataProcessor);

        final LayeredDataProcessor layeredDataProcessor = new LayeredDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(LayeredData.class, SpongeLayeredData.class, ImmutableLayeredData.class,
                ImmutableSpongeLayeredData.class, layeredDataProcessor);

        final DecayableDataProcessor decayableDataProcessor = new DecayableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DecayableData.class, SpongeDecayableData.class, ImmutableDecayableData.class,
                ImmutableSpongeDecayableData.class, decayableDataProcessor);

        final AxisDataProcessor axisDataProcessor = new AxisDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(AxisData.class, SpongeAxisData.class, ImmutableAxisData.class,
                ImmutableSpongeAxisData.class, axisDataProcessor);

        final DelayableDataProcessor delayableDataProcessor = new DelayableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DelayableData.class, SpongeDelayableData.class, ImmutableDelayableData.class,
                ImmutableSpongeDelayableData.class, delayableDataProcessor);

        final AgentDataProcessor agentDataProcessor = new AgentDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(AgentData.class, SpongeAgentData.class, ImmutableAgentData.class,
                ImmutableSpongeAgentData.class, agentDataProcessor);

        final ChargedDataProcessor chargedDataProcessor = new ChargedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ChargedData.class, SpongeChargedData.class, ImmutableChargedData.class,
                ImmutableSpongeChargedData.class, chargedDataProcessor);

        final DurabilityDataProcessor durabilityDataProcessor = new DurabilityDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(DurabilityData.class, SpongeDurabilityData.class, ImmutableDurabilityData.class,
                ImmutableSpongeDurabilityData.class, durabilityDataProcessor);

        final SpawnableDataProcessor spawnableDataProcessor = new SpawnableDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SpawnableData.class, SpongeSpawnableData.class, ImmutableSpawnableData.class,
                ImmutableSpongeSpawnableData.class, spawnableDataProcessor);

        final FallDistanceDataProcessor fallDistanceDataProcessor = new FallDistanceDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FallDistanceData.class, SpongeFallDistanceData.class,
                ImmutableFallDistanceData.class, ImmutableSpongeFallDistanceData.class, fallDistanceDataProcessor);

        final CooldownDataProcessor cooldownDataProcessor = new CooldownDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(CooldownData.class, SpongeCooldownData.class, ImmutableCooldownData.class,
                ImmutableSpongeCooldownData.class, cooldownDataProcessor);

        final NoteDataProcessor noteDataProcessor = new NoteDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(NoteData.class, SpongeNoteData.class, ImmutableNoteData.class,
                ImmutableSpongeNoteData.class, noteDataProcessor);

        final VehicleDataProcessor vehicleDataProcessor = new VehicleDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(VehicleData.class, SpongeVehicleData.class, ImmutableVehicleData.class,
                ImmutableSpongeVehicleData.class, vehicleDataProcessor);

        dataRegistry.registerDataProcessorAndImpl(ArtData.class, SpongeArtData.class, ImmutableArtData.class, ImmutableSpongeArtData.class,
                new ArtDataProcessor());

        final TargetMultipleLivingDataProcessor targetMultipleLivingDataProcessor = new TargetMultipleLivingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(TargetMultipleLivingData.class, SpongeTargetMultipleLivingData.class, ImmutableTargetMultipleLivingData.class,
                ImmutableSpongeTargetMultipleLivingData.class, targetMultipleLivingDataProcessor);

        final TargetLivingDataProcessor targetLivingDataProcessor = new TargetLivingDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(TargetLivingData.class, SpongeTargetLivingData.class, ImmutableTargetLivingData.class,
                ImmutableSpongeTargetLivingData.class, targetLivingDataProcessor);

        dataRegistry.registerDataProcessorAndImpl(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new TileEntityLockableDataProcessor());
        dataRegistry.registerDataProcessorAndImpl(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new ItemLockableDataProcessor());

        final FireworkEffectDataProcessor fireworkEffectDataProcessor = new FireworkEffectDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FireworkEffectData.class, SpongeFireworkEffectData.class,
                ImmutableFireworkEffectData.class, ImmutableSpongeFireworkEffectData.class, fireworkEffectDataProcessor);

        final BlockItemDataProcessor blockItemDataProcessor = new BlockItemDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BlockItemData.class, SpongeBlockItemData.class, ImmutableBlockItemData.class,
                ImmutableSpongeBlockItemData.class, blockItemDataProcessor);

        final FireworkRocketDataProcessor fireworkRocketDataProcessor = new FireworkRocketDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FireworkRocketData.class, SpongeFireworkRocketData.class,
                ImmutableFireworkRocketData.class, ImmutableSpongeFireworkRocketData.class, fireworkRocketDataProcessor);

        final MinecartBlockDataProcessor minecartBlockDataProcessor = new MinecartBlockDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(MinecartBlockData.class, SpongeMinecartBlockData.class,
                ImmutableMinecartBlockData.class, ImmutableSpongeMinecartBlockData.class, minecartBlockDataProcessor);

        final PlayerCreatedDataProcessor playerCreatedDataProcessor = new PlayerCreatedDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(PlayerCreatedData.class, SpongePlayerCreatedData.class, ImmutablePlayerCreatedData.class,
                ImmutableSpongePlayerCreatedData.class, playerCreatedDataProcessor);

        final JukeboxDataProcessor jukeboxDataProcessor = new JukeboxDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, jukeboxDataProcessor);

        final FallingBlockDataProcessor fallingBlockDataProcessor = new FallingBlockDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(FallingBlockData.class, SpongeFallingBlockData.class, ImmutableFallingBlockData.class,
                                                  ImmutableSpongeFallingBlockData.class, fallingBlockDataProcessor);

        final SkeletonDataProcessor skeletonDataProcessor = new SkeletonDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(SkeletonData.class, SpongeSkeletonData.class, ImmutableSkeletonData.class,
                ImmutableSpongeSkeletonData.class, skeletonDataProcessor);

        final OcelotDataProcessor ocelotDataProcessor = new OcelotDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(OcelotData.class, SpongeOcelotData.class, ImmutableOcelotData.class,
                ImmutableSpongeOcelotData.class, ocelotDataProcessor);

        final RabbitDataProcessor rabbitDataProcessor = new RabbitDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(RabbitData.class, SpongeRabbitData.class, ImmutableRabbitData.class,
                ImmutableSpongeRabbitData.class, rabbitDataProcessor);

        final TileEntityBannerDataProcessor bannerDataProcessor = new TileEntityBannerDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(BannerData.class, SpongeBannerData.class, ImmutableBannerData.class,
                ImmutableSpongeBannerData.class, bannerDataProcessor);

        // Values
        dataRegistry.registerValueProcessor(Keys.HEALTH, new HealthValueProcessor());
        dataRegistry.registerValueProcessor(Keys.MAX_HEALTH, new MaxHealthValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIRE_TICKS, new FireTicksValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIRE_DAMAGE_DELAY, new FireDamageDelayValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DISPLAY_NAME, new ItemDisplayNameValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DISPLAY_NAME, new TileEntityDisplayNameValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DISPLAY_NAME, new EntityDisplayNameValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SHOWS_DISPLAY_NAME, new DisplayNameVisibleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CAREER, new CareerValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SIGN_LINES, new SignLinesValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SKULL_TYPE, new TileEntitySkullValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SKULL_TYPE, new ItemSkullValueProcessor());
        dataRegistry.registerValueProcessor(Keys.VELOCITY, new VelocityValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FOOD_LEVEL, new FoodLevelValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SATURATION, new FoodSaturationValueProcessor());
        dataRegistry.registerValueProcessor(Keys.EXHAUSTION, new FoodExhaustionValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_FLYING, new IsFlyingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.MAX_AIR, new MaxAirValueProcessor());
        dataRegistry.registerValueProcessor(Keys.REMAINING_AIR, new RemainingAirValueProcessor());
        dataRegistry.registerValueProcessor(Keys.GAME_MODE, new GameModeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_SCREAMING, new ScreamingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ITEM_ENCHANTMENTS, new ItemEnchantmentValueProcessor());
        dataRegistry.registerValueProcessor(Keys.HORSE_COLOR, new HorseColorValueProcessor());
        dataRegistry.registerValueProcessor(Keys.HORSE_STYLE, new HorseStyleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.HORSE_VARIANT, new HorseVariantValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ITEM_LORE, new ItemLoreValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BOOK_PAGES, new BookPagesValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_SNEAKING, new SneakingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.GOLDEN_APPLE_TYPE, new GoldenAppleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.EXPERIENCE_LEVEL, new ExperienceLevelValueProcessor());
        dataRegistry.registerValueProcessor(Keys.TOTAL_EXPERIENCE, new TotalExperienceValueProcessor());
        dataRegistry.registerValueProcessor(Keys.EXPERIENCE_SINCE_LEVEL, new ExperienceSinceLevelValueProcessor());
        dataRegistry.registerValueProcessor(Keys.EXPERIENCE_FROM_START_OF_LEVEL, new ExperienceFromStartOfLevelValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BOOK_AUTHOR, new BookAuthorValueProcessor());
        dataRegistry.registerValueProcessor(Keys.REPRESENTED_ITEM, new RepresentedItemValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BREAKABLE_BLOCK_TYPES, new BreakableValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PLACEABLE_BLOCKS, new PlaceableValueProcessor());
        dataRegistry.registerValueProcessor(Keys.WALKING_SPEED, new WalkingSpeedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FLYING_SPEED, new FlyingSpeedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SLIME_SIZE, new SlimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_VILLAGER_ZOMBIE, new VillagerZombieValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_PLAYING, new PlayingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_SITTING, new IsSittingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_SHEARED, new IsShearedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PIG_SADDLE, new PigSaddleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CAN_FLY, new CanFlyValueProcessor());
        dataRegistry.registerValueProcessor(Keys.TAMED_OWNER, new TameableOwnerValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_WET, new ItemWetValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IS_WET, new EntityWetValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ELDER_GUARDIAN, new ElderValueProcessor());
        dataRegistry.registerValueProcessor(Keys.COAL_TYPE, new CoalValueProcessor());
        dataRegistry.registerValueProcessor(Keys.COOKED_FISH, new CookedFishValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FISH_TYPE, new FishValueProcessor());
        dataRegistry.registerValueProcessor(Keys.REPRESENTED_PLAYER, new SkullRepresentedPlayerProcessor());
        dataRegistry.registerValueProcessor(Keys.REPRESENTED_PLAYER, new ItemSkullRepresentedPlayerValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PASSED_BURN_TIME, new PassedBurnTimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.MAX_BURN_TIME, new MaxBurnTimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PASSED_COOK_TIME, new PassedCookTimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.MAX_COOK_TIME, new MaxCookTimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CONTAINED_EXPERIENCE, new ExpOrbValueProcessor());
        dataRegistry.registerValueProcessor(Keys.REMAINING_BREW_TIME, new RemainingBrewTimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DIRT_TYPE, new DirtTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.STONE_TYPE, new StoneTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BRICK_TYPE, new BrickTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PRISMARINE_TYPE, new PrismarineTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.QUARTZ_TYPE, new QuartzTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SAND_TYPE, new SandTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SLAB_TYPE, new SlabTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SANDSTONE_TYPE, new SandstoneTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.COMPARATOR_TYPE, new ComparatorTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.TREE_TYPE, new TreeTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.HINGE_POSITION, new HingePositionValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PISTON_TYPE, new PistonTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PORTION_TYPE, new PortionTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.RAIL_DIRECTION, new RailDirectionValueProcessor());
        dataRegistry.registerValueProcessor(Keys.STAIR_SHAPE, new StairShapeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.WALL_TYPE, new WallTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SHRUB_TYPE, new ShrubTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PLANT_TYPE, new PlantTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DOUBLE_PLANT_TYPE, new DoublePlantTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BIG_MUSHROOM_TYPE, new BigMushroomTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DISGUISED_BLOCK_TYPE, new DisguisedBlockTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.COLOR, new ItemColorValueProcessor());
        dataRegistry.registerValueProcessor(Keys.AI_ENABLED, new IsAiEnabledValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CREEPER_CHARGED, new ChargedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.UNBREAKABLE, new UnbreakableValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ITEM_DURABILITY, new ItemDurabilityValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SPAWNABLE_ENTITY_TYPE, new SpawnableEntityTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FALL_DISTANCE, new FallDistanceValueProcessor());
        dataRegistry.registerValueProcessor(Keys.COOLDOWN, new CooldownValueProcessor());
        dataRegistry.registerValueProcessor(Keys.NOTE_PITCH, new NoteValueProcessor());
        dataRegistry.registerValueProcessor(Keys.VEHICLE, new VehicleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BASE_VEHICLE, new BaseVehicleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ART, new ArtValueProcessor());
        dataRegistry.registerValueProcessor(Keys.TARGET, new TargetLivingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.TARGETS, new TargetMultipleLivingValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIREWORK_EFFECTS, new EntityFireworkEffectsValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIREWORK_EFFECTS, new ItemFireworkEffectsValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIREWORK_FLIGHT_MODIFIER, new EntityFireworkRocketValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIREWORK_FLIGHT_MODIFIER, new ItemFireworkRocketValueProcessor());
        dataRegistry.registerValueProcessor(Keys.REPRESENTED_BLOCK, new RepresentedBlockValueProcessor());
        dataRegistry.registerValueProcessor(Keys.OFFSET, new OffsetValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ATTACHED, new AttachedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FALL_DAMAGE_PER_BLOCK, new FallHurtAmountValueProcessor());
        dataRegistry.registerValueProcessor(Keys.MAX_FALL_DAMAGE, new MaxFallDamageValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FALLING_BLOCK_STATE, new FallingBlockStateValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CAN_PLACE_AS_BLOCK, new CanPlaceAsBlockValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CAN_DROP_AS_ITEM, new CanDropAsItemValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FALL_TIME, new FallTimeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, new FallingBlockCanHurtEntitiesValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CONNECTED_DIRECTIONS, new ConnectedDirectionsValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CONNECTED_EAST, new ConnectedEastValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CONNECTED_NORTH, new ConnectedNorthValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CONNECTED_SOUTH, new ConnectedSouthValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CONNECTED_WEST, new ConnectedWestValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DIRECTION, new DirectionValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DISARMED, new DisarmedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SHOULD_DROP, new ShouldDropValueProcessor());
        dataRegistry.registerValueProcessor(Keys.EXTENDED, new ExtendedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.GROWTH_STAGE, new GrowthStageValueProcessor());
        dataRegistry.registerValueProcessor(Keys.OPEN, new OpenValueProcessor());
        dataRegistry.registerValueProcessor(Keys.POWERED, new PoweredValueProcessor());
        dataRegistry.registerValueProcessor(Keys.POWER, new PowerValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SEAMLESS, new SeamlessValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SNOWED, new SnowedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SUSPENDED, new SuspendedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.OCCUPIED, new OccupiedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.LAYER, new LayerValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DECAYABLE, new DecayableValueProcessor());
        dataRegistry.registerValueProcessor(Keys.IN_WALL, new InWallValueProcessor());
        dataRegistry.registerValueProcessor(Keys.AXIS, new AxisValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DELAY, new DelayValueProcessor());
        dataRegistry.registerValueProcessor(Keys.PLAYER_CREATED, new PlayerCreatedValueProcessor());
        dataRegistry.registerValueProcessor(Keys.ITEM_BLOCKSTATE, new BlockItemValueProcessor());
        dataRegistry.registerValueProcessor(Keys.REPRESENTED_ITEM, new JukeboxValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SKELETON_TYPE, new SkeletonTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.OCELOT_TYPE, new OcelotTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.RABBIT_TYPE, new RabbitTypeValueProcessor());
        dataRegistry.registerValueProcessor(Keys.LOCK_TOKEN, new LockTokenValueProcessor());
        dataRegistry.registerValueProcessor(Keys.LOCK_TOKEN, new ItemLockTokenValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BANNER_BASE_COLOR, new TileBannerBaseColorValueProcessor());
        dataRegistry.registerValueProcessor(Keys.BANNER_PATTERNS, new TileBannerPatternLayersValueProcessor());

        // Properties
        final PropertyRegistry propertyRegistry = SpongePropertyRegistry.getInstance();

        // Blocks
        propertyRegistry.register(BlastResistanceProperty.class, new BlastResistancePropertyStore());
        propertyRegistry.register(FlammableProperty.class, new FlammablePropertyStore());
        propertyRegistry.register(GravityAffectedProperty.class, new GravityAffectedPropertyStore());
        propertyRegistry.register(GroundLuminanceProperty.class, new GroundLuminancePropertyStore());
        propertyRegistry.register(HardnessProperty.class, new HardnessPropertyStore());
        propertyRegistry.register(HeldItemProperty.class, new HeldItemPropertyStore());
        propertyRegistry.register(IndirectlyPoweredProperty.class, new IndirectlyPoweredPropertyStore());
        propertyRegistry.register(LightEmissionProperty.class, new LightEmissionPropertyStore());
        propertyRegistry.register(MatterProperty.class, new MatterPropertyStore());
        propertyRegistry.register(PassableProperty.class, new PassablePropertyStore());
        propertyRegistry.register(PoweredProperty.class, new PoweredPropertyStore());
        propertyRegistry.register(ReplaceableProperty.class, new ReplaceablePropertyStore());
        propertyRegistry.register(SkyLuminanceProperty.class, new SkyLuminancePropertyStore());
        propertyRegistry.register(SolidCubeProperty.class, new SolidCubePropertyStore());
        propertyRegistry.register(StatisticsTrackedProperty.class, new StatisticsTrackedPropertyStore());
        propertyRegistry.register(TemperatureProperty.class, new TemperaturePropertyStore());
        propertyRegistry.register(UnbreakableProperty.class, new UnbreakablePropertyStore());

        // Items
        propertyRegistry.register(ApplicableEffectProperty.class, new ApplicableEffectPropertyStore());
        propertyRegistry.register(BurningFuelProperty.class, new BurningFuelPropertyStore());
        propertyRegistry.register(DamageAbsorptionProperty.class, new DamageAbsorptionPropertyStore());
        propertyRegistry.register(EfficiencyProperty.class, new EfficiencyPropertyStore());
        propertyRegistry.register(EquipmentProperty.class, new EquipmentPropertyStore());
        propertyRegistry.register(FoodRestorationProperty.class, new FoodRestorationPropertyStore());
        propertyRegistry.register(HarvestingProperty.class, new HarvestingPropertyStore());
        propertyRegistry.register(SaturationProperty.class, new SaturationPropertyStore());
        propertyRegistry.register(UseLimitProperty.class, new UseLimitPropertyStore());

        // Entities
        propertyRegistry.register(EyeLocationProperty.class, new EyeLocationPropertyStore());
        propertyRegistry.register(EyeHeightProperty.class, new EyeHeightPropertyStore());
    }

}
