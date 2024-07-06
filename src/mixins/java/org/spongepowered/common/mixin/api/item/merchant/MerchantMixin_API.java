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
package org.spongepowered.common.mixin.api.item.merchant;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;

@Mixin(value = Merchant.class)
@Implements(@Interface(iface = net.minecraft.world.item.trading.Merchant.class, prefix = "imerchant$"))
public interface MerchantMixin_API extends Merchant {

    @Nullable
    default Player imerchant$getTradingPlayer() {
        return (Player) this.customer()
            .filter(humanoid -> humanoid instanceof Player)
            .orElse(null);
    }

    @Nullable
    default MerchantOffers imerchant$getOffers() {
        final MerchantOffers merchantRecipes = new MerchantOffers();
        for (final TradeOffer tradeOffer : this.get(Keys.TRADE_OFFERS).orElse(Collections.emptyList())) {
            merchantRecipes.add((MerchantOffer) tradeOffer);
        }
        return merchantRecipes;
    }

    default void imerchant$notifyTrade(final MerchantOffer recipe) {

    }

    default void imerchant$notifyTradeUpdated(final ItemStack stack) {

    }
}
