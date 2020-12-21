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

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeColor;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemTier;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.state.properties.PistonType;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.raid.Raid;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.advancement.criteria.trigger.Triggers;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.common.accessor.advancements.CriteriaTriggersAccessor;
import org.spongepowered.common.accessor.entity.passive.MooshroomEntity_TypeAccessor;
import org.spongepowered.common.accessor.item.ArmorMaterialAccessor;
import org.spongepowered.common.advancement.criterion.SpongeDummyTrigger;
import org.spongepowered.common.advancement.criterion.SpongeScoreTrigger;

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
        this.holder.createRegistry(RegistryTypes.CRITERION, VanillaRegistryLoader.criterion().values());
        this.manualName(RegistryTypes.DRAGON_PHASE_TYPE, PhaseType.getCount(), map -> {
            map.put(PhaseType.HOLDING_PATTERN, "holding_pattern");
            map.put(PhaseType.STRAFE_PLAYER, "strafe_player");
            map.put(PhaseType.LANDING_APPROACH, "landing_approach");
            map.put(PhaseType.LANDING, "landing");
            map.put(PhaseType.TAKEOFF, "takeoff");
            map.put(PhaseType.SITTING_FLAMING, "sitting_flaming");
            map.put(PhaseType.SITTING_SCANNING, "sitting_scanning");
            map.put(PhaseType.SITTING_ATTACKING, "sitting_attacking");
            map.put(PhaseType.CHARGING_PLAYER, "charging_player");
            map.put(PhaseType.DYING, "dying");
            map.put(PhaseType.HOVERING, "hover");
        });
        this.holder.createRegistry(RegistryTypes.FIREWORK_SHAPE, VanillaRegistryLoader.fireworkShape().values());
        this.holder.createRegistry(RegistryTypes.TRIGGER, () -> VanillaRegistryLoader.trigger().values(), true,
                (k, trigger) -> CriteriaTriggersAccessor.invoker$register((ICriterionTrigger<?>) trigger));
    }

    private void loadEnumRegistries() {
        this.knownName(RegistryTypes.ARMOR_MATERIAL, ArmorMaterial.values(), am -> ((ArmorMaterialAccessor) (Object) am).accessor$name());
        this.knownName(RegistryTypes.ATTACHMENT_SURFACE, AttachFace.values(), AttachFace::getSerializedName);
        this.manualName(RegistryTypes.ATTRIBUTE_OPERATION, AttributeModifier.Operation.values(), map -> {
            // names come from net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier#operationFromString
            map.put(AttributeModifier.Operation.ADDITION, "addition");
            map.put(AttributeModifier.Operation.MULTIPLY_BASE, "multiply_base");
            map.put(AttributeModifier.Operation.MULTIPLY_TOTAL, "multiply_total");
        });
        this.knownName(RegistryTypes.BOAT_TYPE, BoatEntity.Type.values(), BoatEntity.Type::getName);
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
        this.manualName(RegistryTypes.EQUIPMENT_GROUP, EquipmentSlotType.Group.values(), map -> {
            map.put(EquipmentSlotType.Group.ARMOR, "worn");
            map.put(EquipmentSlotType.Group.HAND, "held");
        });
        this.manualName(RegistryTypes.EQUIPMENT_TYPE, EquipmentSlotType.values(), map -> {
            map.put(EquipmentSlotType.CHEST, "chest");
            map.put(EquipmentSlotType.FEET, "feet");
            map.put(EquipmentSlotType.HEAD, "head");
            map.put(EquipmentSlotType.LEGS, "legs");
            map.put(EquipmentSlotType.MAINHAND, "main_hand");
            map.put(EquipmentSlotType.OFFHAND, "off_hand");
        });
        this.knownName(RegistryTypes.FOX_TYPE, FoxEntity.Type.values(), FoxEntity.Type::getName);
        this.manualName(RegistryTypes.GAME_MODE, GameType.values(), map -> {
            map.put(GameType.NOT_SET, "not_set"); // getName returns "" (empty string) // TODO(kashike): 1.17
            map.put(GameType.SURVIVAL, GameType.SURVIVAL.getName());
            map.put(GameType.CREATIVE, GameType.CREATIVE.getName());
            map.put(GameType.ADVENTURE, GameType.ADVENTURE.getName());
            map.put(GameType.SPECTATOR, GameType.SPECTATOR.getName());
        });
        this.automaticName(RegistryTypes.HAND_PREFERENCE, HandSide.values());
        this.automaticName(RegistryTypes.HAND_TYPE, Hand.values());
        this.knownName(RegistryTypes.INSTRUMENT_TYPE, NoteBlockInstrument.values(), NoteBlockInstrument::getSerializedName);
        this.automaticName(RegistryTypes.ITEM_TIER, ItemTier.values());
        this.knownName(RegistryTypes.MOOSHROOM_TYPE, MooshroomEntity.Type.values(), type -> ((MooshroomEntity_TypeAccessor) (Object) type).accessor$type());
        this.knownName(RegistryTypes.OBJECTIVE_DISPLAY_MODE, ScoreCriteria.RenderType.values(), ScoreCriteria.RenderType::getId);
        this.knownName(RegistryTypes.PANDA_GENE, PandaEntity.Gene.values(), PandaEntity.Gene::getName);
        this.automaticName(RegistryTypes.PHANTOM_PHASE, PhantomEntity.AttackPhase.values());
        this.automaticName(RegistryTypes.PICKUP_RULE, AbstractArrowEntity.PickupStatus.values());
        this.knownName(RegistryTypes.PISTON_TYPE, PistonType.values(), PistonType::getSerializedName);
        this.knownName(RegistryTypes.PORTION_TYPE, Half.values(), Half::getSerializedName);
        this.automaticName(RegistryTypes.RAID_STATUS, Raid.Status.values());
        this.knownName(RegistryTypes.RAIL_DIRECTION, RailShape.values(), RailShape::getSerializedName);
        this.knownName(RegistryTypes.WIRE_ATTACHMENT_TYPE, RedstoneSide.values(), RedstoneSide::getSerializedName);
        this.knownName(RegistryTypes.SLAB_PORTION, SlabType.values(), SlabType::getSerializedName);
        this.automaticName(RegistryTypes.SPELL_TYPE, SpellcastingIllagerEntity.SpellType.values());
        this.knownName(RegistryTypes.STAIR_SHAPE, StairsShape.values(), StairsShape::getSerializedName);
        this.knownName(RegistryTypes.STRUCTURE_MODE, StructureMode.values(), StructureMode::getSerializedName);
        this.manualName(RegistryTypes.VISIBILITY, Team.Visible.values(), map -> {
            map.put(Team.Visible.ALWAYS, "always");
            map.put(Team.Visible.NEVER, "never");
            map.put(Team.Visible.HIDE_FOR_OTHER_TEAMS, "hide_for_other_teams");
            map.put(Team.Visible.HIDE_FOR_OWN_TEAM, "hide_for_own_team");
        });
        this.knownName(RegistryTypes.ADVANCEMENT_TYPE, FrameType.values(), FrameType::getName);
    }

    private static RegistryLoader<Criterion> criterion() {
        return RegistryLoader.of(l -> {
            l.add(Criteria.AIR, k -> (Criterion) ScoreCriteria.AIR);
            l.add(Criteria.ARMOR, k -> (Criterion) ScoreCriteria.ARMOR);
            l.add(Criteria.DEATH_COUNT, k -> (Criterion) ScoreCriteria.DEATH_COUNT);
            l.add(Criteria.DUMMY, k -> (Criterion) ScoreCriteria.DUMMY);
            l.add(Criteria.EXPERIENCE, k -> (Criterion) ScoreCriteria.EXPERIENCE);
            l.add(Criteria.FOOD, k -> (Criterion) ScoreCriteria.FOOD);
            l.add(Criteria.HEALTH, k -> (Criterion) ScoreCriteria.HEALTH);
            l.add(Criteria.LEVEL, k -> (Criterion) ScoreCriteria.LEVEL);
            l.add(Criteria.PLAYER_KILL_COUNT, k -> (Criterion) ScoreCriteria.KILL_COUNT_PLAYERS);
            l.add(Criteria.TOTAL_KILL_COUNT, k -> (Criterion) ScoreCriteria.KILL_COUNT_ALL);
            l.add(Criteria.TRIGGER, k -> (Criterion) ScoreCriteria.TRIGGER);
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
                    RegistryKey.of(RegistryTypes.TRIGGER, ResourceKey.sponge("dummy")).asDefaultedReference(() -> Sponge.getGame().registries());
            l.add(dummyKey, k -> (Trigger) (Object) SpongeDummyTrigger.DUMMY_TRIGGER);
            final DefaultedRegistryReference<Trigger<?>> scoreKey =
                    RegistryKey.of(RegistryTypes.TRIGGER, ResourceKey.sponge("score")).asDefaultedReference(() -> Sponge.getGame().registries());
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
