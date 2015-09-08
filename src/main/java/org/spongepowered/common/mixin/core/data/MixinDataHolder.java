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
package org.spongepowered.common.mixin.core.data;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.entity.player.SpongeUser;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(value = {TileEntity.class, Entity.class, ItemStack.class, SpongeUser.class}, priority = 999)
public abstract class MixinDataHolder implements DataHolder {

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = SpongePropertyRegistry.getInstance().getStore(propertyClass);
        if (optional.isPresent()) {
            return optional.get().getFor(this);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(containerClass);
        if (optional.isPresent()) {
            return (Optional<T>) optional.get().from(this);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(containerClass);
        if (optional.isPresent()) {
            return (Optional<T>) optional.get().createFrom(this);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(holderClass);
        return optional.isPresent() && optional.get().supports(this);
    }

    @Override
    public <E> DataTransactionResult transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        if (supports(key)) {
            return offer(key, checkNotNull(function.apply(get(key).orElse(null))));
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = SpongeDataRegistry.getInstance().getBaseValueProcessor(key);
        if (optional.isPresent()) {
            return optional.get().offerToStore(this, value);
        }
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public <E> DataTransactionResult offer(BaseValue<E> value) {
        return offer(value.getKey(), value.get());
    }

    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer) {
        return offer(valueContainer, MergeFunction.IGNORE_ALL);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        final Optional<DataProcessor> optional = SpongeDataRegistry.getInstance().getWildDataProcessor(valueContainer.getClass());
        if (optional.isPresent()) {
            return optional.get().set(this, valueContainer, function);
        }
        return DataTransactionBuilder.failResult(valueContainer.getValues());
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers) {
        DataTransactionBuilder builder = DataTransactionBuilder.builder();
        for (DataManipulator<?, ?> manipulator : valueContainers) {
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
                    return builder.build();
                default:
                    break;
            }
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> values, MergeFunction function) {
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
        for (DataManipulator<?, ?> manipulator : values) {
            builder.absorbResult(offer(manipulator));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(containerClass);
        if (optional.isPresent()) {
            return optional.get().remove(this);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult remove(BaseValue<?> value) {
        final Optional<ValueProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildValueProcessor(checkNotNull(value).getKey());
        if (optional.isPresent()) {
            return optional.get().removeFrom(this);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        final Optional<ValueProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            return optional.get().removeFrom(this);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
            return DataTransactionBuilder.successNoData();
        }
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
        for (ImmutableValue<?> replaced : result.getReplacedData()) {
            builder.absorbResult(offer(replaced));
        }
        for (ImmutableValue<?> successful : result.getSuccessfulData()) {
            builder.absorbResult(remove(successful));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that) {
        return offer(that.getContainers());
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return offer(that.getContainers(), function);
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>>
            optional =
            SpongeDataRegistry.getInstance().getBaseValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            return optional.get().getValueFromContainer(this);
        }
        return Optional.empty();
    }

    @Nullable
    @Override
    public <E> E getOrNull(Key<? extends BaseValue<E>> key) {
        return get(key).orElse(null);
    }

    @Override
    public <E> E getOrElse(Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(key).orElse(checkNotNull(defaultValue));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        final Optional<ValueProcessor<E, V>> optional = SpongeDataRegistry.getInstance().getValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            return optional.get().getApiValueFromContainer(this);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        final Optional<ValueProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildValueProcessor(checkNotNull(key));
        return optional.isPresent() && optional.get().supports(this);
    }

    @Override
    public boolean supports(BaseValue<?> baseValue) {
        final Optional<ValueProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildValueProcessor(checkNotNull(baseValue).getKey());
        return optional.isPresent() && optional.get().supports(this);
    }

}
