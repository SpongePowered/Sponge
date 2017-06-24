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
package org.spongepowered.common.registry.type.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.api.data.key.KeyFactory.makeListKey;
import static org.spongepowered.api.data.key.KeyFactory.makeMapKey;
import static org.spongepowered.api.data.key.KeyFactory.makeOptionalKey;
import static org.spongepowered.api.data.key.KeyFactory.makeSetKey;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.data.SpongeDataManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"rawTypes", "deprecation"})
public class KeyRegistryModule implements AdditionalCatalogRegistryModule<Key<?>> {

    public static KeyRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    // this map is used for mapping the field names to the actual class, since the id's are generated specially through the factory methods.
    @RegisterCatalog(Keys.class)
    private final Map<String, Key<?>> fieldMap = new MapMaker().concurrencyLevel(4).makeMap();

    // This map is the one used for catalog id lookups.
    private final Map<String, Key<?>> keyMap = new MapMaker().concurrencyLevel(4).makeMap();

    @Override
    public void registerDefaults() {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Attempting to register Keys illegally!");

        this.fieldMap.put("axis", makeSingleKey(TypeTokens.AXIS_TOKEN, TypeTokens.AXIS_VALUE_TOKEN, of("Axis"), "sponge:axis", "Axis"));

        this.fieldMap.put("color", makeSingleKey(TypeTokens.COLOR_TOKEN, TypeTokens.COLOR_VALUE_TOKEN, of("Color"), "sponge:color", "Color"));

        this.fieldMap.put("health", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("Health"), "sponge:health", "Health"));

