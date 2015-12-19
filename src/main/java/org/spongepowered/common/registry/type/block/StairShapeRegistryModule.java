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
import net.minecraft.block.BlockStairs;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.api.data.type.StairShapes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class StairShapeRegistryModule implements CatalogRegistryModule<StairShape> {

    @RegisterCatalog(StairShapes.class)
    private final Map<String, StairShape> stairShapeMappings = ImmutableMap.<String, StairShape>builder()
        .put("straight", (StairShape) (Object) BlockStairs.EnumShape.STRAIGHT)
        .put("inner_left", (StairShape) (Object) BlockStairs.EnumShape.INNER_LEFT)
        .put("inner_right", (StairShape) (Object) BlockStairs.EnumShape.INNER_RIGHT)
        .put("outer_left", (StairShape) (Object) BlockStairs.EnumShape.OUTER_LEFT)
        .put("outer_right", (StairShape) (Object) BlockStairs.EnumShape.OUTER_RIGHT)
        .build();

    @Override
    public Optional<StairShape> getById(String id) {
        return Optional.ofNullable(this.stairShapeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<StairShape> getAll() {
        return ImmutableList.copyOf(this.stairShapeMappings.values());
    }

}
