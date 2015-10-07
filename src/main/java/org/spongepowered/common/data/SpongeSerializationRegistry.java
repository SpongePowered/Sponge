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
import org.spongepowered.api.block.tileentity.Banner;
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.block.tileentity.Comparator;
import org.spongepowered.api.block.tileentity.DaylightDetector;
import org.spongepowered.api.block.tileentity.EnchantmentTable;
import org.spongepowered.api.block.tileentity.EndPortal;
import org.spongepowered.api.block.tileentity.EnderChest;
import org.spongepowered.api.block.tileentity.MobSpawner;
import org.spongepowered.api.block.tileentity.Note;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.block.tileentity.carrier.BrewingStand;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.block.tileentity.carrier.Dropper;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkullData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGameModeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableScreamingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.ScreamingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.SpongeItemEnchantmentBuilder;
import org.spongepowered.common.data.builder.block.data.SpongePatternLayerBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeBannerBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeBrewingStandBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeChestBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeCommandBlockBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeComparatorBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeDaylightBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeDispenserBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeDropperBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeEnchantmentTableBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeEndPortalBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeEnderChestBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeFurnaceBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeHopperBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeMobSpawnerBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeNoteBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeSignBuilder;
import org.spongepowered.common.data.builder.block.tileentity.SpongeSkullBuilder;
import org.spongepowered.common.data.builder.item.SpongeItemStackDataBuilder;
import org.spongepowered.common.data.builder.item.SpongeItemStackSnapshotBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.block.ImmutableSpongeTreeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.item.ImmutableItemEnchantmentDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.DisplayNameDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.RepresentedItemDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.SkullDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.BreathingDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.CareerDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.ExperienceHolderDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.FlyingDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.FoodDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.GameModeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.HealthDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.HorseDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.IgniteableDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.ScreamingDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.SneakingDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.entity.VelocityDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.item.ItemEnchantmentDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.item.ItemGoldenAppleDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.item.ItemLoreDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.item.ItemPagedDataBuilder;
import org.spongepowered.common.data.builder.manipulator.mutable.tileentity.SignDataBuilder;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeSkullData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreathingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCareerData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlyingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFoodData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeGameModeData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHealthData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHorseData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeIgniteableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeScreamingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSneakingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVelocityData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeLoreData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePagedData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeSignData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgniteableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeScreamingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSneakingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.data.DisplayNameDataProcessor;
import org.spongepowered.common.data.processor.data.RepresentedItemDataProcessor;
import org.spongepowered.common.data.processor.data.SkullDataProcessor;
import org.spongepowered.common.data.processor.data.entity.BreathingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.CareerDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ExperienceHolderDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FlyingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FoodDataProcessor;
import org.spongepowered.common.data.processor.data.entity.GameModeDataProcessor;
import org.spongepowered.common.data.processor.data.entity.HealthDataProcessor;
import org.spongepowered.common.data.processor.data.entity.HorseDataProcessor;
import org.spongepowered.common.data.processor.data.entity.IgniteableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ScreamingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SneakingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.VelocityDataProcessor;
import org.spongepowered.common.data.processor.data.item.GoldenAppleDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemEnchantmentDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemLoreDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemPagedDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.SignDataProcessor;
import org.spongepowered.common.data.processor.value.DisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.DisplayNameVisibleValueProcessor;
import org.spongepowered.common.data.processor.value.ItemEnchantmentValueProcessor;
import org.spongepowered.common.data.processor.value.SkullValueProcessor;
import org.spongepowered.common.data.processor.value.entity.CareerValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceFromStartOfLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceSinceLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FireDamageDelayValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FireTicksValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodExhaustionValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodSaturationValueProcessor;
import org.spongepowered.common.data.processor.value.entity.GameModeValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HealthValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseColorValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseStyleValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseVariantValueProcessor;
import org.spongepowered.common.data.processor.value.entity.IsFlyingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxAirValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxHealthValueProcessor;
import org.spongepowered.common.data.processor.value.entity.RemainingAirValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ScreamingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.SneakingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.TotalExperienceValueProcessor;
import org.spongepowered.common.data.processor.value.entity.VelocityValueProcessor;
import org.spongepowered.common.data.processor.value.item.BookPagesValueProcessor;
import org.spongepowered.common.data.processor.value.item.GoldenAppleValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemLoreValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.SignLinesValueProcessor;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.service.persistence.SpongeSerializationService;

public class SpongeSerializationRegistry {

