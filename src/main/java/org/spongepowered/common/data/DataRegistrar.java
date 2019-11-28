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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.*;
import org.spongepowered.api.block.tileentity.carrier.*;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
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
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.item.enchantment.Enchantment;
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
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.BookViewDataBuilder;
import org.spongepowered.api.text.serializer.TextConfigSerializer;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.authlib.SpongeGameProfileBuilder;
import org.spongepowered.common.data.builder.block.state.SpongeBlockStateMetaContentUpdater;
import org.spongepowered.common.data.builder.block.tileentity.*;
import org.spongepowered.common.data.builder.item.*;
import org.spongepowered.common.data.builder.manipulator.InvisibilityDataAddVanishUpdater;
import org.spongepowered.common.data.builder.manipulator.immutable.block.ImmutableSpongeTreeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.item.ImmutableItemEnchantmentDataBuilder;
import org.spongepowered.common.effect.potion.PotionEffectContentUpdater;
import org.spongepowered.common.effect.potion.SpongePotionBuilder;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.item.enchantment.SpongeEnchantmentBuilder;
import org.spongepowered.common.data.builder.meta.SpongePatternLayerBuilder;
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
import org.spongepowered.common.data.processor.multi.*;
import org.spongepowered.common.data.processor.multi.block.*;
import org.spongepowered.common.data.processor.multi.entity.*;
import org.spongepowered.common.data.processor.multi.item.*;
import org.spongepowered.common.data.processor.multi.tileentity.*;
import org.spongepowered.common.data.processor.value.block.*;
import org.spongepowered.common.data.processor.value.entity.*;
import org.spongepowered.common.data.processor.value.item.*;
import org.spongepowered.common.data.processor.value.tileentity.*;
import org.spongepowered.common.data.processor.value.*;
import org.spongepowered.common.data.property.store.block.*;
import org.spongepowered.common.data.property.store.entity.*;
import org.spongepowered.common.data.property.store.item.*;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.LegacyCustomDataClassContentUpdater;
import org.spongepowered.common.effect.particle.SpongeParticleEffectBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.item.inventory.SpongeItemStackBuilder;
import org.spongepowered.common.item.inventory.ItemStackSnapshotDuplicateManipulatorUpdater;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.storage.SpongePlayerData;

public final class DataRegistrar {

