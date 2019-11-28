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

import net.minecraft.block.TallGrassBlock;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableShrubData;
import org.spongepowered.api.data.manipulator.mutable.block.ShrubData;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeShrubData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class ShrubDataProcessor extends AbstractCatalogDataProcessor<ShrubType, Value<ShrubType>, ShrubData, ImmutableShrubData> {

    public ShrubDataProcessor() {
        super(Keys.SHRUB_TYPE, input -> input.getItem() == ItemTypes.TALLGRASS);
    }

    @Override
    public ShrubData createManipulator() {
        return new SpongeShrubData();
    }

    @Override
    protected ShrubType getDefaultValue() {
        return ShrubTypes.FERN;
    }

    @Override
    protected ShrubType getFromMeta(int meta) {
        return (ShrubType) (Object) TallGrassBlock.EnumType.func_177045_a(meta);
    }

    @Override
    protected int setToMeta(ShrubType type) {
        return ((TallGrassBlock.EnumType) (Object) type).func_177044_a();
    }

    @Override
    protected Value<ShrubType> constructValue(ShrubType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
