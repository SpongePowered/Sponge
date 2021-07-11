package org.spongepowered.common.event.tracking.context.transaction;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.tracking.PhaseContext;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ContainerSlotTransaction extends ContainerBasedTransaction {

    private final SlotTransaction newTransaction;

    public ContainerSlotTransaction(final Supplier<ServerLevel> worldSupplier, final AbstractContainerMenu menu, final SlotTransaction newTransaction) {
        super(((ServerWorld) worldSupplier.get()).key(), menu);
        this.newTransaction = newTransaction;
    }

    @Override
    Optional<SlotTransaction> getSlotTransaction() {
        return Optional.of(this.newTransaction);
    }

    @Override
    List<Entity> getEntitiesSpawned() {
        return Collections.emptyList();
    }

    @Override
    public void restore(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        // PacketPhaseUtil.handleSlotRestore
        if (!this.newTransaction.isValid()) {
            this.newTransaction.slot().set(this.newTransaction.original().createStack());
        } else if (this.newTransaction.custom().isPresent()) {
            this.newTransaction.slot().set(this.newTransaction.finalReplacement().createStack());
        }
    }


}
