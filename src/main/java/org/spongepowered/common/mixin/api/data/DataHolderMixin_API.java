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
package org.spongepowered.common.mixin.api.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.living.player.User;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = {TileEntity.class, Entity.class, ItemStack.class, SpongeUser.class}, priority = 899)
public abstract class DataHolderMixin_API implements DataHolder {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(final Class<T> containerClass) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataGetManipulator.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(false);
        }
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(containerClass);
        if (optional.isPresent()) {
            final Optional<?> from = optional.get().from(holder);
            SpongeTimings.dataGetManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return (Optional<T>) from;
        } else if (this instanceof CustomDataHolderBridge) {
            final Optional<T> custom = ((CustomDataHolderBridge) holder).bridge$getCustom(containerClass);
            SpongeTimings.dataGetManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return custom;
        }
        SpongeTimings.dataGetManipulator.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(final Class<T> containerClass) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataGetOrCreateManipulator.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(false);
        }
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(containerClass);
        if (optional.isPresent()) {
            final Optional<T> created = (Optional<T>) optional.get().createFrom(holder);
            SpongeTimings.dataGetOrCreateManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return created;
        } else if (this instanceof CustomDataHolderBridge) {
            final Optional<T> custom = ((CustomDataHolderBridge) holder).bridge$getCustom(containerClass);
            if (custom.isPresent()) {
                SpongeTimings.dataGetOrCreateManipulator.stopTimingIfSync();
                TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
                return custom;
            }
            // Try to construct it from the DataManipulatorBuilder
            final Optional<DataManipulatorBuilder<?, ?>> builder = SpongeDataManager.getInstance().getWildManipulatorBuilder(containerClass);
            checkState(builder.isPresent(), "A DataManipulatorBuilder is not registered for the manipulator class: "
                    + containerClass.getName());
            final T manipulator = (T) builder.get().create();
            // Basically at this point, it's up to plugins to validate whether it's supported
            final Optional<T> other = manipulator.fill(holder).map(customManipulator -> (T) customManipulator);
            SpongeTimings.dataGetOrCreateManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return other;
        }
        SpongeTimings.dataGetOrCreateManipulator.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return Optional.empty();
    }

    @Override
    public boolean supports(final Class<? extends DataManipulator<?, ?>> holderClass) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataSupportsManipulator.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(false);
        }

        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(holderClass);
        if (optional.isPresent()) {
            final boolean supports = optional.get().supports(holder);
            SpongeTimings.dataSupportsManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return supports;
        }
        if (this instanceof CustomDataHolderBridge) {
            final Optional<?> custom = ((CustomDataHolderBridge) holder).bridge$getCustom(holderClass);
            if (custom.isPresent()) {
                SpongeTimings.dataSupportsManipulator.stopTimingIfSync();
                TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
                return true;
            }
            // Try to construct it from the DataManipulatorBuilder
            final Optional<DataManipulatorBuilder<?, ?>> builder = SpongeDataManager.getInstance().getWildManipulatorBuilder(holderClass);
            checkState(builder.isPresent(), "A DataManipulatorBuilder is not registered for the manipulator class: "
                    + holderClass.getName());
            final DataManipulator<?, ?> manipulator = builder.get().create();
            // Basically at this point, it's up to plugins to validate whether it's supported
            final boolean present = manipulator.fill(holder).isPresent();
            SpongeTimings.dataSupportsManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return present;
        }
        SpongeTimings.dataSupportsManipulator.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return false;

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public <E> DataTransactionResult offer(final Key<? extends BaseValue<E>> key, final E value) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataOfferKey.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(true);
        }
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = DataUtil.getBaseValueProcessor(key);
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().offerToStore(holder, value);
            SpongeTimings.dataOfferKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        } else if (this instanceof CustomDataHolderBridge) {
            final DataTransactionResult result = ((CustomDataHolderBridge) holder).bridge$offerCustom(key, value);
            SpongeTimings.dataOfferKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        }
        SpongeTimings.dataOfferKey.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return DataTransactionResult.failNoData();
    }

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
    @Override
    public DataTransactionResult offer(final DataManipulator<?, ?> valueContainer, final MergeFunction function) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataOfferManipulator.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(true);
        }
        final Optional<DataProcessor> optional = DataUtil.getWildDataProcessor(valueContainer.getClass());
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().set(holder, valueContainer, checkNotNull(function));
            SpongeTimings.dataOfferManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        } else if (this instanceof CustomDataHolderBridge) {
            final DataTransactionResult result = ((CustomDataHolderBridge) holder).bridge$offerCustom(valueContainer, function);
            SpongeTimings.dataOfferManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        }
        SpongeTimings.dataOfferManipulator.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return DataTransactionResult.failResult(valueContainer.getValues());
    }

    @Override
    public DataTransactionResult offer(final Iterable<DataManipulator<?, ?>> valueContainers) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataOfferMultiManipulators.startTimingIfSync();
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (final DataManipulator<?, ?> manipulator : valueContainers) {
            final DataTransactionResult result = offer(manipulator);
            if (!result.getRejectedData().isEmpty()) {
                builder.reject(result.getRejectedData());
            }
            if (!result.getReplacedData().isEmpty()) {
                builder.replace(result.getReplacedData());
            }
            if (!result.getSuccessfulData().isEmpty()) {
                builder.success(result.getSuccessfulData());
            }
            final DataTransactionResult.Type type = result.getType();
            builder.result(type);
            switch (type) {
                case UNDEFINED:
                case ERROR:
                case CANCELLED:
                    SpongeTimings.dataOfferMultiManipulators.stopTimingIfSync();
                    TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
                    return builder.build();
                default:
                    break;
            }
        }
        SpongeTimings.dataOfferMultiManipulators.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return builder.build();

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public DataTransactionResult remove(final Class<? extends DataManipulator<?, ?>> containerClass) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataRemoveManipulator.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(true);
        }
        final Optional<DataProcessor<?, ?>> optional = DataUtil.getWildProcessor(containerClass);
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().remove(holder);
            SpongeTimings.dataRemoveManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();

            return result;
        } else if (this instanceof CustomDataHolderBridge) {
            final DataTransactionResult result = ((CustomDataHolderBridge) holder).bridge$removeCustom(containerClass);
            SpongeTimings.dataRemoveManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        }
        SpongeTimings.dataOfferMultiManipulators.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return DataTransactionResult.failNoData();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public DataTransactionResult remove(final Key<?> key) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataRemoveKey.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(true);
        }
        final Optional<ValueProcessor<?, ?>> optional = DataUtil.getWildValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().removeFrom(holder);
            SpongeTimings.dataRemoveKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        } else if (this instanceof CustomDataHolderBridge) {
            final DataTransactionResult result = ((CustomDataHolderBridge) holder).bridge$removeCustom(key);
            SpongeTimings.dataRemoveKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return result;
        }
        SpongeTimings.dataRemoveKey.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return DataTransactionResult.failNoData();

    }

    @Override
    public DataTransactionResult undo(final DataTransactionResult result) {
        SpongeTimings.dataOfferManipulator.startTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
            SpongeTimings.dataOfferManipulator.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return DataTransactionResult.successNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (final ImmutableValue<?> replaced : result.getReplacedData()) {
            builder.absorbResult(offer(replaced));
        }
        for (final ImmutableValue<?> successful : result.getSuccessfulData()) {
            builder.absorbResult(remove(successful));
        }
        SpongeTimings.dataOfferManipulator.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(final DataHolder that, final MergeFunction function) {
        return offer(that.getContainers(), function);
    }

    @Override
    public <E> Optional<E> get(final Key<? extends BaseValue<E>> key) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataGetByKey.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(false);
        }
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = DataUtil.getBaseValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final Optional<E> value = optional.get().getValueFromContainer(holder);
            SpongeTimings.dataGetByKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return value;
        } else if (this instanceof CustomDataHolderBridge) {
            final Optional<E> custom = ((CustomDataHolderBridge) holder).bridge$getCustom(key);
            SpongeTimings.dataGetByKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return custom;
        }
        SpongeTimings.dataGetByKey.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(final Key<V> key) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataGetValue.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(false);
        }
        final Optional<ValueProcessor<E, V>> optional = DataUtil.getValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final Optional<V> value = optional.get().getApiValueFromContainer(holder);
            SpongeTimings.dataGetValue.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return value;
        } else if (this instanceof CustomDataHolderBridge) {
            final Optional<V> customValue = ((CustomDataHolderBridge) holder).bridge$getCustomValue(key);
            SpongeTimings.dataGetValue.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return customValue;
        }
        SpongeTimings.dataGetValue.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return Optional.empty();
    }

    @Override
    public boolean supports(final Key<?> key) {
        TimingsManager.DATA_GROUP_HANDLER.startTimingIfSync();
        SpongeTimings.dataSupportsKey.startTimingIfSync();
        boolean isUser = ((Object) this) instanceof SpongeUser;
        DataHolder holder = this;
        if (isUser) {
            holder = ((SpongeUser) holder).getDataHolder(false);
        }
        final Optional<ValueProcessor<?, ?>> optional = DataUtil.getWildValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final boolean supports = optional.get().supports(holder);
            SpongeTimings.dataSupportsKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return supports;
        }
        if (this instanceof CustomDataHolderBridge) {
            final boolean customSupport = ((CustomDataHolderBridge) holder).bridge$supportsCustom(key);
            SpongeTimings.dataSupportsKey.stopTimingIfSync();
            TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
            return customSupport;
        }
        SpongeTimings.dataSupportsKey.stopTimingIfSync();
        TimingsManager.DATA_GROUP_HANDLER.stopTimingIfSync();
        return false;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return getContainers().stream().flatMap(container -> container.getKeys().stream()).collect(Collectors.toSet());
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return getContainers().stream().flatMap(container -> container.getValues().stream()).collect(Collectors.toSet());
    }

    // The rest of these are default implemented in the event some implementation fails.

    @Override
    public boolean validateRawData(final DataView container) {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return Collections.emptyList();
    }

    @Override
    public DataHolder copy() {
        return this;
    }

}
