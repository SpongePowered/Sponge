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

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

@SuppressWarnings("serial")
@NonnullByDefault
@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack implements ItemStack {

    @Shadow public int stackSize;

    @Shadow public abstract int getItemDamage();
    @Shadow public abstract void setItemDamage(int meta);
    @Shadow public abstract int getMaxStackSize();
    @Shadow public abstract NBTTagCompound getTagCompound();
    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.item.ItemStack shadow$copy();
    @Shadow(prefix = "shadow$")
    public abstract Item shadow$getItem();

    @Override
    public ItemType getItem() {
        return (ItemType) shadow$getItem();
    }

    @Override
    public int getQuantity() {
        return this.stackSize;
    }

    @Override
    public void setQuantity(int quantity) throws IllegalArgumentException {
        if (quantity > this.getMaxStackQuantity()) {
            throw new IllegalArgumentException("Quantity (" + quantity + ") exceeded the maximum stack size (" + this.getMaxStackQuantity() + ")");
        } else {
            this.stackSize = quantity;
        }
    }

    @Override
    public int getMaxStackQuantity() {
        return getMaxStackSize();
    }

    @Override
    public boolean validateRawData(DataContainer container) {
        return false;
    }

    @Override
    public void setRawData(DataContainer container) throws InvalidDataException {

    }

    @Override
    public ItemStack copy() {
        return (ItemStack) shadow$copy();
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = new MemoryDataContainer();
        for (DataManipulator<?, ?> manipulator : getContainers()) {
            container.set(of(manipulator.getClass().getCanonicalName()), manipulator.toContainer());
        }
        return new MemoryDataContainer()
                .set(of("ItemType"), this.getItem().getId())
                .set(of("Quantity"), this.getQuantity())
                .set(of("Data"), container);
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(shadow$getItem().getUnlocalizedName((net.minecraft.item.ItemStack) (Object) this) + ".name");
    }

    @Override
    public Text toText() {
        TextBuilder builder;
        Optional<DisplayNameData> optName = get(DisplayNameData.class);
        if (optName.isPresent()) {
            Value<Text> displayName = optName.get().displayName();
            if (displayName.exists()) {
                builder = displayName.get().builder();
            } else {
                builder = Texts.builder(getTranslation());
            }
        } else {
            builder = Texts.builder(getTranslation());
        }
        builder.onHover(TextActions.showItem(this));
        return builder.build();
    }

}
