package org.spongepowered.common.item.inventory.adapter;

import org.spongepowered.common.bridge.item.inventory.InventoryAdapterBridge;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;

public interface ReusableLensInventoryAdapaterBridge extends InventoryAdapter, InventoryAdapterBridge {

    ReusableLens<?> bridge$getReusableLens();

    @Override
    default SlotProvider bridge$generateSlotProvider() {
        return bridge$getReusableLens().getSlots();
    }

    @Override
    default Lens bridge$generateLens() {
        return bridge$getReusableLens().getLens();
    }

}
