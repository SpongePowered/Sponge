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

import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.common.accessor.entity.monster.ZombieEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.horse.AbstractHorseEntityAccessor;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

import java.util.stream.Stream;

public final class IAttributeTypeRegistrar {

    private IAttributeTypeRegistrar() {
    }

    public static void registerRegistry(final SpongeCatalogRegistry registry) {
        registry.generateRegistry(AttributeType.class, ResourceKey.minecraft("attribute_type"), Stream.empty(), false, false);
    }
    
    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
            .registerCatalogAndSupplier(AttributeType.class, "generic_armor", () -> (AttributeType) SharedMonsterAttributes.ARMOR)
            .registerCatalogAndSupplier(AttributeType.class, "generic_armor_toughness", () -> (AttributeType) SharedMonsterAttributes.ARMOR_TOUGHNESS)
            .registerCatalogAndSupplier(AttributeType.class, "generic_attack_damage", () -> (AttributeType) SharedMonsterAttributes.ATTACK_DAMAGE)
            .registerCatalogAndSupplier(AttributeType.class, "generic_attack_knockback", () -> (AttributeType) SharedMonsterAttributes.ATTACK_KNOCKBACK)
            .registerCatalogAndSupplier(AttributeType.class, "generic_attack_speed", () -> (AttributeType) SharedMonsterAttributes.ATTACK_SPEED)
            .registerCatalogAndSupplier(AttributeType.class, "generic_flying_speed", () -> (AttributeType) SharedMonsterAttributes.FLYING_SPEED)
            .registerCatalogAndSupplier(AttributeType.class, "generic_follow_range", () -> (AttributeType) SharedMonsterAttributes.FOLLOW_RANGE)
            .registerCatalogAndSupplier(AttributeType.class, "generic_knockback_resistance", () -> (AttributeType) SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
            .registerCatalogAndSupplier(AttributeType.class, "generic_luck", () -> (AttributeType) SharedMonsterAttributes.LUCK)
            .registerCatalogAndSupplier(AttributeType.class, "generic_max_health", () -> (AttributeType) SharedMonsterAttributes.MAX_HEALTH)
            .registerCatalogAndSupplier(AttributeType.class, "generic_movement_speed", () -> (AttributeType) SharedMonsterAttributes.MOVEMENT_SPEED)
            .registerCatalogAndSupplier(AttributeType.class, "horse_jump_strength", () -> (AttributeType) AbstractHorseEntityAccessor.accessor$getJumpStrength())
            .registerCatalogAndSupplier(AttributeType.class, "zombie_spawn_reinforcements", () -> (AttributeType) ZombieEntityAccessor.accessor$getSpawnReinforcementsChance())
        ;
    }
}
