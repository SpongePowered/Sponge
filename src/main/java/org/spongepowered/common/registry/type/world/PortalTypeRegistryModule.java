/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.PortalType;
import org.spongepowered.api.world.PortalTypes;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.SpongePortalType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PortalTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<PortalType> {

    @RegisterCatalog(PortalTypes.class)
    private final Map<String, PortalType> portalTypeMap = ImmutableMap.<String, PortalType>builder()
            .put("end", new SpongePortalType("end", "End"))
            .put("nether", new SpongePortalType("nether", "Nether"))
            .build();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(PortalType extraCatalog) {
    }

    @Override
    public Optional<PortalType> getById(String id) {
        return Optional.ofNullable(this.portalTypeMap.get(checkNotNull(id, "PortalType ID cannot be null!")
                .toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<PortalType> getAll() {
        return this.portalTypeMap.values();
    }
}
