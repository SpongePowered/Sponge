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

import net.minecraft.inventory.EquipmentSlotType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.common.bridge.ResourceKeyBridge;

import java.util.stream.Stream;

public final class EquipmentTypeStreamGenerator {

    private EquipmentTypeStreamGenerator() {
    }

    public static Stream<EquipmentType> stream() {
        return Stream.of(
            EquipmentTypeStreamGenerator.newEquipmentType(EquipmentSlotType.HEAD, ResourceKey.minecraft("head")),
            EquipmentTypeStreamGenerator.newEquipmentType(EquipmentSlotType.CHEST, ResourceKey.minecraft("chest")),
            EquipmentTypeStreamGenerator.newEquipmentType(EquipmentSlotType.LEGS, ResourceKey.minecraft("legs")),
            EquipmentTypeStreamGenerator.newEquipmentType(EquipmentSlotType.FEET, ResourceKey.minecraft("feet")),
            EquipmentTypeStreamGenerator.newEquipmentType(EquipmentSlotType.MAINHAND, ResourceKey.minecraft("main_hand")),
            EquipmentTypeStreamGenerator.newEquipmentType(EquipmentSlotType.OFFHAND, ResourceKey.minecraft("off_hand"))
        );
    }

    private static EquipmentType newEquipmentType(final EquipmentSlotType type, final ResourceKey key) {
        ((ResourceKeyBridge) (Object) type).bridge$setKey(key);
        return (EquipmentType) (Object) type;
    }
}
