package org.spongepowered.common.event.tracking.context.transaction;

import org.checkerframework.checker.nullness.qual.Nullable;

public class EffectTransactor implements AutoCloseable {
    final @Nullable ResultingTransactionBySideEffect previousEffect;
    final @Nullable BlockTransaction parent;
    private final TransactionalCaptureSupplier supplier;
    private final ResultingTransactionBySideEffect effect;

    EffectTransactor(final ResultingTransactionBySideEffect effect, final @Nullable BlockTransaction parent,
        final @Nullable ResultingTransactionBySideEffect previousEffect, final TransactionalCaptureSupplier transactor) {
        /*
        | ChangeBlock(1) <- head will be RemoveTileEntity(1), tail is still RemoveTileentity(1)
        |  |- RemoveTileEntity <- Head will be ChangeBlock(2) tail is still ChangeBlock(2)
        |  |   |- ChangeBlock <-- Head will be null, tail is still null
         */
        this.effect = effect;
        this.supplier = transactor;
        this.parent = parent;
        this.previousEffect = previousEffect;
    }

    @Override
    public void close() {
        if (this.effect.head == null && this.parent != null && this.parent.getEffects().peekLast() == this.effect) {
            this.parent.getEffects().removeLast();
        }
        this.supplier.popEffect(this);
    }
}
