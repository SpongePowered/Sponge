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
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.manipulator.mutable.SpongeTradeOfferData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.entity.passive.EntityVillagerAccessor;

import java.util.List;
import java.util.Optional;

public class TradeOfferDataProcessor
        extends AbstractEntitySingleDataProcessor<VillagerEntity, List<TradeOffer>, ListValue<TradeOffer>, TradeOfferData, ImmutableTradeOfferData> {

    public TradeOfferDataProcessor() {
        super(VillagerEntity.class, Keys.TRADE_OFFERS);
    }

    private static List<TradeOffer> toTradeOfferList(final MerchantRecipeList list) {
        final List<TradeOffer> offers = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            offers.add((TradeOffer) list.get(i));
        }
        return offers;
    }

    private static MerchantRecipeList toMerchantRecipeList(final List<TradeOffer> offers) {
        final MerchantRecipeList list = new MerchantRecipeList();
        for (final TradeOffer offer : offers) {
            list.add((MerchantRecipe) offer);
        }
        return list;
    }

    @Override
    protected ListValue<TradeOffer> constructValue(final List<TradeOffer> actualValue) {
        return SpongeValueFactory.getInstance().createListValue(Keys.TRADE_OFFERS, actualValue);
    }

    @Override
    protected boolean set(final VillagerEntity entity, final List<TradeOffer> value) {
        ((EntityVillagerAccessor) entity).accessor$setBuyingList(toMerchantRecipeList(value));
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected Optional<List<TradeOffer>> getVal(final VillagerEntity entity) {
        final MerchantRecipeList recipes = ((EntityVillagerAccessor) entity).accessor$getBuyingList();
        if (recipes == null) {
            ((EntityVillagerAccessor) entity).accessor$PopulateBuyingList();
        }
        return Optional.of(toTradeOfferList(((EntityVillagerAccessor) entity).accessor$getBuyingList()));
    }

    @Override
    protected ImmutableValue<List<TradeOffer>> constructImmutableValue(final List<TradeOffer> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected TradeOfferData createManipulator() {
        return new SpongeTradeOfferData();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
