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
package org.spongepowered.common.registry.type.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.managed.ChildExceptionBehavior;
import org.spongepowered.api.command.managed.ChildExceptionBehaviors;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.command.managed.childexception.RethrowChildExceptionBehavior;
import org.spongepowered.common.command.managed.childexception.StoreChildExceptionBehavior;
import org.spongepowered.common.command.managed.childexception.SuppressChildExceptionBehavior;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ChildExceptionBehaviorRegistryModule implements CatalogRegistryModule<ChildExceptionBehavior> {

    @RegisterCatalog(ChildExceptionBehaviors.class)
    private final Map<String, ChildExceptionBehavior> behaviorMap = Maps.newHashMap();

    @Override
    public Optional<ChildExceptionBehavior> getById(String id) {
        return this.behaviorMap.values().stream().filter(x -> x.getId().equalsIgnoreCase(id)).findFirst();
    }

    @Override
    public Collection<ChildExceptionBehavior> getAll() {
        return ImmutableSet.copyOf(this.behaviorMap.values());
    }

    @Override
    public void registerDefaults() {
        this.behaviorMap.put("rethrow", new RethrowChildExceptionBehavior());
        this.behaviorMap.put("store", new StoreChildExceptionBehavior());
        this.behaviorMap.put("suppress", new SuppressChildExceptionBehavior());
    }

}
