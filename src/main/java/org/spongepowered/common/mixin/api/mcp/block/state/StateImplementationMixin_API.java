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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.Cycleable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * This shares implementation with {@link IBlockStateMixin_API}, since this
 * all relies on Data API implementations.
 */
@Mixin(targets = "net.minecraft.block.state.BlockStateContainer$StateImplementation")
public abstract class StateImplementationMixin_API extends BlockStateBase implements BlockState {

    @Shadow @Final private Block block;
    @Shadow @Final private ImmutableMap<IProperty<?>, Comparable<?>> properties;

    // All of these fields are lazily evaluated either at startup or the first time
    // they are accessed by a plugin, depending on how much of an impact the
    // implementation can pose during start up, or whether game state
    // can affect the various systems in place (i.e. we sometimes can't load certain
    // systems before other registries have finished registering their stuff)
    @Nullable private ImmutableSet<ImmutableValue<?>> api$values;
    @Nullable private ImmutableSet<Key<?>> api$keys;
    @Nullable private ImmutableList<ImmutableDataManipulator<?, ?>> api$manipulators;
    @Nullable private ImmutableMap<Key<?>, Object> api$keyMap;
    @Nullable private ImmutableMap<Class<? extends Property<?, ?>>, Property<?, ?>> api$dataProperties;
    @Nullable private String api$id;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public BlockState cycleValue(final Key<? extends BaseValue<? extends Cycleable<?>>> key) {
        final Optional<Cycleable<?>> optional = get((Key) key);
        return optional
            .map(Cycleable::cycleNext)
            .map(newVal -> {
                BlockState o = null;
                try {
                    o = (BlockState) with((Key) key, newVal)
                        .orElseThrow(() -> new IllegalStateException("Unable to retrieve a cycled BlockState for key: " + key + " and value: " + newVal));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return o;
            })
            .orElseThrow(() -> new IllegalArgumentException("Used an invalid cycleable key! Check with supports in the future!"));
    }

    @Override
    public BlockSnapshot snapshotFor(final Location<World> location) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled()
            .blockState((net.minecraft.block.BlockState) (Object) this)
            .position(location.getBlockPosition())
            .worldId(location.getExtent().getUniqueId());
        if (this.block.func_149716_u() && location.getBlockType().equals(this.block)) {
            final TileEntity tileEntity = location.getTileEntity()
                .orElseThrow(() -> new IllegalStateException("Unable to retrieve a TileEntity for location: " + location));
            for (final DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) tileEntity).bridge$getCustomManipulators()) {
                builder.add(manipulator);
            }
            final CompoundNBT compound = new CompoundNBT();
            ((net.minecraft.tileentity.TileEntity) tileEntity).func_189515_b(compound);
            builder.unsafeNbt(compound);
        }
        return builder.build();
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators() {
        return lazyLoadManipulatorsAndKeys();
    }

    @Nullable
    private ImmutableMap<Key<?>, Object> getKeyMap() {
        if (this.api$keyMap == null) {
            lazyLoadManipulatorsAndKeys();
        }
        return this.api$keyMap;
    }

    private ImmutableList<ImmutableDataManipulator<?, ?>> lazyLoadManipulatorsAndKeys() {
        if (this.api$manipulators == null) {
            this.api$manipulators = ImmutableList.copyOf(((BlockBridge) this.block).bridge$getManipulators(this));
        }
        if (this.api$keyMap == null) {
            final ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
            final ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();
            final ImmutableSet.Builder<ImmutableValue<?>> valueBuilder = ImmutableSet.builder();
            for (final ImmutableDataManipulator<?, ?> manipulator : this.api$manipulators) {
                for (final ImmutableValue<?> value : manipulator.getValues()) {
                    builder.put(value.getKey(), value.get());
                    valueBuilder.add(value);
                    keyBuilder.add(value.getKey());
                }
            }
            this.api$values = valueBuilder.build();
            this.api$keys = keyBuilder.build();
            this.api$keyMap = builder.build();
        }
        return this.api$manipulators;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> get(final Class<T> containerClass) {
        for (final ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ImmutableDataManipulator<?, ?>> Optional<T> getOrCreate(final Class<T> containerClass) {
        for (final ImmutableDataManipulator<?, ?> manipulator : this.getManipulators()) {
            if (containerClass.isInstance(manipulator)) {
                return Optional.of(((T) manipulator));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(final Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return ((BlockBridge) this.block).bridge$supports(containerClass);
    }

    @Override
    public <E> Optional<BlockState> transform(final Key<? extends BaseValue<E>> key, final Function<E, E> function) {
        return this.get(checkNotNull(key, "Key cannot be null!")) // If we don't have a value for the key, we don't support it.
            .map(checkNotNull(function, "Function cannot be null!"))
            .map(newVal -> with(key, newVal).orElse(this)); // We can either return this value or the updated value, but not an empty
    }

    @Override
    public <E> Optional<BlockState> with(final Key<? extends BaseValue<E>> key, final E value) {
        if (!supports(key)) {
            return Optional.empty();
        }
        return ((BlockBridge) this.block).bridge$getStateWithValue(this, key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<BlockState> with(final BaseValue<?> value) {
        return with((Key<? extends BaseValue<Object>>) value.getKey(), value.get());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Optional<BlockState> with(final ImmutableDataManipulator<?, ?> valueContainer) {
        if (supports((Class<ImmutableDataManipulator<?, ?>>) valueContainer.getClass())) {
            return ((BlockBridge) this.block).bridge$getStateWithData(this, valueContainer);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BlockState> with(final Iterable<ImmutableDataManipulator<?, ?>> valueContainers) {
        BlockState state = this;
        for (final ImmutableDataManipulator<?, ?> manipulator : valueContainers) {
            final Optional<BlockState> optional = state.with(manipulator);
            if (optional.isPresent()) {
                state = optional.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(state);
    }

    @Override
    public Optional<BlockState> without(final Class<? extends ImmutableDataManipulator<?, ?>> containerClass) {
        return Optional.empty(); // By default, all manipulators have to have the manipulator if it exists, we can't remove data.
    }

    @Override
    public BlockState merge(final BlockState that) {
        if (!getType().equals(that.getType())) {
            return this;
        }
        BlockState temp = this;
        for (final ImmutableDataManipulator<?, ?> manipulator : that.getManipulators()) {
            final Optional<BlockState> optional = temp.with(manipulator);
            if (optional.isPresent()) {
                temp = optional.get();
            } else {
                return temp;
            }
        }
        return temp;
    }

    @Override
    public BlockState merge(final BlockState that, final MergeFunction function) {
        if (!getType().equals(that.getType())) {
            return this;
        }
        BlockState temp = this;
        for (final ImmutableDataManipulator<?, ?> manipulator : that.getManipulators()) {
            @Nullable final ImmutableDataManipulator<?, ?> old = temp.get(manipulator.getClass()).orElse(null);
            final Optional<BlockState> optional = temp.with(checkNotNull(function.merge(old, manipulator)));
            if (optional.isPresent()) {
                temp = optional.get();
            } else {
                return temp;
            }
        }
        return temp;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        return Optional.ofNullable((T) this.getSpongeInternalProperties().get(propertyClass));
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return this.getSpongeInternalProperties().values();
    }

    private ImmutableMap<Class<? extends Property<?, ?>>, Property<?, ?>> getSpongeInternalProperties() {
        if (this.api$dataProperties == null) {
            this.api$dataProperties = ((BlockBridge) this.block).bridge$getProperties(this);
        }
        return this.api$dataProperties;
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getContainers() {
        return this.getManipulators();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<E> get(final Key<? extends BaseValue<E>> key) {
        return Optional.ofNullable((E) this.getKeyMap().get(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(final Key<V> key) {
        checkNotNull(key);
        for (final ImmutableValue<?> value : this.getValues()) {
            if (value.getKey().equals(key)) {
                return Optional.of((V) value.asMutable());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(final Key<?> key) {
        return this.getKeys().contains(checkNotNull(key));
    }

    @Override
    public BlockState copy() {
        return this;
    }

    @Override
    public Set<Key<?>> getKeys() {
        if (this.api$keys == null) {
            lazyLoadManipulatorsAndKeys();
        }
        return this.api$keys;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        if (this.api$values == null) {
            lazyLoadManipulatorsAndKeys();
        }
        return this.api$values;
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Constants.Block.BLOCK_STATE, this.getId());
    }

    @Override
    public String getId() {
        if (this.api$id == null) {
            impl$generateIdFromParentBlock(this.block);
        }
        return this.api$id;
    }

    @Override
    public String getName() {
        if (this.api$id == null) {
            impl$generateIdFromParentBlock(this.block);
        }
        return this.api$id;
    }

    private void impl$generateIdFromParentBlock(final Block block) {
        final StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) block).getId());
        if (!this.properties.isEmpty()) {
            builder.append('[');
            final Joiner joiner = Joiner.on(',');
            final List<String> propertyValues = new ArrayList<>();
            for (final Map.Entry<IProperty<?>, Comparable<?>> entry : this.properties.entrySet()) {
                final Comparable<?> value = entry.getValue();
                final String stringValue = (value instanceof IStringSerializable) ? ((IStringSerializable) value).func_176610_l() : value.toString();
                propertyValues.add(entry.getKey().func_177701_a() + "=" + stringValue);
            }
            builder.append(joiner.join(propertyValues));
            builder.append(']');
        }
        this.api$id = builder.toString();
    }
}
