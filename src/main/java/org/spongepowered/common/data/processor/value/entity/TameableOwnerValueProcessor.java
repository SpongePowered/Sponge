package org.spongepowered.common.data.processor.value.entity;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.EntityTameable;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.processor.data.entity.TameableDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.UUID;

public class TameableOwnerValueProcessor implements ValueProcessor<Optional<UUID>, OptionalValue<UUID>> {

    @Override
    public Key<? extends BaseValue<Optional<UUID>>> getKey() {
        return Keys.TAMED_OWNER;
    }

    @Override
    public Optional<Optional<UUID>> getValueFromContainer(ValueContainer<?> container) {
        if(container instanceof EntityTameable) {
            final Optional<UUID> out = TameableDataProcessor.getTamer((EntityTameable) container);
            return Optional.of(out);
        }
        return Optional.absent();
    }

    @Override
    public Optional<OptionalValue<UUID>> getApiValueFromContainer(ValueContainer<?> container) {
        if(container instanceof EntityTameable) {
            final Optional<UUID> out = TameableDataProcessor.getTamer((EntityTameable) container);
            return Optional.<OptionalValue<UUID>>of(new SpongeOptionalValue<UUID>(Keys.TAMED_OWNER, out));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityTameable;
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<Optional<UUID>, Optional<UUID>> function) {
        if(container instanceof EntityTameable) {
            final Optional<UUID> oldUUID = TameableDataProcessor.getTamer((EntityTameable) container);
            final Optional<UUID> newUUID = Preconditions.checkNotNull(function).apply(oldUUID);
            return offerToStore(container, newUUID);
        } else {
            return DataTransactionBuilder.failNoData();
        }
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        final OptionalValue<UUID> actualValue = (OptionalValue<UUID>) value;
        final ImmutableSpongeOptionalValue<UUID> proposedValue = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, actualValue.get());

        if(container instanceof EntityTameable) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<UUID> tamer = TameableDataProcessor.getTamer((EntityTameable) container);
            final ImmutableValue<Optional<UUID>> oldTamer = this.getApiValueFromContainer(container).get().asImmutable();
            final ImmutableOptionalValue<UUID> newTamer = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, actualValue.get());

            try {
                ((EntityTameable) container).setOwnerId(TameableDataProcessor.asString(actualValue.get()));
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newTamer);
            }
            return builder.success(newTamer).replace(oldTamer).result(DataTransactionResult.Type.SUCCESS).build();
        }

        return DataTransactionBuilder.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Optional<UUID> value) {
        return offerToStore(container, new SpongeOptionalValue<UUID>(Keys.TAMED_OWNER, value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
