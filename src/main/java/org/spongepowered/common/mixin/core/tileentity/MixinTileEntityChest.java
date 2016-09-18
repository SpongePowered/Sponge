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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.item.inventory.Inventory;
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
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;
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
@Mixin(TileEntityChest.class)
@Implements({@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"),
        @Interface(iface = TileEntityInventory.class, prefix = "tileentityinventory$")})
public abstract class MixinTileEntityChest extends MixinTileEntityLockable implements Chest, IMixinCustomNameable, ILockableContainer {

    @Shadow public String customName;
    @Shadow public float lidAngle;
    @Shadow public int numPlayersUsing;
    @Shadow public TileEntityChest adjacentChestZNeg;
    @Shadow public TileEntityChest adjacentChestXPos;
    @Shadow public TileEntityChest adjacentChestXNeg;
    @Shadow public TileEntityChest adjacentChestZPos;

    @Shadow public abstract void checkForAdjacentChests();

    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder().add(27).build();
        this.lens = new GridInventoryLensImpl(0, 9, 3, 9, this.slots);
    }

    /**
     * @author bloodmc - July 21st, 2016
     *
     * @reason Overwritten in case chests ever attempt to tick
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if (this.worldObj == null || !this.worldObj.isRemote) {
            // chests should never tick on server
            ci.cancel();
        }
    }

    @Inject(method = "openInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"), cancellable = true)
    public void onOpenInventory(EntityPlayer player, CallbackInfo ci) {
        // Moved out of tick loop
        if (this.worldObj == null) {
            ci.cancel();
            return;
        }
        if (this.worldObj.isRemote) {
            return;
        }

        this.checkForAdjacentChests();
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
            this.lidAngle = 0.7F;
            double posX = (double)this.pos.getX() + 0.5D;
            double posY = (double)this.pos.getY() + 0.5D;
            double posZ = (double)this.pos.getZ() + 0.5D;

            if (this.adjacentChestXPos != null) {
                posX += 0.5D;
            }

            if (this.adjacentChestZPos != null) {
                posZ += 0.5D;
            }

            this.worldObj.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Inject(method = "closeInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"), cancellable = true)
    public void onCloseInventory(EntityPlayer player, CallbackInfo ci) {
        // Moved out of tick loop
        if (this.worldObj == null) {
            ci.cancel();
            return;
        }
        if (this.worldObj.isRemote) {
            return;
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float f = 0.1F;

            if (this.numPlayersUsing > 0) {
                this.lidAngle += f;
            } else {
                this.lidAngle -= f;
            }

            double posX = (double)this.pos.getX() + 0.5D;
            double posY = (double)this.pos.getY() + 0.5D;
            double posZ = (double)this.pos.getZ() + 0.5D;

            if (this.adjacentChestXPos != null) {
                posX += 0.5D;
            }

            if (this.adjacentChestZPos != null) {
                posZ += 0.5D;
            }

            this.worldObj.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        if (this.customName != null) {
            container.set(of("CustomName"), this.customName);
        }
        return container;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<ConnectedDirectionData> connectedChestData = get(ConnectedDirectionData.class);
        if (connectedChestData.isPresent()) {
            manipulators.add(connectedChestData.get());
        }
    }

    @Override
    public void setCustomDisplayName(String customName) {
        ((TileEntityChest) (Object) this).setCustomName(customName);
    }

    @SuppressWarnings({"RedundantCast", "unchecked"})
    @Override
    public TileEntityInventory<TileEntityCarrier> getInventory() {
        return (TileEntityInventory<TileEntityCarrier>) (Object) this;
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

    public Optional<Chest> tileentityinventory$getTileEntity() {
        return Optional.of(this);
    }

    public Optional<Chest> tileentityinventory$getCarrier() {
        return Optional.of(this);
    }

    @Override
    public Optional<Inventory> getDoubleChestInventory() {
        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockpos = this.pos.offset(enumfacing);

            TileEntity tileentity1 = this.worldObj.getTileEntity(blockpos);

            if (tileentity1 instanceof TileEntityChest) {
                InventoryLargeChest inventory;

                if (enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH)
                {
                    inventory = new InventoryLargeChest("container.chestDouble", this, (TileEntityChest)tileentity1);
                } else {
                    inventory = new InventoryLargeChest("container.chestDouble", (TileEntityChest)tileentity1, this);
                }

                return Optional.of((Inventory) inventory);
            }
        }
        return Optional.empty();
    }
}

