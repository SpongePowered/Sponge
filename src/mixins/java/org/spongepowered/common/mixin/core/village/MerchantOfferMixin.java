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
package org.spongepowered.common.mixin.core.village;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nullable;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin {

    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$getBaseCostA();
    @Shadow @Nullable public abstract net.minecraft.world.item.ItemStack shadow$getCostB();
    @Shadow public abstract net.minecraft.world.item.ItemStack shadow$getResult();
    @Shadow public abstract int shadow$getUses();
    @Shadow public abstract int shadow$getMaxUses();
    @Shadow public abstract int shadow$getXp();

    // This is a little questionable, since we're mixing into a Minecraft class.
    // However, Vanilla doesn't override equals(), so no one except plugins
    // should be calling it,
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MerchantOffer other = (MerchantOffer) o;
        return ItemStackComparators.ALL.get().compare((ItemStack) (Object) this.shadow$getBaseCostA(), (ItemStack) (Object) other.getBaseCostA()) == 0
            && ItemStackComparators.ALL.get().compare((ItemStack) (Object) this.shadow$getCostB(), (ItemStack) (Object) other.getCostB()) == 0
            && ItemStackComparators.ALL.get().compare((ItemStack) (Object) this.shadow$getResult(), (ItemStack) (Object) other.getResult()) == 0
            && this.shadow$getUses() == other.getUses()
            && this.shadow$getMaxUses() == other.getMaxUses()
            && this.shadow$getXp() == other.getXp();
    }
}
