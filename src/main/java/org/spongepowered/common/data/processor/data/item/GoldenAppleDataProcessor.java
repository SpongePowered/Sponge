package org.spongepowered.common.data.processor.data.item;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.processor.common.GoldenAppleUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.item.SpongeGoldenApple;

public class GoldenAppleDataProcessor extends AbstractItemSingleDataProcessor<GoldenApple, Value<GoldenApple>, GoldenAppleData, ImmutableGoldenAppleData> {

    public GoldenAppleDataProcessor() {
        super(new Predicate<ItemStack>() {

            @Override
            public boolean apply(ItemStack input) {
                return input.getItem().equals(Items.golden_apple);
            }
        }, Keys.GOLDEN_APPLE_TYPE);
    }

    @Override
    protected GoldenAppleData createManipulator() {
        return new SpongeGoldenAppleData();
    }

    @Override
    protected boolean set(ItemStack itemStack, GoldenApple value) {
        GoldenAppleUtils.setType(itemStack, value);
        return true;
    }

    @Override
    protected Optional<GoldenApple> getVal(ItemStack itemStack) {
        return Optional.of(GoldenAppleUtils.getType(itemStack));
    }

    @Override
    protected ImmutableValue<GoldenApple> constructImmutableValue(GoldenApple value) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.GOLDEN_APPLE_TYPE, GoldenApples.GOLDEN_APPLE, value);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}
