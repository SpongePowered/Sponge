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
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
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
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.data.SpongeDataManager;

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
public class KeyRegistryModule implements AdditionalCatalogRegistryModule<Key<?>> {

    public static final TypeToken<Double> DOUBLE_TOKEN = new TypeToken<Double>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Boolean> BOOLEAN_TOKEN = new TypeToken<Boolean>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Integer> INTEGER_TOKEN = new TypeToken<Integer>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Boolean>> BOOLEAN_VALUE_TOKEN = new TypeToken<Value<Boolean>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Short> SHORT_TOKEN = new TypeToken<Short>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<String> STRING_TOKEN = new TypeToken<String>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<UUID> UUID_TOKEN = new TypeToken<UUID>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Optional<UUID>> OPTIONAL_UUID_TOKEN = new TypeToken<Optional<UUID>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Vector3d> VECTOR_3D_TOKEN = new TypeToken<Vector3d>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Text> TEXT_TOKEN = new TypeToken<Text>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Text>> TEXT_VALUE_TOKEN = new TypeToken<Value<Text>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MutableBoundedValue<Double>> BOUNDED_DOUBLE_VALUE_TOKEN = new TypeToken<MutableBoundedValue<Double>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Double>> DOUBLE_VALUE_TOKEN = new TypeToken<Value<Double>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Integer>> INTEGER_VALUE_TOKEN = new TypeToken<Value<Integer>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MutableBoundedValue<Integer>> BOUNDED_INTEGER_VALUE_TOKEN = new TypeToken<MutableBoundedValue<Integer>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Vector3d>> VECTOR_3D_VALUE_TOKEN = new TypeToken<Value<Vector3d>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Color> COLOR_TOKEN = new TypeToken<Color>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Color>> COLOR_VALUE_TOKEN = new TypeToken<Value<Color>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Axis> AXIS_TOKEN = new TypeToken<Axis>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Axis>> AXIS_VALUE_TOKEN = new TypeToken<Value<Axis>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Career> CAREER_TOKEN = new TypeToken<Career>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Career>> CAREER_VALUE_TOKEN = new TypeToken<Value<Career>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<Text>> LIST_TEXT_VALUE_TOKEN = new TypeToken<ListValue<Text>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SkullType> SKULL_TOKEN = new TypeToken<SkullType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<SkullType>> SKULL_VALUE_TOKEN = new TypeToken<Value<SkullType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<GameMode> GAME_MODE_TOKEN = new TypeToken<GameMode>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<GameMode>> GAME_MODE_VALUE_TOKEN = new TypeToken<Value<GameMode>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ShrubType> SHRUB_TOKEN = new TypeToken<ShrubType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<ShrubType>> SHRUB_VALUE_TOKEN = new TypeToken<Value<ShrubType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<PlantType> PLANT_TOKEN = new TypeToken<PlantType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<PlantType>> PLANT_VALUE_TOKEN = new TypeToken<Value<PlantType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<TreeType> TREE_TOKEN = new TypeToken<TreeType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<TreeType>> TREE_VALUE_TOKEN = new TypeToken<Value<TreeType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<LogAxis> LOG_AXIS_TOKEN = new TypeToken<LogAxis>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<LogAxis>> LOG_AXIS_VALUE_TOKEN = new TypeToken<Value<LogAxis>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ItemStackSnapshot> ITEM_SNAPSHOT_TOKEN = new TypeToken<ItemStackSnapshot>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<ItemStackSnapshot>> ITEM_SNAPSHOT_VALUE_TOKEN = new TypeToken<Value<ItemStackSnapshot>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<String>> STRING_VALUE_TOKEN = new TypeToken<Value<String>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Optional<Text>> OPTIONAL_TEXT_TOKEN = new TypeToken<Optional<Text>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<OptionalValue<Text>> OPTIONAL_TEXT_VALUE_TOKEN = new TypeToken<OptionalValue<Text>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<TradeOffer>> LIST_TRADE_OFFER_TOKEN = new TypeToken<List<TradeOffer>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<TradeOffer>> LIST_VALUE_TRADE_OFFER_TOKEN = new TypeToken<ListValue<TradeOffer>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<DyeColor> DYE_COLOR_TOKEN = new TypeToken<DyeColor>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<DyeColor>> DYE_COLOR_VALUE_TOKEN = new TypeToken<Value<DyeColor>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<FireworkEffect>> LIST_FIREWORK_TOKEN = new TypeToken<List<FireworkEffect>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<FireworkEffect>> LIST_VALUE_FIREWORK_TOKEN = new TypeToken<ListValue<FireworkEffect>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MutableBoundedValue<Short>> BOUNDED_SHORT_VALUE_TOKEN = new TypeToken<MutableBoundedValue<Short>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Set<Direction>> SET_DIRECTION_TOKEN = new TypeToken<Set<Direction>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SetValue<Direction>> SET_DIRECTION_VALUE_TOKEN = new TypeToken<SetValue<Direction>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Direction> DIRECTION_TOKEN = new TypeToken<Direction>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Direction>> DIRECTION_VALUE_TOKEN = new TypeToken<Value<Direction>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<DirtType> DIRT_TOKEN = new TypeToken<DirtType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<DirtType>> DIRT_VALUE_TOKEN = new TypeToken<Value<DirtType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<DisguisedBlockType> DISGUISED_BLOCK_TOKEN = new TypeToken<DisguisedBlockType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<DisguisedBlockType>> DISGUISED_BLOCK_VALUE_TOKEN = new TypeToken<Value<DisguisedBlockType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<ItemEnchantment>> LIST_ITEM_ENCHANTMENT_TOKEN = new TypeToken<List<ItemEnchantment>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<ItemEnchantment>> LIST_ITEM_ENCHANTMENT_VALUE_TOKEN = new TypeToken<ListValue<ItemEnchantment>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<PatternLayer>> LIST_PATTERN_TOKEN = new TypeToken<List<PatternLayer>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<PatternLayer>> LIST_PATTERN_VALUE_TOKEN = new TypeToken<ListValue<PatternLayer>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<HorseColor> HORSE_COLOR_TOKEN = new TypeToken<HorseColor>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<HorseColor>> HORSE_COLOR_VALUE_TOKEN = new TypeToken<Value<HorseColor>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<DyeColor>> LIST_DYE_COLOR_VALUE_TOKEN = new TypeToken<ListValue<DyeColor>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<DyeColor>> LIST_DYE_COLOR_TOKEN = new TypeToken<List<DyeColor>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<HorseStyle> HORSE_STYLE_TOKEN = new TypeToken<HorseStyle>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<OptionalValue<UUID>> OPTIONAL_UUID_VALUE_TOKEN = new TypeToken<OptionalValue<UUID>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<HorseStyle>> HORSE_STYLE_VALUE_TOKEN = new TypeToken<Value<HorseStyle>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<HorseVariant> HORSE_VARIANT_TOKEN = new TypeToken<HorseVariant>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<HorseVariant>> HORSE_VARIANT_VALUE_TOKEN = new TypeToken<Value<HorseVariant>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<Text>> LIST_TEXT_TOKEN = new TypeToken<List<Text>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<GoldenApple> GOLDEN_APPLE_TOKEN = new TypeToken<GoldenApple>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<GoldenApple>> GOLDEN_APPLE_VALUE_TOKEN = new TypeToken<Value<GoldenApple>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Set<BlockType>> SET_BLOCK_TOKEN = new TypeToken<Set<BlockType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SetValue<BlockType>> SET_BLOCK_VALUE_TOKEN = new TypeToken<SetValue<BlockType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Optional<Profession>> OPTIONAL_PROFESSION_TOKEN = new TypeToken<Optional<Profession>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<OptionalValue<Profession>> OPTIONAL_PROFESSION_VALUE_TOKEN = new TypeToken<OptionalValue<Profession>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<CoalType> COAL_TOKEN = new TypeToken<CoalType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<CoalType>> COAL_VALUE_TOKEN = new TypeToken<Value<CoalType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<CookedFish> COOKED_FISH_TOKEN = new TypeToken<CookedFish>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<CookedFish>> COOKED_FISH_VALUE_TOKEN = new TypeToken<Value<CookedFish>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Fish> FISH_TOKEN = new TypeToken<Fish>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Fish>> FISH_VALUE_TOKEN = new TypeToken<Value<Fish>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<GameProfile> GAME_PROFILE_TOKEN = new TypeToken<GameProfile>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<GameProfile>> GAME_PROFILE_VALUE_TOKEN = new TypeToken<Value<GameProfile>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<StoneType> STONE_TOKEN = new TypeToken<StoneType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<StoneType>> STONE_VALUE_TOKEN = new TypeToken<Value<StoneType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<PrismarineType> PRISMARINE_TOKEN = new TypeToken<PrismarineType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<PrismarineType>> PRISMARINE_VALUE_TOKEN = new TypeToken<Value<PrismarineType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<BrickType> BRICK_TOKEN = new TypeToken<BrickType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<BrickType>> BRICK_VALUE_TOKEN = new TypeToken<Value<BrickType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<QuartzType> QUARTZ_TOKEN = new TypeToken<QuartzType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<QuartzType>> QUARTZ_VALUE_TOKEN = new TypeToken<Value<QuartzType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SandType> SAND_TOKEN = new TypeToken<SandType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<SandType>> SAND_VALUE_TOKEN = new TypeToken<Value<SandType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SandstoneType> SAND_STONE_TOKEN = new TypeToken<SandstoneType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<SandstoneType>> SAND_STONE_VALUE_TOKEN = new TypeToken<Value<SandstoneType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SlabType> SLAB_TOKEN = new TypeToken<SlabType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<SlabType>> SLAB_VALUE_TOKEN = new TypeToken<Value<SlabType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ComparatorType> COMPARATOR_TOKEN = new TypeToken<ComparatorType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<ComparatorType>> COMPARATOR_VALUE_TOKEN = new TypeToken<Value<ComparatorType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Hinge> HINGE_TOKEN = new TypeToken<Hinge>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Hinge>> HINGE_VALUE_TOKEN = new TypeToken<Value<Hinge>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<PistonType> PISTON_TOKEN = new TypeToken<PistonType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<PistonType>> PISTON_VALUE_TOKEN = new TypeToken<Value<PistonType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<PortionType> PORTION_TOKEN = new TypeToken<PortionType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<PortionType>> PORTION_VALUE_TOKEN = new TypeToken<Value<PortionType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<RailDirection> RAIL_TOKEN = new TypeToken<RailDirection>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<RailDirection>> RAIL_VALUE_TOKEN = new TypeToken<Value<RailDirection>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<StairShape> STAIR_TOKEN = new TypeToken<StairShape>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<StairShape>> STAIR_VALUE_TOKEN = new TypeToken<Value<StairShape>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<WallType> WALL_TOKEN = new TypeToken<WallType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<WallType>> WALL_VALUE_TOKEN = new TypeToken<Value<WallType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<DoublePlantType> DOUBLE_PLANT_TOKEN = new TypeToken<DoublePlantType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<DoublePlantType>> DOUBLE_PLANT_VALUE_TOKEN = new TypeToken<Value<DoublePlantType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<BigMushroomType> MUSHROOM_TOKEN = new TypeToken<BigMushroomType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<BigMushroomType>> MUSHROOM_VALUE_TOKEN = new TypeToken<Value<BigMushroomType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<EntityType> ENTITY_TYPE_TOKEN = new TypeToken<EntityType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<EntityType>> ENTITY_TYPE_VALUE_TOKEN = new TypeToken<Value<EntityType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Float> FLOAT_TOKEN = new TypeToken<Float>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MutableBoundedValue<Float>> FLOAT_VALUE_TOKEN = new TypeToken<MutableBoundedValue<Float>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<NotePitch> NOTE_TOKEN = new TypeToken<NotePitch>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<NotePitch>> NOTE_VALUE_TOKEN = new TypeToken<Value<NotePitch>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<EntitySnapshot> ENTITY_TOKEN = new TypeToken<EntitySnapshot>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<EntitySnapshot>> ENTITY_VALUE_TOKEN = new TypeToken<Value<EntitySnapshot>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Art> ART_TOKEN = new TypeToken<Art>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Art>> ART_VALUE_TOKEN = new TypeToken<Value<Art>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<BlockState> BLOCK_TOKEN = new TypeToken<BlockState>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<BlockState>> BLOCK_VALUE_TOKEN = new TypeToken<Value<BlockState>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<SkeletonType> SKELETON_TOKEN = new TypeToken<SkeletonType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<SkeletonType>> SKELETON_VALUE_TOKEN = new TypeToken<Value<SkeletonType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<OcelotType> OCELOT_TOKEN = new TypeToken<OcelotType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<OcelotType>> OCELOT_VALUE_TOKEN = new TypeToken<Value<OcelotType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<RabbitType> RABBIT_TOKEN = new TypeToken<RabbitType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<RabbitType>> RABBIT_VALUE_TOKEN = new TypeToken<Value<RabbitType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Map<UUID, Vector3d>> MAP_UUID_VECTOR3D_TOKEN = new TypeToken<Map<UUID, Vector3d>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MapValue<UUID, Vector3d>> MAP_UUID_VECTOR3D_VALUE_TOKEN = new TypeToken<MapValue<UUID, Vector3d>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<UUID>> UUID_VALUE_TOKEN = new TypeToken<Value<UUID>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Rotation> ROTATION_TOKEN = new TypeToken<Rotation>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Rotation>> ROTATION_VALUE_TOKEN = new TypeToken<Value<Rotation>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<FluidStackSnapshot> FLUID_TOKEN = new TypeToken<FluidStackSnapshot>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<FluidStackSnapshot>> FLUID_VALUE_TOKEN = new TypeToken<Value<FluidStackSnapshot>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Map<Direction, List<FluidStackSnapshot>>> MAP_DIRECTION_FLUID_TOKEN = new TypeToken<Map<Direction, List<FluidStackSnapshot>>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MapValue<Direction, List<FluidStackSnapshot>>> MAP_DIRECTION_FLUID_VALUE_TOKEN = new TypeToken<MapValue<Direction, List<FluidStackSnapshot>>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Instant> INSTANT_TOKEN = new TypeToken<Instant>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<Instant>> INSTANT_VALUE_TOKEN = new TypeToken<Value<Instant>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<List<PotionEffect>> LIST_POTION_TOKEN = new TypeToken<List<PotionEffect>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<ListValue<PotionEffect>> LIST_POTION_VALUE_TOKEN = new TypeToken<ListValue<PotionEffect>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Map<BodyPart, Vector3d>> MAP_BODY_VECTOR3D_TOKEN = new TypeToken<Map<BodyPart, Vector3d>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<MapValue<BodyPart, Vector3d>> MAP_BODY_VECTOR3D_VALUE_TOKEN = new TypeToken<MapValue<BodyPart, Vector3d>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Optional<PotionEffectType>> OPTIONAL_POTION_TOKEN = new TypeToken<Optional<PotionEffectType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<OptionalValue<PotionEffectType>> OPTIONAL_POTION_VALUE_TOKEN = new TypeToken<OptionalValue<PotionEffectType>>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<PickupRule> PICKUP_TOKEN = new TypeToken<PickupRule>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<PickupRule>> PICKUP_VALUE_TOKEN = new TypeToken<Value<PickupRule>>() {
        private static final long serialVersionUID = -1;
    };

