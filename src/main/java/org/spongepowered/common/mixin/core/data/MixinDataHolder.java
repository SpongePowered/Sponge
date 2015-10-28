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

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.Optional;

@Mixin(value = {TileEntity.class, Entity.class, ItemStack.class, SpongeUser.class}, priority = 999)
public abstract class MixinDataHolder implements DataHolder {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        try (Timing timing = SpongeTimings.dataGetManipulator.startTiming()) {
            final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(containerClass);
            if (optional.isPresent()) {
                return (Optional<T>) optional.get().from(this);
            }
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        try (Timing timing = SpongeTimings.dataGetOrCreateManipulator.startTiming()) {
            final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(containerClass);
            if (optional.isPresent()) {
                return (Optional<T>) optional.get().createFrom(this);
            } else if (this instanceof IMixinCustomDataHolder) {
                return ((IMixinCustomDataHolder) this).getCustom(containerClass);
            }
            return Optional.empty();
        }
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        try (Timing timing = SpongeTimings.dataSupportsManipulator.startTiming()) {
            final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(holderClass);
            return optional.isPresent() && optional.get().supports(this);
        }
    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        try (Timing timing = SpongeTimings.dataOfferKey.startTiming()) {
            final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = SpongeDataRegistry.getInstance().getBaseValueProcessor(key);
            if (optional.isPresent()) {
                return optional.get().offerToStore(this, value);
            } else if (this instanceof IMixinCustomDataHolder) {
                return ((IMixinCustomDataHolder) this).offerCustom(key, value);
            }
            return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        try (Timing timing = SpongeTimings.dataOfferManipulator.startTiming()) {
            final Optional<DataProcessor> optional = SpongeDataRegistry.getInstance().getWildDataProcessor(valueContainer.getClass());
            if (optional.isPresent()) {
                return optional.get().set(this, valueContainer, checkNotNull(function));
            } else if (this instanceof IMixinCustomDataHolder) {
                return ((IMixinCustomDataHolder) this).offerCustom(valueContainer, function);
            }
            return DataTransactionBuilder.failResult(valueContainer.getValues());
        }
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers) {
        try (Timing timing = SpongeTimings.dataOfferMultiManipulators.startTiming()) {
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
    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        try (Timing timing = SpongeTimings.dataRemoveManipulator.startTiming()) {
            final Optional<DataProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildProcessor(containerClass);
            if (optional.isPresent()) {
                return optional.get().remove(this);
            } else if (this instanceof IMixinCustomDataHolder) {
                return ((IMixinCustomDataHolder) this).removeCustom(containerClass);
            }
            return DataTransactionBuilder.failNoData();
        }
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        try (Timing timing = SpongeTimings.dataRemoveKey.startTiming()) {
            final Optional<ValueProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildValueProcessor(checkNotNull(key));
            if (optional.isPresent()) {
                return optional.get().removeFrom(this);
            } else if (this instanceof IMixinCustomDataHolder) {
                return ((IMixinCustomDataHolder) this).removeCustom(key);
            }
            return DataTransactionBuilder.failNoData();
        }
    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        try (Timing timing = SpongeTimings.dataOfferManipulator.startTiming()) {
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
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return offer(that.getContainers(), function);
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        try (Timing timing = SpongeTimings.dataGetByKey.startTiming()) {
            final Optional<ValueProcessor<E, ? extends BaseValue<E>>>
                optional =
                SpongeDataRegistry.getInstance().getBaseValueProcessor(checkNotNull(key));
            if (optional.isPresent()) {
                return optional.get().getValueFromContainer(this);
            }
            return Optional.empty();
        }
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        try (Timing timing = SpongeTimings.dataGetValue.startTiming()) {
            final Optional<ValueProcessor<E, V>> optional = SpongeDataRegistry.getInstance().getValueProcessor(checkNotNull(key));
            if (optional.isPresent()) {
                return optional.get().getApiValueFromContainer(this);
            }
            return Optional.empty();
        }
    }

    @Override
    public boolean supports(Key<?> key) {
        try (Timing timing = SpongeTimings.dataSupprtsKey.startTiming()) {
            final Optional<ValueProcessor<?, ?>> optional = SpongeDataRegistry.getInstance().getWildValueProcessor(checkNotNull(key));
            return optional.isPresent() && optional.get().supports(this);
        }
    }

}
