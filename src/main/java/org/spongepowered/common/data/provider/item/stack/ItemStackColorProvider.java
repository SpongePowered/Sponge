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

import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackColorProvider extends ItemStackDataProvider<Color> {

    public ItemStackColorProvider() {
        super(Keys.COLOR);
    }

    @Override
    protected Optional<Color> getFrom(ItemStack dataHolder) {
        int color = ((IDyeableArmorItem) dataHolder.getItem()).getColor(dataHolder);
        return color == -1 ? Optional.empty() : Optional.of(Color.ofRgb(color));
    }

    @Override
    protected boolean set(ItemStack dataHolder, Color value) {
        IDyeableArmorItem item = (IDyeableArmorItem) dataHolder.getItem();
        if (value == null) {
            item.removeColor(dataHolder);
        } else {
            item.setColor(dataHolder, toMojangColor(value));
        }
        return true;
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof IDyeableArmorItem;
    }

    private static int toMojangColor(final Color color) {
        return (((color.getRed() << 8) + color.getGreen()) << 8) + color.getBlue();
    }
}