        this.fieldMap.put("max_health", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("MaxHealth"), "sponge:max_health", "Max Health"));

        this.fieldMap.put("display_name", makeSingleKey(TypeTokens.TEXT_TOKEN, TypeTokens.TEXT_VALUE_TOKEN, of("DisplayName"), "sponge:display_name", "Display Name"));

        this.fieldMap.put("career", makeSingleKey(TypeTokens.CAREER_TOKEN, TypeTokens.CAREER_VALUE_TOKEN, of("Career"), "sponge:career", "Career"));

        this.fieldMap.put("sign_lines", makeListKey(TypeTokens.LIST_TEXT_TOKEN, TypeTokens.LIST_TEXT_VALUE_TOKEN, of("SignLines"), "sponge:sign_lines", "Sign Lines"));

        this.fieldMap.put("skull_type", makeSingleKey(TypeTokens.SKULL_TOKEN, TypeTokens.SKULL_VALUE_TOKEN, of("SkullType"), "sponge:skull_type", "Skull Type"));

        this.fieldMap.put("is_sneaking", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSneaking"), "sponge:sneaking", "Is Sneaking"));

        this.fieldMap.put("velocity", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("Velocity"), "sponge:velocity", "Velocity"));

        this.fieldMap.put("food_level", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("FoodLevel"), "sponge:food_level", "Food Level"));

        this.fieldMap.put("saturation", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("FoodSaturationLevel"), "sponge:food_saturation_level", "Food Saturation Level"));

        this.fieldMap.put("exhaustion", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("FoodExhaustionLevel"), "sponge:food_exhaustion_level", ""));

        this.fieldMap.put("max_air", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("MaxAir"), "sponge:max_air", "Max Air"));

        this.fieldMap.put("remaining_air", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("RemainingAir"), "sponge:remaining_air", "Remaining Air"));

        this.fieldMap.put("fire_ticks", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("FireTicks"), "sponge:fire_ticks", "Fire Ticks"));

        this.fieldMap.put("fire_damage_delay", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("FireDamageDelay"), "sponge:fire_damage_delay", "Fire Damage Delay"));

        this.fieldMap.put("game_mode", makeSingleKey(TypeTokens.GAME_MODE_TOKEN, TypeTokens.GAME_MODE_VALUE_TOKEN, of("GameMode"), "sponge:game_mode", "Game Mode"));

        this.fieldMap.put("is_screaming", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsScreaming"), "sponge:screaming", "Is Screaming"));

        this.fieldMap.put("can_fly", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CanFly"), "sponge:can_fly", "Can Fly"));

        this.fieldMap.put("can_grief", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CanGrief"), "sponge:can_grief", "Can Grief"));

        this.fieldMap.put("shrub_type", makeSingleKey(TypeTokens.SHRUB_TOKEN, TypeTokens.SHRUB_VALUE_TOKEN, of("ShrubType"), "sponge:shrub_type", "Shrub Type"));

        this.fieldMap.put("plant_type", makeSingleKey(TypeTokens.PLANT_TOKEN, TypeTokens.PLANT_VALUE_TOKEN, of("PlantType"), "sponge:plant_type", "Plant Type"));

        this.fieldMap.put("tree_type", makeSingleKey(TypeTokens.TREE_TOKEN, TypeTokens.TREE_VALUE_TOKEN, of("TreeType"), "sponge:tree_type", "Tree Type"));

        this.fieldMap.put("log_axis", makeSingleKey(TypeTokens.LOG_AXIS_TOKEN, TypeTokens.LOG_AXIS_VALUE_TOKEN, of("LogAxis"), "sponge:log_axis", "Log Axis"));

        this.fieldMap.put("invisible", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Invisible"), "sponge:invisible", "Invisible"));

        this.fieldMap.put("vanish", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Vanish"), "sponge:vanish", "Vanish"));

        this.fieldMap.put("invisible", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Invisible"), "sponge:invisible", "Invisible"));

        this.fieldMap.put("powered", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Powered"), "sponge:powered", "Powered"));

        this.fieldMap.put("layer", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Layer"), "sponge:layer", "Layer"));

        this.fieldMap.put("represented_item", makeSingleKey(TypeTokens.ITEM_SNAPSHOT_TOKEN, TypeTokens.ITEM_SNAPSHOT_VALUE_TOKEN, of("ItemStackSnapshot"), "sponge:item_stack_snapshot", "Item Stack Snapshot"));

        this.fieldMap.put("command", makeSingleKey(TypeTokens.STRING_TOKEN, TypeTokens.STRING_VALUE_TOKEN, of("Command"), "sponge:command", "Command"));

        this.fieldMap.put("success_count", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("SuccessCount"), "sponge:success_count", "SuccessCount"));

        this.fieldMap.put("tracks_output", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("TracksOutput"), "sponge:tracks_output", "Tracks Output"));

        this.fieldMap.put("last_command_output", makeOptionalKey(TypeTokens.OPTIONAL_TEXT_TOKEN, TypeTokens.OPTIONAL_TEXT_VALUE_TOKEN, of("LastCommandOutput"), "sponge:last_command_output", "Last Command Output"));

        this.fieldMap.put("trade_offers", makeListKey(TypeTokens.LIST_TRADE_OFFER_TOKEN, TypeTokens.LIST_VALUE_TRADE_OFFER_TOKEN, of("TradeOffers"), "sponge:trade_offers", "Trade Offers"));

        this.fieldMap.put("dye_color", makeSingleKey(TypeTokens.DYE_COLOR_TOKEN, TypeTokens.DYE_COLOR_VALUE_TOKEN, of("DyeColor"), "sponge:dye_color", "Dye Color"));

        this.fieldMap.put("firework_flight_modifier", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("FlightModifier"), "sponge:flight_modifier", "Flight Modifier"));

        this.fieldMap.put("firework_effects", makeListKey(TypeTokens.LIST_FIREWORK_TOKEN, TypeTokens.LIST_VALUE_FIREWORK_TOKEN, of("FireworkEffects"), "sponge:firework_effects", "Firework Effects"));

        this.fieldMap.put("spawner_entities", makeSingleKey(
            TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_TABLE_TOKEN, TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_COLLECTION_VALUE_TOKEN,
                of("SpawnerEntities"), "sponge:spawner_entities", "Spawner Entities"));

        this.fieldMap.put("spawner_maximum_delay", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerMaximumDelay"),
                "sponge:spawner_maximum_delay", "Spawner Maximum Delay"));

        this.fieldMap.put("spawner_maximum_nearby_entities", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of
                ("SpawnerMaximumNearbyEntities"), "sponge:spawner_maximum_nearby_entities", "Spawner Maximum Nearby Entities"));

        this.fieldMap.put("spawner_minimum_delay", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerMinimumDelay"),
                "sponge:spawner_minimum_delay", "Spawner Minimum Delay"));

        this.fieldMap.put("spawner_next_entity_to_spawn", makeSingleKey(
            TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_TOKEN, TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_VALUE_TOKEN,
                of("SpawnerNextEntityToSpawn"), "sponge:spawner_next_entity_to_spawn", "Spawner Next Entity To Spawn"));

        this.fieldMap.put("spawner_remaining_delay", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerRemainingDelay"),
                "sponge:spawner_remaining_delay", "Spawner Remaining Delay"));

        this.fieldMap.put("spawner_required_player_range", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerRequiredPlayerRange"),
                "sponge:spawner_required_player_range", "Spawner Required Player Range"));

        this.fieldMap.put("spawner_spawn_count", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerSpawnCount"),
                "sponge:spawner_spawn_count", "Spawner Spawn Count"));

        this.fieldMap.put("spawner_spawn_range", makeSingleKey(TypeTokens.SHORT_TOKEN, TypeTokens.BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerSpawnRange"),
                "sponge:spawner_spawn_range", "Spawner Spawn Range"));

        this.fieldMap.put("connected_directions", makeSetKey(TypeTokens.SET_DIRECTION_TOKEN, TypeTokens.SET_DIRECTION_VALUE_TOKEN, of("ConnectedDirections"), "sponge:connected_directions", "Connected Directions"));

        this.fieldMap.put("connected_north", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("ConnectedNorth"), "sponge:connected_north", "Connected North"));

        this.fieldMap.put("connected_south", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("ConnectedSouth"), "sponge:connected_south", "Connected South"));

        this.fieldMap.put("connected_east", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("ConnectedEast"), "sponge:connected_east", "Connected East"));

        this.fieldMap.put("connected_west", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("ConnectedWest"), "sponge:connected_west", "Connected West"));

        this.fieldMap.put("direction", makeSingleKey(TypeTokens.DIRECTION_TOKEN, TypeTokens.DIRECTION_VALUE_TOKEN, of("Direction"), "sponge:direction", "Direction"));

        this.fieldMap.put("dirt_type", makeSingleKey(TypeTokens.DIRT_TOKEN, TypeTokens.DIRT_VALUE_TOKEN, of("DirtType"), "sponge:dirt_type", "Dirt Type"));

        this.fieldMap.put("disguised_block_type", makeSingleKey(TypeTokens.DISGUISED_BLOCK_TOKEN, TypeTokens.DISGUISED_BLOCK_VALUE_TOKEN, of("DisguisedBlockType"), "sponge:disguised_block_type", "Disguised Block Type"));

        this.fieldMap.put("disarmed", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Disarmed"), "sponge:disarmed", "Disarmed"));

        this.fieldMap.put("item_enchantments", makeListKey(TypeTokens.LIST_ITEM_ENCHANTMENT_TOKEN, TypeTokens.LIST_ITEM_ENCHANTMENT_VALUE_TOKEN, of("ItemEnchantments"), "sponge:item_enchantments", "Item Enchantments"));

        this.fieldMap.put("banner_patterns", makeListKey(TypeTokens.LIST_PATTERN_TOKEN, TypeTokens.LIST_PATTERN_VALUE_TOKEN, of("BannerPatterns"), "sponge:banner_patterns", "Banner Patterns"));

        this.fieldMap.put("banner_base_color", makeListKey(TypeTokens.LIST_DYE_COLOR_TOKEN, TypeTokens.LIST_DYE_COLOR_VALUE_TOKEN, of("BannerBaseColor"), "sponge:banner_base_color", "Banner Base Color"));

        this.fieldMap.put("horse_color", makeSingleKey(TypeTokens.HORSE_COLOR_TOKEN, TypeTokens.HORSE_COLOR_VALUE_TOKEN, of("HorseColor"), "sponge:horse_color", "Horse Color"));

        this.fieldMap.put("horse_style", makeSingleKey(TypeTokens.HORSE_STYLE_TOKEN, TypeTokens.HORSE_STYLE_VALUE_TOKEN, of("HorseStyle"), "sponge:horse_style", "Horse Style"));

        this.fieldMap.put("horse_variant", makeSingleKey(TypeTokens.HORSE_VARIANT_TOKEN, TypeTokens.HORSE_VARIANT_VALUE_TOKEN, of("HorseVariant"), "sponge:horse_variant", "Horse Variant"));

        this.fieldMap.put("item_lore", makeListKey(TypeTokens.LIST_TEXT_TOKEN, TypeTokens.LIST_TEXT_VALUE_TOKEN, of("ItemLore"), "sponge:item_lore", "Item Lore"));

        this.fieldMap.put("book_pages", makeListKey(TypeTokens.LIST_TEXT_TOKEN, TypeTokens.LIST_TEXT_VALUE_TOKEN, of("BookPages"), "sponge:book_pages", "Book Pages"));

        this.fieldMap.put("golden_apple_type", makeSingleKey(TypeTokens.GOLDEN_APPLE_TOKEN, TypeTokens.GOLDEN_APPLE_VALUE_TOKEN, of("GoldenAppleType"), "sponge:golden_apple_type", "Golden Apple Type"));

        this.fieldMap.put("is_flying", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsFlying"), "sponge:is_flying", "Is Flying"));

        this.fieldMap.put("experience_level", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceLevel"), "sponge:experience_level", "Experience Level"));

        this.fieldMap.put("total_experience", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("TotalExperience"), "sponge:total_experience", "Total Experience"));

        this.fieldMap.put("experience_since_level", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceSinceLevel"), "sponge:experience_since_level", "Experience Since Level"));

        this.fieldMap.put("experience_from_start_of_level", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceFromStartOfLevel"), "sponge:experience_from_start_of_level", "Experience From Start Of Level"));

        this.fieldMap.put("book_author", makeSingleKey(TypeTokens.TEXT_TOKEN, TypeTokens.TEXT_VALUE_TOKEN, of("BookAuthor"), "sponge:book_author", "Book Author"));

        this.fieldMap.put("breakable_block_types", makeSetKey(TypeTokens.SET_BLOCK_TOKEN, TypeTokens.SET_BLOCK_VALUE_TOKEN, of("CanDestroy"), "sponge:can_destroy", "Can Destroy"));

        this.fieldMap.put("placeable_blocks", makeSetKey(TypeTokens.SET_BLOCK_TOKEN, TypeTokens.SET_BLOCK_VALUE_TOKEN, of("CanPlaceOn"), "sponge:can_place_on", "Can Place On"));

        this.fieldMap.put("walking_speed", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("WalkingSpeed"), "sponge:walking_speed", "Walking Speed"));

        this.fieldMap.put("flying_speed", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("FlyingSpeed"), "sponge:flying_speed", "Flying Speed"));

        this.fieldMap.put("slime_size", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("SlimeSize"), "sponge:slime_size", "Slime Size"));

        this.fieldMap.put("villager_zombie_profession", makeOptionalKey(
            TypeTokens.OPTIONAL_PROFESSION_TOKEN, TypeTokens.OPTIONAL_PROFESSION_VALUE_TOKEN, of("VillagerZombieProfession"), "sponge:villager_zombie_profession", "Villager Zombie Profession"));

        this.fieldMap.put("is_playing", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsPlaying"), "sponge:is_playing", "Is Playing"));

        this.fieldMap.put("is_sitting", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSitting"), "sponge:is_sitting", "Is Sitting"));

        this.fieldMap.put("is_sheared", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSheared"), "sponge:is_sheared", "Is Sheared"));

        this.fieldMap.put("pig_saddle", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsPigSaddled"), "sponge:is_pig_saddled", "Is Pig Saddled"));

        this.fieldMap.put("tamed_owner", makeOptionalKey(TypeTokens.OPTIONAL_UUID_TOKEN, TypeTokens.OPTIONAL_UUID_VALUE_TOKEN, of("TamerUUID"), "sponge:tamer_uuid", "Tamer UUID"));

        this.fieldMap.put("is_wet", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsWet"), "sponge:is_wet", "Is Wet"));

        this.fieldMap.put("elder_guardian", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Elder"), "sponge:elder", "Elder"));

        this.fieldMap.put("coal_type", makeSingleKey(TypeTokens.COAL_TOKEN, TypeTokens.COAL_VALUE_TOKEN, of("CoalType"), "sponge:coal_type", "Coal Type"));

        this.fieldMap.put("cooked_fish", makeSingleKey(TypeTokens.COOKED_FISH_TOKEN, TypeTokens.COOKED_FISH_VALUE_TOKEN, of("CookedFishType"), "sponge:cooked_fish_type", "Cooked Fish Type"));

        this.fieldMap.put("fish_type", makeSingleKey(TypeTokens.FISH_TOKEN, TypeTokens.FISH_VALUE_TOKEN, of("RawFishType"), "sponge:raw_fish_type", "Raw Fish Type"));

        this.fieldMap.put("represented_player", makeSingleKey(TypeTokens.GAME_PROFILE_TOKEN, TypeTokens.GAME_PROFILE_VALUE_TOKEN, of("RepresentedPlayer"), "sponge:represented_player", "Represented Player"));

        this.fieldMap.put("passed_burn_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("PassedBurnTime"), "sponge:passed_burn_time", "Passed Burn Time"));

        this.fieldMap.put("max_burn_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("MaxBurnTime"), "sponge:max_burn_time", "Max Burn Time"));

        this.fieldMap.put("passed_cook_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("PassedCookTime"), "sponge:passed_cook_time", "Passed Cook Time"));

        this.fieldMap.put("max_cook_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("MaxCookTime"), "sponge:max_cook_time", "Max Cook Time"));

        this.fieldMap.put("contained_experience", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("ContainedExperience"), "sponge:contained_experience", "Contained Experience"));

        this.fieldMap.put("remaining_brew_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("RemainingBrewTime"), "sponge:remaining_brew_time", "Remaining Brew Time"));

        this.fieldMap.put("stone_type", makeSingleKey(TypeTokens.STONE_TOKEN, TypeTokens.STONE_VALUE_TOKEN, of("StoneType"), "sponge:stone_type", "Stone Type"));

        this.fieldMap.put("prismarine_type", makeSingleKey(TypeTokens.PRISMARINE_TOKEN, TypeTokens.PRISMARINE_VALUE_TOKEN, of("PrismarineType"), "sponge:prismarine_type", "Prismarine Type"));

        this.fieldMap.put("brick_type", makeSingleKey(TypeTokens.BRICK_TOKEN, TypeTokens.BRICK_VALUE_TOKEN, of("BrickType"), "sponge:brick_type", "Brick Type"));

        this.fieldMap.put("quartz_type", makeSingleKey(TypeTokens.QUARTZ_TOKEN, TypeTokens.QUARTZ_VALUE_TOKEN, of("QuartzType"), "sponge:quartz_type", "Quartz Type"));

        this.fieldMap.put("sand_type", makeSingleKey(TypeTokens.SAND_TOKEN, TypeTokens.SAND_VALUE_TOKEN, of("SandType"), "sponge:sand_type", "Sand Type"));

        this.fieldMap.put("sandstone_type", makeSingleKey(TypeTokens.SAND_STONE_TOKEN, TypeTokens.SAND_STONE_VALUE_TOKEN, of("SandstoneType"), "sponge:sandstone_type", "Sandstone Type"));

        this.fieldMap.put("slab_type", makeSingleKey(TypeTokens.SLAB_TOKEN, TypeTokens.SLAB_VALUE_TOKEN, of("SlabType"), "sponge:slab_type", "Slab Type"));

        this.fieldMap.put("sandstone_type", makeSingleKey(TypeTokens.SAND_STONE_TOKEN, TypeTokens.SAND_STONE_VALUE_TOKEN, of("SandstoneType"), "sponge:sandstone_type", "Sandstone Type"));

        this.fieldMap.put("comparator_type", makeSingleKey(TypeTokens.COMPARATOR_TOKEN, TypeTokens.COMPARATOR_VALUE_TOKEN, of("ComparatorType"), "sponge:comparator_type", "Comparator Type"));

        this.fieldMap.put("hinge_position", makeSingleKey(TypeTokens.HINGE_TOKEN, TypeTokens.HINGE_VALUE_TOKEN, of("HingePosition"), "sponge:hinge_position", "Hinge Position"));

        this.fieldMap.put("piston_type", makeSingleKey(TypeTokens.PISTON_TOKEN, TypeTokens.PISTON_VALUE_TOKEN, of("PistonType"), "sponge:piston_type", "Piston Type"));

        this.fieldMap.put("portion_type", makeSingleKey(TypeTokens.PORTION_TOKEN, TypeTokens.PORTION_VALUE_TOKEN, of("PortionType"), "sponge:portion_type", "Portion Type"));

        this.fieldMap.put("rail_direction", makeSingleKey(TypeTokens.RAIL_TOKEN, TypeTokens.RAIL_VALUE_TOKEN, of("RailDirection"), "sponge:rail_direction", "Rail Direction"));

        this.fieldMap.put("stair_shape", makeSingleKey(TypeTokens.STAIR_TOKEN, TypeTokens.STAIR_VALUE_TOKEN, of("StairShape"), "sponge:stair_shape", "Stair Shape"));

        this.fieldMap.put("wall_type", makeSingleKey(TypeTokens.WALL_TOKEN, TypeTokens.WALL_VALUE_TOKEN, of("WallType"), "sponge:wall_type", "Wall Type"));

        this.fieldMap.put("double_plant_type", makeSingleKey(TypeTokens.DOUBLE_PLANT_TOKEN, TypeTokens.DOUBLE_PLANT_VALUE_TOKEN, of("DoublePlantType"), "sponge:double_plant_type", "Double Plant Type"));

        this.fieldMap.put("big_mushroom_type", makeSingleKey(TypeTokens.MUSHROOM_TOKEN, TypeTokens.MUSHROOM_VALUE_TOKEN, of("BigMushroomType"), "sponge:big_mushroom_type", "Big Mushroom Type"));

        this.fieldMap.put("ai_enabled", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsAiEnabled"), "sponge:is_ai_enabled", "Is Ai Enabled"));

        this.fieldMap.put("creeper_charged", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsCreeperCharged"), "sponge:is_creeper_charged", "Is Creeper Charged"));

        this.fieldMap.put("item_durability", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("ItemDurability"), "sponge:item_durability", "Item Durability"));

        this.fieldMap.put("unbreakable", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Unbreakable"), "sponge:unbreakable", "Unbreakable"));

        this.fieldMap.put("spawnable_entity_type", makeSingleKey(TypeTokens.ENTITY_TYPE_TOKEN, TypeTokens.ENTITY_TYPE_VALUE_TOKEN, of("SpawnableEntityType"), "sponge:spawnable_entity_type", "Spawnable Entity Type"));

        this.fieldMap.put("fall_distance", makeSingleKey(TypeTokens.FLOAT_TOKEN, TypeTokens.FLOAT_VALUE_TOKEN, of("FallDistance"), "sponge:fall_distance", "Fall Distance"));

        this.fieldMap.put("cooldown", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("Cooldown"), "sponge:cooldown", "Cooldown"));

        this.fieldMap.put("note_pitch", makeSingleKey(TypeTokens.NOTE_TOKEN, TypeTokens.NOTE_VALUE_TOKEN, of("Note"), "sponge:note", "Note"));

        this.fieldMap.put("vehicle", makeSingleKey(TypeTokens.ENTITY_TOKEN, TypeTokens.ENTITY_VALUE_TOKEN, of("Vehicle"), "sponge:vehicle", "Vehicle"));

        this.fieldMap.put("base_vehicle", makeSingleKey(TypeTokens.ENTITY_TOKEN, TypeTokens.ENTITY_VALUE_TOKEN, of("BaseVehicle"), "sponge:base_vehicle", "Base Vehicle"));

        this.fieldMap.put("art", makeSingleKey(TypeTokens.ART_TOKEN, TypeTokens.ART_VALUE_TOKEN, of("Art"), "sponge:art", "Art"));

        this.fieldMap.put("fall_damage_per_block", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("FallDamagePerBlock"), "sponge:fall_damage_per_block", "Fall Damage Per Block"));

        this.fieldMap.put("max_fall_damage", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("MaxFallDamage"), "sponge:max_fall_damage", "Max Fall Damage"));

        this.fieldMap.put("falling_block_state", makeSingleKey(TypeTokens.BLOCK_TOKEN, TypeTokens.BLOCK_VALUE_TOKEN, of("FallingBlockState"), "sponge:falling_block_state", "Falling Block State"));

        this.fieldMap.put("can_place_as_block", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CanPlaceAsBlock"), "sponge:can_place_as_block", "Can Place As Block"));

        this.fieldMap.put("can_drop_as_item", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CanDropAsItem"), "sponge:can_drop_as_item", "Can Drop As Item"));

        this.fieldMap.put("fall_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("FallTime"), "sponge:fall_time", "Fall Time"));

        this.fieldMap.put("falling_block_can_hurt_entities", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CanFallingBlockHurtEntities"), "sponge:can_falling_block_hurt_entities", "Can Falling Block Hurt Entities"));

        this.fieldMap.put("represented_block", makeSingleKey(TypeTokens.BLOCK_TOKEN, TypeTokens.BLOCK_VALUE_TOKEN, of("RepresentedBlock"), "sponge:represented_block", "Represented Block"));

        this.fieldMap.put("offset", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("BlockOffset"), "sponge:block_offset", "Block Offset"));

        this.fieldMap.put("attached", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Attached"), "sponge:attached", "Attached"));

        this.fieldMap.put("should_drop", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("ShouldDrop"), "sponge:should_drop", "Should Drop"));

        this.fieldMap.put("extended", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Extended"), "sponge:extended", "Extended"));

        this.fieldMap.put("growth_stage", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("GrowthStage"), "sponge:growth_stage", "Growth Stage"));

        this.fieldMap.put("open", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Open"), "sponge:open", "Open"));

        this.fieldMap.put("power", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Power"), "sponge:power", "Power"));

        this.fieldMap.put("seamless", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Seamless"), "sponge:seamless", "Seamless"));

        this.fieldMap.put("snowed", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Snowed"), "sponge:snowed", "Snowed"));

        this.fieldMap.put("suspended", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Suspended"), "sponge:suspended", "Suspended"));

        this.fieldMap.put("occupied", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Occupied"), "sponge:occupied", "Occupied"));

        this.fieldMap.put("decayable", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Decayable"), "sponge:decayable", "Decayable"));

        this.fieldMap.put("in_wall", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("InWall"), "sponge:in_wall", "In Wall"));

        this.fieldMap.put("delay", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Delay"), "sponge:delay", "Delay"));

        this.fieldMap.put("player_created", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("PlayerCreated"), "sponge:player_created", "Player Created"));

        this.fieldMap.put("item_blockstate", makeSingleKey(TypeTokens.BLOCK_TOKEN, TypeTokens.BLOCK_VALUE_TOKEN, of("ItemBlockState"), "sponge:item_block_state", "Item Block State"));

        this.fieldMap.put("skeleton_type", makeSingleKey(TypeTokens.SKELETON_TOKEN, TypeTokens.SKELETON_VALUE_TOKEN, of("SkeletonType"), "sponge:skeleton_type", "Skeleton Type"));

        this.fieldMap.put("ocelot_type", makeSingleKey(TypeTokens.OCELOT_TOKEN, TypeTokens.OCELOT_VALUE_TOKEN, of("OcelotType"), "sponge:ocelot_type", "Ocelot Type"));

        this.fieldMap.put("rabbit_type", makeSingleKey(TypeTokens.RABBIT_TOKEN, TypeTokens.RABBIT_VALUE_TOKEN, of("RabbitType"), "sponge:rabbit_type", "Rabbit Type"));

        this.fieldMap.put("lock_token", makeSingleKey(TypeTokens.STRING_TOKEN, TypeTokens.STRING_VALUE_TOKEN, of("Lock"), "sponge:lock", "Lock"));

        this.fieldMap.put("banner_base_color", makeSingleKey(TypeTokens.DYE_COLOR_TOKEN, TypeTokens.DYE_COLOR_VALUE_TOKEN, of("BannerBaseColor"), "sponge:banner_base_color", "Banner Base Color"));

        this.fieldMap.put("banner_patterns", new PatternKey());

        this.fieldMap.put("respawn_locations", makeMapKey(TypeTokens.MAP_UUID_VECTOR3D_TOKEN, TypeTokens.MAP_UUID_VECTOR3D_VALUE_TOKEN, of("RespawnLocations"), "sponge:respawn_locations", "Respawn Locations"));

        this.fieldMap.put("expiration_ticks", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("ExpirationTicks"), "sponge:expiration_ticks", "Expiration Ticks"));

        this.fieldMap.put("skin_unique_id", makeSingleKey(TypeTokens.UUID_TOKEN, TypeTokens.UUID_VALUE_TOKEN, of("SkinUUID"), "sponge:skin_uuid", "Skin UUID"));

        this.fieldMap.put("moisture", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Moisture"), "sponge:moisture", "Moisture"));

        this.fieldMap.put("angry", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Angry"), "sponge:angry", "Angry"));

        this.fieldMap.put("anger", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Anger"), "sponge:anger", "Anger"));

        this.fieldMap.put("rotation", makeSingleKey(TypeTokens.ROTATION_TOKEN, TypeTokens.ROTATION_VALUE_TOKEN, of("Rotation"), "sponge:rotation", "Rotation"));

        this.fieldMap.put("is_splash_potion", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSplashPotion"), "sponge:is_splash_potion", "Is Splash Potion"));

        this.fieldMap.put("affects_spawning", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("AffectsSpawning"), "sponge:affects_spawning", "Affects Spawning"));

        this.fieldMap.put("critical_hit", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CriticalHit"), "sponge:critical_hit", "Critical Hit"));

        this.fieldMap.put("generation", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Generation"), "sponge:generation", "Generation"));

        this.fieldMap.put("passengers", makeSingleKey(TypeTokens.ENTITY_TOKEN, TypeTokens.ENTITY_VALUE_TOKEN, of("PassengerSnapshot"), "sponge:passenger_snapshot", "Passenger Snapshot"));

        this.fieldMap.put("knockback_strength", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("KnockbackStrength"), "sponge:knockback_strength", "Knockback Strength"));

        this.fieldMap.put("persists", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Persists"), "sponge:persists", "Persists"));

        this.fieldMap.put("stored_enchantments", makeListKey(TypeTokens.LIST_ITEM_ENCHANTMENT_TOKEN, TypeTokens.LIST_ITEM_ENCHANTMENT_VALUE_TOKEN, of("StoredEnchantments"), "sponge:stored_enchantments", "Stored Enchantments"));

        this.fieldMap.put("is_sprinting", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Sprinting"), "sponge:sprinting", "Sprinting"));

        this.fieldMap.put("stuck_arrows", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("StuckArrows"), "sponge:stuck_arrows", "Stuck Arrows"));

        this.fieldMap.put("vanish_ignores_collision", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("VanishIgnoresCollision"), "sponge:vanish_ignores_collision", "Vanish Ignores Collision"));

        this.fieldMap.put("vanish_prevents_targeting", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("VanishPreventsTargeting"), "sponge:vanish_prevents_targeting", "Vanish Prevents Targeting"));

        this.fieldMap.put("is_aflame", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsAflame"), "sponge:is_aflame", "Is Aflame"));

        this.fieldMap.put("can_breed", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CanBreed"), "sponge:can_breed", "Can Breed"));

        this.fieldMap.put("fluid_item_stack", makeSingleKey(TypeTokens.FLUID_TOKEN, TypeTokens.FLUID_VALUE_TOKEN, of("FluidItemContainerSnapshot"), "sponge:fluid_item_container_snapshot", "Fluid Item Container Snapshot"));

        this.fieldMap.put("fluid_tank_contents", makeMapKey(TypeTokens.MAP_DIRECTION_FLUID_TOKEN, TypeTokens.MAP_DIRECTION_FLUID_VALUE_TOKEN, of("FluidTankContents"), "sponge:fluid_tank_contents", "Fluid Tank Contents"));

        this.fieldMap.put("custom_name_visible", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("CustomNameVisible"), "sponge:custom_name_visible", "Custom Name Visible"));

        this.fieldMap.put("first_date_played", makeSingleKey(TypeTokens.INSTANT_TOKEN, TypeTokens.INSTANT_VALUE_TOKEN, of("FirstTimeJoined"), "sponge:first_time_joined", "First Time Joined"));

        this.fieldMap.put("last_date_played", makeSingleKey(TypeTokens.INSTANT_TOKEN, TypeTokens.INSTANT_VALUE_TOKEN, of("LastTimePlayed"), "sponge:last_time_played", "Last Time Played"));

        this.fieldMap.put("hide_enchantments", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HideEnchantments"), "sponge:hide_enchantments", "Hide Enchantments"));

        this.fieldMap.put("hide_attributes", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HideAttributes"), "sponge:hide_attributes", "Hide Attributes"));

        this.fieldMap.put("hide_unbreakable", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HideUnbreakable"), "sponge:hide_unbreakable", "Hide Unbreakable"));

        this.fieldMap.put("hide_can_destroy", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HideCanDestroy"), "sponge:hide_can_destroy", "Hide Can Destroy"));

        this.fieldMap.put("hide_can_place", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HideCanPlace"), "sponge:hide_can_place", "Hide Can Place"));

        this.fieldMap.put("hide_miscellaneous", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HideMiscellaneous"), "sponge:hide_miscellaneous", "Hide Miscellaneous"));

        this.fieldMap.put("potion_effects", makeListKey(TypeTokens.LIST_POTION_TOKEN, TypeTokens.LIST_POTION_VALUE_TOKEN, of("PotionEffects"), "sponge:potion_effects", "Potion Effects"));

        this.fieldMap.put("body_rotations", makeMapKey(TypeTokens.MAP_BODY_VECTOR3D_TOKEN, TypeTokens.MAP_BODY_VECTOR3D_VALUE_TOKEN, of("BodyRotations"), "sponge:body_rotations", "Body Rotations"));

        this.fieldMap.put("head_rotation", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("HeadRotation"), "sponge:head_rotation", "Head Rotation"));

        this.fieldMap.put("chest_rotation", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("ChestRotation"), "sponge:chest_rotation", "Chest Rotation"));

        this.fieldMap.put("left_arm_rotation", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("LeftArmRotation"), "sponge:left_arm_rotation", "Left Arm Rotation"));

        this.fieldMap.put("right_arm_rotation", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("RightArmRotation"), "sponge:right_arm_rotation", "Right Arm Rotation"));

        this.fieldMap.put("left_leg_rotation", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("LeftLegRotation"), "sponge:left_leg_rotation", "Left Leg Rotation"));

        this.fieldMap.put("right_leg_rotation", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("RightLegRotation"), "sponge:right_leg_rotation", "Right Leg Rotation"));

        this.fieldMap.put("beacon_primary_effect", makeOptionalKey(TypeTokens.OPTIONAL_POTION_TOKEN, TypeTokens.OPTIONAL_POTION_VALUE_TOKEN, of("BeaconPrimaryEffect"), "sponge:beacon_primary_effect", "Beacon Primary Effect"));

        this.fieldMap.put("beacon_secondary_effect", makeOptionalKey(TypeTokens.OPTIONAL_POTION_TOKEN, TypeTokens.OPTIONAL_POTION_VALUE_TOKEN, of("BeaconSecondaryEffect"), "sponge:beacon_secondary_effect", "Beacon Secondary Effect"));

        this.fieldMap.put("targeted_location", makeSingleKey(TypeTokens.VECTOR_3D_TOKEN, TypeTokens.VECTOR_3D_VALUE_TOKEN, of("TargetedVector3d"), "sponge:targeted_vector_3d", "Targeted Vector3d"));

        this.fieldMap.put("fuse_duration", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("FuseDuration"), "sponge:fuse_duration", "Fuse Duration"));

        this.fieldMap.put("ticks_remaining", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("TicksRemaining"), "sponge:ticks_remaining", "Ticks Remaining"));

        this.fieldMap.put("explosion_radius", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("ExplosionRadius"), "sponge:explosion_radius", "Explosion Radius"));

        this.fieldMap.put("armor_stand_has_arms", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HasArms"), "sponge:has_arms", "Has Arms"));

        this.fieldMap.put("armor_stand_has_base_plate", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HasBasePlate"), "sponge:has_base_plate", "Has Base Plate"));

        this.fieldMap.put("armor_stand_marker", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsMarker"), "sponge:is_marker", "Is Marker"));

        this.fieldMap.put("armor_stand_is_small", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSmall"), "sponge:is_small", "Is Small"));

        this.fieldMap.put("is_silent", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSilent"), "sponge:is_silent", "Is Silent"));

        this.fieldMap.put("glowing", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Glowing"), "sponge:glowing", "Glowing"));

        this.fieldMap.put("pickup_rule", makeSingleKey(TypeTokens.PICKUP_TOKEN, TypeTokens.PICKUP_VALUE_TOKEN, of("PickupRule"), "sponge:pickup_rule", "Pickup Rule"));

        this.fieldMap.put("invulnerability_ticks", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("HurtTime"), "sponge:invulnerability_ticks", "Invulnerability Ticks"));

        this.fieldMap.put("has_gravity", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("HasGravity"), "sponge:has_gravity", "Has Gravity"));

        this.fieldMap.put("zombie_type", makeSingleKey(TypeTokens.ZOMBIE_TYPE_TOKEN, TypeTokens.ZOMBIE_TYPE_VALUE_TOKEN, of("ZombieType"), "sponge:zombie_type", "Zombie Type"));

        this.fieldMap.put("achievements", makeSingleKey(TypeTokens.ACHIEVEMENT_SET_TOKEN, TypeTokens.ACHIEVEMENT_SET_VALUE_TOKEN, of("Achievements"), "sponge:achievements", "Achievements"));

        this.fieldMap.put("statistics", makeSingleKey(TypeTokens.STATISTIC_MAP_TOKEN, TypeTokens.STATISTIC_MAP_VALUE_TOKEN, of("Statistics"), "sponge:statistics", "Statistics"));

        this.fieldMap.put("infinite_despawn_delay", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("InfiniteDespawnDelay"), "sponge:infinite_despawn_delay", "Infinite Despawn Delay"));

        this.fieldMap.put("infinite_pickup_delay", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("InfinitePickupDelay"), "sponge:infinite_pickup_delay", "Infinite Pickup Delay"));

        this.fieldMap.put("despawn_delay", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("DespawnDelay"), "sponge:despawn_delay", "Despawn Delay"));

        this.fieldMap.put("pickup_delay", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("PickupDelay"), "sponge:pickup_delay", "Pickup Delay"));

        this.fieldMap.put("end_gateway_age", makeSingleKey(TypeTokens.LONG_TOKEN, TypeTokens.LONG_VALUE_TOKEN, of("EndGatewayAge"), "sponge:end_gateway_age", "End Gateway Age"));
        this.fieldMap.put("end_gateway_teleport_cooldown", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.INTEGER_VALUE_TOKEN, of("EndGatewayTeleportCooldown"), "sponge:end_gateway_teleport_cooldown", "End Gateway Teleport Cooldown"));
        this.fieldMap.put("exit_position", makeSingleKey(TypeTokens.VECTOR_3I_TOKEN, TypeTokens.VECTOR_3I_VALUE_TOKEN, of("ExitPosition"), "sponge:exit_position", "Exit Position"));
        this.fieldMap.put("exact_teleport", makeSingleKey(TypeTokens.VECTOR_3I_TOKEN, TypeTokens.VECTOR_3I_VALUE_TOKEN, of("ExactTeleport"), "sponge:exact_teleport", "Exact Teleport"));
        this.fieldMap.put("structure_author", makeSingleKey(TypeTokens.STRING_TOKEN, TypeTokens.STRING_VALUE_TOKEN, of("StructureAuthor"), "sponge:structure_author", "Structure Author"));
        this.fieldMap.put("structure_ignore_entities", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("StructureIgnoreEntities"), "sponge:structure_ignore_entities", "Structure Ignore Entities"));
        this.fieldMap.put("structure_integrity", makeSingleKey(TypeTokens.FLOAT_TOKEN, TypeTokens.FLOAT_VALUE_TOKEN, of("StructureIntegrity"), "sponge:structure_integrity", "Structure Integrity"));
        this.fieldMap.put("structure_mode", makeSingleKey(TypeTokens.STRUCTURE_MODE_TOKEN, TypeTokens.STRUCTURE_MODE_VALUE_TOKEN, of("StructureMode"), "sponge:structure_mode", "Structure Mode"));
        this.fieldMap.put("structure_position", makeSingleKey(TypeTokens.STRING_TOKEN, TypeTokens.STRING_VALUE_TOKEN, of("StructurePosition"), "sponge:structure_position", "Structure Position"));
        this.fieldMap.put("structure_powered", makeSingleKey(TypeTokens.STRING_TOKEN, TypeTokens.STRING_VALUE_TOKEN, of("StructurePowered"), "sponge:structure_powered", "Structure Powered"));
        this.fieldMap.put("structure_seed", makeSingleKey(TypeTokens.LONG_TOKEN, TypeTokens.LONG_VALUE_TOKEN, of("StructureSeed"), "sponge:structure_seed", "Structure Seed"));
        this.fieldMap.put("structure_show_air", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("StructureShowAir"), "sponge:structure_show_air", "Structure Show Air"));
        this.fieldMap.put("structure_show_bounding_box", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("StructureShowBoundingBox"), "sponge:structure_show_bounding_box", "Structure Show Bounding Box"));
        this.fieldMap.put("structure_size", makeSingleKey(TypeTokens.VECTOR_3I_TOKEN, TypeTokens.VECTOR_3I_VALUE_TOKEN, of("StructureSize"), "sponge:structure_size", "Structure Size"));
        this.fieldMap.put("absorption", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("Absorption"), "sponge:absorption", "Absorption"));

        this.fieldMap.put("area_effect_cloud_radius", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("CloudRadius"), "sponge:area_effect_cloud_radius", "AreaEffectCloud Radius"));

        this.fieldMap.put("area_effect_cloud_radius_on_use", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("CloudRadiusOnUse"), "sponge:area_effect_cloud_radius_on_use", "AreaEffectCloud Radius On Use"));

        this.fieldMap.put("area_effect_cloud_radius_per_tick", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("CloudRadiusPerTick"), "sponge:area_effect_cloud_radius_per_tick", "AreaEffectCloud Radius Per Tick"));

        this.fieldMap.put("area_effect_cloud_color", makeSingleKey(TypeTokens.COLOR_TOKEN, TypeTokens.COLOR_VALUE_TOKEN, of("CloudColor"), "sponge:area_effect_cloud_color", "AreaEffectCloud Color"));

        this.fieldMap.put("area_effect_cloud_duration", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("CloudDuration"), "sponge:area_effect_cloud_duration", "AreaEffectCloud Duration"));

        this.fieldMap.put("area_effect_cloud_duration_on_use", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("CloudDurationOnUse"), "sponge:area_effect_cloud_duration_on_use", "AreaEffectCloud Duration On Use"));

        this.fieldMap.put("area_effect_cloud_wait_time", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("CloudWaitTime"), "sponge:area_effect_cloud_wait_time", "AreaEffectCloud Wait Time"));

        this.fieldMap.put("area_effect_cloud_reapplication_delay", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("CloudReapplicationDelay"), "sponge:area_effect_cloud_wait_time", "AreaEffectCloud Wait Time"));

        this.fieldMap.put("area_effect_cloud_age", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("CloudAge"), "sponge:area_effect_cloud_age", "AreaEffectCloud Age"));

        this.fieldMap.put("area_effect_cloud_particle_type", makeSingleKey(TypeTokens.PARTICLE_TYPE_TOKEN, TypeTokens.PARTICLE_TYPE_VALUE_TOKEN, of("CloudParticleType"), "sponge:area_effect_cloud_particle_type", "AreaEffectCloud ParticleType"));

        this.fieldMap.put("age", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("EntityAge"), "sponge:entity_age", "Entity Age"));

        this.fieldMap.put("attack_damage", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("EntityAttackDamage"), "sponge:entity_attack_damage", "Entity Attack Damage"));

        this.fieldMap.put("base_size", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("EntityBaseSize"), "sponge:base_size", "Entity Base Size"));

        this.fieldMap.put("damage_entity_map", makeMapKey(TypeTokens.ENTITY_TYPE_DOUBLE_MAP_TOKEN, TypeTokens.ENTITY_TYPE_DOUBLE_MAP_VALUE_TOKEN, of("DamageEntityTypeMap"), "sponge:entity_type_damage_map", "Entity Type Damage Map"));

        this.fieldMap.put("dominant_hand", makeSingleKey(TypeTokens.HAND_PREFERENCE_TYPE_TOKEN, TypeTokens.HAND_PREFERENCE_VALUE_TOKEN, of("HandPreference"), "sponge:hand_preference", "Hand Preference"));

        this.fieldMap.put("filled", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("Filled"), "sponge:filled", "Filled"));

        this.fieldMap.put("fluid_level", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("LiquidLevel"), "sponge:fluid_level", "Fluid Level"));

        this.fieldMap.put("health_scale", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("HealthScale"), "sponge:health_scale", "Health Scale"));

        this.fieldMap.put("height", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("EntityHeight"), "sponge:entity_height", "Entity Height"));

        this.fieldMap.put("held_experience", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("HeldExperience"), "sponge:held_experience", "Held Experience"));

        this.fieldMap.put("is_sleeping", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsSleeping"), "sponge:is_sleeping", "Is Sleeping"));

        this.fieldMap.put("johnny_vindicator", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("JohnnyVindicator"), "sponge:johnny_vindicator", "Johnny Vindicator"));

        this.fieldMap.put("last_attacker", makeSingleKey(TypeTokens.LAST_ATTACKER_TOKEN, TypeTokens.LAST_ATTACKER_VALUE_TOKEN, of("LastAttacker"), "sponge:last_attacker", "Last Attacking Entity"));

        this.fieldMap.put("llama_strength", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("LlamaStrength"), "sponge:llama_strength", "Llama Strength"));

        this.fieldMap.put("llama_variant", makeSingleKey(TypeTokens.LLAMA_VARIANT_TOKEN, TypeTokens.LLAMA_VARIANT_VALUE_TOKEN, of("LlamaVariant"), "sponge:llama_variant", "Llama Variant"));

        this.fieldMap.put("scale", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.DOUBLE_VALUE_TOKEN, of("EntityScale"), "sponge:entity_scale", "Entity Scale"));

        this.fieldMap.put("will_shatter", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("WillShatter"), "sponge:will_shatter", "Will Shatter"));

        this.fieldMap.put("wire_attachments", makeMapKey(TypeTokens.WIRE_ATTACHMENT_MAP_TOKEN, TypeTokens.WIRE_ATTACHMENT_MAP_VALUE_TOKEN, of("WireAttachmentMap"), "sponge:wire_attachment_map", "Wire Attachment Map"));

        this.fieldMap.put("wire_attachment_east", makeSingleKey(TypeTokens.WIRE_ATTACHMENT_TYPE_TOKEN, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN, of("WireAttachmentEast"), "sponge:wire_attachment_east", "Wire Attachment East"));
        this.fieldMap.put("wire_attachment_south", makeSingleKey(TypeTokens.WIRE_ATTACHMENT_TYPE_TOKEN, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN, of("WireAttachmentSouth"), "sponge:wire_attachment_south", "Wire Attachment South"));
        this.fieldMap.put("wire_attachment_north", makeSingleKey(TypeTokens.WIRE_ATTACHMENT_TYPE_TOKEN, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN, of("WireAttachmentNorth"), "sponge:wire_attachment_north", "Wire Attachment North"));
        this.fieldMap.put("wire_attachment_west", makeSingleKey(TypeTokens.WIRE_ATTACHMENT_TYPE_TOKEN, TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN, of("WireAttachmentWest"), "sponge:wire_attachment_west", "Wire Attachment West"));

        this.fieldMap.put("age", makeSingleKey(TypeTokens.INTEGER_TOKEN, TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN, of("Age"), "sponge:age", "Age"));
        this.fieldMap.put("is_adult", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsAdult"), "sponge:is_adult", "Is Adult"));
        this.fieldMap.put("is_baby", makeSingleKey(TypeTokens.BOOLEAN_TOKEN, TypeTokens.BOOLEAN_VALUE_TOKEN, of("IsBaby"), "sponge:is_baby", "Is Baby"));

        this.fieldMap.put("health_scale", makeSingleKey(TypeTokens.DOUBLE_TOKEN, TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN, of("HealthScale"), "sponge:health_scale", "Health Scale"));

        for (Key<?> key : this.fieldMap.values()) {
            this.keyMap.put(key.getId().toLowerCase(Locale.ENGLISH), key);
        }

    }

    @Override
    public void registerAdditionalCatalog(Key<?> extraCatalog) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Cannot register new Keys after Data Registration has completed!");
        checkNotNull(extraCatalog, "Key cannot be null!");
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        checkArgument(!id.startsWith("sponge:"), "A plugin is trying to register custom keys under the sponge id namespace! This is a fake key! " + id);
        this.keyMap.put(id, extraCatalog);
    }

    @Override
    public Optional<Key<?>> getById(String id) {
        return Optional.ofNullable(this.keyMap.get(id));
    }

    @Override
    public Collection<Key<?>> getAll() {
        return Collections.unmodifiableCollection(this.keyMap.values());
    }

    private static final class PatternKey implements Key<PatternListValue> {

        static final TypeToken<PatternListValue> VALUE_TOKEN = new TypeToken<PatternListValue>() {
            private static final long serialVersionUID = -1;
        };
        static final TypeToken<List<PatternLayer>> ELEMENT_TOKEN = new TypeToken<List<PatternLayer>>() {
            private static final long serialVersionUID = -1;
        };
        private static final DataQuery BANNER_PATTERNS = of("BannerPatterns");

        PatternKey() {
        }

        @Override
        public TypeToken<PatternListValue> getValueToken() {
            return VALUE_TOKEN;
        }

        @Override
        public TypeToken<?> getElementToken() {
            return ELEMENT_TOKEN;
        }

        @Override
        public DataQuery getQuery() {
            return BANNER_PATTERNS;
        }

        @Override
        public String getId() {
            return "sponge:banner_patterns";
        }

        @Override
        public String getName() {
            return "BannerPatterns";
        }
    }

    KeyRegistryModule() {
    }

    static final class Holder {
        static final KeyRegistryModule INSTANCE = new KeyRegistryModule();
    }
}
