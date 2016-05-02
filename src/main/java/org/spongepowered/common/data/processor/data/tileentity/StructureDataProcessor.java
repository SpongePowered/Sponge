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
package org.spongepowered.common.data.processor.data.tileentity;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import net.minecraft.tileentity.TileEntityStructure;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableStructureData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.StructureData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeStructureData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntityStructure;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class StructureDataProcessor extends AbstractTileEntityDataProcessor<TileEntityStructure, StructureData, ImmutableStructureData> {

    public StructureDataProcessor() {
        super(TileEntityStructure.class);
    }

    @Override
    protected boolean doesDataExist(TileEntityStructure container) {
        return true;
    }

    @Override
    protected boolean set(TileEntityStructure container, Map<Key<?>, Object> map) {
        if (map.containsKey(Keys.STRUCTURE_POSITION)) {
            @Nullable Vector3i position = (Vector3i) map.get(Keys.STRUCTURE_POSITION);
            if (position != null) {
                ((IMixinTileEntityStructure) container).setPosition(position);
                container.markDirty();
            }
        }

        if (map.containsKey(Keys.STRUCTURE_SIZE)) {
            @Nullable Vector3i size = (Vector3i) map.get(Keys.STRUCTURE_SIZE);
            if (size != null) {
                ((IMixinTileEntityStructure) container).setSize(size);
            }
        }

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(TileEntityStructure container) {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        builder.put(Keys.STRUCTURE_POSITION, ((IMixinTileEntityStructure) container).getPosition());
        builder.put(Keys.STRUCTURE_SIZE, ((IMixinTileEntityStructure) container).getSize());
        return builder.build();
    }

    @Override
    protected StructureData createManipulator() {
        return new SpongeStructureData();
    }

    @Override
    public Optional<StructureData> fill(DataContainer container, StructureData data) {
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}
