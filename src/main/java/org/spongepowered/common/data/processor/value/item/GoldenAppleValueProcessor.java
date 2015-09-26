package org.spongepowered.common.data.processor.value.item;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.GoldenAppleUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.item.SpongeGoldenApple;

public class GoldenAppleValueProcessor extends AbstractSpongeValueProcessor<GoldenApple, Value<GoldenApple>> {

    public GoldenAppleValueProcessor() {
        super(Keys.GOLDEN_APPLE_TYPE);
    }

    @Override
    protected Value<GoldenApple> constructValue(GoldenApple defaultValue) {
        return new SpongeValue<GoldenApple>(Keys.GOLDEN_APPLE_TYPE, defaultValue);
    }

    @Override
    public Optional<GoldenApple> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            return Optional.of(GoldenAppleUtils.getType((ItemStack) container));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof ItemStack && ((ItemStack) container).getItem().equals(Items.golden_apple);
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, GoldenApple value) {
        if (this.supports(container)) {
            GoldenApple old = this.getValueFromContainer(container).get();
            final ImmutableValue<GoldenApple> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.GOLDEN_APPLE_TYPE, old,
                    GoldenApples.GOLDEN_APPLE);
            final ImmutableValue<GoldenApple> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.GOLDEN_APPLE_TYPE, value,
                    GoldenApples.GOLDEN_APPLE);

            GoldenAppleUtils.setType((ItemStack) container, value);
            return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
        }
        return DataTransactionBuilder.failResult(ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.GOLDEN_APPLE_TYPE, value, GoldenApples.GOLDEN_APPLE));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
