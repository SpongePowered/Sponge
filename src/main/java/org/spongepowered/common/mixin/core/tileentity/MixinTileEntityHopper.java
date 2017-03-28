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

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(TileEntityHopper.class)
@Implements(@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"))
public abstract class MixinTileEntityHopper extends MixinTileEntityLockableLoot implements Hopper {

    @Shadow private int transferCooldown;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder().add(5).build();
        this.lens = new GridInventoryLensImpl(0, 5, 1, 5, this.slots);
    }

    @Inject(method = "putDropInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;getEntityItem()Lnet/minecraft/item/ItemStack;"))
    private static void onPutDrop(IInventory inventory, IInventory hopper, EntityItem entityItem, CallbackInfoReturnable<Boolean> callbackInfo) {
        IMixinEntity spongeEntity = (IMixinEntity) entityItem;
        spongeEntity.getCreatorUser().ifPresent(owner -> {
            if (inventory instanceof TileEntity) {
                TileEntity te = (TileEntity) inventory;
                BlockPos pos = te.getPos();
                IMixinChunk spongeChunk = (IMixinChunk) te.getWorld().getChunkFromBlockCoords(pos);
                spongeChunk.addTrackedBlockPosition(te.getBlockType(), pos, owner, PlayerTracker.Type.NOTIFIER);
            }
        });
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        return container.set(of("TransferCooldown"), this.transferCooldown);
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<CooldownData> cooldownData = get(CooldownData.class);
        if (cooldownData.isPresent()) {
            manipulators.add(cooldownData.get());
        }
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
}
