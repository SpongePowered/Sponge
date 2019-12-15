/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.inventory.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.ClickHandler;
import org.spongepowered.api.item.inventory.menu.handler.CloseHandler;
import org.spongepowered.api.item.inventory.menu.handler.InventoryCallbackHandler;
import org.spongepowered.api.item.inventory.menu.handler.KeySwapHandler;
import org.spongepowered.api.item.inventory.menu.handler.SlotChangeHandler;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeInventoryMenu implements InventoryMenu {

    private ViewableInventory inventory;
    private Map<Container, Player> tracked = new HashMap<>();
    private Text title;

    @Nullable
    private SlotClickHandler slotClickHandler;
    @Nullable private ClickHandler clickHandler;
    @Nullable private KeySwapHandler keySwapHandler;
    @Nullable private SlotChangeHandler changeHandler;
    @Nullable private CloseHandler closeHandler;

    private boolean readonly;

    public SpongeInventoryMenu(ViewableInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ViewableInventory getInventory() {
        return this.inventory;
    }

    @Override
    public ContainerType getType() {
        return this.inventory.getType();
    }

    @Override
    public void setCurrentInventory(ViewableInventory inventory) {
        if (this.getType().equals(inventory.getType())) {
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
        new ArrayList<>(this.tracked.values()).stream().distinct().forEach(this::open);
    }

    public void setTitle(Text title) {
        this.title = title;
        this.reopen();
    }

    @Override
    public InventoryMenu setReadOnly(boolean readOnly) {
        this.readonly = readOnly;
        return this;
    }

    @Override
    public void registerHandler(InventoryCallbackHandler handler) {
        if (handler instanceof ClickHandler) {
            this.registerClick(((ClickHandler) handler));
        }
        if (handler instanceof SlotClickHandler) {
            this.registerSlotClick(((SlotClickHandler) handler));
        }
        if (handler instanceof KeySwapHandler) {
            this.registerKeySwap(((KeySwapHandler) handler));
        }
        if (handler instanceof CloseHandler) {
            this.registerClose(((CloseHandler) handler));
        }
        if (handler instanceof SlotChangeHandler) {
            this.registerChange(((SlotChangeHandler) handler));
        }
    }

    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public void registerClose(CloseHandler handler) {
        this.closeHandler = handler;
    }

    public void registerClick(ClickHandler handler) {
        this.clickHandler = handler;
    }
    @Override
    public void registerSlotClick(SlotClickHandler handler) {
        this.slotClickHandler = handler;
    }

    @Override
    public void registerKeySwap(KeySwapHandler handler) {
        this.keySwapHandler = handler;
    }

    public void registerChange(SlotChangeHandler handler) {
        this.changeHandler = handler;
    }

    @Override
    public void unregisterAll() {
        this.clickHandler = null;
        this.slotClickHandler = null;
        this.keySwapHandler = null;
        this.changeHandler = null;
        this.closeHandler = null;
    }

    @Override
    public Optional<Container> open(Player player) {
        Optional<Container> container = player.openInventory(this.inventory, this.title);
        container.ifPresent(c -> {
            ((TrackedContainerBridge)c).bridge$setMenu(this);
            this.tracked.put(c, player);
        });
        return container;
    }

    public void onClose(PlayerEntity player, Container container) {

        if (this.closeHandler != null) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(player);
                Cause cause = frame.getCurrentCause();
                this.closeHandler.handle(cause, container);
            }
        }
        this.tracked.remove(container);
    }

    public boolean onClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player, Container container) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            Cause cause = frame.getCurrentCause();
            if (clickTypeIn == ClickType.QUICK_CRAFT) {
                return this.onClickDrag(cause, slotId, dragType, container);
            }
            Optional<org.spongepowered.api.item.inventory.Slot> slot = container.getSlot(slotId);

            if (slot.isPresent()) {
                switch (clickTypeIn) {
                    case SWAP:
                        if (dragType >= 0 && dragType < 9) {
                            Optional<org.spongepowered.api.item.inventory.Slot> slot2 = container.getSlot(dragType);
                            if (slot2.isPresent() && this.keySwapHandler != null) {
                                return this.keySwapHandler.handle(cause, container, slot.get(), slotId, ClickTypes.KEY_SWAP, slot2.get());
                            }
                        }
                        return true;
                    case CLONE:
                        if (this.slotClickHandler != null) {
                            return this.slotClickHandler.handle(cause, container, slot.get(), slotId, ClickTypes.CLICK_MIDDLE);
                        }
                    case PICKUP_ALL:
                        if (this.slotClickHandler != null) {
                            return this.slotClickHandler.handle(cause, container, slot.get(), slotId, ClickTypes.DOUBLE_CLICK);
                        }
                    default:
                        if (this.slotClickHandler != null) {
                            if (dragType == 0) {
                                return this.onClickLeft(cause, this.slotClickHandler, clickTypeIn, container, slotId, slot.get());
                            } else if (dragType == 1) {
                                return this.onClickRight(cause, this.slotClickHandler, clickTypeIn, container, slotId, slot.get());
                            }
                            // else unknown drag-type
                        }
                        return true;
                }
            }
            // else no slot present
            switch (clickTypeIn) {
                case PICKUP:
                    if (slotId == -999 && this.clickHandler != null) {
                        if (dragType == 0) {return this.clickHandler.handle(cause, container, ClickTypes.CLICK_LEFT_OUTSIDE);
                        } else if (dragType == 1) {  return this.clickHandler.handle(cause, container, ClickTypes.CLICK_RIGHT_OUTSIDE);
                        } }  // else unknown slotId/drag-type
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
                    }        // else unknown slotId/drag-type
                    break;
            }     return true;
        }
    }

    private boolean onClickDrag(Cause cause, int slotId, int dragType, Container container) {

        int dragMode = dragType >> 2 & 3; // (0 : evenly split, 1 : one item by slot, 2 : creative)
        int dragEvent = dragType & 3; // (0 : start drag, 1 : add slot, 2 : end drag)

        switch (dragEvent) {
            case 0: // start drag
                if (this.clickHandler != null) {
                    return this.clickHandler.handle(cause, container, ClickTypes.DRAG_START);
                }
            case 1: // add drag
                Optional<org.spongepowered.api.item.inventory.Slot> slot = container.getSlot(slotId);
                if (slot.isPresent() && this.slotClickHandler != null) {
                    switch (dragMode) {
                        case 0:
                            return this.slotClickHandler.handle(cause, container, slot.get(), slotId, ClickTypes.DRAG_LEFT_ADD);
                        case 1:
                            return this.slotClickHandler.handle(cause, container, slot.get(), slotId, ClickTypes.DRAG_RIGHT_ADD);
                        case 2:
                            return this.slotClickHandler.handle(cause, container, slot.get(), slotId, ClickTypes.DRAG_MIDDLE_ADD);
                    }
                }
                break;
            case 2: // end drag
                if (this.clickHandler != null) {
                    return this.clickHandler.handle(cause, container, ClickTypes.DRAG_END);
                }
        }
        return true;
    }

    private boolean onClickRight(Cause cause, SlotClickHandler handler, ClickType clickTypeIn, Container container,
                                 int idx, org.spongepowered.api.item.inventory.Slot slot) {
        switch (clickTypeIn) {
            case PICKUP:
                return handler.handle(cause, container, slot, idx, ClickTypes.CLICK_RIGHT);
            case QUICK_MOVE:
                return handler.handle(cause, container, slot, idx, ClickTypes.SHIFT_CLICK_RIGHT);
            case THROW:
                // TODO empty cursor check?
                return handler.handle(cause, container, slot, idx, ClickTypes.KEY_THROW_ALL);

        }
        return true;
    }

    private Boolean onClickLeft(Cause cause, SlotClickHandler handler, ClickType clickTypeIn, Container container,
                                int idx, org.spongepowered.api.item.inventory.Slot slot) {
        switch (clickTypeIn) {
            case PICKUP:
                return handler.handle(cause, container, slot, idx, ClickTypes.CLICK_LEFT);
            case QUICK_MOVE:
                return handler.handle(cause, container, slot, idx, ClickTypes.SHIFT_CLICK_LEFT);
            case THROW:
                // TODO empty cursor check?
                return handler.handle(cause, container, slot, idx, ClickTypes.KEY_THROW_ONE);
        }
        return true;
    }

    public boolean onChange(ItemStack newStack, ItemStack oldStack, Container container, int slotIndex, Slot slot) {

        // readonly by default cancels top inventory changes . but can be overridden by change callbacks
        if (this.readonly && !(slot.inventory instanceof PlayerInventory)) {
            return false;
        }

        if (this.changeHandler != null) {
            Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            return this.changeHandler.handle(cause, container, ((org.spongepowered.api.item.inventory.Slot) slot), slotIndex,
                    ItemStackUtil.snapshotOf(oldStack), ItemStackUtil.snapshotOf(newStack));
        }
        return true;
    }
}
