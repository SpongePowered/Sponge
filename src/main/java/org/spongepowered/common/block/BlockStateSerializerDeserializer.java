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
package org.spongepowered.common.block;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.Block;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockStateMatcher;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.util.Tuple;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockStateSerializerDeserializer {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Function<Map.Entry<IProperty<?>, Comparable<?>>, String> MAP_ENTRY_TO_STRING = p_apply_1_ -> {
        if (p_apply_1_ == null) {
            return "<NULL>";
        } else {
            final IProperty iproperty = p_apply_1_.getKey();
            return iproperty.getName() + "=" + iproperty.getName(p_apply_1_.getValue());
        }
    };

    public static String serialize(final BlockState state) {
        final StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Registry.BLOCK.getKey((Block) state.getType()).toString());
        if (!((net.minecraft.block.BlockState) state).getValues().isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append(
                ((net.minecraft.block.BlockState) state).getValues()
                    .entrySet()
                    .stream()
                    .map(BlockStateSerializerDeserializer.MAP_ENTRY_TO_STRING)
                    .collect(Collectors.joining(","))
            );
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Optional<BlockState> deserialize(final String string) {
        final String state = checkNotNull(string, "Id cannot be null!").toLowerCase(Locale.ENGLISH);
        if (state.contains("[")) {
            final String[] split = state.split("\\[");
            final ResourceLocation key = ResourceLocation.tryCreate(split[0]);
            return Registry.BLOCK.getValue(key)
                .flatMap(block -> {
                    final Collection<IProperty<?>> properties = block.getStateContainer().getProperties();
                    final String propertyValues = split[1].replace("[", "").replace("]", "");
                    if (properties.isEmpty()) {
                        throw new IllegalArgumentException("The properties cannot be specified and empty (omit [] if there are no properties)");
                    }
                    final String[] propertyValuePairs = propertyValues.split(",");
                    final List<? extends Tuple<? extends IProperty<?>, ?>> propertyValuesFound = Arrays.stream(propertyValuePairs)
                        .map(propertyValue -> propertyValue.split("="))
                        .filter(pair -> pair.length == 2)
                        .map(pair -> Optional.ofNullable(block.getStateContainer().getProperty(pair[0]))
                            .flatMap(property -> property.parseValue(pair[1]).map(value -> Tuple.of(property, value))))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                    final BlockStateMatcher.Builder matcher = BlockState.matcher((BlockType) block);
                    propertyValuesFound.forEach(tuple -> matcher.property((StateProperty) tuple.getFirst(), (Comparable) tuple.getSecond()));

                    return matcher.build()
                        .getCompatibleStates()
                        .stream()
                        .findFirst();
                });

        }
        final ResourceLocation block = ResourceLocation.tryCreate(string);
        return (Optional<BlockState>) (Optional) Registry.BLOCK.getValue(block).map(Block::getDefaultState);
    }
}
