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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.state.State;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

@Mixin(StateHolder.class)
public abstract class StateHolderMixin_API<S extends State<S>, C> implements State<S>, SpongeImmutableDataHolder<S> {

    // @formatter:off
    @Shadow public abstract <V extends Comparable<V>> boolean shadow$hasProperty(Property<V> property);
    @Shadow public abstract <T extends Comparable<T>> T shadow$getValue(Property<T> property);
    @Shadow public abstract <T extends Comparable<T>, V extends T> C shadow$setValue(Property<T> property, V value);
    @Shadow public abstract <T extends Comparable<T>> C shadow$cycle(Property<T> property);
    @Shadow public abstract ImmutableMap<Property<?>, Comparable<?>> shadow$getValues();
    // @formatter:on

    @Override
    public <T extends Comparable<T>> Optional<T> stateProperty(StateProperty<T> stateProperty) {
        if (!this.shadow$hasProperty((Property) stateProperty)) {
            return Optional.empty();
        }

        return Optional.of((T) this.shadow$getValue((Property<?>) stateProperty));
    }

    @Override
    public Optional<StateProperty<?>> findStateProperty(String name) {
        return this.stateProperties().stream().filter(p -> p.name().equals(name)).findFirst();
    }

    @Override
    public <T extends Comparable<T>, V extends T> Optional<S> withStateProperty(StateProperty<T> stateProperty, V value) {
        if (!this.shadow$hasProperty((Property) stateProperty)) {
            return Optional.empty();
        }

        return Optional.of((S) this.shadow$setValue((Property) stateProperty, value));
    }

    @Override
    public <T extends Comparable<T>> Optional<S> cycleStateProperty(StateProperty<T> stateProperty) {
        if (!this.shadow$hasProperty((Property) stateProperty)) {
            return Optional.empty();
        }

        return Optional.of((S) this.shadow$cycle((Property) stateProperty));
    }

    @Override
    public <T extends Cycleable<T>> Optional<S> cycleValue(Key<? extends Value<T>> key) {
        Optional<T> optionalValue = this.get(key);
        if (optionalValue.isPresent()) {
            if (optionalValue.get() instanceof Cycleable) {
                T next = optionalValue.get().cycleNext();
                return this.with(key, next);
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<StateProperty<?>> stateProperties() {
        return (Collection) this.shadow$getValues().keySet();
    }

    @Override
    public Collection<?> statePropertyValues() {
        return this.shadow$getValues().values();
    }

    @Override
    public Map<StateProperty<?>, ?> statePropertyMap() {
        return (Map) this.shadow$getValues();
    }
}
