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
package org.spongepowered.common.event.tracking.context.transaction;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.accessor.server.level.ServerLevelAccessor;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.math.vector.Vector3d;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@DefaultQualifier(NonNull.class)
public final class SpawnEntityTransaction extends GameTransaction<SpawnEntityEvent> {

    final Supplier<ServerLevel> worldSupplier;
    final CompoundTag entityTag;
    final Entity entityToSpawn;
    final Vector3d originalPosition;
    final Supplier<SpawnType> deducedSpawnType;

    public static final class DummySnapshot {
        final Vector3d originalPosition;
        final CompoundTag entityTag;
        final Supplier<ServerLevel> worldSupplier;

        public DummySnapshot(final Vector3d originalPosition, final CompoundTag entityTag,
            final Supplier<ServerLevel> worldSupplier
        ) {
            this.originalPosition = originalPosition;
            this.entityTag = entityTag;
            this.worldSupplier = worldSupplier;
        }
    }

    SpawnEntityTransaction(final Supplier<ServerLevel> worldSupplier, final Entity entityToSpawn,
        final Supplier<SpawnType> deducedSpawnType
    ) {
        super(TransactionTypes.SPAWN_ENTITY.get(), ((org.spongepowered.api.world.server.ServerWorld) worldSupplier.get()).key());
        this.worldSupplier = worldSupplier;
        this.entityToSpawn = entityToSpawn;
        this.entityTag = entityToSpawn.saveWithoutId(new CompoundTag());
        this.originalPosition = new Vector3d(entityToSpawn.getX(), entityToSpawn.getY(), entityToSpawn.getZ());
        this.deducedSpawnType = deducedSpawnType;
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        final @Nullable GameTransaction<@NonNull ?> parent
    ) {
        return Optional.of((context, frame) -> {
            if (parent instanceof ChangeBlock) {
                frame.pushCause(((ChangeBlock) parent).original);
                frame.addContext(EventContextKeys.BLOCK_TARGET, ((ChangeBlock) parent).original);
            }
            frame.addContext(EventContextKeys.SPAWN_TYPE, this.deducedSpawnType);
        });
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

    @Override
    boolean shouldBuildEventAndRestartBatch(
        final GameTransaction<@NonNull ?> pointer, final PhaseContext<@NonNull ?> context
    ) {
        return super.shouldBuildEventAndRestartBatch(pointer, context)
            || this.deducedSpawnType != ((SpawnEntityTransaction) pointer).deducedSpawnType;
    }

    @Override
    public Optional<SpawnEntityEvent> generateEvent(
        final PhaseContext<@NonNull ?> context,
        final @Nullable GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<SpawnEntityEvent>> gameTransactions, final Cause currentCause
    ) {
        final ImmutableList<Tuple<Entity, DummySnapshot>> collect = gameTransactions.stream()
            .map(transaction -> (SpawnEntityTransaction) transaction)
            .map(spawnRequest -> {
                return new Tuple<>(
                    spawnRequest.entityToSpawn,
                    new DummySnapshot(spawnRequest.originalPosition, spawnRequest.entityTag, spawnRequest.worldSupplier)
                );
            }).collect(ImmutableList.toImmutableList());
        return Optional.of(context.createSpawnEvent(parent, collect, currentCause));
    }

    @Override
    public void restore() {
        final ServerLevel serverWorld = this.worldSupplier.get();
        if (((ServerLevelAccessor) serverWorld).accessor$tickingEntities()) {
            // More than likely we could also be needing to remove the entity from both the entities to add
            // and the chunk.
            ((ServerLevelAccessor) serverWorld).accessor$toAddAfterTick().remove(this.entityToSpawn);
            ((ServerLevelAccessor) serverWorld).invoker$removeFromChunk(this.entityToSpawn);
        } else {
            serverWorld.despawn(this.entityToSpawn);
        }
    }

    @Override
    public boolean markCancelledTransactions(final SpawnEntityEvent event,
        final ImmutableList<? extends GameTransaction<SpawnEntityEvent>> gameTransactions
    ) {
        return false;
    }

    @Override
    public void postProcessEvent(final PhaseContext<@NonNull ?> context, final SpawnEntityEvent event) {
        Stream.of(
            context.getNotifier(),
            context.getCreator(),
            context.getSource(ServerPlayer.class).map(ServerPlayer::user)
        )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .ifPresent(creator -> event.entities().forEach(entity -> ((CreatorTrackedBridge) entity).tracked$setCreatorReference(creator)));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpawnEntityTransaction.class.getSimpleName() + "[", "]")
            .add("worldKey=" + this.worldKey)
            .add("entityToSpawn=" + this.entityToSpawn)
            .add("originalPosition=" + this.originalPosition)
            .toString();
    }
}
