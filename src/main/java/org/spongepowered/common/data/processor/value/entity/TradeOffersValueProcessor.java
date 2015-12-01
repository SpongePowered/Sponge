package org.spongepowered.common.data.processor.value.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradeOffersValueProcessor extends AbstractSpongeValueProcessor<EntityVillager, List<TradeOffer>, ListValue<TradeOffer>> {

    public TradeOffersValueProcessor() {
        super(EntityVillager.class, Keys.TRADE_OFFERS);
    }

    @Override
    protected ListValue<TradeOffer> constructValue(List<TradeOffer> defaultValue) {
        return new SpongeListValue<>(Keys.TRADE_OFFERS, defaultValue);
    }

    @Override
    protected boolean set(EntityVillager container, List<TradeOffer> value) {
        MerchantRecipeList list = new MerchantRecipeList();
        list.addAll(value.stream().map(tradeOffer -> (MerchantRecipe) tradeOffer).collect(Collectors.toList()));
        container.buyingList = list;
        return true;
    }

    @Override
    protected Optional<List<TradeOffer>> getVal(EntityVillager container) {
        List<TradeOffer> offers = Lists.newArrayList();
        for (int i = 0; i < container.buyingList.size(); i++) {
            offers.add((TradeOffer) container.buyingList.get(i));
        }
        return Optional.of(offers);
    }

    @Override
    protected ImmutableValue<List<TradeOffer>> constructImmutableValue(List<TradeOffer> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
