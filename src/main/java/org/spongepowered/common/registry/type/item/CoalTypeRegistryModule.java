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
package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.type.CoalType;
import org.spongepowered.api.data.type.CoalTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.item.SpongeCoalType;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CoalTypeRegistryModule implements CatalogRegistryModule<CoalType> {

    @RegisterCatalog(CoalTypes.class)
    public final Map<String, CoalType> coaltypeMappings = Maps.newHashMap();

    @Override
    public Optional<CoalType> getById(String id) {
        return Optional.ofNullable(this.coaltypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CoalType> getAll() {
        return ImmutableList.copyOf(this.coaltypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.coaltypeMappings.put("coal", new SpongeCoalType(0, "COAL", new SpongeTranslation("item.coal.name")));
        this.coaltypeMappings.put("charcoal", new SpongeCoalType(1, "CHARCOAL", new SpongeTranslation("item.charcoal.name")));
    }
}
