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

import com.google.common.collect.ImmutableMap;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import org.spongepowered.api.state.State;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Map;

@Mixin(IStateHolder.class)
public interface IStateHolderMixin_API<S extends State<S>> extends State<S> {

    @Shadow <T extends Comparable<T>> T shadow$get(IProperty<T> property);
    @Shadow ImmutableMap<IProperty<?>, Comparable<?>> shadow$getValues();

    @Override
    default Collection<StateProperty<?>> getStateProperties() {
        return (Collection) this.shadow$getValues().keySet();
    }

    @Override
    default Collection<?> getStatePropertyValues() {
        return this.shadow$getValues().values();
    }

    @Override
    default Map<StateProperty<?>, ?> getStatePropertyMap() {
        return (Map) this.shadow$getValues();
    }
}
