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
package org.spongepowered.common.world.volume.block;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.block.BlockVolume;
import org.spongepowered.api.world.volume.block.BlockVolumeFactory;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.common.world.volume.buffer.block.ArrayImmutableBlockBuffer;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.common.world.volume.buffer.block.BlockBackingData;
import org.spongepowered.math.vector.Vector3i;

public class SpongeBlockVolumeFactory implements BlockVolumeFactory {

    @Override
    public BlockVolume.Mutable empty(
        final Palette<BlockState, BlockType> palette,
        final RegistryReference<BlockType> defaultState,
        final Vector3i min,
        final Vector3i max
    ) {
        return new ArrayMutableBlockBuffer(palette, defaultState, min, max.sub(min).add(Vector3i.ONE));
    }

    @Override
    public BlockVolume.Mutable copyFromRange(
        final BlockVolume.Streamable<@NonNull ?> existing, final Vector3i newMin, final Vector3i newMax
    ) {
        final ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(newMin, newMax.sub(newMin).add(Vector3i.ONE));
        existing.blockStateStream(newMin, newMax, StreamOptions.lazily())
            .apply(VolumeCollectors.of(buffer, VolumePositionTranslators.identity(), VolumeApplicators.applyBlocks()));
        return buffer;
    }

    @Override
    public BlockVolume.Mutable copy(final BlockVolume.Streamable<@NonNull ?> existing
    ) {
        final ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(existing.min(), existing.size());
        existing.blockStateStream(existing.min(), existing.max(), StreamOptions.lazily())
            .apply(VolumeCollectors.of(buffer, VolumePositionTranslators.identity(), VolumeApplicators.applyBlocks()));
        return buffer;
    }

    @Override
    public BlockVolume.Immutable immutableOf(final BlockVolume.Streamable<@NonNull ?> existing
    ) {
        if (existing instanceof ArrayMutableBlockBuffer) {
            return this.createImmutableFromBufferData((ArrayMutableBlockBuffer) existing);
        }
        final ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(
            existing.min(),
            existing.size()
        );
        existing.blockStateStream(existing.min(), existing.max(), StreamOptions.lazily())
            .apply(VolumeCollectors.of(buffer, VolumePositionTranslators.identity(), VolumeApplicators.applyBlocks()));
        return this.createImmutableFromBufferData(buffer);
    }

    private ArrayImmutableBlockBuffer createImmutableFromBufferData(final ArrayMutableBlockBuffer arrayBuffer) {
        final BlockBackingData data = arrayBuffer.getCopiedBackingData();
        final Palette.Immutable<BlockState, BlockType> immutablePalette = arrayBuffer.blockPalette().asImmutable();
        return new ArrayImmutableBlockBuffer(immutablePalette, arrayBuffer.min(), arrayBuffer.size(), data);
    }

    @Override
    public BlockVolume.Immutable immutableOf(final BlockVolume.Streamable<@NonNull ?> existing, final Vector3i newMin, final Vector3i newMax
    ) {
        final ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(newMin, newMax.sub(newMin).add(Vector3i.ONE));
        existing.blockStateStream(newMin, newMax, StreamOptions.lazily())
            .apply(VolumeCollectors.of(buffer, VolumePositionTranslators.identity(), VolumeApplicators.applyBlocks()));
        return this.createImmutableFromBufferData(buffer);
    }
}