    @SuppressWarnings("unchecked")
    public static void setupSerialization() {
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
        dataManager.registerBuilder(LocatableBlock.class, new SpongeLocatableBlockBuilder());

        // Block stuff
        dataManager.registerBuilder(BlockSnapshot.class, SpongeBlockSnapshotBuilder.pooled());
        dataManager.registerBuilder(BlockState.class, new SpongeBlockStateBuilder());
        dataManager.registerBuilderAndImpl(ImmutableTreeData.class, ImmutableSpongeTreeData.class, new ImmutableSpongeTreeDataBuilder());

        // Entity stuff
        dataManager.registerBuilder(EntitySnapshot.class, new SpongeEntitySnapshotBuilder());
        dataManager.registerBuilder(EntityArchetype.class, new SpongeEntityArchetypeBuilder());

        // ItemStack stuff
        dataManager.registerBuilder(ItemStack.class, new SpongeItemStackBuilder());
        dataManager.registerBuilder(ItemStackSnapshot.class, new SpongeItemStackSnapshotBuilder());
        dataManager.registerBuilder(Enchantment.class, new SpongeEnchantmentBuilder());
        dataManager.registerBuilderAndImpl(ImmutableEnchantmentData.class, ImmutableSpongeEnchantmentData.class,
                new ImmutableItemEnchantmentDataBuilder());
        dataManager.registerBuilder(FireworkEffect.class, new SpongeFireworkEffectDataBuilder());

        // Text stuff
        dataManager.registerBuilder(Text.class, new TextConfigSerializer());
        dataManager.registerBuilder(BookView.class, new BookViewDataBuilder());
        dataManager.registerBuilder(FluidStackSnapshot.class, new SpongeFluidStackSnapshotBuilder());

        // Effects stuff
        dataManager.registerBuilder(ParticleEffect.class, new SpongeParticleEffectBuilder());

        // Util stuff
        dataManager.registerBuilder(VariableAmount.BaseAndAddition.class, new BaseAndAdditionBuilder());
        dataManager.registerBuilder(VariableAmount.BaseAndVariance.class, new BaseAndVarianceBuilder());
        dataManager.registerBuilder(VariableAmount.Fixed.class, new FixedBuilder());
        dataManager.registerBuilder(VariableAmount.OptionalAmount.class, new OptionalVarianceBuilder());

        dataManager.registerBuilder((Class<Location<World>>) (Class<?>) Location.class, new LocationBuilder());
        dataManager.registerBuilder(SpongePlayerData.class, new SpongePlayerData.Builder());

        dataManager.registerBuilder(GameProfile.class, new SpongeGameProfileBuilder());

        dataManager.registerBuilder(Color.class, new Color.Builder());
        dataManager.registerBuilder(TradeOffer.class, new SpongeTradeOfferBuilder());

        dataManager.registerBuilder(PotionEffect.class, new SpongePotionBuilder());

        // Content Updaters
        dataManager.registerContentUpdater(BlockState.class, new SpongeBlockStateMetaContentUpdater());
        final InvisibilityDataAddVanishUpdater invisibilityUpdater = new InvisibilityDataAddVanishUpdater();
        dataManager.registerContentUpdater(InvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(ImmutableInvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(SpongeInvisibilityData.class, invisibilityUpdater);
        dataManager.registerContentUpdater(ImmutableSpongeInvisibilityData.class, invisibilityUpdater);
        final PotionEffectContentUpdater potionUpdater = new PotionEffectContentUpdater();
        dataManager.registerContentUpdater(PotionEffect.class, potionUpdater);
        dataManager.registerContentUpdater(PotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(ImmutablePotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(SpongePotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(ImmutableSpongePotionEffectData.class, potionUpdater);
        dataManager.registerContentUpdater(ItemStackSnapshot.class, new ItemStackSnapshotDuplicateManipulatorUpdater());

        // Content Updaters for Custom Data
        dataManager.registerContentUpdater(DataManipulator.class, new LegacyCustomDataClassContentUpdater());

        // Data Manipulators

        DataUtil.registerDataProcessorAndImpl(DisplayNameData.class, SpongeDisplayNameData.class,
                ImmutableDisplayNameData.class, ImmutableSpongeDisplayNameData.class, new DisplayNameDataProcessor());

        // Entity Processors

        DataUtil.registerDataProcessorAndImpl(ArmorStandData.class, SpongeArmorStandData.class,
                ImmutableArmorStandData.class, ImmutableSpongeArmorStandData.class, new ArmorStandDataProcessor());

        DataUtil.registerDataProcessorAndImpl(InvulnerabilityData.class, SpongeInvulnerabilityData.class,
                ImmutableInvulnerabilityData.class, ImmutableSpongeInvulnerabilityData.class, new InvulnerabilityDataProcessor());

        DataUtil.registerDataProcessorAndImpl(DamageableData.class, SpongeDamageableData.class,
                ImmutableDamageableData.class, ImmutableSpongeDamageableData.class, new DamageableDataProcessor());

        DataUtil.registerDataProcessorAndImpl(FuseData.class, SpongeFuseData.class, ImmutableFuseData.class,
                ImmutableSpongeFuseData.class, new FuseDataProcessor());

        DataUtil.registerDualProcessor(ExplosionRadiusData.class, SpongeExplosionRadiusData.class, ImmutableExplosionRadiusData.class,
                ImmutableSpongeExplosionRadiusData.class, new ExplosionRadiusDataProcessor());

        DataUtil.registerDualProcessor(ActiveItemData.class, SpongeActiveItemData.class, ImmutableActiveItemData.class,
                ImmutableSpongeActiveItemData.class, new ActiveItemDataProcessor());

        DataUtil.registerDualProcessor(FireworkEffectData.class, SpongeFireworkEffectData.class,
                ImmutableFireworkEffectData.class, ImmutableSpongeFireworkEffectData.class, new FireworkEffectDataProcessor());

        DataUtil.registerDualProcessor(FireworkRocketData.class, SpongeFireworkRocketData.class,
                ImmutableFireworkRocketData.class, ImmutableSpongeFireworkRocketData.class, new FireworkRocketDataProcessor());

        DataUtil.registerDataProcessorAndImpl(HealthData.class, SpongeHealthData.class, ImmutableHealthData.class,
                ImmutableSpongeHealthData.class, new HealthDataProcessor());

        DataUtil.registerDataProcessorAndImpl(AgeableData.class, SpongeAgeableData.class, ImmutableAgeableData.class,
                ImmutableSpongeAgeableData.class, new AgeableDataProcessor());

        DataUtil.registerDataProcessorAndImpl(AgeableData.class, SpongeAgeableData.class, ImmutableAgeableData.class,
                ImmutableSpongeAgeableData.class, new ZombieAgeableDataProcessor());

        DataUtil.registerDataProcessorAndImpl(IgniteableData.class, SpongeIgniteableData.class, ImmutableIgniteableData.class,
                ImmutableSpongeIgniteableData.class, new IgniteableDataProcessor());

        DataUtil.registerDualProcessor(VelocityData.class, SpongeVelocityData.class, ImmutableVelocityData.class,
                ImmutableSpongeVelocityData.class, new VelocityDataProcessor());

        DataUtil.registerDualProcessor(AccelerationData.class, SpongeAccelerationData.class, ImmutableAccelerationData.class,
          ImmutableSpongeAccelerationData.class, new AccelerationDataProcessor());

        DataUtil.registerDataProcessorAndImpl(FoodData.class, SpongeFoodData.class, ImmutableFoodData.class,
                ImmutableSpongeFoodData.class, new FoodDataProcessor());

        DataUtil.registerDataProcessorAndImpl(BreathingData.class, SpongeBreathingData.class, ImmutableBreathingData.class,
                ImmutableSpongeBreathingData.class, new BreathingDataProcessor());

        DataUtil.registerDualProcessor(ScreamingData.class, SpongeScreamingData.class, ImmutableScreamingData.class,
                ImmutableSpongeScreamingData.class, new ScreamingDataProcessor());

        DataUtil.registerDualProcessor(SilentData.class, SpongeSilentData.class, ImmutableSilentData.class,
                ImmutableSpongeSilentData.class, new SilentDataProcessor());

        DataUtil.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new RepresentedItemDataProcessor());

        DataUtil.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new ThrownPotionItemDataProcessor());

        DataUtil.registerDataProcessorAndImpl(HorseData.class, SpongeHorseData.class, ImmutableHorseData.class,
                ImmutableSpongeHorseData.class, new HorseDataProcessor());

        DataUtil.registerDualProcessor(SneakingData.class, SpongeSneakingData.class, ImmutableSneakingData.class,
                ImmutableSpongeSneakingData.class, new SneakingDataProcessor());

        DataUtil.registerDataProcessorAndImpl(ExperienceHolderData.class, SpongeExperienceHolderData.class, ImmutableExperienceHolderData.class,
                ImmutableSpongeExperienceHolderData.class, new ExperienceHolderDataProcessor());

        DataUtil.registerDataProcessorAndImpl(MovementSpeedData.class, SpongeMovementSpeedData.class, ImmutableMovementSpeedData.class,
                ImmutableSpongeMovementSpeedData.class, new MovementSpeedDataProcessor());

        DataUtil.registerDualProcessor(ElytraFlyingData.class, SpongeElytraFlyingData.class, ImmutableElytraFlyingData.class,
            ImmutableSpongeElytraFlyingData.class, new ElytraFlyingDataProcessor());

        DataUtil.registerDualProcessor(SlimeData.class, SpongeSlimeData.class, ImmutableSlimeData.class, ImmutableSpongeSlimeData.class,
                new SlimeDataProcessor());

        DataUtil.registerDualProcessor(PlayingData.class, SpongePlayingData.class, ImmutablePlayingData.class,
                ImmutableSpongePlayingData.class, new PlayingDataProcessor());

        DataUtil.registerDualProcessor(SittingData.class, SpongeSittingData.class, ImmutableSittingData.class,
                ImmutableSpongeSittingData.class, new SittingDataProcessor());

        DataUtil.registerDualProcessor(JohnnyData.class, SpongeJohnnyData.class, ImmutableJohnnyData.class,
                ImmutableSpongeJohnnyData.class, new JohnnyDataProcessor());

        DataUtil.registerDualProcessor(ShearedData.class, SpongeShearedData.class, ImmutableShearedData.class,
                ImmutableSpongeShearedData.class, new ShearedDataProcessor());

        DataUtil.registerDualProcessor(PigSaddleData.class, SpongePigSaddleData.class, ImmutablePigSaddleData.class,
                ImmutableSpongePigSaddleData.class, new PigSaddleDataProcessor());

        DataUtil.registerDualProcessor(TameableData.class, SpongeTameableData.class, ImmutableTameableData.class,
                ImmutableSpongeTameableData.class, new TameableDataProcessor());

        DataUtil.registerDualProcessor(TameableData.class, SpongeTameableData.class, ImmutableTameableData.class,
                ImmutableSpongeTameableData.class, new HorseTameableDataProcessor());

        DataUtil.registerDualProcessor(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                new WolfWetDataProcessor());

        DataUtil.registerDualProcessor(AgentData.class, SpongeAgentData.class, ImmutableAgentData.class,
                ImmutableSpongeAgentData.class, new AgentDataProcessor());

        DataUtil.registerDualProcessor(ChargedData.class, SpongeChargedData.class, ImmutableChargedData.class,
                ImmutableSpongeChargedData.class, new ChargedDataProcessor());

        DataUtil.registerDualProcessor(FallDistanceData.class, SpongeFallDistanceData.class,
                ImmutableFallDistanceData.class, ImmutableSpongeFallDistanceData.class, new FallDistanceDataProcessor());

        DataUtil.registerDataProcessorAndImpl(VehicleData.class, SpongeVehicleData.class, ImmutableVehicleData.class,
                ImmutableSpongeVehicleData.class, new VehicleDataProcessor());

        DataUtil.registerDualProcessor(TreeData.class, SpongeTreeData.class, ImmutableTreeData.class,
                ImmutableSpongeTreeData.class, new BoatTreeTypeValueProcessor());

        DataUtil.registerDualProcessor(PassengerData.class, SpongePassengerData.class, ImmutablePassengerData.class,
                ImmutableSpongePassengerData.class, new PassengerDataProcessor());

        DataUtil.registerDataProcessorAndImpl(MinecartBlockData.class, SpongeMinecartBlockData.class,
                ImmutableMinecartBlockData.class, ImmutableSpongeMinecartBlockData.class, new MinecartBlockDataProcessor());

        DataUtil.registerDualProcessor(PlayerCreatedData.class, SpongePlayerCreatedData.class, ImmutablePlayerCreatedData.class,
                ImmutableSpongePlayerCreatedData.class, new PlayerCreatedDataProcessor());

        DataUtil.registerDataProcessorAndImpl(InvisibilityData.class, SpongeInvisibilityData.class, ImmutableInvisibilityData.class,
                ImmutableSpongeInvisibilityData.class, new InvisibilityDataProcessor());

        DataUtil.registerDataProcessorAndImpl(FallingBlockData.class, SpongeFallingBlockData.class, ImmutableFallingBlockData.class,
                ImmutableSpongeFallingBlockData.class, new FallingBlockDataProcessor());

        DataUtil.registerDualProcessor(RabbitData.class, SpongeRabbitData.class, ImmutableRabbitData.class,
                ImmutableSpongeRabbitData.class, new RabbitDataProcessor());

        DataUtil.registerDualProcessor(RespawnLocationData.class, SpongeRespawnLocationData.class, ImmutableRespawnLocation.class,
                ImmutableSpongeRespawnLocation.class, new RespawnLocationDataProcessor());

        DataUtil.registerDataProcessorAndImpl(CommandData.class, SpongeCommandData.class, ImmutableCommandData.class,
                ImmutableSpongeCommandData.class, new EntityCommandDataProcessor());

        DataUtil.registerDualProcessor(ExpirableData.class, SpongeExpirableData.class, ImmutableExpirableData.class,
                ImmutableSpongeExpirableData.class, new EndermiteExpirableDataProcessor());

        DataUtil.registerDualProcessor(ArtData.class, SpongeArtData.class, ImmutableArtData.class, ImmutableSpongeArtData.class,
                new ArtDataProcessor());

        DataUtil.registerDualProcessor(CareerData.class, SpongeCareerData.class, ImmutableCareerData.class,
                ImmutableSpongeCareerData.class, new CareerDataProcessor());

        DataUtil.registerDualProcessor(SkinData.class, SpongeSkinData.class, ImmutableSkinData.class,
                ImmutableSpongeSkinData.class, new SkinDataProcessor());

        DataUtil.registerDualProcessor(ExpOrbData.class, SpongeExpOrbData.class, ImmutableExpOrbData.class,
                ImmutableSpongeExpOrbData.class, new ExpOrbDataProcessor());

        DataUtil.registerDualProcessor(FlyingData.class, SpongeFlyingData.class, ImmutableFlyingData.class,
                ImmutableSpongeFlyingData.class, new FlyingDataProcessor());

        DataUtil.registerDualProcessor(FlyingAbilityData.class, SpongeFlyingAbilityData.class, ImmutableFlyingAbilityData.class,
                ImmutableSpongeFlyingAbilityData.class, new FlyingAbilityDataProcessor());

        DataUtil.registerDualProcessor(OcelotData.class, SpongeOcelotData.class, ImmutableOcelotData.class,
                ImmutableSpongeOcelotData.class, new OcelotDataProcessor());

        DataUtil.registerDualProcessor(GameModeData.class, SpongeGameModeData.class, ImmutableGameModeData.class,
                ImmutableSpongeGameModeData.class, new GameModeDataProcessor());

        DataUtil.registerDualProcessor(AbsorptionData.class, SpongeAbsorptionData.class, ImmutableAbsorptionData.class,
                ImmutableSpongeAbsorptionData.class, new AbsorptionDataProcessor());

        DataUtil.registerDualProcessor(AggressiveData.class, SpongeAggressiveData.class, ImmutableAggressiveData.class,
                ImmutableSpongeAggressiveData.class, new AggressiveDataProcessor());

        DataUtil.registerDualProcessor(AngerableData.class, SpongeAngerableData.class, ImmutableAngerableData.class,
                ImmutableSpongeAngerableData.class, new AngerableDataProcessor());

        DataUtil.registerDualProcessor(RotationalData.class, SpongeRotationalData.class, ImmutableRotationalData.class,
                ImmutableSpongeRotationalData.class, new RotationalDataProcessor());

        DataUtil.registerDualProcessor(AffectsSpawningData.class, SpongeAffectsSpawningData.class, ImmutableAffectsSpawningData.class,
                ImmutableSpongeAffectsSpawningData.class, new AffectsSpawningDataProcessor());

        DataUtil.registerDualProcessor(CriticalHitData.class, SpongeCriticalHitData.class, ImmutableCriticalHitData.class,
                ImmutableSpongeCriticalHitData.class, new CriticalHitDataProcessor());

        DataUtil.registerDualProcessor(TradeOfferData.class, SpongeTradeOfferData.class, ImmutableTradeOfferData.class,
                ImmutableSpongeTradeOfferData.class, new TradeOfferDataProcessor());

        DataUtil.registerDualProcessor(KnockbackData.class, SpongeKnockbackData.class, ImmutableKnockbackData.class,
                ImmutableSpongeKnockbackData.class, new KnockbackDataProcessor());

        DataUtil.registerDualProcessor(FlammableData.class, SpongeFlammableData.class,
                ImmutableFlammableData.class, ImmutableSpongeFlammableData.class, new BlazeFlammableDataProcessor());

        DataUtil.registerDualProcessor(PersistingData.class, SpongePersistingData.class, ImmutablePersistingData.class,
                ImmutableSpongePersistingData.class, new PersistingDataProcessor());

        DataUtil.registerDualProcessor(SprintData.class, SpongeSprintData.class, ImmutableSprintData.class,
                ImmutableSpongeSprintData.class, new SprintDataProcessor());

        DataUtil.registerDualProcessor(StatisticData.class, SpongeStatisticData.class, ImmutableStatisticData.class,
                ImmutableSpongeStatisticData.class, new StatisticDataProcessor());

        DataUtil.registerDualProcessor(StuckArrowsData.class, SpongeStuckArrowsData.class, ImmutableStuckArrowsData.class,
                ImmutableSpongeStuckArrowsData.class, new StuckArrowsDataProcessor());

        DataUtil.registerDualProcessor(BreedableData.class, SpongeBreedableData.class, ImmutableBreedableData.class,
                ImmutableSpongeBreedableData.class, new BreedableDataProcessor());

        DataUtil.registerDataProcessorAndImpl(JoinData.class, SpongeJoinData.class, ImmutableJoinData.class, ImmutableSpongeJoinData.class,
                new JoinDataProcessor());

        DataUtil.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new EntityPotionDataProcessor());

        DataUtil.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new PotionEntityPotionDataProcessor());

        DataUtil.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new TippedArrowPotionDataProcessor());

        DataUtil.registerDataProcessorAndImpl(BodyPartRotationalData.class, SpongeBodyPartRotationalData.class,
                ImmutableBodyPartRotationalData.class, ImmutableSpongeBodyPartRotationalData.class, new ArmorStandBodyPartRotationalDataProcessor());

        DataUtil.registerDualProcessor(GriefingData.class, SpongeGriefingData.class, ImmutableGriefingData.class,
                ImmutableSpongeGriefingData.class, new GriefingDataProcessor());

        DataUtil.registerDualProcessor(TargetedEntityData.class, SpongeTargetedEntityData.class,
                ImmutableTargetedEntityData.class, ImmutableSpongeTargetedEntityData.class, new EntityTargetedEntityDataProcessor());

        DataUtil.registerDualProcessor(TargetedLocationData.class, SpongeTargetedLocationData.class,
                ImmutableTargetedLocationData.class, ImmutableSpongeTargetedLocationData.class, new EntityTargetedLocationDataProcessor());

        DataUtil.registerDualProcessor(CustomNameVisibleData.class, SpongeCustomNameVisibleData.class, ImmutableCustomNameVisibleData.class,
                ImmutableSpongeCustomNameVisibleData.class, new CustomNameVisibleProcessor());

        DataUtil.registerDualProcessor(GlowingData.class, SpongeGlowingData.class, ImmutableGlowingData.class, ImmutableSpongeGlowingData.class,
                new GlowingDataProcessor());

        DataUtil.registerDualProcessor(GravityData.class, SpongeGravityData.class, ImmutableGravityData.class, ImmutableSpongeGravityData.class,
                new GravityDataProcessor());

        DataUtil.registerDualProcessor(PickupRuleData.class, SpongePickupRuleData.class, ImmutablePickupRuleData.class,
                ImmutableSpongePickupRuleData.class, new PickupRuleDataProcessor());

        DataUtil.registerDualProcessor(ParrotData.class, SpongeParrotData.class, ImmutableParrotData.class,
                ImmutableSpongeParrotData.class, new ParrotDataProcessor());

        DataUtil.registerDataProcessorAndImpl(PickupDelayData.class, SpongePickupDelayData.class, ImmutablePickupDelayData.class,
                ImmutableSpongePickupDelayData.class, new PickupDelayDataProcessor());

        DataUtil.registerDataProcessorAndImpl(DespawnDelayData.class, SpongeDespawnDelayData.class, ImmutableDespawnDelayData.class,
                ImmutableSpongeDespawnDelayData.class, new DespawnDelayDataProcessor());

        DataUtil.registerDataProcessorAndImpl(AreaEffectCloudData.class, SpongeAreaEffectData.class, ImmutableAreaEffectCloudData.class,
                ImmutableSpongeAreaEffectCloudData.class, new AreaEffectCloudDataProcessor());

        DataUtil.registerDualProcessor(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class, ImmutableSpongeDyeableData.class,
                new SheepDyeColorDataProcessor());
        DataUtil.registerDualProcessor(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class, ImmutableSpongeDyeableData.class,
                new ShulkerDyeColorDataProcessor());
        DataUtil.registerDualProcessor(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class, ImmutableSpongeDyeableData.class,
                new WolfDyeColorDataProcessor());

        DataUtil.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class, ImmutableSpongeDirectionalData.class,
                new ShulkerDirectionalDataProcessor());
        DataUtil.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class, ImmutableSpongeDirectionalData.class,
                new ShulkerBulletDirectionalDataProcessor());

        // Item Processors

        DataUtil.registerDualProcessor(FireworkEffectData.class, SpongeFireworkEffectData.class,
                ImmutableFireworkEffectData.class, ImmutableSpongeFireworkEffectData.class, new ItemFireworkEffectDataProcessor());

        DataUtil.registerDualProcessor(FireworkRocketData.class, SpongeFireworkRocketData.class,
                ImmutableFireworkRocketData.class, ImmutableSpongeFireworkRocketData.class, new ItemFireworkRocketDataProcessor());

        DataUtil.registerDualProcessor(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                ImmutableSpongeSkullData.class, new ItemSkullDataProcessor());

        DataUtil.registerDualProcessor(SignData.class, SpongeSignData.class,
                ImmutableSignData.class, ImmutableSpongeSignData.class, new ItemSignDataProcessor());

        DataUtil.registerDualProcessor(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                new ItemWetDataProcessor());

        DataUtil.registerDualProcessor(ColoredData.class, SpongeColoredData.class,
                ImmutableColoredData.class, ImmutableSpongeColoredData.class, new ColoredDataProcessor());

        DataUtil.registerDualProcessor(EnchantmentData.class, SpongeEnchantmentData.class, ImmutableEnchantmentData.class,
                ImmutableSpongeEnchantmentData.class, new ItemEnchantmentDataProcessor());

        DataUtil.registerDualProcessor(LoreData.class, SpongeLoreData.class, ImmutableLoreData.class, ImmutableSpongeLoreData.class,
                new ItemLoreDataProcessor());

        DataUtil.registerDualProcessor(PagedData.class, SpongePagedData.class, ImmutablePagedData.class, ImmutableSpongePagedData.class,
                new ItemPagedDataProcessor());

        DataUtil.registerDualProcessor(PlainPagedData.class, SpongePlainPagedData.class, ImmutablePlainPagedData.class,
                ImmutableSpongePlainPagedData.class, new ItemPlainPagedDataProcessor());

        DataUtil.registerDualProcessor(GoldenAppleData.class, SpongeGoldenAppleData.class, ImmutableGoldenAppleData.class,
                ImmutableSpongeGoldenAppleData.class, new GoldenAppleDataProcessor());

        DataUtil.registerDualProcessor(AuthorData.class, SpongeAuthorData.class, ImmutableAuthorData.class,
                ImmutableSpongeAuthorData.class, new ItemAuthorDataProcessor());

        DataUtil.registerDualProcessor(BreakableData.class, SpongeBreakableData.class, ImmutableBreakableData.class,
                ImmutableSpongeBreakableData.class, new BreakableDataProcessor());

        DataUtil.registerDualProcessor(PlaceableData.class, SpongePlaceableData.class, ImmutablePlaceableData.class,
                ImmutableSpongePlaceableData.class, new PlaceableDataProcessor());

        DataUtil.registerDualProcessor(CoalData.class, SpongeCoalData.class, ImmutableCoalData.class,
                ImmutableSpongeCoalData.class, new CoalDataProcessor());

        DataUtil.registerDualProcessor(CookedFishData.class, SpongeCookedFishData.class, ImmutableCookedFishData.class,
                ImmutableSpongeCookedFishData.class, new CookedFishDataProcessor());

        DataUtil.registerDualProcessor(FishData.class, SpongeFishData.class, ImmutableFishData.class,
                ImmutableSpongeFishData.class, new FishDataProcessor());

        DataUtil.registerDualProcessor(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class, ImmutableRepresentedPlayerData.class,
                ImmutableSpongeRepresentedPlayerData.class, new ItemSkullRepresentedPlayerDataProcessor());

        DataUtil.registerDualProcessor(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new ItemLockableDataProcessor());

        DataUtil.registerDataProcessorAndImpl(DurabilityData.class, SpongeDurabilityData.class, ImmutableDurabilityData.class,
                ImmutableSpongeDurabilityData.class, new DurabilityDataProcessor());

        DataUtil.registerDualProcessor(SpawnableData.class, SpongeSpawnableData.class, ImmutableSpawnableData.class,
                ImmutableSpongeSpawnableData.class, new SpawnableDataProcessor());

        DataUtil.registerDualProcessor(BlockItemData.class, SpongeBlockItemData.class, ImmutableBlockItemData.class,
                ImmutableSpongeBlockItemData.class, new BlockItemDataProcessor());

        DataUtil.registerDualProcessor(GenerationData.class, SpongeGenerationData.class,
                ImmutableGenerationData.class, ImmutableSpongeGenerationData.class, new GenerationDataProcessor());

        DataUtil.registerDualProcessor(StoredEnchantmentData.class, SpongeStoredEnchantmentData.class,
                ImmutableStoredEnchantmentData.class, ImmutableSpongeStoredEnchantmentData.class, new StoredEnchantmentDataProcessor());

        DataUtil.registerDualProcessor(FluidItemData.class, SpongeFluidItemData.class, ImmutableFluidItemData.class,
                ImmutableSpongeFluidItemData.class, new FluidItemDataProcessor());

        DataUtil.registerDualProcessor(PotionColorData.class, SpongePotionColorData.class, ImmutablePotionColorData.class,
                ImmutableSpongePotionColorData.class, new ItemPotionColorDataProcessor());

        DataUtil.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new ItemPotionEffectDataProcessor());

        DataUtil.registerDualProcessor(PotionTypeData.class, SpongePotionTypeData.class, ImmutablePotionTypeData.class,
                ImmutableSpongePotionTypeData.class, new ItemPotionTypeDataProcessor());

        DataUtil.registerDataProcessorAndImpl(HideData.class, SpongeHideData.class, ImmutableHideData.class, ImmutableSpongeHideData.class,
                new HideDataProcessor());

        DataUtil.registerDualProcessor(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class, ImmutableSpongeDyeableData.class, new
                ItemDyeColorDataProcessor());

        // Block Processors

        DataUtil.registerDualProcessor(DirtData.class, SpongeDirtData.class, ImmutableDirtData.class,
                ImmutableSpongeDirtData.class, new DirtDataProcessor());

        DataUtil.registerDualProcessor(StoneData.class, SpongeStoneData.class, ImmutableStoneData.class,
                ImmutableSpongeStoneData.class, new StoneDataProcessor());

        DataUtil.registerDualProcessor(PrismarineData.class, SpongePrismarineData.class, ImmutablePrismarineData.class,
                ImmutableSpongePrismarineData.class, new PrismarineDataProcessor());

        DataUtil.registerDualProcessor(BrickData.class, SpongeBrickData.class, ImmutableBrickData.class,
                ImmutableSpongeBrickData.class, new BrickDataProcessor());

        DataUtil.registerDualProcessor(QuartzData.class, SpongeQuartzData.class, ImmutableQuartzData.class,
                ImmutableSpongeQuartzData.class, new QuartzDataProcessor());

        DataUtil.registerDualProcessor(SandData.class, SpongeSandData.class, ImmutableSandData.class,
                ImmutableSpongeSandData.class, new SandDataProcessor());

        DataUtil.registerDualProcessor(SlabData.class, SpongeSlabData.class, ImmutableSlabData.class,
                ImmutableSpongeSlabData.class, new SlabDataProcessor());

        DataUtil.registerDualProcessor(SandstoneData.class, SpongeSandstoneData.class, ImmutableSandstoneData.class,
                ImmutableSpongeSandstoneData.class, new SandstoneDataProcessor());

        DataUtil.registerDualProcessor(ComparatorData.class, SpongeComparatorData.class, ImmutableComparatorData.class,
                ImmutableSpongeComparatorData.class, new ComparatorDataProcessor());

        DataUtil.registerDualProcessor(TreeData.class, SpongeTreeData.class, ImmutableTreeData.class,
                ImmutableSpongeTreeData.class, new TreeDataProcessor());

        DataUtil.registerDualProcessor(DisguisedBlockData.class, SpongeDisguisedBlockData.class, ImmutableDisguisedBlockData.class,
                ImmutableSpongeDisguisedBlockData.class, new DisguisedBlockDataProcessor());

        DataUtil.registerDualProcessor(HingeData.class, SpongeHingeData.class, ImmutableHingeData.class,
                ImmutableSpongeHingeData.class, new HingeDataProcessor());

        DataUtil.registerDualProcessor(PistonData.class, SpongePistonData.class, ImmutablePistonData.class,
                ImmutableSpongePistonData.class, new PistonDataProcessor());

        DataUtil.registerDualProcessor(PortionData.class, SpongePortionData.class, ImmutablePortionData.class,
                ImmutableSpongePortionData.class, new PortionDataProcessor());

        DataUtil.registerDualProcessor(RailDirectionData.class, SpongeRailDirectionData.class, ImmutableRailDirectionData.class,
                ImmutableSpongeRailDirectionData.class, new RailDirectionDataProcessor());

        DataUtil.registerDualProcessor(StairShapeData.class, SpongeStairShapeData.class, ImmutableStairShapeData.class,
                ImmutableSpongeStairShapeData.class, new StairShapeDataProcessor());

        DataUtil.registerDualProcessor(WallData.class, SpongeWallData.class, ImmutableWallData.class,
                ImmutableSpongeWallData.class, new WallDataProcessor());

        DataUtil.registerDualProcessor(ShrubData.class, SpongeShrubData.class, ImmutableShrubData.class,
                ImmutableSpongeShrubData.class, new ShrubDataProcessor());

        DataUtil.registerDualProcessor(PlantData.class, SpongePlantData.class, ImmutablePlantData.class,
                ImmutableSpongePlantData.class, new PlantDataProcessor());

        DataUtil.registerDualProcessor(DoublePlantData.class, SpongeDoublePlantData.class, ImmutableDoublePlantData.class,
                ImmutableSpongeDoublePlantData.class, new DoublePlantDataProcessor());

        DataUtil.registerDualProcessor(BigMushroomData.class, SpongeBigMushroomData.class, ImmutableBigMushroomData.class,
                ImmutableSpongeBigMushroomData.class, new BigMushroomDataProcessor());

        DataUtil.registerDualProcessor(AttachedData.class, SpongeAttachedData.class, ImmutableAttachedData.class,
                ImmutableSpongeAttachedData.class, new AttachedDataProcessor());

        DataUtil.registerDataProcessorAndImpl(ConnectedDirectionData.class, SpongeConnectedDirectionData.class,
                ImmutableConnectedDirectionData.class, ImmutableSpongeConnectedDirectionData.class, new ConnectedDirectionDataProcessor());

        DataUtil.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class,
                ImmutableSpongeDirectionalData.class, new DirectionalDataProcessor());

        DataUtil.registerDualProcessor(DisarmedData.class, SpongeDisarmedData.class, ImmutableDisarmedData.class,
                ImmutableSpongeDisarmedData.class, new DisarmedDataProcessor());

        DataUtil.registerDualProcessor(DropData.class, SpongeDropData.class, ImmutableDropData.class,
                ImmutableSpongeDropData.class, new DropDataProcessor());

        DataUtil.registerDualProcessor(ExtendedData.class, SpongeExtendedData.class, ImmutableExtendedData.class,
                ImmutableSpongeExtendedData.class, new ExtendedDataProcessor());

        DataUtil.registerDualProcessor(GrowthData.class, SpongeGrowthData.class, ImmutableGrowthData.class,
                ImmutableSpongeGrowthData.class, new GrowthDataProcessor());

        DataUtil.registerDualProcessor(OpenData.class, SpongeOpenData.class, ImmutableOpenData.class,
                ImmutableSpongeOpenData.class, new OpenDataProcessor());

        DataUtil.registerDualProcessor(PoweredData.class, SpongePoweredData.class, ImmutablePoweredData.class,
                ImmutableSpongePoweredData.class, new PoweredDataProcessor());

        DataUtil.registerDualProcessor(RedstonePoweredData.class, SpongeRedstonePoweredData.class, ImmutableRedstonePoweredData.class,
                ImmutableSpongeRedstonePoweredData.class, new RedstonePoweredDataProcessor());

        DataUtil.registerDualProcessor(SeamlessData.class, SpongeSeamlessData.class, ImmutableSeamlessData.class,
                ImmutableSpongeSeamlessData.class, new SeamlessDataProcessor());

        DataUtil.registerDualProcessor(SnowedData.class, SpongeSnowedData.class, ImmutableSnowedData.class,
                ImmutableSpongeSnowedData.class, new SnowedDataProcessor());

        DataUtil.registerDualProcessor(OccupiedData.class, SpongeOccupiedData.class, ImmutableOccupiedData.class,
                ImmutableSpongeOccupiedData.class, new OccupiedDataProcessor());

        DataUtil.registerDualProcessor(InWallData.class, SpongeInWallData.class, ImmutableInWallData.class,
                ImmutableSpongeInWallData.class, new InWallDataProcessor());

        DataUtil.registerDualProcessor(LayeredData.class, SpongeLayeredData.class, ImmutableLayeredData.class,
                ImmutableSpongeLayeredData.class, new LayeredDataProcessor());

        DataUtil.registerDualProcessor(DecayableData.class, SpongeDecayableData.class, ImmutableDecayableData.class,
                ImmutableSpongeDecayableData.class, new DecayableDataProcessor());

        DataUtil.registerDualProcessor(AxisData.class, SpongeAxisData.class, ImmutableAxisData.class,
                ImmutableSpongeAxisData.class, new AxisDataProcessor());

        DataUtil.registerDualProcessor(DelayableData.class, SpongeDelayableData.class, ImmutableDelayableData.class,
                ImmutableSpongeDelayableData.class, new DelayableDataProcessor());

        DataUtil.registerDualProcessor(MoistureData.class, SpongeMoistureData.class, ImmutableMoistureData.class,
                ImmutableSpongeMoistureData.class, new MoistureDataProcessor());

        DataUtil.registerDataProcessorAndImpl(WireAttachmentData.class, SpongeWireAttachmentData.class, ImmutableWireAttachmentData.class,
                ImmutableSpongeWireAttachmentData.class, new WireAttachmentDataProcessor());

        // TileEntity Processors

        DataUtil.registerDualProcessor(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                ImmutableSpongeSkullData.class, new TileEntitySkullDataProcessor());

        DataUtil.registerDualProcessor(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class, ImmutableRepresentedPlayerData.class,
                ImmutableSpongeRepresentedPlayerData.class, new SkullRepresentedPlayerDataProcessor());

        DataUtil.registerDualProcessor(SignData.class, SpongeSignData.class,
                ImmutableSignData.class, ImmutableSpongeSignData.class, new TileEntitySignDataProcessor());

        DataUtil.registerDataProcessorAndImpl(FurnaceData.class, SpongeFurnaceData.class,
                ImmutableFurnaceData.class, ImmutableSpongeFurnaceData.class, new FurnaceDataProcessor());

        DataUtil.registerDualProcessor(BrewingStandData.class, SpongeBrewingStandData.class, ImmutableBrewingStandData.class,
                ImmutableSpongeBrewingStandData.class, new BrewingStandDataProcessor());

        DataUtil.registerDataProcessorAndImpl(ConnectedDirectionData.class, SpongeConnectedDirectionData.class,
                ImmutableConnectedDirectionData.class, ImmutableSpongeConnectedDirectionData.class, new TileConnectedDirectionDataProcessor());

        DataUtil.registerDualProcessor(CooldownData.class, SpongeCooldownData.class, ImmutableCooldownData.class,
                ImmutableSpongeCooldownData.class, new CooldownDataProcessor());

        DataUtil.registerDualProcessor(NoteData.class, SpongeNoteData.class, ImmutableNoteData.class,
                ImmutableSpongeNoteData.class, new NoteDataProcessor());

        DataUtil.registerDualProcessor(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new TileEntityLockableDataProcessor());

        DataUtil.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new JukeboxDataProcessor());

        DataUtil.registerDualProcessor(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, new FlowerPotDataProcessor());

        DataUtil.registerDataProcessorAndImpl(BannerData.class, SpongeBannerData.class, ImmutableBannerData.class,
                ImmutableSpongeBannerData.class, new TileEntityBannerDataProcessor());

        DataUtil.registerDataProcessorAndImpl(BannerData.class, SpongeBannerData.class, ImmutableBannerData.class,
                ImmutableSpongeBannerData.class, new ShieldBannerDataProcessor());

        DataUtil.registerDataProcessorAndImpl(CommandData.class, SpongeCommandData.class, ImmutableCommandData.class,
                ImmutableSpongeCommandData.class, new TileEntityCommandDataProcessor());

        DataUtil.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class,
                ImmutableSpongeDirectionalData.class, new SkullRotationDataProcessor());

        DataUtil.registerDualProcessor(DirectionalData.class, SpongeDirectionalData.class,
            ImmutableDirectionalData.class, ImmutableSpongeDirectionalData.class, new HangingDataProcessor());

        DataUtil.registerDataProcessorAndImpl(BeaconData.class, SpongeBeaconData.class,
                ImmutableBeaconData.class, ImmutableSpongeBeaconData.class, new BeaconDataProcessor());

        DataUtil.registerDualProcessor(DominantHandData.class, SpongeDominantHandData.class, ImmutableDominantHandData.class, ImmutableSpongeDominantHandData.class,
                new DominantHandDataProcessor());

        DataUtil.registerDataProcessorAndImpl(EndGatewayData.class, SpongeEndGatewayData.class,
            ImmutableEndGatewayData.class, ImmutableSpongeEndGatewayData.class, new EndGatewayDataProcessor());

        DataUtil.registerDataProcessorAndImpl(StructureData.class, SpongeStructureData.class,
            ImmutableStructureData.class, ImmutableSpongeStructureData.class, new StructureDataProcessor());

        DataUtil.registerDataProcessorAndImpl(MobSpawnerData.class, SpongeMobSpawnerData.class, ImmutableMobSpawnerData.class,
                ImmutableSpongeMobSpawnerData.class, new MobSpawnerDataProcessor());

        DataUtil.registerDualProcessor(HealthScalingData.class, SpongeHealthScaleData.class, ImmutableHealthScalingData.class, ImmutableSpongeHealthScalingData.class,
                new HealthScalingProcessor());

        DataUtil.registerDualProcessor(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class, ImmutableSpongeDyeableData.class,
                new BedDyeColorDataProcessor());

        DataUtil.registerDataProcessorAndImpl(DisabledSlotsData.class, SpongeDisabledSlotsData.class,
                ImmutableDisabledSlotsData.class, ImmutableSpongeDisabledSlotsData.class, new DisabledSlotsDataProcessor());

        // Values

        DataUtil.registerValueProcessor(Keys.FUSE_DURATION, new FuseDurationValueProcessor());
        DataUtil.registerValueProcessor(Keys.TICKS_REMAINING, new TicksRemainingValueProcessor());
        DataUtil.registerValueProcessor(Keys.HEALTH, new HealthValueProcessor());
        DataUtil.registerValueProcessor(Keys.MAX_HEALTH, new MaxHealthValueProcessor());
        DataUtil.registerValueProcessor(Keys.FIRE_TICKS, new FireTicksValueProcessor());
        DataUtil.registerValueProcessor(Keys.FIRE_DAMAGE_DELAY, new FireDamageDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.DISPLAY_NAME, new ItemDisplayNameValueProcessor());
        DataUtil.registerValueProcessor(Keys.DISPLAY_NAME, new TileEntityDisplayNameValueProcessor());
        DataUtil.registerValueProcessor(Keys.DISPLAY_NAME, new EntityDisplayNameValueProcessor());
        DataUtil.registerValueProcessor(Keys.FOOD_LEVEL, new FoodLevelValueProcessor());
        DataUtil.registerValueProcessor(Keys.SATURATION, new FoodSaturationValueProcessor());
        DataUtil.registerValueProcessor(Keys.EXHAUSTION, new FoodExhaustionValueProcessor());
        DataUtil.registerValueProcessor(Keys.MAX_AIR, new MaxAirValueProcessor());
        DataUtil.registerValueProcessor(Keys.REMAINING_AIR, new RemainingAirValueProcessor());
        DataUtil.registerValueProcessor(Keys.HORSE_COLOR, new HorseColorValueProcessor());
        DataUtil.registerValueProcessor(Keys.HORSE_STYLE, new HorseStyleValueProcessor());
        DataUtil.registerValueProcessor(Keys.EXPERIENCE_LEVEL, new ExperienceLevelValueProcessor());
        DataUtil.registerValueProcessor(Keys.TOTAL_EXPERIENCE, new TotalExperienceValueProcessor());
        DataUtil.registerValueProcessor(Keys.EXPERIENCE_SINCE_LEVEL, new ExperienceSinceLevelValueProcessor());
        DataUtil.registerValueProcessor(Keys.EXPERIENCE_FROM_START_OF_LEVEL, new ExperienceFromStartOfLevelValueProcessor());
        DataUtil.registerValueProcessor(Keys.WALKING_SPEED, new WalkingSpeedValueProcessor());
        DataUtil.registerValueProcessor(Keys.FLYING_SPEED, new FlyingSpeedValueProcessor());
        DataUtil.registerValueProcessor(Keys.PASSED_BURN_TIME, new PassedBurnTimeValueProcessor());
        DataUtil.registerValueProcessor(Keys.MAX_BURN_TIME, new MaxBurnTimeValueProcessor());
        DataUtil.registerValueProcessor(Keys.PASSED_COOK_TIME, new PassedCookTimeValueProcessor());
        DataUtil.registerValueProcessor(Keys.MAX_COOK_TIME, new MaxCookTimeValueProcessor());
        DataUtil.registerValueProcessor(Keys.UNBREAKABLE, new UnbreakableValueProcessor());
        DataUtil.registerValueProcessor(Keys.ITEM_DURABILITY, new ItemDurabilityValueProcessor());
        DataUtil.registerValueProcessor(Keys.VEHICLE, new VehicleValueProcessor());
        DataUtil.registerValueProcessor(Keys.BASE_VEHICLE, new BaseVehicleValueProcessor());
        DataUtil.registerValueProcessor(Keys.REPRESENTED_BLOCK, new RepresentedBlockValueProcessor());
        DataUtil.registerValueProcessor(Keys.OFFSET, new OffsetValueProcessor());
        DataUtil.registerValueProcessor(Keys.FALL_DAMAGE_PER_BLOCK, new FallHurtAmountValueProcessor());
        DataUtil.registerValueProcessor(Keys.MAX_FALL_DAMAGE, new MaxFallDamageValueProcessor());
        DataUtil.registerValueProcessor(Keys.FALLING_BLOCK_STATE, new FallingBlockStateValueProcessor());
        DataUtil.registerValueProcessor(Keys.CAN_PLACE_AS_BLOCK, new CanPlaceAsBlockValueProcessor());
        DataUtil.registerValueProcessor(Keys.CAN_DROP_AS_ITEM, new CanDropAsItemValueProcessor());
        DataUtil.registerValueProcessor(Keys.FALL_TIME, new FallTimeValueProcessor());
        DataUtil.registerValueProcessor(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, new FallingBlockCanHurtEntitiesValueProcessor());
        DataUtil.registerValueProcessor(Keys.CONNECTED_DIRECTIONS, new ConnectedDirectionsValueProcessor());
        DataUtil.registerValueProcessor(Keys.CONNECTED_EAST, new ConnectedEastValueProcessor());
        DataUtil.registerValueProcessor(Keys.CONNECTED_NORTH, new ConnectedNorthValueProcessor());
        DataUtil.registerValueProcessor(Keys.CONNECTED_SOUTH, new ConnectedSouthValueProcessor());
        DataUtil.registerValueProcessor(Keys.CONNECTED_WEST, new ConnectedWestValueProcessor());
        DataUtil.registerValueProcessor(Keys.BANNER_BASE_COLOR, new TileBannerBaseColorValueProcessor());
        DataUtil.registerValueProcessor(Keys.BANNER_PATTERNS, new TileBannerPatternLayersValueProcessor());
        DataUtil.registerValueProcessor(Keys.LAST_COMMAND_OUTPUT, new EntityLastCommandOutputValueProcessor());
        DataUtil.registerValueProcessor(Keys.LAST_COMMAND_OUTPUT, new TileEntityLastCommandOutputValueProcessor());
        DataUtil.registerValueProcessor(Keys.COMMAND, new EntityCommandValueProcessor());
        DataUtil.registerValueProcessor(Keys.COMMAND, new TileEntityCommandValueProcessor());
        DataUtil.registerValueProcessor(Keys.SUCCESS_COUNT, new EntitySuccessCountValueProcessor());
        DataUtil.registerValueProcessor(Keys.SUCCESS_COUNT, new TileEntitySuccessCountValueProcessor());
        DataUtil.registerValueProcessor(Keys.TRACKS_OUTPUT, new EntityTracksOutputValueProcessor());
        DataUtil.registerValueProcessor(Keys.TRACKS_OUTPUT, new TileEntityTracksOutputValueProcessor());
        DataUtil.registerValueProcessor(Keys.INVISIBLE, new InvisibilityValueProcessor());
        DataUtil.registerValueProcessor(Keys.VANISH, new VanishValueProcessor());
        DataUtil.registerValueProcessor(Keys.VANISH_IGNORES_COLLISION, new VanishCollisionValueProcessor());
        DataUtil.registerValueProcessor(Keys.VANISH_PREVENTS_TARGETING, new VanishTargetValueProcessor());
        DataUtil.registerValueProcessor(Keys.FIRST_DATE_PLAYED, new FirstJoinValueProcessor());
        DataUtil.registerValueProcessor(Keys.LAST_DATE_PLAYED, new LastPlayedValueProcessor());
        DataUtil.registerValueProcessor(Keys.HIDE_ENCHANTMENTS, new HideEnchantmentsValueProcessor());
        DataUtil.registerValueProcessor(Keys.HIDE_ATTRIBUTES, new HideAttributesValueProcessor());
        DataUtil.registerValueProcessor(Keys.HIDE_UNBREAKABLE, new HideUnbreakableValueProcessor());
        DataUtil.registerValueProcessor(Keys.HIDE_CAN_DESTROY, new HideCanDestroyValueProcessor());
        DataUtil.registerValueProcessor(Keys.HIDE_CAN_PLACE, new HideCanPlaceValueProcessor());
        DataUtil.registerValueProcessor(Keys.HIDE_MISCELLANEOUS, new HideMiscellaneousValueProcessor());
        DataUtil.registerValueProcessor(Keys.BODY_ROTATIONS, new BodyRotationsValueProcessor());
        DataUtil.registerValueProcessor(Keys.HEAD_ROTATION, new HeadRotationValueProcessor());
        DataUtil.registerValueProcessor(Keys.CHEST_ROTATION, new ChestRotationValueProcessor());
        DataUtil.registerValueProcessor(Keys.LEFT_ARM_ROTATION, new LeftArmRotationValueProcessor());
        DataUtil.registerValueProcessor(Keys.RIGHT_ARM_ROTATION, new RightArmRotationValueProcessor());
        DataUtil.registerValueProcessor(Keys.LEFT_LEG_ROTATION, new LeftLegRotationValueProcessor());
        DataUtil.registerValueProcessor(Keys.RIGHT_LEG_ROTATION, new RightLegRotationValueProcessor());
        DataUtil.registerValueProcessor(Keys.BEACON_PRIMARY_EFFECT, new BeaconPrimaryEffectValueProcessor());
        DataUtil.registerValueProcessor(Keys.BEACON_SECONDARY_EFFECT, new BeaconSecondaryEffectValueProcessor());
        DataUtil.registerValueProcessor(Keys.ARMOR_STAND_HAS_BASE_PLATE, new ArmorStandBasePlateValueProcessor());
        DataUtil.registerValueProcessor(Keys.ARMOR_STAND_MARKER, new ArmorStandMarkerValueProcessor());
        DataUtil.registerValueProcessor(Keys.ARMOR_STAND_IS_SMALL, new ArmorStandSmallValueProcessor());
        DataUtil.registerValueProcessor(Keys.ARMOR_STAND_HAS_ARMS, new ArmorStandArmsValueProcessor());
        DataUtil.registerValueProcessor(Keys.PICKUP_DELAY, new PickupDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.INFINITE_PICKUP_DELAY, new InfinitePickupDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.DESPAWN_DELAY, new DespawnDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.INFINITE_DESPAWN_DELAY, new InfiniteDespawnDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_REMAINING_DELAY, new SpawnerRemainingDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_MINIMUM_DELAY, new SpawnerMinimumDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_MAXIMUM_DELAY, new SpawnerMaximumDelayValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_SPAWN_COUNT, new SpawnerSpawnCountValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES, new SpawnerMaximumNearbyEntitiesValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_REQUIRED_PLAYER_RANGE, new SpawnerRequiredPlayerRangeValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_SPAWN_RANGE, new SpawnerSpawnRangeValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN, new SpawnerNextEntityToSpawnValueProcessor());
        DataUtil.registerValueProcessor(Keys.SPAWNER_ENTITIES, new SpawnerEntitiesValueProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_COLOR, new AreaEffectCloudColorProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_AGE, new AreaEffectCloudAgeProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_PARTICLE_TYPE, new AreaEffectCloudParticleTypeProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_DURATION, new AreaEffectCloudDurationProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE, new AreaEffectCloudDurationOnUseProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_RADIUS, new AreaEffectCloudRadiusProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE, new AreaEffectCloudRadiusOnUseProcessor());
        DataUtil.registerValueProcessor(Keys.AREA_EFFECT_CLOUD_WAIT_TIME, new AreaEffectCloudWaitTimeProcessor());
        DataUtil.registerValueProcessor(Keys.POTION_EFFECTS, new AreaEffectCloudPotionEffectsProcessor());
        DataUtil.registerValueProcessor(Keys.IS_ADULT, new IsAdultValueProcessor());
        DataUtil.registerValueProcessor(Keys.IS_ADULT, new IsAdultZombieValueProcessor());
        DataUtil.registerValueProcessor(Keys.AGE, new AgeableAgeValueProcessor());
        DataUtil.registerValueProcessor(Keys.INVULNERABILITY_TICKS, new InvulnerabilityTicksValueProcessor());
        DataUtil.registerValueProcessor(Keys.INVULNERABLE, new InvulnerableValueProcessor());
        DataUtil.registerValueProcessor(Keys.LAST_ATTACKER, new LastAttackerValueProcessor());
        DataUtil.registerValueProcessor(Keys.LAST_DAMAGE, new LastDamageValueProcessor());
        DataUtil.registerValueProcessor(Keys.END_GATEWAY_TELEPORT_COOLDOWN, new EndGatewayTeleportCooldownValueProcessor());
        DataUtil.registerValueProcessor(Keys.END_GATEWAY_AGE, new EndGatewayAgeValueProcessor());
        DataUtil.registerValueProcessor(Keys.EXIT_POSITION, new EndGatewayExitPositionValueProcessor());
        DataUtil.registerValueProcessor(Keys.EXACT_TELEPORT, new EndGatewayExactTeleportValueProcessor());
        DataUtil.registerValueProcessor(Keys.ARMOR_STAND_TAKING_DISABLED, new TakingDisabledValueProcessor());
        DataUtil.registerValueProcessor(Keys.ARMOR_STAND_PLACING_DISABLED, new PlacingDisabledValueProcessor());
        
        // Properties
        final PropertyRegistry propertyRegistry = Sponge.getPropertyRegistry();

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
        propertyRegistry.register(InstrumentProperty.class, new InstrumentPropertyStore());

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
        propertyRegistry.register(RecordProperty.class, new RecordPropertyStore());

        // Entities
        propertyRegistry.register(EyeLocationProperty.class, new EyeLocationPropertyStore());
        propertyRegistry.register(EyeHeightProperty.class, new EyeHeightPropertyStore());
        propertyRegistry.register(DominantHandProperty.class, new DominantHandPropertyStore());
    }

}
