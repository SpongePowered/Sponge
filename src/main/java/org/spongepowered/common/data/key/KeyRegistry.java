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
package org.spongepowered.common.data.key;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeListKey;
import static org.spongepowered.api.data.key.KeyFactory.makeMapKey;
import static org.spongepowered.api.data.key.KeyFactory.makeSetKey;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.type.BigMushroomType;
import org.spongepowered.api.data.type.BrickType;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.CoalType;
import org.spongepowered.api.data.type.ComparatorType;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.DirtType;
import org.spongepowered.api.data.type.DisguisedBlockType;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Fish;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.type.PistonType;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PrismarineType;
import org.spongepowered.api.data.type.QuartzType;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.SandType;
import org.spongepowered.api.data.type.SandstoneType;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StoneType;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.WallType;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.registry.RegistryHelper;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;

public class KeyRegistry {

    private static final Map<String, Key<?>> keyMap = new MapMaker().concurrencyLevel(4).makeMap();

    public static void registerKeys() {
        generateKeyMap();
        RegistryHelper.mapFieldsIgnoreWarning(Keys.class, keyMap);
    }

    private static void generateKeyMap() {
        if (!keyMap.isEmpty()) {
            return;
        }
        keyMap.put("axis", makeSingleKey(Axis.class, Value.class, of("Axis")));
        keyMap.put("color", makeSingleKey(Color.class, Value.class, of("Color")));
        keyMap.put("health", makeSingleKey(Double.class, MutableBoundedValue.class, of("Health")));
        keyMap.put("max_health", makeSingleKey(Double.class, MutableBoundedValue.class, of("MaxHealth")));
        keyMap.put("shows_display_name", makeSingleKey(Boolean.class, Value.class, of("ShowDisplayName")));
        keyMap.put("display_name", makeSingleKey(Text.class, Value.class, of("DisplayName")));
        keyMap.put("career", makeSingleKey(Career.class, Value.class, of("Career")));
        keyMap.put("sign_lines", makeListKey(Text.class, of("SignLines")));
        keyMap.put("skull_type", makeSingleKey(SkullType.class, Value.class, of("SkullType")));
        keyMap.put("is_sneaking", makeSingleKey(Boolean.class, Value.class, of("IsSneaking")));
        keyMap.put("velocity", makeSingleKey(Vector3d.class, Value.class, of("Velocity")));
        keyMap.put("food_level", makeSingleKey(Integer.class, Value.class, of("FoodLevel")));
        keyMap.put("saturation", makeSingleKey(Double.class, Value.class, of("FoodSaturationLevel")));
        keyMap.put("exhaustion", makeSingleKey(Double.class, Value.class, of("FoodExhaustionLevel")));
        keyMap.put("max_air", makeSingleKey(Integer.class, Value.class, of("MaxAir")));
        keyMap.put("remaining_air", makeSingleKey(Integer.class, Value.class, of("RemainingAir")));
        keyMap.put("fire_ticks", makeSingleKey(Integer.class, MutableBoundedValue.class, of("FireTicks")));
        keyMap.put("fire_damage_delay", makeSingleKey(Integer.class, MutableBoundedValue.class, of("FireDamageDelay")));
        keyMap.put("game_mode", makeSingleKey(GameMode.class, Value.class, of("GameMode")));
        keyMap.put("is_screaming", makeSingleKey(Boolean.class, Value.class, of("IsScreaming")));
        keyMap.put("can_fly", makeSingleKey(Boolean.class, Value.class, of("CanFly")));
        keyMap.put("shrub_type", makeSingleKey(ShrubType.class, Value.class, of("ShrubType")));
        keyMap.put("plant_type", makeSingleKey(PlantType.class, Value.class, of("PlantType")));
        keyMap.put("tree_type", makeSingleKey(TreeType.class, Value.class, of("TreeType")));
        keyMap.put("log_axis", makeSingleKey(LogAxis.class, Value.class, of("LogAxis")));
        keyMap.put("invisible", makeSingleKey(Boolean.class, Value.class, of("Invisible")));
        keyMap.put("powered", makeSingleKey(Boolean.class, Value.class, of("Powered")));
        keyMap.put("layer", makeSingleKey(Integer.class, MutableBoundedValue.class, of("Layer")));
        keyMap.put("represented_item", makeSingleKey(ItemStackSnapshot.class, Value.class, of("ItemStackSnapshot")));
        keyMap.put("command", makeSingleKey(String.class, Value.class, of("Command")));
        keyMap.put("success_count", makeSingleKey(Integer.class, Value.class, of("SuccessCount")));
        keyMap.put("tracks_output", makeSingleKey(Boolean.class, Value.class, of("TracksOutput")));
        keyMap.put("last_command_output", makeSingleKey(Text.class, OptionalValue.class, of("LastCommandOutput")));
        keyMap.put("trade_offers", makeListKey(TradeOffer.class, of("TradeOffers")));
        keyMap.put("dye_color", makeSingleKey(DyeColor.class, Value.class, of("DyeColor")));
        keyMap.put("firework_flight_modifier", makeSingleKey(Integer.class, BoundedValue.class, of("FlightModifier")));
        keyMap.put("firework_effects", makeListKey(FireworkEffect.class, of("FireworkEffects")));
        keyMap.put("spawner_remaining_delay", makeSingleKey(Short.class, MutableBoundedValue.class, of("SpawnerRemainingDelay")));
        keyMap.put("spawner_minimum_delay", makeSingleKey(Short.class, MutableBoundedValue.class, of("SpawnerMinimumDelay")));
        keyMap.put("connected_directions", makeSetKey(Direction.class, of("ConnectedDirections")));
        keyMap.put("connected_north", makeSingleKey(Boolean.class, Value.class, of("ConnectedNorth")));
        keyMap.put("connected_south", makeSingleKey(Boolean.class, Value.class, of("ConnectedSouth")));
        keyMap.put("connected_east", makeSingleKey(Boolean.class, Value.class, of("ConnectedEast")));
        keyMap.put("connected_west", makeSingleKey(Boolean.class, Value.class, of("ConnectedWest")));
        keyMap.put("direction", makeSingleKey(Direction.class, Value.class, of("Direction")));
        keyMap.put("dirt_type", makeSingleKey(DirtType.class, Value.class, of("DirtType")));
        keyMap.put("disguised_block_type", makeSingleKey(DisguisedBlockType.class, Value.class, of("DisguisedBlockType")));
        keyMap.put("disarmed", makeSingleKey(Boolean.class, Value.class, of("Disarmed")));
        keyMap.put("item_enchantments", makeListKey(ItemEnchantment.class, of("ItemEnchantments")));
        keyMap.put("banner_patterns", makeListKey(PatternLayer.class, of("BannerPatterns")));
        keyMap.put("banner_base_color", makeListKey(DyeColor.class, of("BannerBaseColor")));
        keyMap.put("horse_color", makeSingleKey(HorseColor.class, Value.class, of("HorseColor")));
        keyMap.put("horse_style", makeSingleKey(HorseStyle.class, Value.class, of("HorseStyle")));
        keyMap.put("horse_variant", makeSingleKey(HorseVariant.class, Value.class, of("HorseVariant")));
        keyMap.put("item_lore", makeListKey(Text.class, of("ItemLore")));
        keyMap.put("book_pages", makeListKey(Text.class, of("BookPages")));
        keyMap.put("golden_apple_type", makeSingleKey(GoldenApple.class, Value.class, of("GoldenAppleType")));
        keyMap.put("is_flying", makeSingleKey(Boolean.class, Value.class, of("IsFlying")));
        keyMap.put("experience_level", makeSingleKey(Integer.class, MutableBoundedValue.class, of("ExperienceLevel")));
        keyMap.put("total_experience", makeSingleKey(Integer.class, MutableBoundedValue.class, of("TotalExperience")));
        keyMap.put("experience_since_level", makeSingleKey(Integer.class, MutableBoundedValue.class, of("ExperienceSinceLevel")));
        keyMap.put("experience_from_start_of_level", makeSingleKey(Integer.class, ImmutableBoundedValue.class, of("ExperienceFromStartOfLevel")));
        keyMap.put("book_author", makeSingleKey(Text.class, Value.class, of("BookAuthor")));
        keyMap.put("breakable_block_types", makeSetKey(BlockType.class, of("CanDestroy")));
        keyMap.put("placeable_blocks", makeSetKey(BlockType.class, of("CanPlaceOn")));
        keyMap.put("walking_speed", makeSingleKey(Double.class, Value.class, of("WalkingSpeed")));
        keyMap.put("flying_speed", makeSingleKey(Double.class, Value.class, of("FlyingSpeed")));
        keyMap.put("slime_size", makeSingleKey(Integer.class, MutableBoundedValue.class, of("SlimeSize")));
        keyMap.put("is_villager_zombie", makeSingleKey(Boolean.class, Value.class, of("IsVillagerZombie")));
        keyMap.put("is_playing", makeSingleKey(Boolean.class, Value.class, of("IsPlaying")));
        keyMap.put("is_sitting", makeSingleKey(Boolean.class, Value.class, of("IsSitting")));
        keyMap.put("is_sheared", makeSingleKey(Boolean.class, Value.class, of("IsSheared")));
        keyMap.put("pig_saddle", makeSingleKey(Boolean.class, Value.class, of("IsPigSaddled")));
        keyMap.put("tamed_owner", makeSingleKey(UUID.class, OptionalValue.class, of("TamerUUID")));
        keyMap.put("is_wet", makeSingleKey(Boolean.class, Value.class, of("IsWet")));
        keyMap.put("elder_guardian", makeSingleKey(Boolean.class, Value.class, of("Elder")));
        keyMap.put("coal_type", makeSingleKey(CoalType.class, Value.class, of("CoalType")));
        keyMap.put("cooked_fish", makeSingleKey(CookedFish.class, Value.class, of("CookedFishType")));
        keyMap.put("fish_type", makeSingleKey(Fish.class, Value.class, of("RawFishType")));
        keyMap.put("represented_player", makeSingleKey(GameProfile.class, Value.class, of("RepresentedPlayer")));
        keyMap.put("passed_burn_time", makeSingleKey(Integer.class, MutableBoundedValue.class, of("PassedBurnTime")));
        keyMap.put("max_burn_time", makeSingleKey(Integer.class, MutableBoundedValue.class, of("MaxBurnTime")));
        keyMap.put("passed_cook_time", makeSingleKey(Integer.class, MutableBoundedValue.class, of("PassedCookTime")));
        keyMap.put("max_cook_time", makeSingleKey(Integer.class, MutableBoundedValue.class, of("MaxCookTime")));
        keyMap.put("contained_experience", makeSingleKey(Integer.class, Value.class, of("ContainedExperience")));
        keyMap.put("remaining_brew_time", makeSingleKey(Integer.class, MutableBoundedValue.class, of("RemainingBrewTime")));
        keyMap.put("stone_type", makeSingleKey(StoneType.class, Value.class, of("StoneType")));
        keyMap.put("prismarine_type", makeSingleKey(PrismarineType.class, Value.class, of("PrismarineType")));
        keyMap.put("brick_type", makeSingleKey(BrickType.class, Value.class, of("BrickType")));
        keyMap.put("quartz_type", makeSingleKey(QuartzType.class, Value.class, of("QuartzType")));
        keyMap.put("sand_type", makeSingleKey(SandType.class, Value.class, of("SandType")));
        keyMap.put("sandstone_type", makeSingleKey(SandstoneType.class, Value.class, of("SandstoneType")));
        keyMap.put("slab_type", makeSingleKey(SlabType.class, Value.class, of("SlabType")));
        keyMap.put("sandstone_type", makeSingleKey(SandstoneType.class, Value.class, of("SandstoneType")));
        keyMap.put("comparator_type", makeSingleKey(ComparatorType.class, Value.class, of("ComparatorType")));
        keyMap.put("hinge_position", makeSingleKey(Hinge.class, Value.class, of("HingePosition")));
        keyMap.put("piston_type", makeSingleKey(PistonType.class, Value.class, of("PistonType")));
        keyMap.put("portion_type", makeSingleKey(PortionType.class, Value.class, of("PortionType")));
        keyMap.put("rail_direction", makeSingleKey(RailDirection.class, Value.class, of("RailDirection")));
        keyMap.put("stair_shape", makeSingleKey(StairShape.class, Value.class, of("StairShape")));
        keyMap.put("wall_type", makeSingleKey(WallType.class, Value.class, of("WallType")));
        keyMap.put("double_plant_type", makeSingleKey(DoublePlantType.class, Value.class, of("DoublePlantType")));
        keyMap.put("big_mushroom_type", makeSingleKey(BigMushroomType.class, Value.class, of("BigMushroomType")));
        keyMap.put("ai_enabled", makeSingleKey(Boolean.class, Value.class, of("IsAiEnabled")));
        keyMap.put("creeper_charged", makeSingleKey(Boolean.class, Value.class, of("IsCreeperCharged")));
        keyMap.put("item_durability", makeSingleKey(Integer.class, MutableBoundedValue.class, of("ItemDurability")));
        keyMap.put("unbreakable", makeSingleKey(Boolean.class, Value.class, of("Unbreakable")));
        keyMap.put("spawnable_entity_type", makeSingleKey(EntityType.class, Value.class, of("SpawnableEntityType")));
        keyMap.put("fall_distance", makeSingleKey(Float.class, MutableBoundedValue.class, of("FallDistance")));
        keyMap.put("cooldown", makeSingleKey(Integer.class, Value.class, of("Cooldown")));
        keyMap.put("note_pitch", makeSingleKey(NotePitch.class, Value.class, of("Note")));
        keyMap.put("vehicle", makeSingleKey(EntitySnapshot.class, Value.class, of("Vehicle")));
        keyMap.put("base_vehicle", makeSingleKey(EntitySnapshot.class, Value.class, of("BaseVehicle")));
        keyMap.put("art", makeSingleKey(Art.class, Value.class, of("Art")));
        keyMap.put("target", makeSingleKey(Living.class, Value.class, of("Target")));
        keyMap.put("targets", makeSingleKey(Living.class, ListValue.class, of("Targets")));
        keyMap.put("fall_damage_per_block", makeSingleKey(Double.class, Value.class, of("FallDamagePerBlock")));
        keyMap.put("max_fall_damage", makeSingleKey(Double.class, Value.class, of("MaxFallDamage")));
        keyMap.put("falling_block_state", makeSingleKey(BlockState.class, Value.class, of("FallingBlockState")));
        keyMap.put("can_place_as_block", makeSingleKey(Boolean.class, Value.class, of("CanPlaceAsBlock")));
        keyMap.put("can_drop_as_item", makeSingleKey(Boolean.class, Value.class, of("CanDropAsItem")));
        keyMap.put("fall_time", makeSingleKey(Integer.class, Value.class, of("FallTime")));
        keyMap.put("falling_block_can_hurt_entities", makeSingleKey(Boolean.class, Value.class, of("CanFallingBlockHurtEntities")));
        keyMap.put("represented_block", makeSingleKey(BlockState.class, Value.class, of("RepresentedBlock")));
        keyMap.put("offset", makeSingleKey(Integer.class, Value.class, of("BlockOffset")));
        keyMap.put("attached", makeSingleKey(Boolean.class, Value.class, of("Attached")));
        keyMap.put("should_drop", makeSingleKey(Boolean.class, Value.class, of("ShouldDrop")));
        keyMap.put("extended", makeSingleKey(Boolean.class, Value.class, of("Extended")));
        keyMap.put("growth_stage", makeSingleKey(Integer.class, MutableBoundedValue.class, of("GrowthStage")));
        keyMap.put("open", makeSingleKey(Boolean.class, Value.class, of("Open")));
        keyMap.put("power", makeSingleKey(Integer.class, MutableBoundedValue.class, of("Power")));
        keyMap.put("seamless", makeSingleKey(Boolean.class, Value.class, of("Seamless")));
        keyMap.put("snowed", makeSingleKey(Boolean.class, Value.class, of("Snowed")));
        keyMap.put("suspended", makeSingleKey(Boolean.class, Value.class, of("Suspended")));
        keyMap.put("occupied", makeSingleKey(Boolean.class, Value.class, of("Occupied")));
        keyMap.put("decayable", makeSingleKey(Boolean.class, Value.class, of("Decayable")));
        keyMap.put("in_wall", makeSingleKey(Boolean.class, Value.class, of("InWall")));
        keyMap.put("delay", makeSingleKey(Integer.class, MutableBoundedValue.class, of("Delay")));
        keyMap.put("player_created", makeSingleKey(Boolean.class, Value.class, of("PlayerCreated")));
        keyMap.put("item_blockstate", makeSingleKey(BlockState.class, Value.class, of("ItemBlockState")));
        keyMap.put("skeleton_type", makeSingleKey(SkeletonType.class, Value.class, of("SkeletonType")));
        keyMap.put("ocelot_type", makeSingleKey(OcelotType.class, Value.class, of("OcelotType")));
        keyMap.put("rabbit_type", makeSingleKey(RabbitType.class, Value.class, of("RabbitType")));
        keyMap.put("lock_token", makeSingleKey(String.class, Value.class, of("Lock")));
        keyMap.put("banner_base_color", makeSingleKey(DyeColor.class, Value.class, of("BannerBaseColor")));
        keyMap.put("banner_patterns", new PatternKey());
        keyMap.put("respawn_locations", makeMapKey(UUID.class, Vector3d.class, of("RespawnLocations")));
        keyMap.put("expiration_ticks", makeSingleKey(Integer.class, MutableBoundedValue.class, of("ExpirationTicks")));
        keyMap.put("skin_unique_id", makeSingleKey(UUID.class, Value.class, of("SkinUUID")));
        keyMap.put("moisture", makeSingleKey(Integer.class, MutableBoundedValue.class, of("Moisture")));
        keyMap.put("angry", makeSingleKey(Boolean.class, Value.class, of("Angry")));
        keyMap.put("anger", makeSingleKey(Integer.class, MutableBoundedValue.class, of("Anger")));
        keyMap.put("rotation", makeSingleKey(Rotation.class, Value.class, of("Rotation")));
        keyMap.put("is_splash_potion", makeSingleKey(Boolean.class, Value.class, of("IsSplashPotion")));
        keyMap.put("affects_spawning", makeSingleKey(Boolean.class, Value.class, of("AffectsSpawning")));
        keyMap.put("critical_hit", makeSingleKey(Boolean.class, Value.class, of("CriticalHit")));
        keyMap.put("generation", makeSingleKey(Integer.class, MutableBoundedValue.class, of("Generation")));
        keyMap.put("passenger", makeSingleKey(EntitySnapshot.class, Value.class, of("PassengerSnapshot")));
        keyMap.put("knockback_strength", makeSingleKey(Integer.class, MutableBoundedValue.class, of("KnockbackStrength")));
        keyMap.put("persists", makeSingleKey(Boolean.class, Value.class, of("Persists")));
        keyMap.put("stored_enchantments", makeListKey(ItemEnchantment.class, of("StoredEnchantments")));
        keyMap.put("is_sprinting", makeSingleKey(Boolean.class, Value.class, of("Sprinting")));
        keyMap.put("stuck_arrows", makeSingleKey(Integer.class, MutableBoundedValue.class, of("StuckArrows")));
        keyMap.put("invisibility_ignores_collision", makeSingleKey(Boolean.class, Value.class, of("InvisiblityIgnoresCollision")));
        keyMap.put("invisibility_prevents_targeting", makeSingleKey(Boolean.class, Value.class, of("InvisibilityPreventsTargeting")));
    }

    @SuppressWarnings("unused") // Used in DataTestUtil.generateKeyMap
    private static Map<String, Key<?>> getKeyMap() {
        generateKeyMap();
        return keyMap;
    }

    private static final class PatternKey implements Key<PatternListValue> {

        @Override
        public Class<PatternListValue> getValueClass() {
            return PatternListValue.class;
        }

        @Override
        public DataQuery getQuery() {
            return of("BannerPatterns");
        }
    }
}
