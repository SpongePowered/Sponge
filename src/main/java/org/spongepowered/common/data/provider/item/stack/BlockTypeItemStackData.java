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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.item.AdventureModePredicateAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlockTypeItemStackData {

    private BlockTypeItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.BREAKABLE_BLOCK_TYPES)
                        .get(h -> BlockTypeItemStackData.get(h, DataComponents.CAN_BREAK))
                        .set((h, v) -> BlockTypeItemStackData.set(h, DataComponents.CAN_BREAK, v))
                    .create(Keys.PLACEABLE_BLOCK_TYPES)
                        .get(h -> BlockTypeItemStackData.get(h, DataComponents.CAN_PLACE_ON))
                        .set((h, v) -> BlockTypeItemStackData.set(h, DataComponents.CAN_PLACE_ON, v));
    }
    // @formatter:on

    private static Set<BlockType> get(final ItemStack stack, final DataComponentType<AdventureModePredicate> component) {
        // TODO change API type to predicates
        final AdventureModePredicate predicate = stack.get(component);
        if (predicate != null) {
            return ((AdventureModePredicateAccessor) predicate).accessor$predicates().stream()
                    .flatMap(p -> p.blocks().orElse(HolderSet.direct()).stream())
                    .map(Holder::value).map(BlockType.class::cast)
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private static boolean set(final ItemStack stack, final DataComponentType<AdventureModePredicate> component, final Set<? extends BlockType> value) {
        if (value.isEmpty()) {
            stack.remove(component);
            return true;
        }

        final AdventureModePredicate prev = stack.get(component);
        final BlockPredicate blockPredicate = BlockPredicate.Builder.block().of(value.stream().map(Block.class::cast).toList()).build();
        final AdventureModePredicate predicate = new AdventureModePredicate(List.of(blockPredicate), prev == null || prev.showInTooltip());
        stack.set(component, predicate);
        return true;
    }
}
