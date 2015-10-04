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
package org.spongepowered.common.data.processor.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractItemDataProcessor<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> extends AbstractSpongeDataProcessor<M, I> {

    private final Predicate<ItemStack> predicate;

    protected AbstractItemDataProcessor(Predicate<ItemStack> predicate) {
        this.predicate = checkNotNull(predicate);
    }

    protected abstract M createManipulator();

    protected abstract boolean doesDataExist(ItemStack itemStack);

    protected abstract boolean set(ItemStack itemStack, Map<Key<?>, Object> keyValues);

    protected abstract Map<Key<?>, ?> getValues(ItemStack itemStack);

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof ItemStack && this.predicate.apply((ItemStack) dataHolder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<M> from(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return Optional.empty();
        } else {
            if (doesDataExist((ItemStack) dataHolder)) {
                final M manipulator = createManipulator();
                final Map<Key<?>, ?> keyValues = getValues((ItemStack) dataHolder);
                for (Map.Entry<Key<?>, ?> entry :keyValues.entrySet()) {
                    manipulator.set((Key) entry.getKey(), entry.getValue());
                }
                return Optional.of(manipulator);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<M> createFrom(DataHolder dataHolder) {
        if (!supports(dataHolder)) {
            return Optional.empty();
        } else {
            Optional<M> optional = from(dataHolder);
            if (!optional.isPresent()) {
                return Optional.of(createManipulator());
            } else {
                return optional;
            }
        }
    }

    @Override
    public Optional<M> fill(DataHolder dataHolder, M manipulator, MergeFunction overlap) {
        if (!supports(dataHolder)) {
            return Optional.empty();
        } else {
            final M merged = checkNotNull(overlap).merge(manipulator.copy(), from(dataHolder).orElse(null));
            for (ImmutableValue<?> value : merged.getValues()) {
                manipulator.set(value);
            }
            return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, M manipulator, MergeFunction function) {
        if (supports(dataHolder)) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<M> old = from(dataHolder);
            final M merged = checkNotNull(function).merge(old.orElse(null), manipulator);
            final Map<Key<?>, Object> map = Maps.newHashMap();
            final Set<ImmutableValue<?>> newVals = merged.getValues();
            for (ImmutableValue<?> value : newVals) {
                map.put(value.getKey(), value.get());
            }
            try {
                if (set((ItemStack) dataHolder, map)) {
                    if (old.isPresent()) {
                        builder.replace(old.get().getValues());
                    }

                    return builder.result(DataTransactionResult.Type.SUCCESS).success(newVals).build();
                } else {
                    return builder.result(DataTransactionResult.Type.FAILURE).reject(newVals).build();
                }
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).reject(newVals).build();
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<I> with(Key<? extends BaseValue<?>> key, Object value, I immutable) {
        if (immutable.supports(key)) {
            return Optional.of((I) immutable.asMutable().set((Key) key, value).asImmutable());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return false;
    }
}
