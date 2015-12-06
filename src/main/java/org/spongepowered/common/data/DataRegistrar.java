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
import org.spongepowered.api.data.manipulator.immutable.ImmutableColoredData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkEffectData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkRocketData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedPlayerData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkullData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAttachedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableAxisData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableBigMushroomData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableBrickData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableComparatorData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDecayableData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDelayableData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirtData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDisarmedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDisguisedBlockData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDoublePlantData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDropData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableExtendedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableGrowthData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableHingeData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableInWallData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableLayeredData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableOccupiedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableOpenData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePistonData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePlantData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePortionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePoweredData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePrismarineData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableQuartzData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRailDirectionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRedstonePoweredData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSandData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSandstoneData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSeamlessData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableShrubData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSlabData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSnowedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableStairShapeData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableStoneData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSuspendedData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableWallData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgentData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableArtData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableChargedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableElderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallDistanceData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingAbilityData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGameModeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMinecartBlockData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePigSaddleData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableScreamingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableShearedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSittingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSlimeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetLivingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVillagerZombieData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableAuthorData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableBlockItemData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableBreakableData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableCoalData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableCookedFishData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableDurabilityData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableFishData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePlaceableData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableCooldownData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableFurnaceData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.data.manipulator.mutable.FireworkRocketData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.manipulator.mutable.WetData;
import org.spongepowered.api.data.manipulator.mutable.block.AttachedData;
import org.spongepowered.api.data.manipulator.mutable.block.AxisData;
import org.spongepowered.api.data.manipulator.mutable.block.BigMushroomData;
import org.spongepowered.api.data.manipulator.mutable.block.BrickData;
import org.spongepowered.api.data.manipulator.mutable.block.ComparatorData;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.DecayableData;
import org.spongepowered.api.data.manipulator.mutable.block.DelayableData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.manipulator.mutable.block.DirtData;
import org.spongepowered.api.data.manipulator.mutable.block.DisarmedData;
import org.spongepowered.api.data.manipulator.mutable.block.DisguisedBlockData;
import org.spongepowered.api.data.manipulator.mutable.block.DoublePlantData;
import org.spongepowered.api.data.manipulator.mutable.block.DropData;
import org.spongepowered.api.data.manipulator.mutable.block.ExtendedData;
import org.spongepowered.api.data.manipulator.mutable.block.GrowthData;
import org.spongepowered.api.data.manipulator.mutable.block.HingeData;
import org.spongepowered.api.data.manipulator.mutable.block.InWallData;
import org.spongepowered.api.data.manipulator.mutable.block.LayeredData;
import org.spongepowered.api.data.manipulator.mutable.block.OccupiedData;
import org.spongepowered.api.data.manipulator.mutable.block.OpenData;
import org.spongepowered.api.data.manipulator.mutable.block.PistonData;
import org.spongepowered.api.data.manipulator.mutable.block.PlantData;
import org.spongepowered.api.data.manipulator.mutable.block.PortionData;
import org.spongepowered.api.data.manipulator.mutable.block.PoweredData;
import org.spongepowered.api.data.manipulator.mutable.block.PrismarineData;
import org.spongepowered.api.data.manipulator.mutable.block.QuartzData;
import org.spongepowered.api.data.manipulator.mutable.block.RailDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.RedstonePoweredData;
import org.spongepowered.api.data.manipulator.mutable.block.SandData;
import org.spongepowered.api.data.manipulator.mutable.block.SandstoneData;
import org.spongepowered.api.data.manipulator.mutable.block.SeamlessData;
import org.spongepowered.api.data.manipulator.mutable.block.ShrubData;
import org.spongepowered.api.data.manipulator.mutable.block.SlabData;
import org.spongepowered.api.data.manipulator.mutable.block.SnowedData;
import org.spongepowered.api.data.manipulator.mutable.block.StairShapeData;
import org.spongepowered.api.data.manipulator.mutable.block.StoneData;
import org.spongepowered.api.data.manipulator.mutable.block.SuspendedData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.manipulator.mutable.block.WallData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.data.manipulator.mutable.entity.ArtData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.ChargedData;
import org.spongepowered.api.data.manipulator.mutable.entity.ElderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallDistanceData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingAbilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.MinecartBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.PigSaddleData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ScreamingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ShearedData;
import org.spongepowered.api.data.manipulator.mutable.entity.SittingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SlimeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetLivingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VillagerZombieData;
import org.spongepowered.api.data.manipulator.mutable.item.AuthorData;
import org.spongepowered.api.data.manipulator.mutable.item.BlockItemData;
import org.spongepowered.api.data.manipulator.mutable.item.BreakableData;
import org.spongepowered.api.data.manipulator.mutable.item.CoalData;
import org.spongepowered.api.data.manipulator.mutable.item.CookedFishData;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.FishData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PlaceableData;
import org.spongepowered.api.data.manipulator.mutable.item.SpawnableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.NoteData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.data.property.block.BlastResistanceProperty;
import org.spongepowered.api.data.property.block.FlammableProperty;
import org.spongepowered.api.data.property.block.GravityAffectedProperty;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.HardnessProperty;
import org.spongepowered.api.data.property.block.HeldItemProperty;
import org.spongepowered.api.data.property.block.IndirectlyPoweredProperty;
import org.spongepowered.api.data.property.block.LightEmissionProperty;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.data.property.block.ReplaceableProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.data.property.block.SolidCubeProperty;
import org.spongepowered.api.data.property.block.StatisticsTrackedProperty;
import org.spongepowered.api.data.property.block.TemperatureProperty;
import org.spongepowered.api.data.property.block.UnbreakableProperty;
import org.spongepowered.api.data.property.entity.EyeHeightProperty;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.data.property.item.ApplicableEffectProperty;
import org.spongepowered.api.data.property.item.BurningFuelProperty;
import org.spongepowered.api.data.property.item.DamageAbsorptionProperty;
import org.spongepowered.api.data.property.item.EfficiencyProperty;
import org.spongepowered.api.data.property.item.EquipmentProperty;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.api.data.property.item.SaturationProperty;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
import org.spongepowered.common.data.builder.SpongeFireworkEffectDataBuilder;
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
import org.spongepowered.common.data.processor.dual.entity.*;
import org.spongepowered.common.data.processor.value.*;
import org.spongepowered.common.data.processor.value.block.*;
import org.spongepowered.common.data.processor.value.entity.*;
import org.spongepowered.common.data.processor.value.item.*;
import org.spongepowered.common.data.processor.value.tileentity.*;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.property.store.block.BlastResistancePropertyStore;
import org.spongepowered.common.data.property.store.block.FlammablePropertyStore;
import org.spongepowered.common.data.property.store.block.GravityAffectedPropertyStore;
import org.spongepowered.common.data.property.store.block.GroundLuminancePropertyStore;
import org.spongepowered.common.data.property.store.block.HardnessPropertyStore;
import org.spongepowered.common.data.property.store.block.HeldItemPropertyStore;
import org.spongepowered.common.data.property.store.block.IndirectlyPoweredPropertyStore;
import org.spongepowered.common.data.property.store.block.LightEmissionPropertyStore;
import org.spongepowered.common.data.property.store.block.MatterPropertyStore;
import org.spongepowered.common.data.property.store.block.PassablePropertyStore;
import org.spongepowered.common.data.property.store.block.PoweredPropertyStore;
import org.spongepowered.common.data.property.store.block.ReplaceablePropertyStore;
import org.spongepowered.common.data.property.store.block.SkyLuminancePropertyStore;
import org.spongepowered.common.data.property.store.block.SolidCubePropertyStore;
import org.spongepowered.common.data.property.store.block.StatisticsTrackedPropertyStore;
import org.spongepowered.common.data.property.store.block.TemperaturePropertyStore;
import org.spongepowered.common.data.property.store.block.UnbreakablePropertyStore;
import org.spongepowered.common.data.property.store.entity.EyeHeightPropertyStore;
import org.spongepowered.common.data.property.store.entity.EyeLocationPropertyStore;
import org.spongepowered.common.data.property.store.item.ApplicableEffectPropertyStore;
import org.spongepowered.common.data.property.store.item.BurningFuelPropertyStore;
import org.spongepowered.common.data.property.store.item.DamageAbsorptionPropertyStore;
import org.spongepowered.common.data.property.store.item.EfficiencyPropertyStore;
import org.spongepowered.common.data.property.store.item.EquipmentPropertyStore;
import org.spongepowered.common.data.property.store.item.FoodRestorationPropertyStore;
import org.spongepowered.common.data.property.store.item.HarvestingPropertyStore;
import org.spongepowered.common.data.property.store.item.SaturationPropertyStore;
import org.spongepowered.common.data.property.store.item.UseLimitPropertyStore;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

