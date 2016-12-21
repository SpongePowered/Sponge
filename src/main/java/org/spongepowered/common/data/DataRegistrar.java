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
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidItemData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidItemData;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.BookViewDataBuilder;
import org.spongepowered.api.text.serializer.TextConfigSerializer;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.block.state.SpongeBlockStateMetaContentUpdater;
import org.spongepowered.common.data.builder.block.tileentity.*;
import org.spongepowered.common.data.builder.item.SpongeFireworkEffectDataBuilder;
import org.spongepowered.common.data.builder.data.meta.*;
import org.spongepowered.common.data.builder.item.*;
import org.spongepowered.common.data.builder.manipulator.InvisibilityDataAddVanishUpdater;
import org.spongepowered.common.data.builder.manipulator.immutable.block.ImmutableSpongeTreeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.item.ImmutableItemEnchantmentDataBuilder;
import org.spongepowered.common.data.builder.util.weighted.BaseAndAdditionBuilder;
import org.spongepowered.common.data.builder.util.weighted.BaseAndVarianceBuilder;
import org.spongepowered.common.data.builder.util.weighted.FixedBuilder;
import org.spongepowered.common.data.builder.util.weighted.OptionalVarianceBuilder;
import org.spongepowered.common.data.builder.world.LocationBuilder;
import org.spongepowered.common.data.manipulator.immutable.*;
import org.spongepowered.common.data.manipulator.immutable.block.*;
import org.spongepowered.common.data.manipulator.immutable.entity.*;
import org.spongepowered.common.data.manipulator.immutable.extra.ImmutableSpongeFluidItemData;
import org.spongepowered.common.data.manipulator.immutable.item.*;
import org.spongepowered.common.data.manipulator.immutable.tileentity.*;
import org.spongepowered.common.data.manipulator.mutable.*;
import org.spongepowered.common.data.manipulator.mutable.block.*;
import org.spongepowered.common.data.manipulator.mutable.entity.*;
import org.spongepowered.common.data.manipulator.mutable.extra.SpongeFluidItemData;
import org.spongepowered.common.data.manipulator.mutable.item.*;
import org.spongepowered.common.data.manipulator.mutable.tileentity.*;
import org.spongepowered.common.data.processor.data.*;
import org.spongepowered.common.data.processor.data.block.*;
import org.spongepowered.common.data.processor.data.entity.*;
import org.spongepowered.common.data.processor.data.extra.FluidItemDataProcessor;
import org.spongepowered.common.data.processor.data.item.*;
import org.spongepowered.common.data.processor.data.tileentity.*;
import org.spongepowered.common.data.processor.multi.block.*;
import org.spongepowered.common.data.processor.multi.entity.*;
import org.spongepowered.common.data.processor.multi.item.*;
import org.spongepowered.common.data.processor.multi.tileentity.*;
import org.spongepowered.common.data.processor.value.block.*;
import org.spongepowered.common.data.processor.value.entity.*;
import org.spongepowered.common.data.processor.value.item.*;
import org.spongepowered.common.data.processor.value.tileentity.*;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.property.store.block.*;
import org.spongepowered.common.data.property.store.entity.*;
import org.spongepowered.common.data.property.store.item.*;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.world.storage.SpongePlayerData;

@SuppressWarnings("deprecation")
public class DataRegistrar {

