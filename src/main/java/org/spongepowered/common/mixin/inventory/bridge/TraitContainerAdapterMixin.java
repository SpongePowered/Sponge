package org.spongepowered.common.mixin.inventory.bridge;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.bridge.inventory.LensGeneratorBridge;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class TraitContainerAdapterMixin implements InventoryBridge, LensGeneratorBridge, InventoryAdapter, ContainerBridge {

    @Shadow public abstract NonNullList<ItemStack> getInventory();

    private boolean impl$isLensInitialized;
    private boolean impl$spectatorChest;

    @Override
    public void inventoryAdapter$setSpectatorChest(final boolean spectatorChest) {
        this.impl$spectatorChest = spectatorChest;
    }

    @Override
    public SlotLensProvider lensGeneratorBridge$generateSlotLensProvider() {
        return new LensRegistrar.BasicSlotLensProvider(this.getInventory().size());
    }

    @Inject(method = "addSlot", at = @At(value = "HEAD"))
    private void impl$onAddSlotToContainer(final Slot slotIn, final CallbackInfoReturnable<Slot> cir) {
        this.impl$isLensInitialized = false;
        this.impl$provider = null;
        this.impl$lens = null;
        this.impl$slots.clear();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Lens lensGeneratorBridge$generateLens(SlotLensProvider slotLensProvider) {
        if (this.impl$isLensInitialized) {
            return null; // Means that we've tried to generate a lens before, but it was null. And because the lens is null,
            // the generate will try again. So, we stop trying to generate it.
        }
        this.impl$isLensInitialized = true;

        if (this.impl$spectatorChest) { // TODO check if this is needed - why can we not provide a basic lens?
            return null;
        }

        return LensRegistrar.getLens(this, slotLensProvider, this.getInventory().size());
    }

    private final Map<Integer, org.spongepowered.api.item.inventory.Slot> impl$slots = new Int2ObjectArrayMap<>();

    @Override
    public Optional<org.spongepowered.api.item.inventory.Slot> inventoryAdapter$getSlot(int ordinal) {
        org.spongepowered.api.item.inventory.Slot slot = this.impl$slots.get(ordinal);
        if (slot == null) {
            Lens rootLens = this.inventoryAdapter$getRootLens();
            SlotLens slotLens = rootLens.getSlotLens(ordinal);
            slot = slotLens.getAdapter(this.inventoryAdapter$getFabric(), ((Inventory) this));
            this.impl$slots.put(ordinal, slot);
        }
        return Optional.of(slot);
    }

    @Nullable private SlotLensProvider impl$provider;
    @Nullable private Lens impl$lens;

    @Override
    public Fabric inventoryAdapter$getFabric() {
        return (Fabric) this;
    }

    @Override
    public SlotLensProvider inventoryAdapter$getSlotLensProvider() {
        if (this.impl$provider == null) {
            this.impl$provider = this.lensGeneratorBridge$generateSlotLensProvider();
        }
        return this.impl$provider;
    }

    @Override
    public Lens inventoryAdapter$getRootLens() {
        if (this.impl$lens == null) {
            this.impl$lens = this.lensGeneratorBridge$generateLens(this.inventoryAdapter$getSlotLensProvider());
        }
        return this.impl$lens;
    }

}
