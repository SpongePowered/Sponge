package org.spongepowered.common.event.tracking.context.transaction.type;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.stream.Stream;

public final class BlockTransactionTypeStreamGenerator {

    public static Stream<TransactionType<@NonNull ?>> stream() {
        return Stream.of(
            new BlockTransactionType(),
            new NoOpTransactionType<>(false, "NEIGHBOR_NOTIFICATION"),
            new NoOpTransactionType<>(false, "SPAWN_ENTITY"),
            new NoOpTransactionType<>(false,"ENTITY_DEATH_DROPS")
        );
    }

    private BlockTransactionTypeStreamGenerator() {}
}
