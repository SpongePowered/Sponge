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
package org.spongepowered.common.item.merchant;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.village.MerchantRecipe;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Optional;

public class SpongeTradeOfferBuilder extends AbstractDataBuilder<TradeOffer> implements TradeOffer.Builder, DataBuilder<TradeOffer> {

    private ItemStackSnapshot firstItem;
    private ItemStackSnapshot secondItem;
    private ItemStackSnapshot sellingItem;
    private int useCount;
    private int maxUses;
    private boolean allowsExperience;

    public SpongeTradeOfferBuilder() {
        super(TradeOffer.class, 1);
        reset();
    }

    @Override
    public TradeOffer.Builder firstBuyingItem(ItemStack item) {
        checkNotNull(item, "Buying item cannot be null");
        this.firstItem = item.createSnapshot();
        return this;
    }

    @Override
    public TradeOffer.Builder secondBuyingItem(ItemStack item) {
        this.secondItem = item.createSnapshot();
        return this;
    }

    @Override
    public TradeOffer.Builder sellingItem(ItemStack item) {
        checkNotNull(item, "Selling item cannot be null");
        this.sellingItem = item.createSnapshot();
        return this;
    }

    @Override
    public TradeOffer.Builder uses(int uses) {
        checkArgument(uses >= 0, "Usage count cannot be negative");
        this.useCount = uses;
        return this;
    }

    @Override
    public TradeOffer.Builder maxUses(int maxUses) {
        checkArgument(maxUses > 0, "Max usage count must be greater than 0");
        this.maxUses = maxUses;
        return this;
    }

    @Override
    public TradeOffer.Builder canGrantExperience(boolean experience) {
        this.allowsExperience = experience;
        return this;
    }

    @Override
    public TradeOffer build() throws IllegalStateException {
        checkState(this.firstItem != null, "Trading item has not been set");
        checkState(this.sellingItem != null, "Selling item has not been set");
        checkState(this.useCount <= this.maxUses, "Usage count cannot be greater than the max usage count (%s)", this.maxUses);
        final ItemStack first = this.firstItem.createStack();
        final ItemStack second = this.secondItem == null ? null : this.secondItem.createStack();
        final ItemStack selling = this.sellingItem.createStack();
        MerchantRecipe recipe = new MerchantRecipe(ItemStackUtil.toNative(first), ItemStackUtil.toNative(second), ItemStackUtil.toNative(selling),
                        this.useCount, this.maxUses);
        recipe.rewardsExp = this.allowsExperience;
        return (TradeOffer) recipe;
    }

    @Override
    public TradeOffer.Builder from(TradeOffer offer) {
        checkNotNull(offer, "Trade offer cannot be null");
        // Assumes the offer's values don't need to be validated
        this.firstItem = offer.getFirstBuyingItem();
        this.secondItem = offer.getSecondBuyingItem().orElse(null);
        this.sellingItem = offer.getSellingItem();
        this.useCount = offer.getUses();
        this.maxUses = offer.getMaxUses();
        this.allowsExperience = offer.doesGrantExperience();
        return this;
    }

    @Override
    public SpongeTradeOfferBuilder reset() {
        this.firstItem = null;
        this.secondItem = null;
        this.sellingItem = null;
        this.useCount = 0;
        this.maxUses = 7;
        this.allowsExperience = true;
        return this;
    }

    @Override
    protected Optional<TradeOffer> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(
            DataQueries.TradeOffer.FIRST_QUERY,
            DataQueries.TradeOffer.SECOND_QUERY,
            DataQueries.TradeOffer.EXPERIENCE_QUERY,
            DataQueries.TradeOffer.MAX_QUERY,
            DataQueries.TradeOffer.USES_QUERY,
            DataQueries.TradeOffer.BUYING_QUERY)) {
            return Optional.empty();
        }
        final ItemStack firstItem = container.getSerializable(DataQueries.TradeOffer.FIRST_QUERY, ItemStack.class).get();
        final ItemStack buyingItem = container.getSerializable(DataQueries.TradeOffer.BUYING_QUERY, ItemStack.class).get();
        final ItemStack secondItem;
        final boolean secondPresent;
        if (container.getString(DataQueries.TradeOffer.SECOND_QUERY).isPresent() && container.getString(DataQueries.TradeOffer.SECOND_QUERY).get().equals("none")) {
            secondPresent = false;
            secondItem = null;
        } else {
            secondPresent = true;
            secondItem = container.getSerializable(DataQueries.TradeOffer.SECOND_QUERY, ItemStack.class).get();
        }
        TradeOffer.Builder builder = new SpongeTradeOfferBuilder();
        builder.firstBuyingItem(firstItem);
        if (secondPresent) {
            builder.secondBuyingItem(secondItem);
        }
        builder.sellingItem(buyingItem)
                .maxUses(container.getInt(DataQueries.TradeOffer.MAX_QUERY).get())
                .uses(container.getInt(DataQueries.TradeOffer.USES_QUERY).get())
                .canGrantExperience(container.getBoolean(DataQueries.TradeOffer.EXPERIENCE_QUERY).get());
        return Optional.of(builder.build());
    }
}
