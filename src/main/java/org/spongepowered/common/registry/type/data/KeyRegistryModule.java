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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import org.h2.mvstore.ConcurrentArrayList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeKey;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.data.datasync.entity.EntityAirConverter;
import org.spongepowered.common.data.datasync.entity.EntityCustomNameConverter;
import org.spongepowered.common.data.datasync.entity.EntityCustomNameVisibleConverter;
import org.spongepowered.common.data.datasync.entity.EntityFlagsConverter;
import org.spongepowered.common.data.datasync.entity.EntityNoGravityConverter;
import org.spongepowered.common.data.datasync.entity.EntitySilentConverter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

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
        Sponge.getCauseStackManager().pushCause(SpongeImpl.getPlugin());

        register("axis", Key.builder().type(TypeTokens.AXIS_VALUE_TOKEN).id("sponge:axis").name("Axis").query(of("Axis")).build());

        register("color", Key.builder().type(TypeTokens.COLOR_VALUE_TOKEN).id("sponge:color").name("Color").query(of("Color")).build());

        register("health", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:health").name("Health").query(of("Health")).build());

        register("max_health", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:max_health").name("Max Health").query(of("MaxHealth")).build());

        register("display_name", Key.builder().type(TypeTokens.TEXT_VALUE_TOKEN).id("sponge:display_name").name("Display Name").query(of("DisplayName")).build());

        register("career", Key.builder().type(TypeTokens.CAREER_VALUE_TOKEN).id("sponge:career").name("Career").query(of("Career")).build());

        register("sign_lines", Key.builder().type(TypeTokens.LIST_TEXT_VALUE_TOKEN).id("sponge:sign_lines").name("Sign Lines").query(of("SignLines")).build());

        register("skull_type", Key.builder().type(TypeTokens.SKULL_VALUE_TOKEN).id("sponge:skull_type").name("Skull Type").query(of("SkullType")).build());

        register("is_sneaking", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:sneaking").name("Is Sneaking").query(of("IsSneaking")).build());

        register("velocity", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:velocity").name("Velocity").query(of("Velocity")).build());

        register("food_level", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:food_level").name("Food Level").query(of("FoodLevel")).build());

        register("saturation", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:food_saturation_level").name("Food Saturation Level").query(of("FoodSaturationLevel")).build());

        register("exhaustion", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:food_exhaustion_level").name("Food Exhaustion Level").query(of("FoodExhaustionLevel")).build());

        register("max_air", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:max_air").name("Max Air").query(of("MaxAir")).build());

        register("remaining_air", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:remaining_air").name("Remaining Air").query(of("RemainingAir")).build());

        register("fire_ticks", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:fire_ticks").name("Fire Ticks").query(of("FireTicks")).build());

        register("fire_damage_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:fire_damage_delay").name("Fire Damage Delay").query(of("FireDamageDelay")).build());

        register("game_mode", Key.builder().type(TypeTokens.GAME_MODE_VALUE_TOKEN).id("sponge:game_mode").name("Game Mode").query(of("GameMode")).build());

        register("is_screaming", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:screaming").name("Is Screaming").query(of("IsScreaming")).build());

        register("can_fly", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:can_fly").name("Can Fly").query(of("CanFly")).build());

        register("can_grief", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:can_grief").name("Can Grief").query(of("CanGrief")).build());

        register("shrub_type", Key.builder().type(TypeTokens.SHRUB_VALUE_TOKEN).id("sponge:shrub_type").name("Shrub Type").query(of("ShrubType")).build());

        register("plant_type", Key.builder().type(TypeTokens.PLANT_VALUE_TOKEN).id("sponge:plant_type").name("Plant Type").query(of("PlantType")).build());

        register("tree_type", Key.builder().type(TypeTokens.TREE_VALUE_TOKEN).id("sponge:tree_type").name("Tree Type").query(of("TreeType")).build());

        register("log_axis", Key.builder().type(TypeTokens.LOG_AXIS_VALUE_TOKEN).id("sponge:log_axis").name("Log Axis").query(of("LogAxis")).build());

        register("invisible", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:invisible").name("Invisible").query(of("Invisible")).build());

        register("vanish", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:vanish").name("Vanish").query(of("Vanish")).build());

        register("invisible", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:invisible").name("Invisible").query(of("Invisible")).build());

        register("powered", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:powered").name("Powered").query(of("Powered")).build());

        register("layer", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:layer").name("Layer").query(of("Layer")).build());

        register("represented_item", Key.builder().type(TypeTokens.ITEM_SNAPSHOT_VALUE_TOKEN).id("sponge:item_stack_snapshot").name("Item Stack Snapshot").query(of("ItemStackSnapshot")).build());

        register("command", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("sponge:command").name("Command").query(of("Command")).build());

        register("success_count", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:success_count").name("SuccessCount").query(of("SuccessCount")).build());

        register("tracks_output", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:tracks_output").name("Tracks Output").query(of("TracksOutput")).build());

        register("last_command_output", Key.builder().type(TypeTokens.OPTIONAL_TEXT_VALUE_TOKEN).id("sponge:last_command_output").name("Last Command Output").query(of("LastCommandOutput")).build());

        register("trade_offers", Key.builder().type(TypeTokens.LIST_VALUE_TRADE_OFFER_TOKEN).id("sponge:trade_offers").name("Trade Offers").query(of("TradeOffers")).build());

        register("dye_color", Key.builder().type(TypeTokens.DYE_COLOR_VALUE_TOKEN).id("sponge:dye_color").name("Dye Color").query(of("DyeColor")).build());

        register("firework_flight_modifier", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:flight_modifier").name("Flight Modifier").query(of("FlightModifier")).build());

        register("firework_effects", Key.builder().type(TypeTokens.LIST_VALUE_FIREWORK_TOKEN).id("sponge:firework_effects").name("Firework Effects").query(of("FireworkEffects")).build());

        register("spawner_entities", Key.builder().type(TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_COLLECTION_VALUE_TOKEN).id("sponge:spawner_entities").name("Spawner Entities").query(of("SpawnerEntities")).build());

        register("spawner_maximum_delay", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_maximum_delay").name("Spawner Maximum Delay").query(of("SpawnerMaximumDelay")).build());

        register("spawner_maximum_nearby_entities", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_maximum_nearby_entities").name("Spawner Maximum Nearby Entities").query(of("SpawnerMaximumNearbyEntities")).build());

        register("spawner_minimum_delay", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_minimum_delay").name("Spawner Minimum Delay").query(of("SpawnerMinimumDelay")).build());

        register("spawner_next_entity_to_spawn", Key.builder().type(TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_VALUE_TOKEN).id("sponge:spawner_next_entity_to_spawn").name("Spawner Next Entity To Spawn").query(of("SpawnerNextEntityToSpawn")).build());

        register("spawner_remaining_delay", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_remaining_delay").name("Spawner Remaining Delay").query(of("SpawnerRemainingDelay")).build());

        register("spawner_required_player_range", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_required_player_range").name("Spawner Required Player Range").query(of("SpawnerRequiredPlayerRange")).build());

        register("spawner_spawn_count", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_spawn_count").name("Spawner Spawn Count").query(of("SpawnerSpawnCount")).build());

        register("spawner_spawn_range", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("sponge:spawner_spawn_range").name("Spawner Spawn Range").query(of("SpawnerSpawnRange")).build());

        register("connected_directions", Key.builder().type(TypeTokens.SET_DIRECTION_VALUE_TOKEN).id("sponge:connected_directions").name("Connected Directions").query(of("ConnectedDirections")).build());

        register("connected_north", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:connected_north").name("Connected North").query(of("ConnectedNorth")).build());

        register("connected_south", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:connected_south").name("Connected South").query(of("ConnectedSouth")).build());

        register("connected_east", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:connected_east").name("Connected East").query(of("ConnectedEast")).build());

        register("connected_west", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:connected_west").name("Connected West").query(of("ConnectedWest")).build());

        register("direction", Key.builder().type(TypeTokens.DIRECTION_VALUE_TOKEN).id("sponge:direction").name("Direction").query(of("Direction")).build());

        register("dirt_type", Key.builder().type(TypeTokens.DIRT_VALUE_TOKEN).id("sponge:dirt_type").name("Dirt Type").query(of("DirtType")).build());

        register("disguised_block_type", Key.builder().type(TypeTokens.DISGUISED_BLOCK_VALUE_TOKEN).id("sponge:disguised_block_type").name("Disguised Block Type").query(of("DisguisedBlockType")).build());

        register("disarmed", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:disarmed").name("Disarmed").query(of("Disarmed")).build());

        register("item_enchantments", Key.builder().type(TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN).id("sponge:item_enchantments").name("Item EnchantmentTypes").query(of("ItemEnchantments")).build());

        register("banner_patterns", Key.builder().type(TypeTokens.LIST_PATTERN_VALUE_TOKEN).id("sponge:banner_patterns").name("Banner Patterns").query(of("BannerPatterns")).build());

        register("banner_base_color", Key.builder().type(TypeTokens.LIST_DYE_COLOR_VALUE_TOKEN).id("sponge:banner_base_color").name("Banner Base Color").query(of("BannerBaseColor")).build());

        register("horse_color", Key.builder().type(TypeTokens.HORSE_COLOR_VALUE_TOKEN).id("sponge:horse_color").name("Horse Color").query(of("HorseColor")).build());

        register("horse_style", Key.builder().type(TypeTokens.HORSE_STYLE_VALUE_TOKEN).id("sponge:horse_style").name("Horse Style").query(of("HorseStyle")).build());

        register("item_lore", Key.builder().type(TypeTokens.LIST_TEXT_VALUE_TOKEN).id("sponge:item_lore").name("Item Lore").query(of("ItemLore")).build());

        register("book_pages", Key.builder().type(TypeTokens.LIST_TEXT_VALUE_TOKEN).id("sponge:book_pages").name("Book Pages").query(of("BookPages")).build());

        register("golden_apple_type", Key.builder().type(TypeTokens.GOLDEN_APPLE_VALUE_TOKEN).id("sponge:golden_apple_type").name("Golden Apple Type").query(of("GoldenAppleType")).build());

        register("is_flying", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_flying").name("Is Flying").query(of("IsFlying")).build());

        register("experience_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:experience_level").name("Experience Level").query(of("ExperienceLevel")).build());

        register("total_experience", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:total_experience").name("Total Experience").query(of("TotalExperience")).build());

        register("experience_since_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:experience_since_level").name("Experience Since Level").query(of("ExperienceSinceLevel")).build());

        register("experience_from_start_of_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:experience_from_start_of_level").name("Experience From Start Of Level").query(of("ExperienceFromStartOfLevel")).build());

        register("book_author", Key.builder().type(TypeTokens.TEXT_VALUE_TOKEN).id("sponge:book_author").name("Book Author").query(of("BookAuthor")).build());

        register("breakable_block_types", Key.builder().type(TypeTokens.SET_BLOCK_VALUE_TOKEN).id("sponge:can_destroy").name("Can Destroy").query(of("CanDestroy")).build());

        register("placeable_blocks", Key.builder().type(TypeTokens.SET_BLOCK_VALUE_TOKEN).id("sponge:can_place_on").name("Can Place On").query(of("CanPlaceOn")).build());

        register("walking_speed", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:walking_speed").name("Walking Speed").query(of("WalkingSpeed")).build());

        register("flying_speed", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:flying_speed").name("Flying Speed").query(of("FlyingSpeed")).build());

        register("slime_size", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:slime_size").name("Slime Size").query(of("SlimeSize")).build());

        register("villager_zombie_profession", Key.builder().type(TypeTokens.OPTIONAL_PROFESSION_VALUE_TOKEN).id("sponge:villager_zombie_profession").name("Villager Zombie Profession").query(of("VillagerZombieProfession")).build());

        register("is_playing", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_playing").name("Is Playing").query(of("IsPlaying")).build());

        register("is_sitting", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_sitting").name("Is Sitting").query(of("IsSitting")).build());

        register("is_sheared", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_sheared").name("Is Sheared").query(of("IsSheared")).build());

        register("pig_saddle", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_pig_saddled").name("Is Pig Saddled").query(of("IsPigSaddled")).build());

        register("tamed_owner", Key.builder().type(TypeTokens.OPTIONAL_UUID_VALUE_TOKEN).id("sponge:tamer_uuid").name("Tamer UUID").query(of("TamerUUID")).build());

        register("is_wet", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_wet").name("Is Wet").query(of("IsWet")).build());

        register("elder_guardian", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:elder").name("Elder").query(of("Elder")).build());

        register("coal_type", Key.builder().type(TypeTokens.COAL_VALUE_TOKEN).id("sponge:coal_type").name("Coal Type").query(of("CoalType")).build());

        register("cooked_fish", Key.builder().type(TypeTokens.COOKED_FISH_VALUE_TOKEN).id("sponge:cooked_fish_type").name("Cooked Fish Type").query(of("CookedFishType")).build());

        register("fish_type", Key.builder().type(TypeTokens.FISH_VALUE_TOKEN).id("sponge:raw_fish_type").name("Raw Fish Type").query(of("RawFishType")).build());

        register("represented_player", Key.builder().type(TypeTokens.GAME_PROFILE_VALUE_TOKEN).id("sponge:represented_player").name("Represented Player").query(of("RepresentedPlayer")).build());

        register("passed_burn_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:passed_burn_time").name("Passed Burn Time").query(of("PassedBurnTime")).build());

        register("max_burn_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:max_burn_time").name("Max Burn Time").query(of("MaxBurnTime")).build());

        register("passed_cook_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:passed_cook_time").name("Passed Cook Time").query(of("PassedCookTime")).build());

        register("max_cook_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:max_cook_time").name("Max Cook Time").query(of("MaxCookTime")).build());

        register("contained_experience", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:contained_experience").name("Contained Experience").query(of("ContainedExperience")).build());

        register("remaining_brew_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:remaining_brew_time").name("Remaining Brew Time").query(of("RemainingBrewTime")).build());

        register("stone_type", Key.builder().type(TypeTokens.STONE_VALUE_TOKEN).id("sponge:stone_type").name("Stone Type").query(of("StoneType")).build());

        register("prismarine_type", Key.builder().type(TypeTokens.PRISMARINE_VALUE_TOKEN).id("sponge:prismarine_type").name("Prismarine Type").query(of("PrismarineType")).build());

        register("brick_type", Key.builder().type(TypeTokens.BRICK_VALUE_TOKEN).id("sponge:brick_type").name("Brick Type").query(of("BrickType")).build());

        register("quartz_type", Key.builder().type(TypeTokens.QUARTZ_VALUE_TOKEN).id("sponge:quartz_type").name("Quartz Type").query(of("QuartzType")).build());

        register("sand_type", Key.builder().type(TypeTokens.SAND_VALUE_TOKEN).id("sponge:sand_type").name("Sand Type").query(of("SandType")).build());

        register("sandstone_type", Key.builder().type(TypeTokens.SAND_STONE_VALUE_TOKEN).id("sponge:sandstone_type").name("Sandstone Type").query(of("SandstoneType")).build());

        register("slab_type", Key.builder().type(TypeTokens.SLAB_VALUE_TOKEN).id("sponge:slab_type").name("Slab Type").query(of("SlabType")).build());

        register("sandstone_type", Key.builder().type(TypeTokens.SAND_STONE_VALUE_TOKEN).id("sponge:sandstone_type").name("Sandstone Type").query(of("SandstoneType")).build());

        register("comparator_type", Key.builder().type(TypeTokens.COMPARATOR_VALUE_TOKEN).id("sponge:comparator_type").name("Comparator Type").query(of("ComparatorType")).build());

        register("hinge_position", Key.builder().type(TypeTokens.HINGE_VALUE_TOKEN).id("sponge:hinge_position").name("Hinge Position").query(of("HingePosition")).build());

        register("piston_type", Key.builder().type(TypeTokens.PISTON_VALUE_TOKEN).id("sponge:piston_type").name("Piston Type").query(of("PistonType")).build());

        register("portion_type", Key.builder().type(TypeTokens.PORTION_VALUE_TOKEN).id("sponge:portion_type").name("Portion Type").query(of("PortionType")).build());

        register("rail_direction", Key.builder().type(TypeTokens.RAIL_VALUE_TOKEN).id("sponge:rail_direction").name("Rail Direction").query(of("RailDirection")).build());

        register("stair_shape", Key.builder().type(TypeTokens.STAIR_VALUE_TOKEN).id("sponge:stair_shape").name("Stair Shape").query(of("StairShape")).build());

        register("wall_type", Key.builder().type(TypeTokens.WALL_VALUE_TOKEN).id("sponge:wall_type").name("Wall Type").query(of("WallType")).build());

        register("double_plant_type", Key.builder().type(TypeTokens.DOUBLE_PLANT_VALUE_TOKEN).id("sponge:double_plant_type").name("Double Plant Type").query(of("DoublePlantType")).build());

        register("big_mushroom_type", Key.builder().type(TypeTokens.MUSHROOM_VALUE_TOKEN).id("sponge:big_mushroom_type").name("Big Mushroom Type").query(of("BigMushroomType")).build());

        register("ai_enabled", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_ai_enabled").name("Is Ai Enabled").query(of("IsAiEnabled")).build());

        register("creeper_charged", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_creeper_charged").name("Is Creeper Charged").query(of("IsCreeperCharged")).build());

        register("item_durability", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:item_durability").name("Item Durability").query(of("ItemDurability")).build());

        register("unbreakable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:unbreakable").name("Unbreakable").query(of("Unbreakable")).build());

        register("spawnable_entity_type", Key.builder().type(TypeTokens.ENTITY_TYPE_VALUE_TOKEN).id("sponge:spawnable_entity_type").name("Spawnable Entity Type").query(of("SpawnableEntityType")).build());

        register("fall_distance", Key.builder().type(TypeTokens.FLOAT_VALUE_TOKEN).id("sponge:fall_distance").name("Fall Distance").query(of("FallDistance")).build());

        register("cooldown", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:cooldown").name("Cooldown").query(of("Cooldown")).build());

        register("note_pitch", Key.builder().type(TypeTokens.NOTE_VALUE_TOKEN).id("sponge:note").name("Note").query(of("Note")).build());

        register("vehicle", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("sponge:vehicle").name("Vehicle").query(of("Vehicle")).build());

        register("base_vehicle", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("sponge:base_vehicle").name("Base Vehicle").query(of("BaseVehicle")).build());

        register("art", Key.builder().type(TypeTokens.ART_VALUE_TOKEN).id("sponge:art").name("Art").query(of("Art")).build());

        register("fall_damage_per_block", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:fall_damage_per_block").name("Fall Damage Per Block").query(of("FallDamagePerBlock")).build());

        register("max_fall_damage", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:max_fall_damage").name("Max Fall Damage").query(of("MaxFallDamage")).build());

        register("falling_block_state", Key.builder().type(TypeTokens.BLOCK_VALUE_TOKEN).id("sponge:falling_block_state").name("Falling Block State").query(of("FallingBlockState")).build());

        register("can_place_as_block", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:can_place_as_block").name("Can Place As Block").query(of("CanPlaceAsBlock")).build());

        register("can_drop_as_item", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:can_drop_as_item").name("Can Drop As Item").query(of("CanDropAsItem")).build());

        register("fall_time", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:fall_time").name("Fall Time").query(of("FallTime")).build());

        register("falling_block_can_hurt_entities", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:can_falling_block_hurt_entities").name("Can Falling Block Hurt Entities").query(of("CanFallingBlockHurtEntities")).build());

        register("represented_block", Key.builder().type(TypeTokens.BLOCK_VALUE_TOKEN).id("sponge:represented_block").name("Represented Block").query(of("RepresentedBlock")).build());

        register("offset", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:block_offset").name("Block Offset").query(of("BlockOffset")).build());

        register("attached", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:attached").name("Attached").query(of("Attached")).build());

        register("should_drop", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:should_drop").name("Should Drop").query(of("ShouldDrop")).build());

        register("extended", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:extended").name("Extended").query(of("Extended")).build());

        register("growth_stage", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:growth_stage").name("Growth Stage").query(of("GrowthStage")).build());

        register("open", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:open").name("Open").query(of("Open")).build());

        register("power", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:power").name("Power").query(of("Power")).build());

        register("seamless", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:seamless").name("Seamless").query(of("Seamless")).build());

        register("snowed", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:snowed").name("Snowed").query(of("Snowed")).build());

        register("suspended", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:suspended").name("Suspended").query(of("Suspended")).build());

        register("occupied", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:occupied").name("Occupied").query(of("Occupied")).build());

        register("decayable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:decayable").name("Decayable").query(of("Decayable")).build());

        register("in_wall", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:in_wall").name("In Wall").query(of("InWall")).build());

        register("delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:delay").name("Delay").query(of("Delay")).build());

        register("player_created", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:player_created").name("Player Created").query(of("PlayerCreated")).build());

        register("item_blockstate", Key.builder().type(TypeTokens.BLOCK_VALUE_TOKEN).id("sponge:item_block_state").name("Item Block State").query(of("ItemBlockState")).build());

        register("ocelot_type", Key.builder().type(TypeTokens.OCELOT_VALUE_TOKEN).id("sponge:ocelot_type").name("Ocelot Type").query(of("OcelotType")).build());

        register("rabbit_type", Key.builder().type(TypeTokens.RABBIT_VALUE_TOKEN).id("sponge:rabbit_type").name("Rabbit Type").query(of("RabbitType")).build());

        register("parrot_variant", Key.builder().type(TypeTokens.PARROT_VARIANT_VALUE_TOKEN).id("sponge:parrot_variant").name("Parrot Variant").query(of("ParrotVariant")).build());

        register("lock_token", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("sponge:lock").name("Lock").query(of("Lock")).build());

        register("banner_base_color", Key.builder().type(TypeTokens.DYE_COLOR_VALUE_TOKEN).id("sponge:banner_base_color").name("Banner Base Color").query(of("BannerBaseColor")).build());

        register("banner_patterns", Key.builder().type(TypeTokens.PATTERN_LIST_VALUE_TOKEN).id("sponge:banner_patterns").name("Banner Patterns").query(of("BannerPatterns")).build());

        register("respawn_locations", Key.builder().type(TypeTokens.MAP_UUID_VECTOR3D_VALUE_TOKEN).id("sponge:respawn_locations").name("Respawn Locations").query(of("RespawnLocations")).build());

        register("expiration_ticks", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:expiration_ticks").name("Expiration Ticks").query(of("ExpirationTicks")).build());

        register("skin_unique_id", Key.builder().type(TypeTokens.UUID_VALUE_TOKEN).id("sponge:skin_uuid").name("Skin UUID").query(of("SkinUUID")).build());

        register("moisture", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:moisture").name("Moisture").query(of("Moisture")).build());

        register("angry", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:angry").name("Angry").query(of("Angry")).build());

        register("anger", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:anger").name("Anger").query(of("Anger")).build());

        register("rotation", Key.builder().type(TypeTokens.ROTATION_VALUE_TOKEN).id("sponge:rotation").name("Rotation").query(of("Rotation")).build());

        register("is_splash_potion", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_splash_potion").name("Is Splash Potion").query(of("IsSplashPotion")).build());

        register("affects_spawning", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:affects_spawning").name("Affects Spawning").query(of("AffectsSpawning")).build());

        register("critical_hit", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:critical_hit").name("Critical Hit").query(of("CriticalHit")).build());

        register("generation", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:generation").name("Generation").query(of("Generation")).build());

        register("passengers", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("sponge:passenger_snapshot").name("Passenger Snapshot").query(of("PassengerSnapshot")).build());

        register("knockback_strength", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:knockback_strength").name("Knockback Strength").query(of("KnockbackStrength")).build());

        register("persists", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:persists").name("Persists").query(of("Persists")).build());

        register("stored_enchantments", Key.builder().type(TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN).id("sponge:stored_enchantments").name("Stored Enchantments").query(of("StoredEnchantments")).build());

        register("is_sprinting", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:sprinting").name("Sprinting").query(of("Sprinting")).build());

        register("stuck_arrows", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:stuck_arrows").name("Stuck Arrows").query(of("StuckArrows")).build());

        register("vanish_ignores_collision", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:vanish_ignores_collision").name("Vanish Ignores Collision").query(of("VanishIgnoresCollision")).build());

        register("vanish_prevents_targeting", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:vanish_prevents_targeting").name("Vanish Prevents Targeting").query(of("VanishPreventsTargeting")).build());

        register("is_aflame", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_aflame").name("Is Aflame").query(of("IsAflame")).build());

        register("can_breed", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:can_breed").name("Can Breed").query(of("CanBreed")).build());

        register("fluid_item_stack", Key.builder().type(TypeTokens.FLUID_VALUE_TOKEN).id("sponge:fluid_item_container_snapshot").name("Fluid Item Container Snapshot").query(of("FluidItemContainerSnapshot")).build());

        register("fluid_tank_contents", Key.builder().type(TypeTokens.MAP_DIRECTION_FLUID_VALUE_TOKEN).id("sponge:fluid_tank_contents").name("Fluid Tank Contents").query(of("FluidTankContents")).build());

        register("custom_name_visible", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:custom_name_visible").name("Custom Name Visible").query(of("CustomNameVisible")).build());

        register("first_date_played", Key.builder().type(TypeTokens.INSTANT_VALUE_TOKEN).id("sponge:first_time_joined").name("First Time Joined").query(of("FirstTimeJoined")).build());

        register("last_date_played", Key.builder().type(TypeTokens.INSTANT_VALUE_TOKEN).id("sponge:last_time_played").name("Last Time Played").query(of("LastTimePlayed")).build());

        register("hide_enchantments", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:hide_enchantments").name("Hide Enchantments").query(of("HideEnchantments")).build());

        register("hide_attributes", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:hide_attributes").name("Hide Attributes").query(of("HideAttributes")).build());

        register("hide_unbreakable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:hide_unbreakable").name("Hide Unbreakable").query(of("HideUnbreakable")).build());

        register("hide_can_destroy", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:hide_can_destroy").name("Hide Can Destroy").query(of("HideCanDestroy")).build());

        register("hide_can_place", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:hide_can_place").name("Hide Can Place").query(of("HideCanPlace")).build());

        register("hide_miscellaneous", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:hide_miscellaneous").name("Hide Miscellaneous").query(of("HideMiscellaneous")).build());

        register("potion_effects", Key.builder().type(TypeTokens.LIST_POTION_VALUE_TOKEN).id("sponge:potion_effects").name("Potion Effects").query(of("PotionEffects")).build());

        register("body_rotations", Key.builder().type(TypeTokens.MAP_BODY_VECTOR3D_VALUE_TOKEN).id("sponge:body_rotations").name("Body Rotations").query(of("BodyRotations")).build());

        register("head_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:head_rotation").name("Head Rotation").query(of("HeadRotation")).build());

        register("chest_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:chest_rotation").name("Chest Rotation").query(of("ChestRotation")).build());

        register("left_arm_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:left_arm_rotation").name("Left Arm Rotation").query(of("LeftArmRotation")).build());

        register("right_arm_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:right_arm_rotation").name("Right Arm Rotation").query(of("RightArmRotation")).build());

        register("left_leg_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:left_leg_rotation").name("Left Leg Rotation").query(of("LeftLegRotation")).build());

        register("right_leg_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:right_leg_rotation").name("Right Leg Rotation").query(of("RightLegRotation")).build());

        register("beacon_primary_effect", Key.builder().type(TypeTokens.OPTIONAL_POTION_VALUE_TOKEN).id("sponge:beacon_primary_effect").name("Beacon Primary Effect").query(of("BeaconPrimaryEffect")).build());

        register("beacon_secondary_effect", Key.builder().type(TypeTokens.OPTIONAL_POTION_VALUE_TOKEN).id("sponge:beacon_secondary_effect").name("Beacon Secondary Effect").query(of("BeaconSecondaryEffect")).build());

        register("targeted_location", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("sponge:targeted_vector_3d").name("Targeted Vector3d").query(of("TargetedVector3d")).build());

        register("fuse_duration", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:fuse_duration").name("Fuse Duration").query(of("FuseDuration")).build());

        register("ticks_remaining", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:ticks_remaining").name("Ticks Remaining").query(of("TicksRemaining")).build());

        register("explosion_radius", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:explosion_radius").name("Explosion Radius").query(of("ExplosionRadius")).build());

        register("armor_stand_has_arms", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:has_arms").name("Has Arms").query(of("HasArms")).build());

        register("armor_stand_has_base_plate", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:has_base_plate").name("Has Base Plate").query(of("HasBasePlate")).build());

        register("armor_stand_marker", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_marker").name("Is Marker").query(of("IsMarker")).build());

        register("armor_stand_is_small", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_small").name("Is Small").query(of("IsSmall")).build());

        register("is_silent", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_silent").name("Is Silent").query(of("IsSilent")).build());

        register("glowing", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:glowing").name("Glowing").query(of("Glowing")).build());

        register("pickup_rule", Key.builder().type(TypeTokens.PICKUP_VALUE_TOKEN).id("sponge:pickup_rule").name("Pickup Rule").query(of("PickupRule")).build());

        register("invulnerability_ticks", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:invulnerability_ticks").name("Invulnerability Ticks").query(of("HurtTime")).build());
        register("invulnerable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:invulnerable").name("Invulnerable").query(of("Invulnerable")).build());

        register("has_gravity", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:has_gravity").name("Has Gravity").query(of("HasGravity")).build());

        register("statistics", Key.builder().type(TypeTokens.STATISTIC_MAP_VALUE_TOKEN).id("sponge:statistics").name("Statistics").query(of("Statistics")).build());

        register("infinite_despawn_delay", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:infinite_despawn_delay").name("Infinite Despawn Delay").query(of("InfiniteDespawnDelay")).build());

        register("infinite_pickup_delay", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:infinite_pickup_delay").name("Infinite Pickup Delay").query(of("InfinitePickupDelay")).build());

        register("despawn_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:despawn_delay").name("Despawn Delay").query(of("DespawnDelay")).build());

        register("pickup_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:pickup_delay").name("Pickup Delay").query(of("PickupDelay")).build());

        register("end_gateway_age", Key.builder().type(TypeTokens.LONG_VALUE_TOKEN).id("sponge:end_gateway_age").name("End Gateway Age").query(of("EndGatewayAge")).build());
        register("end_gateway_teleport_cooldown", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("sponge:end_gateway_teleport_cooldown").name("End Gateway Teleport Cooldown").query(of("EndGatewayTeleportCooldown")).build());
        register("exit_position", Key.builder().type(TypeTokens.VECTOR_3I_VALUE_TOKEN).id("sponge:exit_position").name("Exit Position").query(of("ExitPosition")).build());
        register("exact_teleport", Key.builder().type(TypeTokens.VECTOR_3I_VALUE_TOKEN).id("sponge:exact_teleport").name("Exact Teleport").query(of("ExactTeleport")).build());
        register("structure_author", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("sponge:structure_author").name("Structure Author").query(of("StructureAuthor")).build());
        register("structure_ignore_entities", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:structure_ignore_entities").name("Structure Ignore Entities").query(of("StructureIgnoreEntities")).build());
        register("structure_integrity", Key.builder().type(TypeTokens.FLOAT_VALUE_TOKEN).id("sponge:structure_integrity").name("Structure Integrity").query(of("StructureIntegrity")).build());
        register("structure_mode", Key.builder().type(TypeTokens.STRUCTURE_MODE_VALUE_TOKEN).id("sponge:structure_mode").name("Structure Mode").query(of("StructureMode")).build());
        register("structure_position", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("sponge:structure_position").name("Structure Position").query(of("StructurePosition")).build());
        register("structure_powered", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("sponge:structure_powered").name("Structure Powered").query(of("StructurePowered")).build());
        register("structure_seed", Key.builder().type(TypeTokens.LONG_VALUE_TOKEN).id("sponge:structure_seed").name("Structure Seed").query(of("StructureSeed")).build());
        register("structure_show_air", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:structure_show_air").name("Structure Show Air").query(of("StructureShowAir")).build());
        register("structure_show_bounding_box", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:structure_show_bounding_box").name("Structure Show Bounding Box").query(of("StructureShowBoundingBox")).build());
        register("structure_size", Key.builder().type(TypeTokens.VECTOR_3I_VALUE_TOKEN).id("sponge:structure_size").name("Structure Size").query(of("StructureSize")).build());
        register("absorption", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:absorption").name("Absorption").query(of("Absorption")).build());

        register("area_effect_cloud_radius", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:area_effect_cloud_radius").name("AreaEffectCloud Radius").query(of("CloudRadius")).build());

        register("area_effect_cloud_radius_on_use", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:area_effect_cloud_radius_on_use").name("AreaEffectCloud Radius On Use").query(of("CloudRadiusOnUse")).build());

        register("area_effect_cloud_radius_per_tick", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:area_effect_cloud_radius_per_tick").name("AreaEffectCloud Radius Per Tick").query(of("CloudRadiusPerTick")).build());

        register("area_effect_cloud_color", Key.builder().type(TypeTokens.COLOR_VALUE_TOKEN).id("sponge:area_effect_cloud_color").name("AreaEffectCloud Color").query(of("CloudColor")).build());

        register("area_effect_cloud_duration", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:area_effect_cloud_duration").name("AreaEffectCloud Duration").query(of("CloudDuration")).build());

        register("area_effect_cloud_duration_on_use", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:area_effect_cloud_duration_on_use").name("AreaEffectCloud Duration On Use").query(of("CloudDurationOnUse")).build());

        register("area_effect_cloud_wait_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:area_effect_cloud_wait_time").name("AreaEffectCloud Wait Time").query(of("CloudWaitTime")).build());

        register("area_effect_cloud_reapplication_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:area_effect_cloud_wait_time").name("AreaEffectCloud Wait Time").query(of("CloudReapplicationDelay")).build());

        register("area_effect_cloud_age", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:area_effect_cloud_age").name("AreaEffectCloud Age").query(of("CloudAge")).build());

        register("area_effect_cloud_particle_type", Key.builder().type(TypeTokens.PARTICLE_TYPE_VALUE_TOKEN).id("sponge:area_effect_cloud_particle_type").name("AreaEffectCloud ParticleType").query(of("CloudParticleType")).build());

        register("age", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:entity_age").name("Entity Age").query(of("EntityAge")).build());

        register("attack_damage", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:entity_attack_damage").name("Entity Attack Damage").query(of("EntityAttackDamage")).build());

        register("base_size", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:base_size").name("Entity Base Size").query(of("EntityBaseSize")).build());

        register("damage_entity_map", Key.builder().type(TypeTokens.ENTITY_TYPE_DOUBLE_MAP_VALUE_TOKEN).id("sponge:entity_type_damage_map").name("Entity Type Damage Map").query(of("DamageEntityTypeMap")).build());

        register("dominant_hand", Key.builder().type(TypeTokens.HAND_PREFERENCE_VALUE_TOKEN).id("sponge:hand_preference").name("Hand Preference").query(of("HandPreference")).build());

        register("filled", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:filled").name("Filled").query(of("Filled")).build());

        register("fluid_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:fluid_level").name("Fluid Level").query(of("LiquidLevel")).build());

        register("health_scale", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:health_scale").name("Health Scale").query(of("HealthScale")).build());

        register("height", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:entity_height").name("Entity Height").query(of("EntityHeight")).build());

        register("held_experience", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:held_experience").name("Held Experience").query(of("HeldExperience")).build());

        register("is_sleeping", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_sleeping").name("Is Sleeping").query(of("IsSleeping")).build());

        register("is_johnny", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_johnny").name("Is Johnny").query(of("IsJohnny")).build());

        // Deprecated
        register("johnny_vindicator", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:johnny_vindicator").name("Johnny Vindicator").query(of("JohnnyVindicator")).build());

        register("last_attacker", Key.builder().type(TypeTokens.OPTIONAL_ENTITY_SNAPSHOT_VALUE_TOKEN).id("sponge:last_attacker").name("Last Attacker").query(of("LastAttacker")).build());

        register("last_damage", Key.builder().type(TypeTokens.OPTIONAL_DOUBLE_VALUE_TOKEN).id("sponge:last_damage").name("Last Damage Taken").query(of("LastDamage")).build());

        register("llama_strength", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:llama_strength").name("Llama Strength").query(of("LlamaStrength")).build());

        register("llama_variant", Key.builder().type(TypeTokens.LLAMA_VARIANT_VALUE_TOKEN).id("sponge:llama_variant").name("Llama Variant").query(of("LlamaVariant")).build());

        register("scale", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("sponge:entity_scale").name("Entity Scale").query(of("EntityScale")).build());

        register("will_shatter", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:will_shatter").name("Will Shatter").query(of("WillShatter")).build());

        register("wire_attachments", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_MAP_VALUE_TOKEN).id("sponge:wire_attachment_map").name("Wire Attachment Map").query(of("WireAttachmentMap")).build());

        register("wire_attachment_east", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("sponge:wire_attachment_east").name("Wire Attachment East").query(of("WireAttachmentEast")).build());
        register("wire_attachment_south", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("sponge:wire_attachment_south").name("Wire Attachment South").query(of("WireAttachmentSouth")).build());
        register("wire_attachment_north", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("sponge:wire_attachment_north").name("Wire Attachment North").query(of("WireAttachmentNorth")).build());
        register("wire_attachment_west", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("sponge:wire_attachment_west").name("Wire Attachment West").query(of("WireAttachmentWest")).build());

        register("age", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("sponge:age").name("Age").query(of("Age")).build());
        register("is_adult", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_adult").name("Is Adult").query(of("IsAdult")).build());
        register("is_baby", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_baby").name("Is Baby").query(of("IsBaby")).build());

        register("health_scale", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("sponge:health_scale").name("Health Scale").query(of("HealthScale")).build());

        register("is_elytra_flying", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sponge:is_elytra_flying").name("Is Elytra Flying").query(of("ElytraFlying")).build());

        // All sponge provided keys are belong to sponge. Other plugins are going to have their own keys with their own plugin containers
        Sponge.getCauseStackManager().popCause();

    }

    private void register(String fieldName, Key<?> key) {
        this.fieldMap.put(fieldName, key);
        this.keyMap.put(key.getId().toLowerCase(Locale.ENGLISH), key);
    }

    @Override
    public void registerAdditionalCatalog(Key<?> extraCatalog) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Cannot register new Keys after Data Registration has completed!");
        checkNotNull(extraCatalog, "Key cannot be null!");
        final PluginContainer parent = ((SpongeKey) extraCatalog).getParent();
        final String pluginId = parent.getId().toLowerCase(Locale.ENGLISH);
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        final String[] split = id.split(":");
        checkArgument(split.length == 2, "Key id's have to be in two parts! The first part being the plugin id, the second part being the key's individual id. Currently you have: " + Arrays.toString(split));
        checkArgument(split[0].equals(pluginId),  "A plugin is trying to register custom keys under a different plugin id namespace! This is unsupported! The provided key: " + id);
        this.keyMap.put(id, extraCatalog);
    }

    @Override
    public Optional<Key<?>> getById(String id) {
        return Optional.ofNullable(this.keyMap.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Key<?>> getAll() {
        return Collections.unmodifiableCollection(this.keyMap.values());
    }

    KeyRegistryModule() {
    }

    public void registerKeyListeners() {
        for (Key<?> key : this.keyMap.values()) {
            ((SpongeKey) key).registerListeners();
        }
    }

    public void registerForEntityClass(Class<? extends Entity> cls) {
        try {
            List<DataParameterConverter<?>> converters = LOADED_CLASSES.computeIfAbsent(cls, k -> new ArrayList<>());
            final Callable<List<DataParameterConverter<?>>> callable = DATA_PARAMETER_FUNCTION_GETTERS.get(cls);
            if (callable != null) {
                final List<DataParameterConverter<?>> call = callable.call();
                converters.addAll(call);
                // just need to call, the constructor should perform the actual registration to the parameter.
            }
            // Then start climbing the hierarchy
            Class<?> clazz = cls.getSuperclass();
            do {
                List<DataParameterConverter<?>> superConverters = LOADED_CLASSES.computeIfAbsent(clazz, k -> new ArrayList<>());

                final Callable<List<DataParameterConverter<?>>> listCallable = DATA_PARAMETER_FUNCTION_GETTERS.get(clazz);
                if (listCallable != null) {
                    final List<DataParameterConverter<?>> call = listCallable.call();
                    superConverters.addAll(call);
                    converters.addAll(call);
                    // just need to call, the constructor should perform the actual registration to the parameter.
                }
                clazz = clazz.getSuperclass();
            }  while (clazz.getSuperclass() != Object.class && !LOADED_CLASSES.containsKey(clazz));
        } catch (Exception e) {
            // we don't care about exceptions
        }
    }

    // This is to avoid duplication of calling converters and creating them.
    // Likewise, this will allow us to maybe do some super management
    // of multiple changes in one go.
    private static ConcurrentHashMap<Class<?>, List<DataParameterConverter<?>>> LOADED_CLASSES = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Entity>, Callable<List<DataParameterConverter<?>>>> DATA_PARAMETER_FUNCTION_GETTERS = ImmutableMap.<Class<? extends Entity>, Callable<List<DataParameterConverter<?>>>>builder()
        .put(Entity.class, () -> {
            final ArrayList<DataParameterConverter<?>> objects = new ArrayList<>();
            objects.add(new EntityFlagsConverter());
            objects.add(new EntityCustomNameVisibleConverter());
            objects.add(new EntitySilentConverter());
            objects.add(new EntityAirConverter());
            objects.add(new EntityCustomNameConverter());
            objects.add(new EntityNoGravityConverter());
            return objects;
        })
        .build();

    static final class Holder {
        static final KeyRegistryModule INSTANCE = new KeyRegistryModule();
    }
}
