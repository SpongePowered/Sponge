package org.spongepowered.common.item.inventory.custom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.custom.ContainerType;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.SlotChangeHandler;
import org.spongepowered.api.item.inventory.menu.SlotClickHandler;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.interfaces.IMixinContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SpongeInventoryMenu implements InventoryMenu {

    private ViewableInventory inventory;

    private Map<Container, Player> tracked = new HashMap<>();
    private Text title;

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
            // ideally we would just swap out the IInventory from existing slots
            // TODO handle container changes
            this.reopen(); // if not possible reopen
        } else {
            // Get all distinct players and reopen inventory for them
            this.reopen();
        }
        this.inventory = inventory;
    }

    private void reopen() {
        new ArrayList<>(tracked.values()).stream().distinct().forEach(this::open);
    }

    @Override
    public void setTitle(Text title) {
        this.title = title;
        this.reopen();
    }

    @Override
    public void registerClose(BiConsumer<Container, Player> handler) {
        // TODO
    }

    @Override
    public InventoryMenu setReadOnly(boolean readOnly) {
        // TODO
        return this;
    }

    @Override
    public void registerClick(SlotClickHandler handler, SlotIndex... slotIndices) {
        // TODO
    }

    @Override
    public void registerChange(SlotChangeHandler handler, SlotIndex... slotIndices) {
        // TODO
    }

    @Override
    public void unregisterAt(SlotIndex... slotIndices) {
        for (SlotIndex slotIndex : slotIndices) {
        }// TODO
    }

    @Override
    public void unregisterAll() {
        // TODO
    }

    @Override
    public Optional<Container> open(Player player) {
        Optional<Container> container = player.openInventory(this.inventory, this.title);
        container.ifPresent(c -> {
            if (c instanceof IMixinContainer) {
                ((IMixinContainer)c).setMenu(this);
                tracked.put(c, player);
            }
        });
        return container;
    }

    public void onClose(Container container) {
        // TODO close callbacks
        this.tracked.remove(container);
    }

    public boolean onClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, Container container) {
        // TODO click callbacks
        return true;
    }

    public boolean onChange(ItemStack itemstack, ItemStack oldStack, Container mixinContainer, int slotIndex, Slot slot) {
        // TODO change callbacks
        return true;
    }
}
