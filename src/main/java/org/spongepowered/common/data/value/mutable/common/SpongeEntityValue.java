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
package org.spongepowered.common.data.value.mutable.common;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.value.AbstractBaseValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.lang.ref.WeakReference;

public class SpongeEntityValue extends AbstractBaseValue<Entity> implements Value<Entity> {

    private WeakReference<Entity> weakReference = new WeakReference<Entity>(null);

    public SpongeEntityValue(Key<? extends BaseValue<Entity>> key, Entity actualValue) {
        super(key, actualValue);
        if (actualValue != null) {
            this.weakReference = new WeakReference<Entity>(actualValue);
        } else {
            this.weakReference.clear();
        }
    }

    @Override
    public Entity get() {
        return this.weakReference.get();
    }

    @Override
    public boolean exists() {
        return this.weakReference.get() != null;
    }

    @Override
    public Optional<Entity> getDirect() {
        return fromNullable(this.weakReference.get());
    }

    @Override
    public Value<Entity> set(Entity value) {
        if (value != null) {
            this.weakReference = new WeakReference<Entity>(value);
        } else {
            this.weakReference.clear();
        }
        return this;
    }

    @Override
    public Value<Entity> transform(Function<Entity, Entity> function) {
        final Entity optional = checkNotNull(checkNotNull(function).apply(this.weakReference.get()));
        if (optional != null) {
            this.weakReference = new WeakReference<Entity>(optional);
        } else {
            this.weakReference.clear();
        }
        return this;
    }

    @Override
    public ImmutableValue<Entity> asImmutable() {
        return new ImmutableSpongeValue<Entity>(getKey(), this.weakReference.get());
    }

}