    @SuppressWarnings("unchecked")
    public static void setupSerialization(Game game) {
        SpongeDataManager dataManager = SpongeDataManager.getInstance();

        // TileEntities
        dataManager.registerBuilder(Banner.class, new SpongeBannerBuilder());
        dataManager.registerBuilder(PatternLayer.class, new SpongePatternLayerBuilder());
        dataManager.registerBuilder(BrewingStand.class, new SpongeBrewingStandBuilder());
        dataManager.registerBuilder(Chest.class, new SpongeChestBuilder());
        dataManager.registerBuilder(CommandBlock.class, new SpongeCommandBlockBuilder());
        dataManager.registerBuilder(Comparator.class, new SpongeComparatorBuilder());
        dataManager.registerBuilder(DaylightDetector.class, new SpongeDaylightBuilder());
        dataManager.registerBuilder(Dispenser.class, new SpongeDispenserBuilder());
        dataManager.registerBuilder(Dropper.class, new SpongeDropperBuilder());
        dataManager.registerBuilder(EnchantmentTable.class, new SpongeEnchantmentTableBuilder());
        dataManager.registerBuilder(EnderChest.class, new SpongeEnderChestBuilder());
        dataManager.registerBuilder(EndPortal.class, new SpongeEndPortalBuilder());
        dataManager.registerBuilder(Furnace.class, new SpongeFurnaceBuilder());
        dataManager.registerBuilder(Hopper.class, new SpongeHopperBuilder());
        dataManager.registerBuilder(MobSpawner.class, new SpongeMobSpawnerBuilder());
        dataManager.registerBuilder(Note.class, new SpongeNoteBuilder());
        dataManager.registerBuilder(Sign.class, new SpongeSignBuilder());
        dataManager.registerBuilder(Skull.class, new SpongeSkullBuilder());
        dataManager.registerBuilder(Beacon.class, new SpongeBeaconBuilder());

        // Block stuff
        dataManager.registerBuilder(BlockSnapshot.class, new SpongeBlockSnapshotBuilder());
        dataManager.registerBuilder(BlockState.class, new SpongeBlockStateBuilder());
        dataManager.registerBuilderAndImpl(ImmutableTreeData.class, ImmutableSpongeTreeData.class, new ImmutableSpongeTreeDataBuilder());

        // Entity stuff
        dataManager.registerBuilder(EntitySnapshot.class, new SpongeEntitySnapshotBuilder());

        // ItemStack stuff
        dataManager.registerBuilder(ItemStack.class, new SpongeItemStackDataBuilder());
        dataManager.registerBuilder(ItemStackSnapshot.class, new SpongeItemStackSnapshotBuilder());
        dataManager.registerBuilder(ItemEnchantment.class, new SpongeItemEnchantmentBuilder());
        dataManager.registerBuilderAndImpl(ImmutableEnchantmentData.class, ImmutableSpongeEnchantmentData.class,
                new ImmutableItemEnchantmentDataBuilder());
        dataManager.registerBuilder(FireworkEffect.class, new SpongeFireworkEffectDataBuilder());

        // Text stuff
        dataManager.registerBuilder(Text.class, new TextConfigSerializer());
        dataManager.registerBuilder(BookView.class, new BookViewDataBuilder());

        // Effects stuff
        dataManager.registerBuilder(ParticleEffect.class, new SpongeParticleEffectBuilder());

        // Util stuff
        dataManager.registerBuilder(VariableAmount.BaseAndAddition.class, new BaseAndAdditionBuilder());
        dataManager.registerBuilder(VariableAmount.BaseAndVariance.class, new BaseAndVarianceBuilder());
        dataManager.registerBuilder(VariableAmount.Fixed.class, new FixedBuilder());
        dataManager.registerBuilder(VariableAmount.OptionalAmount.class, new OptionalVarianceBuilder());

        dataManager.registerBuilder((Class<Location<World>>) (Class<?>) Location.class, new LocationBuilder());
        dataManager.registerBuilder(SpongePlayerData.class, new SpongePlayerData.Builder());

        // Content Updaters
        dataManager.registerContentUpdater(BlockState.class, new SpongeBlockStateMetaContentUpdater());
        final InvisibilityDataAddVanishUpdater invisibilityUpdater = new InvisibilityDataAddVanishUpdater();
        dataManager.registerContentUpdater(InvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(ImmutableInvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(SpongeInvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(ImmutableSpongeInvisibilityData.class, invisibilityUpdater);

        // Data Manipulators

        dataManager.registerDataProcessorAndImpl(DisplayNameData.class, SpongeDisplayNameData.class,
                ImmutableDisplayNameData.class, ImmutableSpongeDisplayNameData.class, new DisplayNameDataProcessor());

        dataManager.registerDataProcessorAndImpl(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class,
                ImmutableSpongeDyeableData.class, new DyeableDataProcessor());

        // Entity Processors

        dataManager.registerDataProcessorAndImpl(ArmorStandData.class, SpongeArmorStandData.class,
                ImmutableArmorStandData.class, ImmutableSpongeArmorStandData.class, new ArmorStandDataProcessor());

        dataManager.registerDataProcessorAndImpl(FuseData.class, SpongeFuseData.class, ImmutableFuseData.class,
                ImmutableSpongeFuseData.class, new FuseDataProcessor());

        dataManager.registerDualProcessor(ExplosionRadiusData.class, SpongeExplosionRadiusData.class, ImmutableExplosionRadiusData.class,
                ImmutableSpongeExplosionRadiusData.class, new ExplosionRadiusDataProcessor());

        dataManager.registerDualProcessor(FireworkEffectData.class, SpongeFireworkEffectData.class,
                ImmutableFireworkEffectData.class, ImmutableSpongeFireworkEffectData.class, new FireworkEffectDataProcessor());

        dataManager.registerDualProcessor(FireworkRocketData.class, SpongeFireworkRocketData.class,
                ImmutableFireworkRocketData.class, ImmutableSpongeFireworkRocketData.class, new FireworkRocketDataProcessor());

        dataManager.registerDataProcessorAndImpl(HealthData.class, SpongeHealthData.class, ImmutableHealthData.class,
                ImmutableSpongeHealthData.class, new HealthDataProcessor());

        dataManager.registerDataProcessorAndImpl(IgniteableData.class, SpongeIgniteableData.class, ImmutableIgniteableData.class,
                ImmutableSpongeIgniteableData.class, new IgniteableDataProcessor());

        dataManager.registerDualProcessor(VelocityData.class, SpongeVelocityData.class, ImmutableVelocityData.class,
                ImmutableSpongeVelocityData.class, new VelocityDataProcessor());

        dataManager.registerDataProcessorAndImpl(FoodData.class, SpongeFoodData.class, ImmutableFoodData.class,
                ImmutableSpongeFoodData.class, new FoodDataProcessor());

        dataManager.registerDataProcessorAndImpl(BreathingData.class, SpongeBreathingData.class, ImmutableBreathingData.class,
                ImmutableSpongeBreathingData.class, new BreathingDataProcessor());

        dataManager.registerDualProcessor(ScreamingData.class, SpongeScreamingData.class, ImmutableScreamingData.class,
                ImmutableSpongeScreamingData.class, new ScreamingDataProcessor());

        dataManager.registerDualProcessor(SilentData.class, SpongeSilentData.class, ImmutableSilentData.class,
                ImmutableSpongeSilentData.class, new SilentDataProcessor());

        dataManager.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new RepresentedItemDataProcessor());

        dataManager.registerDataProcessorAndImpl(HorseData.class, SpongeHorseData.class, ImmutableHorseData.class,
                ImmutableSpongeHorseData.class, new HorseDataProcessor());

        dataManager.registerDualProcessor(SneakingData.class, SpongeSneakingData.class, ImmutableSneakingData.class,
                ImmutableSpongeSneakingData.class, new SneakingDataProcessor());

        dataManager.registerDataProcessorAndImpl(ExperienceHolderData.class, SpongeExperienceHolderData.class, ImmutableExperienceHolderData.class,
                ImmutableSpongeExperienceHolderData.class, new ExperienceHolderDataProcessor());

        dataManager.registerDataProcessorAndImpl(MovementSpeedData.class, SpongeMovementSpeedData.class, ImmutableMovementSpeedData.class,
                ImmutableSpongeMovementSpeedData.class, new MovementSpeedDataProcessor());

        dataManager.registerDualProcessor(SlimeData.class, SpongeSlimeData.class, ImmutableSlimeData.class, ImmutableSpongeSlimeData.class,
                new SlimeDataProcessor());

        dataManager.registerDataProcessorAndImpl(ZombieData.class, SpongeZombieData.class, ImmutableZombieData.class,
                ImmutableSpongeZombieData.class, new ZombieDataProcessor());

        dataManager.registerDualProcessor(PlayingData.class, SpongePlayingData.class, ImmutablePlayingData.class,
                ImmutableSpongePlayingData.class, new PlayingDataProcessor());

        dataManager.registerDualProcessor(SittingData.class, SpongeSittingData.class, ImmutableSittingData.class,
                ImmutableSpongeSittingData.class, new SittingDataProcessor());

        dataManager.registerDualProcessor(ShearedData.class, SpongeShearedData.class, ImmutableShearedData.class,
                ImmutableSpongeShearedData.class, new ShearedDataProcessor());

        dataManager.registerDualProcessor(PigSaddleData.class, SpongePigSaddleData.class, ImmutablePigSaddleData.class,
                ImmutableSpongePigSaddleData.class, new PigSaddleDataProcessor());

        dataManager.registerDualProcessor(TameableData.class, SpongeTameableData.class, ImmutableTameableData.class,
                ImmutableSpongeTameableData.class, new TameableDataProcessor());

        dataManager.registerDualProcessor(TameableData.class, SpongeTameableData.class, ImmutableTameableData.class,
                ImmutableSpongeTameableData.class, new HorseTameableDataProcessor());

        dataManager.registerDualProcessor(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                new WolfWetDataProcessor());

        dataManager.registerDualProcessor(ElderData.class, SpongeElderData.class, ImmutableElderData.class, ImmutableSpongeElderData.class,
                new ElderDataProcessor());

        dataManager.registerDualProcessor(AgentData.class, SpongeAgentData.class, ImmutableAgentData.class,
                ImmutableSpongeAgentData.class, new AgentDataProcessor());

        dataManager.registerDualProcessor(ChargedData.class, SpongeChargedData.class, ImmutableChargedData.class,
                ImmutableSpongeChargedData.class, new ChargedDataProcessor());

        dataManager.registerDualProcessor(FallDistanceData.class, SpongeFallDistanceData.class,
                ImmutableFallDistanceData.class, ImmutableSpongeFallDistanceData.class, new FallDistanceDataProcessor());

        dataManager.registerDataProcessorAndImpl(VehicleData.class, SpongeVehicleData.class, ImmutableVehicleData.class,
                ImmutableSpongeVehicleData.class, new VehicleDataProcessor());

        dataManager.registerDualProcessor(PassengerData.class, SpongePassengerData.class, ImmutablePassengerData.class,
                ImmutableSpongePassengerData.class, new PassengerDataProcessor());

        dataManager.registerDataProcessorAndImpl(MinecartBlockData.class, SpongeMinecartBlockData.class,
                ImmutableMinecartBlockData.class, ImmutableSpongeMinecartBlockData.class, new MinecartBlockDataProcessor());

        dataManager.registerDualProcessor(PlayerCreatedData.class, SpongePlayerCreatedData.class, ImmutablePlayerCreatedData.class,
                ImmutableSpongePlayerCreatedData.class, new PlayerCreatedDataProcessor());

        dataManager.registerDataProcessorAndImpl(InvisibilityData.class, SpongeInvisibilityData.class, ImmutableInvisibilityData.class,
                ImmutableSpongeInvisibilityData.class, new InvisibilityDataProcessor());

        dataManager.registerDataProcessorAndImpl(FallingBlockData.class, SpongeFallingBlockData.class, ImmutableFallingBlockData.class,
                ImmutableSpongeFallingBlockData.class, new FallingBlockDataProcessor());

        dataManager.registerDualProcessor(SkeletonData.class, SpongeSkeletonData.class, ImmutableSkeletonData.class,
                ImmutableSpongeSkeletonData.class, new SkeletonDataProcessor());

        dataManager.registerDualProcessor(RabbitData.class, SpongeRabbitData.class, ImmutableRabbitData.class,
                ImmutableSpongeRabbitData.class, new RabbitDataProcessor());

        dataManager.registerDualProcessor(RespawnLocationData.class, SpongeRespawnLocationData.class, ImmutableRespawnLocation.class,
                ImmutableSpongeRespawnLocation.class, new RespawnLocationDataProcessor());

        dataManager.registerDataProcessorAndImpl(CommandData.class, SpongeCommandData.class, ImmutableCommandData.class,
                ImmutableSpongeCommandData.class, new EntityCommandDataProcessor());

        dataManager.registerDualProcessor(ExpirableData.class, SpongeExpirableData.class, ImmutableExpirableData.class,
                ImmutableSpongeExpirableData.class, new EndermiteExpirableDataProcessor());

        dataManager.registerDualProcessor(ArtData.class, SpongeArtData.class, ImmutableArtData.class, ImmutableSpongeArtData.class,
                new ArtDataProcessor());

        dataManager.registerDualProcessor(CareerData.class, SpongeCareerData.class, ImmutableCareerData.class,
                ImmutableSpongeCareerData.class, new CareerDataProcessor());

        dataManager.registerDualProcessor(SkinData.class, SpongeSkinData.class, ImmutableSkinData.class,
                ImmutableSpongeSkinData.class, new SkinDataProcessor());

        dataManager.registerDualProcessor(ExpOrbData.class, SpongeExpOrbData.class, ImmutableExpOrbData.class,
                ImmutableSpongeExpOrbData.class, new ExpOrbDataProcessor());

        dataManager.registerDualProcessor(FlyingData.class, SpongeFlyingData.class, ImmutableFlyingData.class,
                ImmutableSpongeFlyingData.class, new FlyingDataProcessor());

        dataManager.registerDualProcessor(FlyingAbilityData.class, SpongeFlyingAbilityData.class, ImmutableFlyingAbilityData.class,
                ImmutableSpongeFlyingAbilityData.class, new FlyingAbilityDataProcessor());

        dataManager.registerDualProcessor(OcelotData.class, SpongeOcelotData.class, ImmutableOcelotData.class,
                ImmutableSpongeOcelotData.class, new OcelotDataProcessor());

        dataManager.registerDualProcessor(GameModeData.class, SpongeGameModeData.class, ImmutableGameModeData.class,
                ImmutableSpongeGameModeData.class, new GameModeDataProcessor());

        dataManager.registerDualProcessor(AggressiveData.class, SpongeAggressiveData.class, ImmutableAggressiveData.class,
                ImmutableSpongeAggressiveData.class, new AggressiveDataProcessor());

        dataManager.registerDualProcessor(AngerableData.class, SpongeAngerableData.class, ImmutableAngerableData.class,
                ImmutableSpongeAngerableData.class, new AngerableDataProcessor());

        dataManager.registerDualProcessor(RotationalData.class, SpongeRotationalData.class, ImmutableRotationalData.class,
                ImmutableSpongeRotationalData.class, new RotationalDataProcessor());

        dataManager.registerDualProcessor(AffectsSpawningData.class, SpongeAffectsSpawningData.class, ImmutableAffectsSpawningData.class,
                ImmutableSpongeAffectsSpawningData.class, new AffectsSpawningDataProcessor());

        dataManager.registerDualProcessor(CriticalHitData.class, SpongeCriticalHitData.class, ImmutableCriticalHitData.class,
                ImmutableSpongeCriticalHitData.class, new CriticalHitDataProcessor());

        dataManager.registerDualProcessor(TradeOfferData.class, SpongeTradeOfferData.class, ImmutableTradeOfferData.class,
                ImmutableSpongeTradeOfferData.class, new TradeOfferDataProcessor());

        dataManager.registerDualProcessor(KnockbackData.class, SpongeKnockbackData.class, ImmutableKnockbackData.class,
                ImmutableSpongeKnockbackData.class, new KnockbackDataProcessor());

        dataManager.registerDualProcessor(FlammableData.class, SpongeFlammableData.class,
                ImmutableFlammableData.class, ImmutableSpongeFlammableData.class, new BlazeFlammableDataProcessor());

        dataManager.registerDualProcessor(PersistingData.class, SpongePersistingData.class, ImmutablePersistingData.class,
                ImmutableSpongePersistingData.class, new PersistingDataProcessor());

        dataManager.registerDualProcessor(SprintData.class, SpongeSprintData.class, ImmutableSprintData.class,
                ImmutableSpongeSprintData.class, new SprintDataProcessor());

        dataManager.registerDualProcessor(AchievementData.class, SpongeAchievementData.class,
                ImmutableAchievementData.class, ImmutableSpongeAchievementData.class, new AchievementDataProcessor());

        dataManager.registerDualProcessor(StatisticData.class, SpongeStatisticData.class, ImmutableStatisticData.class,
                ImmutableSpongeStatisticData.class, new StatisticDataProcessor());

        dataManager.registerDualProcessor(StuckArrowsData.class, SpongeStuckArrowsData.class, ImmutableStuckArrowsData.class,
                ImmutableSpongeStuckArrowsData.class, new StuckArrowsDataProcessor());

        dataManager.registerDualProcessor(BreedableData.class, SpongeBreedableData.class, ImmutableBreedableData.class,
                ImmutableSpongeBreedableData.class, new BreedableDataProcessor());

        dataManager.registerDataProcessorAndImpl(JoinData.class, SpongeJoinData.class, ImmutableJoinData.class, ImmutableSpongeJoinData.class,
                new JoinDataProcessor());

        dataManager.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new EntityPotionDataProcessor());

        dataManager.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new PotionEntityPotionDataProcessor());

        dataManager.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new TippedArrowPotionDataProcessor());

