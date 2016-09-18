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
package org.spongepowered.common.mixin.core.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityBrewingStand;
import org.spongepowered.api.block.tileentity.carrier.BrewingStand;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.FilteringSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.InputSlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.FilteringSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.InputSlotLensImpl;

import java.util.Optional;

@NonnullByDefault
@Mixin(TileEntityBrewingStand.class)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),
        @Interface(iface = TileEntityInventory.class, prefix = "tileentityinventory$")})
public abstract class MixinTileEntityBrewingStand extends MixinTileEntityLockable implements BrewingStand, IMixinCustomNameable {

    @Shadow private String customName;
    @Shadow public abstract boolean isItemValidForSlot(int index, ItemStack stack);

    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder().add(5)
                .add(InputSlotAdapter.class, (i) -> new InputSlotLensImpl(i, (s) -> this.isItemValidForSlot(i, (ItemStack) s), t
                        -> this.isItemValidForSlot(i, (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(t, 1))))
                .add(InputSlotAdapter.class, (i) -> new InputSlotLensImpl(i, (s) -> this.isItemValidForSlot(i, (ItemStack) s), t
                        -> this.isItemValidForSlot(i, (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(t, 1))))
                .add(FilteringSlotAdapter.class, (i) -> new FilteringSlotLensImpl(i, (s) -> this.isItemValidForSlot(i, (ItemStack) s), t
                        -> this.isItemValidForSlot(i, (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(t, 1))))
                .add(FilteringSlotAdapter.class, (i) -> new FilteringSlotLensImpl(i, (s) -> this.isItemValidForSlot(i, (ItemStack) s), t
                        -> this.isItemValidForSlot(i, (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(t, 1))))
                .add(FilteringSlotAdapter.class, (i) -> new FilteringSlotLensImpl(i, (s) -> this.isItemValidForSlot(i, (ItemStack) s), t
                        -> this.isItemValidForSlot(i, (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(t, 1))))
                .build();
        this.lens = new OrderedInventoryLensImpl(0, 5, 1, slots);
    }

    @Override
    public void sendDataToContainer(DataView dataView) {
        dataView.set(DataQueries.BLOCK_ENTITY_BREWING_TIME, this.getField(0));
        if (this.customName != null) {
            dataView.set(DataQueries.BLOCK_ENTITY_CUSTOM_NAME, this.customName);
        }
    }

    @Override
    public void setCustomDisplayName(String customName) {
        ((TileEntityBrewingStand) (Object) this).setName(customName);
    }

    @Intrinsic
    public void tilentityinventory$markDirty() {
        this.markDirty();
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        return this.slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        return this.lens;
    }

    public Fabric<IInventory> inventory$getInventory() {
        return this.fabric;
    }

    public Optional<BrewingStand> tileentityinventory$getTileEntity() {
        return Optional.of(this);
    }

    public Optional<BrewingStand> tileentityinventory$getCarrier() {
        return Optional.of(this);
    }
}
