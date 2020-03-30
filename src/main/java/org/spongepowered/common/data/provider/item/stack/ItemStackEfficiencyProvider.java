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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.mixin.accessor.item.ToolItemAccessor;

import java.util.Optional;

public class ItemStackEfficiencyProvider extends ItemStackDataProvider<Float> {

    public ItemStackEfficiencyProvider() {
        super(Keys.EFFICIENCY);
    }

    @Override
    protected Optional<Float> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof ToolItemAccessor) {
            float efficiency = ((ToolItemAccessor) dataHolder.getItem()).accessor$getEfficiency();
            return Optional.of(efficiency);
        }
        return Optional.empty();
    }


    @Override
    protected boolean supports(Item item) {
        return item instanceof ToolItemAccessor;
    }
}