        dataManager.registerDataProcessorAndImpl(BodyPartRotationalData.class, SpongeBodyPartRotationalData.class,
                ImmutableBodyPartRotationalData.class, ImmutableSpongeBodyPartRotationalData.class, new ArmorStandBodyPartRotationalDataProcessor());

        dataManager.registerDualProcessor(GriefingData.class, SpongeGriefingData.class, ImmutableGriefingData.class,
                ImmutableSpongeGriefingData.class, new GriefingDataProcessor());

        dataManager.registerDualProcessor(TargetedLocationData.class, SpongeTargetedLocationData.class,
                ImmutableTargetedLocationData.class, ImmutableSpongeTargetedLocationData.class, new EntityTargetedLocationDataProcessor());

        dataManager.registerDualProcessor(CustomNameVisibleData.class, SpongeCustomNameVisibleData.class, ImmutableCustomNameVisibleData.class,
                ImmutableSpongeCustomNameVisibleData.class, new CustomNameVisibleProcessor());

        dataManager.registerDualProcessor(InvulnerabilityData.class, SpongeInvulnerabilityData.class, ImmutableInvulnerabilityData.class,
                ImmutableSpongeInvulnerabilityData.class, new InvulnerabilityDataProcessor());

        dataManager.registerDualProcessor(GlowingData.class, SpongeGlowingData.class, ImmutableGlowingData.class, ImmutableSpongeGlowingData.class,
                new GlowingDataProcessor());

