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


import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.api.util.RandomProvider;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.common.util.Preconditions;

import java.util.Objects;

public final class SpongeTradeOfferGenerator implements TradeOfferGenerator {

    public static SpongeTradeOfferGenerator.Builder builder() {
        return new SpongeTradeOfferGenerator.Builder();
    }

    final ItemStackGenerator firstItemGenerator;
    final ItemStackGenerator sellingItemGenerator;
    final @Nullable ItemStackGenerator secondItemGenerator;
    final double experience;
    final VariableAmount baseUses;
    final VariableAmount maxUses;
    final VariableAmount grantedExperience;

    SpongeTradeOfferGenerator(final SpongeTradeOfferGenerator.Builder builder) {
        this.firstItemGenerator = builder.firstGenerator;
        this.secondItemGenerator = builder.secondGenerator;
        this.sellingItemGenerator = builder.sellingGenerator;
        this.experience = builder.experience;
        this.baseUses = builder.baseUses;
        this.maxUses = builder.maxUses;
        this.grantedExperience = builder.grantedExperience == null ? VariableAmount.fixed(0) : builder.grantedExperience;
    }

    @Override
    public TradeOffer apply(final Entity merchant, final RandomProvider.Source random) {
        Objects.requireNonNull(random, "Random cannot be null!");
        final TradeOffer.Builder builder = TradeOffer.builder();
        builder.firstBuyingItem(this.firstItemGenerator.apply(random));
        if (this.secondItemGenerator != null) {
            builder.secondBuyingItem(this.secondItemGenerator.apply(random));
        }
        builder.sellingItem(this.sellingItemGenerator.apply(random));
        if (random.nextDouble() < this.experience) {
            builder.merchantExperienceGranted(this.grantedExperience.flooredAmount(random));
        }

        builder.uses(this.baseUses.flooredAmount(random));
        builder.maxUses(this.maxUses.flooredAmount(random));
        return builder.build();
    }

    // basically, should be able to just prattle on with BiConsumers
    public static final class Builder implements TradeOfferGenerator.Builder {

        @MonotonicNonNull ItemStackGenerator firstGenerator;
        @Nullable ItemStackGenerator secondGenerator;
        @MonotonicNonNull ItemStackGenerator sellingGenerator;
        double experience;
        @MonotonicNonNull VariableAmount baseUses;
        @MonotonicNonNull VariableAmount maxUses;
        @Nullable VariableAmount grantedExperience;

        @Override
        public TradeOfferGenerator.Builder firstBuyingItemGenerator(final ItemStackGenerator generator) {
            this.firstGenerator = Objects.requireNonNull(generator, "ItemStackGenerator cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder secondBuyingItemGenerator(final @Nullable ItemStackGenerator generator) {
            this.secondGenerator = generator;
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder sellingItemGenerator(final ItemStackGenerator sellingGenerator) {
            this.sellingGenerator = Objects.requireNonNull(sellingGenerator, "ItemStackGenerator cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder experienceChance(final double experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder grantedExperience(VariableAmount amount) {
            this.grantedExperience = Objects.requireNonNull(amount, "Granted experience cannot be null");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder startingUses(final VariableAmount amount) {
            this.baseUses = Objects.requireNonNull(amount, "Variable amount cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator.Builder maxUses(final VariableAmount amount) {
            this.maxUses = Objects.requireNonNull(amount, "Variable amount cannot be null!");
            return this;
        }

        @Override
        public TradeOfferGenerator build() {
            Preconditions.checkState(this.firstGenerator != null, "First item populators cannot be empty! Populate with some BiConsumers!");
            Preconditions.checkState(this.sellingGenerator != null, "Selling item populators cannot be empty! Populate with some BiConsumers!");
            Preconditions.checkState(this.baseUses != null);
            Preconditions.checkState(this.maxUses != null);
            return new SpongeTradeOfferGenerator(this);
        }

        @Override
        public TradeOfferGenerator.Builder from(final TradeOfferGenerator value) {
            this.reset();
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
            this.grantedExperience = null;
            return this;
        }
    }

}
