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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableStructureData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.StructureData;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeStructureData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.bridge.tileentity.TileEntityStructureBridge;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.tileentity.StructureBlockTileEntity;

public final class StructureDataProcessor extends AbstractTileEntityDataProcessor<StructureBlockTileEntity, StructureData, ImmutableStructureData> {

    public StructureDataProcessor() {
        super(StructureBlockTileEntity.class);
    }

    @Override
    protected boolean doesDataExist(StructureBlockTileEntity container) {
        return true;
    }

    @Override
    protected boolean set(StructureBlockTileEntity container, Map<Key<?>, Object> map) {
        @Nullable String author = (String) map.get(Keys.STRUCTURE_AUTHOR);
        if (author != null) {
            ((TileEntityStructureBridge) container).bridge$setAuthor(author);
        }

        container.func_184406_a((Boolean) map.get(Keys.STRUCTURE_IGNORE_ENTITIES));
        container.func_189718_a((Float) map.get(Keys.STRUCTURE_INTEGRITY));

        @Nullable StructureMode mode = (StructureMode) map.get(Keys.STRUCTURE_MODE);
        if (mode != null) {
            ((TileEntityStructureBridge) container).bridge$setMode(mode);
        }

        @Nullable Vector3i position = (Vector3i) map.get(Keys.STRUCTURE_POSITION);
        if (position != null) {
            ((TileEntityStructureBridge) container).bridge$setPosition(position);
        }

        container.func_189723_d((Boolean) map.get(Keys.STRUCTURE_POWERED));

        @Nullable Long seed = (Long) map.get(Keys.STRUCTURE_SEED);
        if (seed != null) {
            container.func_189725_a(seed);
        }

        container.func_189703_e((Boolean) map.get(Keys.STRUCTURE_SHOW_AIR));
        container.func_189710_f((Boolean) map.get(Keys.STRUCTURE_SHOW_BOUNDING_BOX));

        @Nullable Boolean showBoundingBox = (Boolean) map.get(Keys.STRUCTURE_SHOW_BOUNDING_BOX);
        if (showBoundingBox != null) {
        }

        @Nullable Vector3i size = (Vector3i) map.get(Keys.STRUCTURE_SIZE);
        if (size != null) {
            ((TileEntityStructureBridge) container).bridge$setSize(size);
        }

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(StructureBlockTileEntity container) {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        builder.put(Keys.STRUCTURE_AUTHOR, ((TileEntityStructureBridge) container).bridge$getAuthor());
        builder.put(Keys.STRUCTURE_IGNORE_ENTITIES, ((TileEntityStructureBridge) container).bridge$shouldIgnoreEntities());
        builder.put(Keys.STRUCTURE_INTEGRITY, ((TileEntityStructureBridge) container).bridge$getIntegrity());
        builder.put(Keys.STRUCTURE_MODE, ((TileEntityStructureBridge) container).bridge$getMode());
        builder.put(Keys.STRUCTURE_POSITION, ((TileEntityStructureBridge) container).bridge$getPosition());
        builder.put(Keys.STRUCTURE_POWERED, container.func_189722_G());
        builder.put(Keys.STRUCTURE_SHOW_AIR, ((TileEntityStructureBridge) container).bridge$shouldShowAir());
        builder.put(Keys.STRUCTURE_SHOW_BOUNDING_BOX, ((TileEntityStructureBridge) container).bridge$shouldShowBoundingBox());
        builder.put(Keys.STRUCTURE_SIZE, ((TileEntityStructureBridge) container).bridge$getSize());
        return builder.build();
    }

    @Override
    protected StructureData createManipulator() {
        return new SpongeStructureData();
    }

    @Override
    public Optional<StructureData> fill(DataContainer container, StructureData data) {
        checkNotNull(data, "data");

        Optional<String> author = container.getString(Keys.STRUCTURE_AUTHOR.getQuery());
        if (author.isPresent()) {
            data = data.set(Keys.STRUCTURE_AUTHOR, author.get());
        }

        Optional<Boolean> ignoreEntities = container.getBoolean(Keys.STRUCTURE_IGNORE_ENTITIES.getQuery());
        if (ignoreEntities.isPresent()) {
            data = data.set(Keys.STRUCTURE_IGNORE_ENTITIES, ignoreEntities.get());
        }

        Optional<Float> integrity = container.getFloat(Keys.STRUCTURE_INTEGRITY.getQuery());
        if (integrity.isPresent()) {
            data = data.set(Keys.STRUCTURE_INTEGRITY, integrity.get());
        }

        Optional<StructureMode> mode = container.getObject(Keys.STRUCTURE_MODE.getQuery(), StructureMode.class);
        if (mode.isPresent()) {
            data = data.set(Keys.STRUCTURE_MODE, mode.get());
        }

        Optional<Vector3i> position = container.getObject(Keys.STRUCTURE_POSITION.getQuery(), Vector3i.class);
        if (position.isPresent()) {
            data = data.set(Keys.STRUCTURE_POSITION, position.get());
        }

        Optional<Boolean> powered = container.getBoolean(Keys.STRUCTURE_POWERED.getQuery());
        if (powered.isPresent()) {
            data = data.set(Keys.STRUCTURE_POWERED, powered.get());
        }

        Optional<Boolean> showAir = container.getBoolean(Keys.STRUCTURE_SHOW_AIR.getQuery());
        if (showAir.isPresent()) {
            data = data.set(Keys.STRUCTURE_SHOW_AIR, showAir.get());
        }

        Optional<Boolean> showBoundingBox = container.getBoolean(Keys.STRUCTURE_SHOW_BOUNDING_BOX.getQuery());
        if (showBoundingBox.isPresent()) {
            data = data.set(Keys.STRUCTURE_SHOW_BOUNDING_BOX, showBoundingBox.get());
        }

        Optional<Vector3i> size = container.getObject(Keys.STRUCTURE_SIZE.getQuery(), Vector3i.class);
        if (size.isPresent()) {
            data = data.set(Keys.STRUCTURE_SIZE, size.get());
        }

        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}
