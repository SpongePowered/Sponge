/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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

package org.spongepowered.common.data.processor.value;

import com.google.common.base.Optional;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.BlockValueProcessor;

public class SpongePlantTypeValueProcessor implements BlockValueProcessor<PlantType, Value<PlantType>> {

    @Override
    public Key<? extends BaseValue<PlantType>> getKey() {
        return Keys.PLANT_TYPE;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean supports(IBlockState blockState) {
        return false;
    }

    @Override
    public Optional<PlantType> getValueForBlockState(IBlockState blockState) {
        return null;
    }

    @Override
    public Optional<Value<PlantType>> getApiValueForBlockState(IBlockState blockState) {
        return null;
    }

    @Override
    public Optional<BlockState> offerValue(IBlockState blockState, PlantType baseValue) {
        if (blockState.getBlock() == Blocks.red_flower) {
            final BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType) (Object) baseValue;
            return Optional.of((BlockState) blockState.withProperty(Blocks.red_flower.getTypeProperty(), flowerType));
        } else if (blockState.getBlock() == Blocks.yellow_flower) {
            return Optional.of((BlockState) blockState);
        }
        return Optional.absent();
    }
}
