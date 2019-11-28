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

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.property.item.DamageAbsorptionProperty;
import org.spongepowered.common.data.property.store.common.AbstractItemStackPropertyStore;

import java.util.Optional;

public class DamageAbsorptionPropertyStore extends AbstractItemStackPropertyStore<DamageAbsorptionProperty> {

    @Override
    protected Optional<DamageAbsorptionProperty> getFor(ItemStack itemStack) {
        if (itemStack.func_77973_b() instanceof ArmorItem) {
            final ArmorItem armor = (ArmorItem) itemStack.func_77973_b();
            final int reduction = armor.field_77879_b;
            return Optional.of(new DamageAbsorptionProperty(reduction));
        }
        return Optional.empty();
    }
}
