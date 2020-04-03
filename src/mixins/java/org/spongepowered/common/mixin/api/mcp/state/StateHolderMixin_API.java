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
package org.spongepowered.common.mixin.api.mcp.state;

import net.minecraft.state.IProperty;
import net.minecraft.state.StateHolder;
import org.spongepowered.api.state.State;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(StateHolder.class)
public abstract class StateHolderMixin_API<S extends State<S>, C> implements IStateHolderMixin_API<S> {

    @Shadow public abstract <V extends Comparable<V>> boolean shadow$has(IProperty<V> property);
    @Shadow public abstract <T extends Comparable<T>, V extends T> C shadow$with(IProperty<T> property, V value);
    @Shadow public abstract <T extends Comparable<T>> C shadow$cycle(IProperty<T> property);

    @Override
    public <T extends Comparable<T>> Optional<T> getStateProperty(StateProperty<T> stateProperty) {
        if (!this.shadow$has((IProperty) stateProperty)) {
            return Optional.empty();
        }

        return Optional.of((T) this.shadow$get((IProperty<?>) stateProperty));
    }

    @Override
    public Optional<StateProperty<?>> getStatePropertyByName(String name) {
        return this.getStateProperties().stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    @Override
    public <T extends Comparable<T>, V extends T> Optional<S> withStateProperty(StateProperty<T> stateProperty, V value) {
        if (!this.shadow$has((IProperty) stateProperty)) {
            return Optional.empty();
        }

        return Optional.of((S) this.shadow$with((IProperty) stateProperty, value));
    }

    @Override
    public <T extends Comparable<T>> Optional<S> cycleStateProperty(StateProperty<T> stateProperty) {
        if (!this.shadow$has((IProperty) stateProperty)) {
            return Optional.empty();
        }

        return Optional.of((S) this.shadow$cycle((IProperty) stateProperty));
    }

    /* TODO: This needs to be implemented within the data API
    @Override
    public <T extends Cycleable<T>> Optional<S> cycleValue(Key<? extends Value<T>> key) {
        return Optional.empty();
    }
    */
}
