package org.spongepowered.common.registry.loader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.common.data.nbt.validation.SpongeValidationType;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.ValidationTypes;
import org.spongepowered.common.event.tracking.context.transaction.type.BlockTransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.NoOpTransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionType;
import org.spongepowered.common.event.tracking.context.transaction.type.TransactionTypes;
import org.spongepowered.common.registry.RegistryLoader;

import java.util.Locale;

public class SpongeCommonRegistryLoader {

    public static RegistryLoader<TransactionType<@NonNull ?>> blockTransactionTypes() {
        return RegistryLoader.of(l -> {
            l.add(TransactionTypes.BLOCK, k -> new BlockTransactionType());
            l.add(TransactionTypes.ENTITY_DEATH_DROPS, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.CLICK_CONTAINER_EVENT, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.NEIGHBOR_NOTIFICATION, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.SPAWN_ENTITY, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.CHANGE_INVENTORY_EVENT, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.SLOT_CHANGE, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
            l.add(TransactionTypes.INTERACT_CONTAINER_EVENT, k -> new NoOpTransactionType<>(false, k.value().toUpperCase(Locale.ROOT)));
        });
    }

    public static RegistryLoader<ValidationType> validationType() {
        return RegistryLoader.of(l -> l.mapping(SpongeValidationType::new, m -> m.add(
                ValidationTypes.BLOCK_ENTITY,
                ValidationTypes.ENTITY
        )));
    }

}
