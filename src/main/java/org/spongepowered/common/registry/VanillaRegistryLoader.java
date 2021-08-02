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
package org.spongepowered.common.registry;

import com.google.common.base.CaseFormat;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.FrameType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.advancement.criteria.trigger.Triggers;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.map.decoration.MapDecorationTypes;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.common.accessor.advancements.CriteriaTriggersAccessor;
import org.spongepowered.common.accessor.world.entity.animal.MushroomCow_MushroomTypeAccessor;
import org.spongepowered.common.accessor.world.item.ArmorMaterialsAccessor;
import org.spongepowered.common.accessor.world.level.GameRulesAccessor;
import org.spongepowered.common.accessor.world.level.block.entity.BannerPatternAccessor;
import org.spongepowered.common.advancement.criterion.SpongeDummyTrigger;
import org.spongepowered.common.advancement.criterion.SpongeScoreTrigger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

final class VanillaRegistryLoader {
    private final SpongeRegistryHolder holder;

    public static void load(final SpongeRegistryHolder holder) {
        final VanillaRegistryLoader loader = new VanillaRegistryLoader(holder);
        loader.loadEnumRegistries();
        loader.loadInstanceRegistries();
    }

    private VanillaRegistryLoader(final SpongeRegistryHolder holder) {
        this.holder = holder;
    }

