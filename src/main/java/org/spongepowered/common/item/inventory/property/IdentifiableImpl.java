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

import org.spongepowered.api.data.Property;
import org.spongepowered.api.item.inventory.property.Identifiable;
import org.spongepowered.api.util.Coerce;

import java.util.UUID;

public final class IdentifiableImpl extends AbstractInventoryProperty<String, UUID> implements Identifiable {

    public IdentifiableImpl(UUID value, Operator operator) {
        super(value, operator);
    }

    @Override
    public int compareTo(Property<?, ?> other) {
        if (other == null) {
            return 1;
        }

        return this.getValue().toString().compareTo(Coerce.toString(other.getValue()));
    }

    public static final class BuilderImpl extends PropertyBuilderImpl<UUID, Identifiable, Identifiable.Builder> implements Identifiable.Builder {

        @Override
        public Identifiable build() {
            return new IdentifiableImpl(this.value, this.operator);
        }
    }
}
