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
package org.spongepowered.common.item.recipe.crafting;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.recipe.crafting.Ingredient;

public class SpongeIngredientBuilder implements Ingredient.Builder {

    private ItemType[] types;
    private CatalogKey itemTag;

    @Override
    public Ingredient.Builder reset() {
        this.types = null;
        this.itemTag = null;
        return this;
    }

    @Override
    public Ingredient.Builder with(ItemType... types) {
        this.types = types;
        this.itemTag = null;
        return this;
    }

    @Override
    public Ingredient.Builder with(CatalogKey itemTag) {
        this.itemTag = itemTag;
        this.types = null;
        return this;
    }

    @Override
    public Ingredient build() {
        if (this.itemTag != null) {
            return IngredientUtil.of(this.itemTag);
        }
        if (this.types != null && this.types.length > 0) {
            return IngredientUtil.of(this.types);
        }
        throw new IllegalStateException("An ingredient must have at least one ItemType or an item tag");
    }
}
