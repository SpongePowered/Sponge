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

import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.living.trader.Villager;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.core.entity.AgeableEntityMixin;
import org.spongepowered.common.registry.SpongeVillagerRegistry;

import java.util.List;

@Mixin(AbstractVillagerEntity.class)
public abstract class AbstractVillagerEntityMixin extends AgeableEntityMixin {
    /**
     * @author i509VCB - February 21st, 2020 - 1.14.4
     * @reason In order to apply the Trade mutators, we need a way to intercept the merchant offers. In VillagerEntityMixin we override this redirect to capture all the merchant offers being added to the merchant.
     * This does the exact same as vanilla but the VillagerEntity and WanderingTraderEntity would override this to implement their own logic.
     *
     * @param merchantOffers The current offers the villager has.
     * @param offer The merchant offer to add.
     * @return true.
     */
    @Redirect(method = "addTrades(Lnet/minecraft/item/MerchantOffers;[Lnet/minecraft/entity/merchant/villager/VillagerTrades$ITrade;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MerchantOffers;add(Lnet/minecraft/item/MerchantOffer;)Z")
    )
    protected boolean impl$addNewOfferToTempMap(MerchantOffers merchantOffers, MerchantOffer offer) {
        return merchantOffers.add(offer);
    }

    /**
     * @author i509VCB - February 21st, 2020 - 1.14.4
     * @reason At TAIL, all merchant offers have been selected. Implementations of this method would process trade mutators and then add to the givenMerchantOffers.
     *
     * @param givenMerchantOffers
     * @param newTrades
     * @param maxNumbers
     * @param ci
     */
    @Inject(method = "addTrades(Lnet/minecraft/item/MerchantOffers;[Lnet/minecraft/entity/merchant/villager/VillagerTrades$ITrade;I)V",
        at = @At("TAIL"))
    protected void impl$addAndApplyTradeMutators(MerchantOffers givenMerchantOffers, VillagerTrades.ITrade[] newTrades, int maxNumbers, CallbackInfo ci) {
        // Do nothing, this is overriden by the Villager/WanderingTraderEntity
    }

}
