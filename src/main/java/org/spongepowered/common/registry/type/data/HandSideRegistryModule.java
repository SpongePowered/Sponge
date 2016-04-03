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
package org.spongepowered.common.registry.type.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.api.data.type.HandSide;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class HandSideRegistryModule implements CatalogRegistryModule<HandSide> {


    public static HandSideRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(HandSide.class)
    public final Map<String, HandSide> handSideMap = Maps.newHashMap();

    @Override
    public void registerDefaults() {

        this.handSideMap.put("left", (HandSide) (Object) EnumHandSide.LEFT);
        this.handSideMap.put("right", (HandSide) (Object) EnumHandSide.RIGHT);
    }

    @Override
    public Optional<HandSide> getById(String id) {
        return Optional.ofNullable(this.handSideMap.get(checkNotNull(id, "id").toLowerCase()));
    }

    @Override
    public Collection<HandSide> getAll() {
        return ImmutableSet.copyOf(this.handSideMap.values());
    }

    HandSideRegistryModule() {
    }

    static final class Holder {
        static final HandSideRegistryModule INSTANCE = new HandSideRegistryModule();
    }
}
