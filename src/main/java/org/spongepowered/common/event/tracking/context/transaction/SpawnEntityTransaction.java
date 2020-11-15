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

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public final class SpawnEntityTransaction extends GameTransaction<SpawnEntityEvent> {

    final Supplier<ServerWorld> worldSupplier;
    final CompoundNBT entityTag;
    final Entity entityToSpawn;
    final Vector3d originalPosition;
    final Supplier<SpawnType> deducedSpawnType;

    public static final class DummySnapshot {
        final Vector3d originalPosition;
        final CompoundNBT entityTag;
        final Supplier<ServerWorld> worldSupplier;

        public DummySnapshot(final Vector3d originalPosition, final CompoundNBT entityTag,
            final Supplier<ServerWorld> worldSupplier
        ) {
            this.originalPosition = originalPosition;
            this.entityTag = entityTag;
            this.worldSupplier = worldSupplier;
        }
    }

    SpawnEntityTransaction(final Supplier<ServerWorld> worldSupplier, final Entity entityToSpawn,
        final Supplier<SpawnType> deducedSpawnType
    ) {
        super(TransactionType.SPAWN_ENTITY);
        this.worldSupplier = worldSupplier;
        this.entityToSpawn = entityToSpawn;
        this.entityTag = entityToSpawn.writeWithoutTypeId(new CompoundNBT());
        this.originalPosition = new Vector3d(entityToSpawn.getPosX(), entityToSpawn.getPosY(), entityToSpawn.getPosZ());
        this.deducedSpawnType = deducedSpawnType;
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        final @Nullable GameTransaction<@NonNull ?> parent
    ) {
        if (parent instanceof ChangeBlock) {
            return Optional.of(((phaseContext, stackFrame) -> {
                stackFrame.pushCause(((ChangeBlock) parent).original);
                stackFrame.addContext(EventContextKeys.BLOCK_TARGET, ((ChangeBlock) parent).original);
            }));
        }
        return Optional.empty();
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SpawnEntityEvent generateEvent(final PhaseContext<@NonNull ?> context,
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
        return ((IPhaseState) context.state).createSpawnEvent(context, parent, collect, currentCause);
    }

    @Override
    public void restore() {
        this.worldSupplier.get().removeEntity(this.entityToSpawn);
    }

    @Override
    public boolean markCancelledTransactions(final SpawnEntityEvent event,
        final ImmutableList<? extends GameTransaction<SpawnEntityEvent>> gameTransactions
    ) {
        return false;
    }

    @Override
    public void postProcessEvent(final PhaseContext<@NonNull ?> context, final SpawnEntityEvent event) {

    }
}
