package org.spongepowered.common.data.processor.value.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseStyles;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeHorseVariant;

public class HorseVariantValueProcessor extends AbstractSpongeValueProcessor<HorseVariant, Value<HorseVariant>> {

    public HorseVariantValueProcessor() {
        super(Keys.HORSE_VARIANT);
    }

    @Override
    protected Value<HorseVariant> constructValue(HorseVariant defaultValue) {
        return new SpongeValue<HorseVariant>(Keys.HORSE_VARIANT, defaultValue);
    }

    @Override
    public Optional<HorseVariant> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            return Optional.of(HorseUtils.getHorseVariant(((EntityHorse) container).getHorseType()));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityHorse;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, HorseVariant value) {
        ImmutableValue<HorseVariant> newValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_VARIANT, value, HorseVariants.HORSE);

        if (this.supports(container)) {
            EntityHorse horse = (EntityHorse) container;

            ImmutableValue<HorseVariant> oldValue = ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.HORSE_VARIANT, HorseUtils.getHorseVariant(horse.getHorseType()), HorseVariants.HORSE);
            horse.setHorseType(((SpongeHorseVariant) value).type);

            return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
