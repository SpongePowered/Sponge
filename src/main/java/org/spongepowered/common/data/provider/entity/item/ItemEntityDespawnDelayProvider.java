package org.spongepowered.common.data.provider.entity.item;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.bridge.entity.item.ItemEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class ItemEntityDespawnDelayProvider extends GenericMutableDataProvider<ItemEntityBridge, Integer> {

    public ItemEntityDespawnDelayProvider() {
        super(Keys.DESPAWN_DELAY);
    }

    @Override
    protected Optional<Integer> getFrom(ItemEntityBridge dataHolder) {
        return Optional.of(dataHolder.bridge$getDespawnDelay());
    }

    @Override
    protected boolean set(ItemEntityBridge dataHolder, Integer value) {
        dataHolder.bridge$setDespawnDelay(value);
        return true;
    }
}
