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
package org.spongepowered.common.mixin.api.minecraft.world.level;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.ProtoWorld;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.level.chunk.ChunkBiomeContainerAccessor;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3i;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.storage.LevelData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Mixin(LevelAccessor.class)
@Implements(@Interface(iface = ProtoWorld.class, prefix = "protoWorld$", remap = Remap.NONE))
public interface LevelAccessorMixin_API {

    //@formatter:off
    @Shadow boolean shadow$hasChunk(int p_217354_1_, int p_217354_2_);
    @Shadow Random shadow$getRandom();
    @Shadow LevelData shadow$getLevelData();
    //@formatter:on

    // MutableBiomeVolume

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    default boolean protoWorld$setBiome(final int x, final int y, final int z, final org.spongepowered.api.world.biome.Biome biome) {
        Objects.requireNonNull(biome, "biome");

        final ChunkAccess iChunk = ((LevelReader) this).getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, true);
        if (iChunk == null) {
            return false;
        }
        if (iChunk instanceof ProtoChunk) {
            return ((ProtoChunk) iChunk).setBiome(x, y, z, biome);
        } else {
            final Biome[] biomes = ((ChunkBiomeContainerAccessor) iChunk.getBiomes()).accessor$biomes();

            final int maskedX = x & ChunkBiomeContainer.HORIZONTAL_MASK;
            final int maskedY = Mth.clamp(y, 0, ChunkBiomeContainer.VERTICAL_MASK);
            final int maskedZ = z & ChunkBiomeContainer.HORIZONTAL_MASK;

            final int WIDTH_BITS = ChunkBiomeContainerAccessor.accessor$WIDTH_BITS();
            final int posKey = maskedY << WIDTH_BITS + WIDTH_BITS | maskedZ << WIDTH_BITS | maskedX;
            biomes[posKey] = (Biome) (Object) biome;

            return true;
        }
    }

    // Volume

    default Vector3i protoWorld$blockMin() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    default Vector3i protoWorld$blockMax() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    default Vector3i protoWorld$blockSize() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    default boolean protoWorld$containsBlock(final int x, final int y, final int z) {
        return this.shadow$hasChunk(x >> 4, z >> 4);
    }

    default boolean protoWorld$isAreaAvailable(final int x, final int y, final int z) {
        return this.shadow$hasChunk(x >> 4, z >> 4);
    }

    // EntityVolume

    default Optional<Entity> protoWorld$entity(final UUID uuid) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    // RandomProvider

    @Intrinsic
    default Random protoWorld$random() {
        return this.shadow$getRandom();
    }

    // ProtoWorld

    default Difficulty protoWorld$difficulty() {
        return (Difficulty) (Object) this.shadow$getLevelData().getDifficulty();
    }

    default Collection<Entity> protoWorld$spawnEntities(final Iterable<? extends Entity> entities) {
        Objects.requireNonNull(entities, "entities");

        final List<Entity> entitiesToSpawn = new ArrayList<>();
        entities.forEach(entitiesToSpawn::add);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(PhaseTracker.getCauseStackManager().currentCause(), entitiesToSpawn);
        if (Sponge.eventManager().post(event)) {
            return Collections.emptyList();
        }
        for (final Entity entity : event.entities()) {
            EntityUtil.processEntitySpawn(entity, Optional::empty, e -> e.level.addFreshEntity(e));
        }
        return Collections.unmodifiableCollection(new ArrayList<>(event.entities()));
    }

    default boolean protoWorld$spawnEntity(final Entity entity) {
        return ((LevelAccessor) this).addFreshEntity((net.minecraft.world.entity.Entity) Objects.requireNonNull(entity, "entity"));
    }

    // MutableBlockVolume

    default boolean protoWorld$setBlock(final int x, final int y, final int z, final org.spongepowered.api.block.BlockState blockState, final BlockChangeFlag flag) {
        Objects.requireNonNull(blockState, "blockState");
        Objects.requireNonNull(flag, "flag");

        if (!Level.isInWorldBounds(new BlockPos(x, y, z))) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), Constants.World.BLOCK_MIN, Constants.World.BLOCK_MAX);
        }
        try (final @Nullable PhaseContext<@NonNull ?> context = PluginPhase.State.BLOCK_WORKER.switchIfNecessary(PhaseTracker.SERVER)) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return ((LevelAccessor) this).setBlock(new BlockPos(x, y, z), (BlockState) blockState, ((SpongeBlockChangeFlag) flag).getRawFlag());
        }
    }

}
