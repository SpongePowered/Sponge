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
package org.spongepowered.common.mixin.core.item.data;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.component.base.DisplayNameComponent;
import org.spongepowered.api.data.component.item.EnchantmentComponent;
import org.spongepowered.api.data.component.item.LoreComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.item.IMixinItem;

import java.util.List;

@Mixin(Item.class)
public abstract class MixinItem implements IMixinItem {

    @Override
    public List<Component<?>> getManipulatorsFor(ItemStack itemStack) {
        final List<Component<?>> list = Lists.newArrayList();
        if (!itemStack.hasTagCompound()) {
            return list;
        }
        if (itemStack.isItemEnchanted()) {
            list.add(getData(itemStack, EnchantmentComponent.class));
        }
        if (itemStack.getTagCompound().hasKey("display")) {
            final NBTTagCompound displayCompound = itemStack.getTagCompound().getCompoundTag("display");
            if (displayCompound.hasKey("Name")) {
                list.add(getData(itemStack, DisplayNameComponent.class));
            }
            if (displayCompound.hasKey("Lore")) {
                list.add(getData(itemStack, LoreComponent.class));
            }
        }
        return list;
    }

    protected final <T extends Component<T>> T getData(ItemStack itemStack, Class<T> manipulatorClass) {
        return ((org.spongepowered.api.item.inventory.ItemStack) itemStack).getData(manipulatorClass).get();
    }
}
