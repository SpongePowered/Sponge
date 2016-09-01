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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.*;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.data.SpongeDataManager;

import java.awt.Color;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("rawTypes")
public class KeyRegistryModule implements AdditionalCatalogRegistryModule<Key> {

    private static final TypeToken<Double> DOUBLE_TOKEN = new TypeToken<Double>() {};
    private static final TypeToken<Boolean> BOOLEAN_TOKEN = new TypeToken<Boolean>() {};
    private static final TypeToken<Integer> INTEGER_TOKEN = new TypeToken<Integer>() {};
    private static final TypeToken<Value<Boolean>> BOOLEAN_VALUE_TOKEN = new TypeToken<Value<Boolean>>() {};
    private static final TypeToken<Short> SHORT_TOKEN = new TypeToken<Short>() {};
    private static final TypeToken<String> STRING_TOKEN = new TypeToken<String>() {};
    private static final TypeToken<UUID> UUID_TOKEN = new TypeToken<UUID>() {};
    public static final TypeToken<Optional<UUID>> OPTIONAL_UUID_TOKEN = new TypeToken<Optional<UUID>>() {};
    private static final TypeToken<Vector3d> VECTOR_3D_TOKEN = new TypeToken<Vector3d>() {};
    private static final TypeToken<Text> TEXT_TOKEN = new TypeToken<Text>() {};
    private static final TypeToken<Value<Text>> TEXT_VALUE_TOKEN = new TypeToken<Value<Text>>() {};
    private static final TypeToken<MutableBoundedValue<Double>> BOUNDED_DOUBLE_VALUE_TOKEN = new TypeToken<MutableBoundedValue<Double>>() {};
    private static final TypeToken<Value<Double>> DOUBLE_VALUE_TOKEN = new TypeToken<Value<Double>>() {};
    private static final TypeToken<Value<Integer>> INTEGER_VALUE_TOKEN = new TypeToken<Value<Integer>>() {};
    private static final TypeToken<MutableBoundedValue<Integer>> BOUNDED_INTEGER_VALUE_TOKEN = new TypeToken<MutableBoundedValue<Integer>>() {};

    public static KeyRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(Keys.class)
    private final Map<String, Key<?>> keyMap = new MapMaker().concurrencyLevel(4).makeMap();

