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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.item.IMixinItem;
import org.spongepowered.common.interfaces.item.IMixinItemStack;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(net.minecraft.item.ItemStack.class)
@Implements(@Interface(iface = ItemStack.class, prefix = "itemstack$"))
public abstract class MixinItemStack implements DataHolder, IMixinItemStack, IMixinCustomDataHolder {

    private List<DataView> failedData = new ArrayList<>();

    @Shadow public abstract int getCount();
    @Shadow public abstract void setCount(int size); // Do not use field directly as Minecraft tracks the empty state
    @Shadow public abstract void setItemDamage(int meta);
    @Shadow public abstract void setTagCompound(@Nullable NBTTagCompound compound);
    @Shadow public abstract void setTagInfo(String key, NBTBase nbtBase);
    @Shadow public abstract int getItemDamage();
    @Shadow public abstract int getMaxStackSize();
    @Shadow public abstract boolean hasTagCompound();
    @Shadow public abstract boolean shadow$isEmpty();
    @Shadow public abstract NBTTagCompound getTagCompound();
    @Shadow public abstract NBTTagCompound getOrCreateSubCompound(String key);
    @Shadow public abstract net.minecraft.item.ItemStack shadow$copy();
    @Shadow public abstract Item shadow$getItem();


    @Inject(method = "writeToNBT", at = @At(value = "HEAD"))
    private void onWrite(NBTTagCompound incoming, CallbackInfoReturnable<NBTTagCompound> info) {
        if (this.hasManipulators()) {
            writeToNbt(incoming);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
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

    @Inject(method = "setTagCompound", at = @At("RETURN"))
    private void onSet(NBTTagCompound compound, CallbackInfo callbackInfo) {
        if (hasTagCompound() && getTagCompound().hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            readFromNbt(getTagCompound().getCompoundTag(NbtDataUtil.SPONGE_DATA));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "onBlockDestroyed", at = @At("HEAD"))
    private void capturePlayerOnBlockDestroyed(World worldIn, IBlockState blockIn, BlockPos pos, EntityPlayer playerIn, CallbackInfo ci) {
        if (!((IMixinWorld) worldIn).isFake()) {
            final PhaseTracker phaseTracker = PhaseTracker.getInstance();
            final PhaseData peek = phaseTracker.getCurrentPhaseData();
            final IPhaseState state = peek.state;
            state.capturePlayerUsingStackToBreakBlock((ItemStack) this, (EntityPlayerMP) playerIn, peek.context);
        }
    }

    public int itemstack$getQuantity() {
        return this.getCount();
    }

    public ItemType itemstack$getType() {
        return (ItemType) shadow$getItem();
    }

    public void itemstack$setQuantity(int quantity) throws IllegalArgumentException {
        this.setCount(quantity);
    }

    public int itemstack$getMaxStackQuantity() {
        return getMaxStackSize();
    }

    @Override
    public boolean validateRawData(DataView container) {
        return false;
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {

    }
    
    @Override
    public DataHolder copy() {
        return this.itemstack$copy();
    }

    public ItemStack itemstack$copy() {
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
                .set(DataQueries.ITEM_TYPE, this.itemstack$getType().getId())
                .set(DataQueries.ITEM_COUNT, this.itemstack$getQuantity())
                .set(DataQueries.ITEM_DAMAGE_VALUE, this.getItemDamage());
        if (hasTagCompound()) { // no tag? no data, simple as that.
            final NBTTagCompound compound = getTagCompound().copy();
            if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
                final NBTTagCompound spongeCompound = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA);
                if (spongeCompound.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST)) {
                    spongeCompound.removeTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST);
                }
            }
            NbtDataUtil.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
            if (!compound.isEmpty()) {
                final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
                container.set(DataQueries.UNSAFE_NBT, unsafeNbt);
            }
        }
        // We only need to include the custom data, not vanilla manipulators supported by sponge implementation
        final Collection<DataManipulator<?, ?>> manipulators = getCustomManipulators();
        if (!manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        return container;
    }

    public Translation itemstack$getTranslation() {
        return new SpongeTranslation(shadow$getItem().getTranslationKey((net.minecraft.item.ItemStack) (Object) this) + ".name");
    }

    public ItemStackSnapshot itemstack$createSnapshot() {
        return new SpongeItemStackSnapshot((ItemStack)this);
    }

    public boolean itemstack$equalTo(ItemStack that) {
        return net.minecraft.item.ItemStack.areItemStacksEqual(
                (net.minecraft.item.ItemStack) (Object) this,
                (net.minecraft.item.ItemStack) that
        );
    }

    @Intrinsic
    public boolean itemstack$isEmpty() {
        return this.shadow$isEmpty();
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
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
        ((IMixinItem) item).getManipulatorsFor((net.minecraft.item.ItemStack) (Object) this, manipulators);
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
            if (!list.isEmpty()) {
                compound.removeTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST);
                final List<DataView> views = Lists.newArrayList();
                for (int i = 0; i < list.tagCount(); i++) {
                    final NBTTagCompound dataCompound = list.getCompoundTagAt(i);
                    views.add(NbtTranslator.getInstance().translateFrom(dataCompound));
                }
                final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
                final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
                for (DataManipulator<?, ?> manipulator : manipulators) {
                    offerCustom(manipulator, MergeFunction.IGNORE_ALL);
                }
                if (!transaction.failedData.isEmpty()) {
                    addFailedData(transaction.failedData);
                }
            } else {
                compound.removeTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST);
                if (compound.isEmpty()) {
                    getTagCompound().removeTag(NbtDataUtil.SPONGE_DATA);
                    return;
                }
            }
        }
        if (compound.hasKey(NbtDataUtil.FAILED_CUSTOM_DATA, NbtDataUtil.TAG_LIST)) {
            final NBTTagList list = compound.getTagList(NbtDataUtil.FAILED_CUSTOM_DATA, NbtDataUtil.TAG_COMPOUND);
            final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
            if (list.tagCount() != 0) {
                for (int i = 0; i < list.tagCount(); i++) {
                    final NBTTagCompound internal = list.getCompoundTagAt(i);
                    builder.add(NbtTranslator.getInstance().translateFrom(internal));
                }
            }
            // Re-attempt to deserialize custom data
            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(builder.build());
            final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
            for (DataManipulator<?, ?> manipulator : manipulators) {
                offer(manipulator);
            }
            if (!transaction.failedData.isEmpty()) {
                this.addFailedData(transaction.failedData);
            }
        }
        if (compound.isEmpty()) {
            getTagCompound().removeTag(NbtDataUtil.SPONGE_DATA);
            if (getTagCompound().isEmpty()) {
                setTagCompound(null);
            }
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        resyncCustomToTag();
    }

    private List<DataManipulator<?, ?>> manipulators = Lists.newArrayList();

    @SuppressWarnings("rawtypes")
    @Override
    public DataTransactionResult offerCustom(DataManipulator<?, ?> manipulator, MergeFunction function) {
        @Nullable DataManipulator<?, ?> existingManipulator = null;
        for (DataManipulator<?, ?> existing : this.manipulators) {
            if (manipulator.getClass().isInstance(existing)) {
                existingManipulator = existing;
                break;
            }
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
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

    @Override
    public void addFailedData(ImmutableList<DataView> failedData) {
        this.failedData.addAll(failedData);
        resyncCustomToTag();
    }

    @Override
    public List<DataView> getFailedData() {
        return this.failedData;
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
            final NBTTagCompound spongeCompound = getOrCreateSubCompound(NbtDataUtil.SPONGE_DATA);
            spongeCompound.setTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, newList);
        } else if (!this.failedData.isEmpty()) {
            final NBTTagList newList = new NBTTagList();
            for (DataView failedDatum : this.failedData) {
                newList.appendTag(NbtTranslator.getInstance().translateData(failedDatum));
            }
            final NBTTagCompound spongeCompound = getOrCreateSubCompound(NbtDataUtil.SPONGE_DATA);
            spongeCompound.setTag(NbtDataUtil.FAILED_CUSTOM_DATA, newList);
        } else {
            if (hasTagCompound()) {
                this.getTagCompound().removeTag(NbtDataUtil.SPONGE_DATA);
            }
            if (this.getTagCompound().isEmpty()) {
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
            return DataTransactionResult.builder().replace(manipulator.getValues()).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean hasManipulators() {
        return !this.manipulators.isEmpty();
    }

    @Override
    public List<DataManipulator<?, ?>> getCustomManipulators() {
        return this.manipulators.stream()
            .map(DataManipulator::copy)
            .collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult offerCustom(Key<? extends BaseValue<E>> key, E value) {
        for (DataManipulator<?, ?> manipulator : this.manipulators) {
            if (manipulator.supports(key)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(((Value) manipulator.getValue((Key) key).get()).asImmutable());
                manipulator.set(key, value);
                builder.success(((Value) manipulator.getValue((Key) key).get()).asImmutable());
                resyncCustomToTag();
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult removeCustom(Key<?> key) {
        final Iterator<DataManipulator<?, ?>> iterator = this.manipulators.iterator();
        while (iterator.hasNext()) {
            final DataManipulator<?, ?> manipulator = iterator.next();
            if (manipulator.getKeys().size() == 1 && manipulator.supports(key)) {
                iterator.remove();
                resyncCustomToTag();
                return DataTransactionResult.builder()
                    .replace(manipulator.getValues())
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean supportsCustom(Key<?> key) {
        return this.manipulators.stream()
                .anyMatch(manipulator -> manipulator.supports(key));
    }

    @Override
    public <E> Optional<E> getCustom(Key<? extends BaseValue<E>> key) {
        return this.manipulators.stream()
                .filter(manipulator -> manipulator.supports(key))
                .findFirst()
                .flatMap(supported -> supported.get(key));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getCustomValue(Key<V> key) {
        return this.manipulators.stream()
                .filter(manipulator -> manipulator.supports(key))
                .findFirst()
                .flatMap(supported -> supported.getValue(key));
    }
}
