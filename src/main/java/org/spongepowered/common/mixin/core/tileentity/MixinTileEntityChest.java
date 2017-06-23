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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.ILockableContainer;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.ConnectedDirectionData;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(TileEntityChest.class)
public abstract class MixinTileEntityChest extends MixinTileEntityLockableLoot implements Chest, ILockableContainer {

    @Shadow public float lidAngle;
    @Shadow public int numPlayersUsing;
    @Shadow public TileEntityChest adjacentChestZNeg;
    @Shadow public TileEntityChest adjacentChestXPos;
    @Shadow public TileEntityChest adjacentChestXNeg;
    @Shadow public TileEntityChest adjacentChestZPos;

    @Shadow public abstract void checkForAdjacentChests();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        ReusableLens<? extends Lens<IInventory, ItemStack>> reusableLens = MinecraftLens.getLens(GridInventoryLens.class,
                ((InventoryAdapter) this),
                s -> new GridInventoryLensImpl(0, 9, 3, 9, s),
                () -> new SlotCollection.Builder().add(27).build());
        this.slots = reusableLens.getSlots();
        this.lens = reusableLens.getLens();
    }

    /**
     * @author bloodmc - July 21st, 2016
     *
     * @reason Overwritten in case chests ever attempt to tick
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if (this.world == null || !this.world.isRemote) {
            // chests should never tick on server
            ci.cancel();
        }
    }

    @Inject(method = "openInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"), cancellable = true)
    public void onOpenInventory(EntityPlayer player, CallbackInfo ci) {
        // Moved out of tick loop
        if (this.world == null) {
            ci.cancel();
            return;
        }
        if (this.world.isRemote) {
            return;
        }

        this.checkForAdjacentChests();
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
            this.lidAngle = 0.7F;
            double posX = this.pos.getX() + 0.5D;
            double posY = this.pos.getY() + 0.5D;
            double posZ = this.pos.getZ() + 0.5D;

            if (this.adjacentChestXPos != null) {
                posX += 0.5D;
            }

            if (this.adjacentChestZPos != null) {
                posZ += 0.5D;
            }

            this.world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Inject(method = "closeInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"), cancellable = true)
    public void onCloseInventory(EntityPlayer player, CallbackInfo ci) {
        // Moved out of tick loop
        if (this.world == null) {
            ci.cancel();
            return;
        }
        if (this.world.isRemote) {
            return;
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float f = 0.1F;

            if (this.numPlayersUsing > 0) {
                this.lidAngle += f;
            } else {
                this.lidAngle = 0.0f;
            }

            double posX = this.pos.getX() + 0.5D;
            double posY = this.pos.getY() + 0.5D;
            double posZ = this.pos.getZ() + 0.5D;

            if (this.adjacentChestXPos != null) {
                posX += 0.5D;
            }

            if (this.adjacentChestZPos != null) {
                posZ += 0.5D;
            }

            this.world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
        }
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
    public Optional<Inventory> getDoubleChestInventory() {
        return InventoryUtil.getDoubleChestInventory(((TileEntityChest)(Object) this));
    }

}

