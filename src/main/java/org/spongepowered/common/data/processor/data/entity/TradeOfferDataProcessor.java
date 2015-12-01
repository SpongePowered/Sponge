package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.manipulator.mutable.SpongeTradeOfferData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradeOfferDataProcessor extends AbstractEntitySingleDataProcessor<EntityVillager, List<TradeOffer>, ListValue<TradeOffer>,
        TradeOfferData, ImmutableTradeOfferData> {

    public TradeOfferDataProcessor() {
        super(EntityVillager.class, Keys.TRADE_OFFERS);
    }

    @Override
    protected boolean set(EntityVillager entity, List<TradeOffer> value) {
        MerchantRecipeList list = new MerchantRecipeList();
        list.addAll(value.stream().map(tradeOffer -> (MerchantRecipe) tradeOffer).collect(Collectors.toList()));
        entity.buyingList = list;
        return true;
    }

    @Override
    protected Optional<List<TradeOffer>> getVal(EntityVillager entity) {
        List<TradeOffer> offers = Lists.newArrayList();
        for (int i = 0; i < entity.buyingList.size(); i++) {
            offers.add((TradeOffer) entity.buyingList.get(i));
        }
        return Optional.of(offers);
    }

    @Override
    protected ImmutableValue<List<TradeOffer>> constructImmutableValue(List<TradeOffer> value) {
        return new ImmutableSpongeListValue<>(Keys.TRADE_OFFERS, ImmutableList.copyOf(value));
    }

    @Override
    protected TradeOfferData createManipulator() {
        return new SpongeTradeOfferData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}
