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

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.monster.ZombieEntity;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.common.accessor.entity.monster.ZombieEntityAccessor;
import org.spongepowered.common.accessor.entity.monster.ZombiePigmanEntityAccessor;
import org.spongepowered.common.accessor.entity.passive.horse.AbstractHorseEntityAccessor;

import java.util.stream.Stream;

public final class AttributeTypeStreamGenerator {

    private AttributeTypeStreamGenerator() {
    }

    public static Stream<AttributeType> stream() {
        return Stream.of(
            (AttributeType) SharedMonsterAttributes.ARMOR,
            (AttributeType) SharedMonsterAttributes.ARMOR_TOUGHNESS,
            (AttributeType) SharedMonsterAttributes.ATTACK_DAMAGE,
            (AttributeType) SharedMonsterAttributes.ATTACK_KNOCKBACK,
            (AttributeType) SharedMonsterAttributes.ATTACK_SPEED,
            (AttributeType) SharedMonsterAttributes.FLYING_SPEED,
            (AttributeType) SharedMonsterAttributes.FOLLOW_RANGE,
            (AttributeType) SharedMonsterAttributes.KNOCKBACK_RESISTANCE,
            (AttributeType) SharedMonsterAttributes.LUCK,
            (AttributeType) SharedMonsterAttributes.MAX_HEALTH,
            (AttributeType) SharedMonsterAttributes.MOVEMENT_SPEED,
            // Entity specific attributes
            ((AttributeType) AbstractHorseEntityAccessor.accessor$getJumpStrength()),
            ((AttributeType) ZombieEntityAccessor.accessor$getSpawnReinforcementsChance())
        );
    }
}
