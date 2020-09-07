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
package org.spongepowered.common.registry.builtin.sponge;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.data.key.SpongeKey;
import org.spongepowered.common.data.key.SpongeKeyBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class KeyStreamGenerator {

    private KeyStreamGenerator() {
    }

    @SuppressWarnings("rawtypes")
    public static Stream<Key> stream() {
        // Do not remove the explicit generic unless you like 40+ min compile times
        final List<Key> keys = new ArrayList<>();
        // @formatter:off
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("absorption"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("acceleration"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("active_item"), TypeTokens.ITEM_STACK_SNAPSHOT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("affects_spawning"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("age"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("airborne_velocity_modifier"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("anger_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("applicable_potion_effects"), TypeTokens.LIST_POTION_EFFECT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("applied_enchantments"), TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("armor_material"), TypeTokens.ARMOR_MATERIAL_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("art_type"), TypeTokens.ART_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("attachment_surface"), TypeTokens.ATTACHMENT_SURFACE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("attack_damage"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("attack_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("author"), TypeTokens.COMPONENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("axis"), TypeTokens.AXIS_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("baby_ticks"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("banner_pattern_layers"), TypeTokens.LIST_BANNER_PATTERN_LAYER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("base_size"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("base_vehicle"), TypeTokens.ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("beam_target_entity"), TypeTokens.LIVING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("biome_temperature"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("blast_resistance"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("block_light"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("block_state"), TypeTokens.BLOCK_STATE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("block_temperature"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("boat_type"), TypeTokens.BOAT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("body_rotations"), TypeTokens.MAP_BODY_VECTOR3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("boss_bar"), TypeTokens.BOSS_BAR_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("breakable_block_types"), TypeTokens.SET_BLOCK_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("breeder"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("breeding_cooldown"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("burn_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_breed"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_drop_as_item"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_fly"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_grief"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_harvest"), TypeTokens.SET_BLOCK_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_hurt_entities"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_join_raid"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_move_on_land"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("can_place_as_block"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("casting_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("cat_type"), TypeTokens.CAT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("chest_attachment_type"), TypeTokens.CHEST_ATTACHMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("chest_rotation"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("color"), TypeTokens.COLOR_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("command"), TypeTokens.STRING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("comparator_mode"), TypeTokens.COMPARATOR_MODE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("connected_directions"), TypeTokens.SET_DIRECTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("container_item"), TypeTokens.ITEM_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("cooldown"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("creator"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("current_spell"), TypeTokens.SPELL_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("custom_attack_damage"), TypeTokens.MAP_ENTITY_TYPE_DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("damage_absorption"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("damage_per_block"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("decay_distance"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("derailed_velocity_modifier"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("despawn_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("detonator"), TypeTokens.LIVING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("direction"), TypeTokens.DIRECTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("display_name"), TypeTokens.COMPONENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("dominant_hand"), TypeTokens.HAND_PREFERENCE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("door_hinge"), TypeTokens.DOOR_HINGE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("do_exact_teleport"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("duration"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("duration_on_use"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("dye_color"), TypeTokens.DYE_COLOR_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("eating_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("efficiency"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("egg_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("end_gateway_age"), TypeTokens.LONG_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("equipment_type"), TypeTokens.EQUIPMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("exhaustion"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("experience"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("experience_from_start_of_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("experience_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("experience_since_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("explosion_radius"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("eye_height"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("eye_position"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fall_distance"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fall_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("firework_effects"), TypeTokens.LIST_FIREWORK_EFFECT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("firework_flight_modifier"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fire_damage_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fire_ticks"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("first_date_joined"), TypeTokens.INSTANT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("first_trusted"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fluid_item_stack"), TypeTokens.FLUID_STACK_SNAPSHOT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fluid_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fluid_tank_contents"), TypeTokens.MAP_DIRECTION_FLUID_STACK_SNAPSHOT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("flying_speed"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("food_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fox_type"), TypeTokens.FOX_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fuel"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("fuse_duration"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("game_mode"), TypeTokens.GAME_MODE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("game_profile"), TypeTokens.GAME_PROFILE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("generation"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("growth_stage"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hardness"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_arms"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_base_plate"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_chest"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_egg"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_fish"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_marker"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_pores_down"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_pores_east"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_pores_north"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_pores_south"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_pores_up"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_pores_west"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("has_viewed_credits"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("head_rotation"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("healing_crystal"), TypeTokens.ENDER_CRYSTAL_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("health"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("health_scale"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("height"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("held_item"), TypeTokens.ITEM_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hidden_gene"), TypeTokens.PANDA_GENE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hide_attributes"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hide_can_destroy"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hide_can_place"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hide_enchantments"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hide_miscellaneous"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("hide_unbreakable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("home_position"), TypeTokens.VECTOR_3I_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("horse_color"), TypeTokens.HORSE_COLOR_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("horse_style"), TypeTokens.HORSE_STYLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("infinite_despawn_delay"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("infinite_pickup_delay"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("instrument_type"), TypeTokens.INSTRUMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("inverted"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("invulnerability_ticks"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("invulnerable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("in_wall"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_adult"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_aflame"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_ai_enabled"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_angry"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_attached"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_begging_for_food"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_celebrating"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_charged"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_charging_crossbow"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_climbing"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_connected_east"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_connected_north"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_connected_south"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_connected_up"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_connected_west"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_critical_hit"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_crouching"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_custom_name_visible"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_defending"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_disarmed"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_eating"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_effect_only"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_elytra_flying"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_extended"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_faceplanted"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_filled"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_flammable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_flying"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_frightened"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_full_block"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_glowing"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_going_home"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_gravity_affected"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_hissing"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_immobilized"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_indirectly_powered"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_interested"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_invisible"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_in_water"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_johnny"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_laying_egg"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_leader"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_lit"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_lying_down"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_lying_on_back"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_occupied"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_on_rail"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_open"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_passable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_patrolling"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_persistent"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_placing_disabled"), TypeTokens.MAP_EQUIPMENT_TYPE_BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_player_created"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_pouncing"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_powered"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_primed"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_purring"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_relaxed"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_replaceable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_roaring"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_rolling_around"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_saddled"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_screaming"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sheared"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_silent"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sitting"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sleeping"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sleeping_ignored"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_small"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sneaking"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sneezing"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_snowy"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_solid"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_sprinting"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_standing"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_stunned"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_surrogate_block"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_taking_disabled"), TypeTokens.MAP_EQUIPMENT_TYPE_BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_tamed"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_trading"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_traveling"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_trusting"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_unbreakable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_unhappy"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_waterlogged"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("is_wet"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("item_durability"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("item_stack_snapshot"), TypeTokens.ITEM_STACK_SNAPSHOT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("knockback_strength"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("known_gene"), TypeTokens.PANDA_GENE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("last_attacker"), TypeTokens.ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("last_command_output"), TypeTokens.COMPONENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("last_damage_received"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("last_date_joined"), TypeTokens.INSTANT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("last_date_played"), TypeTokens.INSTANT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("layer"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("leash_holder"), TypeTokens.ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("left_arm_rotation"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("left_leg_rotation"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("life_ticks"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("light_emission"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("llama_type"), TypeTokens.LLAMA_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("lock_token"), TypeTokens.STRING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("lore"), TypeTokens.LIST_COMPONENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("matter_state"), TypeTokens.MATTER_STATE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_air"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_burn_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_cook_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_durability"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_fall_damage"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_health"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_nearby_entities"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_spawn_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_speed"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("max_stack_size"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("minecart_block_offset"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("min_spawn_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("moisture"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("mooshroom_type"), TypeTokens.MOOSHROOM_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("music_disc"), TypeTokens.MUSIC_DISC_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("next_entity_to_spawn"), TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("note_pitch"), TypeTokens.NOTE_PITCH_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("notifier"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("occupied_deceleration"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("on_ground"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("pages"), TypeTokens.LIST_COMPONENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("parrot_type"), TypeTokens.PARROT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("particle_effect"), TypeTokens.PARTICLE_EFFECT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("passed_cook_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("passengers"), TypeTokens.LIST_ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("pattern_color"), TypeTokens.DYE_COLOR_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("phantom_phase"), TypeTokens.PHANTOM_PHASE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("pickup_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("pickup_rule"), TypeTokens.PICKUP_RULE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("piston_type"), TypeTokens.PISTON_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("placeable_block_types"), TypeTokens.SET_BLOCK_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("plain_pages"), TypeTokens.LIST_STRING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("plugin_container"), TypeTokens.PLUGIN_CONTAINER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("pores"), TypeTokens.SET_DIRECTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("portion_type"), TypeTokens.PORTION_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("potential_max_speed"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("potion_effects"), TypeTokens.LIST_POTION_EFFECT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("potion_type"), TypeTokens.POTION_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("power"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("primary_potion_effect_type"), TypeTokens.POTION_EFFECT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("profession_type"), TypeTokens.PROFESSION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("profession_level"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("rabbit_type"), TypeTokens.RABBIT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("radius"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("radius_on_use"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("radius_per_tick"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("raid_wave"), TypeTokens.RAID_WAVE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("rail_direction"), TypeTokens.RAIL_DIRECTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("reapplication_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("redstone_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("remaining_air"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("remaining_brew_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("remaining_spawn_delay"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("replenished_food"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("replenished_saturation"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("represented_instrument"), TypeTokens.INSTRUMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("required_player_range"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("respawn_locations"), TypeTokens.MAP_UUID_RESPAWN_LOCATION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("right_arm_rotation"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("right_leg_rotation"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("roaring_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("rotation"), TypeTokens.ROTATION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("saturation"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("scale"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("scoreboard_tags"), TypeTokens.SET_STRING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("secondary_potion_effect_type"), TypeTokens.POTION_EFFECT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("second_trusted"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("shooter"), TypeTokens.PROJECTILE_SOURCE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("show_bottom"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("sign_lines"), TypeTokens.LIST_COMPONENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("size"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("skin_profile_property"), TypeTokens.PROFILE_PROPERTY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("skin_moisture"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("sky_light"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("slab_portion"), TypeTokens.SLAB_PORTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("sleep_timer"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("slot_index"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("slot_position"), TypeTokens.VECTOR_2I_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("slot_side"), TypeTokens.DIRECTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("slows_unoccupied"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("sneezing_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("spawnable_entities"), TypeTokens.WEIGHTED_ENTITY_ARCHETYPE_COLLECTION_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("spawn_count"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("spawn_range"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("spectator_target"), TypeTokens.ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("stair_shape"), TypeTokens.STAIR_SHAPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("statistics"), TypeTokens.MAP_STATISTIC_LONG_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("stored_enchantments"), TypeTokens.LIST_ENCHANTMENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("strength"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_author"), TypeTokens.STRING_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_ignore_entities"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_integrity"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_mode"), TypeTokens.STRUCTURE_MODE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_position"), TypeTokens.VECTOR_3I_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_powered"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_seed"), TypeTokens.LONG_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_show_air"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_show_bounding_box"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("structure_size"), TypeTokens.VECTOR_3I_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("stuck_arrows"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("stunned_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("success_count"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("suspended"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("swiftness"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("tamer"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("target_entity"), TypeTokens.ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("target_location"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("target_position"), TypeTokens.VECTOR_3I_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("ticks_remaining"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("tool_type"), TypeTokens.TOOL_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("tracks_output"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("trade_offers"), TypeTokens.LIST_TRADE_OFFER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("transient"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("tropical_fish_shape"), TypeTokens.TROPICAL_FISH_SHAPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("unhappy_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("unique_id"), TypeTokens.UUID_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("unoccupied_deceleration"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("unstable"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("update_game_profile"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("vanish"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("vanish_ignores_collision"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("vanish_prevents_targeting"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("vehicle"), TypeTokens.ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("velocity"), TypeTokens.VECTOR_3D_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("villager_type"), TypeTokens.VILLAGER_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wait_time"), TypeTokens.INTEGER_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("walking_speed"), TypeTokens.DOUBLE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("will_shatter"), TypeTokens.BOOLEAN_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wire_attachments"), TypeTokens.MAP_DIRECTION_WIRE_ATTACHMENT_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wire_attachment_east"), TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wire_attachment_north"), TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wire_attachment_south"), TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wire_attachment_west"), TypeTokens.WIRE_ATTACHMENT_TYPE_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wither_targets"), TypeTokens.LIST_ENTITY_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wololo_target"), TypeTokens.SHEEP_VALUE_TOKEN));
        keys.add(KeyStreamGenerator.key(ResourceKey.sponge("wood_type"), TypeTokens.WOOD_TYPE_VALUE_TOKEN));
        // @formatter:on
        return keys.stream();
    }

    private static <E, V extends Value<E>> SpongeKey<V, E> key(final ResourceKey key, final TypeToken<V> token) {
        final SpongeKeyBuilder<E, V> builder = new SpongeKeyBuilder<>();
        return (SpongeKey<V, E>) builder
                .key(key)
                .type(token)
                .build();
    }
}
