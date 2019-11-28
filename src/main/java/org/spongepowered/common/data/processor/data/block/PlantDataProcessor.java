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

import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowerBlock.EnumFlowerColor;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePlantData;
import org.spongepowered.api.data.manipulator.mutable.block.PlantData;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePlantData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class PlantDataProcessor extends AbstractCatalogDataProcessor<PlantType, Value<PlantType>, PlantData, ImmutablePlantData> {

    public PlantDataProcessor() {
        super(Keys.PLANT_TYPE, input -> input.getItem() == ItemTypes.YELLOW_FLOWER || input.getItem() == ItemTypes.RED_FLOWER);
    }

    @Override
    public PlantData createManipulator() {
        return new SpongePlantData();
    }

    @Override
    protected PlantType getDefaultValue() {
        return PlantTypes.DANDELION;
    }

    @Override
    protected int setToMeta(PlantType value) {
        return ((FlowerBlock.EnumFlowerType) (Object) value).func_176968_b();
    }

    @Override
    protected PlantType getFromMeta(int meta) {
        return null; // not used
    }

    @Override
    protected Optional<PlantType> getVal(ItemStack itemStack) {
        if (itemStack.getItem() == ItemTypes.RED_FLOWER) {
            return Optional.of((PlantType) (Object) FlowerBlock.EnumFlowerType.func_176967_a(EnumFlowerColor.RED, itemStack.getDamage()));
        }
        return Optional.of((PlantType) (Object) FlowerBlock.EnumFlowerType.func_176967_a(EnumFlowerColor.YELLOW, itemStack.getDamage()));
    }

    @Override
    protected Value<PlantType> constructValue(PlantType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
