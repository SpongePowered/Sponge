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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ProfessionType;
import org.spongepowered.api.data.type.VillagerType;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.accessor.world.entity.npc.AbstractVillagerAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.stream.Collectors;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class VillagerData {

    private VillagerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Villager.class)
                    .create(Keys.PROFESSION_LEVEL)
                        .get(h -> h.getVillagerData().getLevel())
                        .set((h, v) -> h.setVillagerData(h.getVillagerData().setLevel(v)))
                    .create(Keys.PROFESSION_TYPE)
                        .get(h -> (ProfessionType) h.getVillagerData().getProfession())
                        .set((h, v) -> h.setVillagerData(h.getVillagerData().setProfession((VillagerProfession) v)))
                    .create(Keys.EXPERIENCE)
                        .get(Villager::getVillagerXp)
                        .set(Villager::setVillagerXp)
                    .delete(h -> h.setVillagerXp(0))
                    .create(Keys.EXPERIENCE_LEVEL)
                        .get(h -> h.getVillagerData().getLevel())
                        .setAnd((h, v) -> {
                            if (v < 1) {
                                return false;
                            }
                            h.setVillagerData(h.getVillagerData().setLevel(v));
                            return true;
                        })
                    .create(Keys.TRADE_OFFERS)
                        .get(h -> h.getOffers().stream().map(TradeOffer.class::cast).collect(Collectors.toList()))
                        .set((h, v) -> h.setOffers(v.stream().map(MerchantOffer.class::cast).collect(Collectors.toCollection(MerchantOffers::new))))
                    .create(Keys.VILLAGER_TYPE)
                        .get(h -> (VillagerType) (Object) h.getVillagerData().getType())
                        .set((h, v) -> h.setVillagerData(h.getVillagerData().setType((net.minecraft.world.entity.npc.VillagerType) (Object) v)))
                .asMutable(WanderingTrader.class)
                    .create(Keys.TRADE_OFFERS)
                    .get(h -> h.getOffers().stream().map(TradeOffer.class::cast).collect(Collectors.toList()))
                    .set((h, v) -> ((AbstractVillagerAccessor)h).accessor$offers(v.stream().map(MerchantOffer.class::cast).collect(Collectors.toCollection(MerchantOffers::new))));
    }
    // @formatter:on
}
