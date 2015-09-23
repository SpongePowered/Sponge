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
import org.spongepowered.api.data.type.HorseStyles;
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

public class HorseStyleValueProcessor extends AbstractSpongeValueProcessor<HorseStyle, Value<HorseStyle>> {

    public HorseStyleValueProcessor() {
        super(Keys.HORSE_STYLE);
    }

    @Override
    protected Value<HorseStyle> constructValue(HorseStyle defaultValue) {
        return new SpongeValue<HorseStyle>(Keys.HORSE_STYLE, defaultValue);
    }

    @Override
    public Optional<HorseStyle> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            return Optional.of(HorseUtils.getHorseStyle((EntityHorse) container));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityHorse;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, HorseStyle value) {
        ImmutableValue<HorseStyle> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_STYLE, value, HorseStyles.NONE);

        if (this.supports(container)) {
            EntityHorse horse = (EntityHorse) container;

            HorseStyle old = HorseUtils.getHorseStyle(horse);
            SpongeHorseColor color = (SpongeHorseColor) HorseUtils.getHorseColor(horse);

            ImmutableValue<HorseStyle> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_STYLE, old, HorseStyles.NONE);

            horse.setHorseVariant(HorseUtils.getInternalVariant(color, (SpongeHorseStyle) value));
            return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
