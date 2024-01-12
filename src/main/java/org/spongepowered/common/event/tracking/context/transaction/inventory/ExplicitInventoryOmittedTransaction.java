package org.spongepowered.common.event.tracking.context.transaction.inventory;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;

import java.util.Optional;

public class ExplicitInventoryOmittedTransaction extends ContainerBasedTransaction {
    public ExplicitInventoryOmittedTransaction(
        final AbstractContainerMenu menu
    ) {
        super(menu);
    }

    @Override
    public Optional<ClickContainerEvent> generateEvent(
        final PhaseContext<@NonNull ?> context, @Nullable final GameTransaction<@NonNull ?> parent,
        final ImmutableList<GameTransaction<ClickContainerEvent>> gameTransactions, final Cause currentCause
    ) {
        return Optional.empty();
    }
}
