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
package org.spongepowered.common.util;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetSerializer implements TypeSerializer<Set<?>> {

    @Override
    public Set<?> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return new HashSet<>(value.getList(getInnerToken(type)));
    }

    @Override
    public void serialize(TypeToken<?> type, Set<?> obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(getListTokenFromSet(type), new ArrayList<>(obj));
    }

    private TypeToken<?> getInnerToken(TypeToken<?> type) {
        return type.resolveType(Set.class.getTypeParameters()[0]);
    }

    @SuppressWarnings("unchecked")
    private <E> TypeToken<List<E>> getListTokenFromSet(TypeToken<?> type) {
        // Get the inner type out of the type token
        TypeToken<?> innerType = getInnerToken(type);

        // Put it into the new list token
        return new TypeToken<List<E>>() { private static final long serialVersionUID = 1L; }.where(new TypeParameter<E>() {}, (TypeToken<E>)innerType);
    }
}