        dataManager.registerDualProcessor(GravityData.class, SpongeGravityData.class, ImmutableGravityData.class, ImmutableSpongeGravityData.class,
                new GravityDataProcessor());

        dataManager.registerDualProcessor(PickupRuleData.class, SpongePickupRuleData.class, ImmutablePickupRuleData.class,
                ImmutableSpongePickupRuleData.class, new PickupRuleDataProcessor());

        dataManager.registerDataProcessorAndImpl(PickupDelayData.class, SpongePickupDelayData.class, ImmutablePickupDelayData.class,
                ImmutableSpongePickupDelayData.class, new PickupDelayDataProcessor());

        dataManager.registerDataProcessorAndImpl(DespawnDelayData.class, SpongeDespawnDelayData.class, ImmutableDespawnDelayData.class,
                ImmutableSpongeDespawnDelayData.class, new DespawnDelayDataProcessor());

        // Item Processors

        dataManager.registerDualProcessor(FireworkEffectData.class, SpongeFireworkEffectData.class,
                ImmutableFireworkEffectData.class, ImmutableSpongeFireworkEffectData.class, new ItemFireworkEffectDataProcessor());

        dataManager.registerDualProcessor(FireworkRocketData.class, SpongeFireworkRocketData.class,
                ImmutableFireworkRocketData.class, ImmutableSpongeFireworkRocketData.class, new ItemFireworkRocketDataProcessor());

        dataManager.registerDualProcessor(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                ImmutableSpongeSkullData.class, new ItemSkullDataProcessor());

        dataManager.registerDualProcessor(SignData.class, SpongeSignData.class,
                ImmutableSignData.class, ImmutableSpongeSignData.class, new ItemSignDataProcessor());

        dataManager.registerDualProcessor(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                new ItemWetDataProcessor());

        dataManager.registerDualProcessor(ColoredData.class, SpongeColoredData.class,
                ImmutableColoredData.class, ImmutableSpongeColoredData.class, new ColoredDataProcessor());

