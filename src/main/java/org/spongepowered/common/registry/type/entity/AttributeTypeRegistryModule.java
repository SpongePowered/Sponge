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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.api.entity.attribute.type.AttributeTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AttributeTypeRegistryModule implements CatalogRegistryModule<AttributeType> {

    private final Map<String, AttributeType> attributes = new HashMap<>();

    @Override
    public void registerDefaults() {
        this.map(AttributeTypes.Entity.class, ImmutableMap.<String, AttributeType>builder()
                .put("armor", (AttributeType) SharedMonsterAttributes.ARMOR)
                .put("armor_toughness", (AttributeType) SharedMonsterAttributes.ARMOR_TOUGHNESS)
                .put("attack_damage", (AttributeType) SharedMonsterAttributes.ATTACK_DAMAGE)
                .put("attack_speed", (AttributeType) SharedMonsterAttributes.ATTACK_SPEED)
                .put("follow_range", (AttributeType) SharedMonsterAttributes.FOLLOW_RANGE)
                .put("knockback_resistance", (AttributeType) SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                .put("luck", (AttributeType) SharedMonsterAttributes.LUCK)
                .put("max_health", (AttributeType) SharedMonsterAttributes.MAX_HEALTH)
                .put("movement_speed", (AttributeType) SharedMonsterAttributes.MOVEMENT_SPEED)
                .build());
        this.map(AttributeTypes.Entity.Horse.class, ImmutableMap.of(
                "jump_strength", (AttributeType) AbstractHorse.JUMP_STRENGTH
        ));
        this.map(AttributeTypes.Entity.Zombie.class, ImmutableMap.of(
                "spawn_reinforcements_chance", (AttributeType) EntityZombie.SPAWN_REINFORCEMENTS_CHANCE
        ));
    }

    private void map(Class<?> apiClass, Map<String, AttributeType> attributes) {
        this.attributes.putAll(attributes);
        RegistryHelper.mapFields(apiClass, attributes);
    }

    @Override
    public Optional<AttributeType> getById(String id) {
        return Optional.ofNullable(this.attributes.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<AttributeType> getAll() {
        return ImmutableSet.copyOf(this.attributes.values());
    }
}
