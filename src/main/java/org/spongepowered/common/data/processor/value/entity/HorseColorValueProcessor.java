package org.spongepowered.common.data.processor.value.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseColors;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;

public class HorseColorValueProcessor extends AbstractSpongeValueProcessor<HorseColor, Value<HorseColor>> {

    public HorseColorValueProcessor() {
        super(Keys.HORSE_COLOR);
    }

    @Override
    protected Value<HorseColor> constructValue(HorseColor defaultValue) {
        return new SpongeValue<HorseColor>(Keys.HORSE_COLOR, defaultValue);
    }

    @Override
    public Optional<HorseColor> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            return Optional.of(HorseUtils.getHorseColor((EntityHorse) container));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityHorse;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, HorseColor value) {
        ImmutableValue<HorseColor> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_COLOR, value, HorseColors.WHITE);

        if (this.supports(container)) {
            EntityHorse horse = (EntityHorse) container;

            HorseColor old = HorseUtils.getHorseColor(horse);
            SpongeHorseStyle style = (SpongeHorseStyle) HorseUtils.getHorseStyle(horse);

            ImmutableValue<HorseColor> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_COLOR, old, HorseColors.WHITE);

            horse.setHorseVariant(HorseUtils.getInternalVariant((SpongeHorseColor) value, style));
            return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
