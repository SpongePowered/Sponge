package org.spongepowered.common.data.processor.value;

import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import com.google.common.base.Optional;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;

public class WetValueProcessor extends AbstractSpongeValueProcessor<Boolean, Value<Boolean>> {
    protected WetValueProcessor() {
        super(Keys.IS_WET);
    }

    @Override
    public Optional<Boolean> getValueFromContainer(ValueContainer<?> container) {
        if (container.supports(Keys.IS_WET)) {
            return Optional.of(container.get(Keys.IS_WET).get());
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityWolf || container instanceof ItemStack;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Boolean value) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<Boolean> constructValue(Boolean defaultValue) {
        return new SpongeValue<Boolean>(Keys.IS_WET, false, defaultValue);
    }

}
