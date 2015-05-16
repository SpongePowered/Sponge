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
package org.spongepowered.common.data.utils.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.TradeOfferData;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulators.SpongeTradeOfferData;
import org.spongepowered.common.item.merchant.SpongeTradeOfferBuilder;

public class SpongeTradeOfferProcessor implements SpongeDataProcessor<TradeOfferData> {

    @Override
    public Optional<TradeOfferData> fillData(DataHolder holder, TradeOfferData manipulator, DataPriority priority) {
        return Optional.absent(); // todo
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTransactionResult setData(DataHolder dataHolder, TradeOfferData manipulator, DataPriority priority) {
        checkNotNull(manipulator);
        checkNotNull(priority);
        if (checkNotNull(dataHolder) instanceof EntityVillager) {
            final MerchantRecipeList recipeList = ((EntityVillager) dataHolder).getRecipes(null);
            recipeList.clear();
            for (TradeOffer offer : manipulator.getOffers()) {
                recipeList.add((Object) new SpongeTradeOfferBuilder().from(offer).build());
            }
            return successNoData(); // todo
        }
        return successNoData(); // todo
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<TradeOfferData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public TradeOfferData create() {
        return new SpongeTradeOfferData();
    }

    @Override
    public Optional<TradeOfferData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityVillager) {
            final MerchantRecipeList currentList = ((EntityVillager) dataHolder).getRecipes(null);
            TradeOfferData data = create();
            for (Object recipe : currentList) {
                data.addOffer(new SpongeTradeOfferBuilder().from((TradeOffer) recipe).build());
            }
            return Optional.of(data);
        }
        return Optional.absent();
    }

    @Override
    public Optional<TradeOfferData> getFrom(DataHolder holder) {
        return Optional.absent();
    }
}
