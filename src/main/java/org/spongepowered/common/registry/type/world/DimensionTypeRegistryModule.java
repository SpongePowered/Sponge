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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.WorldManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public final class DimensionTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<DimensionType> {

    @RegisterCatalog(DimensionTypes.class)
    private final Map<String, DimensionType> dimensionTypeMappings = Maps.newHashMap();

    public static DimensionTypeRegistryModule getInstance() {
        return Holder.instance;
    }

    @Override
    public void registerDefaults() {
        WorldManager.registerVanillaTypesAndDimensions();
        for (net.minecraft.world.dimension.DimensionType dimensionType : WorldManager.getDimensionTypes()) {
            final DimensionType apiDimensionType = (DimensionType) (Object) dimensionType;
            this.dimensionTypeMappings.put(apiDimensionType.getId(), apiDimensionType);
        }
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(DimensionType dimType) {
        this.dimensionTypeMappings.put(dimType.getId().toLowerCase(), dimType);
        WorldManager.registerDimensionType((net.minecraft.world.dimension.DimensionType) (Object) dimType);
    }

    @Override
    public Optional<DimensionType> getById(String id) {
        checkNotNull(id);
        id = WorldManager.fixDimensionTypeId(id);
        return Optional.ofNullable(this.dimensionTypeMappings.get(id.toLowerCase()));
    }

    @Override
    public Collection<DimensionType> getAll() {
        return Collections.unmodifiableCollection(this.dimensionTypeMappings.values());
    }

    @AdditionalRegistration
    public void reApplyDimensionTypes() {
        // Re-map fields in case mods have changed vanilla providers
        RegistryHelper.mapFields(DimensionTypes.class, this.dimensionTypeMappings);
    }

    private DimensionTypeRegistryModule() {
    }

    private static final class Holder {

        private static final DimensionTypeRegistryModule instance = new DimensionTypeRegistryModule();
    }
}
