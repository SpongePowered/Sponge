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

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.block.tileentity.carrier.Beacon;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinTileEntityBeacon;
import org.spongepowered.common.interfaces.data.IMixinCustomNameable;
import org.spongepowered.common.item.inventory.adapter.impl.slots.InputSlotAdapter;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.slots.InputSlotLensImpl;

import java.util.List;

@NonnullByDefault
@Mixin(TileEntityBeacon.class)
public abstract class MixinTileEntityBeacon extends MixinTileEntityLockable implements Beacon, IMixinCustomNameable, IMixinTileEntityBeacon {

    @Shadow private Potion primaryEffect;
    @Shadow private Potion secondaryEffect;
    @Shadow private int levels;
    @Shadow private String customName;
    @Override @Shadow public abstract boolean isItemValidForSlot(int index, ItemStack stack);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        InputSlotLensImpl lens = new InputSlotLensImpl(0, itemStack -> isItemValidForSlot(0, (ItemStack) itemStack),
                itemType -> isItemValidForSlot(0, (ItemStack) org.spongepowered.api.item.inventory.ItemStack.of(itemType, 1)));

        this.slots = new SlotCollection.Builder()
                .add(InputSlotAdapter.class, i -> lens)
                .build();
        this.lens = lens;
    }

    @Override
    public int getCompletedLevels() {
        return this.levels < 0 ? 0 : this.levels;
    }

    // We want to preserve the normal behavior of isBeaconEffect, except when reading saved effects from NBT
    // and when setting from a Sponge DataProcessor. This ensures that vanilla in-game interactions with the beacon
    // work as normal, while still allowing plugins to set a custom potion through the API
    @Redirect(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityBeacon;isBeaconEffect(I)Lnet/minecraft/potion/Potion;", ordinal = 0))
    private Potion onFirstIsBeaconEffect(int id) {
        return Potion.getPotionById(id);
    }

    @Redirect(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityBeacon;isBeaconEffect(I)Lnet/minecraft/potion/Potion;", ordinal = 1))
    private Potion oSecondIsBeaconEffect(int id) {
        return Potion.getPotionById(id);
    }


    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(of("effect1"), getField(1));
        container.set(of("effect2"), getField(2));
        return container;
    }

    @Override
    public void sendDataToContainer(DataView dataView) {
        dataView.set(of("effect1"), getField(1));
        dataView.set(of("effect2"), getField(2));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getBeaconData());
    }

    @Override
    public void setCustomDisplayName(String customName) {
        ((TileEntityBeacon) (Object) this).setName(customName);
    }

    @Override
    public void forceSetPrimaryEffect(Potion potion) {
        this.primaryEffect = potion;
    }

    @Override
    public void forceSetSecondaryEffect(Potion potion) {
        this.secondaryEffect = potion;
    }
}
