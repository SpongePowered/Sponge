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
import static org.spongepowered.api.data.key.KeyFactory.makeSetKey;
import static org.spongepowered.api.data.key.KeyFactory.makeSingleKey;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.DirtType;
import org.spongepowered.api.data.type.DisguisedBlockType;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.LogAxis;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.registry.RegistryHelper;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"unchecked"})
public class KeyRegistry {

    private static final Map<String, Key<?>> keyMap = new MapMaker().concurrencyLevel(4).makeMap();

    public static void registerKeys() {
        generateKeyMap();
        RegistryHelper.mapFields(Keys.class, keyMap);
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
        keyMap.put("shrub_type", makeSingleKey(ShrubType.class, Value.class, of("ShrubType")));
        keyMap.put("plant_type", makeSingleKey(PlantType.class, Value.class, of("PlantType")));
        keyMap.put("tree_type", makeSingleKey(TreeType.class, Value.class, of("TreeType")));
        keyMap.put("log_axis", makeSingleKey(LogAxis.class, Value.class, of("LogAxis")));
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

    }

    private static Map<String, Key<?>> getKeyMap() {
        generateKeyMap();
        return keyMap;
    }

}
