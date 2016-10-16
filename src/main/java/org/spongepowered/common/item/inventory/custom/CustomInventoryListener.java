package org.spongepowered.common.item.inventory.custom;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.List;
import java.util.function.Consumer;

public class CustomInventoryListener implements EventListener<InteractInventoryEvent> {

    private Inventory inventory;
    List<Consumer<InteractInventoryEvent>> consumers;

    @SuppressWarnings("unchecked")
    public CustomInventoryListener(Inventory inventory, List<Consumer<? extends InteractInventoryEvent>> consumers) {
        this.inventory = inventory;
        this.consumers = (List) ImmutableList.copyOf(consumers);
    }

    @Override
    public void handle(InteractInventoryEvent event) throws Exception {
        if (!(event.getTargetInventory() == this.inventory)) {
            return;
        }
        for (Consumer<InteractInventoryEvent> consumer: this.consumers) {
            consumer.accept(event);
        }
    }
}
