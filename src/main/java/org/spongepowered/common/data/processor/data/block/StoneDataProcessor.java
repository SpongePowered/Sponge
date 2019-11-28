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

import net.minecraft.block.BlockStone;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableStoneData;
import org.spongepowered.api.data.manipulator.mutable.block.StoneData;
import org.spongepowered.api.data.type.StoneType;
import org.spongepowered.api.data.type.StoneTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeStoneData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class StoneDataProcessor extends AbstractCatalogDataProcessor<StoneType, Value<StoneType>, StoneData, ImmutableStoneData> {

    public StoneDataProcessor() {
        super(Keys.STONE_TYPE, input -> input.func_77973_b() == ItemTypes.STONE || input.func_77973_b() == ItemTypes.STONE_STAIRS);
    }

    @Override
    protected int setToMeta(StoneType value) {
        return ((BlockStone.EnumType) (Object) value).func_176642_a();
    }

    @Override
    protected StoneType getFromMeta(int meta) {
        return (StoneType) (Object) BlockStone.EnumType.func_176643_a(meta);
    }

    @Override
    public StoneData createManipulator() {
        return new SpongeStoneData();
    }

    @Override
    protected StoneType getDefaultValue() {
        return StoneTypes.STONE;
    }

    @Override
    protected Value<StoneType> constructValue(StoneType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
