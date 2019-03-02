package org.spongepowered.common.item.inventory.custom;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.menu.SlotChangeHandler;
import org.spongepowered.api.item.inventory.menu.SlotClickHandler;
import org.spongepowered.api.item.inventory.property.ContainerType;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeInventoryMenu implements InventoryMenu {

    private ViewableInventory inventory;

    private Map<SlotIndex, List<SpongeMenuCallback>> callbacks = new HashMap<>();

    public SpongeInventoryMenu(ViewableInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ViewableInventory getCurrentInventory() {
        return this.inventory;
    }

    @Override
    public ContainerType getType() {
        return this.inventory.getContainerType();
    }

    @Override
    public void setCurrentInventory(ViewableInventory inventory) {
        if (this.getType().equals(inventory.getContainerType())) {
            // TODO handle container changes
        } else {
            // TODO close and reopen with new container
        }
        this.inventory = inventory;
    }

    @Override
    public MenuCallback registerClick(SlotClickHandler handler, SlotIndex... slotIndices) {
        SpongeMenuCallback cb = new SpongeMenuCallback(); // TODO handler
        return registerCallback(cb, slotIndices);
    }

    @Override
    public MenuCallback registerChange(SlotChangeHandler handler, SlotIndex... slotIndices) {
        SpongeMenuCallback cb = new SpongeMenuCallback(); // TODO handler
        return registerCallback(cb, slotIndices);
    }

    private MenuCallback registerCallback(SpongeMenuCallback cb, SlotIndex[] slotIndices) {
        for (SlotIndex slotIndex : slotIndices) {
            this.callbacks.computeIfAbsent(slotIndex, si -> new ArrayList<>()).add(cb);
        }
        return cb;
    }

    @Override
    public void unregisterAt(SlotIndex... slotIndices) {
        for (SlotIndex slotIndex : slotIndices) {
            this.callbacks.remove(slotIndex);
        }
    }

    @Override
    public boolean unregister(MenuCallback callback) {
        boolean removed = false;
        for (Map.Entry<SlotIndex, List<SpongeMenuCallback>> entry : this.callbacks.entrySet()) {
            if (entry.getValue().remove(callback)) {
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public void unregisterAll() {
        this.callbacks.clear();
    }

    @Override
    public Optional<Container> open(Player player) {
        return player.openInventory(this.inventory);
    }

    public class SpongeMenuCallback implements MenuCallback {

    }
}
