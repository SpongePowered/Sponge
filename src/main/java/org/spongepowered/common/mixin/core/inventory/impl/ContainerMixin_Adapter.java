package org.spongepowered.common.mixin.core.inventory.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.InventoryAdapterBridge;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.SlotCollection;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DefaultEmptyLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.util.ContainerUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class ContainerMixin_Adapter implements InventoryAdapterBridge, InventoryAdapter {

    @Shadow public abstract NonNullList<ItemStack> getInventory();
    @Shadow @Final public List<Slot> inventorySlots;

    private boolean impl$isLensInitialized;
    private boolean impl$spectatorChest;

    @Override
    public void bridge$setSpectatorChest(final boolean spectatorChest) {
        this.impl$spectatorChest = spectatorChest;
    }

    @Override
    public SlotLensProvider bridge$generateSlotProvider() {
        return ContainerUtil.countSlots((Container) (Object) this, this.bridge$getFabric());
    }


    @Inject(method = "addSlot", at = @At(value = "HEAD"))
    private void impl$onAddSlotToContainer(final Slot slotIn, final CallbackInfoReturnable<Slot> cir) {
        this.impl$isLensInitialized = false;
        // Reset the lens and slot provider
        this.bridge$setSlotProvider(null);
        this.bridge$setLens(null);
        this.impl$adapters = null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Lens bridge$generateLens(SlotLensProvider slots) {
        if (this.impl$isLensInitialized) {
            return null; // Means that we've tried to generate a lens before, but it was null. And because the lens is null,
            // the generate will try again. So, we stop trying to generate it.
        }
        this.impl$isLensInitialized = true;
        final Fabric fabric = this.bridge$getFabric();
        final Lens lens;
        if (this.impl$spectatorChest) { // TODO check if this is needed - why can we not provide a basic lens?
            return null;
        }

        if (this instanceof LensProviderBridge) {
            // TODO LensProviders for all Vanilla Containers
            lens = ((LensProviderBridge) this).bridge$rootLens(fabric, this);
        } else if (this.getInventory().size() == 0) {
            lens = new DefaultEmptyLens(this); // Empty Container
        } else {
            lens = ContainerUtil.generateLens((Container) (Object) this, slots);
        }
        return lens;
    }

    @Override
    public Optional<org.spongepowered.api.item.inventory.Slot> bridge$getSlot(int ordinal) {
        // TODO clean this up
        return Optional.ofNullable(this.bridge$getContainerSlot(ordinal));
    }


    @Nullable private Map<Integer, SlotAdapter> impl$adapters;

    private Map<Integer, SlotAdapter> impl$getAdapters() {
        if (this.impl$adapters == null) {
            this.impl$adapters = new Int2ObjectArrayMap<>();
            // If we know the lens, we can cache the adapters now

            final Lens lens = this.bridge$getRootLens();
            if (lens != null) {
                final SlotCollection iter = new SlotCollection((Inventory) this, this.bridge$getFabric(), lens, this.bridge$getSlotProvider());
                for (final org.spongepowered.api.item.inventory.Slot slot : iter.slots()) {
                    this.impl$adapters.put(((SlotAdapter) slot).getOrdinal(), (SlotAdapter) slot);
                }
            }
        }
        return this.impl$adapters;
    }

    private org.spongepowered.api.item.inventory.Slot bridge$getContainerSlot(final int slot) {
        final org.spongepowered.api.item.inventory.Slot adapter = this.impl$getAdapters().get(slot);
        if (adapter == null) // Slot is not in Lens
        {
            if (slot >= this.inventorySlots.size()) {
                SpongeImpl.getLogger().warn("Could not find slot #{} in Container {}", slot, this.getClass().getName());
                return null;
            }
            final Slot mcSlot = this.inventorySlots.get(slot); // Try falling back to vanilla slot
            if (mcSlot == null) {
                SpongeImpl.getLogger().warn("Could not find slot #{} in Container {}", slot, this.getClass().getName());
                return null;
            }
            return ((org.spongepowered.api.item.inventory.Slot) mcSlot);
        }
        return adapter;
    }
}
