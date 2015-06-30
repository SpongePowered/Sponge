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
package org.spongepowered.common.data.manipulator.mutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTradeOfferData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;

public class SpongeTradeOfferData extends AbstractData<TradeOfferData, ImmutableTradeOfferData> implements TradeOfferData {

    public static final DataQuery OFFERS = of("Offers");

    private List<TradeOffer> offers = Lists.newArrayList();

    public SpongeTradeOfferData() {
        super(TradeOfferData.class);
    }

    public SpongeTradeOfferData(List<TradeOffer> tradeOffers) {
        super(TradeOfferData.class);
        this.offers.addAll(tradeOffers);
    }

    public List<TradeOffer> getOffers() {
        return this.offers;
    }

    public TradeOfferData setOffers(List<TradeOffer> offers) {
        this.offers.clear();
        for (TradeOffer offer : offers) {
            this.offers.add(checkNotNull(offer));
        }
        return this;
    }

    public TradeOfferData addOffer(TradeOffer offer) {
        this.offers.add(checkNotNull(offer));
        return this;
    }

    @Override
    public TradeOfferData copy() {
        return new SpongeTradeOfferData(this.offers);
    }

    @Override
    public ImmutableTradeOfferData asImmutable() {
        return new ImmutableSpongeTradeOfferData(this.offers);
    }

    @Override
    public int compareTo(TradeOfferData o) {
        return 0; // todo
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(OFFERS, this.offers);
    }

    @Override
    public ListValue<TradeOffer> tradeOffers() {
        return new SpongeListValue<TradeOffer>(Keys.TRADE_OFFERS, this.offers);
    }
}
