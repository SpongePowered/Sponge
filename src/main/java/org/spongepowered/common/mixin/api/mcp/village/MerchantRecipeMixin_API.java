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
package org.spongepowered.common.mixin.api.mcp.village;

import net.minecraft.village.MerchantRecipe;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(MerchantRecipe.class)
public abstract class MerchantRecipeMixin_API implements TradeOffer {

    @Shadow public abstract net.minecraft.item.ItemStack getItemToBuy();
    @Shadow public abstract boolean hasSecondItemToBuy();
    @Shadow @Nullable public abstract net.minecraft.item.ItemStack getSecondItemToBuy();
    @Shadow public abstract net.minecraft.item.ItemStack getItemToSell();
    @Shadow public abstract int getToolUses();
    @Shadow public abstract int getMaxTradeUses();
    @Shadow public abstract boolean isRecipeDisabled();
    @Shadow public abstract boolean getRewardsExp();

    @Override
    public ItemStackSnapshot getFirstBuyingItem() {
        return ((ItemStack) getItemToBuy()).createSnapshot();
    }

    @Override
    public boolean hasSecondItem() {
        return hasSecondItemToBuy();
    }

    @Override
    public Optional<ItemStackSnapshot> getSecondBuyingItem() {
        if (getSecondItemToBuy() == null) {
            return Optional.empty();
        }
        return Optional.of(((ItemStack) getSecondItemToBuy()).createSnapshot());
    }

    @Override
    public ItemStackSnapshot getSellingItem() {
        return ((ItemStack) getItemToSell()).createSnapshot();
    }

    @Override
    public int getUses() {
        return getToolUses();
    }

    @Override
    public int getMaxUses() {
        return getMaxTradeUses();
    }

    @Override
    public boolean hasExpired() {
        return isRecipeDisabled();
    }

    @Override
    public boolean doesGrantExperience() {
        return getRewardsExp();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(Constants.Item.TradeOffer.FIRST_QUERY, this.getFirstBuyingItem())
                .set(Constants.Item.TradeOffer.SECOND_QUERY, this.hasSecondItem() ? this.getSecondBuyingItem().get() : "none")
                .set(Constants.Item.TradeOffer.BUYING_QUERY, this.getItemToBuy())
                .set(Constants.Item.TradeOffer.EXPERIENCE_QUERY, this.doesGrantExperience())
                .set(Constants.Item.TradeOffer.MAX_QUERY, this.getMaxTradeUses())
                .set(Constants.Item.TradeOffer.USES_QUERY, this.getUses());
    }

}
