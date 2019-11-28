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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.state.StateProperty;
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

@Mixin(net.minecraft.block.BlockState.class)
public interface IBlockStateMixin_API extends net.minecraft.block.BlockState, BlockState {

    @Override
    default BlockType getType() {
        return (BlockType) getBlock();
    }

    @Override
    default BlockState withExtendedProperties(final Location<World> location) {
        return (BlockState) this.getActualState((net.minecraft.world.World) location.getExtent(), VecHelper.toBlockPos(location));

    }

    @Override
    default BlockState cycleValue(final Key<? extends Value<? extends Cycleable<?>>> key) {
        return this;
    }


    @SuppressWarnings({"unchecked"})
    @Override
    default <T extends Comparable<T>> Optional<T> getTraitValue(final StateProperty<T> blockTrait) {
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : getProperties().entrySet()) {
            //noinspection EqualsBetweenInconvertibleTypes
            if (entry.getKey() == blockTrait) {
                return Optional.of((T) entry.getValue());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    default Optional<StateProperty<?>> getTrait(final String blockTrait) {
        for (final IProperty property : getProperties().keySet()) {
            if (property.getName().equalsIgnoreCase(blockTrait)) {
                return Optional.of((StateProperty<?>) property);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"rawtypes", "unchecked", "RedundantCast"})
    @Override
    default Optional<BlockState> withTrait(final StateProperty<?> trait, final Object value) {
        if (value instanceof String) {
            Comparable foundValue = null;
            for (final Comparable comparable : trait.getPossibleValues()) {
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
            if (getProperties().containsKey((IProperty) trait) && ((IProperty) trait).getAllowedValues().contains(value)) {
                return Optional.of((BlockState) this.withProperty((IProperty) trait, (Comparable) value));
            }
        }
        return Optional.empty();
    }

    @Override
    default Collection<StateProperty<?>> getTraits() {
        return getTraitMap().keySet();
    }

    @Override
    default Collection<?> getTraitValues() {
        return getTraitMap().values();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Map<StateProperty<?>, ?> getTraitMap() {
        return (ImmutableMap) getProperties();
    }

    @Override
    default String getId() {
        final StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) getBlock()).getId());
        final ImmutableMap<IProperty<?>, Comparable<?>> properties =  this.getProperties();
        if (!properties.isEmpty()) {
            builder.append('[');
            final Joiner joiner = Joiner.on(',');
            final List<String> propertyValues = new ArrayList<>();
            for (final Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
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
    default <T extends Property<?, ?>> Optional<T> getProperty(final Direction direction, final Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    default List<Immutable<?, ?>> getManipulators() {
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
    default <T extends Immutable<?, ?>> Optional<T> get(final Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    default <T extends Immutable<?, ?>> Optional<T> getOrCreate(final Class<T> containerClass) {
        return Optional.empty();
    }

    @Override
    default boolean supports(final Class<? extends Immutable<?, ?>> containerClass) {
        return false;
    }

    @Override
    default <E> Optional<BlockState> transform(final Key<? extends Value<E>> key, final Function<E, E> function) {
        return Optional.empty();
    }

    @Override
    default <E> Optional<BlockState> with(final Key<? extends Value<E>> key, final E value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(final Value<?> value) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(final Immutable<?, ?> valueContainer) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> with(final Iterable<Immutable<?, ?>> valueContainers) {
        return Optional.empty();
    }

    @Override
    default Optional<BlockState> without(final Class<? extends Immutable<?, ?>> containerClass) {
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
    default List<Immutable<?, ?>> getContainers() {
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
    default <E> Optional<E> get(final Key<? extends Value<E>> key) {
        return Optional.empty();
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
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
    default Set<org.spongepowered.api.data.value.Value.Immutable<?>> getValues() {
        return Collections.emptySet();
    }
}
