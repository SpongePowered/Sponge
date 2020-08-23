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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.tracking.context.CaptureBlockPos;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.BlockChange;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Similar to {@link Cause} except it can be built continuously and retains no
 * real side effects. Strictly speaking this object exists to avoid confusion
 * between what is suggested to be a {@link Cause} for an {@link Event} versus
 * the context of which a {@link IPhaseState} is being completed with.
 */
@SuppressWarnings("unchecked")
@DefaultQualifier(NonNull.class)
public class PhaseContext<P extends PhaseContext<P>> implements AutoCloseable {

    @MonotonicNonNull private static PhaseContext<?> EMPTY;
    protected final PhaseTracker createdTracker;
    @MonotonicNonNull private TransactionalCaptureSupplier transactor;

    /**
     * Default flagged empty PhaseContext that can be used for stubbing in corner cases.
     */
    public static PhaseContext<?> empty() {
        if (PhaseContext.EMPTY == null) {
            PhaseContext.EMPTY = new GeneralizedContext(GeneralPhase.State.COMPLETE, new PhaseTracker()).markEmpty();
        }
        return PhaseContext.EMPTY;
    }



    public final IPhaseState<? extends P> state; // Only temporary to verify the state creation with constructors
    protected boolean isCompleted = false;
    // Only used in hard debugging instances.
    @Nullable private StackTraceElement[] stackTrace;

    // Per block captures (useful for things like explosions to capture multiple targets at a time)
    @Nullable CaptureBlockPos captureBlockPos;

    // General
    @Nullable protected User creator;
    @Nullable protected User notifier;
    private boolean allowsBlockEvents = true; // Defaults to allow block events
    private boolean allowsEntityEvents = true;
    private boolean allowsBulkBlockCaptures = true; // Defaults to allow block captures
    private boolean allowsBulkEntityCaptures = true;
    @Nullable Deque<CauseStackManager.StackFrame> usedFrame;

    @Nullable private Object source;

