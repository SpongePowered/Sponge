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
import net.minecraft.block.BlockHugeMushroom;
import org.spongepowered.api.data.type.BigMushroomType;
import org.spongepowered.api.data.type.BigMushroomTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class BigMushroomRegistryModule implements CatalogRegistryModule<BigMushroomType> {

    @RegisterCatalog(BigMushroomTypes.class)
    private final Map<String, BigMushroomType> bigMushroomTypeMappings = ImmutableMap.<String, BigMushroomType>builder()
        .put("center", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.CENTER)
        .put("all_inside", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.ALL_INSIDE)
        .put("all_outside", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.ALL_OUTSIDE)
        .put("all_stem", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.ALL_STEM)
        .put("east", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.EAST)
        .put("north", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.NORTH)
        .put("north_east", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.NORTH_EAST)
        .put("north_west", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.NORTH_WEST)
        .put("south", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.SOUTH)
        .put("south_east", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.SOUTH_EAST)
        .put("south_west", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.SOUTH_WEST)
        .put("stem", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.STEM)
        .put("west", (BigMushroomType) (Object) BlockHugeMushroom.EnumType.WEST)
        .build();

    @Override
    public Optional<BigMushroomType> getById(String id) {
        return Optional.ofNullable(this.bigMushroomTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<BigMushroomType> getAll() {
        return ImmutableList.copyOf(this.bigMushroomTypeMappings.values());
    }

}
