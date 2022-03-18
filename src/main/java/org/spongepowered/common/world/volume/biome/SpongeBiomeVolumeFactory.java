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
package org.spongepowered.common.world.volume.biome;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.volume.biome.BiomeVolume;
import org.spongepowered.api.world.volume.biome.BiomeVolumeFactory;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.api.world.volume.virtual.UnrealizedBiomeVolume;
import org.spongepowered.common.world.volume.buffer.biome.ByteArrayImmutableBiomeBuffer;
import org.spongepowered.common.world.volume.buffer.biome.ByteArrayMutableBiomeBuffer;
import org.spongepowered.math.vector.Vector3i;

public class SpongeBiomeVolumeFactory implements BiomeVolumeFactory {

    @Override
    public BiomeVolume.Mutable empty(Palette<Biome, Biome> palette, RegistryReference<Biome> defaultBiome, Vector3i min, Vector3i max) {
        final RegistryHolder registries = Sponge.server().worldManager().defaultWorld();
        final Biome biome = defaultBiome.get(registries);
        final ByteArrayMutableBiomeBuffer buffer = new ByteArrayMutableBiomeBuffer(palette, min, max);
        buffer.biomeStream(min, max, StreamOptions.lazily()).apply(VolumeCollectors.of(buffer, VolumePositionTranslators.identity(),
                (volume, element) -> volume.setBiome(element.position().round().toInt(), biome)));
        return buffer;
    }

    @Override
    public BiomeVolume.Mutable copyFromRange(BiomeVolume.Streamable<@NonNull ?> existing, Vector3i newMin, Vector3i newMax) {
        final RegistryHolder registries = Sponge.server().worldManager().defaultWorld();
        final Palette<Biome, Biome> palette = PaletteTypes.BIOME_PALETTE.get().create(registries, RegistryTypes.BIOME);
        final ByteArrayMutableBiomeBuffer buffer = new ByteArrayMutableBiomeBuffer(palette, newMin, newMax);
        existing.biomeStream(newMin, newMax, StreamOptions.lazily()).apply(
                VolumeCollectors.of(buffer, VolumePositionTranslators.identity(), VolumeApplicators.applyBiomes())
        );
        return buffer;
    }

    @Override
    public BiomeVolume.Mutable copy(BiomeVolume.Streamable<@NonNull ?> existing) {
        return this.copyFromRange(existing, existing.min(), existing.max());
    }

    @Override
    public BiomeVolume.Immutable immutableOf(BiomeVolume.Streamable<@NonNull ?> existing) {
        if (existing instanceof ByteArrayMutableBiomeBuffer) {
            return this.createImmutableFromBufferData((ByteArrayMutableBiomeBuffer)existing);
        }
        final BiomeVolume.Mutable buffer = this.copy(existing);
        return this.createImmutableFromBufferData((ByteArrayMutableBiomeBuffer) buffer);
    }

    @Override
    public BiomeVolume.Immutable immutableOf(BiomeVolume.Streamable<@NonNull ?> existing, Vector3i newMin, Vector3i newMax) {
        final BiomeVolume.Mutable buffer = this.copyFromRange(existing, newMin, newMax);
        return this.createImmutableFromBufferData((ByteArrayMutableBiomeBuffer) buffer);
    }

    private BiomeVolume.Immutable createImmutableFromBufferData(final ByteArrayMutableBiomeBuffer buffer) {
        return new ByteArrayImmutableBiomeBuffer(buffer.getPalette(), buffer.getCopiedBackingData(), buffer.min(), buffer.max());
    }

    @Override
    public UnrealizedBiomeVolume.Mutable empty(Vector3i min, Vector3i max) {
        throw new UnsupportedOperationException("No implementation yet"); // TODO implement me
    }
}