    public P source(final Object owner) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.source = owner;
        return (P) this;
    }

    public P creator(final Supplier<Optional<User>> supplier) {
        supplier.get().ifPresent(this::creator);
        return (P) this;
    }

    public P creator(final User owner) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        if (this.creator != null) {
            throw new IllegalStateException("Owner for this phase context is already set!");
        }
        this.creator = checkNotNull(owner, "Owner cannot be null!");
        return (P) this;
    }

    public P notifier(final Supplier<Optional<User>> supplier) {
        supplier.get().ifPresent(this::notifier);
        return (P) this;
    }

    public P notifier(final User notifier) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        if (this.notifier != null) {
            throw new IllegalStateException("Notifier for this phase context is already set!");
        }
        this.notifier = checkNotNull(notifier, "Notifier cannot be null!");
        return (P) this;
    }

    private void checkBlockSuppliers() {
        checkState(this.captureBlockPos == null, "CaptureBlockPos is already set!");
    }

    public P addBlockCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.checkBlockSuppliers();

        this.transactor = new TransactionalCaptureSupplier();
        return (P) this;
    }

    public P addCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        return (P) this;
    }

    public P addEntityCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        return (P) this;
    }

    public P addEntityDropCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        return (P) this;
    }

    public P setBulkBlockCaptures(final boolean captures) {
        this.allowsBulkBlockCaptures = captures;
        return (P) this;
    }

    public boolean allowsBulkBlockCaptures() {
        return this.allowsBulkBlockCaptures;
    }

    public P setBlockEvents(final boolean events) {
        this.allowsBlockEvents = events;
        return (P) this;
    }

    public boolean allowsBlockEvents() {
        return this.allowsBlockEvents;
    }

    protected P setEntitySpawnEvents(final boolean b) {
        this.allowsEntityEvents = b;
        return (P) this;
    }

    public boolean allowsEntityEvents() {
        return this.allowsEntityEvents;
    }

    protected P setBulkEntityCaptures(final boolean b) {
        this.allowsBulkEntityCaptures = b;
        return (P) this;
    }

    public boolean allowsBulkEntityCaptures() {
        return this.allowsBulkEntityCaptures;
    }

    public P buildAndSwitch() {
        this.isCompleted = true;
        if (SpongeConfigs.getCommon().get().getPhaseTracker().generateStackTracePerStateEntry()) {
            this.stackTrace = new Exception("Debug Trace").getStackTrace();
        }
        PhaseTracker.getInstance().switchToPhase(this.state, this);
        return (P) this;
    }

    public boolean isComplete() {
        return this.isCompleted;
    }

    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        if (this.stackTrace != null) {
            printer.add(s + "StackTrace On Entry")
                .add(this.stackTrace);
        }
        if (this.creator != null) {
            printer.add(s + "- %s: %s", "Owner", this.creator);
        }
        if (this.source != null) {
            printer.add(s + "- %s: %s", "Source", this.source);
        }
        if (this.transactor != null && !this.transactor.isEmpty()) {
            printer.add(s + "- %s: %s", "CapturedTransactions", this.transactor);
        }
        return printer;
    }


    public boolean notAllCapturesProcessed() {
        // we can safely pop the frame here since this is only called when we're checking for processing
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getSource(final Class<T> sourceClass) {
        if (this.source == null) {
            return Optional.empty();
        }
        if (sourceClass.isInstance(this.source)) {
            return Optional.of((T) this.source);
        }
        return Optional.empty();
    }

    @Nullable
    public Object getSource() {
        return this.source;
    }

    public <T> T requireSource(final Class<T> targetClass) {
        return this.getSource(targetClass)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", this));
    }

    public Optional<User> getCreator() {
        return Optional.ofNullable(this.creator);
    }

    /**
     * Applies the owner to the provided {@link Consumer} if available.
     *
     * @param consumer The consumer consuming the owner that isn't null
     * @return True if the consumer was called
     */
    public boolean applyOwnerIfAvailable(final Consumer<? super User> consumer) {
        if (this.creator != null) {
            consumer.accept(this.creator);
            return true;
        }
        return false;
    }

    public Optional<User> getNotifier() {
        return Optional.ofNullable(this.notifier);
    }

    /**
     * Applies the notifier to the provided {@link Consumer} if available.
     *
     * @param consumer The consumer consuming the notifier that isn't null
     * @return True if the consumer was called
     */
    public boolean applyNotifierIfAvailable(final Consumer<? super User> consumer) {
        if (this.notifier != null) {
            consumer.accept(this.notifier);
            return true;
        }
        return false;
    }

    /**
     * Gets the {@link List} of the <b>first</b> {@link BlockSnapshot}s that originally
     * existed at their set {@link BlockPos block position} such that this list is not
     * self updating and a copy of the parsed list. The reason for this to return
     * a list of {@link SpongeBlockSnapshot}s is to handle the ability for
     * {@link BlockChange} being ensured to be correct according to this context's
     * {@link TransactionalCaptureSupplier}. It is intended that the returned list is not
     * self updating, nor is it to be used as to "remove" blocks from being captured.
     *
     * <p>To mutate entries presented in the returned list, use {@link #getTransactor()}
     * and methods available in {@link TransactionalCaptureSupplier} such as:
     * <ul>
     *     <li>{@link TransactionalCaptureSupplier#clear()} - To clear the captured lists</li>
     *     <li>{@link TransactionalCaptureSupplier#reset()} to remove a block snapshot change</li>
     * </ul>
     * Provided functionality through the supplier is aimed for common manipulation in
     * {@link IPhaseState}s and for the obvious reasons of capturing block changes, as long
     * as {@code IPhaseState#shouldCaptureBlockChangeOrSkip(PhaseContext, BlockPos, IBlockState, IBlockState, org.spongepowered.api.world.BlockChangeFlag)} returns
     * {@code true}.
     *
     * <p>If post phase processing requires constant updating of the list and/or intermediary
     * {@link SpongeBlockSnapshot} changes to be pruned, it is advised to do so via
     * a post state since internal tracked event transactions are not clearable.</p>
     *
     * @return A list of original block snapshots that are now changed
     * @throws IllegalStateException If there is no capture supplier set up for this context
     */
    public List<SpongeBlockSnapshot> getCapturedOriginalBlocksChanged() throws IllegalStateException {
        if (this.transactor == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing blocks, but we're not capturing them!", this).get();
        }
        return Collections.emptyList();
    }

    public TransactionalCaptureSupplier getTransactor() {
        if (this.transactor == null) {
            this.transactor = new TransactionalCaptureSupplier();
        }
        return this.transactor;
    }

    public CaptureBlockPos getCaptureBlockPos() throws IllegalStateException {
        if (this.captureBlockPos == null) {
            throw TrackingUtil.throwWithContext("Intended to capture a block position!", this).get();
        }
        return this.captureBlockPos;
    }

    public boolean hasCaptures() {
        if (this.transactor != null && !this.transactor.isEmpty()) {
            return true;
        }
        if (this.source != null && this.source instanceof PlayerEntity) {
            if (!((TrackedInventoryBridge) ((PlayerEntity) this.source).inventory).bridge$getCapturedSlotTransactions().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public Optional<BlockPos> getBlockPosition() {
        return this.getCaptureBlockPos()
            .getPos();
    }

    public void addCreatorAndNotifierToCauseStack(final CauseStackManager.StackFrame frame) {
        if (this.creator != null) {
            frame.addContext(EventContextKeys.CREATOR, this.creator);
        }
        if (this.notifier != null) {
            frame.addContext(EventContextKeys.NOTIFIER, this.notifier);
        }
    }

    protected PhaseContext(final IPhaseState<? extends P> state, final PhaseTracker tracker) {
        this.state = state;
        this.createdTracker = checkNotNull(tracker, "Null PhaseTracker!");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isCompleted);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final PhaseContext<?> other = (PhaseContext<?>) obj;
        return Objects.equals(this.isCompleted, other.isCompleted);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
            .add("isCompleted", this.isCompleted)
            .toString();
    }

    protected P markEmpty() {
        this.isCompleted = true;
        return (P) this;
    }

    public boolean isEmpty() {
        if (this == PhaseContext.EMPTY) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void close() { // Should never throw an exception
        if (this.isEmpty()) {
            // We aren't ever supposed to close here...
            PhasePrinter
                .printMessageWithCaughtException(this.createdTracker.stack, "Closing an empty Phasecontext",
                    "We should never be closing an empty phase context (or complete phase) This is likely an error from sponge.",
                    this.state, this, new IllegalStateException("Closing empty phase context"));
            return;
        }
        final PhaseTracker instance = PhaseTracker.getInstance();
        instance.completePhase(this);
        if (!((IPhaseState) this.state).shouldProvideModifiers(this)) {
            if (this.usedFrame != null) {
                this.usedFrame.iterator().forEachRemaining(instance::popCauseFrame);
            }
            return;
        }
        if (this.usedFrame == null) {
            // So, this part is interesting... Since the used frame is null, that means
            // the cause stack manager still has the refernce of this context/phase, we have
            // to "pop off" the list.
            instance.popFrameMutator(this);
        }
        if (this.usedFrame != null) {
            this.usedFrame.iterator().forEachRemaining(instance::popCauseFrame);
            this.usedFrame.clear();
            this.usedFrame = null;
        }
        this.reset();
        this.isCompleted = false;
        if (this.state instanceof PooledPhaseState) {
            ((PooledPhaseState) this.state).releaseContextFromPool(this);
        }
    }

    protected void reset() {
        this.source = null;
        this.stackTrace = null;
        this.creator = null;
        this.notifier = null;
        if (this.transactor != null) {
            this.transactor.reset();
        }

        if (this.captureBlockPos != null) {
            this.captureBlockPos.setPos(null);
            this.captureBlockPos.setWorld((ServerWorld) null);
        }

    }

    public void printTrace(final PrettyPrinter printer) {
        if (SpongeConfigs.getCommon().get().getPhaseTracker().generateStackTracePerStateEntry()) {
            printer.add("Entrypoint:")
                .add(this.stackTrace);
        }
    }

    public boolean allowsBlockPosCapturing() {
        return this.captureBlockPos != null;
    }

    public boolean captureEntity(final Entity entity) {

        // Throw an exception if we're not capturing at all but the state says we do?
        throw new IllegalStateException("Expected to capture entities, but we aren't capturing them.");
    }

    @Nullable
    public User getActiveUser() {
        if (this.notifier != null) {
            return this.notifier;
        }
        if (this.creator != null) {
            return this.creator;
        }
        if (this.source != null && this.source instanceof User) {
            return (User) this.source;
        }
        return null;
    }

    protected boolean isRunaway(final PhaseContext<?> phaseContext) {
        return phaseContext.getClass() == this.getClass();
    }

    protected P defensiveCopy(final PhaseTracker tracker) {
        final P newCopy = this.state instanceof PooledPhaseState ? ((PooledPhaseState<P>) this.state).createNewContext(tracker) :  this.state.createPhaseContext(tracker);
        newCopy.source(this.source);
        // tODO - blah

        return newCopy;
    }
}
