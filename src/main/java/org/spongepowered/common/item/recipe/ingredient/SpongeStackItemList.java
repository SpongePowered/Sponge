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
package org.spongepowered.common.item.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

public class SpongeStackItemList extends SpongeItemList {

    public static final String TYPE_STACK = "sponge:stack";

    public SpongeStackItemList(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public JsonObject serialize() {
        final JsonObject jsonobject = super.serialize();
        jsonobject.addProperty(SpongeItemList.INGREDIENT_TYPE, SpongeStackItemList.TYPE_STACK);
        return jsonobject;
    }

    @Override
    public boolean test(ItemStack testStack) {
        if (this.stacks.length == 0) {
            return testStack.isEmpty();
        }

        for (ItemStack itemstack : this.stacks) {
            if (itemstack.getItem() == testStack.getItem()) {
                if (ItemStack.matches(itemstack, testStack)) {
                    return true;
                }
            }
        }

        return false;
    }
}
