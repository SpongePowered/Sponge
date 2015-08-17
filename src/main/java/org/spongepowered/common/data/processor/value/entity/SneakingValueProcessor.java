package org.spongepowered.common.data.processor.value.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import net.minecraft.entity.Entity;

import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Function;
import com.google.common.base.Optional;

public class SneakingValueProcessor implements ValueProcessor<Boolean, Value<Boolean>> {

	@Override
	public Key<? extends BaseValue<Boolean>> getKey() {
		return Keys.IS_SNEAKING;
	}

	@Override
	public Optional<Boolean> getValueFromContainer(ValueContainer<?> container) {
		if (container instanceof Entity) {
            return Optional.of(Boolean.valueOf(((Entity) container).isSneaking()));
        }
        return Optional.absent();
	}

	@Override
	public Optional<Value<Boolean>> getApiValueFromContainer(ValueContainer<?> container) {
		if (container instanceof Entity) {
            final boolean sneaking = ((Entity) container).isSneaking();
            return Optional.<Value<Boolean>>of(new SpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, Boolean.valueOf(sneaking)));
        }
        return Optional.absent();
	}

	@Override
	public boolean supports(ValueContainer<?> container) {
		return container instanceof Entity;
	}

	@Override
	public DataTransactionResult transform(ValueContainer<?> container, Function<Boolean, Boolean> function) {
		if (container instanceof Entity) {
            final Boolean old = getValueFromContainer(container).get();
            final ImmutableValue<Boolean> oldValue = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, old);
            final Boolean sneaking = checkNotNull(checkNotNull(function, "function").apply(old), "The function returned a null value!");
            final ImmutableValue<Boolean> newVal = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, sneaking);
            try {
                ((Entity) container).setSneaking(sneaking);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newVal);
            }
            return DataTransactionBuilder.successReplaceResult(newVal, oldValue);
        }
        return DataTransactionBuilder.failNoData();
	}

	@Override
	public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
		return offerToStore(container, ((Boolean) value.get()));
	}

	@Override
	public DataTransactionResult offerToStore(ValueContainer<?> container, Boolean value) {
		final ImmutableValue<Boolean> newValue = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, value);
        if (container instanceof Entity) {
            final Boolean old = getValueFromContainer(container).get();
            final ImmutableValue<Boolean> oldValue = new ImmutableSpongeValue<Boolean>(Keys.IS_SNEAKING, Boolean.FALSE, old);
            try {
                ((Entity) container).setSneaking(value);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(newValue);
	}

	@Override
	public DataTransactionResult removeFrom(ValueContainer<?> container) {
		return DataTransactionBuilder.failNoData();
	}

}
