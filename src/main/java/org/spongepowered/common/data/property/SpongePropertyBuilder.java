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
package org.spongepowered.common.data.property;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongePropertyBuilder<V> extends SpongeCatalogBuilder<Property<V>, Property.Builder<V>> implements Property.Builder<V> {

    protected TypeToken<V> valueType;

    @Nullable protected Comparator<V> valueComparator;
    @Nullable protected BiPredicate<V, V> includesTester;

    @Override
    public <NV> SpongePropertyBuilder<NV> valueType(TypeToken<NV> typeToken) {
        checkNotNull(typeToken, "typeToken");
        this.valueType = (TypeToken<V>) typeToken;
        this.valueComparator = null;
        return (SpongePropertyBuilder<NV>) this;
    }

    @Override
    public SpongePropertyBuilder<V> valueComparator(Comparator<V> comparator) {
        checkNotNull(comparator, "comparator");
        this.valueComparator = comparator;
        return this;
    }

    @Override
    public SpongePropertyBuilder<V> valueIncludesTester(BiPredicate<V, V> predicate) {
        checkNotNull(predicate, "predicate");
        this.includesTester = predicate;
        return this;
    }

    @Override
    protected Property<V> build(CatalogKey key, Translation name) {
        checkState(this.valueType != null, "The value type be set");
        final Class<?> raw = this.valueType.getRawType();
        Comparator<V> valueComparator = this.valueComparator;
        if (valueComparator == null) {
            if (Comparable.class.isAssignableFrom(raw)) {
                valueComparator = (o1, o2) -> ((Comparable) o1).compareTo(o2);
            } else if (CatalogType.class.isAssignableFrom(raw)) {
                valueComparator = Comparator.comparing(o -> ((CatalogType) o).getKey());
            } else {
                // Just match hash code
                valueComparator = Comparator.comparingInt(Object::hashCode);
            }
        }
        BiPredicate<V, V> includesTester = this.includesTester;
        if (includesTester == null) {
            if (Collection.class.isAssignableFrom(raw)) {
                includesTester = (o1, o2) -> ((Collection) o1).containsAll((Collection) o2);
            } else if (EquipmentType.class.isAssignableFrom(raw)) {
                includesTester = (o1, o2) -> ((EquipmentType) o1).includes((EquipmentType) o2);
            } else {
                includesTester = (o1, o2) -> false;
            }
        }
        return new SpongeProperty<>(key, this.valueType, valueComparator, includesTester);
    }

    @Override
    public SpongePropertyBuilder<V> reset() {
        super.reset();
        this.valueType = null;
        this.valueComparator = null;
        this.includesTester = null;
        return this;
    }
}
