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
package org.spongepowered.common.mixin.core.entity.merchant.villager;

import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.living.trader.Villager;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.registry.SpongeVillagerRegistry;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends AbstractVillagerEntityMixin {

    @Shadow public abstract VillagerData shadow$getVillagerData();

    private List<MerchantOffer> impl$newOffers = new ArrayList<>();

    /**
     * @author i509VCB - February 21st, 2020 - 1.14.4
     * @reason Here we apply the Trade Mutators to the villager's new merchant offers and then add them to the entity.
     *
     * @param givenMerchantOffers The merchant's current merchant offers.
     * @param newTrades The trade factories representing the new trades this villager will receive.
     * @param maxNumbers The maximum amount of trades for this villager to receive.
     * @param ci CallbackInfo.
     */
    @Override
    protected void impl$addAndApplyTradeMutators(final MerchantOffers givenMerchantOffers,
        final VillagerTrades.ITrade[] newTrades, final int maxNumbers, final CallbackInfo ci) {
        SpongeVillagerRegistry.getInstance().populateOffers((Villager) this,
            this.impl$newOffers,
            this.shadow$getVillagerData(),
            this.rand);
        givenMerchantOffers.addAll(this.impl$newOffers); // Finally add the mutated offers to the trade offer map.
        this.impl$newOffers.clear(); // And clean up our temp values
    }

    /**
     * @author i509VCB - February 21st, 2020 - 1.14.4
     * @reason Override the redirect call in AbstractVillagerEntityMixin to capture merchant offers to be mutated.
     *
     * @param merchantOffers The current offers the villager has.
     * @param offer The merchant offer to add.
     * @return true
     */
    @Override
    protected boolean impl$addNewOfferToTempMap(final MerchantOffers merchantOffers, final MerchantOffer offer) {
        return this.impl$newOffers.add(offer);
    }
}
