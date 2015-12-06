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
package org.spongepowered.common.data.processor.value.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradeOffersValueProcessor extends AbstractSpongeValueProcessor<EntityVillager, List<TradeOffer>, ListValue<TradeOffer>> {

    public TradeOffersValueProcessor() {
        super(EntityVillager.class, Keys.TRADE_OFFERS);
    }

    @Override
    protected ListValue<TradeOffer> constructValue(List<TradeOffer> defaultValue) {
        return new SpongeListValue<>(Keys.TRADE_OFFERS, defaultValue);
    }

    @Override
    protected boolean set(EntityVillager container, List<TradeOffer> value) {
        MerchantRecipeList list = new MerchantRecipeList();
        list.addAll(value.stream().map(tradeOffer -> (MerchantRecipe) tradeOffer).collect(Collectors.toList()));
        container.buyingList = list;
        return true;
    }

    @Override
    protected Optional<List<TradeOffer>> getVal(EntityVillager container) {
        List<TradeOffer> offers = Lists.newArrayList();
        if(container.buyingList == null) {
            container.populateBuyingList();
        }
        for (int i = 0; i < container.buyingList.size(); i++) {
            offers.add((TradeOffer) container.buyingList.get(i));
        }
        return Optional.of(offers);
    }

    @Override
    protected ImmutableValue<List<TradeOffer>> constructImmutableValue(List<TradeOffer> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
