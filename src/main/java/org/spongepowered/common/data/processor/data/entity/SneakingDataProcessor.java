package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;
import static org.spongepowered.common.data.util.DataUtil.getData;
import net.minecraft.entity.Entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSneakingData;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSneakingData;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVelocityData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSneakingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;

public class SneakingDataProcessor implements DataProcessor<SneakingData, ImmutableSneakingData> {

	@Override
	public Optional<SneakingData> build(DataView container) throws InvalidDataException {
		return Optional.absent();
	}

	@Override
	public SneakingData create() {
		return new SpongeSneakingData();
	}

	@Override
	public ImmutableSneakingData createImmutable() {
		return new ImmutableSpongeSneakingData(false);
	}

	@Override
	public Optional<SneakingData> createFrom(DataHolder dataHolder) {
		if (dataHolder instanceof Entity) {
            final boolean sneaking = ((Entity) dataHolder).isSneaking();
            return Optional.<SneakingData>of(new SpongeSneakingData(sneaking));
        }
        return Optional.absent();
	}

	@Override
	public boolean supports(DataHolder dataHolder) {
		return dataHolder instanceof Entity;
	}

	@Override
	public Optional<SneakingData> from(DataHolder dataHolder) {
		if (dataHolder instanceof Entity) {
			final boolean sneaking = ((Entity) dataHolder).isSneaking();
			return Optional.<SneakingData>of(new SpongeSneakingData(sneaking));
		}
		return Optional.absent();
	}

	@Override
	public Optional<SneakingData> fill(DataHolder dataHolder, SneakingData manipulator) {
		if (dataHolder instanceof Entity) {
			final Value<Boolean> sneakingValue = manipulator.sneaking();
			final boolean sneaking = ((Entity) dataHolder).isSneaking();
			sneakingValue.set(sneaking);
			return Optional.of(manipulator.set(sneakingValue));
		}
		return Optional.absent();
	}

	@Override
	public Optional<SneakingData> fill(DataHolder dataHolder, SneakingData manipulator, MergeFunction overlap) {
		if (dataHolder instanceof Entity) {
			final Optional<SneakingData> oldData = from(dataHolder);
			final SneakingData newData = checkNotNull(overlap, "Merge function was null!").merge(oldData.orNull(), manipulator);
			final Value<Boolean> newValue = newData.sneaking();
			return Optional.of(manipulator.set(newValue));
		}
		return Optional.absent();
	}

	@Override
	public Optional<SneakingData> fill(DataContainer container, SneakingData sneakingData) {
		sneakingData.set(Keys.IS_SNEAKING, getData(container, Keys.IS_SNEAKING));
		return Optional.of(sneakingData);
	}

	@Override
	public DataTransactionResult set(DataHolder dataHolder, SneakingData manipulator) {
		if (dataHolder instanceof Entity) {
			final ImmutableValue<Boolean> newValue = manipulator.sneaking().asImmutable();
            final SneakingData old = from(dataHolder).get();
            final ImmutableValue<Boolean> oldValue = old.asImmutable().sneaking();
            final Boolean sneaking = manipulator.sneaking().get();
            try {
                ((Entity) dataHolder).setSneaking(sneaking);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                DataTransactionBuilder.errorResult(newValue);
            }
		}
		return DataTransactionBuilder.failResult(manipulator.getValues());
	}

	@Override
	public DataTransactionResult set(DataHolder dataHolder, SneakingData manipulator, MergeFunction function) {
		if (dataHolder instanceof Entity) {
            final ImmutableValue<Boolean> newValue = manipulator.sneaking().asImmutable();
            final SneakingData old = from(dataHolder).get();
            final ImmutableValue<Boolean> oldValue = old.asImmutable().sneaking();
            final SneakingData newData = checkNotNull(function, "function").merge(old, manipulator);
            final Boolean sneaking = newData.sneaking().get();
            try {
            	((Entity) dataHolder).setSneaking(sneaking);
                return DataTransactionBuilder.successReplaceResult(newValue, oldValue);
            } catch (Exception e) {
                DataTransactionBuilder.errorResult(newValue);
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

	@Override
	public Optional<ImmutableSneakingData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableSneakingData immutable) {
		if (!key.equals(Keys.IS_SNEAKING)) {
            return Optional.absent();
        }
        final ImmutableSneakingData data = new ImmutableSpongeSneakingData((Boolean) value);
        return Optional.of(data);
	}

	@Override
	public DataTransactionResult remove(DataHolder dataHolder) {
		return DataTransactionBuilder.failNoData();
	}

}
