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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(ChestTileEntity.class)
public abstract class TileEntityChestMixin extends TileEntityLockableLootMixin {

    @Shadow public float lidAngle;
    @Shadow public int numPlayersUsing;
    @Shadow public ChestTileEntity adjacentChestZNeg;
    @Shadow public ChestTileEntity adjacentChestXPos;
    @Shadow public ChestTileEntity adjacentChestXNeg;
    @Shadow public ChestTileEntity adjacentChestZPos;

    @Shadow public abstract void checkForAdjacentChests();
    @Shadow public abstract int getSizeInventory();

    @Override
    public ReusableLens<?> bridge$generateReusableLens(final Fabric fabric, final InventoryAdapter adapter) {
        return ReusableLens.getLens(GridInventoryLens.class, this, this::impl$generateSlotProvider, this::impl$generateRootLens);
    }

    private SlotProvider impl$generateSlotProvider() {
        return new SlotCollection.Builder().add(this.getSizeInventory()).build();
    }

    @SuppressWarnings("unchecked")
    private GridInventoryLens impl$generateRootLens(final SlotProvider slots) {
        final int size = this.getSizeInventory();
        return new GridInventoryLensImpl(0, 9, size / 9, 9, (Class<? extends Inventory>) this.getClass(), slots);
    }

    /**
     * @author bloodmc - July 21st, 2016
     *
     * @reason Overwritten in case chests ever attempt to tick
     */
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void impl$DisableTickingChestsOnServer(final CallbackInfo ci) {
        if (this.world == null || !this.world.field_72995_K) {
            // chests should never tick on server
            ci.cancel();
        }
    }

    @Inject(method = "openInventory",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"),
        cancellable = true)
    private void impl$Moved(final PlayerEntity player, final CallbackInfo ci) {
        // Moved out of tick loop
        if (this.world == null) {
            ci.cancel();
            return;
        }
        if (this.world.field_72995_K) {
            return;
        }

        this.checkForAdjacentChests();
        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
            this.lidAngle = 0.7F;
            double posX = this.pos.func_177958_n() + 0.5D;
            final double posY = this.pos.func_177956_o() + 0.5D;
            double posZ = this.pos.func_177952_p() + 0.5D;

            if (this.adjacentChestXPos != null) {
                posX += 0.5D;
            }

            if (this.adjacentChestZPos != null) {
                posZ += 0.5D;
            }

            this.world.func_184148_a(null, posX, posY, posZ, SoundEvents.field_187657_V, SoundCategory.BLOCKS, 0.5F, this.world.field_73012_v.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Inject(
        method = "closeInventory",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"),
        cancellable = true)
    private void impl$MovedSoundOutofTickLoop(final PlayerEntity player, final CallbackInfo ci) {
        // Moved out of tick loop
        if (this.world == null) {
            ci.cancel();
            return;
        }
        if (this.world.field_72995_K) {
            return;
        }

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            final float f = 0.1F;

            if (this.numPlayersUsing > 0) {
                this.lidAngle += f;
            } else {
                this.lidAngle = 0.0f;
            }

            double posX = this.pos.func_177958_n() + 0.5D;
            final double posY = this.pos.func_177956_o() + 0.5D;
            double posZ = this.pos.func_177952_p() + 0.5D;

            if (this.adjacentChestXPos != null) {
                posX += 0.5D;
            }

            if (this.adjacentChestZPos != null) {
                posZ += 0.5D;
            }

            this.world.func_184148_a(null, posX, posY, posZ, SoundEvents.field_187651_T, SoundCategory.BLOCKS, 0.5F, this.world.field_73012_v.nextFloat() * 0.1F + 0.9F);
        }
    }

}

