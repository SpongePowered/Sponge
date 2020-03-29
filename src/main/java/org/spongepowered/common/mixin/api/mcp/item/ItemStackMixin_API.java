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

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(net.minecraft.item.ItemStack.class)
@Implements(@Interface(iface = ItemStack.class, prefix = "apiStack$")) // We need to soft implement this interface due to a synthetic bridge method
public abstract class ItemStackMixin_API implements SerializableDataHolder.Mutable {       // conflict from overriding ValueContainer#copy() from DataHolder

    @Shadow public abstract int shadow$getCount();
    @Shadow public abstract void shadow$setCount(int size); // Do not use field directly as Minecraft tracks the empty state
    @Shadow public abstract void shadow$setDamage(int meta);
    @Shadow public abstract void shadow$setTag(@Nullable CompoundNBT compound);
    @Shadow public abstract int shadow$getDamage();
    @Shadow public abstract int shadow$getMaxStackSize();
    @Shadow public abstract boolean shadow$hasTag();
    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract CompoundNBT shadow$getTag();
    @Shadow public abstract net.minecraft.item.ItemStack shadow$copy();
    @Shadow public abstract Item shadow$getItem();

    public int apiStack$getQuantity() {
        return this.shadow$getCount();
    }

    public ItemType apiStack$getType() {
        return (ItemType) this.shadow$getItem();
    }

    public void apiStack$setQuantity(final int quantity) throws IllegalArgumentException {
        this.shadow$setCount(quantity);
    }

    public int apiStack$getMaxStackQuantity() {
        return this.shadow$getMaxStackSize();
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return false;
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        if (this.shadow$isEmpty()) {
            throw new IllegalArgumentException("Cannot set data on empty item stacks!");
        }
        if (!container.contains(Constants.Sponge.UNSAFE_NBT)) {
            throw new InvalidDataException("There's no NBT Data set in the provided container");
        }
        final DataView nbtData = container.getView(Constants.Sponge.UNSAFE_NBT).get();
        try {
            final int integer = container.getInt(Constants.ItemStack.DAMAGE_VALUE).orElse(this.shadow$getDamage());
            this.shadow$setDamage(integer);
            final CompoundNBT stackCompound = NbtTranslator.getInstance().translate(nbtData);
            this.shadow$setTag(stackCompound);
        } catch (final Exception e) {
            throw new InvalidDataException("Unable to set raw data or translate raw data for ItemStack setting", e);
        }
    }

    @Override
    public SerializableDataHolder.Mutable copy() {
        return this.apiStack$copy();
    }

    public ItemStack apiStack$copy() {
        return (ItemStack) (Object) this.shadow$copy();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(Constants.ItemStack.TYPE, this.apiStack$getType().getKey())
                .set(Constants.ItemStack.COUNT, this.apiStack$getQuantity())
                .set(Constants.ItemStack.DAMAGE_VALUE, this.shadow$getDamage());
        if (this.shadow$hasTag()) { // no tag? no data, simple as that.
            final CompoundNBT compound = this.shadow$getTag().copy();
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
//        final Collection<Mutable<?, ?>> manipulators = ((CustomDataHolderBridge) this).bridge$getCustomManipulators();
//        if (!manipulators.isEmpty()) {
//            container.set(Constants.Sponge.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
//        }
        try {
            SpongeImplHooks.writeItemStackCapabilitiesToDataView(container, (net.minecraft.item.ItemStack) (Object) this);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return container;
    }

    public Translation apiStack$getTranslation() {
        return new SpongeTranslation(this.shadow$getItem().getTranslationKey((net.minecraft.item.ItemStack) (Object) this) + ".name");
    }

    public ItemStackSnapshot apiStack$createSnapshot() {
//        return new SpongeItemStackSnapshot((ItemStack) this);
        throw new UnsupportedOperationException("Reimplement me!");
    }

    public boolean apiStack$equalTo(final ItemStack that) {
        return net.minecraft.item.ItemStack.areItemStacksEqual(
                (net.minecraft.item.ItemStack) (Object) this,
                (net.minecraft.item.ItemStack) (Object) that
        );
    }

    @Intrinsic
    public boolean apiStack$isEmpty() {
        return this.shadow$isEmpty();
    }

}
