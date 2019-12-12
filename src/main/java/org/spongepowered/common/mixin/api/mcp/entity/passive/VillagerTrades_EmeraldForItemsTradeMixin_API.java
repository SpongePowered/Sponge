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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
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
//
// i509VCB: Yes the classes which implement ITrade, like ItemsForEmeraldsAndItemsTrade are package private
// For clarification this trade is multiple Items -> 1 Emerald
// TODO: These need a new home
@Mixin(targets = "net/minecraft/entity/merchant/villager/VillagerTrades$EmeraldForItemsTrade")
public class VillagerTrades_EmeraldForItemsTradeMixin_API implements TradeOfferGenerator {

    @Shadow public Item tradeItem;
    @Shadow public int count; // Amount of item required to receive an emerald
    @Shadow public int maxUses;
    @Shadow public int xpValue;
    @Shadow public float priceMultiplier;

    @Override
    public TradeOffer apply(Random random) {
        checkNotNull(random, "Random cannot be null!");

        final ItemStack buyingItem = new ItemStack(this.tradeItem, count);
        return (TradeOffer) new MerchantOffer(buyingItem, new ItemStack(Items.EMERALD), maxUses, xpValue, priceMultiplier);
    }

    @Override
    public void accept(Merchant owner, List<TradeOffer> tradeOffers, Random random) {
        tradeOffers.add(this.apply(random));
    }
}
