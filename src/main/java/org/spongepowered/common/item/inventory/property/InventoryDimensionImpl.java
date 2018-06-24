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

import com.flowpowered.math.vector.Vector2i;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.util.Coerce;

public final class InventoryDimensionImpl extends AbstractInventoryProperty<String, Vector2i> implements InventoryDimension {

    public InventoryDimensionImpl(Vector2i value, Operator operator) {
        super(value, operator);
    }

    public InventoryDimensionImpl(Vector2i value) {
        this(value, Operator.DELEGATE);
    }

    public InventoryDimensionImpl(int xValue, int yValue) {
        this(new Vector2i(xValue, yValue), Operator.DELEGATE);
    }

    @Override
    public int compareTo(Property<?, ?> other) {
        if (other == null) {
            return 1;
        }

        return this.getValue().compareTo(Coerce.toVector2i(other.getValue()));
    }

    public static final class BuilderImpl extends PropertyBuilderImpl<Vector2i, InventoryDimension, InventoryDimension.Builder> implements InventoryDimension.Builder{

        @Override
        public InventoryDimension build() {
            return new InventoryDimensionImpl(this.value, this.operator);
        }
    }

}
