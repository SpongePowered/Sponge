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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.EntitySection;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.api.world.volume.game.Region;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.accessor.client.multiplayer.ClientLevelAccessor;
import org.spongepowered.common.accessor.server.level.ServerLevelAccessor;
import org.spongepowered.common.accessor.world.level.block.entity.BlockEntityAccessor;
import org.spongepowered.common.accessor.world.level.entity.PersistentEntitySectionManagerAccessor;
import org.spongepowered.common.accessor.world.level.entity.TransientEntitySectionManagerAccessor;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.volume.buffer.blockentity.ObjectArrayMutableBlockEntityBuffer;
import org.spongepowered.common.world.volume.buffer.entity.ObjectArrayMutableEntityBuffer;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
        return VolumeStreamUtils.filterPositions(tuple -> tuple.first().toInt(), min, max);
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

    public static <R extends Region<R>> BiFunction<R, ChunkPos, @Nullable ChunkAccess> getChunkAccessorByStatus(
        final LevelReader worldReader,
        final boolean shouldGenerate
    ) {
        final Supplier<LevelReader> readerSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier(worldReader, "IWorldReader");
        return (world, chunkPos) -> {
            final ChunkStatus chunkStatus = shouldGenerate
                ? ChunkStatus.FULL
                : ChunkStatus.EMPTY;
            final @Nullable ChunkAccess ichunk = readerSupplier.get().getChunk(chunkPos.x, chunkPos.z, chunkStatus, shouldGenerate);
            if (shouldGenerate) {
                Objects.requireNonNull(ichunk, "Chunk was expected to load fully and generate, but somehow got a null chunk!");
            }
            if (ichunk instanceof ImposterProtoChunk) {
                return ((ImposterProtoChunk) ichunk).getWrapped();
            }
            return (ChunkAccess) ichunk;
        };
    }

    public static Function<ChunkAccess, Stream<Map.Entry<BlockPos, Biome>>> getBiomesForChunkByPos(final LevelReader reader, final Vector3i min,
        final Vector3i max
    ) {
        return VolumeStreamUtils.getElementByPosition(VolumeStreamUtils.chunkSectionBiomeGetter().asTri(reader), min, max);
    }

    public static Function<ChunkAccess, Stream<Map.Entry<BlockPos, BlockState>>> getBlockStatesForSections(
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

    @SuppressWarnings("unchecked")
    public static Stream<Map.Entry<BlockPos, net.minecraft.world.entity.Entity>> getEntitiesFromChunk(
        final Vector3i min, final Vector3i max, final LevelChunk chunk
    ) {
        if (chunk.getLevel() instanceof ServerLevel) {
            return ((PersistentEntitySectionManagerAccessor<Entity>) ((ServerLevelAccessor) chunk.getLevel()).accessor$getEntityManager()).accessor$sectionStorage()
                .getExistingSectionsInChunk(SectionPos.of(chunk.getPos(), 0).asLong())
                .flatMap(EntitySection::getEntities)
                .filter(entity -> VecHelper.inBounds(entity.blockPosition(), min, max))
                .map(entity -> new AbstractMap.SimpleEntry<>(entity.blockPosition(), entity));
        } else if (Sponge.isClientAvailable() && chunk.getLevel() instanceof ClientLevel) {
            return ((TransientEntitySectionManagerAccessor<Entity>) ((ClientLevelAccessor) chunk.getLevel()).accessor$getEntityStorage())
                .accessor$sectionStorage()
                .getExistingSectionsInChunk(SectionPos.of(chunk.getPos(), 0).asLong())
                .flatMap(EntitySection::getEntities)
                .filter(entity -> VecHelper.inBounds(entity.blockPosition(), min, max))
                .map(entity -> new AbstractMap.SimpleEntry<>(entity.blockPosition(), entity));
        }
        throw new UnsupportedOperationException("Unknown Chunk Level Type");
    }

    @NotNull
    public static BiConsumer<UUID, net.minecraft.world.entity.Entity> getOrCloneEntityWithVolume(
        final boolean shouldCarbonCopy,
        final @MonotonicNonNull ObjectArrayMutableEntityBuffer backingVolume,
        final Level level
    ) {
        return shouldCarbonCopy ? (pos, entity) -> {
            final CompoundTag nbt = new CompoundTag();
            entity.save(nbt);
            final net.minecraft.world.entity.@Nullable Entity cloned = entity.getType().create(level);
            Objects.requireNonNull(
                cloned,
                () -> String.format(
                    "EntityType[%s] creates a null Entity!",
                    net.minecraft.world.entity.EntityType.getKey(entity.getType())
                )
            ).load(nbt);
            backingVolume.spawnEntity((org.spongepowered.api.entity.Entity) cloned);
        } : (pos, tile) -> {
        };
    }

    @NotNull
    public static BiConsumer<BlockPos, net.minecraft.world.level.block.entity.BlockEntity> getBlockEntityOrCloneToBackingVolume(
        final boolean shouldCarbonCopy, final ObjectArrayMutableBlockEntityBuffer backingVolume, final @Nullable Level level
    ) {
        return shouldCarbonCopy ? (pos, tile) -> {
            final CompoundTag nbt = tile.save(new CompoundTag());
            final BlockState state = tile.getBlockState();
            final net.minecraft.world.level.block.entity.@Nullable BlockEntity cloned = tile.getType().create(pos, state);
            Objects.requireNonNull(
                cloned,
                () -> String.format(
                    "TileEntityType[%s] creates a null TileEntity!", BlockEntityType.getKey(tile.getType()))
            ).load(nbt);

            if (level != null) {
                ((BlockEntityAccessor) cloned).accessor$level(level);
            }
            backingVolume.addBlockEntity(pos.getX(), pos.getY(), pos.getZ(), (BlockEntity) cloned);
        } : (pos, tile) -> {
        };
    }

    public static Predicate<net.minecraft.world.entity.Entity> apiToImplPredicate(final Predicate<? super org.spongepowered.api.entity.Entity> filter) {
        return entity -> entity instanceof Entity && filter.test((org.spongepowered.api.entity.Entity) entity);
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

    private static QuadFunction<ChunkAccess, LevelChunkSection, BlockPos, LevelReader, Biome> chunkSectionBiomeGetter() {
        return ((chunk, chunkSection, pos, world) -> {
            if (chunk.getBiomes() == null) {
                if (chunk instanceof LevelChunk) {
                    return ((LevelChunk) chunk).getLevel().getNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
                } else {
                    // Failover to use the World
                    return world.getUncachedNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
                }
            }
            return chunk.getBiomes().getNoiseBiome(pos.getX(), pos.getY(), pos.getZ());
        }
        );
    }

    private static TriFunction<ChunkAccess, LevelChunkSection, BlockPos, BlockState> chunkSectionBlockStateGetter() {
        return ((chunk, chunkSection, pos) -> chunkSection.getBlockState(
            pos.getX() - (chunk.getPos().x << 4),
            pos.getY() & 15,
            pos.getZ() - (chunk.getPos().z << 4)));
    }

    private static <T> Function<ChunkAccess, Stream<Map.Entry<BlockPos, T>>> getElementByPosition(
        final TriFunction<ChunkAccess, LevelChunkSection, BlockPos, T> elementAccessor, final Vector3i min,
        final Vector3i max
    ) {
        // Build the min and max
        final ChunkCursor minCursor = new ChunkCursor(min);
        final ChunkCursor maxCursor = new ChunkCursor(max);

        return chunk -> {
            final ChunkPos pos = chunk.getPos();

            final int xStart = pos.x == minCursor.chunkX ? minCursor.xOffset : 0;
            final int xEnd = pos.x == maxCursor.chunkX ? maxCursor.xOffset + 1 : 16; // 16 because IntStream.range is upper range exclusive
            final int zStart = pos.z == minCursor.chunkZ ? minCursor.zOffset : 0;
            final int zEnd = pos.z == maxCursor.chunkZ ? maxCursor.zOffset + 1 : 16; // 16 because IntStream.range is upper range exclusive

            final int chunkMinX = pos.x << 4;
            final int chunkMinZ = pos.z << 4;

            return Arrays.stream(chunk.getSections())
                .filter(Objects::nonNull)
                .filter(chunkSection -> chunkSection.bottomBlockY() >= minCursor.ySection && chunkSection.bottomBlockY() <= maxCursor.ySection)
                .flatMap(
                chunkSection -> IntStream.range(zStart, zEnd)
                    .mapToObj(z -> IntStream.range(xStart, xEnd)
                        .mapToObj(x -> {
                            final int sectionY = chunkSection.bottomBlockY();
                            final int yStart = sectionY == minCursor.ySection ? minCursor.yOffset : 0;
                            final int yEnd = sectionY == maxCursor.ySection ? maxCursor.yOffset + 1 : 16; // plus 1 because of IntStream range exclusive
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
        final Stream<Section> sectionStream = IntStream.range(chunkMin.getX(), chunkMax.getX() + 1)
            .mapToObj(x -> IntStream.range(chunkMin.getZ(), chunkMax.getZ() + 1).mapToObj(z -> new ChunkPos(x, z)))
            .flatMap(Function.identity())
            .map(pos -> chunkAccessor.apply(ref, pos));

        return VolumeStreamUtils.generateStreamInternal(
            options, ref, identityFunction, entityToKey, entityAccessor, filteredPositionEntityAccessor, worldSupplier,
            sectionStream
        );
    }


    public static <R extends Volume, API, MC, Section, KeyReference> VolumeStream<R, API> generateStream(
        final StreamOptions options,
        final R ref,
        final Section section,
        final Function<Section, Stream<Map.Entry<BlockPos, MC>>> entityAccessor,
        final BiConsumer<KeyReference, MC> identityFunction,
        final BiFunction<BlockPos, MC, KeyReference> entityToKey,
        final BiFunction<KeyReference, R, Tuple<BlockPos, @Nullable MC>> filteredPositionEntityAccessor

    ) {
        final Supplier<R> worldSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier(ref, "World");
        // Generate the chunk position stream to iterate on, whether they're accessed immediately
        // or lazily is up to the stream options.
        final Stream<Section> sectionStream = Stream.of(section);
        return VolumeStreamUtils.generateStreamInternal(
            options,
            ref,
            identityFunction,
            entityToKey,
            entityAccessor,
            filteredPositionEntityAccessor,
            worldSupplier,
            sectionStream
        );
    }

    @SuppressWarnings("unchecked")
    private static <R extends Volume, API, MC, Section, KeyReference> SpongeVolumeStream<R, API> generateStreamInternal(
        final StreamOptions options, final R ref, final BiConsumer<KeyReference, MC> identityFunction,
        final BiFunction<BlockPos, MC, KeyReference> entityToKey,
        final Function<Section, Stream<Map.Entry<BlockPos, MC>>> entityAccessor,
        final BiFunction<KeyReference, R, @Nullable Tuple<BlockPos, @Nullable MC>> filteredPositionEntityAccessor,
        final Supplier<R> worldSupplier,
        final Stream<Section> sectionStream
    ) {
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
            sectionStream
                .map(entityAccessor)
                .forEach((map) -> map.forEach(entry -> entryConsumer.accept(entry, availableTileEntityPositions)));
            filteredPosStream = availableTileEntityPositions.stream();
        } else {
            // This is where the entirety of stream lazy evaluation occurs:
            // Since we're operating on the chunk positions, we generate the Stream of keys
            // for each position, which in turn generate their filtered lists on demand.
            filteredPosStream = sectionStream
                .flatMap(chunk -> {
                    final Set<KeyReference> blockEntityPoses = new LinkedHashSet<>();
                    entityAccessor.apply(chunk)
                        .forEach(entry -> entryConsumer.accept(entry, blockEntityPoses));
                    return blockEntityPoses.stream();
                });
        }
        // And finally, the complete stream turning objects into VolumeElements.
        final Stream<VolumeElement<R, API>> volumeStreamBacker = filteredPosStream
            .map(pos -> filteredPositionEntityAccessor.apply(pos, ref))
            .filter(Objects::nonNull)
            .filter(tuple -> Objects.nonNull(tuple.getB()))
            .map(elementGenerator);
        return new SpongeVolumeStream<>(volumeStreamBacker, worldSupplier);
    }

}
