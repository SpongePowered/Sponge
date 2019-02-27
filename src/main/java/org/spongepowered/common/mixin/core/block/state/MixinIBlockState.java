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
package org.spongepowered.common.mixin.core.block.state;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.IProperty;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataVersions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Mixin(IBlockState.class)
public interface MixinIBlockState extends BlockState {

    @Shadow Block shadow$getBlock();

    @Override
    default BlockType getType() {
        return (BlockType) shadow$getBlock();
    }


    @Override
    default <T extends Cycleable<T>> Optional<BlockState> cycleValue(Key<? extends Value<T>> key) {
        return Optional.of(this);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    default <T extends Comparable<T>> Optional<T> getStateProperty(StateProperty<T> stateProperty) {
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : ((IBlockState) this).getValues().entrySet()) {
            if (entry.getKey() == stateProperty) {
                return Optional.of((T) entry.getValue());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    default Optional<StateProperty<?>> getStatePropertyByName(String blockTrait) {
        for (IProperty property : ((IBlockState) this).getValues().keySet()) {
            if (property.getName().equalsIgnoreCase(blockTrait)) {
                return Optional.of((StateProperty<?>) property);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default <T extends Comparable<T>, V extends T> Optional<BlockState> withStateProperty(StateProperty<T> trait, V value) {
        if (value instanceof String) {
            Comparable foundValue = null;
            for (Comparable comparable : trait.getPossibleValues()) {
                if (comparable.toString().equals(value)) {
                    foundValue = comparable;
                    break;
                }
            }
            if (foundValue != null) {
                return Optional.of((BlockState) ((IBlockState) this).with((IProperty) trait, foundValue));
            }
        }
        if (value instanceof Comparable) {
            if (getProperties().containsKey(trait) && ((IProperty) trait).getAllowedValues().contains(value)) {
                return Optional.of((BlockState) ((IBlockState) this).with((IProperty) trait, (Comparable) value));
            }
        }
        return Optional.empty();
    }

    @Override
    default Collection<StateProperty<?>> getStateProperties() {
        return getStatePropertyMap().keySet();
    }

    @Override
    default Collection<?> getStatePropertyValues() {
        return getStatePropertyMap().values();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Map<StateProperty<?>, ?> getStatePropertyMap() {
        return (ImmutableMap) ((IBlockState) this).getProperties();
    }

    @Override
    default CatalogKey getKey() {
        final String nameSpace = ((BlockType) shadow$getBlock()).getKey().getNamespace();
        StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) shadow$getBlock()).getKey().getValue());

        final ImmutableMap<IProperty<?>, Comparable<?>> properties = ((IBlockState) this).getValues();
        if (!properties.isEmpty()) {
            builder.append('[');
            Joiner joiner = Joiner.on(',');
            List<String> propertyValues = new ArrayList<>();
            for (Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
                propertyValues.add(entry.getKey().getName() + "=" + entry.getValue());
            }
            builder.append(joiner.join(propertyValues));
            builder.append(']');
        }
        return CatalogKey.of(nameSpace, builder.toString());
    }

    @Override
    default String getName() {
        return getKey().getValue();
    }

    @Override
    default List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return Collections.emptyList();
    }

    @Override
    default int getContentVersion() {
        return DataVersions.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    default DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.BLOCK_STATE, getKey());
    }

    @Override
    default <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    default <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    default boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return false;
    }

    @Override
    default <E> Optional<BlockState> transform(Key<? extends Value<E>> key, Function<E, E> function) {
        return Optional.empty();
    }

    @Override
    default <E> Optional<BlockState> with(Key<? extends Value<E>> key, E value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(Value<?> value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(ImmutableDataManipulator<?, ?> valueContainer) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> without(Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty();
    }

    @Override
    default BlockState merge(BlockState that) {
        return this;
    }

    @Override
    default BlockState merge(BlockState that, MergeFunction function) {
        return this;
    }

    @Override
    default List<ImmutableDataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    default <E> Optional<E> get(Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    default boolean supports(Key<?> key) {
        return false;
    }

    @Override
    default BlockState copy() {
        return this;
    }

    @Override
    default Set<Key<?>> getKeys() {
        return Collections.emptySet();
    }

    @Override
    default Set<Value.Immutable<?>> getValues() {
        return Collections.emptySet();
    }
}
