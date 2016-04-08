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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.InternalNamedCauses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Similar to {@link Cause} except it can be built continuously
 * and retains no real side effects. Strictly speaking this object
 * exists to avoid confusion between what is suggested to be a
 * {@link Cause} for an {@link Event} versus the context of which
 * a {@link IPhaseState} is being completed with.
 */
public class PhaseContext {

    private boolean isCompleted = false;
    private final ArrayList<NamedCause> contextObjects = new ArrayList<>(10);
    @Nullable private Cause cause = null;

    public static PhaseContext start() {
        return new PhaseContext();
    }

    public PhaseContext add(@Nullable NamedCause namedCause) {
        if (namedCause == null) {
            return this;
        }
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(namedCause);
        return this;
    }

    public PhaseContext addCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, new CapturedBlocksSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEMS, new CapturedItemsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, new CapturedEntitiesSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.INVALID_TRANSACTIONS, new InvalidTransactionSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, new BlockItemDropsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITY_DROPS, new EntityItemDropsSupplier()));
        return this;
    }

    public PhaseContext addEntityCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, new CapturedEntitiesSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEMS, new CapturedItemsSupplier()));
        return this;
    }

    public PhaseContext complete() {
        this.isCompleted = true;
        return this;
    }

    public boolean isComplete() {
        return this.isCompleted;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> first(Class<T> tClass) {
        for (NamedCause cause : this.contextObjects) {
            if (tClass.isInstance(cause.getCauseObject())) {
                return Optional.of((T) cause.getCauseObject());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> firstNamed(String name, Class<T> tClass) {
        for (NamedCause cause : this.contextObjects) {
            if (cause.getName().equalsIgnoreCase(name) && tClass.isInstance(cause.getCauseObject())) {
                return Optional.of((T) cause.getCauseObject());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<List<Entity>> getCapturedEntities() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, CapturedEntitiesSupplier.class).map(CapturedEntitiesSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<Entity>> getCapturedEntitySupplier() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, (Class<CapturedSupplier<Entity>>) (Class<?>) CapturedEntitiesSupplier.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<List<Entity>> getCapturedItems() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ITEMS, CapturedItemsSupplier.class).map(CapturedItemsSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<Entity>> getCapturedItemsSupplier() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ITEMS, (Class<CapturedSupplier<Entity>>) (Class<?>) CapturedItemsSupplier.class);
    }


    @SuppressWarnings("unchecked")
    public Optional<List<Transaction<BlockSnapshot>>> getInvalidTransactions() {
        return firstNamed(InternalNamedCauses.Tracker.INVALID_TRANSACTIONS, InvalidTransactionSupplier.class).map(InvalidTransactionSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<Transaction<BlockSnapshot>>> getInvalidTransactionSupplier() {
        return firstNamed(InternalNamedCauses.Tracker.INVALID_TRANSACTIONS, (Class<CapturedSupplier<Transaction<BlockSnapshot>>>) (Class<?>) InvalidTransactionSupplier.class);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<BlockSnapshot>> getCapturedBlocks() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, CapturedBlocksSupplier.class).map(CapturedBlocksSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<BlockSnapshot>> getCapturedBlockSupplier() {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, (Class<CapturedSupplier<BlockSnapshot>>) (Class<?>) CapturedBlocksSupplier.class);
    }

    public Optional<Multimap<BlockPos, ItemStack>> getCapturedBlockDrops() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, BlockItemDropsSupplier.class).map(CapturedMultiMapSupplier::get);
    }

    public Optional<Collection<ItemStack>> getCapturedBlockDrops(BlockPos position) {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, BlockItemDropsSupplier.class).map(supplier -> supplier.get().get(position));
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedMultiMapSupplier<BlockPos, ItemStack>> getBlockDropSupplier() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, (Class<CapturedMultiMapSupplier<BlockPos, ItemStack>>) (Class<?>) BlockItemDropsSupplier.class);
    }

    public Optional<Multimap<UUID, ItemStack>> getCapturedEntityDrops() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITY_DROPS, EntityItemDropsSupplier.class).map(CapturedMultiMapSupplier::get);
    }

    public Optional<Collection<ItemStack>> getCapturedEntityDrops(UUID position) {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITY_DROPS, EntityItemDropsSupplier.class).map(supplier -> supplier.get().get(position));
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedMultiMapSupplier<UUID, ItemStack>> getCapturedEntityDropSupplier() {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITY_DROPS, (Class<CapturedMultiMapSupplier<UUID, ItemStack>>) (Class<?>) EntityItemDropsSupplier.class);
    }

    public Cause toCause() {
        checkState(this.isCompleted, "Cannot get a cuase for an incomplete PhaseContext!");
        if (this.cause == null) {
            this.cause = Cause.of(this.contextObjects);
        }
        return this.cause;
    }

    public void forEach(Consumer<NamedCause> consumer) {
        this.contextObjects.forEach(consumer::accept);
    }

    public PrettyPrinter populatePrinter(PrettyPrinter printer) {
        printer.table(" %s : %s%n");
        for (NamedCause cause : this.contextObjects) {
            printer.tr(cause.getName(), cause.getCauseObject());
        }
        return printer;
    }

    PhaseContext() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isCompleted, this.contextObjects, this.cause);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PhaseContext other = (PhaseContext) obj;
        return Objects.equals(this.isCompleted, other.isCompleted)
               && Objects.equals(this.contextObjects, other.contextObjects)
               && Objects.equals(this.cause, other.cause);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("isCompleted", this.isCompleted)
                .add("contextObjects", this.contextObjects)
                .add("cause", this.cause)
                .toString();
    }

    public static abstract class CapturedMultiMapSupplier<K, V> implements Supplier<Multimap<K, V>> {

        @Nullable private Multimap<K, V> captured;

        CapturedMultiMapSupplier() {
        }

        @Override
        public Multimap<K, V> get() {
            if (this.captured == null) {
                this.captured = ArrayListMultimap.create();
            }
            return this.captured;
        }

        public final boolean isEmpty() {
            return this.captured == null || this.captured.isEmpty();
        }

        public final void ifPresentAndNotEmpty(Consumer<Multimap<K, V>> consumer) {
            if (this.captured != null && !this.captured.isEmpty()) {
                consumer.accept(this.captured);
            }
        }

        public final Multimap<K, V> orElse(Multimap<K, V> list) {
            return this.captured == null ? list : this.captured;
        }

        public final Stream<V> stream(K key) {
            // authors note: Multimap#get(K) returns an empty collection if there is no mapping.
            return this.captured == null ? Stream.empty() : this.captured.get(key).stream();
        }

        @Nullable
        public final <U> U map(K key, Function<Collection<V>, ? extends U> function) {
            return this.captured == null ? null : function.apply(this.captured.get(key));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.captured);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final CapturedSupplier other = (CapturedSupplier) obj;
            return Objects.equals(this.captured, other.captured);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("Captured", this.captured == null ? 0 : this.captured.size())
                    .toString();
        }
    }

    static class BlockItemDropsSupplier extends CapturedMultiMapSupplier<BlockPos, ItemStack> {

        BlockItemDropsSupplier() {
        }

    }

    static class EntityItemDropsSupplier extends CapturedMultiMapSupplier<UUID, ItemStack> {

        EntityItemDropsSupplier() {
        }
    }


    public static abstract class CapturedSupplier<T> implements Supplier<List<T>> {
        @Nullable private List<T> captured;

        CapturedSupplier() {
        }

        @Override
        public final List<T> get() {
            if (this.captured == null) {
                this.captured = new ArrayList<>();
            }
            return this.captured;
        }

        public final boolean isEmpty() {
            return this.captured == null || this.captured.isEmpty();
        }

        public final void ifPresentAndNotEmpty(Consumer<List<T>> consumer) {
            if (this.captured != null && !this.captured.isEmpty()) {
                consumer.accept(this.captured);
            }
        }

        public final List<T> orElse(List<T> list) {
            return this.captured == null ? list : this.captured;
        }

        public final List<T> orEmptyList() {
            return orElse(Collections.emptyList());
        }

        public final Stream<T> stream() {
            return this.captured == null ? Stream.empty() : this.captured.stream();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.captured);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final CapturedSupplier other = (CapturedSupplier) obj;
            return Objects.equals(this.captured, other.captured);
        }


        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("Captured", this.captured == null ? 0 : this.captured.size())
                    .toString();
        }
    }

    static final class InvalidTransactionSupplier extends CapturedSupplier<Transaction<BlockSnapshot>> {

        InvalidTransactionSupplier() {
        }
    }

    static final class CapturedItemsSupplier extends CapturedSupplier<Entity> {

        CapturedItemsSupplier() {
        }
    }

    static final class CapturedBlocksSupplier extends CapturedSupplier<BlockSnapshot> {

        CapturedBlocksSupplier() {
        }
    }

    static final class CapturedEntitiesSupplier extends CapturedSupplier<Entity> {

        CapturedEntitiesSupplier() {
        }
    }

}
