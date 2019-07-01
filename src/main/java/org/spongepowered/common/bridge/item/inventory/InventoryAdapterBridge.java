package org.spongepowered.common.bridge.item.inventory;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;

public interface InventoryAdapterBridge {

    SlotProvider bridge$generateSlotProvider();

    void bridge$setSlotProvider(SlotProvider provider);

    Lens bridge$generateLens();

    void bridge$setLens(Lens lens);

    default Fabric bridge$generateFabric() {
        return MinecraftFabric.of(this);
    }

    void bridge$setFabric(Fabric fabric);

    PluginContainer bridge$getPlugin();

    void bridge$setPlugin(PluginContainer container);

}
