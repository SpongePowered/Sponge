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
import net.minecraft.block.BlockFlower;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class PlantTypeModuleRegistry implements CatalogRegistryModule<PlantType> {

    @RegisterCatalog(PlantTypes.class)
    private final Map<String, PlantType> plantTypeMappings = new ImmutableMap.Builder<String, PlantType>()
        .put("dandelion", (PlantType) (Object) BlockFlower.EnumFlowerType.DANDELION)
        .put("poppy", (PlantType) (Object) BlockFlower.EnumFlowerType.POPPY)
        .put("blue_orchid", (PlantType) (Object) BlockFlower.EnumFlowerType.BLUE_ORCHID)
        .put("allium", (PlantType) (Object) BlockFlower.EnumFlowerType.ALLIUM)
        .put("houstonia", (PlantType) (Object) BlockFlower.EnumFlowerType.HOUSTONIA)
        .put("red_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.RED_TULIP)
        .put("orange_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.ORANGE_TULIP)
        .put("white_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.WHITE_TULIP)
        .put("pink_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.PINK_TULIP)
        .put("oxeye_daisy", (PlantType) (Object) BlockFlower.EnumFlowerType.OXEYE_DAISY)
        .build();

    @Override
    public Optional<PlantType> getById(String id) {
        return Optional.ofNullable(this.plantTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PlantType> getAll() {
        return ImmutableList.copyOf(this.plantTypeMappings.values());
    }

}