        dataManager.registerDualProcessor(EnchantmentData.class, SpongeEnchantmentData.class, ImmutableEnchantmentData.class,
                ImmutableSpongeEnchantmentData.class, new ItemEnchantmentDataProcessor());

        dataManager.registerDualProcessor(LoreData.class, SpongeLoreData.class, ImmutableLoreData.class, ImmutableSpongeLoreData.class,
                new ItemLoreDataProcessor());

        dataManager.registerDualProcessor(PagedData.class, SpongePagedData.class, ImmutablePagedData.class, ImmutableSpongePagedData.class,
                new ItemPagedDataProcessor());

        dataManager.registerDualProcessor(GoldenAppleData.class, SpongeGoldenAppleData.class, ImmutableGoldenAppleData.class,
                ImmutableSpongeGoldenAppleData.class, new GoldenAppleDataProcessor());

        dataManager.registerDualProcessor(AuthorData.class, SpongeAuthorData.class, ImmutableAuthorData.class,
                ImmutableSpongeAuthorData.class, new ItemAuthorDataProcessor());

        dataManager.registerDualProcessor(BreakableData.class, SpongeBreakableData.class, ImmutableBreakableData.class,
                ImmutableSpongeBreakableData.class, new BreakableDataProcessor());

        dataManager.registerDualProcessor(PlaceableData.class, SpongePlaceableData.class, ImmutablePlaceableData.class,
                ImmutableSpongePlaceableData.class, new PlaceableDataProcessor());

        dataManager.registerDualProcessor(CoalData.class, SpongeCoalData.class, ImmutableCoalData.class,
                ImmutableSpongeCoalData.class, new CoalDataProcessor());

        dataManager.registerDualProcessor(CookedFishData.class, SpongeCookedFishData.class, ImmutableCookedFishData.class,
                ImmutableSpongeCookedFishData.class, new CookedFishDataProcessor());

        dataManager.registerDualProcessor(FishData.class, SpongeFishData.class, ImmutableFishData.class,
                ImmutableSpongeFishData.class, new FishDataProcessor());

        dataManager.registerDualProcessor(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class, ImmutableRepresentedPlayerData.class,
                ImmutableSpongeRepresentedPlayerData.class, new ItemSkullRepresentedPlayerDataProcessor());

        dataManager.registerDualProcessor(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new ItemLockableDataProcessor());

        dataManager.registerDataProcessorAndImpl(DurabilityData.class, SpongeDurabilityData.class, ImmutableDurabilityData.class,
                ImmutableSpongeDurabilityData.class, new DurabilityDataProcessor());

        dataManager.registerDualProcessor(SpawnableData.class, SpongeSpawnableData.class, ImmutableSpawnableData.class,
                ImmutableSpongeSpawnableData.class, new SpawnableDataProcessor());

        dataManager.registerDualProcessor(BlockItemData.class, SpongeBlockItemData.class, ImmutableBlockItemData.class,
                ImmutableSpongeBlockItemData.class, new BlockItemDataProcessor());

        dataManager.registerDualProcessor(GenerationData.class, SpongeGenerationData.class,
                ImmutableGenerationData.class, ImmutableSpongeGenerationData.class, new GenerationDataProcessor());

        dataManager.registerDualProcessor(StoredEnchantmentData.class, SpongeStoredEnchantmentData.class,
                ImmutableStoredEnchantmentData.class, ImmutableSpongeStoredEnchantmentData.class, new StoredEnchantmentDataProcessor());

        dataManager.registerDualProcessor(FluidItemData.class, SpongeFluidItemData.class, ImmutableFluidItemData.class,
                ImmutableSpongeFluidItemData.class, new FluidItemDataProcessor());

