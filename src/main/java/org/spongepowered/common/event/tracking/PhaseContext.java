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

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * Similar to {@link Cause} except it can be built continuously
 * and retains no real side effects. Strictly speaking this object
 * exists to avoid confusion between what is suggested to be a
 * {@link Cause} for an {@link Event} versus the context of which
 * a {@link IPhaseState} is being completed with.
 */
public final class PhaseContext {

    private boolean isCompleted = false;
    private final LinkedHashSet<NamedCause> contextObjects = new LinkedHashSet<>();
    @Nullable private Cause cause = null;

    public static PhaseContext start() {
        return new PhaseContext();
    }

    public PhaseContext add(NamedCause namedCause) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(namedCause);
        return this;
    }

    public PhaseContext addCaptures() {
        add(NamedCause.of(TrackingHelper.CAPTURED_BLOCKS, new CapturedBlocksSupplier()));
        add(NamedCause.of(TrackingHelper.CAPTURED_ITEMS, new CapturedItemsSupplier()));
        add(NamedCause.of(TrackingHelper.CAPTURED_ENTITIES, new CapturedEntitiesSupplier()));
        add(NamedCause.of(TrackingHelper.INVALID_TRANSACTIONS, new InvalidTransactionSupplier()));
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
        return firstNamed(TrackingHelper.CAPTURED_ENTITIES, CapturedEntitiesSupplier.class).map(CapturedEntitiesSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<Entity>> getCapturedEntitySupplier() {
        return firstNamed(TrackingHelper.CAPTURED_ENTITIES, (Class<CapturedSupplier<Entity>>) (Class<?>) CapturedEntitiesSupplier.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<List<Entity>> getCapturedItems() {
        return firstNamed(TrackingHelper.CAPTURED_ITEMS, CapturedItemsSupplier.class).map(CapturedItemsSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<Entity>> getCapturedItemsSupplier() {
        return firstNamed(TrackingHelper.CAPTURED_ENTITIES, (Class<CapturedSupplier<Entity>>) (Class<?>) CapturedItemsSupplier.class);
    }


    @SuppressWarnings("unchecked")
    public Optional<List<Transaction<BlockSnapshot>>> getInvalidTransactions() {
        return firstNamed(TrackingHelper.INVALID_TRANSACTIONS, InvalidTransactionSupplier.class)
                .flatMap(supplier -> Optional.ofNullable(supplier.get()));
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<Transaction<BlockSnapshot>>> getInvalidTransactionSupplier() {
        return firstNamed(TrackingHelper.INVALID_TRANSACTIONS, (Class<CapturedSupplier<Transaction<BlockSnapshot>>>) (Class<?>) InvalidTransactionSupplier.class);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<BlockSnapshot>> getCapturedBlocks() {
        return firstNamed(TrackingHelper.CAPTURED_BLOCKS, CapturedBlocksSupplier.class).map(CapturedBlocksSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public Optional<CapturedSupplier<BlockSnapshot>> getCapturedBlockSupplier() {
        return this.firstNamed(TrackingHelper.CAPTURED_BLOCKS, (Class<CapturedSupplier<BlockSnapshot>>) (Class<?>) CapturedBlocksSupplier.class);
    }

    public Cause toCause() {
        checkState(this.isCompleted, "Cannot get a cuase for an incomplete PhaseContext!");
        if (this.cause == null) {
            this.cause = Cause.of(this.contextObjects);
        }
        return this.cause;
    }

    private PhaseContext() {
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

        public final void ifPresent(Consumer<List<T>> consumer) {
            if (this.captured != null) {
                consumer.accept(this.captured);
            }
        }

        public final List<T> orElse(List<T> list) {
            return this.captured == null ? list : this.captured;
        }

        public final List<T> orEmptyList() {
            return orElse(Collections.emptyList());
        }

        @Nullable
        public final <U> U map(Function<List<T>, ? extends U> function) {
            return this.captured == null ? null : function.apply(this.captured);
        }
    }

    public static final class InvalidTransactionSupplier extends CapturedSupplier<Transaction<BlockSnapshot>> {

        InvalidTransactionSupplier() {
        }
    }

    public static final class CapturedItemsSupplier extends CapturedSupplier<Entity> {

        CapturedItemsSupplier() {
        }
    }

    public static final class CapturedBlocksSupplier extends CapturedSupplier<BlockSnapshot> {

        CapturedBlocksSupplier() {
        }
    }

    public static final class CapturedEntitiesSupplier extends CapturedSupplier<Entity> {

        CapturedEntitiesSupplier() {
        }
    }

}
