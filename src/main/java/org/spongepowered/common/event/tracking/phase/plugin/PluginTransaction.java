package org.spongepowered.common.event.tracking.phase.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.server.ServerTransaction;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;
import java.util.function.Predicate;

public class PluginTransaction extends PluginPhaseState<PluginTransaction.TransactionContext> {

    @Override
    public void unwind(final TransactionContext phaseContext) {
        if (!TrackingUtil.processBlockCaptures(phaseContext)) {
            phaseContext.rollBack();
        }
    }

    @Override
    public boolean forceRollbackEvents(final TransactionContext context) {
        return !context.successContainer.booleanValue();
    }

    @Override
    public void notifyEventChanges(final TransactionContext context, final Event event) {
        ChangeBlockEvent.@Nullable Post post = null;
        if (event instanceof ChangeBlockEvent.All) {
            final ListMultimap<BlockPos, SpongeBlockSnapshot> positions = LinkedListMultimap.create();
            ((ChangeBlockEvent.All) event).transactions().stream()
                .filter(BlockTransaction::isValid)
                .forEach(transactions -> {
                    // Then "put" the most recent transactions such that we have a complete rebuild of
                    // each position according to what originally existed and then
                    // the ultimate final block on that position
                    final SpongeBlockSnapshot original = (SpongeBlockSnapshot) transactions.original();
                    positions.put(original.getBlockPos(), original);
                    positions.put(original.getBlockPos(), (SpongeBlockSnapshot) transactions.finalReplacement());
                });
            final ImmutableList<BlockTransactionReceipt> transactions = positions.asMap()
                .values()
                .stream()
                .map(TrackingUtil.createBlockTransactionReceipt(context, (a, b) -> {}))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableList.toImmutableList());
            post = SpongeEventFactory.createChangeBlockEventPost(event.cause(), transactions, ((ChangeBlockEvent.All) event).world());
        }
        @Nullable SpawnEntityEvent entities = null;
        if (event instanceof SpawnEntityEvent) {
            entities = (SpawnEntityEvent) event;
        }
        final ServerTransaction transaction = new ServerTransaction(entities, post);
        if (!context.predicate.test(transaction)) {
            if (event instanceof ChangeBlockEvent.All) {
                ((ChangeBlockEvent.All) event).setCancelled(true);
            } else if (event instanceof SpawnEntityEvent) {
                ((SpawnEntityEvent) event).setCancelled(true);
            }
            context.rollBack();
        }
    }

    @Override
    protected TransactionContext createNewContext(final PhaseTracker tracker) {
        return new TransactionContext(this, tracker);
    }

    public static final class TransactionContext extends PluginPhaseContext<TransactionContext> {

        @MonotonicNonNull private PluginContainer container;
        @MonotonicNonNull private Runnable runnable;
        @MonotonicNonNull private Predicate<ServerTransaction> predicate;
        @MonotonicNonNull private MutableBoolean successContainer;

        TransactionContext(
            final IPhaseState<TransactionContext> phaseState,
            final PhaseTracker tracker
        ) {
            super(phaseState, tracker);
        }

        public PluginContainer getContainer() {
            return container;
        }

        public TransactionContext setContainer(final PluginContainer container) {
            this.container = container;
            return this;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public TransactionContext setRunnable(final Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public Predicate<ServerTransaction> getPredicate() {
            return predicate;
        }

        public TransactionContext setPredicate(final Predicate<ServerTransaction> predicate) {
            this.predicate = predicate;
            return this;
        }

        @Override
        protected void reset() {
            this.container = null;
            this.runnable = null;
            this.predicate = null;
            this.successContainer = null;
        }

        public void rollBack() {
            this.successContainer.setFalse();
        }

        public TransactionContext setSuccessContainer(final MutableBoolean succeeded) {
            this.successContainer = succeeded;
            return this;
        }
    }
}
