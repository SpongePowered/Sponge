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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.interfaces.world.IMixinLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Mixin(IBlockState.class)
public interface MixinIBlockState extends IBlockState, BlockState {

    @Override
    default BlockType getType() {
        return (BlockType) getBlock();
    }

    @Override
    default BlockState withExtendedProperties(Location<World> location) {
        return (BlockState) this.getActualState((net.minecraft.world.World) location.getExtent(), ((IMixinLocation) (Object) location).getBlockPos());

    }

    @Override
    default BlockState cycleValue(Key<? extends BaseValue<? extends Cycleable<?>>> key) {
        return this;
    }


    @SuppressWarnings({"unchecked"})
    @Override
    default <T extends Comparable<T>> Optional<T> getTraitValue(BlockTrait<T> blockTrait) {
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : getProperties().entrySet()) {
            if (entry.getKey() == blockTrait) {
                return Optional.of((T) entry.getValue());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    default Optional<BlockTrait<?>> getTrait(String blockTrait) {
        for (IProperty property : getProperties().keySet()) {
            if (property.getName().equalsIgnoreCase(blockTrait)) {
                return Optional.of((BlockTrait<?>) property);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Optional<BlockState> withTrait(BlockTrait<?> trait, Object value) {
        if (value instanceof String) {
            Comparable foundValue = null;
            for (Comparable comparable : trait.getPossibleValues()) {
                if (comparable.toString().equals(value)) {
                    foundValue = comparable;
                    break;
                }
            }
            if (foundValue != null) {
                return Optional.of((BlockState) this.withProperty((IProperty) trait, foundValue));
            }
        }
        if (value instanceof Comparable) {
            if (getProperties().containsKey(trait) && ((IProperty) trait).getAllowedValues().contains(value)) {
                return Optional.of((BlockState) this.withProperty((IProperty) trait, (Comparable) value));
            }
        }
        return Optional.empty();
    }

    @Override
    default Collection<BlockTrait<?>> getTraits() {
        return getTraitMap().keySet();
    }

    @Override
    default Collection<?> getTraitValues() {
        return getTraitMap().values();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Map<BlockTrait<?>, ?> getTraitMap() {
        return (ImmutableMap) getProperties();
    }

    @SuppressWarnings("unchecked")
    @Override
    default String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) getBlock()).getKey());
        final ImmutableMap<IProperty<?>, Comparable<?>> properties = (ImmutableMap<IProperty<?>, Comparable<?>>) (ImmutableMap<?, ?>) this.getProperties();
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
        return builder.toString();
    }

    @Override
    default String getName() {
        return getId();
    }

    @Override
    default <T extends Property<?, ?>> Optional<T> getProperty(Direction direction, Class<T> clazz) {
        return Optional.empty();
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
                .set(DataQueries.BLOCK_STATE, getId());
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
    default <E> Optional<BlockState> transform(Key<? extends BaseValue<E>> key, Function<E, E> function) {
        return Optional.empty();
    }

    @Override
    default <E> Optional<BlockState> with(Key<? extends BaseValue<E>> key, E value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(BaseValue<?> value) {
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
    default <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    default Collection<Property<?, ?>> getApplicableProperties() {
        return Collections.emptyList();
    }

    @Override
    default <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
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
    default Set<ImmutableValue<?>> getValues() {
        return Collections.emptySet();
    }
}
