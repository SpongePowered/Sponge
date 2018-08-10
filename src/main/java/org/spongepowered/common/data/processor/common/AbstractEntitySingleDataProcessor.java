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
package org.spongepowered.common.data.processor.common;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;

/**
 * A superclass for targeting specific {@link Entity} hierarchical types,
 * since we can
 * @param <E>
 * @param <ValueType>
 * @param <ValueClassType>
 * @param <Manipulator>
 * @param <Immutable>
 */
public abstract class AbstractEntitySingleDataProcessor<E extends Entity, ValueType, ValueClassType extends BaseValue<ValueType>,
    Manipulator extends DataManipulator<Manipulator, Immutable>, Immutable extends ImmutableDataManipulator<Immutable, Manipulator>>
    extends AbstractSingleDataSingleTargetProcessor<E, ValueType, ValueClassType, Manipulator, Immutable> {

    public AbstractEntitySingleDataProcessor(Class<E> entityClass, Key<ValueClassType> key) {
        super(key, entityClass);
    }
    
}
