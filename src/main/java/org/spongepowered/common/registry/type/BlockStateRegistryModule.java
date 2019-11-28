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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BlockStateRegistryModule implements CatalogRegistryModule<BlockState> {

    private final Map<String, BlockState> blockStateMap = new LinkedHashMap<>();

    public static BlockStateRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings({"rawTypes", "unchecked"})
    @Override
    public Optional<BlockState> getById(String id) {
        final String state = checkNotNull(id, "Id cannot be null!").toLowerCase(Locale.ENGLISH);
        final BlockState potential = this.blockStateMap.get(state);
        if (potential != null) {
            return Optional.of(potential);
        }
        if (state.contains("[")) {
            final String[] split = state.split("\\[");
            final Optional<BlockType> blockType = BlockTypeRegistryModule.getInstance().getById(split[0]);
            if (!blockType.isPresent()) {
                return Optional.empty();
            }
            final BlockType type = blockType.get();
            final Collection<BlockTrait<?>> traits = type.getTraits();
            final String properties = split[1].replace("[", "").replace("]", "");
            final String[] propertyValuePairs = properties.split(",");
            List<Tuple<BlockTrait<?>, ? extends Comparable<?>>> foundPairs = new ArrayList<>(propertyValuePairs.length);
            for (String propertyValuePair : propertyValuePairs) {
                final String name = propertyValuePair.split("=")[0];
                final String value = propertyValuePair.split("=")[1];
                type.getTrait(name).ifPresent(trait -> {
                    trait.parseValue(value).ifPresent(propertyValue -> {
                        foundPairs.add(Tuple.of(trait, propertyValue));
                    });
                });
            }

            final BlockState.MatcherBuilder matcher = BlockState.matcher(type);
            for (Tuple<BlockTrait<?>, ? extends Comparable<?>> foundPair : foundPairs) {
                //noinspection RedundantCast // Intellij will compile this, but the jdk won't.
                matcher.trait((BlockTrait) foundPair.getFirst(), (Comparable) foundPair.getSecond());
            }
            final BlockState.StateMatcher build = matcher.build();
            return type.getAllBlockStates().stream()
                .filter(build::matches)
                .findFirst();
        } else {
            return BlockTypeRegistryModule.getInstance().getById(state).map(BlockType::getDefaultState);
        }
    }

    @Override
    public Collection<BlockState> getAll() {
        return ImmutableList.copyOf(this.blockStateMap.values());
    }

    void registerBlockState(BlockState blockState) {
        checkNotNull(blockState, "BlockState cannot be null!");
        if (!this.blockStateMap.containsKey(blockState.getId().toLowerCase(Locale.ENGLISH))) {
            this.blockStateMap.put(blockState.getId().toLowerCase(Locale.ENGLISH), blockState);
        }
    }

    BlockStateRegistryModule() {
    }

    private static final class Holder {
        static final BlockStateRegistryModule INSTANCE = new BlockStateRegistryModule();
    }

}
