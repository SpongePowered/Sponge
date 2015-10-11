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
package org.spongepowered.common.data.manipulator.immutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeTradeOfferData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;

import java.util.List;

public class ImmutableSpongeTradeOfferData extends AbstractImmutableData<ImmutableTradeOfferData, TradeOfferData> implements ImmutableTradeOfferData {

    private final ImmutableList<TradeOffer> offers;

    public ImmutableSpongeTradeOfferData(List<TradeOffer> offers) {
        super(ImmutableTradeOfferData.class);
        this.offers = ImmutableList.copyOf(checkNotNull(offers));
        registerGetters();
    }

    @Override
    public ImmutableListValue<TradeOffer> tradeOffers() {
        return new ImmutableSpongeListValue<>(Keys.TRADE_OFFERS, this.offers);
    }

    @Override
    public TradeOfferData asMutable() {
        return new SpongeTradeOfferData(this.offers);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.TRADE_OFFERS.getQuery(), this.offers);
    }

    @Override
    public int compareTo(ImmutableTradeOfferData o) {
        return 0;
    }

    @Override
    protected void registerGetters() {
        // TODO
    }
}
