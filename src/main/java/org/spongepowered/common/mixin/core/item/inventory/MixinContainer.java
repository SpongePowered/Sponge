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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftFabric;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.util.ContainerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NonnullByDefault
@Mixin(Container.class)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$")})
public abstract class MixinContainer implements org.spongepowered.api.item.inventory.Container, IMixinContainer {

    @Shadow public List<Slot> inventorySlots;
    @Shadow public List<ItemStack> inventoryItemStacks;
    @Shadow public int windowId;
    @Shadow protected List<IContainerListener> listeners;

    @SuppressWarnings("rawtypes")
    @Shadow
    public abstract List getInventory();

    @Shadow
    public abstract Slot getSlot(int slotId);

    private Container this$ = (Container) (Object) this;

    private boolean captureInventory = false;
    private List<SlotTransaction> capturedSlotTransactions = new ArrayList<>();
    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;
    private boolean initialized;
    private Map<Integer, SlotAdapter> adapters = new HashMap<>();
    private InventoryArchetype archetype;
    protected Optional<Carrier> carrier;

    private void init() {
        this.initialized = true;
        this.fabric = MinecraftFabric.of(this.this$);
        this.slots = ContainerUtil.countSlots(this.this$);
        this.lens = ContainerUtil.getLens(this.this$, this.slots);
        this.archetype = ContainerUtil.getArchetype(this$);
        this.carrier = Optional.ofNullable(ContainerUtil.getCarrier(this));

        // If we know the lens, we can cache the adapters now
        if (this.lens != null) {
            for (org.spongepowered.api.item.inventory.Slot slot : this.slots.getIterator(this, (MinecraftInventoryAdapter) this)) {
                this.adapters.put(((SlotAdapter) slot).slotNumber, (SlotAdapter) slot);
            }
        }
    }

    @Override
    public InventoryArchetype getArchetype() {
        if (!this.initialized) {
            this.init();
        }
        return this.archetype;
    }

    /**
     * @author bloodmc
     * @reason As we do not create a new player object on respawn, we
     * need to update the client with changes if listener already
     * exists.
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public void addListener(IContainerListener listener) {
        Container container = (Container) (Object) this;
        if (this.listeners.contains(listener)) {
            // Sponge start
            // throw new IllegalArgumentException("Listener already listening");
            listener.updateCraftingInventory(container, this.getInventory());
            container.detectAndSendChanges();
            // Sponge end
        } else {
            this.listeners.add(listener);
            listener.updateCraftingInventory(container, this.getInventory());
            container.detectAndSendChanges();
        }
    }

    /**
     * @author bloodmc
     * @reason All player fabric changes that need to be synced to
     * client flow through this method. Overwrite is used as no mod
     * should be touching this method.
     *
     */
    @Overwrite
    public void detectAndSendChanges() {
        if (!this.initialized) {
            this.init();
        }

        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            final Slot slot = this.inventorySlots.get(i);
            final ItemStack itemstack = slot.getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {

                // Sponge start
                if (this.captureInventory) {
                    final ItemStackSnapshot originalItem = itemstack1 == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack1).createSnapshot();
                    final ItemStackSnapshot newItem = itemstack == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                    SlotAdapter adapter = this.adapters.get(i);

                    // TODO If slotid is not in adapters map, either no lens is known and we didn't cache or this is a newly added slot. The
                    // TODO following is fallback code until a fallback container lens is added which would be sensitive to these changes.
                    if (adapter == null) {
                        adapter = new SlotAdapter(slot);
                        this.adapters.put(i, adapter);
                    }

                    this.capturedSlotTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
                }
                // Sponge end

                itemstack1 = itemstack == null ? null : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (IContainerListener listener : this.listeners) {
                    listener.sendSlotContents((Container) (Object) this, i, itemstack1);
                }
            }
        }
    }

    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    public void onPutStackInSlot(int slotId, ItemStack itemstack, CallbackInfo ci) {
        if (this.captureInventory) {
            if (!this.initialized) {
                this.init();
            }

            final Slot slot = getSlot(slotId);
            if (slot != null) {
                ItemStackSnapshot originalItem = slot.getStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
                ItemStackSnapshot newItem =
                        itemstack == null ? ItemStackSnapshot.NONE : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();

                SlotAdapter adapter = this.adapters.get(slotId);

                // TODO If slotid is not in adapters map, either no lens is known and we didn't cache or this is a newly added slot. The
                // TODO following is fallback code until a fallback container lens is added which would be sensitive to these changes.
                if (adapter == null) {
                    adapter = new SlotAdapter(slot);
                    this.adapters.put(slotId, adapter);
                }

                this.capturedSlotTransactions.add(new SlotTransaction(adapter, originalItem, newItem));
            }
        }
    }

    @Override
    public boolean capturingInventory() {
        return this.captureInventory;
    }

    @Override
    public void setCaptureInventory(boolean flag) {
        this.captureInventory = flag;
    }

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedSlotTransactions;
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        if (!this.initialized) {
            this.init();
        }
        return this.slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        if (!this.initialized) {
            this.init();
        }
        return this.lens;
    }

    public Fabric<IInventory> inventory$getInventory() {
        if (!this.initialized) {
            this.init();
        }
        return this.fabric;
    }
}

