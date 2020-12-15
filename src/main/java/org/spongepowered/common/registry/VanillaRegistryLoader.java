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

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemTier;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.state.properties.PistonType;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registries;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.accessor.item.ArmorMaterialAccessor;

final class VanillaRegistryLoader {
    private final SpongeRegistryHolder registries;

    public static void load(final SpongeRegistryHolder registries) {
        final VanillaRegistryLoader loader = new VanillaRegistryLoader(registries);
        loader.load();
    }

    private VanillaRegistryLoader(final SpongeRegistryHolder registries) {
        this.registries = registries;
    }

    private void load() {
        this.knownName(Registries.ARMOR_MATERIAL, ArmorMaterial.values(), am -> ((ArmorMaterialAccessor) (Object) am).accessor$name());
        this.knownName(Registries.ATTACHMENT_SURFACE, AttachFace.values(), AttachFace::getSerializedName);
        this.manualName(Registries.ATTRIBUTE_OPERATION, AttributeModifier.Operation.values(), ImmutableMap.of(
            // names come from net.minecraft.world.level.storage.loot.functions.SetAttributesFunction.Modifier#operationFromString
            AttributeModifier.Operation.ADDITION, "addition",
            AttributeModifier.Operation.MULTIPLY_BASE, "multiply_base",
            AttributeModifier.Operation.MULTIPLY_TOTAL, "multiply_total"
        ));
        this.knownName(Registries.CHEST_ATTACHMENT_TYPE, ChestType.values(), ChestType::getSerializedName);
        this.knownName(Registries.COLLISION_RULE, Team.CollisionRule.values(), cr -> cr.name);
        this.knownName(Registries.DIFFICULTY, Difficulty.values(), Difficulty::getKey);
        this.knownName(Registries.DYE_COLOR, DyeColor.values(), DyeColor::getSerializedName);
        this.knownName(Registries.DOOR_HINGE, DoorHingeSide.values(), DoorHingeSide::getSerializedName);
        this.knownName(Registries.FOX_TYPE, FoxEntity.Type.values(), FoxEntity.Type::getName);
        this.knownName(Registries.GAME_MODE, GameType.values(), GameType::getName);
        this.automaticName(Registries.HAND_PREFERENCE, HandSide.values());
        this.automaticName(Registries.HAND_TYPE, Hand.values());
        this.knownName(Registries.INSTRUMENT_TYPE, NoteBlockInstrument.values(), NoteBlockInstrument::getSerializedName);
        this.automaticName(Registries.ITEM_TIER, ItemTier.values());
        this.knownName(Registries.OBJECTIVE_DISPLAY_MODE, ScoreCriteria.RenderType.values(), ScoreCriteria.RenderType::getId);
        this.knownName(Registries.PANDA_GENE, PandaEntity.Gene.values(), PandaEntity.Gene::getName);
        this.automaticName(Registries.PHANTOM_PHASE, PhantomEntity.AttackPhase.values());
        this.automaticName(Registries.PICKUP_RULE, AbstractArrowEntity.PickupStatus.values());
        this.knownName(Registries.PISTON_TYPE, PistonType.values(), PistonType::getSerializedName);
        this.knownName(Registries.RAIL_DIRECTION, RailShape.values(), RailShape::getSerializedName);
        this.knownName(Registries.WIRE_ATTACHMENT_TYPE, RedstoneSide.values(), RedstoneSide::getSerializedName);
        this.knownName(Registries.SLAB_PORTION, SlabType.values(), SlabType::getSerializedName);
        this.automaticName(Registries.SPELL_TYPE, SpellcastingIllagerEntity.SpellType.values());
        this.knownName(Registries.STAIR_SHAPE, StairsShape.values(), StairsShape::getSerializedName);
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
    private <A, I extends Enum<I>> Registry<A> manualName(final RegistryType<A> type, final I[] values, final Map<I, String> byName) {
        return this.naming(type, values, byName);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> naming(final RegistryType<A> type, final I[] values, final Function<I, String> name) {
        final Map<I, String> map = new HashMap<>();
        for (final I value : values) {
            map.put(value, name.apply(value));
        }
        return this.manualName(type, values, map);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <A, I extends Enum<I>> Registry<A> naming(final RegistryType<A> type, final I[] values, final Map<I, String> byName) {
        if (values.length != byName.size()) {
            throw new IllegalStateException(type.location() + " in " + type.root() + " is has value mismatch: " + values.length + " / " + byName.size());
        }
        return this.registries.createRegistry(type, () -> {
            final Map<ResourceKey, A> map = new HashMap<>();
            for (final Map.Entry<I, String> value : byName.entrySet()) {
                map.put(ResourceKey.minecraft(value.getValue()), (A) value.getKey());
            }
            return map;
        }, false);
    }
}
