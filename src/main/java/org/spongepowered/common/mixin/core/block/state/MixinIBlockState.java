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

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Mixin(IBlockState.class)
public interface MixinIBlockState extends IBlockState, BlockState {

    @Override
    default BlockType getType() {
        return (BlockType) getBlock();
    }
    @Override
    default BlockState withExtendedProperties(Location<World> location) {
        return (BlockState) getBlock().getActualState(this, (net.minecraft.world.World) location.getExtent(), VecHelper.toBlockPos(location));

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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

    @SuppressWarnings("rawtypes")
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

}
