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

import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.api.data.type.ArmorType;

import java.util.Optional;

// TODO add ArmorType back to API - IArmorMaterial/ArmorMaterial exists e138911acad1eedfaf927483804587891213a887
public class ItemStackArmorTypeProvider extends ItemStackDataProvider<ArmorType> {

    public ItemStackArmorTypeProvider() {
        super(Keys.ARMOR_TYPE);
    }

    @Override
    protected Optional<ArmorType> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item instanceof ArmorItem) {
            IArmorMaterial armor = ((ArmorItem) item).getArmorMaterial();
            if (armor instanceof ArmorType) {
                return Optional.of((ArmorType)armor);
            }
        }
        return Optional.empty();
    }


    @Override
    protected boolean supports(Item item) {
        return item instanceof ArmorItem;
    }

}
