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

import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerColor;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class PlantTypeValueProcessor extends AbstractCatalogDataValueProcessor<PlantType, Value<PlantType>> {

    public PlantTypeValueProcessor() {
        super(Keys.PLANT_TYPE);
    }

    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem() == ItemTypes.RED_FLOWER || container.getItem() == ItemTypes.YELLOW_FLOWER;
    }

    @Override
    protected Value<PlantType> constructValue(PlantType defaultValue) {
        return new SpongeValue<>(Keys.PLANT_TYPE, PlantTypes.DANDELION, defaultValue);
    }

    @Override
    protected PlantType getFromMeta(int meta) {
        return null;
    }

    @Override
    protected int setToMeta(PlantType value) {
        return ((BlockFlower.EnumFlowerType) (Object) value).getMeta();
    }

    @Override
    protected Optional<PlantType> getVal(ItemStack itemStack) {
        if (itemStack.getItem() == ItemTypes.RED_FLOWER) {
            return Optional.of((PlantType) (Object) BlockFlower.EnumFlowerType.getType(EnumFlowerColor.RED, itemStack.getItemDamage()));
        } else {
            return Optional.of((PlantType) (Object) BlockFlower.EnumFlowerType.getType(EnumFlowerColor.YELLOW, itemStack.getItemDamage()));
        }
    }

}
