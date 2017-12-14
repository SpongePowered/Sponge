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
package org.spongepowered.common.item.inventory.property;

import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.util.Coerce;
import org.spongepowered.common.data.type.SpongeEquipmentType;

import java.util.Arrays;
import java.util.stream.StreamSupport;

public final class EquipmentSlotTypeImpl extends AbstractInventoryProperty<String, EquipmentType> implements EquipmentSlotType {

    public EquipmentSlotTypeImpl(EquipmentType value, Operator operator) {
        super(value, operator);
    }

    public EquipmentSlotTypeImpl(EquipmentType value) {
        this(value, Operator.DELEGATE);
    }

    @Override
    public int compareTo(Property<?, ?> other) {
        if (other == null) {
            return 1;
        }

        EquipmentType otherValue =
                Coerce.<EquipmentType>toPseudoEnum(other.getValue(), EquipmentType.class, EquipmentTypes.class, EquipmentTypes.WORN);
        return this.getValue().getName().compareTo(otherValue.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EquipmentSlotType)) {
            return false;
        }

        EquipmentSlotType other = (EquipmentSlotType) obj;
        if (other.getKey().equals(this.getKey())) {
            if (this.getValue() instanceof SpongeEquipmentType && other.getValue() instanceof SpongeEquipmentType) {
                EntityEquipmentSlot[] thisSlots = ((SpongeEquipmentType) this.getValue()).getSlots();
                EntityEquipmentSlot[] otherSlots = ((SpongeEquipmentType) other.getValue()).getSlots();

                if (thisSlots.length == 1 && otherSlots.length == 0) {
                    // this is ANY or EQUIPPED
                    // all sub-types do match
                    return true;
                }
                if (thisSlots.length == 1 && otherSlots.length > 1) {
                    // this is a composite type like HELD/WORN
                    // matching a single of its sub-types is a match
                    return Arrays.stream(otherSlots).anyMatch(s -> s == thisSlots[0]);
                }
            }
            return other.getValue().equals(this.getValue());
        }
        return false;
    }

    public static final class BuilderImpl extends PropertyBuilderImpl<EquipmentType, EquipmentSlotType, EquipmentSlotType.Builder> implements EquipmentSlotType.Builder{

        @Override
        public EquipmentSlotType build() {
            return new EquipmentSlotTypeImpl(this.value, this.operator);
        }
    }

}
