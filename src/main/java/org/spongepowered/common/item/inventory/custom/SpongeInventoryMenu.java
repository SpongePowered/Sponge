package org.spongepowered.common.item.inventory.custom;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.custom.ContainerType;
import org.spongepowered.api.item.inventory.menu.ClickHandler;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.KeySwapHandler;
import org.spongepowered.api.item.inventory.menu.SlotChangeHandler;
import org.spongepowered.api.item.inventory.menu.SlotClickHandler;
import org.spongepowered.api.item.inventory.slot.SlotIndex;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.interfaces.IMixinContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SpongeInventoryMenu implements InventoryMenu {

    private ViewableInventory inventory;

    private Map<Container, Player> tracked = new HashMap<>();
    private Text title;

    private List<SlotClickHandler> slotClickHandlers = new ArrayList<>();
    private List<ClickHandler> clickHandlers = new ArrayList<>();
    private List<KeySwapHandler> keySwapHandlers = new ArrayList<>();
    private List<SlotChangeHandler> changeHandlers = new ArrayList<>();

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
    public void registerClick(ClickHandler handler) {
        // TODO
    }

    @Override
    public void registerSlotClick(SlotClickHandler handler, SlotIndex... slotIndices) {
        // TODO
    }

    @Override
    public void registerChange(SlotChangeHandler handler, SlotIndex... slotIndices) {
        // TODO
    }

    @Override
    public void registerKeySwap(KeySwapHandler handler, SlotIndex... slotIndices) {
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

        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            return this.onClickDrag(slotId, dragType, container);
        }

        SlotIndex idx = SlotIndex.of(slotId);
        Optional<org.spongepowered.api.item.inventory.Slot> slot = container.getSlot(idx);

        if (slot.isPresent()) {
            switch (clickTypeIn) {
                case SWAP:
                    if (dragType >= 0 && dragType < 9) {
                        SlotIndex idx2 = SlotIndex.of(dragType);
                        Optional<org.spongepowered.api.item.inventory.Slot> slot2 = container.getSlot(idx2);
                        if (slot2.isPresent()) {
                            return this.keySwapHandlers.stream().allMatch(h -> h.handle(container, slot.get(), idx, ClickTypes.KEY_SWAP, slot2.get()));
                        }
                    }
                    return true;
                case CLONE:
                    return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot.get(), idx, ClickTypes.CLICK_MIDDLE));
                case PICKUP_ALL:
                    return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot.get(), idx, ClickTypes.DOUBLE_CLICK));
                default:
                    if (dragType == 0) {
                        return onClickLeft(clickTypeIn, container, idx, slot.get());
                    } else if (dragType == 1) {
                        return onClickRight(clickTypeIn, container, idx, slot.get());
                    }
                    // else unknown drag-type
                    return true;
            }
        }
        // else no slot present
        switch (clickTypeIn) {
            case PICKUP:
                if (slotId == -999) {
                    if (dragType == 0) {
                        return this.clickHandlers.stream().allMatch(h -> h.handle(container, ClickTypes.CLICK_LEFT_OUTSIDE));
                    } else if (dragType == 1) {
                        return this.clickHandlers.stream().allMatch(h -> h.handle(container, ClickTypes.CLICK_RIGHT_OUTSIDE));
                    }
                }
                // else unknown slotId/drag-type
                break;
            case THROW:
                if (slotId == -999) {
                    // TODO check packets - does THROW with slotid -999 exist or is this actually PICKUP?
                    // its supposed to be l/r-click with nothing in hand
                    // TODO check if those exist
                    ///**
                    // * Throwing one item on the cursor by clicking outside the inventory window.
                    // */
                    //public static final org.spongepowered.api.item.inventory.menu.ClickType
                    //        CLICK_THROW_ONE = DummyObjectProvider.createFor(org.spongepowered.api.item.inventory.menu.ClickType.class, "click_throw_one");
                    ///**
                    // * Throwing all items on the cursor by clicking outside the inventory window.
                    // */
                    //public static final org.spongepowered.api.item.inventory.menu.ClickType
                    //        CLICK_THROW_ALL = DummyObjectProvider.createFor(org.spongepowered.api.item.inventory.menu.ClickType.class, "click_throw_all");
                }
                // else unknown slotId/drag-type
                break;
        }

        return true;
    }

    private boolean onClickDrag(int slotId, int dragType, Container container) {


        int dragMode = dragType >> 2 & 3; // (0 : evenly split, 1 : one item by slot, 2 : creative)
        int dragEvent = dragType & 3; // (0 : start drag, 1 : add slot, 2 : end drag)

        switch (dragEvent) {
            case 0: // start drag
                return this.clickHandlers.stream().allMatch(h -> h.handle(container, ClickTypes.DRAG_START));
            case 1: // add drag
                SlotIndex idx = SlotIndex.of(slotId);
                Optional<org.spongepowered.api.item.inventory.Slot> slot = container.getSlot(idx);
                if (slot.isPresent()) {
                    switch (dragMode) {
                        case 0:
                            return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot.get(), idx, ClickTypes.DRAG_LEFT_ADD));
                        case 1:
                            return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot.get(), idx, ClickTypes.DRAG_RIGHT_ADD));
                        case 2:
                            return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot.get(), idx, ClickTypes.DRAG_MIDDLE_ADD));
                    }
                }
                break;
            case 2: // end drag
                return this.clickHandlers.stream().allMatch(h -> h.handle(container, ClickTypes.DRAG_END));
        }
        return true;
    }

    private boolean onClickRight(ClickType clickTypeIn, Container container, SlotIndex idx, org.spongepowered.api.item.inventory.Slot slot) {
        switch (clickTypeIn) {
            case PICKUP:
                return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot, idx, ClickTypes.CLICK_RIGHT));
            case QUICK_MOVE:
                return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot, idx, ClickTypes.SHIFT_CLICK_RIGHT));
            case THROW:
                // TODO empty cursor check?
                return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot, idx, ClickTypes.KEY_THROW_ALL));

        }
        return true;
    }

    private Boolean onClickLeft(ClickType clickTypeIn, Container container, SlotIndex idx, org.spongepowered.api.item.inventory.Slot slot) {
        switch (clickTypeIn) {
            case PICKUP:
                return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot, idx, ClickTypes.CLICK_LEFT));
            case QUICK_MOVE:
                return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot, idx, ClickTypes.SHIFT_CLICK_LEFT));
            case THROW:
                // TODO empty cursor check?
                return this.slotClickHandlers.stream().allMatch(h -> h.handle(container, slot, idx, ClickTypes.KEY_THROW_ONE));
        }
        return true;
    }

    public boolean onChange(ItemStack itemstack, ItemStack oldStack, Container mixinContainer, int slotIndex, Slot slot) {
        // TODO change callbacks
        return true;
    }
}
