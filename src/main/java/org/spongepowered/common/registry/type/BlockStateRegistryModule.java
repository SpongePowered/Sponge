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
import org.spongepowered.api.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BlockStateRegistryModule implements CatalogRegistryModule<BlockState> {

    private final Map<String, BlockState> blockStateMap = new LinkedHashMap<>();

    public static BlockStateRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Optional<BlockState> getById(String id) {
        return Optional.ofNullable(this.blockStateMap.get(checkNotNull(id, "Id cannot be null!").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<BlockState> getAll() {
        return ImmutableList.copyOf(this.blockStateMap.values());
    }

    void registerBlockState(BlockState blockState) {
        checkNotNull(blockState, "BlockState cannot be null!");
        final String key = blockState.getKey().toString().toLowerCase(Locale.ENGLISH);
        if (!this.blockStateMap.containsKey(key)) {
            this.blockStateMap.put(key, blockState);
        }
    }

    BlockStateRegistryModule() {
    }

    private static final class Holder {
        static final BlockStateRegistryModule INSTANCE = new BlockStateRegistryModule();
    }

}
