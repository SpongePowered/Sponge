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
package org.spongepowered.common.data.generator;

import org.objectweb.asm.Type;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;

import javax.annotation.Nullable;

class KeyEntry<V extends BaseValue<E>, E> {

    final Key<V> key;
    final E defaultValue;

    String keyFieldName;
    String keyFieldDescriptor;
    @Nullable public String keyFieldSignature; // With generics, if present

    Class<?> valueClass;
    Type valueType;
    String valueTypeName;
    String valueFieldName;
    String valueFieldDescriptor;
    @Nullable String valueFieldSignature; // With generics, if present
    // The boxed value descriptor, can be equal to valueFieldDescriptor
    // if there isn't a primitive variant
    Class<?> boxedValueClass;
    String boxedValueDescriptor;
    @Nullable String boxedValueFieldSignature; // With generics, if present

    // Descriptor and signatures are the same as the actual value field
    String defaultValueFieldName;

    public KeyEntry(Key<V> key, E defaultValue) {
        this.defaultValue = defaultValue;
        this.key = key;
    }
}