    public static final TypeToken<ZombieType> ZOMBIE_TYPE_TOKEN = new TypeToken<ZombieType>() {
        private static final long serialVersionUID = -1;
    };
    public static final TypeToken<Value<ZombieType>> ZOMBIE_TYPE_VALUE_TOKEN = new TypeToken<Value<ZombieType>>() {
        private static final long serialVersionUID = -1;
    };

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

        this.fieldMap.put("axis", makeSingleKey(AXIS_TOKEN, AXIS_VALUE_TOKEN, of("Axis"), "sponge:axis", "Axis"));

        this.fieldMap.put("color", makeSingleKey(COLOR_TOKEN, COLOR_VALUE_TOKEN, of("Color"), "sponge:color", "Color"));

        this.fieldMap.put("health", makeSingleKey(DOUBLE_TOKEN, BOUNDED_DOUBLE_VALUE_TOKEN, of("Health"), "sponge:health", "Health"));

        this.fieldMap.put("max_health", makeSingleKey(DOUBLE_TOKEN, BOUNDED_DOUBLE_VALUE_TOKEN, of("MaxHealth"), "sponge:max_health", "Max Health"));

        this.fieldMap.put("display_name", makeSingleKey(TEXT_TOKEN, TEXT_VALUE_TOKEN, of("DisplayName"), "sponge:display_name", "Display Name"));

