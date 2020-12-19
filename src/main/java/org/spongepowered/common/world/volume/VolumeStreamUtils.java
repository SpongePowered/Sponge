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
package org.spongepowered.common.world.volume;

import net.minecraft.block.BlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.game.Region;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class VolumeStreamUtils {

    private VolumeStreamUtils() {}

    /**
     * Creates a {@link Supplier Supplier&lt;T&gt;} that weakly references the
     * provided {@code object} such that the object will not be referenced otherwise
     * within the Supplier itself, allowing it to be freely garbage collected, in the
     * circumstance the reference to the supplier is retained for an extended time.
     *
     * @param object The object being referenced
     * @param name The name of the object, for error printing
     * @param <T> The type of object requested
     * @return The supplier
     */
    public static <T> Supplier<T> createWeaklyReferencedSupplier(final T object, final String name) {
        final WeakReference<T> weakReference = new WeakReference<>(object);
        return () -> {
            final @Nullable T weaklyReferenced = weakReference.get();
            return Objects.requireNonNull(weaklyReferenced, () -> String.format("%s de-referenced!", name));
        };
    }

    public static Predicate<org.spongepowered.api.util.Tuple<Vector3d, EntityArchetype>> entityArchetypePositionFilter(final Vector3i min, final Vector3i max) {
        return VolumeStreamUtils.filterPositions(tuple -> tuple.getFirst().toInt(), min, max);
    }

    public static Predicate<Map.Entry<Vector3i, BlockEntityArchetype>> blockEntityArchetypePositionFilter(final Vector3i min, final Vector3i max) {
        return VolumeStreamUtils.filterPositions(Map.Entry::getKey, min, max);
    }

    public static <T> Predicate<T> filterPositions(final Function<T, Vector3i> pos, final Vector3i min, final Vector3i max) {
        return (entity) -> {
            final Vector3i apply = pos.apply(entity);
            return apply.getX() >= min.getX() && apply.getX() <= max.getX()
                && apply.getY() >= min.getY() && apply.getY() <= max.getY()
                && apply.getZ() >= min.getZ() && apply.getZ() <= max.getZ();
        };
    }

    public static <R extends Region<R>> BiFunction<R, ChunkPos, @Nullable IChunk> getChunkAccessorByStatus(
        final IWorldReader worldReader,
        final boolean shouldGenerate
    ) {
        final Supplier<IWorldReader> readerSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier(worldReader, "IWorldReader");
        return (world, chunkPos) -> {
            final ChunkStatus chunkStatus = shouldGenerate
                ? ChunkStatus.FULL
                : ChunkStatus.EMPTY;
            final @Nullable IChunk ichunk = readerSupplier.get().getChunk(chunkPos.x, chunkPos.z, chunkStatus, shouldGenerate);
            if (shouldGenerate) {
                Objects.requireNonNull(ichunk, "Chunk was expected to load fully and generate, but somehow got a null chunk!");
            }
            return (IChunk) ichunk;
        };
    }

    public static Function<IChunk, Stream<Map.Entry<BlockPos, Biome>>> getBiomesForChunkByPos(final IWorldReader reader, final Vector3i min,
        final Vector3i max
    ) {
        return VolumeStreamUtils.getElementByPosition(VolumeStreamUtils.chunkSectionBiomeGetter().asTri(reader), min, max);
    }

    public static Function<IChunk, Stream<Map.Entry<BlockPos, BlockState>>> getBlockStatesForSections(
        final Vector3i min,
        final Vector3i max
    ) {
        return VolumeStreamUtils.getElementByPosition(VolumeStreamUtils.chunkSectionBlockStateGetter(), min, max);
    }

    public static void validateStreamArgs(final Vector3i min, final Vector3i max, final StreamOptions options) {
        Objects.requireNonNull(min, "Minimum coordinates cannot be null");
        Objects.requireNonNull(max, "Maximum coordinates cannot be null");
        Objects.requireNonNull(options, "StreamOptions cannot be null!");

        if (min.getX() > max.getX()) {
            throw new IllegalArgumentException("Min(x) must be greater than max(x)!");
        }
        if (min.getY() > max.getY()) {
            throw new IllegalArgumentException("Min(y) must be greater than max y!");
        }
        if (min.getZ() > max.getZ()) {
            throw new IllegalArgumentException("Min(z) must be greater than max z!");
        }
    }

    public static void validateStreamArgs(final Vector3i min, final Vector3i max, final Vector3i existingMin, final Vector3i existingMax, final StreamOptions options) {
        VolumeStreamUtils.validateStreamArgs(min, max, options);
        if (existingMin.compareTo(Objects.requireNonNull(min, "Minimum coordinates cannot be null!")) < 0) {
            throw new IllegalArgumentException(String.format("Minimum %s cannot be lower than the current minimum coordinates: %s", min, existingMin));
        }
        if (existingMax.compareTo(Objects.requireNonNull(max, "Minimum coordinates cannot be null!")) < 0) {
            throw new IllegalArgumentException(String.format("Maximum %s cannot be greater than the current maximum coordinates: %s", max, existingMax));
        }
    }

    private interface TriFunction<A, B, C, Out> {
        Out apply(A a, B b, C c);
    }

    private interface QuadFunction<A, B, C, D, Out> {

        Out apply(A a, B b, C c, D d);

        default TriFunction<A, B, C, Out> asTri(final D d) {
            final Supplier<D> dSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier(d, "D");
            return (a, b, c) -> this.apply(a, b, c, dSupplier.get());
        }
    }

    private static QuadFunction<IChunk, ChunkSection, BlockPos, IWorldReader, Biome> chunkSectionBiomeGetter() {
        return ((chunk, chunkSection, pos, world) -> {
            if (chunk.getBiomes() == null) {
                if (chunk instanceof Chunk) {
                    return ((Chunk) chunk).getLevel().getNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
                } else {
                    // Failover to use the World
                    return world.getUncachedNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
                }
            }
            return chunk.getBiomes().getNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
        }
        );
    }

    private static TriFunction<IChunk, ChunkSection, BlockPos, BlockState> chunkSectionBlockStateGetter() {
        return ((chunk, chunkSection, pos) -> chunkSection.getBlockState(
            pos.getX() - (chunk.getPos().x << 4),
            pos.getY() & 15,
            pos.getZ() - (chunk.getPos().z << 4)));
    }

    private static <T> Function<IChunk, Stream<Map.Entry<BlockPos, T>>> getElementByPosition(
        final TriFunction<IChunk, ChunkSection, BlockPos, T> elementAccessor, final Vector3i min,
        final Vector3i max
    ) {
        // Get the mins
        final int minChunkX = min.getX() >> 4;
        final int minXOffset = min.getX() & 15;
        final int minChunkZ = min.getZ() >> 4;
        final int minZOffset = min.getZ() & 15;
        final int minYSection = min.getY() >> 4 << 4;
        final int minYOffset = min.getY() & 15;

        // Now for the maxes
        final int maxChunkX = max.getX() >> 4;
        final int maxXOffset = max.getX() & 15;
        final int maxChunkZ = max.getZ() >> 4;
        final int maxZOffset = max.getZ() & 15;
        final int maxYSection = max.getY() >> 4 << 4;
        final int maxYOffset = max.getY() & 15;

        return chunk -> {
            final ChunkPos pos = chunk.getPos();

            final int xStart = pos.x == minChunkX ? minXOffset : 0;
            final int xEnd = pos.x == maxChunkX ? maxXOffset + 1 : 16; // 16 because IntStream.range is upper range exclusive
            final int zStart = pos.z == minChunkZ ? minZOffset : 0;
            final int zEnd = pos.z == maxChunkZ ? maxZOffset + 1 : 16; // 16 because IntStream.range is upper range exclusive

            final int chunkMinX = pos.x << 4;
            final int chunkMinZ = pos.z << 4;

            return Arrays.stream(chunk.getSections())
                .filter(Objects::nonNull)
                .filter(chunkSection -> chunkSection.bottomBlockY() >= minYSection && chunkSection.bottomBlockY() <= maxYSection)
                .flatMap(
                chunkSection -> IntStream.range(zStart, zEnd)
                    .mapToObj(z -> IntStream.range(xStart, xEnd)
                        .mapToObj(x -> {
                            final int sectionY = chunkSection.bottomBlockY();
                            final int yStart = sectionY == minYSection ? minYOffset : 0;
                            final int yEnd = sectionY == maxYSection ? maxYOffset + 1 : 16; // plus 1 because of IntStream range exclusive
                            return IntStream.range(yStart, yEnd)
                                .mapToObj(y ->
                                    {
                                        final int adjustedX = x + chunkMinX;
                                        final int adjustedY = y + sectionY;
                                        final int adjustedZ = z + chunkMinZ;

                                        final BlockPos blockPos = new BlockPos(adjustedX, adjustedY, adjustedZ);
                                        final T apply = Objects.requireNonNull(elementAccessor.apply(chunk, chunkSection, blockPos), "Element cannot be null");
                                        return new AbstractMap.SimpleEntry<>(blockPos, apply);
                                    }
                                );
                        }))
                    .flatMap(Function.identity())
                    .flatMap(Function.identity())
            );
        };
    }

    @SuppressWarnings({"unchecked"})
    public static <R extends Volume, API, MC, Section, KeyReference> VolumeStream<R, API> generateStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options,
        final R ref,
        final BiConsumer<KeyReference, MC> identityFunction,
        final BiFunction<R, ChunkPos, Section> chunkAccessor,
        final BiFunction<BlockPos, MC, KeyReference> entityToKey,
        final Function<Section, Stream<Map.Entry<BlockPos, MC>>> entityAccessor,
        final BiFunction<KeyReference, R, Tuple<BlockPos, MC>> filteredPositionEntityAccessor
    ) {
        final Supplier<R> worldSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier(ref, "World");
        final BlockPos chunkMin = new BlockPos(min.getX() >> 4, 0, min.getZ() >> 4);
        final BlockPos chunkMax = new BlockPos(max.getX() >> 4, 0, max.getZ() >> 4);

        // Generate the chunk position stream to iterate on, whether they're accessed immediately
        // or lazily is up to the stream options.
        final Stream<ChunkPos> chunkPosStream = IntStream.range(chunkMin.getX(), chunkMax.getX() + 1)
            .mapToObj(x -> IntStream.range(chunkMin.getZ(), chunkMax.getZ() + 1).mapToObj(z -> new ChunkPos(x, z)))
            .flatMap(Function.identity());

        // This effectively creates a weakly referenced object supplier casting the MC variant to the API variant
        // without consideration, assuming the MC variant is always mixed in to implement the API variant.
        // Then constructs the VolumeElement
        final Function<Tuple<BlockPos, MC>, VolumeElement<R, API>> elementGenerator = (tuple) -> {
            final Supplier<API> blockEntitySupplier = VolumeStreamUtils.createWeaklyReferencedSupplier((API) tuple.getB(), "Element");
            final Vector3i blockEntityPos = VecHelper.toVector3i(tuple.getA());
            return VolumeElement.of(worldSupplier, blockEntitySupplier, blockEntityPos);
        };
        // Fairly trivial, but just acts as a filter and provides the set of filtered references back to the `poses`
        // passed in. This effectively builds the set of key references by their key, usually passing the entity
        // to the identity function whether the entity is to be "cloned" or merely retained by key. This is useful
        // compared to a traditional filter operation since the identity function renders the entity completely
        // separated from the volume target in the event of transformational operations being run on the VolumeStream
        // itself.
        final BiConsumer<Map.Entry<BlockPos, MC>, Set<KeyReference>> entryConsumer = (entry, poses) -> {
            final BlockPos pos = entry.getKey();
            final Vector3i v = VecHelper.toVector3i(pos);
            final KeyReference keyRef = entityToKey.apply(pos, entry.getValue());
            poses.add(keyRef);
            identityFunction.accept(keyRef, entry.getValue());
        };
        // The stream of filtered key references, whether they're BlockPos or UUID,
        // depending on how the stream is being constructed, (immediate loading or not)
        // the positions can be dynamically generated by a stream, or can be pre-calculated
        // and offered as a pre-initialized collection of keys.
        final Stream<KeyReference> filteredPosStream;
        if (options.loadingStyle().immediateLoading()) {
            final Set<KeyReference> availableTileEntityPositions = new LinkedHashSet<>();
            chunkPosStream
                .map(pos -> chunkAccessor.apply(ref, pos))
                .map(entityAccessor)
                .forEach((map) -> map.forEach(entry -> entryConsumer.accept(entry, availableTileEntityPositions)));
            filteredPosStream = availableTileEntityPositions.stream();
        } else {
            // This is where the entirety of stream lazy evaluation occurs:
            // Since we're operating on the chunk positions, we generate the Stream of keys
            // for each position, which in turn generate their filtered lists on demand.
            filteredPosStream = chunkPosStream
                .flatMap(chunkPos -> {
                    final Set<KeyReference> blockEntityPoses = new LinkedHashSet<>();
                    entityAccessor.apply(chunkAccessor.apply(ref, chunkPos))
                        .forEach(entry -> entryConsumer.accept(entry, blockEntityPoses));
                    return blockEntityPoses.stream();
                });
        }
        // And finally, the complete stream turning objects into VolumeElements.
        final Stream<VolumeElement<R, API>> volumeStreamBacker = filteredPosStream
            .map(pos -> filteredPositionEntityAccessor.apply(pos, ref))
            .filter(tuple -> Objects.nonNull(tuple.getB()))
            .map(elementGenerator);
        return new SpongeVolumeStream<>(volumeStreamBacker, worldSupplier);
    }

}