        dataManager.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new ItemPotionDataProcessor());

        dataManager.registerDataProcessorAndImpl(HideData.class, SpongeHideData.class, ImmutableHideData.class, ImmutableSpongeHideData.class,
                new HideDataProcessor());

        // Block Processors

        dataManager.registerDualProcessor(DirtData.class, SpongeDirtData.class, ImmutableDirtData.class,
                ImmutableSpongeDirtData.class, new DirtDataProcessor());

        dataManager.registerDualProcessor(StoneData.class, SpongeStoneData.class, ImmutableStoneData.class,
                ImmutableSpongeStoneData.class, new StoneDataProcessor());

        dataManager.registerDualProcessor(PrismarineData.class, SpongePrismarineData.class, ImmutablePrismarineData.class,
                ImmutableSpongePrismarineData.class, new PrismarineDataProcessor());

        dataManager.registerDualProcessor(BrickData.class, SpongeBrickData.class, ImmutableBrickData.class,
                ImmutableSpongeBrickData.class, new BrickDataProcessor());

        dataManager.registerDualProcessor(QuartzData.class, SpongeQuartzData.class, ImmutableQuartzData.class,
                ImmutableSpongeQuartzData.class, new QuartzDataProcessor());

        dataManager.registerDualProcessor(SandData.class, SpongeSandData.class, ImmutableSandData.class,
                ImmutableSpongeSandData.class, new SandDataProcessor());

        dataManager.registerDualProcessor(SlabData.class, SpongeSlabData.class, ImmutableSlabData.class,
                ImmutableSpongeSlabData.class, new SlabDataProcessor());

        dataManager.registerDualProcessor(SandstoneData.class, SpongeSandstoneData.class, ImmutableSandstoneData.class,
                ImmutableSpongeSandstoneData.class, new SandstoneDataProcessor());

        dataManager.registerDualProcessor(ComparatorData.class, SpongeComparatorData.class, ImmutableComparatorData.class,
                ImmutableSpongeComparatorData.class, new ComparatorDataProcessor());

        dataManager.registerDualProcessor(TreeData.class, SpongeTreeData.class, ImmutableTreeData.class,
                ImmutableSpongeTreeData.class, new TreeDataProcessor());

        dataManager.registerDualProcessor(DisguisedBlockData.class, SpongeDisguisedBlockData.class, ImmutableDisguisedBlockData.class,
                ImmutableSpongeDisguisedBlockData.class, new DisguisedBlockDataProcessor());

        dataManager.registerDualProcessor(HingeData.class, SpongeHingeData.class, ImmutableHingeData.class,
                ImmutableSpongeHingeData.class, new HingeDataProcessor());

        dataManager.registerDualProcessor(PistonData.class, SpongePistonData.class, ImmutablePistonData.class,
                ImmutableSpongePistonData.class, new PistonDataProcessor());

        dataManager.registerDualProcessor(PortionData.class, SpongePortionData.class, ImmutablePortionData.class,
                ImmutableSpongePortionData.class, new PortionDataProcessor());

        dataManager.registerDualProcessor(RailDirectionData.class, SpongeRailDirectionData.class, ImmutableRailDirectionData.class,
                ImmutableSpongeRailDirectionData.class, new RailDirectionDataProcessor());

        dataManager.registerDualProcessor(StairShapeData.class, SpongeStairShapeData.class, ImmutableStairShapeData.class,
                ImmutableSpongeStairShapeData.class, new StairShapeDataProcessor());

        dataManager.registerDualProcessor(WallData.class, SpongeWallData.class, ImmutableWallData.class,
                ImmutableSpongeWallData.class, new WallDataProcessor());

        dataManager.registerDualProcessor(ShrubData.class, SpongeShrubData.class, ImmutableShrubData.class,
                ImmutableSpongeShrubData.class, new ShrubDataProcessor());

        dataManager.registerDualProcessor(PlantData.class, SpongePlantData.class, ImmutablePlantData.class,
                ImmutableSpongePlantData.class, new PlantDataProcessor());

        dataManager.registerDualProcessor(DoublePlantData.class, SpongeDoublePlantData.class, ImmutableDoublePlantData.class,
                ImmutableSpongeDoublePlantData.class, new DoublePlantDataProcessor());

        dataManager.registerDualProcessor(BigMushroomData.class, SpongeBigMushroomData.class, ImmutableBigMushroomData.class,
                ImmutableSpongeBigMushroomData.class, new BigMushroomDataProcessor());

        dataManager.registerDualProcessor(AttachedData.class, SpongeAttachedData.class, ImmutableAttachedData.class,
                ImmutableSpongeAttachedData.class, new AttachedDataProcessor());

        dataManager.registerDataProcessorAndImpl(ConnectedDirectionData.class, SpongeConnectedDirectionData.class,
                ImmutableConnectedDirectionData.class, ImmutableSpongeConnectedDirectionData.class, new ConnectedDirectionDataProcessor());

        dataManager.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class,
                ImmutableSpongeDirectionalData.class, new DirectionalDataProcessor());

        dataManager.registerDualProcessor(DisarmedData.class, SpongeDisarmedData.class, ImmutableDisarmedData.class,
                ImmutableSpongeDisarmedData.class, new DisarmedDataProcessor());

        dataManager.registerDualProcessor(DropData.class, SpongeDropData.class, ImmutableDropData.class,
                ImmutableSpongeDropData.class, new DropDataProcessor());

        dataManager.registerDualProcessor(ExtendedData.class, SpongeExtendedData.class, ImmutableExtendedData.class,
                ImmutableSpongeExtendedData.class, new ExtendedDataProcessor());

        dataManager.registerDualProcessor(GrowthData.class, SpongeGrowthData.class, ImmutableGrowthData.class,
                ImmutableSpongeGrowthData.class, new GrowthDataProcessor());

        dataManager.registerDualProcessor(OpenData.class, SpongeOpenData.class, ImmutableOpenData.class,
                ImmutableSpongeOpenData.class, new OpenDataProcessor());

        dataManager.registerDualProcessor(PoweredData.class, SpongePoweredData.class, ImmutablePoweredData.class,
                ImmutableSpongePoweredData.class, new PoweredDataProcessor());

        dataManager.registerDualProcessor(RedstonePoweredData.class, SpongeRedstonePoweredData.class, ImmutableRedstonePoweredData.class,
                ImmutableSpongeRedstonePoweredData.class, new RedstonePoweredDataProcessor());

        dataManager.registerDualProcessor(SeamlessData.class, SpongeSeamlessData.class, ImmutableSeamlessData.class,
                ImmutableSpongeSeamlessData.class, new SeamlessDataProcessor());

        dataManager.registerDualProcessor(SnowedData.class, SpongeSnowedData.class, ImmutableSnowedData.class,
                ImmutableSpongeSnowedData.class, new SnowedDataProcessor());

        dataManager.registerDualProcessor(OccupiedData.class, SpongeOccupiedData.class, ImmutableOccupiedData.class,
                ImmutableSpongeOccupiedData.class, new OccupiedDataProcessor());

        dataManager.registerDualProcessor(InWallData.class, SpongeInWallData.class, ImmutableInWallData.class,
                ImmutableSpongeInWallData.class, new InWallDataProcessor());

        dataManager.registerDualProcessor(LayeredData.class, SpongeLayeredData.class, ImmutableLayeredData.class,
                ImmutableSpongeLayeredData.class, new LayeredDataProcessor());

        dataManager.registerDualProcessor(DecayableData.class, SpongeDecayableData.class, ImmutableDecayableData.class,
                ImmutableSpongeDecayableData.class, new DecayableDataProcessor());

        dataManager.registerDualProcessor(AxisData.class, SpongeAxisData.class, ImmutableAxisData.class,
                ImmutableSpongeAxisData.class, new AxisDataProcessor());

        dataManager.registerDualProcessor(DelayableData.class, SpongeDelayableData.class, ImmutableDelayableData.class,
                ImmutableSpongeDelayableData.class, new DelayableDataProcessor());

        dataManager.registerDualProcessor(MoistureData.class, SpongeMoistureData.class, ImmutableMoistureData.class,
                ImmutableSpongeMoistureData.class, new MoistureDataProcessor());

        // TileEntity Processors

        dataManager.registerDualProcessor(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                ImmutableSpongeSkullData.class, new TileEntitySkullDataProcessor());

        dataManager.registerDualProcessor(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class, ImmutableRepresentedPlayerData.class,
                ImmutableSpongeRepresentedPlayerData.class, new SkullRepresentedPlayerDataProcessor());

        dataManager.registerDualProcessor(SignData.class, SpongeSignData.class,
                ImmutableSignData.class, ImmutableSpongeSignData.class, new TileEntitySignDataProcessor());

        dataManager.registerDataProcessorAndImpl(FurnaceData.class, SpongeFurnaceData.class,
                ImmutableFurnaceData.class, ImmutableSpongeFurnaceData.class, new FurnaceDataProcessor());

        dataManager.registerDualProcessor(BrewingStandData.class, SpongeBrewingStandData.class, ImmutableBrewingStandData.class,
                ImmutableSpongeBrewingStandData.class, new BrewingStandDataProcessor());

        dataManager.registerDataProcessorAndImpl(ConnectedDirectionData.class, SpongeConnectedDirectionData.class,
                ImmutableConnectedDirectionData.class, ImmutableSpongeConnectedDirectionData.class, new TileConnectedDirectionDataProcessor());

        dataManager.registerDualProcessor(CooldownData.class, SpongeCooldownData.class, ImmutableCooldownData.class,
                ImmutableSpongeCooldownData.class, new CooldownDataProcessor());

        dataManager.registerDualProcessor(NoteData.class, SpongeNoteData.class, ImmutableNoteData.class,
                ImmutableSpongeNoteData.class, new NoteDataProcessor());

        dataManager.registerDualProcessor(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new TileEntityLockableDataProcessor());

        dataManager.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new JukeboxDataProcessor());

        dataManager.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new FlowerPotDataProcessor());

        dataManager.registerDataProcessorAndImpl(BannerData.class, SpongeBannerData.class, ImmutableBannerData.class,
                ImmutableSpongeBannerData.class, new TileEntityBannerDataProcessor());

        dataManager.registerDataProcessorAndImpl(CommandData.class, SpongeCommandData.class, ImmutableCommandData.class,
                ImmutableSpongeCommandData.class, new TileEntityCommandDataProcessor());

        dataManager.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class,
                ImmutableSpongeDirectionalData.class, new SkullRotationDataProcessor());

        dataManager.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class,
            ImmutableDirectionalData.class, ImmutableSpongeDirectionalData.class, new HangingDataProcessor());

        dataManager.registerDataProcessorAndImpl(BeaconData.class, SpongeBeaconData.class,
                ImmutableBeaconData.class, ImmutableSpongeBeaconData.class, new BeaconDataProcessor());

        // Values

        dataManager.registerValueProcessor(Keys.FUSE_DURATION, new FuseDurationValueProcessor());
        dataManager.registerValueProcessor(Keys.TICKS_REMAINING, new TicksRemainingValueProcessor());
        dataManager.registerValueProcessor(Keys.HEALTH, new HealthValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_HEALTH, new MaxHealthValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRE_TICKS, new FireTicksValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRE_DAMAGE_DELAY, new FireDamageDelayValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new ItemDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new TileEntityDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new EntityDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.FOOD_LEVEL, new FoodLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.SATURATION, new FoodSaturationValueProcessor());
        dataManager.registerValueProcessor(Keys.EXHAUSTION, new FoodExhaustionValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_AIR, new MaxAirValueProcessor());
        dataManager.registerValueProcessor(Keys.REMAINING_AIR, new RemainingAirValueProcessor());
        dataManager.registerValueProcessor(Keys.HORSE_COLOR, new HorseColorValueProcessor());
        dataManager.registerValueProcessor(Keys.HORSE_STYLE, new HorseStyleValueProcessor());
        dataManager.registerValueProcessor(Keys.HORSE_VARIANT, new HorseVariantValueProcessor());
        dataManager.registerValueProcessor(Keys.EXPERIENCE_LEVEL, new ExperienceLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.TOTAL_EXPERIENCE, new TotalExperienceValueProcessor());
        dataManager.registerValueProcessor(Keys.EXPERIENCE_SINCE_LEVEL, new ExperienceSinceLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.EXPERIENCE_FROM_START_OF_LEVEL, new ExperienceFromStartOfLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.WALKING_SPEED, new WalkingSpeedValueProcessor());
        dataManager.registerValueProcessor(Keys.FLYING_SPEED, new FlyingSpeedValueProcessor());
        dataManager.registerValueProcessor(Keys.PASSED_BURN_TIME, new PassedBurnTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_BURN_TIME, new MaxBurnTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.PASSED_COOK_TIME, new PassedCookTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_COOK_TIME, new MaxCookTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.UNBREAKABLE, new UnbreakableValueProcessor());
        dataManager.registerValueProcessor(Keys.ITEM_DURABILITY, new ItemDurabilityValueProcessor());
        dataManager.registerValueProcessor(Keys.VEHICLE, new VehicleValueProcessor());
        dataManager.registerValueProcessor(Keys.BASE_VEHICLE, new BaseVehicleValueProcessor());
        dataManager.registerValueProcessor(Keys.REPRESENTED_BLOCK, new RepresentedBlockValueProcessor());
        dataManager.registerValueProcessor(Keys.OFFSET, new OffsetValueProcessor());
        dataManager.registerValueProcessor(Keys.FALL_DAMAGE_PER_BLOCK, new FallHurtAmountValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_FALL_DAMAGE, new MaxFallDamageValueProcessor());
        dataManager.registerValueProcessor(Keys.FALLING_BLOCK_STATE, new FallingBlockStateValueProcessor());
        dataManager.registerValueProcessor(Keys.CAN_PLACE_AS_BLOCK, new CanPlaceAsBlockValueProcessor());
        dataManager.registerValueProcessor(Keys.CAN_DROP_AS_ITEM, new CanDropAsItemValueProcessor());
        dataManager.registerValueProcessor(Keys.FALL_TIME, new FallTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, new FallingBlockCanHurtEntitiesValueProcessor());
        dataManager.registerValueProcessor(Keys.CONNECTED_DIRECTIONS, new ConnectedDirectionsValueProcessor());
        dataManager.registerValueProcessor(Keys.CONNECTED_EAST, new ConnectedEastValueProcessor());
        dataManager.registerValueProcessor(Keys.CONNECTED_NORTH, new ConnectedNorthValueProcessor());
        dataManager.registerValueProcessor(Keys.CONNECTED_SOUTH, new ConnectedSouthValueProcessor());
        dataManager.registerValueProcessor(Keys.CONNECTED_WEST, new ConnectedWestValueProcessor());
        dataManager.registerValueProcessor(Keys.BANNER_BASE_COLOR, new TileBannerBaseColorValueProcessor());
        dataManager.registerValueProcessor(Keys.BANNER_PATTERNS, new TileBannerPatternLayersValueProcessor());
        dataManager.registerValueProcessor(Keys.LAST_COMMAND_OUTPUT, new EntityLastCommandOutputValueProcessor());
        dataManager.registerValueProcessor(Keys.LAST_COMMAND_OUTPUT, new TileEntityLastCommandOutputValueProcessor());
        dataManager.registerValueProcessor(Keys.COMMAND, new EntityCommandValueProcessor());
        dataManager.registerValueProcessor(Keys.COMMAND, new TileEntityCommandValueProcessor());
        dataManager.registerValueProcessor(Keys.SUCCESS_COUNT, new EntitySuccessCountValueProcessor());
        dataManager.registerValueProcessor(Keys.SUCCESS_COUNT, new TileEntitySuccessCountValueProcessor());
        dataManager.registerValueProcessor(Keys.TRACKS_OUTPUT, new EntityTracksOutputValueProcessor());
        dataManager.registerValueProcessor(Keys.TRACKS_OUTPUT, new TileEntityTracksOutputValueProcessor());
        dataManager.registerValueProcessor(Keys.INVISIBLE, new InvisibilityValueProcessor());
        dataManager.registerValueProcessor(Keys.VANISH, new VanishValueProcessor());
        dataManager.registerValueProcessor(Keys.VANISH_IGNORES_COLLISION, new VanishCollisionValueProcessor());
        dataManager.registerValueProcessor(Keys.VANISH_PREVENTS_TARGETING, new VanishTargetValueProcessor());
        dataManager.registerValueProcessor(Keys.DYE_COLOR, new WolfDyeColorValueProcessor());
        dataManager.registerValueProcessor(Keys.DYE_COLOR, new SheepDyeColorValueProcessor());
        dataManager.registerValueProcessor(Keys.DYE_COLOR, new ItemDyeColorValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRST_DATE_PLAYED, new FirstJoinValueProcessor());
        dataManager.registerValueProcessor(Keys.LAST_DATE_PLAYED, new LastPlayedValueProcessor());
        dataManager.registerValueProcessor(Keys.HIDE_ENCHANTMENTS, new HideEnchantmentsValueProcessor());
        dataManager.registerValueProcessor(Keys.HIDE_ATTRIBUTES, new HideAttributesValueProcessor());
        dataManager.registerValueProcessor(Keys.HIDE_UNBREAKABLE, new HideUnbreakableValueProcessor());
        dataManager.registerValueProcessor(Keys.HIDE_CAN_DESTROY, new HideCanDestroyValueProcessor());
        dataManager.registerValueProcessor(Keys.HIDE_CAN_PLACE, new HideCanPlaceValueProcessor());
        dataManager.registerValueProcessor(Keys.HIDE_MISCELLANEOUS, new HideMiscellaneousValueProcessor());
        dataManager.registerValueProcessor(Keys.BODY_ROTATIONS, new BodyRotationsValueProcessor());
        dataManager.registerValueProcessor(Keys.HEAD_ROTATION, new HeadRotationValueProcessor());
        dataManager.registerValueProcessor(Keys.CHEST_ROTATION, new ChestRotationValueProcessor());
        dataManager.registerValueProcessor(Keys.LEFT_ARM_ROTATION, new LeftArmRotationValueProcessor());
        dataManager.registerValueProcessor(Keys.RIGHT_ARM_ROTATION, new RightArmRotationValueProcessor());
        dataManager.registerValueProcessor(Keys.LEFT_LEG_ROTATION, new LeftLegRotationValueProcessor());
        dataManager.registerValueProcessor(Keys.RIGHT_LEG_ROTATION, new RightLegRotationValueProcessor());
        dataManager.registerValueProcessor(Keys.BEACON_PRIMARY_EFFECT, new BeaconPrimaryEffectValueProcessor());
        dataManager.registerValueProcessor(Keys.BEACON_SECONDARY_EFFECT, new BeaconSecondaryEffectValueProcessor());
        dataManager.registerValueProcessor(Keys.ARMOR_STAND_HAS_BASE_PLATE, new ArmorStandBasePlateValueProcessor());
        dataManager.registerValueProcessor(Keys.ARMOR_STAND_MARKER, new ArmorStandMarkerValueProcessor());
        dataManager.registerValueProcessor(Keys.ARMOR_STAND_IS_SMALL, new ArmorStandSmallValueProcessor());
        dataManager.registerValueProcessor(Keys.ARMOR_STAND_HAS_ARMS, new ArmorStandArmsValueProcessor());
        dataManager.registerValueProcessor(Keys.ZOMBIE_TYPE, new ZombieTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.VILLAGER_ZOMBIE_PROFESSION, new VillagerZombieProfessionValueProcessor());
        dataManager.registerValueProcessor(Keys.PICKUP_DELAY, new PickupDelayValueProcessor());
        dataManager.registerValueProcessor(Keys.INFINITE_PICKUP_DELAY, new InfinitePickupDelayValueProcessor());
        dataManager.registerValueProcessor(Keys.DESPAWN_DELAY, new DespawnDelayValueProcessor());
        dataManager.registerValueProcessor(Keys.INFINITE_DESPAWN_DELAY, new InfiniteDespawnDelayValueProcessor());

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
        propertyRegistry.register(SurrogateBlockProperty.class, new SurrogateBlockPropertyStore());
        propertyRegistry.register(FullBlockSelectionBoxProperty.class, new FullBlockSelectionBoxPropertyStore());

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
        propertyRegistry.register(ToolTypeProperty.class, new ToolTypePropertyStore());
        propertyRegistry.register(ArmorTypeProperty.class, new ArmorTypePropertyStore());
        propertyRegistry.register(SmeltableProperty.class, new SmeltablePropertyStore());

        // Entities
        propertyRegistry.register(EyeLocationProperty.class, new EyeLocationPropertyStore());
        propertyRegistry.register(EyeHeightProperty.class, new EyeHeightPropertyStore());
    }

}
