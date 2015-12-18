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
package org.spongepowered.common.registry.type.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockDoublePlant;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class DoublePlantTypeRegistryModule implements CatalogRegistryModule<DoublePlantType> {

    @RegisterCatalog(DoublePlantTypes.class)
    private final Map<String, DoublePlantType> doublePlantMappings = new ImmutableMap.Builder<String, DoublePlantType>()
        .put("sunflower", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.SUNFLOWER)
        .put("syringa", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.SYRINGA)
        .put("grass", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.GRASS)
        .put("fern", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.FERN)
        .put("rose", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.ROSE)
        .put("paeonia", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.PAEONIA)
        .build();

    @Override
    public Optional<DoublePlantType> getById(String id) {
        return Optional.ofNullable(this.doublePlantMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DoublePlantType> getAll() {
        return ImmutableList.copyOf(this.doublePlantMappings.values());
    }

}
