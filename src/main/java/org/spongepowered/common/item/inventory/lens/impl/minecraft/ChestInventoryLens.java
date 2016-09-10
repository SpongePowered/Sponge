package org.spongepowered.common.item.inventory.lens.impl.minecraft;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.comp.HotbarLensImpl;

/**
 * A {@link Lens} comprising of when a Minecraft-like {@link Chest} is opened.
 */
public class ChestInventoryLens extends MinecraftLens {

    private GridInventoryLensImpl playerInventory, chestInventory;

    private HotbarLensImpl hotbarInventory;

    public ChestInventoryLens(InventoryAdapter<IInventory, ItemStack> adapter, SlotProvider<IInventory, ItemStack> slots) {
        super(0, adapter.size(), adapter, slots);
    }

    @Override
    protected void init(SlotProvider<IInventory, ItemStack> slots) {
        this.hotbarInventory = new HotbarLensImpl(0, 9, slots);
        this.playerInventory = new GridInventoryLensImpl(9, 9, 3, slots);
        // TODO Figure out if we're double chest. Figure out stride correctly
        this.chestInventory = new GridInventoryLensImpl(36, 9, 3, slots);

        this.addSpanningChild(this.hotbarInventory);
        this.addSpanningChild(this.playerInventory);
        this.addSpanningChild(this.chestInventory);
    }
}
