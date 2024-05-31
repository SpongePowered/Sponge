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
package org.spongepowered.common.world.volume.buffer.archetype;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.api.util.mirror.Mirrors;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.block.entity.BlockEntityArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeVolume;
import org.spongepowered.api.world.volume.biome.BiomeVolume;
import org.spongepowered.api.world.volume.block.BlockVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.util.MemoizedSupplier;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractReferentArchetypeVolume<A extends ArchetypeVolume> implements ArchetypeVolume {

    protected final Supplier<A> reference;
    protected final Transformation transformation;

    protected AbstractReferentArchetypeVolume(final Supplier<A> reference, final Transformation transformation) {
        this.reference = MemoizedSupplier.memoize(reference);
        this.transformation = transformation;
    }

    public final Transformation transformation() {
        return this.transformation;
    }

    protected <T> T applyReference(final Function<A, T> function) {
        final @Nullable A archetypeVolume = this.reference.get();
        Objects.requireNonNull(archetypeVolume, "ArchetypeVolume reference lost");
        return function.apply(archetypeVolume);
    }

    protected void consumeReference(final Consumer<A> function) {
        final @Nullable A archetypeVolume = this.reference.get();
        Objects.requireNonNull(archetypeVolume, "ArchetypeVolume reference lost");
        function.accept(archetypeVolume);
    }

    public Vector3i inverseTransform(final double x, final double y, final double z) {
        return this.transformation.inverse()
            .transformPosition(new Vector3d(x, y, z))
            .toInt();
    }

    protected Vector3i transformBlockSizes(final Vector3i min, final Vector3i max, final BiFunction<Vector3i, Vector3i, Vector3i> minmax) {
        final Vector3d rawBlockMin = min.toDouble().add(VolumePositionTranslators.BLOCK_OFFSET);
        final Vector3i transformedMin = this.transformation.transformPosition(rawBlockMin)
            .sub(VolumePositionTranslators.BLOCK_OFFSET)
            .toInt();
        final Vector3d rawBlockMax = max.toDouble().add(VolumePositionTranslators.BLOCK_OFFSET);
        final Vector3i transformedMax = this.transformation.transformPosition(rawBlockMax)
            .sub(VolumePositionTranslators.BLOCK_OFFSET)
            .toInt();
        return minmax.apply(transformedMin, transformedMax);
    }

    protected Vector3i transformBlockSize(final BiFunction<Vector3i, Vector3i, Vector3i> minmax) {
        return this.applyReference(a -> this.transformBlockSizes(a.min(), a.max(), minmax));
    }

    protected Vector3d transformStreamBlockPosition(final Vector3d blockPosition) {
        // we assume that the position is already center adjusted, but since
        // we're not going to want to "flatten" the positions, we need to correctly
        // round and then re-add the offset post transformation
        return this.transformation.transformPosition(blockPosition);
    }

    @Override
    public Palette<BlockState, BlockType> blockPalette() {
        return this.reference.get().blockPalette();
    }

    @Override
    public Vector3i min() {
        return this.transformBlockSize(Vector3i::min);
    }

    @Override
    public Vector3i max() {
        return this.transformBlockSize(Vector3i::max);
    }

    @Override
    public Vector3i size() {
        return this.applyReference(ArchetypeVolume::size);
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.contains(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public boolean isAreaAvailable(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.isAreaAvailable(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public ArchetypeVolume transform(final Transformation transformation) {
        return new ReferentArchetypeVolume(
            this, Objects.requireNonNull(transformation, "Transformation cannot be null"));
    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(
        final int x, final int y, final int z
    ) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.blockEntityArchetype(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        return this.applyReference(a -> a.blockEntityArchetypes().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> this.transformation.transformPosition(e.getKey().toDouble()).toInt(),
                    Map.Entry::getValue
                )));
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockEntityArchetype> blockEntityArchetypeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.applyTransformationsToStream(
            min,
            max,
            options,
            BlockEntityArchetypeVolume.Streamable::blockEntityArchetypeStream,
            (e, rotation, mirror) -> e.type()
        );
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        this.consumeReference(a -> a.addBlockEntity(transformed.x(), transformed.y(), transformed.z(), archetype));
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        this.consumeReference(a -> a.removeBlockEntity(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes() {
        return this.applyReference(EntityArchetypeVolume::entityArchetypes);
    }

    @Override
    public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
        return this.applyReference(a -> a.entityArchetypesByPosition()
            .stream()
            .map(e -> EntityArchetypeEntry.of(e.archetype(), this.transformation.transformPosition(e.position())))
            .collect(Collectors.toList())
        );
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes(
        final Predicate<EntityArchetype> filter
    ) {
        return this.applyReference(e -> e.entityArchetypes(filter));
    }

    @Override
    public VolumeStream<ArchetypeVolume, EntityArchetype> entityArchetypeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.applyTransformationsToStream(
            min,
            max,
            options,
            EntityArchetypeVolume.Streamable::entityArchetypeStream,
            (e, rotation, mirror) -> e.type()
        );
    }

    @Override
    public Stream<EntityArchetypeEntry> entitiesByPosition() {
        return this.applyReference(a -> a.entityArchetypesByPosition()
            .stream()
            .map(e -> EntityArchetypeEntry.of(e.archetype(), this.transformation.transformPosition(e.position())))
        );
    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        final Vector3d position = entry.position();
        final Vector3i transformed = this.inverseTransform(position.x(), position.y(), position.z());
        this.consumeReference(a -> a.addEntity(entry.archetype(), transformed.toDouble()));
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.biome(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public VolumeStream<ArchetypeVolume, Biome> biomeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.applyTransformationsToStream(
            min,
            max,
            options,
            BiomeVolume.Streamable::biomeStream,
            (e, rotation, mirror) -> e.type()
        );
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.setBiome(transformed.x(), transformed.y(), transformed.z(), biome));
    }

    @Override
    public BlockState block(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.block(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.fluid(transformed.x(), transformed.y(), transformed.z()));
    }

    @Override
    public int highestYAt(final int x, final int z) {
        final Vector3i transformed = this.inverseTransform(x, 0, z);
        return this.applyReference(a -> a.highestYAt(transformed.x(), transformed.z()));
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockState> blockStateStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        return this.applyTransformationsToStream(
            min,
            max,
            options,
            BlockVolume.Streamable::blockStateStream,
            (e, rotation, mirror) -> e.type()
                    .mirror(mirror)
                    .rotate(rotation)
        );
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.setBlock(transformed.x(), transformed.y(), transformed.z(), block));
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        final Vector3i transformed = this.inverseTransform(x, y, z);
        return this.applyReference(a -> a.removeBlock(transformed.x(), transformed.y(), transformed.z()));
    }

    protected interface StreamCreator<TA extends Volume, SE> {
        VolumeStream<ArchetypeVolume, SE> createStream(
            TA targetVolume,
            Vector3i min,
            Vector3i max,
            StreamOptions options
        );
    }

    private <T> VolumeStream<ArchetypeVolume, T> applyTransformationsToStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options,
        final StreamCreator<A, T> streamCreator,
        final VolumeStreamUtils.TriFunction<VolumeElement<ArchetypeVolume, T>, Supplier<Rotation>, Supplier<Mirror>, T> elementTransform
    ) {
        final Vector3i transformedMin = this.min();
        final Vector3i transformedMax = this.max();
        VolumeStreamUtils.validateStreamArgs(min, max, transformedMin, transformedMax, options);
        final Vector3i minDiff = min.sub(transformedMin);
        final Vector3i maxDiff = transformedMax.sub(max);
        final boolean xMirror = this.transformation.mirror(Axis.X);
        final boolean zMirror = this.transformation.mirror(Axis.Z);
        final Supplier<Mirror> mirror = xMirror
            ? Mirrors.FRONT_BACK
            : zMirror ? Mirrors.LEFT_RIGHT : Mirrors.NONE;
        return this.applyReference(a -> streamCreator.createStream(a, a.min().add(minDiff), a.max().sub(maxDiff), options))
            .transform(e -> VolumeElement.of(
                this,
                elementTransform.apply(e, this.transformation::rotation, mirror),
                this.transformStreamBlockPosition(e.position().add(VolumePositionTranslators.BLOCK_OFFSET)).sub(VolumePositionTranslators.BLOCK_OFFSET)
            ));

    }
}
