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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin({TileEntity.class, Entity.class, SpongeUser.class})
public abstract class CustomDataHolderMixin implements CustomDataHolderBridge {

    private List<DataManipulator<?, ?>> impl$manipulators = Lists.newArrayList();
    private List<DataView> impl$failedData = Lists.newArrayList();

    @SuppressWarnings({"rawtypes", "Duplicates"})
    @Override
    public DataTransactionResult bridge$offerCustom(DataManipulator<?, ?> manipulator, MergeFunction function) {
        @Nullable DataManipulator<?, ?> existingManipulator = null;
        for (DataManipulator<?, ?> existing : this.impl$manipulators) {
            if (manipulator.getClass().isInstance(existing)) {
                existingManipulator = existing;
                break;
            }
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final DataManipulator<?, ?> newManipulator = checkNotNull(function.merge(existingManipulator, (DataManipulator) manipulator.copy()));
        if (existingManipulator != null) {
            builder.replace(existingManipulator.getValues());
            this.impl$manipulators.remove(existingManipulator);
        }
        this.impl$manipulators.add(newManipulator);
        return builder.success(newManipulator.getValues())
            .result(DataTransactionResult.Type.SUCCESS)
            .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> bridge$getCustom(Class<T> customClass) {
        for (DataManipulator<?, ?> existing : this.impl$manipulators) {
            if (customClass.isInstance(existing)) {
                return Optional.of((T) existing.copy());
            }
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult bridge$removeCustom(Class<? extends DataManipulator<?, ?>> customClass) {
        @Nullable DataManipulator<?, ?> manipulator = null;
        for (DataManipulator<?, ?> existing : this.impl$manipulators) {
            if (customClass.isInstance(existing)) {
                manipulator = existing;
            }
        }
        if (manipulator != null) {
            this.impl$manipulators.remove(manipulator);
            this.bridge$removeCustomFromNbt(manipulator);
            return DataTransactionResult.builder().replace(manipulator.getValues()).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean bridge$hasManipulators() {
        return !this.impl$manipulators.isEmpty();
    }

    @Override
    public boolean bridge$supportsCustom(Key<?> key) {
        return this.impl$manipulators.stream()
                .anyMatch(manipulator -> manipulator.supports(key));
    }

    @Override
    public <E> Optional<E> bridge$getCustom(Key<? extends BaseValue<E>> key) {
        return this.impl$manipulators.stream()
                .filter(manipulator -> manipulator.supports(key))
                .findFirst()
                .flatMap(supported -> supported.get(key));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> bridge$getCustomValue(Key<V> key) {
        return this.impl$manipulators.stream()
                .filter(manipulator -> manipulator.supports(key))
                .findFirst()
                .flatMap(supported -> supported.getValue(key));
    }

    @Override
    public Collection<DataManipulator<?, ?>> bridge$getCustomManipulators() {
        return this.impl$manipulators.stream().map(DataManipulator::copy).collect(Collectors.toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <E> DataTransactionResult bridge$offerCustom(Key<? extends BaseValue<E>> key, E value) {
        for (DataManipulator<?, ?> manipulator : this.impl$manipulators) {
            if (manipulator.supports(key)) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(((Value) manipulator.getValue((Key) key).get()).asImmutable());
                manipulator.set(key, value);
                builder.success(((Value) manipulator.getValue((Key) key).get()).asImmutable());
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult bridge$removeCustom(Key<?> key) {
        final Iterator<DataManipulator<?, ?>> iterator = this.impl$manipulators.iterator();
        while (iterator.hasNext()) {
            final DataManipulator<?, ?> manipulator = iterator.next();
            if (manipulator.getKeys().size() == 1 && manipulator.supports(key)) {
                iterator.remove();
                bridge$removeCustomFromNbt(manipulator);
                return DataTransactionResult.builder()
                    .replace(manipulator.getValues())
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public void bridge$addFailedData(ImmutableList<DataView> failedData) {
        this.impl$failedData.addAll(failedData);
    }

    @Override
    public List<DataView> bridge$getFailedData() {
        return this.impl$failedData;
    }
}
