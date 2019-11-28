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

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutablePistonData;
import org.spongepowered.api.data.manipulator.mutable.block.PistonData;
import org.spongepowered.api.data.type.PistonType;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongePistonData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class PistonDataProcessor extends AbstractCatalogDataProcessor<PistonType, Value<PistonType>, PistonData, ImmutablePistonData> {

    public PistonDataProcessor() {
        super(Keys.PISTON_TYPE, input -> input.getItem() == ItemTypes.PISTON);
    }

    @Override
    protected int setToMeta(PistonType value) {
        // Not used due to overriding set method
        return -1;

    }

    @Override
    protected PistonType getFromMeta(int meta) {
        boolean isSticky = (meta & 8) > 0;
        return isSticky ? PistonTypes.STICKY : PistonTypes.NORMAL;
    }

    @Override
    public PistonData createManipulator() {
        return new SpongePistonData();
    }

    @Override
    protected boolean set(ItemStack itemStack, PistonType value) {
        int oldMeta = itemStack.getDamage();
        boolean isSticky = (oldMeta & 8) > 0;
        boolean isStickyValue = value.equals(PistonTypes.STICKY);
        if (isSticky && !isStickyValue) {
            itemStack.setItemDamage(oldMeta - 8);
        }
        if (!isSticky && isStickyValue) {
            itemStack.setItemDamage(oldMeta + 8);
        }
        return true;
    }

    @Override
    protected PistonType getDefaultValue() {
        return PistonTypes.NORMAL;
    }

    @Override
    protected Value<PistonType> constructValue(PistonType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
