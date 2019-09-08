package org.spongepowered.common.item.inventory.fabric;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.bridge.item.inventory.InventoryBridge;
import org.spongepowered.common.item.inventory.lens.Fabric;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface UniversalFabric extends Fabric, InventoryBridge {

    @Override
    default Collection<InventoryBridge> fabric$allInventories() {
        return InventoryTranslators.getTranslator(this.getClass()).allInventories(this);
    }

    @Override
    default InventoryBridge fabric$get(int index) {
        return InventoryTranslators.getTranslator(this.getClass()).get(this, index);
    }

    @Override
    default ItemStack fabric$getStack(int index) {
        return InventoryTranslators.getTranslator(this.getClass()).getStack(this, index);
    }

    @Override
    default void fabric$setStack(int index, ItemStack stack) {
        InventoryTranslators.getTranslator(this.getClass()).setStack(this, index, stack);
    }

    @Override default int fabric$getMaxStackSize() {
        return InventoryTranslators.getTranslator(this.getClass()).getMaxStackSize(this);
    }

    @Override default Translation fabric$getDisplayName() {
        return InventoryTranslators.getTranslator(this.getClass()).getDisplayName(this);
    }

    @Override default int fabric$getSize() {
        return InventoryTranslators.getTranslator(this.getClass()).getSize(this);
    }

    @Override default void fabric$clear() {
        InventoryTranslators.getTranslator(this.getClass()).clear(this);
    }

    @Override default void fabric$markDirty() {
        InventoryTranslators.getTranslator(this.getClass()).markDirty(this);
    }
}
