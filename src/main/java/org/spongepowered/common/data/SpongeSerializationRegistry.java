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
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedPlayerData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkullData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableBigMushroomData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableBrickData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableComparatorData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirtData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDisguisedBlockData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDoublePlantData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableHingeData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePistonData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePlantData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePortionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePrismarineData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableQuartzData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableRailDirectionData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSandData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSandstoneData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableShrubData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSlabData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableStairShapeData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableStoneData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableWallData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgentData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBreathingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableChargedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableElderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallDistanceData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingAbilityData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFoodData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGameModeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMovementSpeedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePigSaddleData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableScreamingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableShearedData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSittingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSlimeData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVillagerZombieData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableAuthorData;
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
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableCooldownData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableFurnaceData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableNoteData;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.manipulator.mutable.WetData;
import org.spongepowered.api.data.manipulator.mutable.block.BigMushroomData;
import org.spongepowered.api.data.manipulator.mutable.block.BrickData;
import org.spongepowered.api.data.manipulator.mutable.block.ComparatorData;
import org.spongepowered.api.data.manipulator.mutable.block.DirtData;
import org.spongepowered.api.data.manipulator.mutable.block.DisguisedBlockData;
import org.spongepowered.api.data.manipulator.mutable.block.DoublePlantData;
import org.spongepowered.api.data.manipulator.mutable.block.HingeData;
import org.spongepowered.api.data.manipulator.mutable.block.PistonData;
import org.spongepowered.api.data.manipulator.mutable.block.PlantData;
import org.spongepowered.api.data.manipulator.mutable.block.PortionData;
import org.spongepowered.api.data.manipulator.mutable.block.PrismarineData;
import org.spongepowered.api.data.manipulator.mutable.block.QuartzData;
import org.spongepowered.api.data.manipulator.mutable.block.RailDirectionData;
import org.spongepowered.api.data.manipulator.mutable.block.SandData;
import org.spongepowered.api.data.manipulator.mutable.block.SandstoneData;
import org.spongepowered.api.data.manipulator.mutable.block.ShrubData;
import org.spongepowered.api.data.manipulator.mutable.block.SlabData;
import org.spongepowered.api.data.manipulator.mutable.block.StairShapeData;
import org.spongepowered.api.data.manipulator.mutable.block.StoneData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.manipulator.mutable.block.WallData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.data.manipulator.mutable.entity.BreathingData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.ChargedData;
import org.spongepowered.api.data.manipulator.mutable.entity.ElderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallDistanceData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingAbilityData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.MovementSpeedData;
import org.spongepowered.api.data.manipulator.mutable.entity.PigSaddleData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ScreamingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ShearedData;
import org.spongepowered.api.data.manipulator.mutable.entity.SittingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SlimeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VillagerZombieData;
import org.spongepowered.api.data.manipulator.mutable.item.AuthorData;
import org.spongepowered.api.data.manipulator.mutable.item.BreakableData;
import org.spongepowered.api.data.manipulator.mutable.item.CoalData;
import org.spongepowered.api.data.manipulator.mutable.item.CookedFishData;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.FishData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.manipulator.mutable.item.PagedData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.item.PlaceableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.item.SpawnableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.NoteData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
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
import org.spongepowered.common.data.key.KeyRegistry;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeColoredData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeCooldownData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedPlayerData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeSkullData;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeWetData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeBigMushroomData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeBrickData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeComparatorData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirtData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDisguisedBlockData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDoublePlantData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeHingeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePistonData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePlantData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePortionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongePrismarineData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeQuartzData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeRailDirectionData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSandData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSandstoneData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeShrubData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeSlabData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeStairShapeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeStoneData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeWallData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAgentData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBreathingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCareerData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeChargedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeElderData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExpOrbData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFallDistanceData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlyingAbilityData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFlyingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFoodData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeGameModeData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHealthData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHorseData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeIgniteableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeMovementSpeedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePigSaddleData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePlayingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeScreamingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeShearedData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSittingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSlimeData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSneakingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTameableData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVelocityData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVillagerZombieData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeAuthorData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeBreakableData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeCoalData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeCookedFishData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeDurabilityData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeFishData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeLoreData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePagedData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongePlaceableData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBrewingStandData;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeSpawnableData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeFurnaceData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeNoteData;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeSignData;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.manipulator.mutable.SpongeWetData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeBigMushroomData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeBrickData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeComparatorData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirtData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDisguisedBlockData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDoublePlantData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeHingeData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePistonData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePlantData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePortionData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePrismarineData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeQuartzData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeRailDirectionData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSandData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSandstoneData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeShrubData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSlabData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeStairShapeData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeStoneData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeWallData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBreathingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeChargedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeElderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpOrbData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallDistanceData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingAbilityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFoodData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgniteableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMovementSpeedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePigSaddleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeScreamingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeShearedData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSittingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSlimeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSneakingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVillagerZombieData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeAuthorData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBreakableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCoalData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCookedFishData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeDurabilityData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeFishData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeLoreData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePagedData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongePlaceableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBrewingStandData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeSpawnableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeCooldownData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeFurnaceData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeNoteData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.data.ColoredDataProcessor;
import org.spongepowered.common.data.processor.data.DisplayNameDataProcessor;
import org.spongepowered.common.data.processor.data.RepresentedItemDataProcessor;
import org.spongepowered.common.data.processor.data.SkullDataProcessor;
import org.spongepowered.common.data.processor.data.block.BigMushroomDataProcessor;
import org.spongepowered.common.data.processor.data.block.BrickDataProcessor;
import org.spongepowered.common.data.processor.data.block.ComparatorDataProcessor;
import org.spongepowered.common.data.processor.data.block.DirtDataProcessor;
import org.spongepowered.common.data.processor.data.block.DisguisedBlockDataProcessor;
import org.spongepowered.common.data.processor.data.block.DoublePlantDataProcessor;
import org.spongepowered.common.data.processor.data.block.HingeDataProcessor;
import org.spongepowered.common.data.processor.data.block.PistonDataProcessor;
import org.spongepowered.common.data.processor.data.block.PlantDataProcessor;
import org.spongepowered.common.data.processor.data.block.PortionDataProcessor;
import org.spongepowered.common.data.processor.data.block.PrismarineDataProcessor;
import org.spongepowered.common.data.processor.data.block.QuartzDataProcessor;
import org.spongepowered.common.data.processor.data.block.RailDirectionDataProcessor;
import org.spongepowered.common.data.processor.data.block.SandDataProcessor;
import org.spongepowered.common.data.processor.data.block.SandstoneDataProcessor;
import org.spongepowered.common.data.processor.data.block.ShrubDataProcessor;
import org.spongepowered.common.data.processor.data.block.SlabDataProcessor;
import org.spongepowered.common.data.processor.data.block.StairShapeDataProcessor;
import org.spongepowered.common.data.processor.data.block.StoneDataProcessor;
import org.spongepowered.common.data.processor.data.block.TreeDataProcessor;
import org.spongepowered.common.data.processor.data.block.WallDataProcessor;
import org.spongepowered.common.data.processor.data.entity.AgentDataProcessor;
import org.spongepowered.common.data.processor.data.entity.BreathingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.CareerDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ChargedDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ElderDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ExpOrbDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ExperienceHolderDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FallDistanceDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FlyingAbilityDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FlyingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.FoodDataProcessor;
import org.spongepowered.common.data.processor.data.entity.GameModeDataProcessor;
import org.spongepowered.common.data.processor.data.entity.HealthDataProcessor;
import org.spongepowered.common.data.processor.data.entity.HorseDataProcessor;
import org.spongepowered.common.data.processor.data.entity.IgniteableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.MovementSpeedDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PigSaddleDataProcessor;
import org.spongepowered.common.data.processor.data.entity.PlayingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ScreamingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.ShearedDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SittingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SlimeDataProcessor;
import org.spongepowered.common.data.processor.data.entity.SneakingDataProcessor;
import org.spongepowered.common.data.processor.data.entity.TameableDataProcessor;
import org.spongepowered.common.data.processor.data.entity.VelocityDataProcessor;
import org.spongepowered.common.data.processor.data.entity.VillagerZombieProcessor;
import org.spongepowered.common.data.processor.data.entity.WolfWetDataProcessor;
import org.spongepowered.common.data.processor.data.item.BreakableDataProcessor;
import org.spongepowered.common.data.processor.data.item.CoalDataProcessor;
import org.spongepowered.common.data.processor.data.item.CookedFishDataProcessor;
import org.spongepowered.common.data.processor.data.item.DurabilityDataProcessor;
import org.spongepowered.common.data.processor.data.item.FishDataProcessor;
import org.spongepowered.common.data.processor.data.item.GoldenAppleDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemAuthorDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemEnchantmentDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemLoreDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemPagedDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemSkullRepresentedPlayerDataProcessor;
import org.spongepowered.common.data.processor.data.item.ItemWetDataProcessor;
import org.spongepowered.common.data.processor.data.item.PlaceableDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.BrewingStandDataProcessor;
import org.spongepowered.common.data.processor.data.item.SpawnableDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.CooldownDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.FurnaceDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.NoteDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.SignDataProcessor;
import org.spongepowered.common.data.processor.data.tileentity.SkullRepresentedPlayerDataProcessor;
import org.spongepowered.common.data.processor.value.DisplayNameVisibleValueProcessor;
import org.spongepowered.common.data.processor.value.ItemEnchantmentValueProcessor;
import org.spongepowered.common.data.processor.value.RepresentedItemValueProcessor;
import org.spongepowered.common.data.processor.value.block.BigMushroomTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.BrickTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.ComparatorTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.DirtTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.DisguisedBlockTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.DoublePlantTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.HingePositionValueProcessor;
import org.spongepowered.common.data.processor.value.block.PistonTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.PlantTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.PortionTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.PrismarineTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.QuartzTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.RailDirectionValueProcessor;
import org.spongepowered.common.data.processor.value.block.SandTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.SandstoneTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.ShrubTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.SlabTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.StairShapeValueProcessor;
import org.spongepowered.common.data.processor.value.block.StoneTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.TreeTypeValueProcessor;
import org.spongepowered.common.data.processor.value.block.WallTypeValueProcessor;
import org.spongepowered.common.data.processor.value.entity.CanFlyValueProcessor;
import org.spongepowered.common.data.processor.value.entity.CareerValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ElderValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntityDisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.entity.EntityWetValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExpOrbValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceFromStartOfLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ExperienceSinceLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FallDistanceValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FireDamageDelayValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FireTicksValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FlyingSpeedValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodExhaustionValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodLevelValueProcessor;
import org.spongepowered.common.data.processor.value.entity.FoodSaturationValueProcessor;
import org.spongepowered.common.data.processor.value.entity.GameModeValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HealthValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseColorValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseStyleValueProcessor;
import org.spongepowered.common.data.processor.value.entity.HorseVariantValueProcessor;
import org.spongepowered.common.data.processor.value.entity.IsAiEnabledValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ChargedValueProcessor;
import org.spongepowered.common.data.processor.value.entity.IsFlyingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.IsShearedValueProcessor;
import org.spongepowered.common.data.processor.value.entity.IsSittingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxAirValueProcessor;
import org.spongepowered.common.data.processor.value.entity.MaxHealthValueProcessor;
import org.spongepowered.common.data.processor.value.entity.PigSaddleValueProcessor;
import org.spongepowered.common.data.processor.value.entity.PlayingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.RemainingAirValueProcessor;
import org.spongepowered.common.data.processor.value.entity.ScreamingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.SlimeValueProcessor;
import org.spongepowered.common.data.processor.value.entity.SneakingValueProcessor;
import org.spongepowered.common.data.processor.value.entity.TameableOwnerValueProcessor;
import org.spongepowered.common.data.processor.value.entity.TotalExperienceValueProcessor;
import org.spongepowered.common.data.processor.value.entity.VelocityValueProcessor;
import org.spongepowered.common.data.processor.value.entity.VillagerZombieValueProcessor;
import org.spongepowered.common.data.processor.value.entity.WalkingSpeedValueProcessor;
import org.spongepowered.common.data.processor.value.item.BookAuthorValueProcessor;
import org.spongepowered.common.data.processor.value.item.BookPagesValueProcessor;
import org.spongepowered.common.data.processor.value.item.BreakableValueProcessor;
import org.spongepowered.common.data.processor.value.item.CoalValueProcessor;
import org.spongepowered.common.data.processor.value.item.CookedFishValueProcessor;
import org.spongepowered.common.data.processor.value.item.FishValueProcessor;
import org.spongepowered.common.data.processor.value.item.GoldenAppleValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemColorValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemDisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemDurabilityValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemLoreValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemSkullRepresentedPlayerValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemSkullValueProcessor;
import org.spongepowered.common.data.processor.value.item.ItemWetValueProcessor;
import org.spongepowered.common.data.processor.value.item.PlaceableValueProcessor;
import org.spongepowered.common.data.processor.value.item.UnbreakableValueProcessor;
import org.spongepowered.common.data.processor.value.item.SpawnableEntityTypeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.CooldownValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.MaxBurnTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.MaxCookTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.NoteValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.PassedBurnTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.PassedCookTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.RemainingBrewTimeValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.SignLinesValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.SkullRepresentedPlayerProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntityDisplayNameValueProcessor;
import org.spongepowered.common.data.processor.value.tileentity.TileEntitySkullValueProcessor;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.service.persistence.SpongeSerializationService;

public class SpongeSerializationRegistry {

    public static void setupSerialization(Game game) {
        KeyRegistry.registerKeys();
        SpongeSerializationService service = SpongeSerializationService.getInstance();
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
        service.registerBuilderAndImpl(ImmutableEnchantmentData.class, ImmutableSpongeEnchantmentData.class,
                new ImmutableItemEnchantmentDataBuilder());

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
        dataRegistry.registerDataProcessorAndImpl(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class, wolfWetDataProcessor);
        dataRegistry.registerDataProcessorAndImpl(WetData.class, SpongeWetData.class, ImmutableWetData.class, ImmutableSpongeWetData.class, itemWetDataProcessor);

        final ElderDataProcessor elderDataProcessor = new ElderDataProcessor();
        dataRegistry.registerDataProcessorAndImpl(ElderData.class, SpongeElderData.class, ImmutableElderData.class, ImmutableSpongeElderData.class, elderDataProcessor);

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
    }

}
