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

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.world.volume.buffer.archetype.AbstractReferentArchetypeVolume;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.Set;

public class ReferentSchematicVolume extends AbstractReferentArchetypeVolume<Schematic> implements Schematic {
    public ReferentSchematicVolume(final Schematic reference, final Transformation transformation) {
        super(() -> reference, transformation);
    }

    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return this.applyReference(Schematic::blockPalette);
    }

    @Override
    public Palette<Biome, Biome> biomePalette() {
        return this.applyReference(Schematic::biomePalette);
    }

    @Override
    public DataView metadata() {
        return this.applyReference(Schematic::metadata);
    }

    @Override
    public <E> Optional<E> get(
        final int x, final int y, final int z, final Key<? extends Value<E>> key
    ) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.get(transformed, key));
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final int x, final int y, final int z, final Key<V> key) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.getValue(transformed, key));
    }

    @Override
    public boolean supports(final int x, final int y, final int z, final Key<?> key) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.supports(transformed, key));
    }

    @Override
    public Set<Key<?>> keys(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.keys(transformed));
    }

    @Override
    public Set<Value.Immutable<?>> getValues(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.getValues(transformed));
    }

    @Override
    public <E> DataTransactionResult offer(
        final int x, final int y, final int z, final Key<? extends Value<E>> key, final E value
    ) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.offer(transformed, key, value));
    }

    @Override
    public DataTransactionResult remove(final int x, final int y, final int z, final Key<?> key) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.remove(transformed, key));
    }

    @Override
    public DataTransactionResult undo(final int x, final int y, final int z, final DataTransactionResult result) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.undo(transformed, result));
    }

    @Override
    public DataTransactionResult copyFrom(final int xTo, final int yTo, final int zTo, final ValueContainer from) {
        final Vector3i transformed = this.inverseTransform(xTo, yTo, zTo);
        return this.applyReference(a -> a.copyFrom(transformed, from));
    }

    @Override
    public DataTransactionResult copyFrom(
        final int xTo, final int yTo, final int zTo, final ValueContainer from, final MergeFunction function
    ) {
        final Vector3i transformed = this.inverseTransform(xTo, yTo, zTo);
        return this.applyReference(a -> a.copyFrom(transformed, from, function));
    }

    @Override
    public DataTransactionResult copyFrom(
        final int xTo, final int yTo, final int zTo, final int xFrom, final int yFrom, final int zFrom, final MergeFunction function
    ) {
        final Vector3i transformed = this.inverseTransform(xTo, yTo, zTo);
        final Vector3i from = this.inverseTransform(xFrom, yFrom, zFrom);
        return this.applyReference(a -> a.copyFrom(transformed, from, function));
    }

    @Override
    public boolean validateRawData(final int x, final int y, final int z, final DataView container) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.validateRawData(transformed, container));
    }

    @Override
    public void setRawData(final int x, final int y, final int z, final DataView container) throws InvalidDataException {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        this.consumeReference(a -> a.setRawData(transformed, container));
    }
}
