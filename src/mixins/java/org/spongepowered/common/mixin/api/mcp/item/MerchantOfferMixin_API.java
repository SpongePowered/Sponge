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
package org.spongepowered.common.mixin.api.mcp.item;

import net.minecraft.item.MerchantOffer;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(MerchantOffer.class)
@Implements(@Interface(iface = TradeOffer.class, prefix = "tradeOffer$"))
public abstract class MerchantOfferMixin_API implements TradeOffer {

    @Shadow public abstract net.minecraft.item.ItemStack shadow$getBuyingStackFirst();
    @Shadow @Nullable public abstract net.minecraft.item.ItemStack shadow$getBuyingStackSecond();
    @Shadow public abstract net.minecraft.item.ItemStack shadow$getSellingStack();
    @Shadow public abstract int shadow$getUses();
    @Shadow public abstract int shadow$func_222214_i();
    @Shadow public abstract boolean shadow$hasNoUsesLeft();
    @Shadow public abstract boolean shadow$getDoesRewardExp();
    @Shadow public abstract int shadow$getGivenExp();
    @Shadow public abstract float shadow$getPriceMultiplier();

    @Shadow public abstract int shadow$getDemand();

    @Override
    public ItemStackSnapshot getFirstBuyingItem() {
        return ((ItemStack) (Object) this.shadow$getBuyingStackFirst()).createSnapshot();
    }

    @Override
    public boolean hasSecondItem() {
        return this.shadow$getBuyingStackSecond() != net.minecraft.item.ItemStack.EMPTY;
    }

    @Override
    public Optional<ItemStackSnapshot> getSecondBuyingItem() {
        if (this.shadow$getBuyingStackSecond() == null) {
            return Optional.empty();
        }
        return Optional.of(((ItemStack) (Object) this.shadow$getBuyingStackSecond()).createSnapshot());
    }

    @Override
    public ItemStackSnapshot getSellingItem() {
        return ((ItemStack) (Object) this.shadow$getSellingStack()).createSnapshot();
    }

    @Intrinsic
    public int tradeOffer$getUses() {
        return this.shadow$getUses();
    }

    @Override
    public int getMaxUses() {
        return this.shadow$func_222214_i();
    }

    @Override
    public boolean hasExpired() {
        return this.shadow$hasNoUsesLeft();
    }

    @Override
    public boolean doesGrantExperience() {
        return this.shadow$getDoesRewardExp();
    }

    @Override
    public int getExperienceGrantedToMerchant() {
        return this.shadow$getGivenExp();
    }

    @Override
    public double getPriceGrowthMultiplier() {
        return this.shadow$getPriceMultiplier();
    }

    @Override
    public int getDemandBonus() {
        return this.shadow$getDemand();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.Item.TradeOffer.FIRST_QUERY, this.getFirstBuyingItem())
            .set(Constants.Item.TradeOffer.SECOND_QUERY, this.hasSecondItem() ? this.getSecondBuyingItem().get() : "none")
            .set(Constants.Item.TradeOffer.BUYING_QUERY, this.shadow$getBuyingStackFirst())
            .set(Constants.Item.TradeOffer.EXPERIENCE_QUERY, this.doesGrantExperience())
            .set(Constants.Item.TradeOffer.MAX_QUERY, this.shadow$func_222214_i())
            .set(Constants.Item.TradeOffer.USES_QUERY, this.getUses())
            .set(Constants.Item.TradeOffer.EXPERIENCE_GRANTED_TO_MERCHANT_QUERY, this.shadow$getGivenExp())
            .set(Constants.Item.TradeOffer.PRICE_GROWTH_MULTIPLIER_QUERY, this.shadow$getPriceMultiplier())
            .set(Constants.Item.TradeOffer.DEMAND_BONUS_QUERY, this.shadow$getDemand());
    }

}
