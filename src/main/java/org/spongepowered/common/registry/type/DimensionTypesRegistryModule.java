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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.world.SpongeDimensionType;

import java.util.Collection;
import java.util.Optional;

public class DimensionTypesRegistryModule implements CatalogRegistryModule<DimensionType> {

    @RegisterCatalog(DimensionTypes.class)
    public final BiMap<String, DimensionType> dimensionMappings = HashBiMap.create();

    @Override
    public Optional<DimensionType> getById(String id) {
        return Optional.ofNullable(this.dimensionMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DimensionType> getAll() {
        return ImmutableList.copyOf(this.dimensionMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.dimensionMappings.put("nether", new SpongeDimensionType("nether", true, WorldProviderHell.class, -1));
        this.dimensionMappings.put("overworld", new SpongeDimensionType("overworld", true, WorldProviderSurface.class, 0));
        this.dimensionMappings.put("end", new SpongeDimensionType("end", false, WorldProviderEnd.class, 1));
    }
}
