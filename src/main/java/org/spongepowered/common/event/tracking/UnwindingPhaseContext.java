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

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.context.MultiBlockCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import javax.annotation.Nullable;

public final class UnwindingPhaseContext extends PhaseContext<UnwindingPhaseContext> {

    @Override
    public boolean hasCaptures() {
        return super.hasCaptures() || (this.usesMulti && this.blockSuppliers != null && !this.blockSuppliers.isEmpty());
    }

    @Override
    protected void reset() {
        super.reset();
    }

    @Nullable
    static UnwindingPhaseContext unwind(IPhaseState<?> state, PhaseContext<?> context, boolean hasCaptures) {
        if (!state.requiresPost() || !hasCaptures) {
            return null;
        }
        return new UnwindingPhaseContext(state, context)
                .source(context.getSource())
                .addCaptures()
                .addEntityDropCaptures()
                .buildAndSwitch();
    }

    private final IPhaseState<?> unwindingState;
    private final PhaseContext<?> unwindingContext;
    @Nullable Deque<MultiBlockCaptureSupplier> blockSuppliers;
    @Nullable private Deque<SpongeBlockSnapshot> singleSnapshots;
    final boolean usesMulti;
    final boolean tracksNeighborNotifications;
    private final boolean isPostingSpecial;
    private boolean hasGotten = true;

    final boolean tracksTiles;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private UnwindingPhaseContext(IPhaseState<?> unwindingState, PhaseContext<?> unwindingContext) {
        super(GeneralPhase.Post.UNWINDING);
        this.unwindingState = unwindingState;
        this.unwindingContext = unwindingContext;
        this.tracksTiles = ((IPhaseState) unwindingState).tracksTileEntityChanges(unwindingContext);
        this.tracksNeighborNotifications = ((IPhaseState) unwindingState).doesCaptureNeighborNotifications(unwindingContext);
        this.isPostingSpecial = ((IPhaseState) unwindingState).hasSpecificBlockProcess(unwindingContext);
        setBulkBlockCaptures(((IPhaseState) unwindingState).doesBulkBlockCapture(unwindingContext));
        // Basically put, the post state needs to understand that if we're expecting potentially chained block changes
        // to worlds, AND we're potentially getting any neighbor notification requests OR tile entity requests,
        // we'll need to switch on to capture such objects. If for example, we do not track tile changes, but we track
        // neighbor notifications, that would be fine, but we cannot require that both are tracked unless specified.
        this.usesMulti = this.allowsBulkBlockCaptures() && !this.isPostingSpecial;
        if (this.usesMulti) {
            // 8 is the minimum element size required by the ArrayDeque
            this.blockSuppliers = new ArrayDeque<>(8);
            this.blockSuppliers.push(new MultiBlockCaptureSupplier());
        }
    }

    @Override
    public Optional<User> getOwner() {
        return this.unwindingContext.getOwner();
    }

    @Override
    public Optional<User> getNotifier() {
        return this.unwindingContext.getNotifier();
    }

    @SuppressWarnings("unchecked")
    public <T extends PhaseContext<T>> T getUnwindingContext() {
        return (T) this.unwindingContext;
    }

    IPhaseState<?> getUnwindingState() {
        return this.unwindingState;
    }

    boolean isPostingSpecialProcess() {
        return this.isPostingSpecial;
    }

    void pushNewCaptureSupplier() {
        if (!this.usesMulti) {
            throw new IllegalStateException("This post state is not meant to capture multiple changes with neighbor notifications or tile entity changes!");
        }
        this.hasGotten = false;
    }

    void popBlockSupplier() {
        if (!this.usesMulti) {
            throw new IllegalStateException("This post state is not meant to capture multiple changes with neighbor notifications or tile entity changes!");
        }
        this.blockSuppliers.pop();
    }

    @Override
    public SpongeBlockSnapshot getSingleSnapshot() {
        if (this.singleSnapshot == null) {
            if (this.singleSnapshots == null) {
                throw new IllegalStateException("Expected to be capturing single snapshots for immediate throwing, but we're not finding any!");
            }
            return this.singleSnapshots.pop();
        }
        return this.singleSnapshot;
    }

    @Override
    public void setSingleSnapshot(@Nullable SpongeBlockSnapshot singleSnapshot) {
        if (singleSnapshot == null) {
            if (this.singleSnapshots != null && !this.singleSnapshots.isEmpty()) {
                this.singleSnapshots.pop();
            } else {
                this.singleSnapshot = null;
            }
        }
        if (this.singleSnapshot != null) {
            if (this.singleSnapshots == null) {
                this.singleSnapshots = new ArrayDeque<>();
            }
            this.singleSnapshots.push(this.singleSnapshot);
            this.singleSnapshot = null;
            this.singleSnapshots.push(singleSnapshot);
        } else {
            if (this.singleSnapshots != null) {
                this.singleSnapshots.push(singleSnapshot);
            } else {
                this.singleSnapshot = singleSnapshot;
            }
        }
    }

    /**
     * Gets the {@link MultiBlockCaptureSupplier} object from this context. Note that
     * accessing
     * @return
     * @throws IllegalStateException
     */
    @Override
    public MultiBlockCaptureSupplier getCapturedBlockSupplier() throws IllegalStateException {
        if (!this.usesMulti) {
            return super.getCapturedBlockSupplier();
        }
        if (!this.hasGotten) {
            // Because we have to tell the context to push a new capture supplier before
            // we start processing on that capture supplier, we have to use this boolean state
            // to trigger pushing a new capture supplier and peek at the existing one (the one
            // at the head of the stack, assuming we're capturing)
            final MultiBlockCaptureSupplier peek = this.blockSuppliers.peek();
            this.blockSuppliers.push(new MultiBlockCaptureSupplier());
            this.hasGotten = true;
            return peek;
        }
        return this.blockSuppliers.peek();

    }

    @Override
    public PrettyPrinter printCustom(PrettyPrinter printer, int indent) {
        String s = String.format("%1$" + indent + "s", "");
        super.printCustom(printer, indent)
            .add(s + "- %s: %s", "UnwindingState", this.unwindingState)
            .add(s + "- %s: %s", "UnwindingContext", this.unwindingContext)
            .add(s + "- %s: %s", "IsPostingSpecial", this.tracksTiles);
        this.unwindingContext.printCustom(printer, indent * 2);
        return printer;
    }
}
