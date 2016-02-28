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
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDyeableData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkEffectData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkRocketData;
import org.spongepowered.api.data.manipulator.immutable.ImmutablePotionEffectData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedPlayerData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRotationalData;
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
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableMoistureData;
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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAffectsSpawningData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgentData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAggressiveData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAngerableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableArtData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreedableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableChargedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCriticalHitData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableElderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpirableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallDistanceData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlammableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingAbilityData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGameModeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableInvisibilityData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableJoinData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableKnockbackData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMinecartBlockData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableOcelotData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePersistingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePigSaddleData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayerCreatedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableRabbitData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableRespawnLocation;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableScreamingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableShearedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSittingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkeletonData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkinData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSlimeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSprintData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableStuckArrowsData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
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
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGenerationData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableHideData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableLoreData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePagedData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutablePlaceableData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSplashPotionData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableStoredEnchantmentData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableCooldownData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableFurnaceData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DyeableData;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.data.manipulator.mutable.FireworkRocketData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.RotationalData;
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
import org.spongepowered.api.data.manipulator.mutable.block.MoistureData;
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
import org.spongepowered.api.data.manipulator.mutable.entity.AffectsSpawningData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.data.manipulator.mutable.entity.AggressiveData;
import org.spongepowered.api.data.manipulator.mutable.entity.AngerableData;
import org.spongepowered.api.data.manipulator.mutable.entity.ArtData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreedableData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.ChargedData;
import org.spongepowered.api.data.manipulator.mutable.entity.CriticalHitData;
import org.spongepowered.api.data.manipulator.mutable.entity.ElderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpirableData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallDistanceData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlammableData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingAbilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.InvisibilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.manipulator.mutable.entity.KnockbackData;
import org.spongepowered.api.data.manipulator.mutable.entity.MinecartBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.OcelotData;
import org.spongepowered.api.data.manipulator.mutable.entity.PersistingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PigSaddleData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayerCreatedData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.RabbitData;
import org.spongepowered.api.data.manipulator.mutable.entity.RespawnLocationData;
import org.spongepowered.api.data.manipulator.mutable.entity.ScreamingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ShearedData;
import org.spongepowered.api.data.manipulator.mutable.entity.SittingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinData;
import org.spongepowered.api.data.manipulator.mutable.entity.SlimeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SprintData;
import org.spongepowered.api.data.manipulator.mutable.entity.StuckArrowsData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
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
import org.spongepowered.api.data.manipulator.mutable.item.GenerationData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.HideData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.manipulator.mutable.item.PlaceableData;
import org.spongepowered.api.data.manipulator.mutable.item.SpawnableData;
import org.spongepowered.api.data.manipulator.mutable.item.SplashPotionData;
import org.spongepowered.api.data.manipulator.mutable.item.StoredEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
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
import org.spongepowered.api.data.property.item.ArmorTypeProperty;
import org.spongepowered.api.data.property.item.BurningFuelProperty;
import org.spongepowered.api.data.property.item.DamageAbsorptionProperty;
import org.spongepowered.api.data.property.item.EfficiencyProperty;
import org.spongepowered.api.data.property.item.EquipmentProperty;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.api.data.property.item.SaturationProperty;
import org.spongepowered.api.data.property.item.ToolTypeProperty;
import org.spongepowered.api.data.property.item.UseLimitProperty;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidItemData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidItemData;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextConfigSerializer;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.block.SpongeBlockStateBuilder;
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
import org.spongepowered.common.data.builder.data.meta.SpongeItemEnchantmentBuilder;
import org.spongepowered.common.data.builder.data.meta.SpongePatternLayerBuilder;
import org.spongepowered.common.data.builder.item.SpongeFireworkEffectDataBuilder;
import org.spongepowered.common.data.builder.item.SpongeItemStackDataBuilder;
import org.spongepowered.common.data.builder.item.SpongeItemStackSnapshotBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.block.ImmutableSpongeTreeDataBuilder;
import org.spongepowered.common.data.builder.manipulator.immutable.item.ImmutableItemEnchantmentDataBuilder;
import org.spongepowered.common.data.builder.util.weighted.BaseAndAdditionBuilder;
import org.spongepowered.common.data.builder.util.weighted.BaseAndVarianceBuilder;
import org.spongepowered.common.data.builder.util.weighted.FixedBuilder;
import org.spongepowered.common.data.builder.util.weighted.OptionalVarianceBuilder;
import org.spongepowered.common.data.builder.world.LocationBuilder;
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeColoredData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeCommandData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeCooldownData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDyeableData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeFireworkEffectData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeFireworkRocketData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongePotionEffectData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedPlayerData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRotationalData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeSkullData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeWetData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeAttachedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeAxisData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeBigMushroomData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeBrickData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeComparatorData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeConnectedDirectionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDecayableData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDelayableData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirtData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDisarmedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDisguisedBlockData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDoublePlantData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDropData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeExtendedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeGrowthData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeHingeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeInWallData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeLayeredData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeMoistureData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeOccupiedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeOpenData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePistonData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePlantData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePortionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePoweredData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePrismarineData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeQuartzData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRailDirectionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRedstonePoweredData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSandData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSandstoneData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSeamlessData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeShrubData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSlabData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSnowedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeStairShapeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeStoneData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSuspendedData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeWallData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAffectsSpawningData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAgentData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAggressiveData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAngerableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeArtData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreathingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreedableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCareerData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeChargedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCriticalHitData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeElderData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExpOrbData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExpirableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFallDistanceData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFallingBlockData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlammableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlyingAbilityData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlyingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFoodData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeGameModeData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHealthData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHorseData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeIgniteableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeInvisibilityData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeJoinData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeKnockbackData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeMinecartBlockData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeMovementSpeedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeOcelotData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePersistingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePigSaddleData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePlayerCreatedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePlayingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeRabbitData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeRespawnLocation;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeScreamingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeShearedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSittingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSkeletonData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSkinData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSlimeData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSneakingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSprintData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeStuckArrowsData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTameableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTradeOfferData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVehicleData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVelocityData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVillagerZombieData;
import org.spongepowered.common.data.manipulator.immutable.extra.ImmutableSpongeFluidItemData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeAuthorData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeBlockItemData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeBreakableData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeCoalData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeCookedFishData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeDurabilityData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeFishData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeGenerationData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeHideData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeLoreData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePagedData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePlaceableData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeSpawnableData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeSplashPotionData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeStoredEnchantmentData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBannerData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBrewingStandData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeFurnaceData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeLockableData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeNoteData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeSignData;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.manipulator.mutable.SpongeCommandData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkEffectData;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkRocketData;
import org.spongepowered.common.data.manipulator.mutable.SpongePotionEffectData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRotationalData;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.manipulator.mutable.SpongeTradeOfferData;
import org.spongepowered.common.data.manipulator.mutable.SpongeWetData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeAttachedData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeAxisData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeBigMushroomData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeBrickData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeComparatorData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeConnectedDirectionData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDecayableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDelayableData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirtData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDisarmedData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDisguisedBlockData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDoublePlantData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDropData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeExtendedData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeGrowthData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeHingeData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeInWallData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeLayeredData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeMoistureData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeOccupiedData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeOpenData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePistonData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePlantData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePortionData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePoweredData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePrismarineData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeQuartzData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeRailDirectionData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeRedstonePoweredData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSandData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSandstoneData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSeamlessData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeShrubData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSlabData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSnowedData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeStairShapeData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeStoneData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSuspendedData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeWallData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAffectsSpawningData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAggressiveData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAngerableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArtData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreedableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeChargedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCriticalHitData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeElderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpOrbData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpirableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallDistanceData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallingBlockData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlammableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingAbilityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgniteableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeInvisibilityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJoinData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeKnockbackData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMinecartBlockData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMovementSpeedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeOcelotData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePersistingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePigSaddleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayerCreatedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeRabbitData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeRespawnLocationData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeScreamingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeShearedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSittingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkeletonData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkinData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSlimeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSneakingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSprintData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeStuckArrowsData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVillagerZombieData;
import org.spongepowered.common.data.manipulator.mutable.extra.SpongeFluidItemData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeAuthorData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBlockItemData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBreakableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCoalData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCookedFishData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeDurabilityData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeFishData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGenerationData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeHideData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePlaceableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeSpawnableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeSplashPotionData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeStoredEnchantmentData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBrewingStandData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeCooldownData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeFurnaceData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeNoteData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.data.DisplayNameDataProcessor;
import org.spongepowered.common.data.processor.data.DyeableDataProcessor;
import org.spongepowered.common.data.processor.data.block.AttachedDataProcessor;
import org.spongepowered.common.data.processor.data.block.AxisDataProcessor;
import org.spongepowered.common.data.processor.data.block.BigMushroomDataProcessor;
import org.spongepowered.common.data.processor.data.block.BrickDataProcessor;
import org.spongepowered.common.data.processor.data.block.ComparatorDataProcessor;
import org.spongepowered.common.data.processor.data.block.DecayableDataProcessor;
import org.spongepowered.common.data.processor.data.block.DelayableDataProcessor;
import org.spongepowered.common.data.processor.data.block.DirectionalDataProcessor;
import org.spongepowered.common.data.processor.data.block.DirtDataProcessor;
import org.spongepowered.common.data.processor.data.block.DisarmedDataProcessor;
import org.spongepowered.common.data.processor.data.block.DisguisedBlockDataProcessor;
import org.spongepowered.common.data.processor.data.block.DoublePlantDataProcessor;
import org.spongepowered.common.data.processor.data.block.DropDataProcessor;
import org.spongepowered.common.data.processor.data.block.ExtendedDataProcessor;
import org.spongepowered.common.data.processor.data.block.GrowthDataProcessor;
import org.spongepowered.common.data.processor.data.block.HingeDataProcessor;
import org.spongepowered.common.data.processor.data.block.InWallDataProcessor;
import org.spongepowered.common.data.processor.data.block.LayeredDataProcessor;
import org.spongepowered.common.data.processor.data.block.MoistureDataProcessor;
import org.spongepowered.common.data.processor.data.block.OccupiedDataProcessor;
import org.spongepowered.common.data.processor.data.block.OpenDataProcessor;
import org.spongepowered.common.data.processor.data.block.PistonDataProcessor;
import org.spongepowered.common.data.processor.data.block.PlantDataProcessor;
import org.spongepowered.common.data.processor.data.block.PortionDataProcessor;
import org.spongepowered.common.data.processor.data.block.PoweredDataProcessor;
import org.spongepowered.common.data.processor.data.block.PrismarineDataProcessor;
import org.spongepowered.common.data.processor.data.block.QuartzDataProcessor;
import org.spongepowered.common.data.processor.data.block.RailDirectionDataProcessor;
import org.spongepowered.common.data.processor.data.block.RedstonePoweredDataProcessor;
import org.spongepowered.common.data.processor.data.block.SandDataProcessor;
import org.spongepowered.common.data.processor.data.block.SandstoneDataProcessor;
import org.spongepowered.common.data.processor.data.block.SeamlessDataProcessor;
import org.spongepowered.common.data.processor.data.block.ShrubDataProcessor;
import org.spongepowered.common.data.processor.data.block.SlabDataProcessor;
import org.spongepowered.common.data.processor.data.block.SnowedDataProcessor;
import org.spongepowered.common.data.processor.data.block.StairShapeDataProcessor;
import org.spongepowered.common.data.processor.data.block.StoneDataProcessor;
import org.spongepowered.common.data.processor.data.block.SuspendedDataProcessor;
import org.spongepowered.common.data.processor.data.block.TreeDataProcessor;
import org.spongepowered.common.data.processor.data.block.WallDataProcessor;
import org.spongepowered.common.data.processor.data.entity.AffectsSpawningDataProcessor;
import org.spongepowered.common.data.processor.data.entity.AgentDataProcessor;
import org.spongepowered.common.data.processor.data.entity.AggressiveDataProcessor;
import org.spongepowered.common.data.processor.data.entity.AngerableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ArtDataProcessor;
import org.spongepowered.common.data.processor.data.entity.BlazeFlammableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.BreedableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.CareerDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ChargedDataProcessor;
import org.spongepowered.common.data.processor.data.entity.CriticalHitDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ElderDataProcessor;
import org.spongepowered.common.data.processor.data.entity.EndermiteExpirableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.EntityPotionDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ExpOrbDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FallDistanceDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FireworkEffectDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FireworkRocketDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FlyingAbilityDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FlyingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.GameModeDataProcessor;
import org.spongepowered.common.data.processor.data.entity.KnockbackDataProcessor;
import org.spongepowered.common.data.processor.data.entity.OcelotDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PersistingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PigSaddleDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PlayerCreatedDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PlayingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PotionEntityPotionDataProcessor;
import org.spongepowered.common.data.processor.data.entity.RabbitDataProcessor;
import org.spongepowered.common.data.processor.data.entity.RepresentedItemDataProcessor;
import org.spongepowered.common.data.processor.data.entity.RespawnLocationDataProcessor;
import org.spongepowered.common.data.processor.data.entity.RotationalDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ScreamingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ShearedDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SittingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SkeletonDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SkinDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SlimeDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SneakingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SprintDataProcessor;
import org.spongepowered.common.data.processor.data.entity.StuckArrowsDataProcessor;
import org.spongepowered.common.data.processor.data.entity.TameableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.TradeOfferDataProcessor;
import org.spongepowered.common.data.processor.data.entity.VelocityDataProcessor;
import org.spongepowered.common.data.processor.data.entity.VillagerZombieProcessor;
import org.spongepowered.common.data.processor.data.entity.WolfWetDataProcessor;
import org.spongepowered.common.data.processor.data.extra.FluidItemDataProcessor;
import org.spongepowered.common.data.processor.data.item.BlockItemDataProcessor;
import org.spongepowered.common.data.processor.data.item.BreakableDataProcessor;
import org.spongepowered.common.data.processor.data.item.CoalDataProcessor;
import org.spongepowered.common.data.processor.data.item.ColoredDataProcessor;
import org.spongepowered.common.data.processor.data.item.CookedFishDataProcessor;
import org.spongepowered.common.data.processor.data.item.FishDataProcessor;
import org.spongepowered.common.data.processor.data.item.GenerationDataProcessor;
import org.spongepowered.common.data.processor.data.item.GoldenAppleDataProcessor;
import org.spongepowered.common.data.processor.data.item.HideDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemAuthorDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemEnchantmentDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemFireworkEffectDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemFireworkRocketDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemLockableDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemLoreDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemPagedDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemPotionDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemSignDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemSkullDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemSkullRepresentedPlayerDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemWetDataProcessor;
import org.spongepowered.common.data.processor.data.item.PlaceableDataProcessor;
import org.spongepowered.common.data.processor.data.item.SpawnableDataProcessor;
import org.spongepowered.common.data.processor.data.item.SplashPotionDataProcessor;
import org.spongepowered.common.data.processor.data.item.StoredEnchantmentDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.BrewingStandDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.CooldownDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.FlowerPotDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.JukeboxDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.NoteDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.SkullRepresentedPlayerDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.SkullRotationDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.TileEntityLockableDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.TileEntitySignDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.TileEntitySkullDataProcessor;
import org.spongepowered.common.data.processor.multi.block.ConnectedDirectionDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.BreathingDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.EntityCommandDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.ExperienceHolderDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.FallingBlockDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.FoodDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.HealthDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.HorseDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.IgniteableDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.InvisibilityDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.JoinDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.MinecartBlockDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.MovementSpeedDataProcessor;
import org.spongepowered.common.data.processor.multi.entity.VehicleDataProcessor;
import org.spongepowered.common.data.processor.multi.item.DurabilityDataProcessor;
import org.spongepowered.common.data.processor.multi.tileentity.FurnaceDataProcessor;
import org.spongepowered.common.data.processor.multi.tileentity.TileConnectedDirectionDataProcessor;
import org.spongepowered.common.data.processor.multi.tileentity.TileEntityBannerDataProcessor;
import org.spongepowered.common.data.processor.multi.tileentity.TileEntityCommandDataProcessor;
import org.spongepowered.common.data.processor.value.DisplayNameVisibleValueProcessor;
import org.spongepowered.common.data.processor.value.ItemDyeColorValueProcessor;
import org.spongepowered.common.data.processor.value.SheepDyeColorValueProcessor;
import org.spongepowered.common.data.processor.value.WolfDyeColorValueProcessor;
import org.spongepowered.common.data.processor.value.block.ConnectedDirectionsValueProcessor;
import org.spongepowered.common.data.processor.value.block.ConnectedEastValueProcessor;
import org.spongepowered.common.data.processor.value.block.ConnectedNorthValueProcessor;
import org.spongepowered.common.data.processor.value.block.ConnectedSouthValueProcessor;
import org.spongepowered.common.data.processor.value.block.ConnectedWestValueProcessor;
import org.spongepowered.common.data.processor.value.entity.BaseVehicleValueProcessor;
import org.spongepowered.common.data.processor.value.entity.CanDropAsItemValueProcessor;
import org.spongepowered.common.data.processor.value.entity.CanPlaceAsBlockValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntityCommandValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntityDisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntityLastCommandOutputValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntitySuccessCountValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntityTracksOutputValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceFromStartOfLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceSinceLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FallHurtAmountValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FallTimeValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FallingBlockCanHurtEntitiesValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FallingBlockStateValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FireDamageDelayValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FireTicksValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FirstJoinValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FlyingSpeedValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodExhaustionValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodSaturationValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HealthValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseColorValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseStyleValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseVariantValueProcessor;
import org.spongepowered.common.data.processor.value.entity.InvisibilityCollisionValueProcessor;
import org.spongepowered.common.data.processor.value.entity.InvisibilityTargetValueProcessor;
import org.spongepowered.common.data.processor.value.entity.InvisibilityValueProcessor;
import org.spongepowered.common.data.processor.value.entity.LastPlayedValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxAirValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxFallDamageValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxHealthValueProcessor;
import org.spongepowered.common.data.processor.value.entity.OffsetValueProcessor;
import org.spongepowered.common.data.processor.value.entity.RemainingAirValueProcessor;
import org.spongepowered.common.data.processor.value.entity.RepresentedBlockValueProcessor;
import org.spongepowered.common.data.processor.value.entity.TotalExperienceValueProcessor;
import org.spongepowered.common.data.processor.value.entity.VehicleValueProcessor;
import org.spongepowered.common.data.processor.value.entity.WalkingSpeedValueProcessor;
import org.spongepowered.common.data.processor.value.item.HideAttributesValueProcessor;
import org.spongepowered.common.data.processor.value.item.HideCanDestroyValueProcessor;
import org.spongepowered.common.data.processor.value.item.HideCanPlaceValueProcessor;
import org.spongepowered.common.data.processor.value.item.HideEnchantmentsValueProcessor;
import org.spongepowered.common.data.processor.value.item.HideMiscellaneousValueProcessor;
import org.spongepowered.common.data.processor.value.item.HideUnbreakableValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemDisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemDurabilityValueProcessor;
import org.spongepowered.common.data.processor.value.item.UnbreakableValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.MaxBurnTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.MaxCookTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.PassedBurnTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.PassedCookTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileBannerBaseColorValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileBannerPatternLayersValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntityCommandValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntityDisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntityLastCommandOutputValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntitySuccessCountValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntityTracksOutputValueProcessor;
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
import org.spongepowered.common.data.property.store.item.ArmorTypePropertyStore;
import org.spongepowered.common.data.property.store.item.BurningFuelPropertyStore;
import org.spongepowered.common.data.property.store.item.DamageAbsorptionPropertyStore;
import org.spongepowered.common.data.property.store.item.EfficiencyPropertyStore;
import org.spongepowered.common.data.property.store.item.EquipmentPropertyStore;
import org.spongepowered.common.data.property.store.item.FoodRestorationPropertyStore;
import org.spongepowered.common.data.property.store.item.HarvestingPropertyStore;
import org.spongepowered.common.data.property.store.item.SaturationPropertyStore;
import org.spongepowered.common.data.property.store.item.ToolTypePropertyStore;
import org.spongepowered.common.data.property.store.item.UseLimitPropertyStore;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.world.storage.SpongePlayerData;

