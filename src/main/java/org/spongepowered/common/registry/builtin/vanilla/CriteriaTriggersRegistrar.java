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
package org.spongepowered.common.registry.builtin.vanilla;

import net.minecraft.advancements.CriteriaTriggers;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

import java.util.stream.Stream;

public final class CriteriaTriggersRegistrar {

    private CriteriaTriggersRegistrar() {
    }

    // Oh Vanilla, you're fun...

    public static void registerRegistry(final SpongeCatalogRegistry registry) {
        registry.generateRegistry(Trigger.class, ResourceKey.minecraft("trigger"), Stream.empty(), false, false);
    }

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
                .registerCatalogAndSupplier(Trigger.class, "impossible", () -> (Trigger) CriteriaTriggers.IMPOSSIBLE)
                .registerCatalogAndSupplier(Trigger.class, "player_killed_entity", () -> (Trigger) CriteriaTriggers.PLAYER_KILLED_ENTITY)
                .registerCatalogAndSupplier(Trigger.class, "entity_killed_player", () -> (Trigger) CriteriaTriggers.ENTITY_KILLED_PLAYER)
                .registerCatalogAndSupplier(Trigger.class, "enter_block", () -> (Trigger) CriteriaTriggers.ENTER_BLOCK)
                .registerCatalogAndSupplier(Trigger.class, "inventory_changed", () -> (Trigger) CriteriaTriggers.INVENTORY_CHANGED)
                .registerCatalogAndSupplier(Trigger.class, "recipe_unlocked", () -> (Trigger) CriteriaTriggers.RECIPE_UNLOCKED)
                .registerCatalogAndSupplier(Trigger.class, "player_hurt_entity", () -> (Trigger) CriteriaTriggers.PLAYER_HURT_ENTITY)
                .registerCatalogAndSupplier(Trigger.class, "entity_hurt_player", () -> (Trigger) CriteriaTriggers.ENTITY_HURT_PLAYER)
                .registerCatalogAndSupplier(Trigger.class, "enchanted_item", () -> (Trigger) CriteriaTriggers.ENCHANTED_ITEM)
                .registerCatalogAndSupplier(Trigger.class, "filled_bucket", () -> (Trigger) CriteriaTriggers.FILLED_BUCKET)
                .registerCatalogAndSupplier(Trigger.class, "brewed_potion", () -> (Trigger) CriteriaTriggers.BREWED_POTION)
                .registerCatalogAndSupplier(Trigger.class, "construct_beacon", () -> (Trigger) CriteriaTriggers.CONSTRUCT_BEACON)
                .registerCatalogAndSupplier(Trigger.class, "used_ender_eye", () -> (Trigger) CriteriaTriggers.USED_ENDER_EYE)
                .registerCatalogAndSupplier(Trigger.class, "summoned_entity", () -> (Trigger) CriteriaTriggers.SUMMONED_ENTITY)
                .registerCatalogAndSupplier(Trigger.class, "bred_animals", () -> (Trigger) CriteriaTriggers.BRED_ANIMALS)
                .registerCatalogAndSupplier(Trigger.class, "location", () -> (Trigger) CriteriaTriggers.LOCATION)
                .registerCatalogAndSupplier(Trigger.class, "slept_in_bed", () -> (Trigger) CriteriaTriggers.SLEPT_IN_BED)
                .registerCatalogAndSupplier(Trigger.class, "cured_zombie_villager", () -> (Trigger) CriteriaTriggers.CURED_ZOMBIE_VILLAGER)
                .registerCatalogAndSupplier(Trigger.class, "villager_trade", () -> (Trigger) CriteriaTriggers.TRADE)
                .registerCatalogAndSupplier(Trigger.class, "item_durability_changed", () -> (Trigger) CriteriaTriggers.ITEM_DURABILITY_CHANGED)
                .registerCatalogAndSupplier(Trigger.class, "levitation", () -> (Trigger) CriteriaTriggers.LEVITATION)
                .registerCatalogAndSupplier(Trigger.class, "changed_dimension", () -> (Trigger) CriteriaTriggers.CHANGED_DIMENSION)
                .registerCatalogAndSupplier(Trigger.class, "tick", () -> (Trigger) CriteriaTriggers.TICK)
                .registerCatalogAndSupplier(Trigger.class, "tame_animal", () -> (Trigger) CriteriaTriggers.TAME_ANIMAL)
                .registerCatalogAndSupplier(Trigger.class, "placed_block", () -> (Trigger) CriteriaTriggers.PLACED_BLOCK)
                .registerCatalogAndSupplier(Trigger.class, "consume_item", () -> (Trigger) CriteriaTriggers.CONSUME_ITEM)
                .registerCatalogAndSupplier(Trigger.class, "effects_changed", () -> (Trigger) CriteriaTriggers.EFFECTS_CHANGED)
                .registerCatalogAndSupplier(Trigger.class, "used_totem", () -> (Trigger) CriteriaTriggers.USED_TOTEM)
                .registerCatalogAndSupplier(Trigger.class, "nether_travel", () -> (Trigger) CriteriaTriggers.NETHER_TRAVEL)
                .registerCatalogAndSupplier(Trigger.class, "fishing_rod_hooked", () -> (Trigger) CriteriaTriggers.FISHING_ROD_HOOKED)
                .registerCatalogAndSupplier(Trigger.class, "channeled_lightning", () -> (Trigger) CriteriaTriggers.CHANNELED_LIGHTNING)
                .registerCatalogAndSupplier(Trigger.class, "shot_crossbow", () -> (Trigger) CriteriaTriggers.SHOT_CROSSBOW)
                .registerCatalogAndSupplier(Trigger.class, "killed_by_crossbow", () -> (Trigger) CriteriaTriggers.KILLED_BY_CROSSBOW)
                .registerCatalogAndSupplier(Trigger.class, "hero_of_the_village", () -> (Trigger) CriteriaTriggers.RAID_WIN)
                .registerCatalogAndSupplier(Trigger.class, "voluntary_exile", () -> (Trigger) CriteriaTriggers.BAD_OMEN)
                .registerCatalogAndSupplier(Trigger.class, "slide_down_block", () -> (Trigger) CriteriaTriggers.HONEY_BLOCK_SLIDE)
        ;
    }
}
