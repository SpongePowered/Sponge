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
import static com.google.common.base.Preconditions.checkState;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = {TileEntity.class, Entity.class, ItemStack.class, SpongeUser.class}, priority = 999)
public abstract class MixinDataHolder implements DataHolder {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        try (Timing timing = SpongeTimings.dataGetManipulator.startTiming()) {
            final Optional<DataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildProcessor(containerClass);
            if (optional.isPresent()) {
                return (Optional<T>) optional.get().from(this);
            } else if (this instanceof IMixinCustomDataHolder) {
                return ((IMixinCustomDataHolder) this).getCustom(containerClass);
            }
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataGetOrCreateManipulator.startTiming();
        }

        final Optional<DataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildProcessor(containerClass);
        if (optional.isPresent()) {
            Optional<T> created = (Optional<T>) optional.get().createFrom(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataGetOrCreateManipulator.stopTiming();
            }
            return created;
        } else if (this instanceof IMixinCustomDataHolder) {
            Optional<T> custom = ((IMixinCustomDataHolder) this).getCustom(containerClass);
            if (custom.isPresent()) {
                if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                    SpongeTimings.dataGetOrCreateManipulator.stopTiming();
                }
                return custom;
            } else { // Try to construct it from the DataManipulatorBuilder
                Optional<DataManipulatorBuilder<?, ?>> builder = SpongeDataManager.getInstance().getWildManipulatorBuilder(containerClass);
                checkState(builder.isPresent(), "A DataManipulatorBuilder is not registered for the manipulator class: "
                                                + containerClass.getName());
                T manipulator = (T) builder.get().create();
                // Basically at this point, it's up to plugins to validate whether it's supported
                Optional<T> other = manipulator.fill(this).map(customManipulator -> (T) customManipulator);
                if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                    SpongeTimings.dataGetOrCreateManipulator.stopTiming();
                }
                return other;
            }
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataGetOrCreateManipulator.stopTiming();
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(Class<? extends DataManipulator<?, ?>> holderClass) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataSupportsManipulator.startTiming();
        }

        final Optional<DataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildProcessor(holderClass);
        if (optional.isPresent()) {
            boolean supports = optional.get().supports(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataSupportsManipulator.stopTiming();
            }
            return supports;
        }
        if (this instanceof IMixinCustomDataHolder) {
            Optional<?> custom = ((IMixinCustomDataHolder) this).getCustom(holderClass);
            if (custom.isPresent()) {
                if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                    SpongeTimings.dataSupportsManipulator.stopTiming();
                }
                return true;
            } else { // Try to construct it from the DataManipulatorBuilder
                Optional<DataManipulatorBuilder<?, ?>> builder = SpongeDataManager.getInstance().getWildManipulatorBuilder(holderClass);
                checkState(builder.isPresent(), "A DataManipulatorBuilder is not registered for the manipulator class: "
                                                + holderClass.getName());
                DataManipulator<?, ?> manipulator = builder.get().create();
                // Basically at this point, it's up to plugins to validate whether it's supported
                boolean present = manipulator.fill(this).isPresent();
                if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                    SpongeTimings.dataSupportsManipulator.stopTiming();
                }
                return present;
            }
        }
        return false;

    }

    @Override
    public <E> DataTransactionResult offer(Key<? extends BaseValue<E>> key, E value) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferKey.startTiming();
        }
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = SpongeDataManager.getInstance().getBaseValueProcessor(key);
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().offerToStore(this, value);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataOfferKey.stopTiming();
            }
            return result;
        } else if (this instanceof IMixinCustomDataHolder) {
            final DataTransactionResult result = ((IMixinCustomDataHolder) this).offerCustom(key, value);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataOfferKey.stopTiming();
            }
            return result;
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferKey.stopTiming();
        }
        return DataTransactionResult.failNoData();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public DataTransactionResult offer(DataManipulator<?, ?> valueContainer, MergeFunction function) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferManipulator.startTiming();
        }
        try (Timing timing = SpongeTimings.dataOfferManipulator.startTiming()) {
            final Optional<DataProcessor> optional = SpongeDataManager.getInstance().getWildDataProcessor(valueContainer.getClass());
            if (optional.isPresent()) {
                final DataTransactionResult result = optional.get().set(this, valueContainer, checkNotNull(function));
                if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                    SpongeTimings.dataOfferManipulator.stopTiming();
                }
                return result;
            } else if (this instanceof IMixinCustomDataHolder) {
                final DataTransactionResult result = ((IMixinCustomDataHolder) this).offerCustom(valueContainer, function);
                if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                    SpongeTimings.dataOfferManipulator.stopTiming();
                }
                return result;
            }
            return DataTransactionResult.failResult(valueContainer.getValues());
        }
    }

    @Override
    public DataTransactionResult offer(Iterable<DataManipulator<?, ?>> valueContainers) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferMultiManipulators.startTiming();
        }
        DataTransactionResult.Builder builder = DataTransactionResult.builder();
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
                    if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                        SpongeTimings.dataOfferMultiManipulators.stopTiming();
                    }
                    return builder.build();
                default:
                    break;
            }
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferMultiManipulators.stopTiming();
        }
        return builder.build();

    }

    @Override
    public DataTransactionResult remove(Class<? extends DataManipulator<?, ?>> containerClass) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataRemoveManipulator.startTiming();
        }

        final Optional<DataProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildProcessor(containerClass);
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().remove(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataRemoveManipulator.stopTiming();
            }

            return result;
        } else if (this instanceof IMixinCustomDataHolder) {
            final DataTransactionResult result = ((IMixinCustomDataHolder) this).removeCustom(containerClass);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataRemoveManipulator.stopTiming();
            }
            return result;
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferMultiManipulators.stopTiming();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(Key<?> key) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataRemoveKey.startTiming();
        }
        final Optional<ValueProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final DataTransactionResult result = optional.get().removeFrom(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataRemoveKey.stopTiming();
            }

            return result;
        } else if (this instanceof IMixinCustomDataHolder) {
            final DataTransactionResult result = ((IMixinCustomDataHolder) this).removeCustom(key);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataRemoveKey.stopTiming();
            }
            return result;
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataRemoveKey.stopTiming();
        }
        return DataTransactionResult.failNoData();

    }

    @Override
    public DataTransactionResult undo(DataTransactionResult result) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferManipulator.startTiming();
        }
        if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataOfferManipulator.stopTiming();
            }
            return DataTransactionResult.successNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (ImmutableValue<?> replaced : result.getReplacedData()) {
            builder.absorbResult(offer(replaced));
        }
        for (ImmutableValue<?> successful : result.getSuccessfulData()) {
            builder.absorbResult(remove(successful));
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataOfferManipulator.stopTiming();
        }

        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(DataHolder that, MergeFunction function) {
        return offer(that.getContainers(), function);
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataGetByKey.startTiming();
        }
        final Optional<ValueProcessor<E, ? extends BaseValue<E>>> optional = SpongeDataManager.getInstance().getBaseValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final Optional<E> value = optional.get().getValueFromContainer(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataGetByKey.stopTiming();
            }
            return value;
        } else if (this instanceof IMixinCustomDataHolder) {
            final Optional<E> custom = ((IMixinCustomDataHolder) this).getCustom(key);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataGetByKey.stopTiming();
            }
            return custom;
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataGetByKey.stopTiming();
        }
        return Optional.empty();

    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataGetValue.startTiming();
        }

        final Optional<ValueProcessor<E, V>> optional = SpongeDataManager.getInstance().getValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final Optional<V> value = optional.get().getApiValueFromContainer(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataGetValue.stopTiming();
            }
            return value;
        } else if (this instanceof IMixinCustomDataHolder) {
            final Optional<V> customValue = ((IMixinCustomDataHolder) this).getCustomValue(key);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataGetValue.stopTiming();
            }
            return customValue;
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataGetValue.stopTiming();
        }

        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataSupportsKey.startTiming();
        }

        final Optional<ValueProcessor<?, ?>> optional = SpongeDataManager.getInstance().getWildValueProcessor(checkNotNull(key));
        if (optional.isPresent()) {
            final boolean supports = optional.get().supports(this);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataSupportsKey.stopTiming();
            }
            return supports;
        }
        if (this instanceof IMixinCustomDataHolder) {
            final boolean customSupport = ((IMixinCustomDataHolder) this).supportsCustom(key);
            if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
                SpongeTimings.dataSupportsKey.stopTiming();
            }
            return customSupport;
        }
        if (MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            SpongeTimings.dataSupportsKey.stopTiming();
        }
        return false;
    }

}