public class DataRegistrar {

    @SuppressWarnings("unchecked")
    public static void setupSerialization(Game game) {
        KeyRegistry.registerKeys();
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

        // Util stuff
        dataManager.registerBuilder(VariableAmount.BaseAndAddition.class, new BaseAndAdditionBuilder());
        dataManager.registerBuilder(VariableAmount.BaseAndVariance.class, new BaseAndVarianceBuilder());
        dataManager.registerBuilder(VariableAmount.Fixed.class, new FixedBuilder());
        dataManager.registerBuilder(VariableAmount.OptionalAmount.class, new OptionalVarianceBuilder());

        dataManager.registerBuilder((Class<Location<World>>) (Class<?>) Location.class, new LocationBuilder());
        dataManager.registerBuilder(SpongePlayerData.class, new SpongePlayerData.Builder());

        // Data Manipulators

        final DisplayNameDataProcessor displayNameDataProcessor = new DisplayNameDataProcessor();
        dataManager.registerDataProcessorAndImpl(DisplayNameData.class, SpongeDisplayNameData.class,
                ImmutableDisplayNameData.class, ImmutableSpongeDisplayNameData.class, displayNameDataProcessor);

        // Entity Processors

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

        dataManager.registerDualProcessor(VillagerZombieData.class, SpongeVillagerZombieData.class, ImmutableVillagerZombieData.class,
                ImmutableSpongeVillagerZombieData.class, new VillagerZombieProcessor());

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

        dataManager.registerDualProcessor(SplashPotionData.class, SpongeSplashPotionData.class, ImmutableSplashPotionData.class,
                ImmutableSpongeSplashPotionData.class, new SplashPotionDataProcessor());

        dataManager.registerDualProcessor(GenerationData.class, SpongeGenerationData.class,
                ImmutableGenerationData.class, ImmutableSpongeGenerationData.class, new GenerationDataProcessor());

        dataManager.registerDualProcessor(StoredEnchantmentData.class, SpongeStoredEnchantmentData.class,
                ImmutableStoredEnchantmentData.class, ImmutableSpongeStoredEnchantmentData.class, new StoredEnchantmentDataProcessor());

        dataManager.registerDualProcessor(FluidItemData.class, SpongeFluidItemData.class, ImmutableFluidItemData.class,
                ImmutableSpongeFluidItemData.class, new FluidItemDataProcessor());

        dataManager.registerDualProcessor(PotionEffectData.class, SpongePotionEffectData.class, ImmutablePotionEffectData.class,
                ImmutableSpongePotionEffectData.class, new ItemPotionDataProcessor());

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

        dataManager.registerDualProcessor(SuspendedData.class, SpongeSuspendedData.class, ImmutableSuspendedData.class,
                ImmutableSpongeSuspendedData.class, new SuspendedDataProcessor());

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

        final DyeableDataProcessor dyeableDataProcessor = new DyeableDataProcessor();
        dataManager.registerDataProcessorAndImpl(DyeableData.class, SpongeDyeableData.class, ImmutableDyeableData.class,
                ImmutableSpongeDyeableData.class, dyeableDataProcessor);


        final HideDataProcessor hideDataProcessor = new HideDataProcessor();
        dataManager.registerDataProcessorAndImpl(HideData.class, SpongeHideData.class, ImmutableHideData.class, ImmutableSpongeHideData.class,
                hideDataProcessor);

        // Values

        dataManager.registerValueProcessor(Keys.HEALTH, new HealthValueProcessor());
        dataManager.registerValueProcessor(Keys.MAX_HEALTH, new MaxHealthValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRE_TICKS, new FireTicksValueProcessor());
        dataManager.registerValueProcessor(Keys.FIRE_DAMAGE_DELAY, new FireDamageDelayValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new ItemDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new TileEntityDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.DISPLAY_NAME, new EntityDisplayNameValueProcessor());
        dataManager.registerValueProcessor(Keys.SHOWS_DISPLAY_NAME, new DisplayNameVisibleValueProcessor());
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
        dataManager.registerValueProcessor(Keys.INVISIBILITY_IGNORES_COLLISION, new InvisibilityCollisionValueProcessor());
        dataManager.registerValueProcessor(Keys.INVISIBILITY_PREVENTS_TARGETING, new InvisibilityTargetValueProcessor());
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
        propertyRegistry.register(ToolTypeProperty.class, new ToolTypePropertyStore());
        propertyRegistry.register(ArmorTypeProperty.class, new ArmorTypePropertyStore());

        // Entities
        propertyRegistry.register(EyeLocationProperty.class, new EyeLocationPropertyStore());
        propertyRegistry.register(EyeHeightProperty.class, new EyeHeightPropertyStore());
    }

}
