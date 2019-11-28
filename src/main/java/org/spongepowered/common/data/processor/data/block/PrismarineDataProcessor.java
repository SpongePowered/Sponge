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
package org.spongepowered.common.data.processor.data.block;

import net.minecraft.block.BlockPrismarine;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePrismarineData;
import org.spongepowered.api.data.manipulator.mutable.block.PrismarineData;
import org.spongepowered.api.data.type.PrismarineType;
import org.spongepowered.api.data.type.PrismarineTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePrismarineData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class PrismarineDataProcessor extends
        AbstractCatalogDataProcessor<PrismarineType, Value<PrismarineType>, PrismarineData, ImmutablePrismarineData> {

    public PrismarineDataProcessor() {
        super(Keys.PRISMARINE_TYPE, input -> input.getItem() == ItemTypes.PRISMARINE);
    }

    @Override
    protected int setToMeta(PrismarineType value) {
        return ((BlockPrismarine.EnumType) (Object) value).func_176807_a();
    }

    @Override
    protected PrismarineType getFromMeta(int meta) {
        return (PrismarineType) (Object) BlockPrismarine.EnumType.func_176810_a(meta);
    }

    @Override
    public PrismarineData createManipulator() {
        return new SpongePrismarineData();
    }

    @Override
    protected PrismarineType getDefaultValue() {
        return PrismarineTypes.BRICKS;
    }

    @Override
    protected Value<PrismarineType> constructValue(PrismarineType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
