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
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class CriteriaTriggersSupplier {

    private CriteriaTriggersSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(Trigger.class, "impossible", () -> (Trigger) CriteriaTriggers.IMPOSSIBLE)
            .registerSupplier(Trigger.class, "player_killed_entity", () -> (Trigger) CriteriaTriggers.PLAYER_KILLED_ENTITY)
            .registerSupplier(Trigger.class, "entity_killed_player", () -> (Trigger) CriteriaTriggers.ENTITY_KILLED_PLAYER)
            .registerSupplier(Trigger.class, "enter_block", () -> (Trigger) CriteriaTriggers.ENTER_BLOCK)
            .registerSupplier(Trigger.class, "inventory_changed", () -> (Trigger) CriteriaTriggers.INVENTORY_CHANGED)
            .registerSupplier(Trigger.class, "recipe_unlocked", () -> (Trigger) CriteriaTriggers.RECIPE_UNLOCKED)
            .registerSupplier(Trigger.class, "player_hurt_entity", () -> (Trigger) CriteriaTriggers.PLAYER_HURT_ENTITY)
            .registerSupplier(Trigger.class, "entity_hurt_player", () -> (Trigger) CriteriaTriggers.ENTITY_HURT_PLAYER)
            .registerSupplier(Trigger.class, "enchanted_item", () -> (Trigger) CriteriaTriggers.ENCHANTED_ITEM)
            .registerSupplier(Trigger.class, "filled_bucket", () -> (Trigger) CriteriaTriggers.FILLED_BUCKET)
            .registerSupplier(Trigger.class, "brewed_potion", () -> (Trigger) CriteriaTriggers.BREWED_POTION)
            .registerSupplier(Trigger.class, "construct_beacon", () -> (Trigger) CriteriaTriggers.CONSTRUCT_BEACON)
            .registerSupplier(Trigger.class, "used_ender_eye", () -> (Trigger) CriteriaTriggers.USED_ENDER_EYE)
            .registerSupplier(Trigger.class, "summoned_entity", () -> (Trigger) CriteriaTriggers.SUMMONED_ENTITY)
            .registerSupplier(Trigger.class, "bred_animals", () -> (Trigger) CriteriaTriggers.BRED_ANIMALS)
            .registerSupplier(Trigger.class, "location", () -> (Trigger) CriteriaTriggers.LOCATION)
            .registerSupplier(Trigger.class, "slept_in_bed", () -> (Trigger) CriteriaTriggers.SLEPT_IN_BED)
            .registerSupplier(Trigger.class, "cured_zombie_villager", () -> (Trigger) CriteriaTriggers.CURED_ZOMBIE_VILLAGER)
            .registerSupplier(Trigger.class, "villager_trade", () -> (Trigger) CriteriaTriggers.VILLAGER_TRADE)
            .registerSupplier(Trigger.class, "item_durability_changed", () -> (Trigger) CriteriaTriggers.ITEM_DURABILITY_CHANGED)
            .registerSupplier(Trigger.class, "levitation", () -> (Trigger) CriteriaTriggers.LEVITATION)
            .registerSupplier(Trigger.class, "changed_dimension", () -> (Trigger) CriteriaTriggers.CHANGED_DIMENSION)
            .registerSupplier(Trigger.class, "tick", () -> (Trigger) CriteriaTriggers.TICK)
            .registerSupplier(Trigger.class, "tame_animal", () -> (Trigger) CriteriaTriggers.TAME_ANIMAL)
            .registerSupplier(Trigger.class, "placed_block", () -> (Trigger) CriteriaTriggers.PLACED_BLOCK)
            .registerSupplier(Trigger.class, "consume_item", () -> (Trigger) CriteriaTriggers.CONSUME_ITEM)
            .registerSupplier(Trigger.class, "effects_changed", () -> (Trigger) CriteriaTriggers.EFFECTS_CHANGED)
            .registerSupplier(Trigger.class, "used_totem", () -> (Trigger) CriteriaTriggers.USED_TOTEM)
            .registerSupplier(Trigger.class, "nether_travel", () -> (Trigger) CriteriaTriggers.NETHER_TRAVEL)
            .registerSupplier(Trigger.class, "fishing_rod_hooked", () -> (Trigger) CriteriaTriggers.FISHING_ROD_HOOKED)
            .registerSupplier(Trigger.class, "channeled_lightning", () -> (Trigger) CriteriaTriggers.CHANNELED_LIGHTNING)
            .registerSupplier(Trigger.class, "shot_crossbow", () -> (Trigger) CriteriaTriggers.SHOT_CROSSBOW)
            .registerSupplier(Trigger.class, "killed_by_crossbow", () -> (Trigger) CriteriaTriggers.KILLED_BY_CROSSBOW)
            .registerSupplier(Trigger.class, "hero_of_the_village", () -> (Trigger) CriteriaTriggers.HERO_OF_THE_VILLAGE)
            .registerSupplier(Trigger.class, "voluntary_exile", () -> (Trigger) CriteriaTriggers.VOLUNTARY_EXILE)
        ;
    }
}
