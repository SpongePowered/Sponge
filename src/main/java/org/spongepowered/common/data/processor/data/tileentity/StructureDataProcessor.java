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
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeStructureData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.mixin.core.tileentity.TileEntityStructureAccessor;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StructureDataProcessor extends AbstractTileEntityDataProcessor<TileEntityStructure, StructureData, ImmutableStructureData> {

    public StructureDataProcessor() {
        super(TileEntityStructure.class);
    }

    @Override
    protected boolean doesDataExist(final TileEntityStructure container) {
        return true;
    }

    @Override
    protected boolean set(final TileEntityStructure container, final Map<Key<?>, Object> map) {
        @Nullable final String author = (String) map.get(Keys.STRUCTURE_AUTHOR);
        if (author != null) {
            ((TileEntityStructureAccessor) container).accessor$setAuthor(author);
        }

        container.setIgnoresEntities((Boolean) map.get(Keys.STRUCTURE_IGNORE_ENTITIES));
        container.setIntegrity((Float) map.get(Keys.STRUCTURE_INTEGRITY));

        @Nullable final StructureMode mode = (StructureMode) map.get(Keys.STRUCTURE_MODE);
        if (mode != null) {
            ((TileEntityStructureAccessor) container).accessor$setMode((TileEntityStructure.Mode) (Object) mode);
        }

        @Nullable final Vector3i position = (Vector3i) map.get(Keys.STRUCTURE_POSITION);
        if (position != null) {
            ((TileEntityStructureAccessor) container).accessor$setPosition(VecHelper.toBlockPos(checkNotNull(position, "position")));
        }

        container.setPowered((Boolean) map.get(Keys.STRUCTURE_POWERED));

        @Nullable final Long seed = (Long) map.get(Keys.STRUCTURE_SEED);
        if (seed != null) {
            container.setSeed(seed);
        }

        container.setShowAir((Boolean) map.get(Keys.STRUCTURE_SHOW_AIR));
        container.setShowBoundingBox((Boolean) map.get(Keys.STRUCTURE_SHOW_BOUNDING_BOX));

        @Nullable final Boolean showBoundingBox = (Boolean) map.get(Keys.STRUCTURE_SHOW_BOUNDING_BOX);
        if (showBoundingBox != null) {
        }

        @Nullable final Vector3i size = (Vector3i) map.get(Keys.STRUCTURE_SIZE);
        if (size != null) {
            ((TileEntityStructureAccessor) container).accessor$setSize(VecHelper.toBlockPos(checkNotNull(size, "position")));
        }

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final TileEntityStructure container) {
        final ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        builder.put(Keys.STRUCTURE_AUTHOR, ((TileEntityStructureAccessor) container).accessor$getAuthor());
        builder.put(Keys.STRUCTURE_IGNORE_ENTITIES, ((TileEntityStructureAccessor) container).accessor$getIgnoreEntities());
        builder.put(Keys.STRUCTURE_INTEGRITY, ((TileEntityStructureAccessor) container).accessor$getIntegrity());
        builder.put(Keys.STRUCTURE_MODE, ((TileEntityStructureAccessor) container).accessor$getMode());
        builder.put(Keys.STRUCTURE_POSITION, ((TileEntityStructureAccessor) container).accessor$getPosition());
        builder.put(Keys.STRUCTURE_POWERED, container.isPowered());
        builder.put(Keys.STRUCTURE_SHOW_AIR, ((TileEntityStructureAccessor) container).accessor$getShowAir());
        builder.put(Keys.STRUCTURE_SHOW_BOUNDING_BOX, ((TileEntityStructureAccessor) container).accessor$getShowBoundingBox());
        builder.put(Keys.STRUCTURE_SIZE, ((TileEntityStructureAccessor) container).accessor$getSize());
        return builder.build();
    }

    @Override
    protected StructureData createManipulator() {
        return new SpongeStructureData();
    }

    @Override
    public Optional<StructureData> fill(final DataContainer container, StructureData data) {
        checkNotNull(data, "data");

        final Optional<String> author = container.getString(Keys.STRUCTURE_AUTHOR.getQuery());
        if (author.isPresent()) {
            data = data.set(Keys.STRUCTURE_AUTHOR, author.get());
        }

        final Optional<Boolean> ignoreEntities = container.getBoolean(Keys.STRUCTURE_IGNORE_ENTITIES.getQuery());
        if (ignoreEntities.isPresent()) {
            data = data.set(Keys.STRUCTURE_IGNORE_ENTITIES, ignoreEntities.get());
        }

        final Optional<Float> integrity = container.getFloat(Keys.STRUCTURE_INTEGRITY.getQuery());
        if (integrity.isPresent()) {
            data = data.set(Keys.STRUCTURE_INTEGRITY, integrity.get());
        }

        final Optional<StructureMode> mode = container.getObject(Keys.STRUCTURE_MODE.getQuery(), StructureMode.class);
        if (mode.isPresent()) {
            data = data.set(Keys.STRUCTURE_MODE, mode.get());
        }

        final Optional<Vector3i> position = container.getObject(Keys.STRUCTURE_POSITION.getQuery(), Vector3i.class);
        if (position.isPresent()) {
            data = data.set(Keys.STRUCTURE_POSITION, position.get());
        }

        final Optional<Boolean> powered = container.getBoolean(Keys.STRUCTURE_POWERED.getQuery());
        if (powered.isPresent()) {
            data = data.set(Keys.STRUCTURE_POWERED, powered.get());
        }

        final Optional<Boolean> showAir = container.getBoolean(Keys.STRUCTURE_SHOW_AIR.getQuery());
        if (showAir.isPresent()) {
            data = data.set(Keys.STRUCTURE_SHOW_AIR, showAir.get());
        }

        final Optional<Boolean> showBoundingBox = container.getBoolean(Keys.STRUCTURE_SHOW_BOUNDING_BOX.getQuery());
        if (showBoundingBox.isPresent()) {
            data = data.set(Keys.STRUCTURE_SHOW_BOUNDING_BOX, showBoundingBox.get());
        }

        final Optional<Vector3i> size = container.getObject(Keys.STRUCTURE_SIZE.getQuery(), Vector3i.class);
        if (size.isPresent()) {
            data = data.set(Keys.STRUCTURE_SIZE, size.get());
        }

        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(final DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}
