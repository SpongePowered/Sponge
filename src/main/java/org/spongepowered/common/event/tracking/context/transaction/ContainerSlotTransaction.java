package org.spongepowered.common.event.tracking.context.transaction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.function.Supplier;

public class ContainerSlotTransaction extends ContainerBasedTransaction {

    private final SlotTransaction transactoin;

    public ContainerSlotTransaction(final Supplier<ServerLevel> worldSupplier, final AbstractContainerMenu menu, final SlotTransaction newTransaction) {
        super(((ServerWorld) worldSupplier.get()).key(), menu);
        this.transactoin = newTransaction;
    }

    @Override
    Optional<SlotTransaction> getSlotTransaction() {
        return Optional.of(this.transactoin);
    }

}
