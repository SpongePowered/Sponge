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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.manipulator.mutable.SpongeTradeOfferData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.List;
import java.util.Optional;

public class TradeOfferDataProcessor
        extends AbstractEntitySingleDataProcessor<EntityVillager, List<TradeOffer>, TradeOfferData, ImmutableTradeOfferData> {

    public TradeOfferDataProcessor() {
        super(EntityVillager.class, Keys.TRADE_OFFERS);
    }

    public static List<TradeOffer> toTradeOfferList(MerchantRecipeList list) {
        List<TradeOffer> offers = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            offers.add((TradeOffer) list.get(i));
        }
        return offers;
    }

    public static MerchantRecipeList toMerchantRecipeList(List<TradeOffer> offers) {
        MerchantRecipeList list = new MerchantRecipeList();
        for (TradeOffer offer : offers) {
            list.add((MerchantRecipe) offer);
        }
        return list;
    }

    @Override
    protected Value.Mutable<List<TradeOffer>> constructMutableValue(List<TradeOffer> actualValue) {
        return SpongeValueFactory.getInstance().createListValue(Keys.TRADE_OFFERS, actualValue);
    }

    @Override
    protected boolean set(EntityVillager entity, List<TradeOffer> value) {
        entity.buyingList = toMerchantRecipeList(value);
        return true;
    }

    @Override
    protected Optional<List<TradeOffer>> getVal(EntityVillager entity) {
        if (entity.buyingList == null) {
            entity.populateBuyingList();
        }
        return Optional.of(toTradeOfferList(entity.buyingList));
    }

    @Override
    protected Value.Immutable<List<TradeOffer>> constructImmutableValue(List<TradeOffer> value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    protected TradeOfferData createManipulator() {
        return new SpongeTradeOfferData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
