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
package org.spongepowered.common.data.property.store.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.store.DoublePropertyStore;
import org.spongepowered.api.data.property.store.IntPropertyStore;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import javax.annotation.Nullable;

public abstract class AbstractBlockPropertyStore<V> extends AbstractSpongePropertyStore<V> {

    final boolean checksItemStack;

    AbstractBlockPropertyStore(boolean checksItemStack) {
        this.checksItemStack = checksItemStack;
    }

    public abstract static class Generic<V> extends AbstractBlockPropertyStore<V> {

        protected Generic(boolean checksItemStack) {
            super(checksItemStack);
        }

        /**
         * Gets the property for the block, if the block is actually containing a
         * property in the first place.
         *
         * @param location The location
         * @param block The block
         * @return The property value, if available
         */
        protected abstract Optional<V> getForBlock(@Nullable Location location, IBlockState block, @Nullable EnumFacing facing);

        @SuppressWarnings({"unchecked", "ConstantConditions"})
        @Override
        public final Optional<V> getFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof Location) {
                final Location location = (Location) propertyHolder;
                return getFor(location, Direction.NONE);
            } else if (this.checksItemStack && (Object) propertyHolder instanceof ItemStack) {
                final Item item = ((ItemStack)  (Object) propertyHolder).getItem();
                if (item instanceof ItemBlock) {
                    final Block block = ((ItemBlock) item).getBlock();
                    if (block != null) {
                        return getForBlock(null, block.getDefaultState(), null);
                    }
                }
            } else if (propertyHolder instanceof IBlockState) {
                return getForBlock(null, ((IBlockState) propertyHolder), null);
            } else if (propertyHolder instanceof Block) {
                return getForBlock(null, ((Block) propertyHolder).getDefaultState(), null);
            }
            return Optional.empty();
        }

        @Override
        public final Optional<V> getFor(Location location, Direction direction) {
            return getForBlock(location, (IBlockState) location.getBlock(), toEnumFacing(direction).orElse(null));
        }
    }

    public abstract static class Dbl extends AbstractBlockPropertyStore<Double> implements DoublePropertyStore {

        protected Dbl(boolean checksItemStack) {
            super(checksItemStack);
        }

        protected abstract OptionalDouble getForBlock(@Nullable Location location, IBlockState block, @Nullable EnumFacing facing);

        @SuppressWarnings({"unchecked", "ConstantConditions"})
        @Override
        public final OptionalDouble getDoubleFor(PropertyHolder propertyHolder) {
            if (propertyHolder instanceof Location) {
                final Location location = (Location) propertyHolder;
                return getDoubleFor(location, Direction.NONE);
            } else if (this.checksItemStack &&  (Object) propertyHolder instanceof ItemStack) {
                final Item item = ((ItemStack)  (Object) propertyHolder).getItem();
                if (item instanceof ItemBlock) {
                    final Block block = ((ItemBlock) item).getBlock();
                    if (block != null) {
                        return getForBlock(null, block.getDefaultState(), null);
                    }
                }
            } else if (propertyHolder instanceof IBlockState) {
                return getForBlock(null, ((IBlockState) propertyHolder), null);
            } else if (propertyHolder instanceof Block) {
                return getForBlock(null, ((Block) propertyHolder).getDefaultState(), null);
            }
            return OptionalDouble.empty();
        }

        @Override
        public final OptionalDouble getDoubleFor(Location location, Direction direction) {
            return getForBlock(location, (IBlockState) location.getBlock(), toEnumFacing(direction).orElse(null));
        }

        @Override
        public Optional<Double> getFor(PropertyHolder propertyHolder) {
            final OptionalDouble optionalDouble = getDoubleFor(propertyHolder);
            return optionalDouble.isPresent() ? Optional.of(optionalDouble.getAsDouble()) : Optional.empty();
        }

        @Override
        public Optional<Double> getFor(Location location, Direction direction) {
            final OptionalDouble optionalDouble = getDoubleFor(location, direction);
            return optionalDouble.isPresent() ? Optional.of(optionalDouble.getAsDouble()) : Optional.empty();
        }
    }

    public abstract static class IntValue extends AbstractBlockPropertyStore<Integer> implements IntPropertyStore {

        protected IntValue(boolean checksItemStack) {
            super(checksItemStack);
        }

        // TODO: Optimize by using optional ints

        @Override
        public OptionalInt getIntFor(PropertyHolder propertyHolder) {
            return getFor(propertyHolder).map(OptionalInt::of).orElseGet(OptionalInt::empty);
        }

        @Override
        public OptionalInt getIntFor(Location location, Direction direction) {
            return getFor(location, direction).map(OptionalInt::of).orElseGet(OptionalInt::empty);
        }
    }
}
