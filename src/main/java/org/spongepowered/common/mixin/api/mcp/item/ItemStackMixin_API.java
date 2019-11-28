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
package org.spongepowered.common.mixin.api.mcp.item;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.item.ItemBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(net.minecraft.item.ItemStack.class)
@Implements(@Interface(iface = ItemStack.class, prefix = "apiStack$")) // We need to soft implement this interface due to a synthetic bridge method
public abstract class ItemStackMixin_API implements DataHolder {       // conflict from overriding ValueContainer#copy() from DataHolder

    @Shadow public abstract int getCount();
    @Shadow public abstract void setCount(int size); // Do not use field directly as Minecraft tracks the empty state
    @Shadow public abstract void setItemDamage(int meta);
    @Shadow public abstract void setTagCompound(@Nullable CompoundNBT compound);
    @Shadow public abstract int getItemDamage();
    @Shadow public abstract int getMaxStackSize();
    @Shadow public abstract boolean hasTagCompound();
    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract CompoundNBT getTagCompound();
    @Shadow public abstract net.minecraft.item.ItemStack shadow$copy();
    @Shadow public abstract Item shadow$getItem();

    public int apiStack$getQuantity() {
        return this.getCount();
    }

    public ItemType apiStack$getType() {
        return (ItemType) shadow$getItem();
    }

    public void apiStack$setQuantity(int quantity) throws IllegalArgumentException {
        this.setCount(quantity);
    }

    public int apiStack$getMaxStackQuantity() {
        return getMaxStackSize();
    }

    @Override
    public boolean validateRawData(DataView container) {
        return false;
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        if (this.shadow$isEmpty()) {
            throw new IllegalArgumentException("Cannot set data on empty item stacks!");
        }
        if (!container.contains(Constants.Sponge.UNSAFE_NBT)) {
            throw new InvalidDataException("There's no NBT Data set in the provided container");
        }
        final DataView nbtData = container.getView(Constants.Sponge.UNSAFE_NBT).get();
        try {
            final int integer = container.getInt(Constants.ItemStack.DAMAGE_VALUE).orElse(this.getItemDamage());
            this.setItemDamage(integer);
            final CompoundNBT stackCompound = NbtTranslator.getInstance().translate(nbtData);
            this.setTagCompound(stackCompound);
        } catch (Exception e) {
            throw new InvalidDataException("Unable to set raw data or translate raw data for ItemStack setting", e);
        }
    }

    @Override
    public DataHolder copy() {
        return this.apiStack$copy();
    }

    public ItemStack apiStack$copy() {
        return (ItemStack) shadow$copy();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(Constants.ItemStack.TYPE, this.apiStack$getType().getId())
                .set(Constants.ItemStack.COUNT, this.apiStack$getQuantity())
                .set(Constants.ItemStack.DAMAGE_VALUE, this.getItemDamage());
        if (hasTagCompound()) { // no tag? no data, simple as that.
            final CompoundNBT compound = getTagCompound().copy();
            if (compound.contains(Constants.Sponge.SPONGE_DATA)) {
                final CompoundNBT spongeCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);
                if (spongeCompound.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST)) {
                    spongeCompound.remove(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST);
                }
            }
            Constants.NBT.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
            if (!compound.isEmpty()) {
                final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
                container.set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);
            }
        }
        // We only need to include the custom data, not vanilla manipulators supported by sponge implementation
        final Collection<DataManipulator<?, ?>> manipulators = ((CustomDataHolderBridge) this).bridge$getCustomManipulators();
        if (!manipulators.isEmpty()) {
            container.set(Constants.Sponge.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        try {
            SpongeImplHooks.writeItemStackCapabilitiesToDataView(container, (net.minecraft.item.ItemStack) (Object) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return container;
    }

    public Translation apiStack$getTranslation() {
        return new SpongeTranslation(shadow$getItem().getTranslationKey((net.minecraft.item.ItemStack) (Object) this) + ".name");
    }

    public ItemStackSnapshot apiStack$createSnapshot() {
        return new SpongeItemStackSnapshot((ItemStack) this);
    }

    public boolean apiStack$equalTo(ItemStack that) {
        return net.minecraft.item.ItemStack.areItemStacksEqual(
                (net.minecraft.item.ItemStack) (Object) this,
                (net.minecraft.item.ItemStack) that
        );
    }

    @Intrinsic
    public boolean apiStack$isEmpty() {
        return this.shadow$isEmpty();
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        if (this.shadow$isEmpty()) {
            return Lists.newArrayList();
        }
        final List<DataManipulator<?, ?>> manipulators = Lists.newArrayList();
        final Item item = this.shadow$getItem();
        // Null items should be impossible to create
        if (item == null) {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Null Item found!").centre().hr();
            printer.add("An ItemStack has a null ItemType! This is usually not supported as it will likely have issues elsewhere.");
            printer.add("Please ask help for seeing if this is an issue with a mod and report it!");
            printer.add("Printing a Stacktrace:");
            printer.add(new Exception());
            printer.log(SpongeImpl.getLogger(), Level.WARN);
            return manipulators;
        }
        ((ItemBridge) item).bridge$gatherManipulators((net.minecraft.item.ItemStack) (Object) this, manipulators);
        if (((CustomDataHolderBridge) this).bridge$hasManipulators()) {
            final Collection<DataManipulator<?, ?>> customManipulators = ((CustomDataHolderBridge) this).bridge$getCustomManipulators();
            manipulators.addAll(customManipulators);
        }
        return manipulators;
    }

}
