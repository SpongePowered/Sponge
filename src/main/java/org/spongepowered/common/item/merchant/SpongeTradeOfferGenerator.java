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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Random;

import javax.annotation.Nullable;

public final class SpongeTradeOfferGenerator implements TradeOfferGenerator {

    public static org.spongepowered.common.item.merchant.SpongeTradeOfferGenerator.Builder builder() {
        return new org.spongepowered.common.item.merchant.SpongeTradeOfferGenerator.Builder();
    }

    final ItemStackGenerator firstItemGenerator;
    final ItemStackGenerator sellingItemGenerator;
    @Nullable final ItemStackGenerator secondItemGenerator;
    final double experience;
    final VariableAmount baseUses;
    final VariableAmount maxUses;

    SpongeTradeOfferGenerator(org.spongepowered.common.item.merchant.SpongeTradeOfferGenerator.Builder builder) {
        this.firstItemGenerator = builder.firstGenerator;
        this.secondItemGenerator = builder.secondGenerator;
        this.sellingItemGenerator = builder.sellingGenerator;
        this.experience = builder.experience;
        this.baseUses = builder.baseUses;
        this.maxUses = builder.maxUses;
    }

    @Override
    public TradeOffer apply(Random random) {
        checkNotNull(random, "Random cannot be null!");
        final TradeOffer.Builder builder = TradeOffer.builder();
        builder.firstBuyingItem(this.firstItemGenerator.apply(random));
        if (this.secondItemGenerator != null) {
            builder.secondBuyingItem(this.secondItemGenerator.apply(random));
        }
        builder.sellingItem(this.sellingItemGenerator.apply(random));
        builder.canGrantExperience(random.nextDouble() < this.experience);
        builder.uses(this.baseUses.getFlooredAmount(random));
        builder.maxUses(this.maxUses.getFlooredAmount(random));
        return builder.build();
    }

    // basically, should be able to just prattle on with BiConsumers
    public static final class Builder implements TradeOfferGenerator.Builder {

        ItemStackGenerator firstGenerator;
        @Nullable ItemStackGenerator secondGenerator;
        ItemStackGenerator sellingGenerator;
        double experience;
        VariableAmount baseUses;
        VariableAmount maxUses;

        @Override
        public TradeOfferGenerator.Builder setPrimaryItemGenerator(ItemStackGenerator generator) {
            this.firstGenerator = checkNotNull(generator, "ItemStackGenerator cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder setSecondItemGenerator(@Nullable ItemStackGenerator generator) {
            this.secondGenerator = generator;
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder setSellingGenerator(ItemStackGenerator sellingGenerator) {
            this.sellingGenerator = checkNotNull(sellingGenerator, "ItemStackGenerator cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder experienceChance(double experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder startingUses(VariableAmount amount) {
            this.baseUses = checkNotNull(amount, "Variable amount cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder maxUses(VariableAmount amount) {
            this.maxUses = checkNotNull(amount, "Variable amount cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator build() {
            checkState(this.firstGenerator != null, "First item populators cannot be empty! Populate with some BiConsumers!");
            checkState(this.sellingGenerator != null, "Selling item populators cannot be empty! Populate with some BiConsumers!");
            checkState(this.baseUses != null);
            checkState(this.maxUses != null);
            return new SpongeTradeOfferGenerator(this);
        }

        @Override
        public TradeOfferGenerator.Builder from(TradeOfferGenerator value) {
            reset();
            if (value instanceof SpongeTradeOfferGenerator) {
                final SpongeTradeOfferGenerator generator = (SpongeTradeOfferGenerator) value;
                this.firstGenerator = generator.firstItemGenerator;
                this.secondGenerator = generator.secondItemGenerator;
                this.sellingGenerator = generator.sellingItemGenerator;
                this.experience = generator.experience;
                this.baseUses = generator.baseUses;
                this.maxUses = generator.maxUses;
                return this;
            }
            throw new IllegalArgumentException("The provided TradeOfferGenerator is incompatible with the current implementation!");
        }

        @Override
        public TradeOfferGenerator.Builder reset() {
            this.firstGenerator = null;
            this.secondGenerator = null;
            this.sellingGenerator = null;
            this.experience = 0.5D;
            this.baseUses = null;
            this.maxUses = null;
            return this;
        }
    }

}
