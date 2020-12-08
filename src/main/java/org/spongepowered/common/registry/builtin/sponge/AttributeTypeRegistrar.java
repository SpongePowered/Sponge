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

import net.minecraft.entity.ai.attributes.Attributes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

import java.util.stream.Stream;

public final class AttributeTypeRegistrar {

    private AttributeTypeRegistrar() {
    }

    public static void registerRegistry(final SpongeCatalogRegistry registry) {
        registry.generateRegistry(AttributeType.class, ResourceKey.minecraft("attribute_type"), Stream.empty(), false, false);
    }
    
    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
            .registerCatalogAndSupplier(AttributeType.class, "generic.armor", () -> (AttributeType) Attributes.ARMOR)
            .registerCatalogAndSupplier(AttributeType.class, "generic.armor_toughness", () -> (AttributeType) Attributes.ARMOR_TOUGHNESS)
            .registerCatalogAndSupplier(AttributeType.class, "generic.attack_damage", () -> (AttributeType) Attributes.ATTACK_DAMAGE)
            .registerCatalogAndSupplier(AttributeType.class, "generic.attack_knockback", () -> (AttributeType) Attributes.ATTACK_KNOCKBACK)
            .registerCatalogAndSupplier(AttributeType.class, "generic.attack_speed", () -> (AttributeType) Attributes.ATTACK_SPEED)
            .registerCatalogAndSupplier(AttributeType.class, "generic.flying_speed", () -> (AttributeType) Attributes.FLYING_SPEED)
            .registerCatalogAndSupplier(AttributeType.class, "generic.follow_range", () -> (AttributeType) Attributes.FOLLOW_RANGE)
            .registerCatalogAndSupplier(AttributeType.class, "generic.knockback_resistance", () -> (AttributeType) Attributes.KNOCKBACK_RESISTANCE)
            .registerCatalogAndSupplier(AttributeType.class, "generic.luck", () -> (AttributeType) Attributes.LUCK)
            .registerCatalogAndSupplier(AttributeType.class, "generic.max_health", () -> (AttributeType) Attributes.MAX_HEALTH)
            .registerCatalogAndSupplier(AttributeType.class, "generic.movement_speed", () -> (AttributeType) Attributes.MOVEMENT_SPEED)
            .registerCatalogAndSupplier(AttributeType.class, "horse.jump_strength", () -> (AttributeType) Attributes.JUMP_STRENGTH)
            .registerCatalogAndSupplier(AttributeType.class, "zombie.spawn_reinforcements", () -> (AttributeType) Attributes.SPAWN_REINFORCEMENTS_CHANCE)

        ;
    }
}
