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
package org.spongepowered.common.event.tracking.context.transaction.world;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.common.accessor.world.damagesource.CombatTrackerAccessor;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.volume.VolumeStreamUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class EntityPerformingDropsTransaction extends WorldBasedTransaction<HarvestEntityEvent> {

    private @MonotonicNonNull Supplier<ServerLevel> worldSupplier;
    final Entity destroyingEntity;
    private @MonotonicNonNull CompoundTag entityTag;
    private @MonotonicNonNull Supplier<Optional<DamageSource>> lastAttacker;

    public EntityPerformingDropsTransaction(final Entity destroyingEntity) {
        super(TransactionTypes.ENTITY_DEATH_DROPS.get(), ((org.spongepowered.api.world.server.ServerWorld) destroyingEntity.level()).key());
        this.destroyingEntity = destroyingEntity;
    }

    @Override
    protected void captureState() {
        super.captureState();
        final Entity entity = this.destroyingEntity;
        this.worldSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier((ServerLevel) entity.level(), "ServerLevel");

        final CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        this.entityTag = tag;

        final @Nullable DamageSource lastAttacker;
        if (entity instanceof LivingEntity) {
            final List<CombatEntry> entries = ((CombatTrackerAccessor) ((LivingEntity) entity).getCombatTracker()).accessor$entries();
            if (!entries.isEmpty()) {
                final CombatEntry entry = entries.get(entries.size() - 1);
                lastAttacker = entry.source();
            } else {
                lastAttacker = null;
            }
        } else {
            lastAttacker = null;
        }
        final WeakReference<@Nullable DamageSource> ref = new WeakReference<>(lastAttacker);
        this.lastAttacker = () -> {
            final @Nullable DamageSource damageSource = ref.get();
            // Yes, I know, we're effectively
            if (damageSource == null) {
                return Optional.empty();
            }
            return Optional.of(damageSource);
        };
    }

    @Override
    public Optional<AbsorbingFlowStep> parentAbsorber() {
        return Optional.of((ctx, tx) -> tx.acceptEntityDrops(this.destroyingEntity));
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        final @Nullable GameTransaction<@NonNull ?> parent
    ) {
        return Optional.of((context, stackFrame) -> {
            stackFrame.pushCause(this.destroyingEntity);
            this.lastAttacker.get()
                .ifPresent(attacker -> stackFrame.addContext(EventContextKeys.LAST_DAMAGE_SOURCE, (org.spongepowered.api.event.cause.entity.damage.source.DamageSource) attacker));
        });
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {

    }

    @Override
    public boolean acceptEntityDrops(
        final Entity entity
    ) {
        return this.destroyingEntity == entity;
    }

    @Override
    public boolean isUnbatchable() {
        return true;
    }

    @Override
    public Optional<HarvestEntityEvent> generateEvent(
        final PhaseContext<@NonNull ?> context, final @Nullable GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<HarvestEntityEvent>> gameTransactions,
        final Cause currentCause
    ) {
        return Optional.of(SpongeEventFactory.createHarvestEntityEvent(currentCause, (org.spongepowered.api.entity.Entity) this.destroyingEntity));
    }

    @Override
    public void restore(PhaseContext<?> context, HarvestEntityEvent event) {
        // TODO this is actually respawning the entity A LOT which is then dying immediately again
//        @Nullable final Entity spawn = this.destroyingEntity.getType()
//                .spawn(this.worldSupplier.get(), null, this.destroyingEntity.blockPosition(), MobSpawnType.COMMAND, false, false);
//        if (spawn != null) {
//            spawn.load(this.entityTag);
//        }
    }

    @Override
    public boolean markCancelledTransactions(
        final HarvestEntityEvent event,
        final ImmutableList<? extends GameTransaction<HarvestEntityEvent>> gameTransactions
    ) {
        return false;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EntityPerformingDropsTransaction.class.getSimpleName() + "[", "]")
            .add("destroyingEntity=" + this.destroyingEntity)
            .add("lastAttacker=" + this.lastAttacker)
            .add("worldKey=" + this.worldKey)
            .add("cancelled=" + this.cancelled)
            .toString();
    }
}