        this.fieldMap.put("career", makeSingleKey(CAREER_TOKEN, CAREER_VALUE_TOKEN, of("Career"), "sponge:career", "Career"));

        this.fieldMap.put("sign_lines", makeListKey(LIST_TEXT_TOKEN, LIST_TEXT_VALUE_TOKEN, of("SignLines"), "sponge:sign_lines", "Sign Lines"));

        this.fieldMap.put("skull_type", makeSingleKey(SKULL_TOKEN, SKULL_VALUE_TOKEN, of("SkullType"), "sponge:skull_type", "Skull Type"));

        this.fieldMap.put("is_sneaking", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSneaking"), "sponge:sneaking", "Is Sneaking"));

        this.fieldMap.put("velocity", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("Velocity"), "sponge:velocity", "Velocity"));

        this.fieldMap.put("food_level", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("FoodLevel"), "sponge:food_level", "Food Level"));

        this.fieldMap.put("saturation", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FoodSaturationLevel"), "sponge:food_saturation_level", "Food Saturation Level"));

        this.fieldMap.put("exhaustion", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FoodExhaustionLevel"), "sponge:food_exhaustion_level", ""));

        this.fieldMap.put("max_air", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("MaxAir"), "sponge:max_air", "Max Air"));

        this.fieldMap.put("remaining_air", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("RemainingAir"), "sponge:remaining_air", "Remaining Air"));

        this.fieldMap.put("fire_ticks", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("FireTicks"), "sponge:fire_ticks", "Fire Ticks"));

        this.fieldMap.put("fire_damage_delay", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("FireDamageDelay"), "sponge:fire_damage_delay", "Fire Damage Delay"));

        this.fieldMap.put("game_mode", makeSingleKey(GAME_MODE_TOKEN, GAME_MODE_VALUE_TOKEN, of("GameMode"), "sponge:game_mode", "Game Mode"));

        this.fieldMap.put("is_screaming", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsScreaming"), "sponge:screaming", "Is Screaming"));

        this.fieldMap.put("can_fly", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanFly"), "sponge:can_fly", "Can Fly"));

        this.fieldMap.put("can_grief", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanGrief"), "sponge:can_grief", "Can Grief"));

        this.fieldMap.put("shrub_type", makeSingleKey(SHRUB_TOKEN, SHRUB_VALUE_TOKEN, of("ShrubType"), "sponge:shrub_type", "Shrub Type"));

        this.fieldMap.put("plant_type", makeSingleKey(PLANT_TOKEN, PLANT_VALUE_TOKEN, of("PlantType"), "sponge:plant_type", "Plant Type"));

        this.fieldMap.put("tree_type", makeSingleKey(TREE_TOKEN, TREE_VALUE_TOKEN, of("TreeType"), "sponge:tree_type", "Tree Type"));

        this.fieldMap.put("log_axis", makeSingleKey(LOG_AXIS_TOKEN, LOG_AXIS_VALUE_TOKEN, of("LogAxis"), "sponge:log_axis", "Log Axis"));

        this.fieldMap.put("invisible", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Invisible"), "sponge:invisible", "Invisible"));

        this.fieldMap.put("vanish", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Vanish"), "sponge:vanish", "Vanish"));

        this.fieldMap.put("invisible", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Invisible"), "sponge:invisible", "Invisible"));

        this.fieldMap.put("powered", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Powered"), "sponge:powered", "Powered"));

        this.fieldMap.put("layer", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Layer"), "sponge:layer", "Layer"));

        this.fieldMap.put("represented_item", makeSingleKey(ITEM_SNAPSHOT_TOKEN, ITEM_SNAPSHOT_VALUE_TOKEN, of("ItemStackSnapshot"), "sponge:item_stack_snapshot", "Item Stack Snapshot"));

        this.fieldMap.put("command", makeSingleKey(STRING_TOKEN, STRING_VALUE_TOKEN, of("Command"), "sponge:command", "Command"));

        this.fieldMap.put("success_count", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("SuccessCount"), "sponge:success_count", "SuccessCount"));

        this.fieldMap.put("tracks_output", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("TracksOutput"), "sponge:tracks_output", "Tracks Output"));

        this.fieldMap.put("last_command_output", makeOptionalKey(OPTIONAL_TEXT_TOKEN, OPTIONAL_TEXT_VALUE_TOKEN, of("LastCommandOutput"), "sponge:last_command_output", "Last Command Output"));

        this.fieldMap.put("trade_offers", makeListKey(LIST_TRADE_OFFER_TOKEN, LIST_VALUE_TRADE_OFFER_TOKEN, of("TradeOffers"), "sponge:trade_offers", "Trade Offers"));

        this.fieldMap.put("dye_color", makeSingleKey(DYE_COLOR_TOKEN, DYE_COLOR_VALUE_TOKEN, of("DyeColor"), "sponge:dye_color", "Dye Color"));

        this.fieldMap.put("firework_flight_modifier", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("FlightModifier"), "sponge:flight_modifier", "Flight Modifier"));

        this.fieldMap.put("firework_effects", makeListKey(LIST_FIREWORK_TOKEN, LIST_VALUE_FIREWORK_TOKEN, of("FireworkEffects"), "sponge:firework_effects", "Firework Effects"));

        this.fieldMap.put("spawner_remaining_delay", makeSingleKey(SHORT_TOKEN, BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerRemainingDelay"), "sponge:spawner_remaining_delay", "Spawner Remaining Delay"));

        this.fieldMap.put("spawner_minimum_delay", makeSingleKey(SHORT_TOKEN, BOUNDED_SHORT_VALUE_TOKEN, of("SpawnerMinimumDelay"), "sponge:spawner_minimum_delay", "Spawner Minimum Delay"));

        this.fieldMap.put("connected_directions", makeSetKey(SET_DIRECTION_TOKEN, SET_DIRECTION_VALUE_TOKEN, of("ConnectedDirections"), "sponge:connected_directions", "Connected Directions"));

        this.fieldMap.put("connected_north", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedNorth"), "sponge:connected_north", "Connected North"));

        this.fieldMap.put("connected_south", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedSouth"), "sponge:connected_south", "Connected South"));

        this.fieldMap.put("connected_east", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedEast"), "sponge:connected_east", "Connected East"));

        this.fieldMap.put("connected_west", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ConnectedWest"), "sponge:connected_west", "Connected West"));

        this.fieldMap.put("direction", makeSingleKey(DIRECTION_TOKEN, DIRECTION_VALUE_TOKEN, of("Direction"), "sponge:direction", "Direction"));

        this.fieldMap.put("dirt_type", makeSingleKey(DIRT_TOKEN, DIRT_VALUE_TOKEN, of("DirtType"), "sponge:dirt_type", "Dirt Type"));

        this.fieldMap.put("disguised_block_type", makeSingleKey(DISGUISED_BLOCK_TOKEN, DISGUISED_BLOCK_VALUE_TOKEN, of("DisguisedBlockType"), "sponge:disguised_block_type", "Disguised Block Type"));

        this.fieldMap.put("disarmed", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Disarmed"), "sponge:disarmed", "Disarmed"));

        this.fieldMap.put("item_enchantments", makeListKey(LIST_ITEM_ENCHANTMENT_TOKEN, LIST_ITEM_ENCHANTMENT_VALUE_TOKEN, of("ItemEnchantments"), "sponge:item_enchantments", "Item Enchantments"));

        this.fieldMap.put("banner_patterns", makeListKey(LIST_PATTERN_TOKEN, LIST_PATTERN_VALUE_TOKEN, of("BannerPatterns"), "sponge:banner_patterns", "Banner Patterns"));

        this.fieldMap.put("banner_base_color", makeListKey(LIST_DYE_COLOR_TOKEN, LIST_DYE_COLOR_VALUE_TOKEN, of("BannerBaseColor"), "sponge:banner_base_color", "Banner Base Color"));

        this.fieldMap.put("horse_color", makeSingleKey(HORSE_COLOR_TOKEN, HORSE_COLOR_VALUE_TOKEN, of("HorseColor"), "sponge:horse_color", "Horse Color"));

        this.fieldMap.put("horse_style", makeSingleKey(HORSE_STYLE_TOKEN, HORSE_STYLE_VALUE_TOKEN, of("HorseStyle"), "sponge:horse_style", "Horse Style"));

        this.fieldMap.put("horse_variant", makeSingleKey(HORSE_VARIANT_TOKEN, HORSE_VARIANT_VALUE_TOKEN, of("HorseVariant"), "sponge:horse_variant", "Horse Variant"));

        this.fieldMap.put("item_lore", makeListKey(LIST_TEXT_TOKEN, LIST_TEXT_VALUE_TOKEN, of("ItemLore"), "sponge:item_lore", "Item Lore"));

        this.fieldMap.put("book_pages", makeListKey(LIST_TEXT_TOKEN, LIST_TEXT_VALUE_TOKEN, of("BookPages"), "sponge:book_pages", "Book Pages"));

        this.fieldMap.put("golden_apple_type", makeSingleKey(GOLDEN_APPLE_TOKEN, GOLDEN_APPLE_VALUE_TOKEN, of("GoldenAppleType"), "sponge:golden_apple_type", "Golden Apple Type"));

        this.fieldMap.put("is_flying", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsFlying"), "sponge:is_flying", "Is Flying"));

        this.fieldMap.put("experience_level", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceLevel"), "sponge:experience_level", "Experience Level"));

        this.fieldMap.put("total_experience", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("TotalExperience"), "sponge:total_experience", "Total Experience"));

        this.fieldMap.put("experience_since_level", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceSinceLevel"), "sponge:experience_since_level", "Experience Since Level"));

        this.fieldMap.put("experience_from_start_of_level", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExperienceFromStartOfLevel"), "sponge:experience_from_start_of_level", "Experience From Start Of Level"));

        this.fieldMap.put("book_author", makeSingleKey(TEXT_TOKEN, TEXT_VALUE_TOKEN, of("BookAuthor"), "sponge:book_author", "Book Author"));

        this.fieldMap.put("breakable_block_types", makeSetKey(SET_BLOCK_TOKEN, SET_BLOCK_VALUE_TOKEN, of("CanDestroy"), "sponge:can_destroy", "Can Destroy"));

        this.fieldMap.put("placeable_blocks", makeSetKey(SET_BLOCK_TOKEN, SET_BLOCK_VALUE_TOKEN, of("CanPlaceOn"), "sponge:can_place_on", "Can Place On"));

        this.fieldMap.put("walking_speed", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("WalkingSpeed"), "sponge:walking_speed", "Walking Speed"));

        this.fieldMap.put("flying_speed", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FlyingSpeed"), "sponge:flying_speed", "Flying Speed"));

        this.fieldMap.put("slime_size", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("SlimeSize"), "sponge:slime_size", "Slime Size"));

        this.fieldMap.put("villager_zombie_profession", makeOptionalKey(OPTIONAL_PROFESSION_TOKEN, OPTIONAL_PROFESSION_VALUE_TOKEN, of("VillagerZombieProfession"), "sponge:villager_zombie_profession", "Villager Zombie Profession"));

        this.fieldMap.put("is_playing", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsPlaying"), "sponge:is_playing", "Is Playing"));

        this.fieldMap.put("is_sitting", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSitting"), "sponge:is_sitting", "Is Sitting"));

        this.fieldMap.put("is_sheared", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSheared"), "sponge:is_sheared", "Is Sheared"));

        this.fieldMap.put("pig_saddle", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsPigSaddled"), "sponge:is_pig_saddled", "Is Pig Saddled"));

        this.fieldMap.put("tamed_owner", makeOptionalKey(OPTIONAL_UUID_TOKEN, OPTIONAL_UUID_VALUE_TOKEN, of("TamerUUID"), "sponge:tamer_uuid", "Tamer UUID"));

        this.fieldMap.put("is_wet", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsWet"), "sponge:is_wet", "Is Wet"));

        this.fieldMap.put("elder_guardian", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Elder"), "sponge:elder", "Elder"));

        this.fieldMap.put("coal_type", makeSingleKey(COAL_TOKEN, COAL_VALUE_TOKEN, of("CoalType"), "sponge:coal_type", "Coal Type"));

        this.fieldMap.put("cooked_fish", makeSingleKey(COOKED_FISH_TOKEN, COOKED_FISH_VALUE_TOKEN, of("CookedFishType"), "sponge:cooked_fish_type", "Cooked Fish Type"));

        this.fieldMap.put("fish_type", makeSingleKey(FISH_TOKEN, FISH_VALUE_TOKEN, of("RawFishType"), "sponge:raw_fish_type", "Raw Fish Type"));

        this.fieldMap.put("represented_player", makeSingleKey(GAME_PROFILE_TOKEN, GAME_PROFILE_VALUE_TOKEN, of("RepresentedPlayer"), "sponge:represented_player", "Represented Player"));

        this.fieldMap.put("passed_burn_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("PassedBurnTime"), "sponge:passed_burn_time", "Passed Burn Time"));

        this.fieldMap.put("max_burn_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("MaxBurnTime"), "sponge:max_burn_time", "Max Burn Time"));

        this.fieldMap.put("passed_cook_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("PassedCookTime"), "sponge:passed_cook_time", "Passed Cook Time"));

        this.fieldMap.put("max_cook_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("MaxCookTime"), "sponge:max_cook_time", "Max Cook Time"));

        this.fieldMap.put("contained_experience", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("ContainedExperience"), "sponge:contained_experience", "Contained Experience"));

        this.fieldMap.put("remaining_brew_time", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("RemainingBrewTime"), "sponge:remaining_brew_time", "Remaining Brew Time"));

        this.fieldMap.put("stone_type", makeSingleKey(STONE_TOKEN, STONE_VALUE_TOKEN, of("StoneType"), "sponge:stone_type", "Stone Type"));

        this.fieldMap.put("prismarine_type", makeSingleKey(PRISMARINE_TOKEN, PRISMARINE_VALUE_TOKEN, of("PrismarineType"), "sponge:prismarine_type", "Prismarine Type"));

        this.fieldMap.put("brick_type", makeSingleKey(BRICK_TOKEN, BRICK_VALUE_TOKEN, of("BrickType"), "sponge:brick_type", "Brick Type"));

        this.fieldMap.put("quartz_type", makeSingleKey(QUARTZ_TOKEN, QUARTZ_VALUE_TOKEN, of("QuartzType"), "sponge:quartz_type", "Quartz Type"));

        this.fieldMap.put("sand_type", makeSingleKey(SAND_TOKEN, SAND_VALUE_TOKEN, of("SandType"), "sponge:sand_type", "Sand Type"));

        this.fieldMap.put("sandstone_type", makeSingleKey(SAND_STONE_TOKEN, SAND_STONE_VALUE_TOKEN, of("SandstoneType"), "sponge:sandstone_type", "Sandstone Type"));

        this.fieldMap.put("slab_type", makeSingleKey(SLAB_TOKEN, SLAB_VALUE_TOKEN, of("SlabType"), "sponge:slab_type", "Slab Type"));

        this.fieldMap.put("sandstone_type", makeSingleKey(SAND_STONE_TOKEN, SAND_STONE_VALUE_TOKEN, of("SandstoneType"), "sponge:sandstone_type", "Sandstone Type"));

        this.fieldMap.put("comparator_type", makeSingleKey(COMPARATOR_TOKEN, COMPARATOR_VALUE_TOKEN, of("ComparatorType"), "sponge:comparator_type", "Comparator Type"));

        this.fieldMap.put("hinge_position", makeSingleKey(HINGE_TOKEN, HINGE_VALUE_TOKEN, of("HingePosition"), "sponge:hinge_position", "Hinge Position"));

        this.fieldMap.put("piston_type", makeSingleKey(PISTON_TOKEN, PISTON_VALUE_TOKEN, of("PistonType"), "sponge:piston_type", "Piston Type"));

        this.fieldMap.put("portion_type", makeSingleKey(PORTION_TOKEN, PORTION_VALUE_TOKEN, of("PortionType"), "sponge:portion_type", "Portion Type"));

        this.fieldMap.put("rail_direction", makeSingleKey(RAIL_TOKEN, RAIL_VALUE_TOKEN, of("RailDirection"), "sponge:rail_direction", "Rail Direction"));

        this.fieldMap.put("stair_shape", makeSingleKey(STAIR_TOKEN, STAIR_VALUE_TOKEN, of("StairShape"), "sponge:stair_shape", "Stair Shape"));

        this.fieldMap.put("wall_type", makeSingleKey(WALL_TOKEN, WALL_VALUE_TOKEN, of("WallType"), "sponge:wall_type", "Wall Type"));

        this.fieldMap.put("double_plant_type", makeSingleKey(DOUBLE_PLANT_TOKEN, DOUBLE_PLANT_VALUE_TOKEN, of("DoublePlantType"), "sponge:double_plant_type", "Double Plant Type"));

        this.fieldMap.put("big_mushroom_type", makeSingleKey(MUSHROOM_TOKEN, MUSHROOM_VALUE_TOKEN, of("BigMushroomType"), "sponge:big_mushroom_type", "Big Mushroom Type"));

        this.fieldMap.put("ai_enabled", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsAiEnabled"), "sponge:is_ai_enabled", "Is Ai Enabled"));

        this.fieldMap.put("creeper_charged", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsCreeperCharged"), "sponge:is_creeper_charged", "Is Creeper Charged"));

        this.fieldMap.put("item_durability", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ItemDurability"), "sponge:item_durability", "Item Durability"));

        this.fieldMap.put("unbreakable", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Unbreakable"), "sponge:unbreakable", "Unbreakable"));

        this.fieldMap.put("spawnable_entity_type", makeSingleKey(ENTITY_TYPE_TOKEN, ENTITY_TYPE_VALUE_TOKEN, of("SpawnableEntityType"), "sponge:spawnable_entity_type", "Spawnable Entity Type"));

        this.fieldMap.put("fall_distance", makeSingleKey(FLOAT_TOKEN, FLOAT_VALUE_TOKEN, of("FallDistance"), "sponge:fall_distance", "Fall Distance"));

        this.fieldMap.put("cooldown", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("Cooldown"), "sponge:cooldown", "Cooldown"));

        this.fieldMap.put("note_pitch", makeSingleKey(NOTE_TOKEN, NOTE_VALUE_TOKEN, of("Note"), "sponge:note", "Note"));

        this.fieldMap.put("vehicle", makeSingleKey(ENTITY_TOKEN, ENTITY_VALUE_TOKEN, of("Vehicle"), "sponge:vehicle", "Vehicle"));

        this.fieldMap.put("base_vehicle", makeSingleKey(ENTITY_TOKEN, ENTITY_VALUE_TOKEN, of("BaseVehicle"), "sponge:base_vehicle", "Base Vehicle"));

        this.fieldMap.put("art", makeSingleKey(ART_TOKEN, ART_VALUE_TOKEN, of("Art"), "sponge:art", "Art"));

        this.fieldMap.put("fall_damage_per_block", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("FallDamagePerBlock"), "sponge:fall_damage_per_block", "Fall Damage Per Block"));

        this.fieldMap.put("max_fall_damage", makeSingleKey(DOUBLE_TOKEN, DOUBLE_VALUE_TOKEN, of("MaxFallDamage"), "sponge:max_fall_damage", "Max Fall Damage"));

        this.fieldMap.put("falling_block_state", makeSingleKey(BLOCK_TOKEN, BLOCK_VALUE_TOKEN, of("FallingBlockState"), "sponge:falling_block_state", "Falling Block State"));

        this.fieldMap.put("can_place_as_block", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanPlaceAsBlock"), "sponge:can_place_as_block", "Can Place As Block"));

        this.fieldMap.put("can_drop_as_item", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanDropAsItem"), "sponge:can_drop_as_item", "Can Drop As Item"));

        this.fieldMap.put("fall_time", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("FallTime"), "sponge:fall_time", "Fall Time"));

        this.fieldMap.put("falling_block_can_hurt_entities", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanFallingBlockHurtEntities"), "sponge:can_falling_block_hurt_entities", "Can Falling Block Hurt Entities"));

        this.fieldMap.put("represented_block", makeSingleKey(BLOCK_TOKEN, BLOCK_VALUE_TOKEN, of("RepresentedBlock"), "sponge:represented_block", "Represented Block"));

        this.fieldMap.put("offset", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("BlockOffset"), "sponge:block_offset", "Block Offset"));

        this.fieldMap.put("attached", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Attached"), "sponge:attached", "Attached"));

        this.fieldMap.put("should_drop", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("ShouldDrop"), "sponge:should_drop", "Should Drop"));

        this.fieldMap.put("extended", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Extended"), "sponge:extended", "Extended"));

        this.fieldMap.put("growth_stage", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("GrowthStage"), "sponge:growth_stage", "Growth Stage"));

        this.fieldMap.put("open", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Open"), "sponge:open", "Open"));

        this.fieldMap.put("power", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Power"), "sponge:power", "Power"));

        this.fieldMap.put("seamless", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Seamless"), "sponge:seamless", "Seamless"));

        this.fieldMap.put("snowed", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Snowed"), "sponge:snowed", "Snowed"));

        this.fieldMap.put("suspended", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Suspended"), "sponge:suspended", "Suspended"));

        this.fieldMap.put("occupied", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Occupied"), "sponge:occupied", "Occupied"));

        this.fieldMap.put("decayable", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Decayable"), "sponge:decayable", "Decayable"));

        this.fieldMap.put("in_wall", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("InWall"), "sponge:in_wall", "In Wall"));

        this.fieldMap.put("delay", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Delay"), "sponge:delay", "Delay"));

        this.fieldMap.put("player_created", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("PlayerCreated"), "sponge:player_created", "Player Created"));

        this.fieldMap.put("item_blockstate", makeSingleKey(BLOCK_TOKEN, BLOCK_VALUE_TOKEN, of("ItemBlockState"), "sponge:item_block_state", "Item Block State"));

        this.fieldMap.put("skeleton_type", makeSingleKey(SKELETON_TOKEN, SKELETON_VALUE_TOKEN, of("SkeletonType"), "sponge:skeleton_type", "Skeleton Type"));

        this.fieldMap.put("ocelot_type", makeSingleKey(OCELOT_TOKEN, OCELOT_VALUE_TOKEN, of("OcelotType"), "sponge:ocelot_type", "Ocelot Type"));

        this.fieldMap.put("rabbit_type", makeSingleKey(RABBIT_TOKEN, RABBIT_VALUE_TOKEN, of("RabbitType"), "sponge:rabbit_type", "Rabbit Type"));

        this.fieldMap.put("lock_token", makeSingleKey(STRING_TOKEN, STRING_VALUE_TOKEN, of("Lock"), "sponge:lock", "Lock"));

        this.fieldMap.put("banner_base_color", makeSingleKey(DYE_COLOR_TOKEN, DYE_COLOR_VALUE_TOKEN, of("BannerBaseColor"), "sponge:banner_base_color", "Banner Base Color"));

        this.fieldMap.put("banner_patterns", new PatternKey());

        this.fieldMap.put("respawn_locations", makeMapKey(MAP_UUID_VECTOR3D_TOKEN, MAP_UUID_VECTOR3D_VALUE_TOKEN, of("RespawnLocations"), "sponge:respawn_locations", "Respawn Locations"));

        this.fieldMap.put("expiration_ticks", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("ExpirationTicks"), "sponge:expiration_ticks", "Expiration Ticks"));

        this.fieldMap.put("skin_unique_id", makeSingleKey(UUID_TOKEN, UUID_VALUE_TOKEN, of("SkinUUID"), "sponge:skin_uuid", "Skin UUID"));

        this.fieldMap.put("moisture", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Moisture"), "sponge:moisture", "Moisture"));

        this.fieldMap.put("angry", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Angry"), "sponge:angry", "Angry"));

        this.fieldMap.put("anger", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Anger"), "sponge:anger", "Anger"));

        this.fieldMap.put("rotation", makeSingleKey(ROTATION_TOKEN, ROTATION_VALUE_TOKEN, of("Rotation"), "sponge:rotation", "Rotation"));

        this.fieldMap.put("is_splash_potion", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSplashPotion"), "sponge:is_splash_potion", "Is Splash Potion"));

        this.fieldMap.put("affects_spawning", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("AffectsSpawning"), "sponge:affects_spawning", "Affects Spawning"));

        this.fieldMap.put("critical_hit", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CriticalHit"), "sponge:critical_hit", "Critical Hit"));

        this.fieldMap.put("generation", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("Generation"), "sponge:generation", "Generation"));

        this.fieldMap.put("passenger", makeSingleKey(ENTITY_TOKEN, ENTITY_VALUE_TOKEN, of("PassengerSnapshot"), "sponge:passenger_snapshot", "Passenger Snapshot"));

        this.fieldMap.put("knockback_strength", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("KnockbackStrength"), "sponge:knockback_strength", "Knockback Strength"));

        this.fieldMap.put("persists", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Persists"), "sponge:persists", "Persists"));

        this.fieldMap.put("stored_enchantments", makeListKey(LIST_ITEM_ENCHANTMENT_TOKEN, LIST_ITEM_ENCHANTMENT_VALUE_TOKEN, of("StoredEnchantments"), "sponge:stored_enchantments", "Stored Enchantments"));

        this.fieldMap.put("is_sprinting", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Sprinting"), "sponge:sprinting", "Sprinting"));

        this.fieldMap.put("stuck_arrows", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("StuckArrows"), "sponge:stuck_arrows", "Stuck Arrows"));

        this.fieldMap.put("vanish_ignores_collision", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("VanishIgnoresCollision"), "sponge:vanish_ignores_collision", "Vanish Ignores Collision"));

        this.fieldMap.put("vanish_prevents_targeting", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("VanishPreventsTargeting"), "sponge:vanish_prevents_targeting", "Vanish Prevents Targeting"));

        this.fieldMap.put("is_aflame", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsAflame"), "sponge:is_aflame", "Is Aflame"));

        this.fieldMap.put("can_breed", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CanBreed"), "sponge:can_breed", "Can Breed"));

        this.fieldMap.put("fluid_item_stack", makeSingleKey(FLUID_TOKEN, FLUID_VALUE_TOKEN, of("FluidItemContainerSnapshot"), "sponge:fluid_item_container_snapshot", "Fluid Item Container Snapshot"));

        this.fieldMap.put("fluid_tank_contents", makeMapKey(MAP_DIRECTION_FLUID_TOKEN, MAP_DIRECTION_FLUID_VALUE_TOKEN, of("FluidTankContents"), "sponge:fluid_tank_contents", "Fluid Tank Contents"));

        this.fieldMap.put("custom_name_visible", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("CustomNameVisible"), "sponge:custom_name_visible", "Custom Name Visible"));

        this.fieldMap.put("first_date_played", makeSingleKey(INSTANT_TOKEN, INSTANT_VALUE_TOKEN, of("FirstTimeJoined"), "sponge:first_time_joined", "First Time Joined"));

        this.fieldMap.put("last_date_played", makeSingleKey(INSTANT_TOKEN, INSTANT_VALUE_TOKEN, of("LastTimePlayed"), "sponge:last_time_played", "Last Time Played"));

        this.fieldMap.put("hide_enchantments", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideEnchantments"), "sponge:hide_enchantments", "Hide Enchantments"));

        this.fieldMap.put("hide_attributes", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideAttributes"), "sponge:hide_attributes", "Hide Attributes"));

        this.fieldMap.put("hide_unbreakable", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideUnbreakable"), "sponge:hide_unbreakable", "Hide Unbreakable"));

        this.fieldMap.put("hide_can_destroy", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideCanDestroy"), "sponge:hide_can_destroy", "Hide Can Destroy"));

        this.fieldMap.put("hide_can_place", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideCanPlace"), "sponge:hide_can_place", "Hide Can Place"));

        this.fieldMap.put("hide_miscellaneous", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HideMiscellaneous"), "sponge:hide_miscellaneous", "Hide Miscellaneous"));

        this.fieldMap.put("potion_effects", makeListKey(LIST_POTION_TOKEN, LIST_POTION_VALUE_TOKEN, of("PotionEffects"), "sponge:potion_effects", "Potion Effects"));

        this.fieldMap.put("body_rotations", makeMapKey(MAP_BODY_VECTOR3D_TOKEN, MAP_BODY_VECTOR3D_VALUE_TOKEN, of("BodyRotations"), "sponge:body_rotations", "Body Rotations"));

        this.fieldMap.put("head_rotation", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("HeadRotation"), "sponge:head_rotation", "Head Rotation"));

        this.fieldMap.put("chest_rotation", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("ChestRotation"), "sponge:chest_rotation", "Chest Rotation"));

        this.fieldMap.put("left_arm_rotation", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("LeftArmRotation"), "sponge:left_arm_rotation", "Left Arm Rotation"));

        this.fieldMap.put("right_arm_rotation", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("RightArmRotation"), "sponge:right_arm_rotation", "Right Arm Rotation"));

        this.fieldMap.put("left_leg_rotation", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("LeftLegRotation"), "sponge:left_leg_rotation", "Left Leg Rotation"));

        this.fieldMap.put("right_leg_rotation", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("RightLegRotation"), "sponge:right_leg_rotation", "Right Leg Rotation"));

        this.fieldMap.put("beacon_primary_effect", makeOptionalKey(OPTIONAL_POTION_TOKEN, OPTIONAL_POTION_VALUE_TOKEN, of("BeaconPrimaryEffect"), "sponge:beacon_primary_effect", "Beacon Primary Effect"));

        this.fieldMap.put("beacon_secondary_effect", makeOptionalKey(OPTIONAL_POTION_TOKEN, OPTIONAL_POTION_VALUE_TOKEN, of("BeaconSecondaryEffect"), "sponge:beacon_secondary_effect", "Beacon Secondary Effect"));

        this.fieldMap.put("targeted_location", makeSingleKey(VECTOR_3D_TOKEN, VECTOR_3D_VALUE_TOKEN, of("TargetedVector3d"), "sponge:targeted_vector_3d", "Targeted Vector3d"));

        this.fieldMap.put("fuse_duration", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("FuseDuration"), "sponge:fuse_duration", "Fuse Duration"));

        this.fieldMap.put("ticks_remaining", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("TicksRemaining"), "sponge:ticks_remaining", "Ticks Remaining"));

        this.fieldMap.put("explosion_radius", makeSingleKey(INTEGER_TOKEN, INTEGER_VALUE_TOKEN, of("ExplosionRadius"), "sponge:explosion_radius", "Explosion Radius"));

        this.fieldMap.put("armor_stand_has_arms", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HasArms"), "sponge:has_arms", "Has Arms"));

        this.fieldMap.put("armor_stand_has_base_plate", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HasBasePlate"), "sponge:has_base_plate", "Has Base Plate"));

        this.fieldMap.put("armor_stand_marker", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsMarker"), "sponge:is_marker", "Is Marker"));

        this.fieldMap.put("armor_stand_is_small", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSmall"), "sponge:is_small", "Is Small"));

        this.fieldMap.put("is_silent", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("IsSilent"), "sponge:is_silent", "Is Silent"));

        this.fieldMap.put("glowing", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("Glowing"), "sponge:glowing", "Glowing"));

        this.fieldMap.put("pickup_rule", makeSingleKey(PICKUP_TOKEN, PICKUP_VALUE_TOKEN, of("PickupRule"), "sponge:pickupRule", "Pickup Rule"));

        this.fieldMap.put("invulnerability_ticks", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("HurtTime"), "sponge:invulnerability_ticks", "Invulnerability Ticks"));

        this.fieldMap.put("has_gravity", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("HasGravity"), "sponge:has_gravity", "Has Gravity"));

        this.fieldMap.put("zombie_type", makeSingleKey(ZOMBIE_TYPE_TOKEN, ZOMBIE_TYPE_VALUE_TOKEN, of("ZombieType"), "sponge:zombie_type", "Zombie Type"));

        this.fieldMap.put("infinite_despawn_delay", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("InfiniteDespawnDelay"), "sponge:infinite_despawn_delay", "Infinite Despawn Delay"));
        this.fieldMap.put("infinite_pickup_delay", makeSingleKey(BOOLEAN_TOKEN, BOOLEAN_VALUE_TOKEN, of("InfinitePickupDelay"), "sponge:infinite_pickup_delay", "Infinite Pickup Delay"));
        this.fieldMap.put("despawn_delay", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("DespawnDelay"), "sponge:despawn_delay", "Despawn Delay"));
        this.fieldMap.put("pickup_delay", makeSingleKey(INTEGER_TOKEN, BOUNDED_INTEGER_VALUE_TOKEN, of("PickupDelay"), "sponge:pickup_delay", "Pickup Delay"));

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
        if (checkNotNull(id, "Key id cannot be null!").contains("sponge:")) {
            id = id.replace("sponge:", "");
        }
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
