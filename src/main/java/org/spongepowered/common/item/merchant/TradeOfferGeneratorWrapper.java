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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;

import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;

public final class TradeOfferGeneratorWrapper implements VillagerTrades.ItemListing, TradeOfferGenerator {

    private final TradeOfferGenerator generator;

    public TradeOfferGeneratorWrapper(final TradeOfferGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @Nullable MerchantOffer getOffer(final Entity trader, final Random rand) {
        return (MerchantOffer) this.generator.apply((org.spongepowered.api.entity.Entity) trader, rand);
    }

    @Override
    public TradeOffer apply(final org.spongepowered.api.entity.Entity entity, final Random random) {
        return this.generator.apply(entity, random);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final TradeOfferGeneratorWrapper that = (TradeOfferGeneratorWrapper) o;
        return Objects.equals(this.generator, that.generator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.generator);
    }

    @Override
    public String toString() {
        return new StringJoiner(
            ", ",
            TradeOfferGeneratorWrapper.class.getSimpleName() + "[",
            "]"
        )
            .add("generator=" + this.generator)
            .toString();
    }
}
