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
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;
import org.spongepowered.api.block.tileentity.carrier.ShulkerBox;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntityShulkerBox;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;

import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(TileEntityShulkerBox.class)
@Implements(@Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$"))
public abstract class MixinTileEntityShulkerBox extends MixinTileEntityLockableLoot implements ShulkerBox, IMixinTileEntityShulkerBox {

    @Shadow
    @Nullable
    private EnumDyeColor color;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this);
        this.slots = new SlotCollection.Builder().add(27).build();
        this.lens = new GridInventoryLensImpl(0, 9, 3, 9, this.slots);
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        if (this.color != null) {
            manipulators.add(new SpongeDyeableData((DyeColor) (Object) this.color));
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

    @Override
    public void setColor(@Nullable EnumDyeColor color) {
        this.color = color;
    }

    @Override
    @Nullable
    public EnumDyeColor getColor() {
        return this.color;
    }

}
