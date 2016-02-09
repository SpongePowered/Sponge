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
package org.spongepowered.common.registry.type.block;

import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.common.registry.type.ImmutableCatalogRegistryModule;

import java.util.function.BiConsumer;

public final class SlabTypeRegistryModule extends ImmutableCatalogRegistryModule<SlabType> {

    @Override
    protected void collect(BiConsumer<String, SlabType> consumer) {
        addUnsafe(consumer, BlockStoneSlab.EnumType.BRICK, "brick");
        addUnsafe(consumer, BlockStoneSlab.EnumType.COBBLESTONE, "cobblestone");
        addUnsafe(consumer, BlockStoneSlab.EnumType.NETHERBRICK, "netherbrick");
        addUnsafe(consumer, BlockStoneSlab.EnumType.QUARTZ, "quartz");
        addUnsafe(consumer, BlockStoneSlab.EnumType.SAND, "sand");
        addUnsafe(consumer, BlockStoneSlab.EnumType.SMOOTHBRICK, "smooth_brick");
        addUnsafe(consumer, BlockStoneSlab.EnumType.STONE, "stone");
        addUnsafe(consumer, BlockStoneSlab.EnumType.WOOD, "wood");
        addUnsafe(consumer, BlockStoneSlabNew.EnumType.RED_SANDSTONE, "red_sand");
    };

}
