package org.spongepowered.common.mixin.core.inventory.impl;

import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class ContainerMixin_TrackedBridge implements TrackedInventoryBridge, TrackedContainerBridge, InventoryAdapter {

    // TrackedInventoryBridge

    private List<SlotTransaction> impl$capturedSlotTransactions = new ArrayList<>();
    private boolean impl$captureInventory = false;

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedSlotTransactions;
    }

    @Override
    public boolean bridge$capturingInventory() {
        return this.impl$captureInventory;
    }

    @Override
    public void bridge$setCaptureInventory(final boolean flag) {
        this.impl$captureInventory = flag;
    }

    // TrackedContainerBridge

    private boolean impl$shiftCraft = false;

    @Override
    public void bridge$setShiftCrafting(final boolean flag) {
        this.impl$shiftCraft = flag;
    }

    @Override
    public boolean bridge$isShiftCrafting() {
        return this.impl$shiftCraft;
    }


    @Nullable private CraftItemEvent.Craft impl$lastCraft = null;

    @Override
    public void bridge$setLastCraft(final CraftItemEvent.Craft event) {
        this.impl$lastCraft = event;
    }

    @Nullable @Override
    public CraftItemEvent.Craft bridge$getLastCraft() {
        return this.impl$lastCraft;
    }

    @Nullable private ItemStack impl$previousCursor;

    @Override public void bridge$setPreviousCursor(@Nullable ItemStack stack) {
        this.impl$previousCursor = stack;
    }

    @Override
    public ItemStack bridge$getPreviousCursor() {
        return this.impl$previousCursor;
    }

    private boolean impl$firePreview = true;

    @Override
    public void bridge$setFirePreview(final boolean firePreview) {
        this.impl$firePreview = firePreview;
    }

    @Override
    public boolean bridge$firePreview() {
        return this.impl$firePreview;
    }

    private List<SlotTransaction> impl$capturedCraftPreviewTransactions = new ArrayList<>();

    @Override
    public List<SlotTransaction> bridge$getPreviewTransactions() {
        return this.impl$capturedCraftPreviewTransactions;
    }

    // Detects if a mod overrides detectAndSendChanges
    private boolean impl$captureSuccess = false;

    @Override
    public boolean bridge$capturePossible() {
        return this.impl$captureSuccess;
    }

    @Override
    public void bridge$setCapturePossible() {
        this.impl$captureSuccess = true;
    }


    @Nullable private SpongeInventoryMenu impl$menu;

    @Override
    public void bridge$setMenu(SpongeInventoryMenu menu) {
        this.impl$menu = menu;
    }

    @Nullable @Override
    public SpongeInventoryMenu bridge$getMenu() {
        return this.impl$menu;
    }

    @Nullable private Object impl$viewed;

    @Override
    public void bridge$setViewed(@Nullable Object viewed) {
        if (viewed == null) {
            this.impl$unTrackInteractable(this.impl$viewed);
        }
        this.impl$viewed = viewed;
    }

    private void impl$unTrackInteractable(@Nullable Object inventory) {
        if (inventory instanceof Carrier) {
            inventory = ((Carrier) inventory).getInventory();
        }
        if (inventory instanceof Inventory) {
            ((Inventory) inventory).asViewable().ifPresent(i -> ((ViewableInventoryBridge) i).bridge$removeContainer(((Container) (Object) this)));
        }
        // TODO else unknown inventory - try to provide wrapper Interactable
    }

}