    public static void setupSerialization(Game game) {
        KeyRegistry.registerKeys();
        SpongeSerializationService service = (SpongeSerializationService) game.getServiceManager().provide(SerializationService.class).get();
        SpongeDataRegistry dataRegistry = SpongeDataRegistry.getInstance();
        // TileEntities
        service.registerBuilder(Banner.class, new SpongeBannerBuilder(game));
        service.registerBuilder(PatternLayer.class, new SpongePatternLayerBuilder(game));
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
        service
            .registerBuilderAndImpl(ImmutableEnchantmentData.class, ImmutableSpongeEnchantmentData.class, new ImmutableItemEnchantmentDataBuilder());
        // Data Manipulators

        final HealthDataProcessor healthProcessor = new HealthDataProcessor();
        final HealthDataBuilder healthDataBuilder = new HealthDataBuilder();
        service.registerBuilder(HealthData.class, healthDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(HealthData.class, SpongeHealthData.class, ImmutableHealthData.class,
                                                  ImmutableSpongeHealthData.class, healthProcessor, healthDataBuilder);

        final IgniteableDataProcessor igniteableProcessor = new IgniteableDataProcessor();
        final IgniteableDataBuilder igniteableDataBuilder = new IgniteableDataBuilder();
        service.registerBuilder(IgniteableData.class, igniteableDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(IgniteableData.class, SpongeIgniteableData.class, ImmutableIgniteableData.class,
                                                  ImmutableSpongeIgniteableData.class, igniteableProcessor, igniteableDataBuilder);

        final DisplayNameDataProcessor displayNameDataProcessor = new DisplayNameDataProcessor();
        final DisplayNameDataBuilder displayNameDataBuilder = new DisplayNameDataBuilder();
        service.registerBuilder(DisplayNameData.class, displayNameDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(DisplayNameData.class, SpongeDisplayNameData.class,
                                                  ImmutableDisplayNameData.class, ImmutableSpongeDisplayNameData.class, displayNameDataProcessor,
                                                  displayNameDataBuilder);

        final CareerDataProcessor careerDataProcessor = new CareerDataProcessor();
        final CareerDataBuilder careerDataBuilder = new CareerDataBuilder();
        service.registerBuilder(CareerData.class, careerDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(CareerData.class, SpongeCareerData.class, ImmutableCareerData.class,
                                                  ImmutableSpongeCareerData.class, careerDataProcessor, careerDataBuilder);

        final SignDataProcessor signDataProcessor = new SignDataProcessor();
        final SignDataBuilder signDataBuilder = new SignDataBuilder();
        service.registerBuilder(SignData.class, signDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(SignData.class, SpongeSignData.class,
                                                  ImmutableSignData.class, ImmutableSpongeSignData.class, signDataProcessor, signDataBuilder);

        final FlyingDataProcessor flyingDataProcessor = new FlyingDataProcessor();
        final FlyingDataBuilder flyingDataBuilder = new FlyingDataBuilder();
        service.registerBuilder(FlyingData.class, flyingDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(FlyingData.class, SpongeFlyingData.class, ImmutableFlyingData.class,
                                                  ImmutableSpongeFlyingData.class, flyingDataProcessor, flyingDataBuilder);

        final SkullDataProcessor skullDataProcessor = new SkullDataProcessor();
        final SkullDataBuilder skullDataBuilder = new SkullDataBuilder();
        service.registerBuilder(SkullData.class, skullDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                                                  ImmutableSpongeSkullData.class, skullDataProcessor, skullDataBuilder);

        final VelocityDataProcessor velocityDataProcessor = new VelocityDataProcessor();
        final VelocityDataBuilder velocityDataBuilder = new VelocityDataBuilder();
        service.registerBuilder(VelocityData.class, velocityDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(VelocityData.class, SpongeVelocityData.class, ImmutableVelocityData.class,
                                                  ImmutableSpongeVelocityData.class, velocityDataProcessor, velocityDataBuilder);

        final FoodDataProcessor foodDataProcessor = new FoodDataProcessor();
        final FoodDataBuilder foodDataBuilder = new FoodDataBuilder();
        service.registerBuilder(FoodData.class, foodDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(FoodData.class, SpongeFoodData.class, ImmutableFoodData.class,
                                                  ImmutableSpongeFoodData.class, foodDataProcessor, foodDataBuilder);

        final BreathingDataProcessor breathingDataProcessor = new BreathingDataProcessor();
        final BreathingDataBuilder breathingDataBuilder = new BreathingDataBuilder();
        service.registerBuilder(BreathingData.class, breathingDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(BreathingData.class, SpongeBreathingData.class, ImmutableBreathingData.class,
                                                  ImmutableSpongeBreathingData.class, breathingDataProcessor, breathingDataBuilder);

        final GameModeDataProcessor gameModeDataProcessor = new GameModeDataProcessor();
        final GameModeDataBuilder gameModeDataBuilder = new GameModeDataBuilder();
        service.registerBuilder(GameModeData.class, gameModeDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(GameModeData.class, SpongeGameModeData.class, ImmutableGameModeData.class,
                                                  ImmutableSpongeGameModeData.class, gameModeDataProcessor, gameModeDataBuilder);

        final ScreamingDataProcessor screamingDataProcessor = new ScreamingDataProcessor();
        final ScreamingDataBuilder screamingDataBuilder = new ScreamingDataBuilder();
        service.registerBuilder(ScreamingData.class, screamingDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(ScreamingData.class, SpongeScreamingData.class, ImmutableScreamingData.class,
                                                  ImmutableSpongeScreamingData.class, screamingDataProcessor, screamingDataBuilder);

        final RepresentedItemDataProcessor representedItemDataProcessor = new RepresentedItemDataProcessor();
        final RepresentedItemDataBuilder representedItemDataBuilder = new RepresentedItemDataBuilder(service);
        service.registerBuilder(RepresentedItemData.class, representedItemDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                                                  ImmutableSpongeRepresentedItemData.class, representedItemDataProcessor, representedItemDataBuilder);

        final ItemEnchantmentDataProcessor itemEnchantmentDataProcessor = new ItemEnchantmentDataProcessor();
        final ItemEnchantmentDataBuilder itemEnchantmentDataBuilder = new ItemEnchantmentDataBuilder();
        service.registerBuilderAndImpl(EnchantmentData.class, SpongeEnchantmentData.class, itemEnchantmentDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(EnchantmentData.class, SpongeEnchantmentData.class, ImmutableEnchantmentData.class,
                                                  ImmutableSpongeEnchantmentData.class, itemEnchantmentDataProcessor, itemEnchantmentDataBuilder);

        final ItemLoreDataProcessor itemLoreDataProcessor = new ItemLoreDataProcessor();
        final ItemLoreDataBuilder itemLoreDataBuilder = new ItemLoreDataBuilder();
        service.registerBuilderAndImpl(LoreData.class, SpongeLoreData.class, itemLoreDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(LoreData.class, SpongeLoreData.class, ImmutableLoreData.class, ImmutableSpongeLoreData.class,
                                                  itemLoreDataProcessor, itemLoreDataBuilder);

        final ItemPagedDataProcessor itemPagedDataProcessor = new ItemPagedDataProcessor();
        final ItemPagedDataBuilder itemPagedDataBuilder = new ItemPagedDataBuilder();
        service.registerBuilderAndImpl(PagedData.class, SpongePagedData.class, itemPagedDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(PagedData.class, SpongePagedData.class, ImmutablePagedData.class, ImmutableSpongePagedData.class,
                                                  itemPagedDataProcessor, itemPagedDataBuilder);

        final HorseDataProcessor horseDataProcessor = new HorseDataProcessor();
        final HorseDataBuilder horseDataBuilder = new HorseDataBuilder(horseDataProcessor);
        service.registerBuilder(HorseData.class, horseDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(HorseData.class, SpongeHorseData.class, ImmutableHorseData.class,
                                                  ImmutableSpongeHorseData.class, horseDataProcessor, horseDataBuilder);

        final SneakingDataProcessor sneakingDataProcessor = new SneakingDataProcessor();
        final SneakingDataBuilder sneakingDataBuilder = new SneakingDataBuilder();
        service.registerBuilder(SneakingData.class, sneakingDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(SneakingData.class, SpongeSneakingData.class, ImmutableSneakingData.class,
                                                  ImmutableSpongeSneakingData.class, sneakingDataProcessor, sneakingDataBuilder);

        final GoldenAppleDataProcessor goldenAppleDataProcessor = new GoldenAppleDataProcessor();
        final ItemGoldenAppleDataBuilder itemGoldenAppleDataBuilder = new ItemGoldenAppleDataBuilder(goldenAppleDataProcessor);
        service.registerBuilder(GoldenAppleData.class, itemGoldenAppleDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(GoldenAppleData.class, SpongeGoldenAppleData.class, ImmutableGoldenAppleData.class,
                                                  ImmutableSpongeGoldenAppleData.class, goldenAppleDataProcessor, itemGoldenAppleDataBuilder);

        final ExperienceHolderDataProcessor experienceHolderDataProcessor = new ExperienceHolderDataProcessor();
        final ExperienceHolderDataBuilder experienceHolderDataBuilder = new ExperienceHolderDataBuilder();
        service.registerBuilder(ExperienceHolderData.class, experienceHolderDataBuilder);
        dataRegistry.registerDataProcessorAndImpl(ExperienceHolderData.class, SpongeExperienceHolderData.class, ImmutableExperienceHolderData.class,
                                                  ImmutableSpongeExperienceHolderData.class, experienceHolderDataProcessor,
                                                  experienceHolderDataBuilder);

        // Values
        dataRegistry.registerValueProcessor(Keys.HEALTH, new HealthValueProcessor());
        dataRegistry.registerValueProcessor(Keys.MAX_HEALTH, new MaxHealthValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIRE_TICKS, new FireTicksValueProcessor());
        dataRegistry.registerValueProcessor(Keys.FIRE_DAMAGE_DELAY, new FireDamageDelayValueProcessor());
        dataRegistry.registerValueProcessor(Keys.DISPLAY_NAME, new DisplayNameValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SHOWS_DISPLAY_NAME, new DisplayNameVisibleValueProcessor());
        dataRegistry.registerValueProcessor(Keys.CAREER, new CareerValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SIGN_LINES, new SignLinesValueProcessor());
        dataRegistry.registerValueProcessor(Keys.SKULL_TYPE, new SkullValueProcessor());
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

    }

}