    @Override
    public void registerDefaults() {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Attempting to register Keys illegally!");
        this.keyMap.put("axis", makeSingleKey(new TypeToken<Axis>() {}, new TypeToken<Value<Axis>>() {}, of("Axis"), "sponge:axis", "Axis"));
        this.keyMap.put("color", makeSingleKey(new TypeToken<Color>() {}, new TypeToken<Value<Color>>() {}, of("Color"), "sponge:color", "Color"));
        this.keyMap.put("health", makeSingleKey(DOUBLE_TOKEN, BOUNDED_DOUBLE_VALUE_TOKEN, of("Health"), "sponge:health", "Health"));
        this.keyMap.put("max_health", makeSingleKey(DOUBLE_TOKEN, BOUNDED_DOUBLE_VALUE_TOKEN, of("MaxHealth"), "sponge:max_health", "Max Health"));
        this.keyMap.put("display_name", makeSingleKey(TEXT_TOKEN, TEXT_VALUE_TOKEN, of("DisplayName"), "sponge:display_name", "Display Name"));
        this.keyMap.put("career", makeSingleKey(new TypeToken<Career>() {}, new TypeToken<Value<Career>>() {}, of("Career"), "sponge:career", "Career"));
        this.keyMap.put("sign_lines", makeListKey(TEXT_TOKEN, of("SignLines"), "sponge:sign_lines", "Sign Lines"));
        this.keyMap.put("skull_type", makeSingleKey(new TypeToken<SkullType>() {}, new TypeToken<Value<SkullType>>() {}, of("SkullType"), "sponge:skull_type", "Skull Type"));
        this.keyMap.put("is_sneaking", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSneaking"), "sponge:sneaking", "Is Sneaking"));
        this.keyMap.put("velocity", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("Velocity"), "sponge:velocity", "Velocity"));
        this.keyMap.put("food_level", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("FoodLevel"), "sponge:food_level", "Food Level"));
        this.keyMap.put("saturation", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FoodSaturationLevel"), "sponge:food_saturation_level", "Food Saturation Level"));
        this.keyMap.put("exhaustion", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FoodExhaustionLevel"), "sponge:food_exhaustion_level", ""));
        this.keyMap.put("max_air", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("MaxAir"), "sponge:max_air", "Max Air"));
        this.keyMap.put("remaining_air", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("RemainingAir"), "sponge:remaining_air", "Remaining Air"));
        this.keyMap.put("fire_ticks", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("FireTicks"), "sponge:fire_ticks", "Fire Ticks"));
        this.keyMap.put("fire_damage_delay", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("FireDamageDelay"), "sponge:fire_damage_delay", "Fire Damage Delay"));
        this.keyMap.put("game_mode", makeSingleKey(new TypeToken<GameMode>() {}, new TypeToken<Value<GameMode>>() {}, of("GameMode"), "sponge:game_mode", "Game Mode"));
        this.keyMap.put("is_screaming", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsScreaming"), "sponge:screaming", "Is Screaming"));
        this.keyMap.put("can_fly", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanFly"), "sponge:can_fly", "Can Fly"));
        this.keyMap.put("can_grief", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanGrief"), "sponge:can_grief", "Can Grief"));
        this.keyMap.put("shrub_type", makeSingleKey(new TypeToken<ShrubType>() {}, new TypeToken<Value<ShrubType>>() {}, of("ShrubType"), "sponge:shrub_type", "Shrub Type"));
        this.keyMap.put("plant_type", makeSingleKey(new TypeToken<PlantType>() {}, new TypeToken<Value<PlantType>>() {}, of("PlantType"), "sponge:plant_type", "Plant Type"));
        this.keyMap.put("tree_type", makeSingleKey(new TypeToken<TreeType>() {}, new TypeToken<Value<TreeType>>() {}, of("TreeType"), "sponge:tree_type", "Tree Type"));
        this.keyMap.put("log_axis", makeSingleKey(new TypeToken<LogAxis>() {}, new TypeToken<Value<LogAxis>>() {}, of("LogAxis"), "sponge:log_axis", "Log Axis"));
        this.keyMap.put("invisible", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Invisible"), "sponge:invisible", "Invisible"));
        this.keyMap.put("powered", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Powered"), "sponge:powered", "Powered"));
        this.keyMap.put("layer", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Layer"), "sponge:layer", "Layer"));
        this.keyMap.put("represented_item", makeSingleKey(new TypeToken<ItemStackSnapshot>() {}, new TypeToken<Value<ItemStackSnapshot>>() {}, of("ItemStackSnapshot"), "sponge:item_stack_snapshot", "Item Stack Snapshot"));
        this.keyMap.put("command", makeSingleKey(STRING_TOKEN, new TypeToken<Value<String>>() {}, of("Command"), "sponge:command", "Command"));
        this.keyMap.put("success_count", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("SuccessCount"), "sponge:success_count", "SuccessCount"));
        this.keyMap.put("tracks_output", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("TracksOutput"), "sponge:tracks_output", "Tracks Output"));
        this.keyMap.put("last_command_output", makeOptionalKey(TEXT_TOKEN, , of("LastCommandOutput"), "sponge:last_command_output", "Last Command Output", ));
        this.keyMap.put("trade_offers", makeListKey(new TypeToken<TradeOffer>() {}, of("TradeOffers"), "sponge:trade_offers", "Trade Offers"));
        this.keyMap.put("dye_color", makeSingleKey(new TypeToken<DyeColor>() {}, new TypeToken<Value<DyeColor>>() {}, of("DyeColor"), "sponge:dye_color", "Dye Color"));
        this.keyMap.put("firework_flight_modifier", makeSingleKey(INTEGER_TOKEN, new TypeToken<BoundedValue<Integer>>() {}, of("FlightModifier"), "sponge:flight_modifier", "Flight Modifier"));
        this.keyMap.put("firework_effects", makeListKey(new TypeToken<FireworkEffect>() {}, of("FireworkEffects"), "sponge:firework_effects", "Firework Effects"));
        this.keyMap.put("spawner_remaining_delay", makeSingleKey(SHORT_TOKEN, new TypeToken<MutableBoundedValue<Short>>() {}, of("SpawnerRemainingDelay"), "sponge:spawner_remaining_delay", "Spawner Remaining Delay"));
        this.keyMap.put("spawner_minimum_delay", makeSingleKey(SHORT_TOKEN, new TypeToken<MutableBoundedValue<Short>>() {}, of("SpawnerMinimumDelay"), "sponge:spawner_minimum_delay", "Spawner Minimum Delay"));
        this.keyMap.put("connected_directions", makeSetKey(new TypeToken<Set<Direction>>() {}, new TypeToken<SetValue<Direction>>() {}, of("ConnectedDirections"), "sponge:connected_directions",
                "Connected Directions"));
        this.keyMap.put("connected_north", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedNorth"), "sponge:connected_north", "Connected North"));
        this.keyMap.put("connected_south", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedSouth"), "sponge:connected_south", "Connected South"));
        this.keyMap.put("connected_east", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedEast"), "sponge:connected_east", "Connected East"));
        this.keyMap.put("connected_west", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedWest"), "sponge:connected_west", "Connected West"));
        this.keyMap.put("direction", makeSingleKey(new TypeToken<Direction>() {}, new TypeToken<Value<Direction>>() {}, of("Direction"), "sponge:direction", "Direction"));
        this.keyMap.put("dirt_type", makeSingleKey(new TypeToken<DirtType>() {}, new TypeToken<Value<DirtType>>() {}, of("DirtType"), "sponge:dirt_type", "Dirt Type"));
        this.keyMap.put("disguised_block_type", makeSingleKey(new TypeToken<DisguisedBlockType>() {}, new TypeToken<Value<DisguisedBlockType>>() {}, of("DisguisedBlockType"), "sponge:disguised_block_type", "Disguised Block Type"));
        this.keyMap.put("disarmed", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Disarmed"), "sponge:disarmed", "Disarmed"));
        this.keyMap.put("item_enchantments", makeListKey(new TypeToken<ItemEnchantment>() {}, of("ItemEnchantments"), "sponge:item_enchantments", "Item Enchantments"));
        this.keyMap.put("banner_patterns", makeListKey(new TypeToken<PatternLayer>() {}, of("BannerPatterns"), "sponge:banner_patterns", "Banner Patterns"));
        this.keyMap.put("banner_base_color", makeListKey(new TypeToken<DyeColor>() {}, of("BannerBaseColor"), "sponge:banner_base_color", "Banner Base Color"));
        this.keyMap.put("horse_color", makeSingleKey(new TypeToken<HorseColor>() {}, new TypeToken<Value<HorseColor>>() {}, of("HorseColor"), "sponge:horse_color", "Horse Color"));
        this.keyMap.put("horse_style", makeSingleKey(new TypeToken<HorseStyle>() {}, new TypeToken<Value<HorseStyle>>() {}, of("HorseStyle"), "sponge:horse_style", "Horse Style"));
        this.keyMap.put("horse_variant", makeSingleKey(new TypeToken<HorseVariant>() {}, new TypeToken<Value<HorseVariant>>() {}, of("HorseVariant"), "sponge:horse_variant", "Horse Variant"));
        this.keyMap.put("item_lore", makeListKey(TEXT_TOKEN, of("ItemLore"), "sponge:item_lore", "Item Lore"));
        this.keyMap.put("book_pages", makeListKey(TEXT_TOKEN, of("BookPages"), "sponge:book_pages", "Book Pages"));
        this.keyMap.put("golden_apple_type", makeSingleKey(new TypeToken<GoldenApple>() {}, new TypeToken<Value<GoldenApple>>() {}, of("GoldenAppleType"), "sponge:golden_apple_type", "Golden Apple Type"));
        this.keyMap.put("is_flying", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsFlying"), "sponge:is_flying", "Is Flying"));
        this.keyMap.put("experience_level", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceLevel"), "sponge:experience_level", "Experience Level"));
        this.keyMap.put("total_experience", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("TotalExperience"), "sponge:total_experience", "Total Experience"));
        this.keyMap.put("experience_since_level", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceSinceLevel"), "sponge:experience_since_level", "Experience Since Level"));
        this.keyMap.put("experience_from_start_of_level", makeSingleKey(INTEGER_TOKEN, new TypeToken<ImmutableBoundedValue<Integer>>() {}, of("ExperienceFromStartOfLevel"), "sponge:experience_from_start_of_level", "Experience From Start Of Level"));
        this.keyMap.put("book_author", makeSingleKey(TEXT_TOKEN, TEXT_VALUE_TOKEN, of("BookAuthor"), "sponge:book_author", "Book Author"));
        this.keyMap.put("breakable_block_types", makeSetKey(new TypeToken<Set<BlockType>>() {}, new TypeToken<SetValue<BlockType>>() {}, of("CanDestroy"), "sponge:can_destroy", "Can Destroy"));
        this.keyMap.put("placeable_blocks", makeSetKey(new TypeToken<Set<BlockType>>() {}, new TypeToken<SetValue<BlockType>>() {}, of("CanPlaceOn"), "sponge:can_place_on", "Can Place On"));
        this.keyMap.put("walking_speed", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("WalkingSpeed"), "sponge:walking_speed", "Walking Speed"));
        this.keyMap.put("flying_speed", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FlyingSpeed"), "sponge:flying_speed", "Flying Speed"));
        this.keyMap.put("slime_size", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("SlimeSize"), "sponge:slime_size", "Slime Size"));
        this.keyMap.put("villager_zombie_profession", makeSingleKey(new TypeToken<Profession>() {}, new TypeToken<Value<Profession>>() {}, of("VillagerZombieProfession"), "sponge:villager_zombie_profession", "Villager Zombie Profession"));
        this.keyMap.put("is_playing", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsPlaying"), "sponge:is_playing", "Is Playing"));
        this.keyMap.put("is_sitting", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSitting"), "sponge:is_sitting", "Is Sitting"));
        this.keyMap.put("is_sheared", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSheared"), "sponge:is_sheared", "Is Sheared"));
        this.keyMap.put("pig_saddle", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsPigSaddled"), "sponge:is_pig_saddled", "Is Pig Saddled"));
        this.keyMap.put("tamed_owner", makeOptionalKey(OPTIONAL_UUID_TOKEN, new TypeToken<OptionalValue<UUID>>() {}, of("TamerUUID"), "sponge:tamer_uuid", "Tamer UUID", ));
        this.keyMap.put("is_wet", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsWet"), "sponge:is_wet", "Is Wet"));
        this.keyMap.put("elder_guardian", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Elder"), "sponge:elder", "Elder"));
        this.keyMap.put("coal_type", makeSingleKey(new TypeToken<CoalType>() {}, new TypeToken<Value<CoalType>>() {}, of("CoalType"), "sponge:coal_type", "Coal Type"));
        this.keyMap.put("cooked_fish", makeSingleKey(new TypeToken<CookedFish>() {}, new TypeToken<Value<CookedFish>>() {}, of("CookedFishType"), "sponge:cooked_fish_type", "Cooked Fish Type"));
        this.keyMap.put("fish_type", makeSingleKey(new TypeToken<Fish>() {}, new TypeToken<Value<Fish>>() {}, of("RawFishType"), "sponge:raw_fish_type", "Raw Fish Type"));
        this.keyMap.put("represented_player", makeSingleKey(new TypeToken<GameProfile>() {}, new TypeToken<Value<GameProfile>>() {}, of("RepresentedPlayer"), "sponge:represented_player", "Represented Player"));
        this.keyMap.put("passed_burn_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("PassedBurnTime"), "sponge:passed_burn_time", "Passed Burn Time"));
        this.keyMap.put("max_burn_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("MaxBurnTime"), "sponge:max_burn_time", "Max Burn Time"));
        this.keyMap.put("passed_cook_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("PassedCookTime"), "sponge:passed_cook_time", "Passed Cook Time"));
        this.keyMap.put("max_cook_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("MaxCookTime"), "sponge:max_cook_time", "Max Cook Time"));
        this.keyMap.put("contained_experience", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("ContainedExperience"), "sponge:contained_experience", "Contained Experience"));
        this.keyMap.put("remaining_brew_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("RemainingBrewTime"), "sponge:remaining_brew_time", "Remaining Brew Time"));
        this.keyMap.put("stone_type", makeSingleKey(new TypeToken<StoneType>() {}, new TypeToken<Value<StoneType>>() {}, of("StoneType"), "sponge:stone_type", "Stone Type"));
        this.keyMap.put("prismarine_type", makeSingleKey(new TypeToken<PrismarineType>() {}, new TypeToken<Value<PrismarineType>>() {}, of("PrismarineType"), "sponge:prismarine_type", "Prismarine Type"));
        this.keyMap.put("brick_type", makeSingleKey(new TypeToken<BrickType>() {}, new TypeToken<Value<BrickType>>() {}, of("BrickType"), "sponge:brick_type", "Brick Type"));
        this.keyMap.put("quartz_type", makeSingleKey(new TypeToken<QuartzType>() {}, new TypeToken<Value<QuartzType>>() {}, of("QuartzType"), "sponge:quartz_type", "Quartz Type"));
        this.keyMap.put("sand_type", makeSingleKey(new TypeToken<SandType>() {}, new TypeToken<Value<SandType>>() {}, of("SandType"), "sponge:sand_type", "Sand Type"));
        this.keyMap.put("sandstone_type", makeSingleKey(new TypeToken<SandstoneType>() {}, new TypeToken<Value<SandstoneType>>() {}, of("SandstoneType"), "sponge:sandstone_type", "Sandstone Type"));
        this.keyMap.put("slab_type", makeSingleKey(new TypeToken<SlabType>() {}, new TypeToken<Value<SlabType>>() {}, of("SlabType"), "sponge:slab_type", "Slab Type"));
        this.keyMap.put("sandstone_type", makeSingleKey(new TypeToken<SandstoneType>() {}, new TypeToken<Value<SandstoneType>>() {}, of("SandstoneType"), "sponge:sandstone_type", "Sandstone Type"));
        this.keyMap.put("comparator_type", makeSingleKey(new TypeToken<ComparatorType>() {}, new TypeToken<Value<ComparatorType>>() {}, of("ComparatorType"), "sponge:comparator_type", "Comparator Type"));
        this.keyMap.put("hinge_position", makeSingleKey(new TypeToken<Hinge>() {}, new TypeToken<Value<Hinge>>() {}, of("HingePosition"), "sponge:hinge_position", "Hinge Position"));
        this.keyMap.put("piston_type", makeSingleKey(new TypeToken<PistonType>() {}, new TypeToken<Value<PistonType>>() {}, of("PistonType"), "sponge:piston_type", "Piston Type"));
        this.keyMap.put("portion_type", makeSingleKey(new TypeToken<PortionType>() {}, new TypeToken<Value<PortionType>>() {}, of("PortionType"), "sponge:portion_type", "Portion Type"));
        this.keyMap.put("rail_direction", makeSingleKey(new TypeToken<RailDirection>() {}, new TypeToken<Value<RailDirection>>() {}, of("RailDirection"), "sponge:rail_direction", "Rail Direction"));
        this.keyMap.put("stair_shape", makeSingleKey(new TypeToken<StairShape>() {}, new TypeToken<Value<StairShape>>() {}, of("StairShape"), "sponge:stair_shape", "Stair Shape"));
        this.keyMap.put("wall_type", makeSingleKey(new TypeToken<WallType>() {}, new TypeToken<Value<WallType>>() {}, of("WallType"), "sponge:wall_type", "Wall Type"));
        this.keyMap.put("double_plant_type", makeSingleKey(new TypeToken<DoublePlantType>() {}, new TypeToken<Value<DoublePlantType>>() {}, of("DoublePlantType"), "sponge:double_plant_type", "Double Plant Type"));
        this.keyMap.put("big_mushroom_type", makeSingleKey(new TypeToken<BigMushroomType>() {}, new TypeToken<Value<BigMushroomType>>() {}, of("BigMushroomType"), "sponge:big_mushroom_type", "Big Mushroom Type"));
        this.keyMap.put("ai_enabled", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsAiEnabled"), "sponge:is_ai_enabled", "Is Ai Enabled"));
        this.keyMap.put("creeper_charged", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsCreeperCharged"), "sponge:is_creeper_charged", "Is Creeper Charged"));
        this.keyMap.put("item_durability", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ItemDurability"), "sponge:item_durability", "Item Durability"));
        this.keyMap.put("unbreakable", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Unbreakable"), "sponge:unbreakable", "Unbreakable"));
        this.keyMap.put("spawnable_entity_type", makeSingleKey(new TypeToken<EntityType>() {}, new TypeToken<Value<EntityType>>() {}, of("SpawnableEntityType"), "sponge:spawnable_entity_type", "Spawnable Entity Type"));
        this.keyMap.put("fall_distance", makeSingleKey(new TypeToken<Float>() {}, new TypeToken<MutableBoundedValue<Float>>() {}, of("FallDistance"), "sponge:fall_distance", "Fall Distance"));
        this.keyMap.put("cooldown", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("Cooldown"), "sponge:cooldown", "Cooldown"));
        this.keyMap.put("note_pitch", makeSingleKey(new TypeToken<NotePitch>() {}, new TypeToken<Value<NotePitch>>() {}, of("Note"), "sponge:note", "Note"));
        this.keyMap.put("vehicle", makeSingleKey(new TypeToken<EntitySnapshot>() {}, new TypeToken<Value<EntitySnapshot>>() {}, of("Vehicle"), "sponge:vehicle", "Vehicle"));
        this.keyMap.put("base_vehicle", makeSingleKey(new TypeToken<EntitySnapshot>() {}, new TypeToken<Value<EntitySnapshot>>() {}, of("BaseVehicle"), "sponge:base_vehicle", "Base Vehicle"));
        this.keyMap.put("art", makeSingleKey(new TypeToken<Art>() {}, new TypeToken<Value<Art>>() {}, of("Art"), "sponge:art", "Art"));
        this.keyMap.put("fall_damage_per_block", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FallDamagePerBlock"), "sponge:fall_damage_per_block", "Fall Damage Per Block"));
        this.keyMap.put("max_fall_damage", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("MaxFallDamage"), "sponge:max_fall_damage", "Max Fall Damage"));
        this.keyMap.put("falling_block_state", makeSingleKey(new TypeToken<BlockState>() {}, new TypeToken<Value<BlockState>>() {}, of("FallingBlockState"), "sponge:falling_block_state", "Falling Block State"));
        this.keyMap.put("can_place_as_block", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanPlaceAsBlock"), "sponge:can_place_as_block", "Can Place As Block"));
        this.keyMap.put("can_drop_as_item", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanDropAsItem"), "sponge:can_drop_as_item", "Can Drop As Item"));
        this.keyMap.put("fall_time", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("FallTime"), "sponge:fall_time", "Fall Time"));
        this.keyMap.put("falling_block_can_hurt_entities", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanFallingBlockHurtEntities"), "sponge:can_falling_block_hurt_entities", "Can Falling Block Hurt Entities"));
        this.keyMap.put("represented_block", makeSingleKey(new TypeToken<BlockState>() {}, new TypeToken<Value<BlockState>>() {}, of("RepresentedBlock"), "sponge:represented_block", "Represented Block"));
        this.keyMap.put("offset", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("BlockOffset"), "sponge:block_offset", "Block Offset"));
        this.keyMap.put("attached", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Attached"), "sponge:attached", "Attached"));
        this.keyMap.put("should_drop", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ShouldDrop"), "sponge:should_drop", "Should Drop"));
        this.keyMap.put("extended", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Extended"), "sponge:extended", "Extended"));
        this.keyMap.put("growth_stage", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("GrowthStage"), "sponge:growth_stage", "Growth Stage"));
        this.keyMap.put("open", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Open"), "sponge:open", "Open"));
        this.keyMap.put("power", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Power"), "sponge:power", "Power"));
        this.keyMap.put("seamless", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Seamless"), "sponge:seamless", "Seamless"));
        this.keyMap.put("snowed", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Snowed"), "sponge:snowed", "Snowed"));
        this.keyMap.put("suspended", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Suspended"), "sponge:suspended", "Suspended"));
        this.keyMap.put("occupied", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Occupied"), "sponge:occupied", "Occupied"));
        this.keyMap.put("decayable", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Decayable"), "sponge:decayable", "Decayable"));
        this.keyMap.put("in_wall", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("InWall"), "sponge:in_wall", "In Wall"));
        this.keyMap.put("delay", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Delay"), "sponge:delay", "Delay"));
        this.keyMap.put("player_created", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("PlayerCreated"), "sponge:player_created", "Player Created"));
        this.keyMap.put("item_blockstate", makeSingleKey(new TypeToken<BlockState>() {}, new TypeToken<Value<BlockState>>() {}, of("ItemBlockState"), "sponge:item_block_state", "Item Block State"));
        this.keyMap.put("skeleton_type", makeSingleKey(new TypeToken<SkeletonType>() {}, new TypeToken<Value<SkeletonType>>() {}, of("SkeletonType"), "sponge:skeleton_type", "Skeleton Type"));
        this.keyMap.put("ocelot_type", makeSingleKey(new TypeToken<OcelotType>() {}, new TypeToken<Value<OcelotType>>() {}, of("OcelotType"), "sponge:ocelot_type", "Ocelot Type"));
        this.keyMap.put("rabbit_type", makeSingleKey(new TypeToken<RabbitType>() {}, new TypeToken<Value<RabbitType>>() {}, of("RabbitType"), "sponge:rabbit_type", "Rabbit Type"));
        this.keyMap.put("lock_token", makeSingleKey(STRING_TOKEN, new TypeToken<Value<String>>() {}, of("Lock"), "sponge:lock", "Lock"));
        this.keyMap.put("banner_base_color", makeSingleKey(new TypeToken<DyeColor>() {}, new TypeToken<Value<DyeColor>>() {}, of("BannerBaseColor"), "sponge:banner_base_color", "Banner Base Color"));
        this.keyMap.put("banner_patterns", new PatternKey());
        this.keyMap.put("respawn_locations", makeMapKey(UUID_TOKEN, VECTOR_3D_TOKEN, of("RespawnLocations"), "sponge:respawn_locations", "Respawn Locations"));
        this.keyMap.put("expiration_ticks", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExpirationTicks"), "sponge:expiration_ticks", "Expiration Ticks"));
        this.keyMap.put("skin_unique_id", makeSingleKey(UUID_TOKEN, new TypeToken<Value<UUID>>() {}, of("SkinUUID"), "sponge:skin_uuid", "Skin UUID"));
        this.keyMap.put("moisture", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Moisture"), "sponge:moisture", "Moisture"));
        this.keyMap.put("angry", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Angry"), "sponge:angry", "Angry"));
        this.keyMap.put("anger", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Anger"), "sponge:anger", "Anger"));
        this.keyMap.put("rotation", makeSingleKey(new TypeToken<Rotation>() {}, new TypeToken<Value<Rotation>>() {}, of("Rotation"), "sponge:rotation", "Rotation"));
        this.keyMap.put("is_splash_potion", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSplashPotion"), "sponge:is_splash_potion", "Is Splash Potion"));
        this.keyMap.put("affects_spawning", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("AffectsSpawning"), "sponge:affects_spawning", "Affects Spawning"));
        this.keyMap.put("critical_hit", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CriticalHit"), "sponge:critical_hit", "Critical Hit"));
        this.keyMap.put("generation", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Generation"), "sponge:generation", "Generation"));
        this.keyMap.put("passenger", makeSingleKey(new TypeToken<EntitySnapshot>() {}, new TypeToken<Value<EntitySnapshot>>() {}, of("PassengerSnapshot"), "sponge:passenger_snapshot", "Passenger Snapshot"));
        this.keyMap.put("knockback_strength", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("KnockbackStrength"), "sponge:knockback_strength", "Knockback Strength"));
        this.keyMap.put("persists", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Persists"), "sponge:persists", "Persists"));
        this.keyMap.put("stored_enchantments", makeListKey(new TypeToken<ItemEnchantment>() {}, of("StoredEnchantments"), "sponge:stored_enchantments", "Stored Enchantments"));
        this.keyMap.put("is_sprinting", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Sprinting"), "sponge:sprinting", "Sprinting"));
        this.keyMap.put("stuck_arrows", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("StuckArrows"), "sponge:stuck_arrows", "Stuck Arrows"));
        this.keyMap.put("invisibility_ignores_collision", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("InvisiblityIgnoresCollision"), "sponge:invisiblity_ignores_collision", "Invisiblity Ignores Collision"));
        this.keyMap.put("invisibility_prevents_targeting", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("InvisibilityPreventsTargeting"), "sponge:invisibility_prevents_targeting", "Invisibility Prevents Targeting"));
        this.keyMap.put("is_aflame", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsAflame"), "sponge:is_aflame", "Is Aflame"));
        this.keyMap.put("can_breed", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanBreed"), "sponge:can_breed", "Can Breed"));
        this.keyMap.put("fluid_item_stack", makeSingleKey(new TypeToken<FluidStackSnapshot>() {}, new TypeToken<Value<FluidStackSnapshot>>() {}, of("FluidItemContainerSnapshot"), "sponge:fluid_item_container_snapshot", "Fluid Item Container Snapshot"));
        this.keyMap.put("fluid_tank_contents", makeMapKey(new TypeToken<Direction>() {}, new TypeToken<List<Direction>>() {}, of("FluidTankContents"), "sponge:fluid_tank_contents", "Fluid Tank Contents"));
        this.keyMap.put("custom_name_visible", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CustomNameVisible"), "sponge:custom_name_visible", "Custom Name Visible"));
        this.keyMap.put("first_date_played", makeSingleKey(new TypeToken<Instant>() {}, new TypeToken<Value<Instant>>() {}, of("FirstTimeJoined"), "sponge:first_time_joined", "First Time Joined"));
        this.keyMap.put("last_date_played", makeSingleKey(new TypeToken<Instant>() {}, new TypeToken<Value<Instant>>() {}, of("LastTimePlayed"), "sponge:last_time_played", "Last Time Played"));
        this.keyMap.put("hide_enchantments", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideEnchantments"), "sponge:hide_enchantments", "Hide Enchantments"));
        this.keyMap.put("hide_attributes", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideAttributes"), "sponge:hide_attributes", "Hide Attributes"));
        this.keyMap.put("hide_unbreakable", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideUnbreakable"), "sponge:hide_unbreakable", "Hide Unbreakable"));
        this.keyMap.put("hide_can_destroy", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideCanDestroy"), "sponge:hide_can_destroy", "Hide Can Destroy"));
        this.keyMap.put("hide_can_place", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideCanPlace"), "sponge:hide_can_place", "Hide Can Place"));
        this.keyMap.put("hide_miscellaneous", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideMiscellaneous"), "sponge:hide_miscellaneous", "Hide Miscellaneous"));
        this.keyMap.put("potion_effects", makeListKey(new TypeToken<PotionEffect>() {}, of("PotionEffects"), "sponge:potion_effects", "Potion Effects"));
        this.keyMap.put("body_rotations", makeMapKey(new TypeToken<BodyPart>() {}, VECTOR_3D_TOKEN, of("BodyRotations"), "sponge:body_rotations", "Body Rotations"));
        this.keyMap.put("head_rotation", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("HeadRotation"), "sponge:head_rotation", "Head Rotation"));
        this.keyMap.put("chest_rotation", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("ChestRotation"), "sponge:chest_rotation", "Chest Rotation"));
        this.keyMap.put("left_arm_rotation", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("LeftArmRotation"), "sponge:left_arm_rotation", "Left Arm Rotation"));
        this.keyMap.put("right_arm_rotation", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("RightArmRotation"), "sponge:right_arm_rotation", "Right Arm Rotation"));
        this.keyMap.put("left_leg_rotation", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("LeftLegRotation"), "sponge:left_leg_rotation", "Left Leg Rotation"));
        this.keyMap.put("right_leg_rotation", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("RightLegRotation"), "sponge:right_leg_rotation", "Right Leg Rotation"));
        this.keyMap.put("beacon_primary_effect", makeOptionalKey(new TypeToken<PotionEffectType>() {}, , of("BeaconPrimaryEffect"), "sponge:beacon_primary_effect", "Beacon Primary Effect", ));
        this.keyMap.put("beacon_secondary_effect", makeOptionalKey(new TypeToken<PotionEffectType>() {}, , of("BeaconSecondaryEffect"), "sponge:beacon_secondary_effect", "Beacon Secondary Effect", ));
        this.keyMap.put("targeted_location", makeSingleKey(VECTOR_3D_TOKEN, new TypeToken<Value<Vector3d>>() {}, of("TargetedVector3d"), "sponge:targeted_vector_3d", "Targeted Vector3d"));
        this.keyMap.put("fuse_duration", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("FuseDuration"), "sponge:fuse_duration", "Fuse Duration"));
        this.keyMap.put("ticks_remaining", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("TicksRemaining"), "sponge:ticks_remaining", "Ticks Remaining"));
        this.keyMap.put("explosion_radius", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("ExplosionRadius"), "sponge:explosion_radius", "Explosion Radius"));
        this.keyMap.put("armor_stand_has_arms", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HasArms"), "sponge:has_arms", "Has Arms"));
        this.keyMap.put("armor_stand_has_base_plate", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HasBasePlate"), "sponge:has_base_plate", "Has Base Plate"));
        this.keyMap.put("armor_stand_has_gravity", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HasGravity"), "sponge:has_gravity", "Has Gravity"));
        this.keyMap.put("armor_stand_marker", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsMarker"), "sponge:is_marker", "Is Marker"));
        this.keyMap.put("armor_stand_is_small", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSmall"), "sponge:is_small", "Is Small"));

    }

    @Override
    public void registerAdditionalCatalog(Key extraCatalog) {
        checkState(!SpongeDataManager.areRegistrationsComplete(), "Cannot register new Keys after Data Registration has completed!");
        checkNotNull(extraCatalog, "Key cannot be null!");
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        checkArgument(!id.startsWith("sponge:"), "A plugin is trying to register custom keys under the sponge id namespace! This is a fake key! " + id);
        this.keyMap.put(id, extraCatalog);
    }

    @Override
    public Optional<Key> getById(String id) {
        if (checkNotNull(id, "Key id cannot be null!").contains("sponge:")) {
            id = id.replace("sponge:", "");
        }
        return Optional.ofNullable(this.keyMap.get(id));
    }

    @Override
    public Collection<Key> getAll() {
        return Collections.unmodifiableCollection(this.keyMap.values());
    }

    private static final class PatternKey implements Key<PatternListValue> {

        static final TypeToken<PatternListValue> VALUE_TOKEN = new TypeToken<PatternListValue>() {
        };
        static final TypeToken<List<PatternLayer>> ELEMENT_TOKEN = new TypeToken<List<PatternLayer>>() {
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
