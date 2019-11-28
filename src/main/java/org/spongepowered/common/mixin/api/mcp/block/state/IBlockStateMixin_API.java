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
package org.spongepowered.common.mixin.api.mcp.block.state;

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
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Mixin(IBlockState.class)
public interface IBlockStateMixin_API extends IBlockState, BlockState {

    @Override
    default BlockType getType() {
        return (BlockType) func_177230_c();
    }

    @Override
    default BlockState withExtendedProperties(final Location<World> location) {
        return (BlockState) this.func_185899_b((net.minecraft.world.World) location.getExtent(), VecHelper.toBlockPos(location));

    }

    @Override
    default BlockState cycleValue(final Key<? extends BaseValue<? extends Cycleable<?>>> key) {
        return this;
    }


    @SuppressWarnings({"unchecked"})
    @Override
    default <T extends Comparable<T>> Optional<T> getTraitValue(final BlockTrait<T> blockTrait) {
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : func_177228_b().entrySet()) {
            //noinspection EqualsBetweenInconvertibleTypes
            if (entry.getKey() == blockTrait) {
                return Optional.of((T) entry.getValue());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    default Optional<BlockTrait<?>> getTrait(final String blockTrait) {
        for (final IProperty property : func_177228_b().keySet()) {
            if (property.func_177701_a().equalsIgnoreCase(blockTrait)) {
                return Optional.of((BlockTrait<?>) property);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"rawtypes", "unchecked", "RedundantCast"})
    @Override
    default Optional<BlockState> withTrait(final BlockTrait<?> trait, final Object value) {
        if (value instanceof String) {
            Comparable foundValue = null;
            for (final Comparable comparable : trait.getPossibleValues()) {
                if (comparable.toString().equals(value)) {
                    foundValue = comparable;
                    break;
                }
            }
            if (foundValue != null) {
                return Optional.of((BlockState) this.func_177226_a((IProperty) trait, foundValue));
            }
        }
        if (value instanceof Comparable) {
            if (func_177228_b().containsKey((IProperty) trait) && ((IProperty) trait).func_177700_c().contains(value)) {
                return Optional.of((BlockState) this.func_177226_a((IProperty) trait, (Comparable) value));
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
        return (ImmutableMap) func_177228_b();
    }

    @Override
    default String getId() {
        final StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) func_177230_c()).getId());
        final ImmutableMap<IProperty<?>, Comparable<?>> properties =  this.func_177228_b();
        if (!properties.isEmpty()) {
            builder.append('[');
            final Joiner joiner = Joiner.on(',');
            final List<String> propertyValues = new ArrayList<>();
            for (final Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
                propertyValues.add(entry.getKey().func_177701_a() + "=" + entry.getValue());
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
    default <T extends Property<?, ?>> Optional<T> getProperty(final Direction direction, final Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    default List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return Collections.emptyList();
    }

    @Override
    default int getContentVersion() {
        return Constants.Sponge.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    default DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(Constants.Block.BLOCK_STATE, getId());
    }

    @Override
    default <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(final Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    default <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(final Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    default boolean supports(final Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return false;
    }

    @Override
    default <E> Optional<BlockState> transform(final Key<? extends BaseValue<E>> key, final Function<E, E> function) {
        return Optional.empty();
    }

    @Override
    default <E> Optional<BlockState> with(final Key<? extends BaseValue<E>> key, final E value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(final BaseValue<?> value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(final ImmutableDataManipulator<?, ?> valueContainer) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(final Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> without(final Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty();
    }

    @Override
    default BlockState merge(final BlockState that) {
        return this;
    }

    @Override
    default BlockState merge(final BlockState that, final MergeFunction function) {
        return this;
    }

    @Override
    default List<ImmutableDataManipulator<?, ?>> getContainers() {
        return Collections.emptyList();
    }

    @Override
    default <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        return Optional.empty();
    }

    @Override
    default Collection<Property<?, ?>> getApplicableProperties() {
        return Collections.emptyList();
    }

    @Override
    default <E> Optional<E> get(final Key<? extends BaseValue<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends BaseValue<E>> Optional<V> getValue(final Key<V> key) {
        return Optional.empty();
    }

    @Override
    default boolean supports(final Key<?> key) {
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
