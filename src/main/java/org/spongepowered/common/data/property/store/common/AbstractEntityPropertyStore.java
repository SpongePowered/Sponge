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
package org.spongepowered.common.data.property.store.common;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.store.DoublePropertyStore;
import org.spongepowered.api.data.property.store.IntPropertyStore;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public abstract class AbstractEntityPropertyStore<V> extends AbstractSpongePropertyStore<V> {

    public static abstract class Generic<V> extends AbstractEntityPropertyStore<V> {

        protected abstract Optional<V> getForEntity(Entity entity);

        @Override
        public Optional<V> getFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof Entity) {
                return getForEntity((Entity) propertyHolder);
            }
            return Optional.empty();
        }
    }

    public static abstract class Int extends AbstractEntityPropertyStore<Integer> implements IntPropertyStore {

        protected abstract OptionalInt getIntForEntity(Entity entity);

        @Override
        public OptionalInt getIntFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof Entity) {
                return getIntForEntity((Entity) propertyHolder);
            }
            return OptionalInt.empty();
        }
    }

    public static abstract class Dbl extends AbstractEntityPropertyStore<Double> implements DoublePropertyStore {

        protected abstract OptionalDouble getDoubleForEntity(Entity entity);

        @Override
        public OptionalDouble getDoubleFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof Entity) {
                return getDoubleForEntity((Entity) propertyHolder);
            }
            return OptionalDouble.empty();
        }
    }
}
