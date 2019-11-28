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
package org.spongepowered.common.data.property.store.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import org.spongepowered.api.data.property.item.ToolTypeProperty;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.common.data.property.store.common.AbstractItemStackPropertyStore;
import org.spongepowered.common.mixin.core.item.ItemHoeAccessor;
import org.spongepowered.common.mixin.core.item.ItemSwordAccessor;
import org.spongepowered.common.mixin.core.item.ItemToolAccessor;

import java.util.Optional;

public class ToolTypePropertyStore extends AbstractItemStackPropertyStore<ToolTypeProperty> {

    @SuppressWarnings({"ConstantConditions", "LocalCanBeFinal"})
    @Override
    protected Optional<ToolTypeProperty> getFor(ItemStack itemStack) {
        if (itemStack.func_77973_b() instanceof ItemToolAccessor) {
            final ItemToolAccessor tool = (ItemToolAccessor) itemStack.func_77973_b();
            final Item.ToolMaterial toolMaterial = tool.accessor$getToolMaterial();
            return Optional.of(new ToolTypeProperty((ToolType) (Object) toolMaterial));
        } else if (itemStack.func_77973_b() instanceof ItemSwordAccessor) {
            final ItemSwordAccessor itemSword = (ItemSwordAccessor) itemStack.func_77973_b();
            final Item.ToolMaterial swordMaterial = itemSword.accessor$getToolMaterial();
            return Optional.of(new ToolTypeProperty((ToolType) (Object) swordMaterial));
        } else if (itemStack.func_77973_b() instanceof ItemHoeAccessor) {
            final ItemHoeAccessor itemHoe = (ItemHoeAccessor) itemStack.func_77973_b();
            final Item.ToolMaterial hoeMaterial = itemHoe.accessor$getToolMaterial();
            return Optional.of(new ToolTypeProperty((ToolType) (Object) hoeMaterial));
        }
        return Optional.empty();
    }
}
