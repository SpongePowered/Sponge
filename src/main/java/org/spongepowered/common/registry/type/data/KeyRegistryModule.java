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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.SpongeKey;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.data.datasync.entity.EntityAirConverter;
import org.spongepowered.common.data.datasync.entity.EntityBabyConverter;
import org.spongepowered.common.data.datasync.entity.EntityCustomNameConverter;
import org.spongepowered.common.data.datasync.entity.EntityCustomNameVisibleConverter;
import org.spongepowered.common.data.datasync.entity.EntityFlagsConverter;
import org.spongepowered.common.data.datasync.entity.EntityLivingAIFlagsConverter;
import org.spongepowered.common.data.datasync.entity.EntityLivingBaseArrowCountConverter;
import org.spongepowered.common.data.datasync.entity.EntityLivingBaseHealthConverter;
import org.spongepowered.common.data.datasync.entity.EntityNoGravityConverter;
import org.spongepowered.common.data.datasync.entity.EntitySilentConverter;
import org.spongepowered.common.registry.type.data.KeyRegistryModule.Holder;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

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

@RegistrationDependency(EntityTypeRegistryModule.class)
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
        Sponge.getCauseStackManager().pushCause(SpongeImpl.getSpongePlugin());

        this.register("axis", Key.builder().type(TypeTokens.AXIS_VALUE_TOKEN).id("axis").name("Axis").query(of("Axis")).build());

        this.register("color", Key.builder().type(TypeTokens.COLOR_VALUE_TOKEN).id("color").name("Color").query(of("Color")).build());

        this.register("health", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("health").name("Health").query(of("Health")).build());

        this.register("max_health", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("max_health").name("Max Health").query(of("MaxHealth")).build());

        this.register("display_name", Key.builder().type(TypeTokens.TEXT_VALUE_TOKEN).id("display_name").name("Display Name").query(of("DisplayName")).build());

        this.register("career", Key.builder().type(TypeTokens.CAREER_VALUE_TOKEN).id("career").name("Career").query(of("Career")).build());

        this.register("sign_lines", Key.builder().type(TypeTokens.LIST_TEXT_VALUE_TOKEN).id("sign_lines").name("Sign Lines").query(of("SignLines")).build());

        this.register("skull_type", Key.builder().type(TypeTokens.SKULL_VALUE_TOKEN).id("skull_type").name("Skull Type").query(of("SkullType")).build());

        this.register("is_sneaking", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sneaking").name("Is Sneaking").query(of("IsSneaking")).build());

        this.register("velocity", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("velocity").name("Velocity").query(of("Velocity")).build());

        this.register("acceleration", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("acceleration").name("Acceleration").query(of("Acceleration")).build());

        this.register("food_level", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("food_level").name("Food Level").query(of("FoodLevel")).build());

        this.register("saturation", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("food_saturation_level").name("Food Saturation Level").query(of("FoodSaturationLevel")).build());

        this.register("exhaustion", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("food_exhaustion_level").name("Food Exhaustion Level").query(of("FoodExhaustionLevel")).build());

        this.register("max_air", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("max_air").name("Max Air").query(of("MaxAir")).build());

        this.register("remaining_air", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("remaining_air").name("Remaining Air").query(of("RemainingAir")).build());

        this.register("fire_ticks", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("fire_ticks").name("Fire Ticks").query(of("FireTicks")).build());

        this.register("fire_damage_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("fire_damage_delay").name("Fire Damage Delay").query(of("FireDamageDelay")).build());

        this.register("game_mode", Key.builder().type(TypeTokens.GAME_MODE_VALUE_TOKEN).id("game_mode").name("Game Mode").query(of("GameMode")).build());

        this.register("is_screaming", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("screaming").name("Is Screaming").query(of("IsScreaming")).build());

        this.register("can_fly", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("can_fly").name("Can Fly").query(of("CanFly")).build());

        this.register("can_grief", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("can_grief").name("Can Grief").query(of("CanGrief")).build());

        this.register("shrub_type", Key.builder().type(TypeTokens.SHRUB_VALUE_TOKEN).id("shrub_type").name("Shrub Type").query(of("ShrubType")).build());

        this.register("plant_type", Key.builder().type(TypeTokens.PLANT_VALUE_TOKEN).id("plant_type").name("Plant Type").query(of("PlantType")).build());

        this.register("tree_type", Key.builder().type(TypeTokens.TREE_VALUE_TOKEN).id("tree_type").name("Tree Type").query(of("TreeType")).build());

        this.register("log_axis", Key.builder().type(TypeTokens.LOG_AXIS_VALUE_TOKEN).id("log_axis").name("Log Axis").query(of("LogAxis")).build());

        this.register("invisible", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("invisible").name("Invisible").query(of("Invisible")).build());

        this.register("vanish", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("vanish").name("Vanish").query(of("Vanish")).build());

        this.register("invisible", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("invisible").name("Invisible").query(of("Invisible")).build());

        this.register("powered", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("powered").name("Powered").query(of("Powered")).build());

        this.register("layer", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("layer").name("Layer").query(of("Layer")).build());

        this.register("represented_item", Key.builder().type(TypeTokens.ITEM_SNAPSHOT_VALUE_TOKEN).id("item_stack_snapshot").name("Item Stack Snapshot").query(of("ItemStackSnapshot")).build());

        this.register("command", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("command").name("Command").query(of("Command")).build());

        this.register("success_count", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("success_count").name("SuccessCount").query(of("SuccessCount")).build());

        this.register("tracks_output", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("tracks_output").name("Tracks Output").query(of("TracksOutput")).build());

        this.register("last_command_output", Key.builder().type(TypeTokens.OPTIONAL_TEXT_VALUE_TOKEN).id("last_command_output").name("Last Command Output").query(of("LastCommandOutput")).build());

        this.register("trade_offers", Key.builder().type(TypeTokens.LIST_VALUE_TRADE_OFFER_TOKEN).id("trade_offers").name("Trade Offers").query(of("TradeOffers")).build());

        this.register("dye_color", Key.builder().type(TypeTokens.DYE_COLOR_VALUE_TOKEN).id("dye_color").name("Dye Color").query(of("DyeColor")).build());

        this.register("firework_flight_modifier", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("flight_modifier").name("Flight Modifier").query(of("FlightModifier")).build());

        this.register("firework_effects", Key.builder().type(TypeTokens.LIST_VALUE_FIREWORK_TOKEN).id("firework_effects").name("Firework Effects").query(of("FireworkEffects")).build());

        this.register("spawner_entities", Key.builder().type(TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_COLLECTION_VALUE_TOKEN).id("spawner_entities").name("Spawner Entities").query(of("SpawnerEntities")).build());

        this.register("spawner_maximum_delay", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_maximum_delay").name("Spawner Maximum Delay").query(of("SpawnerMaximumDelay")).build());

        this.register("spawner_maximum_nearby_entities", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_maximum_nearby_entities").name("Spawner Maximum Nearby Entities").query(of("SpawnerMaximumNearbyEntities")).build());

        this.register("spawner_minimum_delay", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_minimum_delay").name("Spawner Minimum Delay").query(of("SpawnerMinimumDelay")).build());

        this.register("spawner_next_entity_to_spawn", Key.builder().type(TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_VALUE_TOKEN).id("spawner_next_entity_to_spawn").name("Spawner Next Entity To Spawn").query(of("SpawnerNextEntityToSpawn")).build());

        this.register("spawner_remaining_delay", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_remaining_delay").name("Spawner Remaining Delay").query(of("SpawnerRemainingDelay")).build());

        this.register("spawner_required_player_range", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_required_player_range").name("Spawner Required Player Range").query(of("SpawnerRequiredPlayerRange")).build());

        this.register("spawner_spawn_count", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_spawn_count").name("Spawner Spawn Count").query(of("SpawnerSpawnCount")).build());

        this.register("spawner_spawn_range", Key.builder().type(TypeTokens.BOUNDED_SHORT_VALUE_TOKEN).id("spawner_spawn_range").name("Spawner Spawn Range").query(of("SpawnerSpawnRange")).build());

        this.register("connected_directions", Key.builder().type(TypeTokens.SET_DIRECTION_VALUE_TOKEN).id("connected_directions").name("Connected Directions").query(of("ConnectedDirections")).build());

        this.register("connected_north", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("connected_north").name("Connected North").query(of("ConnectedNorth")).build());

        this.register("connected_south", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("connected_south").name("Connected South").query(of("ConnectedSouth")).build());

        this.register("connected_east", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("connected_east").name("Connected East").query(of("ConnectedEast")).build());

        this.register("connected_west", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("connected_west").name("Connected West").query(of("ConnectedWest")).build());

        this.register("direction", Key.builder().type(TypeTokens.DIRECTION_VALUE_TOKEN).id("direction").name("Direction").query(of("Direction")).build());

        this.register("dirt_type", Key.builder().type(TypeTokens.DIRT_VALUE_TOKEN).id("dirt_type").name("Dirt Type").query(of("DirtType")).build());

        this.register("disguised_block_type", Key.builder().type(TypeTokens.DISGUISED_BLOCK_VALUE_TOKEN).id("disguised_block_type").name("Disguised Block Type").query(of("DisguisedBlockType")).build());

        this.register("disarmed", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("disarmed").name("Disarmed").query(of("Disarmed")).build());

        this.register("item_enchantments", Key.builder().type(TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN).id("item_enchantments").name("Item EnchantmentTypes").query(of("ItemEnchantments")).build());

        this.register("banner_patterns", Key.builder().type(TypeTokens.LIST_PATTERN_VALUE_TOKEN).id("banner_patterns").name("Banner Patterns").query(of("BannerPatterns")).build());

        this.register("banner_base_color", Key.builder().type(TypeTokens.LIST_DYE_COLOR_VALUE_TOKEN).id("banner_base_color").name("Banner Base Color").query(of("BannerBaseColor")).build());

        this.register("horse_color", Key.builder().type(TypeTokens.HORSE_COLOR_VALUE_TOKEN).id("horse_color").name("Horse Color").query(of("HorseColor")).build());

        this.register("horse_style", Key.builder().type(TypeTokens.HORSE_STYLE_VALUE_TOKEN).id("horse_style").name("Horse Style").query(of("HorseStyle")).build());

        this.register("item_lore", Key.builder().type(TypeTokens.LIST_TEXT_VALUE_TOKEN).id("item_lore").name("Item Lore").query(of("ItemLore")).build());

        this.register("book_pages", Key.builder().type(TypeTokens.LIST_TEXT_VALUE_TOKEN).id("book_pages").name("Book Pages").query(of("BookPages")).build());

        this.register("plain_book_pages", Key.builder().type(TypeTokens.LIST_STRING_VALUE_TOKEN).id("plain_book_pages").name("Plain Book Pages").query(of("PlainBookPages")).build());

        this.register("golden_apple_type", Key.builder().type(TypeTokens.GOLDEN_APPLE_VALUE_TOKEN).id("golden_apple_type").name("Golden Apple Type").query(of("GoldenAppleType")).build());

        this.register("is_flying", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_flying").name("Is Flying").query(of("IsFlying")).build());

        this.register("experience_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("experience_level").name("Experience Level").query(of("ExperienceLevel")).build());

        this.register("total_experience", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("total_experience").name("Total Experience").query(of("TotalExperience")).build());

        this.register("experience_since_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("experience_since_level").name("Experience Since Level").query(of("ExperienceSinceLevel")).build());

        this.register("experience_from_start_of_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("experience_from_start_of_level").name("Experience From Start Of Level").query(of("ExperienceFromStartOfLevel")).build());

        this.register("book_author", Key.builder().type(TypeTokens.TEXT_VALUE_TOKEN).id("book_author").name("Book Author").query(of("BookAuthor")).build());

        this.register("breakable_block_types", Key.builder().type(TypeTokens.SET_BLOCK_VALUE_TOKEN).id("can_destroy").name("Can Destroy").query(of("CanDestroy")).build());

        this.register("placeable_blocks", Key.builder().type(TypeTokens.SET_BLOCK_VALUE_TOKEN).id("can_place_on").name("Can Place On").query(of("CanPlaceOn")).build());

        this.register("walking_speed", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("walking_speed").name("Walking Speed").query(of("WalkingSpeed")).build());

        this.register("flying_speed", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("flying_speed").name("Flying Speed").query(of("FlyingSpeed")).build());

        this.register("slime_size", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("slime_size").name("Slime Size").query(of("SlimeSize")).build());

        this.register("villager_zombie_profession", Key.builder().type(TypeTokens.OPTIONAL_PROFESSION_VALUE_TOKEN).id("villager_zombie_profession").name("Villager Zombie Profession").query(of("VillagerZombieProfession")).build());

        this.register("is_playing", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_playing").name("Is Playing").query(of("IsPlaying")).build());

        this.register("is_sitting", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_sitting").name("Is Sitting").query(of("IsSitting")).build());

        this.register("is_sheared", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_sheared").name("Is Sheared").query(of("IsSheared")).build());

        this.register("pig_saddle", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_pig_saddled").name("Is Pig Saddled").query(of("IsPigSaddled")).build());

        this.register("tamed_owner", Key.builder().type(TypeTokens.OPTIONAL_UUID_VALUE_TOKEN).id("tamer_uuid").name("Tamer UUID").query(of("TamerUUID")).build());

        this.register("is_wet", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_wet").name("Is Wet").query(of("IsWet")).build());

        this.register("elder_guardian", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("elder").name("Elder").query(of("Elder")).build());

        this.register("coal_type", Key.builder().type(TypeTokens.COAL_VALUE_TOKEN).id("coal_type").name("Coal Type").query(of("CoalType")).build());

        this.register("cooked_fish", Key.builder().type(TypeTokens.COOKED_FISH_VALUE_TOKEN).id("cooked_fish_type").name("Cooked Fish Type").query(of("CookedFishType")).build());

        this.register("fish_type", Key.builder().type(TypeTokens.FISH_VALUE_TOKEN).id("raw_fish_type").name("Raw Fish Type").query(of("RawFishType")).build());

        this.register("represented_player", Key.builder().type(TypeTokens.GAME_PROFILE_VALUE_TOKEN).id("represented_player").name("Represented Player").query(of("RepresentedPlayer")).build());

        this.register("passed_burn_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("passed_burn_time").name("Passed Burn Time").query(of("PassedBurnTime")).build());

        this.register("max_burn_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("max_burn_time").name("Max Burn Time").query(of("MaxBurnTime")).build());

        this.register("passed_cook_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("passed_cook_time").name("Passed Cook Time").query(of("PassedCookTime")).build());

        this.register("max_cook_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("max_cook_time").name("Max Cook Time").query(of("MaxCookTime")).build());

        this.register("contained_experience", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("contained_experience").name("Contained Experience").query(of("ContainedExperience")).build());

        this.register("remaining_brew_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("remaining_brew_time").name("Remaining Brew Time").query(of("RemainingBrewTime")).build());

        this.register("stone_type", Key.builder().type(TypeTokens.STONE_VALUE_TOKEN).id("stone_type").name("Stone Type").query(of("StoneType")).build());

        this.register("prismarine_type", Key.builder().type(TypeTokens.PRISMARINE_VALUE_TOKEN).id("prismarine_type").name("Prismarine Type").query(of("PrismarineType")).build());

        this.register("brick_type", Key.builder().type(TypeTokens.BRICK_VALUE_TOKEN).id("brick_type").name("Brick Type").query(of("BrickType")).build());

        this.register("quartz_type", Key.builder().type(TypeTokens.QUARTZ_VALUE_TOKEN).id("quartz_type").name("Quartz Type").query(of("QuartzType")).build());

        this.register("sand_type", Key.builder().type(TypeTokens.SAND_VALUE_TOKEN).id("sand_type").name("Sand Type").query(of("SandType")).build());

        this.register("sandstone_type", Key.builder().type(TypeTokens.SAND_STONE_VALUE_TOKEN).id("sandstone_type").name("Sandstone Type").query(of("SandstoneType")).build());

        this.register("slab_type", Key.builder().type(TypeTokens.SLAB_VALUE_TOKEN).id("slab_type").name("Slab Type").query(of("SlabType")).build());

        this.register("sandstone_type", Key.builder().type(TypeTokens.SAND_STONE_VALUE_TOKEN).id("sandstone_type").name("Sandstone Type").query(of("SandstoneType")).build());

        this.register("comparator_type", Key.builder().type(TypeTokens.COMPARATOR_VALUE_TOKEN).id("comparator_type").name("Comparator Type").query(of("ComparatorType")).build());

        this.register("hinge_position", Key.builder().type(TypeTokens.HINGE_VALUE_TOKEN).id("hinge_position").name("Hinge Position").query(of("HingePosition")).build());

        this.register("piston_type", Key.builder().type(TypeTokens.PISTON_VALUE_TOKEN).id("piston_type").name("Piston Type").query(of("PistonType")).build());

        this.register("portion_type", Key.builder().type(TypeTokens.PORTION_VALUE_TOKEN).id("portion_type").name("Portion Type").query(of("PortionType")).build());

        this.register("rail_direction", Key.builder().type(TypeTokens.RAIL_VALUE_TOKEN).id("rail_direction").name("Rail Direction").query(of("RailDirection")).build());

        this.register("stair_shape", Key.builder().type(TypeTokens.STAIR_VALUE_TOKEN).id("stair_shape").name("Stair Shape").query(of("StairShape")).build());

        this.register("wall_type", Key.builder().type(TypeTokens.WALL_VALUE_TOKEN).id("wall_type").name("Wall Type").query(of("WallType")).build());

        this.register("double_plant_type", Key.builder().type(TypeTokens.DOUBLE_PLANT_VALUE_TOKEN).id("double_plant_type").name("Double Plant Type").query(of("DoublePlantType")).build());

        this.register("big_mushroom_type", Key.builder().type(TypeTokens.MUSHROOM_VALUE_TOKEN).id("big_mushroom_type").name("Big Mushroom Type").query(of("BigMushroomType")).build());

        this.register("ai_enabled", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_ai_enabled").name("Is Ai Enabled").query(of("IsAiEnabled")).build());

        this.register("creeper_charged", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_creeper_charged").name("Is Creeper Charged").query(of("IsCreeperCharged")).build());

        this.register("item_durability", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("item_durability").name("Item Durability").query(of("ItemDurability")).build());

        this.register("unbreakable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("unbreakable").name("Unbreakable").query(of("Unbreakable")).build());

        this.register("spawnable_entity_type", Key.builder().type(TypeTokens.ENTITY_TYPE_VALUE_TOKEN).id("spawnable_entity_type").name("Spawnable Entity Type").query(of("SpawnableEntityType")).build());

        this.register("fall_distance", Key.builder().type(TypeTokens.FLOAT_VALUE_TOKEN).id("fall_distance").name("Fall Distance").query(of("FallDistance")).build());

        this.register("cooldown", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("cooldown").name("Cooldown").query(of("Cooldown")).build());

        this.register("note_pitch", Key.builder().type(TypeTokens.NOTE_VALUE_TOKEN).id("note").name("Note").query(of("Note")).build());

        this.register("vehicle", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("vehicle").name("Vehicle").query(of("Vehicle")).build());

        this.register("base_vehicle", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("base_vehicle").name("Base Vehicle").query(of("BaseVehicle")).build());

        this.register("art", Key.builder().type(TypeTokens.ART_VALUE_TOKEN).id("art").name("Art").query(of("Art")).build());

        this.register("fall_damage_per_block", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("fall_damage_per_block").name("Fall Damage Per Block").query(of("FallDamagePerBlock")).build());

        this.register("max_fall_damage", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("max_fall_damage").name("Max Fall Damage").query(of("MaxFallDamage")).build());

        this.register("falling_block_state", Key.builder().type(TypeTokens.BLOCK_VALUE_TOKEN).id("falling_block_state").name("Falling Block State").query(of("FallingBlockState")).build());

        this.register("can_place_as_block", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("can_place_as_block").name("Can Place As Block").query(of("CanPlaceAsBlock")).build());

        this.register("can_drop_as_item", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("can_drop_as_item").name("Can Drop As Item").query(of("CanDropAsItem")).build());

        this.register("fall_time", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("fall_time").name("Fall Time").query(of("FallTime")).build());

        this.register("falling_block_can_hurt_entities", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("can_falling_block_hurt_entities").name("Can Falling Block Hurt Entities").query(of("CanFallingBlockHurtEntities")).build());

        this.register("represented_block", Key.builder().type(TypeTokens.BLOCK_VALUE_TOKEN).id("represented_block").name("Represented Block").query(of("RepresentedBlock")).build());

        this.register("offset", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("block_offset").name("Block Offset").query(of("BlockOffset")).build());

        this.register("attached", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("attached").name("Attached").query(of("Attached")).build());

        this.register("should_drop", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("should_drop").name("Should Drop").query(of("ShouldDrop")).build());

        this.register("extended", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("extended").name("Extended").query(of("Extended")).build());

        this.register("growth_stage", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("growth_stage").name("Growth Stage").query(of("GrowthStage")).build());

        this.register("open", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("open").name("Open").query(of("Open")).build());

        this.register("power", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("power").name("Power").query(of("Power")).build());

        this.register("seamless", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("seamless").name("Seamless").query(of("Seamless")).build());

        this.register("snowed", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("snowed").name("Snowed").query(of("Snowed")).build());

        this.register("suspended", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("suspended").name("Suspended").query(of("Suspended")).build());

        this.register("occupied", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("occupied").name("Occupied").query(of("Occupied")).build());

        this.register("decayable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("decayable").name("Decayable").query(of("Decayable")).build());

        this.register("in_wall", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("in_wall").name("In Wall").query(of("InWall")).build());

        this.register("delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("delay").name("Delay").query(of("Delay")).build());

        this.register("player_created", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("player_created").name("Player Created").query(of("PlayerCreated")).build());

        this.register("item_blockstate", Key.builder().type(TypeTokens.BLOCK_VALUE_TOKEN).id("item_block_state").name("Item Block State").query(of("ItemBlockState")).build());

        this.register("ocelot_type", Key.builder().type(TypeTokens.OCELOT_VALUE_TOKEN).id("ocelot_type").name("Ocelot Type").query(of("OcelotType")).build());

        this.register("rabbit_type", Key.builder().type(TypeTokens.RABBIT_VALUE_TOKEN).id("rabbit_type").name("Rabbit Type").query(of("RabbitType")).build());

        this.register("parrot_variant", Key.builder().type(TypeTokens.PARROT_VARIANT_VALUE_TOKEN).id("parrot_variant").name("Parrot Variant").query(of("ParrotVariant")).build());

        this.register("lock_token", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("lock").name("Lock").query(of("Lock")).build());

        this.register("banner_base_color", Key.builder().type(TypeTokens.DYE_COLOR_VALUE_TOKEN).id("banner_base_color").name("Banner Base Color").query(of("BannerBaseColor")).build());

        this.register("banner_patterns", Key.builder().type(TypeTokens.PATTERN_LIST_VALUE_TOKEN).id("banner_patterns").name("Banner Patterns").query(of("BannerPatterns")).build());

        this.register("respawn_locations", Key.builder().type(TypeTokens.MAP_UUID_VECTOR3D_VALUE_TOKEN).id("respawn_locations").name("Respawn Locations").query(of("RespawnLocations")).build());

        this.register("expiration_ticks", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("expiration_ticks").name("Expiration Ticks").query(of("ExpirationTicks")).build());

        this.register("skin_unique_id", Key.builder().type(TypeTokens.UUID_VALUE_TOKEN).id("skin_uuid").name("Skin UUID").query(of("SkinUUID")).build());

        this.register("moisture", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("moisture").name("Moisture").query(of("Moisture")).build());

        this.register("angry", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("angry").name("Angry").query(of("Angry")).build());

        this.register("anger", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("anger").name("Anger").query(of("Anger")).build());

        this.register("rotation", Key.builder().type(TypeTokens.ROTATION_VALUE_TOKEN).id("rotation").name("Rotation").query(of("Rotation")).build());

        this.register("is_splash_potion", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_splash_potion").name("Is Splash Potion").query(of("IsSplashPotion")).build());

        this.register("affects_spawning", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("affects_spawning").name("Affects Spawning").query(of("AffectsSpawning")).build());

        this.register("critical_hit", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("critical_hit").name("Critical Hit").query(of("CriticalHit")).build());

        this.register("generation", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("generation").name("Generation").query(of("Generation")).build());

        this.register("passengers", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("passenger_snapshot").name("Passenger Snapshot").query(of("PassengerSnapshot")).build());

        this.register("knockback_strength", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("knockback_strength").name("Knockback Strength").query(of("KnockbackStrength")).build());

        this.register("persists", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("persists").name("Persists").query(of("Persists")).build());

        this.register("stored_enchantments", Key.builder().type(TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN).id("stored_enchantments").name("Stored Enchantments").query(of("StoredEnchantments")).build());

        this.register("is_sprinting", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("sprinting").name("Sprinting").query(of("Sprinting")).build());

        this.register("stuck_arrows", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("stuck_arrows").name("Stuck Arrows").query(of("StuckArrows")).build());

        this.register("vanish_ignores_collision", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("vanish_ignores_collision").name("Vanish Ignores Collision").query(of("VanishIgnoresCollision")).build());

        this.register("vanish_prevents_targeting", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("vanish_prevents_targeting").name("Vanish Prevents Targeting").query(of("VanishPreventsTargeting")).build());

        this.register("is_aflame", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_aflame").name("Is Aflame").query(of("IsAflame")).build());

        this.register("can_breed", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("can_breed").name("Can Breed").query(of("CanBreed")).build());

        this.register("fluid_item_stack", Key.builder().type(TypeTokens.FLUID_VALUE_TOKEN).id("fluid_item_container_snapshot").name("Fluid Item Container Snapshot").query(of("FluidItemContainerSnapshot")).build());

        this.register("fluid_tank_contents", Key.builder().type(TypeTokens.MAP_DIRECTION_FLUID_VALUE_TOKEN).id("fluid_tank_contents").name("Fluid Tank Contents").query(of("FluidTankContents")).build());

        this.register("custom_name_visible", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("custom_name_visible").name("Custom Name Visible").query(of("CustomNameVisible")).build());

        this.register("first_date_played", Key.builder().type(TypeTokens.INSTANT_VALUE_TOKEN).id("first_time_joined").name("First Time Joined").query(of("FirstTimeJoined")).build());

        this.register("last_date_played", Key.builder().type(TypeTokens.INSTANT_VALUE_TOKEN).id("last_time_played").name("Last Time Played").query(of("LastTimePlayed")).build());

        this.register("hide_enchantments", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("hide_enchantments").name("Hide Enchantments").query(of("HideEnchantments")).build());

        this.register("hide_attributes", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("hide_attributes").name("Hide Attributes").query(of("HideAttributes")).build());

        this.register("hide_unbreakable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("hide_unbreakable").name("Hide Unbreakable").query(of("HideUnbreakable")).build());

        this.register("hide_can_destroy", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("hide_can_destroy").name("Hide Can Destroy").query(of("HideCanDestroy")).build());

        this.register("hide_can_place", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("hide_can_place").name("Hide Can Place").query(of("HideCanPlace")).build());

        this.register("hide_miscellaneous", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("hide_miscellaneous").name("Hide Miscellaneous").query(of("HideMiscellaneous")).build());

        this.register("potion_color", Key.builder().type(TypeTokens.COLOR_VALUE_TOKEN).id("potion_color").name("Potion Color").query(of("PotionColor")).build());

        this.register("potion_effects", Key.builder().type(TypeTokens.LIST_POTION_VALUE_TOKEN).id("potion_effects").name("Potion Effects").query(of("PotionEffects")).build());

        this.register("potion_type", Key.builder().type(TypeTokens.POTION_VALUE_TOKEN).id("potion_type").name("Potion Type").query(of("PotionType")).build());

        this.register("body_rotations", Key.builder().type(TypeTokens.MAP_BODY_VECTOR3D_VALUE_TOKEN).id("body_rotations").name("Body Rotations").query(of("BodyRotations")).build());

        this.register("head_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("head_rotation").name("Head Rotation").query(of("HeadRotation")).build());

        this.register("chest_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("chest_rotation").name("Chest Rotation").query(of("ChestRotation")).build());

        this.register("left_arm_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("left_arm_rotation").name("Left Arm Rotation").query(of("LeftArmRotation")).build());

        this.register("right_arm_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("right_arm_rotation").name("Right Arm Rotation").query(of("RightArmRotation")).build());

        this.register("left_leg_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("left_leg_rotation").name("Left Leg Rotation").query(of("LeftLegRotation")).build());

        this.register("right_leg_rotation", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("right_leg_rotation").name("Right Leg Rotation").query(of("RightLegRotation")).build());

        this.register("beacon_primary_effect", Key.builder().type(TypeTokens.OPTIONAL_POTION_VALUE_TOKEN).id("beacon_primary_effect").name("Beacon Primary Effect").query(of("BeaconPrimaryEffect")).build());

        this.register("beacon_secondary_effect", Key.builder().type(TypeTokens.OPTIONAL_POTION_VALUE_TOKEN).id("beacon_secondary_effect").name("Beacon Secondary Effect").query(of("BeaconSecondaryEffect")).build());

        this.register("targeted_entity", Key.builder().type(TypeTokens.ENTITY_VALUE_TOKEN).id("targeted_entity").name("Targeted Entity").query(of("TargetedEntity")).build());

        this.register("targeted_location", Key.builder().type(TypeTokens.VECTOR_3D_VALUE_TOKEN).id("targeted_vector_3d").name("Targeted Vector3d").query(of("TargetedVector3d")).build());

        this.register("fuse_duration", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("fuse_duration").name("Fuse Duration").query(of("FuseDuration")).build());

        this.register("ticks_remaining", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("ticks_remaining").name("Ticks Remaining").query(of("TicksRemaining")).build());

        this.register("explosion_radius", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("explosion_radius").name("Explosion Radius").query(of("ExplosionRadius")).build());

        this.register("armor_stand_has_arms", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("has_arms").name("Has Arms").query(of("HasArms")).build());

        this.register("armor_stand_has_base_plate", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("has_base_plate").name("Has Base Plate").query(of("HasBasePlate")).build());

        this.register("armor_stand_marker", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_marker").name("Is Marker").query(of("IsMarker")).build());

        this.register("armor_stand_is_small", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_small").name("Is Small").query(of("IsSmall")).build());

        this.register("armor_stand_taking_disabled", Key.builder().type(TypeTokens.SET_EQUIPMENT_TYPE_TOKEN).id("taking_disabled").name("Taking Disabled").query(of("TakingDisabled")).build());

        this.register("armor_stand_placing_disabled", Key.builder().type(TypeTokens.SET_EQUIPMENT_TYPE_TOKEN).id("placing_disabled").name("Placing Disabled").query(of("PlacingDisabled")).build());

        this.register("is_silent", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_silent").name("Is Silent").query(of("IsSilent")).build());

        this.register("glowing", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("glowing").name("Glowing").query(of("Glowing")).build());

        this.register("pickup_rule", Key.builder().type(TypeTokens.PICKUP_VALUE_TOKEN).id("pickup_rule").name("Pickup Rule").query(of("PickupRule")).build());

        this.register("invulnerability_ticks", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("invulnerability_ticks").name("Invulnerability Ticks").query(of("HurtTime")).build());

        this.register("invulnerable", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("invulnerable").name("Invulnerable").query(of("Invulnerable")).build());

        this.register("has_gravity", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("has_gravity").name("Has Gravity").query(of("HasGravity")).build());

        this.register("statistics", Key.builder().type(TypeTokens.STATISTIC_MAP_VALUE_TOKEN).id("statistics").name("Statistics").query(of("Statistics")).build());

        this.register("infinite_despawn_delay", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("infinite_despawn_delay").name("Infinite Despawn Delay").query(of("InfiniteDespawnDelay")).build());

        this.register("infinite_pickup_delay", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("infinite_pickup_delay").name("Infinite Pickup Delay").query(of("InfinitePickupDelay")).build());

        this.register("despawn_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("despawn_delay").name("Despawn Delay").query(of("DespawnDelay")).build());

        this.register("pickup_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("pickup_delay").name("Pickup Delay").query(of("PickupDelay")).build());

        this.register("end_gateway_age", Key.builder().type(TypeTokens.LONG_VALUE_TOKEN).id("end_gateway_age").name("End Gateway Age").query(of("EndGatewayAge")).build());

        this.register("end_gateway_teleport_cooldown", Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN).id("end_gateway_teleport_cooldown").name("End Gateway Teleport Cooldown").query(of("EndGatewayTeleportCooldown")).build());

        this.register("exit_position", Key.builder().type(TypeTokens.VECTOR_3I_VALUE_TOKEN).id("exit_position").name("Exit Position").query(of("ExitPosition")).build());

        this.register("exact_teleport", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("exact_teleport").name("Exact Teleport").query(of("ExactTeleport")).build());

        this.register("structure_author", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("structure_author").name("Structure Author").query(of("StructureAuthor")).build());

        this.register("structure_ignore_entities", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("structure_ignore_entities").name("Structure Ignore Entities").query(of("StructureIgnoreEntities")).build());

        this.register("structure_integrity", Key.builder().type(TypeTokens.FLOAT_VALUE_TOKEN).id("structure_integrity").name("Structure Integrity").query(of("StructureIntegrity")).build());

        this.register("structure_mode", Key.builder().type(TypeTokens.STRUCTURE_MODE_VALUE_TOKEN).id("structure_mode").name("Structure Mode").query(of("StructureMode")).build());

        this.register("structure_position", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("structure_position").name("Structure Position").query(of("StructurePosition")).build());

        this.register("structure_powered", Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("structure_powered").name("Structure Powered").query(of("StructurePowered")).build());

        this.register("structure_seed", Key.builder().type(TypeTokens.LONG_VALUE_TOKEN).id("structure_seed").name("Structure Seed").query(of("StructureSeed")).build());

        this.register("structure_show_air", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("structure_show_air").name("Structure Show Air").query(of("StructureShowAir")).build());

        this.register("structure_show_bounding_box", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("structure_show_bounding_box").name("Structure Show Bounding Box").query(of("StructureShowBoundingBox")).build());

        this.register("structure_size", Key.builder().type(TypeTokens.VECTOR_3I_VALUE_TOKEN).id("structure_size").name("Structure Size").query(of("StructureSize")).build());

        this.register("absorption", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("absorption").name("Absorption").query(of("Absorption")).build());

        this.register("area_effect_cloud_radius", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("area_effect_cloud_radius").name("AreaEffectCloud Radius").query(of("CloudRadius")).build());

        this.register("area_effect_cloud_radius_on_use", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("area_effect_cloud_radius_on_use").name("AreaEffectCloud Radius On Use").query(of("CloudRadiusOnUse")).build());

        this.register("area_effect_cloud_radius_per_tick", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("area_effect_cloud_radius_per_tick").name("AreaEffectCloud Radius Per Tick").query(of("CloudRadiusPerTick")).build());

        this.register("area_effect_cloud_color", Key.builder().type(TypeTokens.COLOR_VALUE_TOKEN).id("area_effect_cloud_color").name("AreaEffectCloud Color").query(of("CloudColor")).build());

        this.register("area_effect_cloud_duration", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("area_effect_cloud_duration").name("AreaEffectCloud Duration").query(of("CloudDuration")).build());

        this.register("area_effect_cloud_duration_on_use", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("area_effect_cloud_duration_on_use").name("AreaEffectCloud Duration On Use").query(of("CloudDurationOnUse")).build());

        this.register("area_effect_cloud_wait_time", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("area_effect_cloud_wait_time").name("AreaEffectCloud Wait Time").query(of("CloudWaitTime")).build());

        this.register("area_effect_cloud_reapplication_delay", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("area_effect_cloud_wait_time").name("AreaEffectCloud Wait Time").query(of("CloudReapplicationDelay")).build());

        this.register("area_effect_cloud_age", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("area_effect_cloud_age").name("AreaEffectCloud Age").query(of("CloudAge")).build());

        this.register("area_effect_cloud_particle_type", Key.builder().type(TypeTokens.PARTICLE_TYPE_VALUE_TOKEN).id("area_effect_cloud_particle_type").name("AreaEffectCloud ParticleType").query(of("CloudParticleType")).build());

        this.register("age", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("entity_age").name("Entity Age").query(of("EntityAge")).build());

        this.register("attack_damage", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("entity_attack_damage").name("Entity Attack Damage").query(of("EntityAttackDamage")).build());

        this.register("base_size", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("base_size").name("Entity Base Size").query(of("EntityBaseSize")).build());

        this.register("damage_entity_map", Key.builder().type(TypeTokens.ENTITY_TYPE_DOUBLE_MAP_VALUE_TOKEN).id("entity_type_damage_map").name("Entity Type Damage Map").query(of("DamageEntityTypeMap")).build());

        this.register("dominant_hand", Key.builder().type(TypeTokens.HAND_PREFERENCE_VALUE_TOKEN).id("hand_preference").name("Hand Preference").query(of("HandPreference")).build());

        this.register("filled", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("filled").name("Filled").query(of("Filled")).build());

        this.register("fluid_level", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("fluid_level").name("Fluid Level").query(of("LiquidLevel")).build());

        this.register("health_scale", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("health_scale").name("Health Scale").query(of("HealthScale")).build());

        this.register("height", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("entity_height").name("Entity Height").query(of("EntityHeight")).build());

        this.register("held_experience", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("held_experience").name("Held Experience").query(of("HeldExperience")).build());

        this.register("is_sleeping", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_sleeping").name("Is Sleeping").query(of("IsSleeping")).build());

        this.register("is_johnny", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_johnny").name("Is Johnny").query(of("IsJohnny")).build());

        // Deprecated
        this.register("johnny_vindicator", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("johnny_vindicator").name("Johnny Vindicator").query(of("JohnnyVindicator")).build());

        this.register("last_attacker", Key.builder().type(TypeTokens.OPTIONAL_ENTITY_SNAPSHOT_VALUE_TOKEN).id("last_attacker").name("Last Attacker").query(of("LastAttacker")).build());

        this.register("last_damage", Key.builder().type(TypeTokens.OPTIONAL_DOUBLE_VALUE_TOKEN).id("last_damage").name("Last Damage Taken").query(of("LastDamage")).build());

        this.register("llama_strength", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("llama_strength").name("Llama Strength").query(of("LlamaStrength")).build());

        this.register("llama_variant", Key.builder().type(TypeTokens.LLAMA_VARIANT_VALUE_TOKEN).id("llama_variant").name("Llama Variant").query(of("LlamaVariant")).build());

        this.register("scale", Key.builder().type(TypeTokens.DOUBLE_VALUE_TOKEN).id("entity_scale").name("Entity Scale").query(of("EntityScale")).build());

        this.register("will_shatter", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("will_shatter").name("Will Shatter").query(of("WillShatter")).build());

        this.register("wire_attachments", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_MAP_VALUE_TOKEN).id("wire_attachment_map").name("Wire Attachment Map").query(of("WireAttachmentMap")).build());

        this.register("wire_attachment_east", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("wire_attachment_east").name("Wire Attachment East").query(of("WireAttachmentEast")).build());

        this.register("wire_attachment_south", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("wire_attachment_south").name("Wire Attachment South").query(of("WireAttachmentSouth")).build());

        this.register("wire_attachment_north", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("wire_attachment_north").name("Wire Attachment North").query(of("WireAttachmentNorth")).build());

        this.register("wire_attachment_west", Key.builder().type(TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN).id("wire_attachment_west").name("Wire Attachment West").query(of("WireAttachmentWest")).build());

        this.register("age", Key.builder().type(TypeTokens.BOUNDED_INTEGER_VALUE_TOKEN).id("age").name("Age").query(of("Age")).build());

        this.register("is_adult", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_adult").name("Is Adult").query(of("IsAdult")).build());

        this.register("is_baby", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_baby").name("Is Baby").query(of("IsBaby")).build());

        this.register("health_scale", Key.builder().type(TypeTokens.BOUNDED_DOUBLE_VALUE_TOKEN).id("health_scale").name("Health Scale").query(of("HealthScale")).build());

        this.register("is_elytra_flying", Key.builder().type(TypeTokens.BOOLEAN_VALUE_TOKEN).id("is_elytra_flying").name("Is Elytra Flying").query(of("ElytraFlying")).build());

        this.register("active_item", Key.builder().type(TypeTokens.ITEM_SNAPSHOT_VALUE_TOKEN).id("active_item").name("Active Item").query(of("ActiveItem")).build());

        // All sponge provided keys are belong to sponge. Other plugins are going to have their own keys with their own plugin containers
        Sponge.getCauseStackManager().popCause();
    }

    private void register(final String fieldName, final Key<?> key) {
        this.fieldMap.put(fieldName, key);
        this.keyMap.put(key.getId().toLowerCase(Locale.ENGLISH), key);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void registerAdditionalCatalog(final Key<?> extraCatalog) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Cannot this.register new Keys after Data Registration has completed!");
        checkNotNull(extraCatalog, "Key cannot be null!");
        final PluginContainer parent = ((SpongeKey) extraCatalog).getParent();
        final String pluginId = parent.getId().toLowerCase(Locale.ENGLISH);
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        final String[] split = id.split(":");
        checkArgument(split.length == 2, "Key id's have to be in two parts! The first part being the plugin id, the second part being the key's individual id. Currently you have: " + Arrays.toString(split));
        checkArgument(split[0].equals(pluginId),  "A plugin is trying to this.register custom keys under a different plugin id namespace! This is unsupported! The provided key: " + id);
        this.keyMap.put(id, extraCatalog);
    }

    @Override
    public Optional<Key<?>> getById(final String id) {
        return Optional.ofNullable(this.keyMap.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Key<?>> getAll() {
        return Collections.unmodifiableCollection(this.keyMap.values());
    }

    private KeyRegistryModule() {
    }

    @SuppressWarnings("rawtypes")
    public void registerKeyListeners() {
        for (final Key<?> key : this.keyMap.values()) {
            ((SpongeKey) key).registerListeners();
        }
    }

    public void registerForEntityClass(final Class<? extends Entity> cls) {
        try {
            final List<DataParameterConverter<?>> converters = LOADED_CLASSES.computeIfAbsent(cls, k -> new ArrayList<>());
            final Callable<List<DataParameterConverter<?>>> callable = DATA_PARAMETER_FUNCTION_GETTERS.get(cls);
            if (callable != null) {
                final List<DataParameterConverter<?>> call = callable.call();
                converters.addAll(call);
                // just need to call, the constructor should perform the actual registration to the parameter.
            }
            // Then start climbing the hierarchy
            Class<?> clazz = cls.getSuperclass();
            do {
                final List<DataParameterConverter<?>> superConverters = LOADED_CLASSES.computeIfAbsent(clazz, k -> new ArrayList<>());

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
    private static final ConcurrentHashMap<Class<?>, List<DataParameterConverter<?>>> LOADED_CLASSES = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Entity>, Callable<List<DataParameterConverter<?>>>> DATA_PARAMETER_FUNCTION_GETTERS = ImmutableMap.<Class<? extends Entity>, Callable<List<DataParameterConverter<?>>>>builder()
        .put(Entity.class, () -> {
            final ImmutableList.Builder<DataParameterConverter<?>> objects = ImmutableList.builder();
            objects.add(new EntityFlagsConverter());
            objects.add(new EntityCustomNameVisibleConverter());
            objects.add(new EntitySilentConverter());
            objects.add(new EntityAirConverter());
            objects.add(new EntityCustomNameConverter());
            objects.add(new EntityNoGravityConverter());
            objects.add(new EntityBabyConverter());
            return objects.build();
        })
        .put(EntityLivingBase.class, () -> {
            final ImmutableList.Builder<DataParameterConverter<?>> list = ImmutableList.builder();
            list.add(new EntityLivingBaseHealthConverter());
            list.add(new EntityLivingBaseArrowCountConverter());
            return list.build();
        })
        .put(EntityLiving.class, () -> {
            final ImmutableList.Builder<DataParameterConverter<?>> list = ImmutableList.builder();
            list.add(new EntityLivingAIFlagsConverter());
            return list.build();
        })
        .build();

    static final class Holder {
        static final KeyRegistryModule INSTANCE = new KeyRegistryModule();
    }
}
