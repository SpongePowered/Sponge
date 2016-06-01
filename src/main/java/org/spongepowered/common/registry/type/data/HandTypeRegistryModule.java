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
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class HandTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<HandType> {

    public static HandTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(HandTypes.class)
    private final Map<String, HandType> handTypeMap = new HashMap<>();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(HandType extraCatalog) {
        throw new UnsupportedOperationException("Cannot register additional HandTypes!!!");
    }

    @Override
    public Optional<HandType> getById(String id) {
        return Optional.ofNullable(this.handTypeMap.get(checkNotNull(id, "Id cannot be null").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<HandType> getAll() {
        return ImmutableSet.copyOf(this.handTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        for (EnumHand enumHand : EnumHand.values()) {
            this.handTypeMap.put(enumHand.name().toLowerCase(Locale.ENGLISH), (HandType) (Object) enumHand);
        }
    }

    HandTypeRegistryModule() {
    }

    static final class Holder {
        static final HandTypeRegistryModule INSTANCE = new HandTypeRegistryModule();
    }
}