public class DataRegistrar {

    public static void setupSerialization(Game game) {
        KeyRegistry.registerKeys();
        SpongeDataManager dataManager = SpongeDataManager.getInstance();
        // TileEntities
        dataManager.registerBuilder(Banner.class, new SpongeBannerBuilder(game));
        dataManager.registerBuilder(PatternLayer.class, new SpongePatternLayerBuilder());
        dataManager.registerBuilder(BrewingStand.class, new SpongeBrewingStandBuilder(game));
        dataManager.registerBuilder(Chest.class, new SpongeChestBuilder(game));
        dataManager.registerBuilder(CommandBlock.class, new SpongeCommandBlockBuilder(game));
        dataManager.registerBuilder(Comparator.class, new SpongeComparatorBuilder(game));
        dataManager.registerBuilder(DaylightDetector.class, new SpongeDaylightBuilder(game));
        dataManager.registerBuilder(Dispenser.class, new SpongeDispenserBuilder(game));
        dataManager.registerBuilder(Dropper.class, new SpongeDropperBuilder(game));
        dataManager.registerBuilder(EnchantmentTable.class, new SpongeEnchantmentTableBuilder(game));
        dataManager.registerBuilder(EnderChest.class, new SpongeEnderChestBuilder(game));
        dataManager.registerBuilder(EndPortal.class, new SpongeEndPortalBuilder(game));
        dataManager.registerBuilder(Furnace.class, new SpongeFurnaceBuilder(game));
        dataManager.registerBuilder(Hopper.class, new SpongeHopperBuilder(game));
        dataManager.registerBuilder(MobSpawner.class, new SpongeMobSpawnerBuilder(game));
        dataManager.registerBuilder(Note.class, new SpongeNoteBuilder(game));
        dataManager.registerBuilder(Sign.class, new SpongeSignBuilder(game));
        dataManager.registerBuilder(Skull.class, new SpongeSkullBuilder(game));

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

        // Data Manipulators

        final HealthDataProcessor healthProcessor = new HealthDataProcessor();
        dataManager.registerDataProcessorAndImpl(HealthData.class, SpongeHealthData.class, ImmutableHealthData.class,
                ImmutableSpongeHealthData.class, healthProcessor);

        final IgniteableDataProcessor igniteableProcessor = new IgniteableDataProcessor();
        dataManager.registerDataProcessorAndImpl(IgniteableData.class, SpongeIgniteableData.class, ImmutableIgniteableData.class,
                ImmutableSpongeIgniteableData.class, igniteableProcessor);

        final DisplayNameDataProcessor displayNameDataProcessor = new DisplayNameDataProcessor();
        dataManager.registerDataProcessorAndImpl(DisplayNameData.class, SpongeDisplayNameData.class,
                ImmutableDisplayNameData.class, ImmutableSpongeDisplayNameData.class, displayNameDataProcessor);

        final ColoredDataProcessor coloredDataProcessor = new ColoredDataProcessor();
        dataManager.registerDataProcessorAndImpl(ColoredData.class, SpongeColoredData.class,
                ImmutableColoredData.class, ImmutableSpongeColoredData.class, coloredDataProcessor);

        final SignDataProcessor signDataProcessor = new SignDataProcessor();
        dataManager.registerDataProcessorAndImpl(SignData.class, SpongeSignData.class,
                ImmutableSignData.class, ImmutableSpongeSignData.class, signDataProcessor);

        final SkullDataProcessor skullDataProcessor = new SkullDataProcessor();
        dataManager.registerDataProcessorAndImpl(SkullData.class, SpongeSkullData.class, ImmutableSkullData.class,
                ImmutableSpongeSkullData.class, skullDataProcessor);

        final VelocityDataProcessor velocityDataProcessor = new VelocityDataProcessor();
        dataManager.registerDataProcessorAndImpl(VelocityData.class, SpongeVelocityData.class, ImmutableVelocityData.class,
                ImmutableSpongeVelocityData.class, velocityDataProcessor);

        final FoodDataProcessor foodDataProcessor = new FoodDataProcessor();
        dataManager.registerDataProcessorAndImpl(FoodData.class, SpongeFoodData.class, ImmutableFoodData.class,
                ImmutableSpongeFoodData.class, foodDataProcessor);

        final BreathingDataProcessor breathingDataProcessor = new BreathingDataProcessor();
        dataManager.registerDataProcessorAndImpl(BreathingData.class, SpongeBreathingData.class, ImmutableBreathingData.class,
                ImmutableSpongeBreathingData.class, breathingDataProcessor);

        final ScreamingDataProcessor screamingDataProcessor = new ScreamingDataProcessor();
        dataManager.registerDataProcessorAndImpl(ScreamingData.class, SpongeScreamingData.class, ImmutableScreamingData.class,
                ImmutableSpongeScreamingData.class, screamingDataProcessor);

        final RepresentedItemDataProcessor representedItemDataProcessor = new RepresentedItemDataProcessor();
        dataManager.registerDataProcessorAndImpl(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, representedItemDataProcessor);

        final ItemEnchantmentDataProcessor itemEnchantmentDataProcessor = new ItemEnchantmentDataProcessor();
        dataManager.registerDataProcessorAndImpl(EnchantmentData.class, SpongeEnchantmentData.class, ImmutableEnchantmentData.class,
                ImmutableSpongeEnchantmentData.class, itemEnchantmentDataProcessor);

        final ItemLoreDataProcessor itemLoreDataProcessor = new ItemLoreDataProcessor();
        dataManager.registerDataProcessorAndImpl(LoreData.class, SpongeLoreData.class, ImmutableLoreData.class, ImmutableSpongeLoreData.class,
                itemLoreDataProcessor);

        final ItemPagedDataProcessor itemPagedDataProcessor = new ItemPagedDataProcessor();
        dataManager.registerDataProcessorAndImpl(PagedData.class, SpongePagedData.class, ImmutablePagedData.class, ImmutableSpongePagedData.class,
                itemPagedDataProcessor);

        final HorseDataProcessor horseDataProcessor = new HorseDataProcessor();
        dataManager.registerDataProcessorAndImpl(HorseData.class, SpongeHorseData.class, ImmutableHorseData.class,
                ImmutableSpongeHorseData.class, horseDataProcessor);

        final SneakingDataProcessor sneakingDataProcessor = new SneakingDataProcessor();
        dataManager.registerDataProcessorAndImpl(SneakingData.class, SpongeSneakingData.class, ImmutableSneakingData.class,
                ImmutableSpongeSneakingData.class, sneakingDataProcessor);

        final GoldenAppleDataProcessor goldenAppleDataProcessor = new GoldenAppleDataProcessor();
        dataManager.registerDataProcessorAndImpl(GoldenAppleData.class, SpongeGoldenAppleData.class, ImmutableGoldenAppleData.class,
                ImmutableSpongeGoldenAppleData.class, goldenAppleDataProcessor);

        final ExperienceHolderDataProcessor experienceHolderDataProcessor = new ExperienceHolderDataProcessor();
        dataManager.registerDataProcessorAndImpl(ExperienceHolderData.class, SpongeExperienceHolderData.class, ImmutableExperienceHolderData.class,
                ImmutableSpongeExperienceHolderData.class, experienceHolderDataProcessor);

        final ItemAuthorDataProcessor itemAuthorDataProcessor = new ItemAuthorDataProcessor();
        dataManager.registerDataProcessorAndImpl(AuthorData.class, SpongeAuthorData.class, ImmutableAuthorData.class,
                ImmutableSpongeAuthorData.class, itemAuthorDataProcessor);

        final BreakableDataProcessor breakableDataProcessor = new BreakableDataProcessor();
        dataManager.registerDataProcessorAndImpl(BreakableData.class, SpongeBreakableData.class, ImmutableBreakableData.class,
                ImmutableSpongeBreakableData.class, breakableDataProcessor);

        final PlaceableDataProcessor placeableDataProcessor = new PlaceableDataProcessor();
        dataManager.registerDataProcessorAndImpl(PlaceableData.class, SpongePlaceableData.class, ImmutablePlaceableData.class,
                ImmutableSpongePlaceableData.class, placeableDataProcessor);

        final MovementSpeedDataProcessor movementSpeedDataProcessor = new MovementSpeedDataProcessor();
        dataManager.registerDataProcessorAndImpl(MovementSpeedData.class, SpongeMovementSpeedData.class, ImmutableMovementSpeedData.class,
                ImmutableSpongeMovementSpeedData.class, movementSpeedDataProcessor);

        final SlimeDataProcessor slimeDataProcessor = new SlimeDataProcessor();
        dataManager.registerDataProcessorAndImpl(SlimeData.class, SpongeSlimeData.class, ImmutableSlimeData.class, ImmutableSpongeSlimeData.class,
                slimeDataProcessor);

        final VillagerZombieProcessor villagerZombieProcessor = new VillagerZombieProcessor();
        dataManager.registerDataProcessorAndImpl(VillagerZombieData.class, SpongeVillagerZombieData.class, ImmutableVillagerZombieData.class,
                ImmutableSpongeVillagerZombieData.class, villagerZombieProcessor);

        final PlayingDataProcessor playingDataProcessor = new PlayingDataProcessor();
        dataManager.registerDataProcessorAndImpl(PlayingData.class, SpongePlayingData.class, ImmutablePlayingData.class,
                ImmutableSpongePlayingData.class, playingDataProcessor);

        final SittingDataProcessor sittingDataProcessor = new SittingDataProcessor();
        dataManager.registerDataProcessorAndImpl(SittingData.class, SpongeSittingData.class, ImmutableSittingData.class,
                ImmutableSpongeSittingData.class, sittingDataProcessor);

        final ShearedDataProcessor shearedDataProcessor = new ShearedDataProcessor();
        dataManager.registerDataProcessorAndImpl(ShearedData.class, SpongeShearedData.class, ImmutableShearedData.class,
                ImmutableSpongeShearedData.class, shearedDataProcessor);

        final PigSaddleDataProcessor pigSaddleDataProcessor = new PigSaddleDataProcessor();
        dataManager.registerDataProcessorAndImpl(PigSaddleData.class, SpongePigSaddleData.class, ImmutablePigSaddleData.class,
                ImmutableSpongePigSaddleData.class, pigSaddleDataProcessor);

        final TameableDataProcessor tameableDataProcessor = new TameableDataProcessor();
        dataManager.registerDataProcessorAndImpl(TameableData.class, SpongeTameableData.class, ImmutableTameableData.class,
                ImmutableSpongeTameableData.class, tameableDataProcessor);

        final WolfWetDataProcessor wolfWetDataProcessor = new WolfWetDataProcessor();
        final ItemWetDataProcessor itemWetDataProcessor = new ItemWetDataProcessor();
        dataManager.registerDataProcessorAndImpl(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                wolfWetDataProcessor);
        dataManager.registerDataProcessorAndImpl(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class,
                itemWetDataProcessor);

        final ElderDataProcessor elderDataProcessor = new ElderDataProcessor();
        dataManager.registerDataProcessorAndImpl(ElderData.class, SpongeElderData.class, ImmutableElderData.class, ImmutableSpongeElderData.class,
                elderDataProcessor);

        final CoalDataProcessor coalDataProcessor = new CoalDataProcessor();
        dataManager.registerDataProcessorAndImpl(CoalData.class, SpongeCoalData.class, ImmutableCoalData.class,
                ImmutableSpongeCoalData.class, coalDataProcessor);

        final CookedFishDataProcessor cookedFishDataProcessor = new CookedFishDataProcessor();
        dataManager.registerDataProcessorAndImpl(CookedFishData.class, SpongeCookedFishData.class, ImmutableCookedFishData.class,
                ImmutableSpongeCookedFishData.class, cookedFishDataProcessor);

        final FishDataProcessor fishDataProcessor = new FishDataProcessor();
        dataManager.registerDataProcessorAndImpl(FishData.class, SpongeFishData.class, ImmutableFishData.class,
                ImmutableSpongeFishData.class, fishDataProcessor);

        dataManager.registerDataProcessorAndImpl(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class,
                ImmutableRepresentedPlayerData.class, ImmutableSpongeRepresentedPlayerData.class,
                new SkullRepresentedPlayerDataProcessor());

        dataManager.registerDataProcessorAndImpl(RepresentedPlayerData.class, SpongeRepresentedPlayerData.class,
                ImmutableRepresentedPlayerData.class, ImmutableSpongeRepresentedPlayerData.class,
                new ItemSkullRepresentedPlayerDataProcessor());

        final FurnaceDataProcessor furnaceDataProcessor = new FurnaceDataProcessor();
        dataManager.registerDataProcessorAndImpl(FurnaceData.class, SpongeFurnaceData.class,
                ImmutableFurnaceData.class, ImmutableSpongeFurnaceData.class, furnaceDataProcessor);

        final DirtDataProcessor dirtDataProcessor = new DirtDataProcessor();
        dataManager.registerDataProcessorAndImpl(DirtData.class, SpongeDirtData.class, ImmutableDirtData.class,
                ImmutableSpongeDirtData.class, dirtDataProcessor);

        final StoneDataProcessor stoneDataProcessor = new StoneDataProcessor();
        dataManager.registerDataProcessorAndImpl(StoneData.class, SpongeStoneData.class, ImmutableStoneData.class,
                ImmutableSpongeStoneData.class, stoneDataProcessor);

        final PrismarineDataProcessor prismarineDataProcessor = new PrismarineDataProcessor();
        dataManager.registerDataProcessorAndImpl(PrismarineData.class, SpongePrismarineData.class, ImmutablePrismarineData.class,
                ImmutableSpongePrismarineData.class, prismarineDataProcessor);

        final BrickDataProcessor brickDataProcessor = new BrickDataProcessor();
        dataManager.registerDataProcessorAndImpl(BrickData.class, SpongeBrickData.class, ImmutableBrickData.class,
                ImmutableSpongeBrickData.class, brickDataProcessor);

        final QuartzDataProcessor quartzDataProcessor = new QuartzDataProcessor();
        dataManager.registerDataProcessorAndImpl(QuartzData.class, SpongeQuartzData.class, ImmutableQuartzData.class,
                ImmutableSpongeQuartzData.class, quartzDataProcessor);

        final SandDataProcessor sandDataProcessor = new SandDataProcessor();
        dataManager.registerDataProcessorAndImpl(SandData.class, SpongeSandData.class, ImmutableSandData.class,
                ImmutableSpongeSandData.class, sandDataProcessor);

        final SlabDataProcessor slabDataProcessor = new SlabDataProcessor();
        dataManager.registerDataProcessorAndImpl(SlabData.class, SpongeSlabData.class, ImmutableSlabData.class,
                ImmutableSpongeSlabData.class, slabDataProcessor);

        final SandstoneDataProcessor sandstoneDataProcessor = new SandstoneDataProcessor();
        dataManager.registerDataProcessorAndImpl(SandstoneData.class, SpongeSandstoneData.class, ImmutableSandstoneData.class,
                ImmutableSpongeSandstoneData.class, sandstoneDataProcessor);

        final ComparatorDataProcessor comparatorDataProcessor = new ComparatorDataProcessor();
        dataManager.registerDataProcessorAndImpl(ComparatorData.class, SpongeComparatorData.class, ImmutableComparatorData.class,
                ImmutableSpongeComparatorData.class, comparatorDataProcessor);

        final TreeDataProcessor treeDataProcessor = new TreeDataProcessor();
        dataManager.registerDataProcessorAndImpl(TreeData.class, SpongeTreeData.class, ImmutableTreeData.class,
                ImmutableSpongeTreeData.class, treeDataProcessor);

        final DisguisedBlockDataProcessor disguisedBlockDataProcessor = new DisguisedBlockDataProcessor();
        dataManager.registerDataProcessorAndImpl(DisguisedBlockData.class, SpongeDisguisedBlockData.class, ImmutableDisguisedBlockData.class,
                ImmutableSpongeDisguisedBlockData.class, disguisedBlockDataProcessor);

        final HingeDataProcessor hingeDataProcessor = new HingeDataProcessor();
        dataManager.registerDataProcessorAndImpl(HingeData.class, SpongeHingeData.class, ImmutableHingeData.class,
                ImmutableSpongeHingeData.class, hingeDataProcessor);

        final PistonDataProcessor pistonDataProcessor = new PistonDataProcessor();
        dataManager.registerDataProcessorAndImpl(PistonData.class, SpongePistonData.class, ImmutablePistonData.class,
                ImmutableSpongePistonData.class, pistonDataProcessor);

        final PortionDataProcessor portionDataProcessor = new PortionDataProcessor();
        dataManager.registerDataProcessorAndImpl(PortionData.class, SpongePortionData.class, ImmutablePortionData.class,
                ImmutableSpongePortionData.class, portionDataProcessor);

        final RailDirectionDataProcessor railDirectionDataProcessor = new RailDirectionDataProcessor();
        dataManager.registerDataProcessorAndImpl(RailDirectionData.class, SpongeRailDirectionData.class, ImmutableRailDirectionData.class,
                ImmutableSpongeRailDirectionData.class, railDirectionDataProcessor);

        final StairShapeDataProcessor stairShapeDataProcessor = new StairShapeDataProcessor();
        dataManager.registerDataProcessorAndImpl(StairShapeData.class, SpongeStairShapeData.class, ImmutableStairShapeData.class,
                ImmutableSpongeStairShapeData.class, stairShapeDataProcessor);

        final WallDataProcessor wallDataProcessor = new WallDataProcessor();
        dataManager.registerDataProcessorAndImpl(WallData.class, SpongeWallData.class, ImmutableWallData.class,
                ImmutableSpongeWallData.class, wallDataProcessor);

        final ShrubDataProcessor shrubDataProcessor = new ShrubDataProcessor();
        dataManager.registerDataProcessorAndImpl(ShrubData.class, SpongeShrubData.class, ImmutableShrubData.class,
                ImmutableSpongeShrubData.class, shrubDataProcessor);

        final PlantDataProcessor plantDataProcessor = new PlantDataProcessor();
        dataManager.registerDataProcessorAndImpl(PlantData.class, SpongePlantData.class, ImmutablePlantData.class,
                ImmutableSpongePlantData.class, plantDataProcessor);

        final DoublePlantDataProcessor doublePlantDataProcessor = new DoublePlantDataProcessor();
        dataManager.registerDataProcessorAndImpl(DoublePlantData.class, SpongeDoublePlantData.class, ImmutableDoublePlantData.class,
                ImmutableSpongeDoublePlantData.class, doublePlantDataProcessor);

        final BigMushroomDataProcessor bigMushroomDataProcessor = new BigMushroomDataProcessor();
        dataManager.registerDataProcessorAndImpl(BigMushroomData.class, SpongeBigMushroomData.class, ImmutableBigMushroomData.class,
                ImmutableSpongeBigMushroomData.class, bigMushroomDataProcessor);


        final BrewingStandDataProcessor brewingStandDataProcessor = new BrewingStandDataProcessor();
        dataManager.registerDataProcessorAndImpl(BrewingStandData.class, SpongeBrewingStandData.class, ImmutableBrewingStandData.class,
                ImmutableSpongeBrewingStandData.class, brewingStandDataProcessor);

        final AttachedDataProcessor attachedDataProcessor = new AttachedDataProcessor();
        dataManager.registerDataProcessorAndImpl(AttachedData.class, SpongeAttachedData.class, ImmutableAttachedData.class,
                ImmutableSpongeAttachedData.class, attachedDataProcessor);

        final ConnectedDirectionDataProcessor connectedDirectionDataProcessor = new ConnectedDirectionDataProcessor();
        dataManager.registerDataProcessorAndImpl(ConnectedDirectionData.class, SpongeConnectedDirectionData.class, ImmutableConnectedDirectionData.class,
                ImmutableSpongeConnectedDirectionData.class, connectedDirectionDataProcessor);

        final DirectionalDataProcessor directionalDataProcessor = new DirectionalDataProcessor();
        dataManager.registerDataProcessorAndImpl(DirectionalData.class, SpongeDirectionalData.class, ImmutableDirectionalData.class,
                ImmutableSpongeDirectionalData.class, directionalDataProcessor);

        final DisarmedDataProcessor disarmedDataProcessor = new DisarmedDataProcessor();
        dataManager.registerDataProcessorAndImpl(DisarmedData.class, SpongeDisarmedData.class, ImmutableDisarmedData.class,
                ImmutableSpongeDisarmedData.class, disarmedDataProcessor);

        final DropDataProcessor dropDataProcessor = new DropDataProcessor();
        dataManager.registerDataProcessorAndImpl(DropData.class, SpongeDropData.class, ImmutableDropData.class,
                ImmutableSpongeDropData.class, dropDataProcessor);

        final ExtendedDataProcessor extendedDataProcessor = new ExtendedDataProcessor();
        dataManager.registerDataProcessorAndImpl(ExtendedData.class, SpongeExtendedData.class, ImmutableExtendedData.class,
                ImmutableSpongeExtendedData.class, extendedDataProcessor);

        final GrowthDataProcessor growthDataProcessor = new GrowthDataProcessor();
        dataManager.registerDataProcessorAndImpl(GrowthData.class, SpongeGrowthData.class, ImmutableGrowthData.class,
                ImmutableSpongeGrowthData.class, growthDataProcessor);

        final OpenDataProcessor openDataProcessor = new OpenDataProcessor();
        dataManager.registerDataProcessorAndImpl(OpenData.class, SpongeOpenData.class, ImmutableOpenData.class,
                ImmutableSpongeOpenData.class, openDataProcessor);

        final PoweredDataProcessor poweredDataProcessor = new PoweredDataProcessor();
        dataManager.registerDataProcessorAndImpl(PoweredData.class, SpongePoweredData.class, ImmutablePoweredData.class,
                ImmutableSpongePoweredData.class, poweredDataProcessor);

        final RedstonePoweredDataProcessor redstonePoweredDataProcessor = new RedstonePoweredDataProcessor();
        dataManager.registerDataProcessorAndImpl(RedstonePoweredData.class, SpongeRedstonePoweredData.class,
                ImmutableRedstonePoweredData.class, ImmutableSpongeRedstonePoweredData.class,
                redstonePoweredDataProcessor);

        final SeamlessDataProcessor seamlessDataProcessor = new SeamlessDataProcessor();
        dataManager.registerDataProcessorAndImpl(SeamlessData.class, SpongeSeamlessData.class, ImmutableSeamlessData.class,
                ImmutableSpongeSeamlessData.class, seamlessDataProcessor);

        final SnowedDataProcessor snowedDataProcessor = new SnowedDataProcessor();
        dataManager.registerDataProcessorAndImpl(SnowedData.class, SpongeSnowedData.class, ImmutableSnowedData.class,
                ImmutableSpongeSnowedData.class, snowedDataProcessor);

        final SuspendedDataProcessor suspendedDataProcessor = new SuspendedDataProcessor();
        dataManager.registerDataProcessorAndImpl(SuspendedData.class, SpongeSuspendedData.class, ImmutableSuspendedData.class,
                ImmutableSpongeSuspendedData.class, suspendedDataProcessor);

        final OccupiedDataProcessor occupiedDataProcessor = new OccupiedDataProcessor();
        dataManager.registerDataProcessorAndImpl(OccupiedData.class, SpongeOccupiedData.class, ImmutableOccupiedData.class,
                ImmutableSpongeOccupiedData.class, occupiedDataProcessor);

        final InWallDataProcessor inWallDataProcessor = new InWallDataProcessor();
        dataManager.registerDataProcessorAndImpl(InWallData.class, SpongeInWallData.class, ImmutableInWallData.class,
                ImmutableSpongeInWallData.class, inWallDataProcessor);

        final LayeredDataProcessor layeredDataProcessor = new LayeredDataProcessor();
        dataManager.registerDataProcessorAndImpl(LayeredData.class, SpongeLayeredData.class, ImmutableLayeredData.class,
                ImmutableSpongeLayeredData.class, layeredDataProcessor);

        final DecayableDataProcessor decayableDataProcessor = new DecayableDataProcessor();
        dataManager.registerDataProcessorAndImpl(DecayableData.class, SpongeDecayableData.class, ImmutableDecayableData.class,
                ImmutableSpongeDecayableData.class, decayableDataProcessor);

        final AxisDataProcessor axisDataProcessor = new AxisDataProcessor();
        dataManager.registerDataProcessorAndImpl(AxisData.class, SpongeAxisData.class, ImmutableAxisData.class,
                ImmutableSpongeAxisData.class, axisDataProcessor);

        final DelayableDataProcessor delayableDataProcessor = new DelayableDataProcessor();
        dataManager.registerDataProcessorAndImpl(DelayableData.class, SpongeDelayableData.class, ImmutableDelayableData.class,
                ImmutableSpongeDelayableData.class, delayableDataProcessor);

        final AgentDataProcessor agentDataProcessor = new AgentDataProcessor();
        dataManager.registerDataProcessorAndImpl(AgentData.class, SpongeAgentData.class, ImmutableAgentData.class,
                ImmutableSpongeAgentData.class, agentDataProcessor);

        final ChargedDataProcessor chargedDataProcessor = new ChargedDataProcessor();
        dataManager.registerDataProcessorAndImpl(ChargedData.class, SpongeChargedData.class, ImmutableChargedData.class,
                ImmutableSpongeChargedData.class, chargedDataProcessor);

        final DurabilityDataProcessor durabilityDataProcessor = new DurabilityDataProcessor();
        dataManager.registerDataProcessorAndImpl(DurabilityData.class, SpongeDurabilityData.class, ImmutableDurabilityData.class,
                ImmutableSpongeDurabilityData.class, durabilityDataProcessor);

        final SpawnableDataProcessor spawnableDataProcessor = new SpawnableDataProcessor();
        dataManager.registerDataProcessorAndImpl(SpawnableData.class, SpongeSpawnableData.class, ImmutableSpawnableData.class,
                ImmutableSpongeSpawnableData.class, spawnableDataProcessor);

        final FallDistanceDataProcessor fallDistanceDataProcessor = new FallDistanceDataProcessor();
        dataManager.registerDataProcessorAndImpl(FallDistanceData.class, SpongeFallDistanceData.class,
                ImmutableFallDistanceData.class, ImmutableSpongeFallDistanceData.class, fallDistanceDataProcessor);

        final CooldownDataProcessor cooldownDataProcessor = new CooldownDataProcessor();
        dataManager.registerDataProcessorAndImpl(CooldownData.class, SpongeCooldownData.class, ImmutableCooldownData.class,
                ImmutableSpongeCooldownData.class, cooldownDataProcessor);

        final NoteDataProcessor noteDataProcessor = new NoteDataProcessor();
        dataManager.registerDataProcessorAndImpl(NoteData.class, SpongeNoteData.class, ImmutableNoteData.class,
                ImmutableSpongeNoteData.class, noteDataProcessor);

        final VehicleDataProcessor vehicleDataProcessor = new VehicleDataProcessor();
        dataManager.registerDataProcessorAndImpl(VehicleData.class, SpongeVehicleData.class, ImmutableVehicleData.class,
                ImmutableSpongeVehicleData.class, vehicleDataProcessor);

        final TargetMultipleLivingDataProcessor targetMultipleLivingDataProcessor = new TargetMultipleLivingDataProcessor();
        dataManager.registerDataProcessorAndImpl(TargetMultipleLivingData.class, SpongeTargetMultipleLivingData.class, ImmutableTargetMultipleLivingData.class,
                ImmutableSpongeTargetMultipleLivingData.class, targetMultipleLivingDataProcessor);

        final TargetLivingDataProcessor targetLivingDataProcessor = new TargetLivingDataProcessor();
        dataManager.registerDataProcessorAndImpl(TargetLivingData.class, SpongeTargetLivingData.class, ImmutableTargetLivingData.class,
                ImmutableSpongeTargetLivingData.class, targetLivingDataProcessor);

        dataManager.registerDataProcessorAndImpl(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new TileEntityLockableDataProcessor());
        dataManager.registerDataProcessorAndImpl(LockableData.class, SpongeLockableData.class,
                ImmutableLockableData.class, ImmutableSpongeLockableData.class, new ItemLockableDataProcessor());

        final FireworkEffectDataProcessor fireworkEffectDataProcessor = new FireworkEffectDataProcessor();
        dataManager.registerDataProcessorAndImpl(FireworkEffectData.class, SpongeFireworkEffectData.class,
                ImmutableFireworkEffectData.class, ImmutableSpongeFireworkEffectData.class, fireworkEffectDataProcessor);

        final BlockItemDataProcessor blockItemDataProcessor = new BlockItemDataProcessor();
        dataManager.registerDataProcessorAndImpl(BlockItemData.class, SpongeBlockItemData.class, ImmutableBlockItemData.class,
                ImmutableSpongeBlockItemData.class, blockItemDataProcessor);

        final FireworkRocketDataProcessor fireworkRocketDataProcessor = new FireworkRocketDataProcessor();
        dataManager.registerDataProcessorAndImpl(FireworkRocketData.class, SpongeFireworkRocketData.class,
                ImmutableFireworkRocketData.class, ImmutableSpongeFireworkRocketData.class, fireworkRocketDataProcessor);

        final MinecartBlockDataProcessor minecartBlockDataProcessor = new MinecartBlockDataProcessor();
        dataManager.registerDataProcessorAndImpl(MinecartBlockData.class, SpongeMinecartBlockData.class,
                ImmutableMinecartBlockData.class, ImmutableSpongeMinecartBlockData.class, minecartBlockDataProcessor);

        final PlayerCreatedDataProcessor playerCreatedDataProcessor = new PlayerCreatedDataProcessor();
        dataManager.registerDataProcessorAndImpl(PlayerCreatedData.class, SpongePlayerCreatedData.class, ImmutablePlayerCreatedData.class,
                ImmutableSpongePlayerCreatedData.class, playerCreatedDataProcessor);

        final JukeboxDataProcessor jukeboxDataProcessor = new JukeboxDataProcessor();
        dataManager.registerDataProcessorAndImpl(RepresentedItemData.class, SpongeRepresentedItemData.class, ImmutableRepresentedItemData.class,
                ImmutableSpongeRepresentedItemData.class, jukeboxDataProcessor);

        final FallingBlockDataProcessor fallingBlockDataProcessor = new FallingBlockDataProcessor();
        dataManager.registerDataProcessorAndImpl(FallingBlockData.class, SpongeFallingBlockData.class, ImmutableFallingBlockData.class,
                                                  ImmutableSpongeFallingBlockData.class, fallingBlockDataProcessor);

        final TradeOfferDataProcessor tradeOfferDataProcessor = new TradeOfferDataProcessor();
        dataManager.registerDataProcessorAndImpl(TradeOfferData.class, SpongeTradeOfferData.class, ImmutableTradeOfferData.class,
                ImmutableSpongeTradeOfferData.class, tradeOfferDataProcessor);

        final SkeletonDataProcessor skeletonDataProcessor = new SkeletonDataProcessor();
        dataManager.registerDataProcessorAndImpl(SkeletonData.class, SpongeSkeletonData.class, ImmutableSkeletonData.class,
                ImmutableSpongeSkeletonData.class, skeletonDataProcessor);

        final RabbitDataProcessor rabbitDataProcessor = new RabbitDataProcessor();
        dataManager.registerDataProcessorAndImpl(RabbitData.class, SpongeRabbitData.class, ImmutableRabbitData.class,
                ImmutableSpongeRabbitData.class, rabbitDataProcessor);

        final TileEntityBannerDataProcessor bannerDataProcessor = new TileEntityBannerDataProcessor();
        dataManager.registerDataProcessorAndImpl(BannerData.class, SpongeBannerData.class, ImmutableBannerData.class,
                ImmutableSpongeBannerData.class, bannerDataProcessor);

        final RespawnLocationDataProcessor respawnLocationDataProcessor = new RespawnLocationDataProcessor();
        dataManager.registerDataProcessorAndImpl(RespawnLocationData.class, SpongeRespawnLocationData.class, ImmutableRespawnLocation.class,
                ImmutableSpongeRespawnLocation.class, respawnLocationDataProcessor);

        final MoistureDataProcessor moistureDataProcessor = new MoistureDataProcessor();
        dataManager.registerDataProcessorAndImpl(MoistureData.class, SpongeMoistureData.class, ImmutableMoistureData.class,
                ImmutableSpongeMoistureData.class, moistureDataProcessor);

        // Values
        dataManager.registerValueProcessor(Keys.HEALTH, new HealthValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_HEALTH, new MaxHealthValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRE_TICKS, new FireTicksValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRE_DAMAGE_DELAY, new FireDamageDelayValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new ItemDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new TileEntityDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new EntityDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.SHOWS_DISPLAY_NAME, new DisplayNameVisibleValueProcessor());
        dataManager.registerValueProcessor(Keys.SIGN_LINES, new SignLinesValueProcessor());
        dataManager.registerValueProcessor(Keys.SKULL_TYPE, new TileEntitySkullValueProcessor());
        dataManager.registerValueProcessor(Keys.SKULL_TYPE, new ItemSkullValueProcessor());
        dataManager.registerValueProcessor(Keys.VELOCITY, new VelocityValueProcessor());
        dataManager.registerValueProcessor(Keys.FOOD_LEVEL, new FoodLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.SATURATION, new FoodSaturationValueProcessor());
        dataManager.registerValueProcessor(Keys.EXHAUSTION, new FoodExhaustionValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_AIR, new MaxAirValueProcessor());
        dataManager.registerValueProcessor(Keys.REMAINING_AIR, new RemainingAirValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_SCREAMING, new ScreamingValueProcessor());
        dataManager.registerValueProcessor(Keys.ITEM_ENCHANTMENTS, new ItemEnchantmentValueProcessor());
        dataManager.registerValueProcessor(Keys.HORSE_COLOR, new HorseColorValueProcessor());
        dataManager.registerValueProcessor(Keys.HORSE_STYLE, new HorseStyleValueProcessor());
        dataManager.registerValueProcessor(Keys.HORSE_VARIANT, new HorseVariantValueProcessor());
        dataManager.registerValueProcessor(Keys.ITEM_LORE, new ItemLoreValueProcessor());
        dataManager.registerValueProcessor(Keys.BOOK_PAGES, new BookPagesValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_SNEAKING, new SneakingValueProcessor());
        dataManager.registerValueProcessor(Keys.GOLDEN_APPLE_TYPE, new GoldenAppleValueProcessor());
        dataManager.registerValueProcessor(Keys.EXPERIENCE_LEVEL, new ExperienceLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.TOTAL_EXPERIENCE, new TotalExperienceValueProcessor());
        dataManager.registerValueProcessor(Keys.EXPERIENCE_SINCE_LEVEL, new ExperienceSinceLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.EXPERIENCE_FROM_START_OF_LEVEL, new ExperienceFromStartOfLevelValueProcessor());
        dataManager.registerValueProcessor(Keys.BOOK_AUTHOR, new BookAuthorValueProcessor());
        dataManager.registerValueProcessor(Keys.REPRESENTED_ITEM, new RepresentedItemValueProcessor());
        dataManager.registerValueProcessor(Keys.BREAKABLE_BLOCK_TYPES, new BreakableValueProcessor());
        dataManager.registerValueProcessor(Keys.PLACEABLE_BLOCKS, new PlaceableValueProcessor());
        dataManager.registerValueProcessor(Keys.WALKING_SPEED, new WalkingSpeedValueProcessor());
        dataManager.registerValueProcessor(Keys.FLYING_SPEED, new FlyingSpeedValueProcessor());
        dataManager.registerValueProcessor(Keys.SLIME_SIZE, new SlimeValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_VILLAGER_ZOMBIE, new VillagerZombieValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_PLAYING, new PlayingValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_SITTING, new IsSittingValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_SHEARED, new IsShearedValueProcessor());
        dataManager.registerValueProcessor(Keys.PIG_SADDLE, new PigSaddleValueProcessor());
        dataManager.registerValueProcessor(Keys.TAMED_OWNER, new TameableOwnerValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_WET, new ItemWetValueProcessor());
        dataManager.registerValueProcessor(Keys.IS_WET, new EntityWetValueProcessor());
        dataManager.registerValueProcessor(Keys.ELDER_GUARDIAN, new ElderValueProcessor());
        dataManager.registerValueProcessor(Keys.COAL_TYPE, new CoalValueProcessor());
        dataManager.registerValueProcessor(Keys.COOKED_FISH, new CookedFishValueProcessor());
        dataManager.registerValueProcessor(Keys.FISH_TYPE, new FishValueProcessor());
        dataManager.registerValueProcessor(Keys.REPRESENTED_PLAYER, new SkullRepresentedPlayerProcessor());
        dataManager.registerValueProcessor(Keys.REPRESENTED_PLAYER, new ItemSkullRepresentedPlayerValueProcessor());
        dataManager.registerValueProcessor(Keys.PASSED_BURN_TIME, new PassedBurnTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_BURN_TIME, new MaxBurnTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.PASSED_COOK_TIME, new PassedCookTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_COOK_TIME, new MaxCookTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.REMAINING_BREW_TIME, new RemainingBrewTimeValueProcessor());
        dataManager.registerValueProcessor(Keys.DIRT_TYPE, new DirtTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.STONE_TYPE, new StoneTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.BRICK_TYPE, new BrickTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.PRISMARINE_TYPE, new PrismarineTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.QUARTZ_TYPE, new QuartzTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.SAND_TYPE, new SandTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.SLAB_TYPE, new SlabTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.SANDSTONE_TYPE, new SandstoneTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.COMPARATOR_TYPE, new ComparatorTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.TREE_TYPE, new TreeTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.HINGE_POSITION, new HingePositionValueProcessor());
        dataManager.registerValueProcessor(Keys.PISTON_TYPE, new PistonTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.PORTION_TYPE, new PortionTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.RAIL_DIRECTION, new RailDirectionValueProcessor());
        dataManager.registerValueProcessor(Keys.STAIR_SHAPE, new StairShapeValueProcessor());
        dataManager.registerValueProcessor(Keys.WALL_TYPE, new WallTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.SHRUB_TYPE, new ShrubTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.PLANT_TYPE, new PlantTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.DOUBLE_PLANT_TYPE, new DoublePlantTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.BIG_MUSHROOM_TYPE, new BigMushroomTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.DISGUISED_BLOCK_TYPE, new DisguisedBlockTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.COLOR, new ItemColorValueProcessor());
        dataManager.registerValueProcessor(Keys.AI_ENABLED, new IsAiEnabledValueProcessor());
        dataManager.registerValueProcessor(Keys.CREEPER_CHARGED, new ChargedValueProcessor());
        dataManager.registerValueProcessor(Keys.UNBREAKABLE, new UnbreakableValueProcessor());
        dataManager.registerValueProcessor(Keys.ITEM_DURABILITY, new ItemDurabilityValueProcessor());
        dataManager.registerValueProcessor(Keys.SPAWNABLE_ENTITY_TYPE, new SpawnableEntityTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.FALL_DISTANCE, new FallDistanceValueProcessor());
        dataManager.registerValueProcessor(Keys.COOLDOWN, new CooldownValueProcessor());
        dataManager.registerValueProcessor(Keys.NOTE_PITCH, new NoteValueProcessor());
        dataManager.registerValueProcessor(Keys.VEHICLE, new VehicleValueProcessor());
        dataManager.registerValueProcessor(Keys.BASE_VEHICLE, new BaseVehicleValueProcessor());
        dataManager.registerValueProcessor(Keys.TARGET, new TargetLivingValueProcessor());
        dataManager.registerValueProcessor(Keys.TARGETS, new TargetMultipleLivingValueProcessor());
        dataManager.registerValueProcessor(Keys.FIREWORK_EFFECTS, new EntityFireworkEffectsValueProcessor());
        dataManager.registerValueProcessor(Keys.FIREWORK_EFFECTS, new ItemFireworkEffectsValueProcessor());
        dataManager.registerValueProcessor(Keys.FIREWORK_FLIGHT_MODIFIER, new EntityFireworkRocketValueProcessor());
        dataManager.registerValueProcessor(Keys.FIREWORK_FLIGHT_MODIFIER, new ItemFireworkRocketValueProcessor());
        dataManager.registerValueProcessor(Keys.REPRESENTED_BLOCK, new RepresentedBlockValueProcessor());
        dataManager.registerValueProcessor(Keys.OFFSET, new OffsetValueProcessor());
        dataManager.registerValueProcessor(Keys.ATTACHED, new AttachedValueProcessor());
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
        dataManager.registerValueProcessor(Keys.DIRECTION, new DirectionValueProcessor());
        dataManager.registerValueProcessor(Keys.DISARMED, new DisarmedValueProcessor());
        dataManager.registerValueProcessor(Keys.SHOULD_DROP, new ShouldDropValueProcessor());
        dataManager.registerValueProcessor(Keys.EXTENDED, new ExtendedValueProcessor());
        dataManager.registerValueProcessor(Keys.GROWTH_STAGE, new GrowthStageValueProcessor());
        dataManager.registerValueProcessor(Keys.OPEN, new OpenValueProcessor());
        dataManager.registerValueProcessor(Keys.POWERED, new PoweredValueProcessor());
        dataManager.registerValueProcessor(Keys.POWER, new PowerValueProcessor());
        dataManager.registerValueProcessor(Keys.SEAMLESS, new SeamlessValueProcessor());
        dataManager.registerValueProcessor(Keys.SNOWED, new SnowedValueProcessor());
        dataManager.registerValueProcessor(Keys.SUSPENDED, new SuspendedValueProcessor());
        dataManager.registerValueProcessor(Keys.OCCUPIED, new OccupiedValueProcessor());
        dataManager.registerValueProcessor(Keys.LAYER, new LayerValueProcessor());
        dataManager.registerValueProcessor(Keys.DECAYABLE, new DecayableValueProcessor());
        dataManager.registerValueProcessor(Keys.IN_WALL, new InWallValueProcessor());
        dataManager.registerValueProcessor(Keys.AXIS, new AxisValueProcessor());
        dataManager.registerValueProcessor(Keys.DELAY, new DelayValueProcessor());
        dataManager.registerValueProcessor(Keys.PLAYER_CREATED, new PlayerCreatedValueProcessor());
        dataManager.registerValueProcessor(Keys.ITEM_BLOCKSTATE, new BlockItemValueProcessor());
        dataManager.registerValueProcessor(Keys.REPRESENTED_ITEM, new JukeboxValueProcessor());
        dataManager.registerValueProcessor(Keys.SKELETON_TYPE, new SkeletonTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.RABBIT_TYPE, new RabbitTypeValueProcessor());
        dataManager.registerValueProcessor(Keys.LOCK_TOKEN, new LockTokenValueProcessor());
        dataManager.registerValueProcessor(Keys.LOCK_TOKEN, new ItemLockTokenValueProcessor());
        dataManager.registerValueProcessor(Keys.BANNER_BASE_COLOR, new TileBannerBaseColorValueProcessor());
        dataManager.registerValueProcessor(Keys.BANNER_PATTERNS, new TileBannerPatternLayersValueProcessor());
        dataManager.registerValueProcessor(Keys.RESPAWN_LOCATIONS, new RespawnLocationValueProcessor());
        dataManager.registerValueProcessor(Keys.MOISTURE, new MoistureValueProcessor());

        // Dual Processors
        final EndermiteExpirableDualProcessor expirableDataProcessor = new EndermiteExpirableDualProcessor();
        dataManager.registerDataProcessorAndImpl(ExpirableData.class, SpongeExpirableData.class, ImmutableExpirableData.class,
            ImmutableSpongeExpirableData.class, expirableDataProcessor);
        dataManager.registerValueProcessor(Keys.EXPIRATION_TICKS, expirableDataProcessor);

        final ArtDualProcessor artDualProcessor = new ArtDualProcessor();
        dataManager.registerDataProcessorAndImpl(ArtData.class, SpongeArtData.class, ImmutableArtData.class, ImmutableSpongeArtData.class,
            artDualProcessor);
        dataManager.registerValueProcessor(Keys.ART, artDualProcessor);

        final CareerDualProcessor careerDualProcessor = new CareerDualProcessor();
        dataManager.registerDataProcessorAndImpl(CareerData.class, SpongeCareerData.class, ImmutableCareerData.class,
            ImmutableSpongeCareerData.class, careerDualProcessor);
        dataManager.registerValueProcessor(Keys.CAREER, careerDualProcessor);

        final SkinDataProcessor skinDataProcessor = new SkinDataProcessor();
        dataManager.registerDataProcessorAndImpl(SkinData.class, SpongeSkinData.class, ImmutableSkinData.class,
            ImmutableSpongeSkinData.class, skinDataProcessor);
        dataManager.registerValueProcessor(Keys.SKIN, skinDataProcessor);

        final ExpOrbDataProcessor expOrbDataProcessor = new ExpOrbDataProcessor();
        dataManager.registerDataProcessorAndImpl(ExpOrbData.class, SpongeExpOrbData.class, ImmutableExpOrbData.class,
            ImmutableSpongeExpOrbData.class, expOrbDataProcessor);
        dataManager.registerValueProcessor(Keys.CONTAINED_EXPERIENCE, expOrbDataProcessor);

        final FlyingDataProcessor flyingDataProcessor = new FlyingDataProcessor();
        dataManager.registerDataProcessorAndImpl(FlyingData.class, SpongeFlyingData.class, ImmutableFlyingData.class,
            ImmutableSpongeFlyingData.class, flyingDataProcessor);
        dataManager.registerValueProcessor(Keys.IS_FLYING, flyingDataProcessor);

        final FlyingAbilityDataProcessor flyingAbilityDataProcessor = new FlyingAbilityDataProcessor();
        dataManager.registerDataProcessorAndImpl(FlyingAbilityData.class, SpongeFlyingAbilityData.class, ImmutableFlyingAbilityData.class,
            ImmutableSpongeFlyingAbilityData.class, flyingAbilityDataProcessor);
        dataManager.registerValueProcessor(Keys.CAN_FLY, flyingAbilityDataProcessor);

        final OcelotDataProcessor ocelotDataProcessor = new OcelotDataProcessor();
        dataManager.registerDataProcessorAndImpl(OcelotData.class, SpongeOcelotData.class, ImmutableOcelotData.class,
            ImmutableSpongeOcelotData.class, ocelotDataProcessor);

        dataManager.registerValueProcessor(Keys.OCELOT_TYPE, ocelotDataProcessor);

        final GameModeDataProcessor gameModeDataProcessor = new GameModeDataProcessor();
        dataManager.registerDataProcessorAndImpl(GameModeData.class, SpongeGameModeData.class, ImmutableGameModeData.class,
            ImmutableSpongeGameModeData.class, gameModeDataProcessor);
        dataManager.registerValueProcessor(Keys.GAME_MODE, gameModeDataProcessor);

        final AggressiveDataProcessor aggressiveDataProcessor = new AggressiveDataProcessor();
        dataManager.registerDataProcessorAndImpl(AggressiveData.class, SpongeAggressiveData.class, ImmutableAggressiveData.class,
            ImmutableSpongeAggressiveData.class, aggressiveDataProcessor);
        dataManager.registerValueProcessor(Keys.ANGRY, aggressiveDataProcessor);


        final AngerableDataProcessor angerableDataProcessor = new AngerableDataProcessor();
        dataManager.registerDataProcessorAndImpl(AngerableData.class, SpongeAngerableData.class, ImmutableAngerableData.class,
            ImmutableSpongeAngerableData.class, angerableDataProcessor);
        dataManager.registerValueProcessor(Keys.ANGER, angerableDataProcessor);


        final RotationalDataProcessor rotationalDataProcessor = new RotationalDataProcessor();
        dataManager.registerDataProcessorAndImpl(RotationalData.class, SpongeRotationalData.class, ImmutableRotationalData.class,
            ImmutableSpongeRotationalData.class, rotationalDataProcessor);
        dataManager.registerValueProcessor(Keys.ROTATION, rotationalDataProcessor);

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
