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
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.data.type.SpongeHeldEquipmentType;
import org.spongepowered.common.data.type.SpongeWornEquipmentType;

import java.util.stream.Stream;

public final class EquipmentTypeStreamGenerator {
    public static Stream<EquipmentType> stream() {
        return Stream.of(
            new SpongeEquipmentType(ResourceKey.minecraft("any"), EquipmentSlotType.values()),
            new SpongeEquipmentType(ResourceKey.minecraft("equipped"), EquipmentSlotType.values()),
            new SpongeHeldEquipmentType(ResourceKey.minecraft("held"), EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND),
            new SpongeHeldEquipmentType(ResourceKey.minecraft("main_hand"), EquipmentSlotType.MAINHAND),
            new SpongeHeldEquipmentType(ResourceKey.minecraft("off_hand"), EquipmentSlotType.OFFHAND),
            new SpongeWornEquipmentType(ResourceKey.minecraft("worn"), EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET),
            new SpongeWornEquipmentType(ResourceKey.minecraft("boots"), EquipmentSlotType.FEET),
            new SpongeWornEquipmentType(ResourceKey.minecraft("leggings"), EquipmentSlotType.LEGS),
            new SpongeWornEquipmentType(ResourceKey.minecraft("chestplate"), EquipmentSlotType.CHEST),
            new SpongeWornEquipmentType(ResourceKey.minecraft("headwear"), EquipmentSlotType.HEAD)
        );
    }
}
