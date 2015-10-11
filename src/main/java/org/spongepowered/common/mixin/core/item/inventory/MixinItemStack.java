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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.item.IMixinItem;
import org.spongepowered.common.interfaces.item.IMixinItemStack;
import org.spongepowered.common.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.service.persistence.NbtTranslator;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
@NonnullByDefault
@Mixin(net.minecraft.item.ItemStack.class)
public abstract class MixinItemStack implements ItemStack, IMixinItemStack, IMixinCustomDataHolder {

    @Shadow public int stackSize;

    @Shadow public abstract void setItemDamage(int meta);
    @Shadow public abstract void setTagCompound(NBTTagCompound compound);
    @Shadow public abstract void setTagInfo(String key, NBTBase nbtBase);
    @Shadow public abstract int getItemDamage();
    @Shadow public abstract int getMaxStackSize();
    @Shadow public abstract boolean hasTagCompound();
    @Shadow public abstract NBTTagCompound getTagCompound();
    @Shadow public abstract NBTTagCompound getSubCompound(String key, boolean create);
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Shadow(prefix = "shadow$")
    public abstract net.minecraft.item.ItemStack shadow$copy();
    @Shadow(prefix = "shadow$")
    public abstract Item shadow$getItem();

    @Inject(method = "writeToNBT", at = @At(value = "HEAD"))
    private void onWrite(NBTTagCompound incoming, CallbackInfoReturnable<NBTTagCompound> info) {
        if (this.hasManipulators()) {
            writeToNbt(incoming);
        }
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void onRead(NBTTagCompound compound, CallbackInfo info) {
        if (hasTagCompound() && getTagCompound().hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            readFromNbt(getTagCompound().getCompoundTag(NbtDataUtil.SPONGE_DATA));
        }
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void onCopy(CallbackInfoReturnable<net.minecraft.item.ItemStack> info) {
        final net.minecraft.item.ItemStack itemStack = info.getReturnValue();
        if (hasManipulators()) { // no manipulators? no problem.
            for (DataManipulator<?, ?> manipulator : this.manipulators) {
                ((IMixinCustomDataHolder) itemStack).offerCustom(manipulator.copy(), MergeFunction.IGNORE_ALL);
            }
        }
    }

    @Inject(method = "splitStack", at = @At("RETURN"))
    private void onSplit(int amount, CallbackInfoReturnable<net.minecraft.item.ItemStack> info) {
        final net.minecraft.item.ItemStack itemStack = info.getReturnValue();
        if (hasManipulators()) {
            for (DataManipulator<?, ?> manipulator : this.manipulators) {
                ((IMixinCustomDataHolder) itemStack).offerCustom(manipulator.copy(), MergeFunction.IGNORE_ALL);
            }
        }
    }

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
        final DataContainer container = new MemoryDataContainer()
                .set(DataQueries.ITEM_TYPE, this.getItem().getId())
                .set(DataQueries.ITEM_COUNT, this.getQuantity())
                .set(DataQueries.ITEM_DAMAGE_VALUE, this.getItemDamage());
        if (hasTagCompound()) { // no tag? no data, simple as that.
            final NBTTagCompound compound = (NBTTagCompound) getTagCompound().copy();
            NbtDataUtil.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
            if (!compound.hasNoTags()) {
                final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
                container.set(DataQueries.UNSAFE_NBT, unsafeNbt);
            }
        }
        final Collection<DataManipulator<?, ?>> manipulators = getContainers();
        if (!manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        return container;
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

    @Override
    public ItemStackSnapshot createSnapshot() {
        return new SpongeItemStackSnapshot(this);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> manipulators = Lists.newArrayList();
        ((IMixinItem) this.getItem()).getManipulatorsFor((net.minecraft.item.ItemStack) (Object) this, manipulators);
        if (hasManipulators()) {
            final List<DataManipulator<?, ?>> customManipulators = this.getCustomManipulators();
            manipulators.addAll(customManipulators);
        }
        return manipulators;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        if (compound.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_LIST)) {
            final NBTTagList list = compound.getTagList(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_COMPOUND);
            if (!list.hasNoTags()) {
                compound.removeTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST);
                final List<DataView> views = Lists.newArrayList();
                for (int i = 0; i < list.tagCount() - 1; i++) {
                    final NBTTagCompound dataCompound = list.getCompoundTagAt(i);
                    views.add(NbtTranslator.getInstance().translateFrom(dataCompound));
                }
                final List<DataManipulator<?, ?>> manipulators = DataUtil.deserializeManipulatorList(views);
                for (DataManipulator<?, ?> manipulator : manipulators) {
                    offerCustom(manipulator, MergeFunction.IGNORE_ALL);
                }
            } else {
                compound.removeTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST);
                if (compound.hasNoTags()) {
                    getTagCompound().removeTag(NbtDataUtil.SPONGE_DATA);
                    return;
                }
            }
        }
        if (compound.hasNoTags()) {
            getTagCompound().removeTag(NbtDataUtil.SPONGE_DATA);
            if (getTagCompound().hasNoTags()) {
                setTagCompound(null);
            }
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        resyncCustomToTag();
    }

    private List<DataManipulator<?, ?>> manipulators = Lists.newArrayList();

    @Override
    public DataTransactionResult offerCustom(DataManipulator<?, ?> manipulator, MergeFunction function) {
        @Nullable DataManipulator<?, ?> existingManipulator = null;
        for (DataManipulator<?, ?> existing : this.manipulators) {
            if (manipulator.getClass().isInstance(existing)) {
                existingManipulator = existing;
                break;
            }
        }
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
        final DataManipulator<?, ?> newManipulator = checkNotNull(function.merge(existingManipulator, (DataManipulator) manipulator.copy()));
        if (existingManipulator != null) {
            builder.replace(existingManipulator.getValues());
            this.manipulators.remove(existingManipulator);
        }
        this.manipulators.add(newManipulator);
        resyncCustomToTag();
        return builder.success(newManipulator.getValues())
            .result(DataTransactionResult.Type.SUCCESS)
            .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getCustom(Class<T> customClass) {
        for (DataManipulator<?, ?> existing : this.manipulators) {
            if (customClass.isInstance(existing)) {
                return Optional.of((T) existing.copy());
            }
        }
        return Optional.empty();
    }

    private void resyncCustomToTag() {
        if (!this.manipulators.isEmpty()) {
            final NBTTagList newList = new NBTTagList();
            final List<DataView> manipulatorViews = DataUtil.getSerializedManipulatorList(this.getCustomManipulators());
            for (DataView dataView : manipulatorViews) {
                newList.appendTag(NbtTranslator.getInstance().translateData(dataView));
            }
            final NBTTagCompound spongeCompound = getSubCompound(NbtDataUtil.SPONGE_DATA, true);
            spongeCompound.setTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, newList);
        } else {
            if (hasTagCompound()) {
                this.getTagCompound().removeTag(NbtDataUtil.SPONGE_DATA);
            }
            if (this.getTagCompound().hasNoTags()) {
                this.setTagCompound(null);
            }
        }
    }

    @Override
    public DataTransactionResult removeCustom(Class<? extends DataManipulator<?, ?>> customClass) {
        @Nullable DataManipulator<?, ?> manipulator = null;
        for (DataManipulator<?, ?> existing : this.manipulators) {
            if (customClass.isInstance(existing)) {
                manipulator = existing;
            }
        }
        if (manipulator != null) {
            this.manipulators.remove(manipulator);
            resyncCustomToTag();
            return DataTransactionBuilder.builder().replace(manipulator.getValues()).result(DataTransactionResult.Type.SUCCESS).build();
        } else {
            return DataTransactionBuilder.failNoData();
        }
    }

    @Override
    public boolean hasManipulators() {
        return !this.manipulators.isEmpty();
    }

    @Override
    public List<DataManipulator<?, ?>> getCustomManipulators() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        for (DataManipulator<?, ?> manipulator : this.manipulators) {
            list.add(manipulator.copy());
        }
        return list;
    }

    @Override
    public <E> DataTransactionResult offerCustom(Key<? extends BaseValue<E>> key, E value) {
        for (DataManipulator<?, ?> manipulator : this.manipulators) {
            if (manipulator.supports(key)) {
                final DataTransactionBuilder builder = DataTransactionBuilder.builder();
                builder.replace(((Value) manipulator.getValue((Key) key).get()).asImmutable());
                manipulator.set(key, value);
                builder.success(((Value) manipulator.getValue((Key) key).get()).asImmutable());
                resyncCustomToTag();
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult removeCustom(Key<?> key) {
        final Iterator<DataManipulator<?, ?>> iterator = this.manipulators.iterator();
        while (iterator.hasNext()) {
            final DataManipulator<?, ?> manipulator = iterator.next();
            if (manipulator.getKeys().size() == 1 && manipulator.supports(key)) {
                iterator.remove();
                resyncCustomToTag();
                return DataTransactionBuilder.builder()
                    .replace(manipulator.getValues())
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }
}