    private void loadInstanceRegistries() {
        this.holder.createRegistry(RegistryTypes.CRITERION, VanillaRegistryLoader.criterion());
        this.manualName(RegistryTypes.DRAGON_PHASE_TYPE, EnderDragonPhase.getCount(), map -> {
            map.put(EnderDragonPhase.HOLDING_PATTERN, "holding_pattern");
            map.put(EnderDragonPhase.STRAFE_PLAYER, "strafe_player");
            map.put(EnderDragonPhase.LANDING_APPROACH, "landing_approach");
            map.put(EnderDragonPhase.LANDING, "landing");
            map.put(EnderDragonPhase.TAKEOFF, "takeoff");
            map.put(EnderDragonPhase.SITTING_FLAMING, "sitting_flaming");
            map.put(EnderDragonPhase.SITTING_SCANNING, "sitting_scanning");
            map.put(EnderDragonPhase.SITTING_ATTACKING, "sitting_attacking");
            map.put(EnderDragonPhase.CHARGING_PLAYER, "charging_player");
            map.put(EnderDragonPhase.DYING, "dying");
            map.put(EnderDragonPhase.HOVERING, "hover");
        });
        this.holder.createRegistry(RegistryTypes.FIREWORK_SHAPE, VanillaRegistryLoader.fireworkShape());
        this.holder.createRegistry(RegistryTypes.TRIGGER, VanillaRegistryLoader.trigger(), true,
                (k, trigger) -> CriteriaTriggersAccessor.invoker$register((CriterionTrigger<?>) trigger));
        this.knownName(RegistryTypes.GAME_RULE, GameRulesAccessor.accessor$GAME_RULE_TYPES().keySet(), rule -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, rule.getId()));
    }

    private void loadEnumRegistries() {
        this.knownName(RegistryTypes.ARMOR_MATERIAL, ArmorMaterials.values(), am -> ((ArmorMaterialsAccessor) (Object) am).accessor$name());
        this.knownName(RegistryTypes.ATTACHMENT_SURFACE, AttachFace.values(), AttachFace::getSerializedName);
        this.manualName(RegistryTypes.ATTRIBUTE_OPERATION, AttributeModifier.Operation.values(), map -> {
            // names come from net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier#operationFromString
            map.put(AttributeModifier.Operation.ADDITION, "addition");
            map.put(AttributeModifier.Operation.MULTIPLY_BASE, "multiply_base");
            map.put(AttributeModifier.Operation.MULTIPLY_TOTAL, "multiply_total");
        });
        this.knownName(RegistryTypes.BOAT_TYPE, Boat.Type.values(), Boat.Type::getName);
        this.knownName(RegistryTypes.CHEST_ATTACHMENT_TYPE, ChestType.values(), ChestType::getSerializedName);
        this.manualName(RegistryTypes.COLLISION_RULE, Team.CollisionRule.values(), map -> {
            map.put(Team.CollisionRule.ALWAYS, "always");
            map.put(Team.CollisionRule.NEVER, "never");
            map.put(Team.CollisionRule.PUSH_OTHER_TEAMS, "push_other_teams");
            map.put(Team.CollisionRule.PUSH_OWN_TEAM, "push_own_team");
        });
        this.knownName(RegistryTypes.COMPARATOR_MODE, ComparatorMode.values(), ComparatorMode::getSerializedName);
        this.knownName(RegistryTypes.DIFFICULTY, Difficulty.values(), Difficulty::getKey);
        this.knownName(RegistryTypes.DYE_COLOR, DyeColor.values(), DyeColor::getSerializedName);
        this.knownName(RegistryTypes.DOOR_HINGE, DoorHingeSide.values(), DoorHingeSide::getSerializedName);
        this.manualName(RegistryTypes.EQUIPMENT_GROUP, EquipmentSlot.Type.values(), map -> {
            map.put(EquipmentSlot.Type.ARMOR, "worn");
            map.put(EquipmentSlot.Type.HAND, "held");
        });
        this.manualName(RegistryTypes.EQUIPMENT_TYPE, EquipmentSlot.values(), map -> {
            map.put(EquipmentSlot.CHEST, "chest");
            map.put(EquipmentSlot.FEET, "feet");
            map.put(EquipmentSlot.HEAD, "head");
            map.put(EquipmentSlot.LEGS, "legs");
            map.put(EquipmentSlot.MAINHAND, "main_hand");
            map.put(EquipmentSlot.OFFHAND, "off_hand");
        });
        this.knownName(RegistryTypes.FOX_TYPE, Fox.Type.values(), Fox.Type::getName);
        this.manualName(RegistryTypes.GAME_MODE, GameType.values(), map -> {
            map.put(GameType.NOT_SET, "not_set"); // getName returns "" (empty string) // TODO(kashike): 1.17
            map.put(GameType.SURVIVAL, GameType.SURVIVAL.getName());
            map.put(GameType.CREATIVE, GameType.CREATIVE.getName());
            map.put(GameType.ADVENTURE, GameType.ADVENTURE.getName());
            map.put(GameType.SPECTATOR, GameType.SPECTATOR.getName());
        });
        this.automaticName(RegistryTypes.HAND_PREFERENCE, HumanoidArm.values());
        this.automaticName(RegistryTypes.HAND_TYPE, InteractionHand.values());
        this.knownName(RegistryTypes.INSTRUMENT_TYPE, NoteBlockInstrument.values(), NoteBlockInstrument::getSerializedName);
        this.automaticName(RegistryTypes.ITEM_RARITY, Rarity.values());
        this.automaticName(RegistryTypes.ITEM_TIER, Tiers.values());
        this.knownName(RegistryTypes.MOOSHROOM_TYPE, MushroomCow.MushroomType.values(), type -> ((MushroomCow_MushroomTypeAccessor) (Object) type).accessor$type());
        this.knownName(RegistryTypes.OBJECTIVE_DISPLAY_MODE, ObjectiveCriteria.RenderType.values(), ObjectiveCriteria.RenderType::getId);
        this.knownName(RegistryTypes.PANDA_GENE, Panda.Gene.values(), Panda.Gene::getName);
        this.automaticName(RegistryTypes.PHANTOM_PHASE, Phantom.AttackPhase.values());
        this.automaticName(RegistryTypes.PICKUP_RULE, AbstractArrow.Pickup.values());
        this.automaticName(RegistryTypes.MIRROR, Mirror.values());
        this.automaticName(RegistryTypes.CHAT_VISIBILITY, ChatVisiblity.values());
        this.knownName(RegistryTypes.PISTON_TYPE, PistonType.values(), PistonType::getSerializedName);
        this.knownName(RegistryTypes.PORTION_TYPE, Half.values(), Half::getSerializedName);
        this.automaticName(RegistryTypes.RAID_STATUS, Raid.RaidStatus.values());
        this.automaticName(RegistryTypes.ROTATION, Rotation.values());
        this.knownName(RegistryTypes.RAIL_DIRECTION, RailShape.values(), RailShape::getSerializedName);
        this.knownName(RegistryTypes.SLAB_PORTION, SlabType.values(), SlabType::getSerializedName);
        this.automaticName(RegistryTypes.SPELL_TYPE, SpellcasterIllager.IllagerSpell.values());
        this.knownName(RegistryTypes.STAIR_SHAPE, StairsShape.values(), StairsShape::getSerializedName);
        this.knownName(RegistryTypes.STRUCTURE_MODE, StructureMode.values(), StructureMode::getSerializedName);
        this.automaticName(RegistryTypes.TASK_PRIORITY, TickPriority.values());
        this.manualName(RegistryTypes.VISIBILITY, Team.Visibility.values(), map -> {
            map.put(Team.Visibility.ALWAYS, "always");
            map.put(Team.Visibility.NEVER, "never");
            map.put(Team.Visibility.HIDE_FOR_OTHER_TEAMS, "hide_for_other_teams");
            map.put(Team.Visibility.HIDE_FOR_OWN_TEAM, "hide_for_own_team");
        });
        this.knownName(RegistryTypes.WIRE_ATTACHMENT_TYPE, RedstoneSide.values(), RedstoneSide::getSerializedName);
        this.knownName(RegistryTypes.ADVANCEMENT_TYPE, FrameType.values(), FrameType::getName);
        this.knownName(RegistryTypes.BANNER_PATTERN_SHAPE, BannerPattern.values(), b -> ((BannerPatternAccessor) (Object) b).accessor$filename());
        this.automaticName(RegistryTypes.TROPICAL_FISH_SHAPE, TropicalFish.Pattern.values());
        this.automaticName(RegistryTypes.HEIGHT_TYPE, Heightmap.Types.values());
    }

    private static RegistryLoader<Criterion> criterion() {
        return RegistryLoader.of(l -> {
            l.add(Criteria.AIR, k -> (Criterion) ObjectiveCriteria.AIR);
            l.add(Criteria.ARMOR, k -> (Criterion) ObjectiveCriteria.ARMOR);
            l.add(Criteria.DEATH_COUNT, k -> (Criterion) ObjectiveCriteria.DEATH_COUNT);
            l.add(Criteria.DUMMY, k -> (Criterion) ObjectiveCriteria.DUMMY);
            l.add(Criteria.EXPERIENCE, k -> (Criterion) ObjectiveCriteria.EXPERIENCE);
            l.add(Criteria.FOOD, k -> (Criterion) ObjectiveCriteria.FOOD);
            l.add(Criteria.HEALTH, k -> (Criterion) ObjectiveCriteria.HEALTH);
            l.add(Criteria.LEVEL, k -> (Criterion) ObjectiveCriteria.LEVEL);
            l.add(Criteria.PLAYER_KILL_COUNT, k -> (Criterion) ObjectiveCriteria.KILL_COUNT_PLAYERS);
            l.add(Criteria.TOTAL_KILL_COUNT, k -> (Criterion) ObjectiveCriteria.KILL_COUNT_ALL);
            l.add(Criteria.TRIGGER, k -> (Criterion) ObjectiveCriteria.TRIGGER);
        });
    }

    private static RegistryLoader<FireworkShape> fireworkShape() {
        return RegistryLoader.of(l -> {
            l.add(FireworkRocketItem.Shape.BURST.getId(), FireworkShapes.BURST, () -> (FireworkShape) (Object) FireworkRocketItem.Shape.BURST);
            l.add(FireworkRocketItem.Shape.CREEPER.getId(), FireworkShapes.CREEPER, () -> (FireworkShape) (Object) FireworkRocketItem.Shape.CREEPER);
            l.add(FireworkRocketItem.Shape.LARGE_BALL.getId(), FireworkShapes.LARGE_BALL, () -> (FireworkShape) (Object) FireworkRocketItem.Shape.LARGE_BALL);
            l.add(FireworkRocketItem.Shape.SMALL_BALL.getId(), FireworkShapes.SMALL_BALL, () -> (FireworkShape) (Object) FireworkRocketItem.Shape.SMALL_BALL);
            l.add(FireworkRocketItem.Shape.STAR.getId(), FireworkShapes.STAR, () -> (FireworkShape) (Object) FireworkRocketItem.Shape.STAR);
        });
    }

    private static RegistryLoader<Trigger<?>> trigger() {
        return RegistryLoader.of(l -> {
            l.add(Triggers.BAD_OMEN, k -> (Trigger) CriteriaTriggers.BAD_OMEN);
            l.add(Triggers.BEE_NEST_DESTROYED, k -> (Trigger) CriteriaTriggers.BEE_NEST_DESTROYED);
            l.add(Triggers.BRED_ANIMALS, k -> (Trigger) CriteriaTriggers.BRED_ANIMALS);
            l.add(Triggers.BREWED_POTION, k -> (Trigger) CriteriaTriggers.BREWED_POTION);
            l.add(Triggers.CHANGED_DIMENSION, k -> (Trigger) CriteriaTriggers.CHANGED_DIMENSION);
            l.add(Triggers.CHANNELED_LIGHTNING, k -> (Trigger) CriteriaTriggers.CHANNELED_LIGHTNING);
            l.add(Triggers.CONSTRUCT_BEACON, k -> (Trigger) CriteriaTriggers.CONSTRUCT_BEACON);
            l.add(Triggers.CONSUME_ITEM, k -> (Trigger) CriteriaTriggers.CONSUME_ITEM);
            l.add(Triggers.CURED_ZOMBIE_VILLAGER, k -> (Trigger) CriteriaTriggers.CURED_ZOMBIE_VILLAGER);
            l.add(Triggers.EFFECTS_CHANGED, k -> (Trigger) CriteriaTriggers.EFFECTS_CHANGED);
            l.add(Triggers.ENCHANTED_ITEM, k -> (Trigger) CriteriaTriggers.ENCHANTED_ITEM);
            l.add(Triggers.ENTER_BLOCK, k -> (Trigger) CriteriaTriggers.ENTER_BLOCK);
            l.add(Triggers.ENTITY_HURT_PLAYER, k -> (Trigger) CriteriaTriggers.ENTITY_HURT_PLAYER);
            l.add(Triggers.ENTITY_KILLED_PLAYER, k -> (Trigger) CriteriaTriggers.ENTITY_KILLED_PLAYER);
            l.add(Triggers.FILLED_BUCKET, k -> (Trigger) CriteriaTriggers.FILLED_BUCKET);
            l.add(Triggers.FISHING_ROD_HOOKED, k -> (Trigger) CriteriaTriggers.FISHING_ROD_HOOKED);
            l.add(Triggers.GENERATE_LOOT, k -> (Trigger) CriteriaTriggers.GENERATE_LOOT);
            l.add(Triggers.HONEY_BLOCK_SIDE, k -> (Trigger) CriteriaTriggers.HONEY_BLOCK_SLIDE);
            l.add(Triggers.IMPOSSIBLE, k -> (Trigger) CriteriaTriggers.IMPOSSIBLE);
            l.add(Triggers.INVENTORY_CHANGED, k -> (Trigger) CriteriaTriggers.INVENTORY_CHANGED);
            l.add(Triggers.ITEM_DURABILITY_CHANGED, k -> (Trigger) CriteriaTriggers.ITEM_DURABILITY_CHANGED);
            l.add(Triggers.ITEM_PICKED_UP_BY_ENTITY, k -> (Trigger) CriteriaTriggers.ITEM_PICKED_UP_BY_ENTITY);
            l.add(Triggers.ITEM_USED_ON_BLOCK, k -> (Trigger) CriteriaTriggers.ITEM_USED_ON_BLOCK);
            l.add(Triggers.KILLED_BY_CROSSBOW, k -> (Trigger) CriteriaTriggers.KILLED_BY_CROSSBOW);
            l.add(Triggers.LEVITATION, k -> (Trigger) CriteriaTriggers.LEVITATION);
            l.add(Triggers.LOCATION, k -> (Trigger) CriteriaTriggers.LOCATION);
            l.add(Triggers.NETHER_TRAVEL, k -> (Trigger) CriteriaTriggers.NETHER_TRAVEL);
            l.add(Triggers.PLACED_BLOCK, k -> (Trigger) CriteriaTriggers.PLACED_BLOCK);
            l.add(Triggers.PLAYER_HURT_ENTITY, k -> (Trigger) CriteriaTriggers.PLAYER_HURT_ENTITY);
            l.add(Triggers.PLAYER_INTERACTED_WITH_ENTITY, k -> (Trigger) CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY);
            l.add(Triggers.PLAYER_KILLED_ENTITY, k -> (Trigger) CriteriaTriggers.PLAYER_KILLED_ENTITY);
            l.add(Triggers.RAID_WIN, k -> (Trigger) CriteriaTriggers.RAID_WIN);
            l.add(Triggers.RECIPE_UNLOCKED, k -> (Trigger) CriteriaTriggers.RECIPE_UNLOCKED);
            l.add(Triggers.SHOT_CROSSBOW, k -> (Trigger) CriteriaTriggers.SHOT_CROSSBOW);
            l.add(Triggers.SLEPT_IN_BED, k -> (Trigger) CriteriaTriggers.SLEPT_IN_BED);
            l.add(Triggers.SUMMONED_ENTITY, k -> (Trigger) CriteriaTriggers.SUMMONED_ENTITY);
            l.add(Triggers.TAME_ANIMAL, k -> (Trigger) CriteriaTriggers.TAME_ANIMAL);
            l.add(Triggers.TARGET_BLOCK_HIT, k -> (Trigger) CriteriaTriggers.TARGET_BLOCK_HIT);
            l.add(Triggers.TICK, k -> (Trigger) CriteriaTriggers.TICK);
            l.add(Triggers.USED_ENDER_EYE, k -> (Trigger) CriteriaTriggers.USED_ENDER_EYE);
            l.add(Triggers.USED_TOTEM, k -> (Trigger) CriteriaTriggers.USED_TOTEM);
            l.add(Triggers.VILLAGER_TRADE, k -> (Trigger) CriteriaTriggers.TRADE);
            final DefaultedRegistryReference<Trigger<?>> dummyKey =
                    RegistryKey.of(RegistryTypes.TRIGGER, ResourceKey.sponge("dummy")).asDefaultedReference(() -> Sponge.game().registries());
            l.add(dummyKey, k -> (Trigger) (Object) SpongeDummyTrigger.DUMMY_TRIGGER);
            final DefaultedRegistryReference<Trigger<?>> scoreKey =
                    RegistryKey.of(RegistryTypes.TRIGGER, ResourceKey.sponge("score")).asDefaultedReference(() -> Sponge.game().registries());
            l.add(scoreKey, k -> (Trigger) (Object) SpongeScoreTrigger.SCORE_TRIGGER);
        });
    }

    // The following methods are named for clarity above.

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> automaticName(final RegistryType<A> type, final I[] values) {
        return this.naming(type, values, value -> value.name().toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> knownName(final RegistryType<A> type,final I[] values, final Function<I, String> name) {
        return this.naming(type, values, name);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I> Registry<A> knownName(final RegistryType<A> type, final Collection<I> values, final Function<I, String> name) {
        final Map<I, String> map = new HashMap<>();
        for (final I value : values) {
            map.put(value, name.apply(value));
        }
        return this.naming(type, values.size(), map);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> manualName(final RegistryType<A> type, final I[] values, final Consumer<Manual<A, I>> byName) {
        final Map<I, String> map = new HashMap<>(values.length);
        byName.accept(map::put);
        return this.naming(type, values, map);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I> Registry<A> manualName(final RegistryType<A> type, final int values, final Consumer<Manual<A, I>> byName) {
        final Map<I, String> map = new HashMap<>(values);
        byName.accept(map::put);
        return this.naming(type, values, map);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> naming(final RegistryType<A> type, final I[] values, final Function<I, String> name) {
        final Map<I, String> map = new HashMap<>();
        for (final I value : values) {
            map.put(value, name.apply(value));
        }
        return this.naming(type, values, map);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> naming(final RegistryType<A> type, final I[] values, final Map<I, String> byName) {
        return this.naming(type, values.length, byName);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I> Registry<A> naming(final RegistryType<A> type, final int values, final Map<I, String> byName) {
        if (values != byName.size()) {
            throw new IllegalStateException(type.location() + " in " + type.root() + " is has value mismatch: " + values + " / " + byName.size());
        }
        return this.holder.createRegistry(type, () -> {
            final Map<ResourceKey, A> map = new HashMap<>();
            for (final Map.Entry<I, String> value : byName.entrySet()) {
                map.put(ResourceKey.sponge(value.getValue()), (A) value.getKey());
            }
            return map;
        }, false);
    }

    @SuppressWarnings("unused")
    private interface Manual<A, I> {
        void put(final I value, final String key);
    }
}
