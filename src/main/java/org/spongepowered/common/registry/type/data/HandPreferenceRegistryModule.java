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
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandPreferences;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.type.data.HandPreferenceRegistryModule.Holder;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class HandPreferenceRegistryModule implements CatalogRegistryModule<HandPreference> {

    public static HandPreferenceRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(HandPreferences.class)
    public final Map<String, HandPreference> handSideMap = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.handSideMap.put("left", (HandPreference) (Object) EnumHandSide.LEFT);
        this.handSideMap.put("right", (HandPreference) (Object) EnumHandSide.RIGHT);
    }

    @Override
    public Optional<HandPreference> getById(String id) {
        return Optional.ofNullable(this.handSideMap.get(checkNotNull(id, "id").toLowerCase()));
    }

    @Override
    public Collection<HandPreference> getAll() {
        return ImmutableSet.copyOf(this.handSideMap.values());
    }

    HandPreferenceRegistryModule() {
    }

    static final class Holder {
        static final HandPreferenceRegistryModule INSTANCE = new HandPreferenceRegistryModule();
    }
}
