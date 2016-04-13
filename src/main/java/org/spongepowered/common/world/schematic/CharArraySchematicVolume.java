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
package org.spongepowered.common.world.schematic;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.SchematicVolume;
import org.spongepowered.common.util.gen.CharArrayMutableBlockBuffer;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CharArraySchematicVolume extends CharArrayMutableBlockBuffer implements SchematicVolume {

    private final Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();
    private final List<EntityArchetype> entities = Lists.newArrayList();

    public CharArraySchematicVolume(Vector3i start, Vector3i size) {
        super(start, size);
    }

    public CharArraySchematicVolume(Palette palette, Vector3i start, Vector3i size) {
        super(palette, start, size);
    }

    @Override
    public Optional<TileEntityArchetype> getBlockArchetype(int x, int y, int z) {
        return Optional.ofNullable(this.tiles.get(getBlockMin().add(x, y, z)));
    }

    @Override
    public Map<Vector3i, TileEntityArchetype> getBlockArchetypes() {
        return this.tiles;
    }

    @Override
    public Collection<EntityArchetype> getEntityArchetypes() {
        return this.entities;
    }

    @Override
    public MutableBlockVolumeWorker<? extends SchematicVolume> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

}
