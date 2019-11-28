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
package org.spongepowered.common.mixin.api.mcp.entity.passive;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.MerchantRecipe;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

// Note that these mixins will not have to exist once mixing into interfaces is
// added as the only thing needing to be done is a simple default implementation
// with an empty MerchantRecipeList and diff the list with an empty one and
// provide the resulting diff'ed MerchantRecipe (TradeOffer) as the result.
@Mixin(VillagerEntity.ItemAndEmeraldToItem.class)
public class EntityVillager_ItemAndEmeraldToItemMixin_API implements TradeOfferGenerator {

    @Shadow public ItemStack buyingItemStack;
    @Shadow public VillagerEntity.PriceInfo buyingPriceInfo;
    @Shadow public ItemStack sellingItemstack;
    @Shadow public VillagerEntity.PriceInfo sellingPriceInfo;

    @Override
    public TradeOffer apply(Random random) {
        checkNotNull(random, "Random cannot be null!");
        int buyingCount = 1;

        if (this.buyingPriceInfo != null) {
            buyingCount = this.buyingPriceInfo.func_179412_a(random);
        }

        int sellingCount = 1;

        if (this.sellingPriceInfo != null) {
            sellingCount = this.sellingPriceInfo.func_179412_a(random);
        }

        final ItemStack itemStackBuying = new ItemStack(this.buyingItemStack.func_77973_b(), buyingCount, this.buyingItemStack.func_77960_j());
        final ItemStack emeraldStack = new ItemStack(Items.field_151166_bC);
        final ItemStack itemStackSelling = new ItemStack(this.sellingItemstack.func_77973_b(), sellingCount, this.sellingItemstack.func_77960_j());
        return (TradeOffer) new MerchantRecipe(itemStackBuying, emeraldStack, itemStackSelling);
    }


    @Override
    public void accept(Merchant owner, List<TradeOffer> tradeOffers, Random random) {
        tradeOffers.add(apply(random));
    }
}
