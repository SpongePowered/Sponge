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
import net.minecraft.block.BlockPrismarine;
import org.spongepowered.api.data.type.PrismarineType;
import org.spongepowered.api.data.type.PrismarineTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class PrismarineRegistryModule implements CatalogRegistryModule<PrismarineType> {

    @RegisterCatalog(PrismarineTypes.class)
    private final Map<String, PrismarineType> prismarineTypeMappings = new ImmutableMap.Builder<String, PrismarineType>()
        .put("bricks", (PrismarineType) (Object) BlockPrismarine.EnumType.BRICKS)
        .put("dark", (PrismarineType) (Object) BlockPrismarine.EnumType.DARK)
        .put("rough", (PrismarineType) (Object) BlockPrismarine.EnumType.ROUGH)
        .build();

    @Override
    public Optional<PrismarineType> getById(String id) {
        return Optional.ofNullable(this.prismarineTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PrismarineType> getAll() {
        return ImmutableList.copyOf(this.prismarineTypeMappings.values());
    }

}
