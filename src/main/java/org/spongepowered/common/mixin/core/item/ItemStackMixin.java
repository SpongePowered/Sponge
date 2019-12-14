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
package org.spongepowered.common.mixin.core.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class ItemStackMixin implements CustomDataHolderBridge {       // conflict from overriding ValueContainer#copy() from DataHolder

    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract CompoundNBT getTagCompound();
    @Shadow public abstract CompoundNBT getOrCreateSubCompound(String key);
    @Shadow public abstract boolean hasTagCompound();
    @Shadow public abstract void setTagCompound(@Nullable CompoundNBT compound);

    @Shadow private CompoundNBT stackTagCompound;
    private List<Mutable<?, ?>> manipulators = Lists.newArrayList();
    private List<DataView> failedData = new ArrayList<>();

    @SuppressWarnings({"rawtypes", "Duplicates"})
    @Override
    public DataTransactionResult bridge$offerCustom(Mutable<?, ?> manipulator, MergeFunction function) {
        if (this.shadow$isEmpty()) {
            return DataTransactionResult.failResult(manipulator.getValues());
        }

        @Nullable Mutable<?, ?> existingManipulator = null;
        for (Mutable<?, ?> existing : this.manipulators) {
            if (manipulator.getClass().isInstance(existing)) {
                existingManipulator = existing;
                break;
            }
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final Mutable<?, ?> newManipulator = checkNotNull(function.merge(existingManipulator, (Mutable) manipulator.copy()));
        if (existingManipulator != null) {
            builder.replace(existingManipulator.getValues());
            this.manipulators.remove(existingManipulator);
        }
        this.manipulators.add(newManipulator);
        this.resyncCustomToTag();
        return builder.success(newManipulator.getValues())
            .result(DataTransactionResult.Type.SUCCESS)
            .build();
    }

    @Override
    public void bridge$addFailedData(ImmutableList<DataView> failedData) {
        this.failedData.addAll(failedData);
        this.resyncCustomToTag();
    }

    @Override
    public List<DataView> bridge$getFailedData() {
        return this.failedData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Mutable<?, ?>> Optional<T> bridge$getCustom(Class<T> customClass) {
        if (this.shadow$isEmpty()) {
            return Optional.empty();
        }
        for (Mutable<?, ?> existing : this.manipulators) {
            if (customClass.isInstance(existing)) {
                return Optional.of((T) existing.copy());
            }
        }
        return Optional.empty();
    }

    private void resyncCustomToTag() {
        if (!this.manipulators.isEmpty()) {
            final ListNBT newList = new ListNBT();
            final List<DataView> manipulatorViews = DataUtil.getSerializedManipulatorList(this.bridge$getCustomManipulators());
            for (DataView dataView : manipulatorViews) {
                newList.add(NbtTranslator.getInstance().translateData(dataView));
            }
            final CompoundNBT spongeCompound = this.getOrCreateSubCompound(Constants.Sponge.SPONGE_DATA);
            spongeCompound.put(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST, newList);
        } else if (!this.failedData.isEmpty()) {
            final ListNBT newList = new ListNBT();
            for (DataView failedDatum : this.failedData) {
                newList.add(NbtTranslator.getInstance().translateData(failedDatum));
            }
            final CompoundNBT spongeCompound = this.getOrCreateSubCompound(Constants.Sponge.SPONGE_DATA);
            spongeCompound.put(Constants.Sponge.FAILED_CUSTOM_DATA, newList);
        } else {
            if (this.hasTagCompound()) {
                this.getTagCompound().remove(Constants.Sponge.SPONGE_DATA);
                if (this.getTagCompound().isEmpty()) {
                    this.setTagCompound(null);
                }
            }
        }
    }

    @Override
    public DataTransactionResult bridge$removeCustom(Class<? extends Mutable<?, ?>> customClass) {
        if (this.shadow$isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        @Nullable Mutable<?, ?> manipulator = null;
        for (Mutable<?, ?> existing : this.manipulators) {
            if (customClass.isInstance(existing)) {
                manipulator = existing;
            }
        }
        if (manipulator != null) {
            this.manipulators.remove(manipulator);
            this.resyncCustomToTag();
            return DataTransactionResult.builder().replace(manipulator.getValues()).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean bridge$hasManipulators() {
        return !this.manipulators.isEmpty();
    }

    @Override
    public Collection<Mutable<?, ?>> bridge$getCustomManipulators() {
        return this.manipulators.stream()
            .map(Mutable::copy)
            .collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult bridge$offerCustom(Key<? extends Value<E>> key, E value) {
        if (this.shadow$isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        for (Mutable<?, ?> manipulator : this.manipulators) {
            if (manipulator.supports(key)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(((org.spongepowered.api.data.value.Value.Mutable) manipulator.getValue((Key) key).get()).asImmutable());
                manipulator.set(key, value);
                builder.success(((org.spongepowered.api.data.value.Value.Mutable) manipulator.getValue((Key) key).get()).asImmutable());
                this.resyncCustomToTag();
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failResult(new SpongeValue(key, value).asImmutable());
    }

    @Override
    public DataTransactionResult bridge$removeCustom(Key<?> key) {
        final Iterator<Mutable<?, ?>> iterator = this.manipulators.iterator();
        while (iterator.hasNext()) {
            final Mutable<?, ?> manipulator = iterator.next();
            if (manipulator.getKeys().size() == 1 && manipulator.supports(key)) {
                iterator.remove();
                this.resyncCustomToTag();
                return DataTransactionResult.builder()
                    .replace(manipulator.getValues())
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean bridge$supportsCustom(Key<?> key) {
        if (this.shadow$isEmpty()) {
            return false;
        }
        return this.manipulators.stream()
            .anyMatch(manipulator -> manipulator.supports(key));
    }

    @Override
    public <E> Optional<E> bridge$getCustom(Key<? extends Value<E>> key) {
        if (this.shadow$isEmpty()) {
            return Optional.empty();
        }
        return this.manipulators.stream()
            .filter(manipulator -> manipulator.supports(key))
            .findFirst()
            .flatMap(supported -> supported.get(key));
    }

    @Override
    public <E, V extends Value<E>> Optional<V> bridge$getCustomValue(Key<V> key) {
        if (this.shadow$isEmpty()) {
            return Optional.empty();
        }
        return this.manipulators.stream()
            .filter(manipulator -> manipulator.supports(key))
            .findFirst()
            .flatMap(supported -> supported.getValue(key));
    }

    // Add our manipulators when creating copies from this ItemStack:
    @Inject(method = "copy", at = @At("RETURN"))
    private void onCopy(CallbackInfoReturnable<ItemStack> info) {
        final net.minecraft.item.ItemStack itemStack = info.getReturnValue();
        if (this.bridge$hasManipulators()) { // no manipulators? no problem.
            for (Mutable<?, ?> manipulator : this.manipulators) {
                ((CustomDataHolderBridge) itemStack).bridge$offerCustom(manipulator.copy(), MergeFunction.IGNORE_ALL);
            }
        }
    }

    @Inject(method = "splitStack", at = @At("RETURN"))
    private void onSplit(int amount, CallbackInfoReturnable<net.minecraft.item.ItemStack> info) {
        final net.minecraft.item.ItemStack itemStack = info.getReturnValue();
        if (this.bridge$hasManipulators()) {
            for (Mutable<?, ?> manipulator : this.manipulators) {
                ((CustomDataHolderBridge) itemStack).bridge$offerCustom(manipulator.copy(), MergeFunction.IGNORE_ALL);
            }
        }
    }

    // Read custom data from nbt
    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void onRead(CompoundNBT compound, CallbackInfo info) {
        if (this.hasTagCompound() && this.getTagCompound().contains(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            DataUtil.readCustomData(this.getTagCompound().getCompound(Constants.Sponge.SPONGE_DATA), ((org.spongepowered.api.item.inventory.ItemStack) this));
        }
    }

    @Inject(method = "setTagCompound", at = @At("RETURN"))
    private void onSet(CompoundNBT compound, CallbackInfo callbackInfo) {
        if (this.stackTagCompound != compound) {
            this.manipulators.clear();
        }
        if (this.hasTagCompound() && this.getTagCompound().contains(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND)) {
            DataUtil.readCustomData(this.getTagCompound().getCompound(Constants.Sponge.SPONGE_DATA), ((org.spongepowered.api.item.inventory.ItemStack) this));
        }
    }

}
