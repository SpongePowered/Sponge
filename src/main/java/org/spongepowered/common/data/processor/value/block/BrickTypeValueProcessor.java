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
package org.spongepowered.common.data.processor.value.block;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.BrickType;
import org.spongepowered.api.data.type.BrickTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class BrickTypeValueProcessor extends AbstractCatalogDataValueProcessor<BrickType, Value<BrickType>> {

    public BrickTypeValueProcessor() {
        super(Keys.BRICK_TYPE);
    }

    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem() == ItemTypes.STONEBRICK || container.getItem() == ItemTypes.STONE_BRICK_STAIRS;
    }

    @Override
    protected BrickType getFromMeta(int meta) {
        return (BrickType) (Object) BlockStoneBrick.EnumType.byMetadata(meta);
    }

    @Override
    protected int setToMeta(BrickType type) {
        return ((BlockStoneBrick.EnumType) (Object) type).getMetadata();
    }

    @Override
    protected Value<BrickType> constructValue(BrickType defaultValue) {
        return new SpongeValue<>(Keys.BRICK_TYPE, BrickTypes.DEFAULT, defaultValue);
    }
}
