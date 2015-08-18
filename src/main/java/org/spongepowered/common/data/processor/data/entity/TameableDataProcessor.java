package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.entity.passive.EntityTameable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTameableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import javax.annotation.Nullable;
import java.util.UUID;

public class TameableDataProcessor implements DataProcessor<TameableData, ImmutableTameableData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityTameable;
    }

    @Override
    public Optional<TameableData> from(DataHolder dataHolder) {
        if(dataHolder instanceof EntityTameable) {
            final Optional<UUID> uuidOptional = asUUID(((EntityTameable) dataHolder));
            return Optional.<TameableData>of(new SpongeTameableData(uuidOptional.orNull()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<TameableData> fill(DataHolder dataHolder, TameableData manipulator) {
        if(dataHolder instanceof EntityTameable) {
            manipulator.set(Keys.TAMED_OWNER, (asUUID((EntityTameable) dataHolder)));
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<TameableData> fill(DataHolder dataHolder, TameableData manipulator, MergeFunction overlap) {
        if(dataHolder instanceof EntityTameable) {
            final TameableData merged = overlap.merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.TAMED_OWNER, merged.owner().get());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<TameableData> fill(final DataContainer container, final TameableData tameableData) {
        tameableData.set(Keys.TAMED_OWNER, getData(container, Keys.TAMED_OWNER));
        return Optional.of(tameableData);
    }

    @Override
    public DataTransactionResult set(final DataHolder dataHolder, final TameableData manipulator) {
        if(dataHolder instanceof EntityTameable) {
            final EntityTameable entityTameable = (EntityTameable) dataHolder;
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final String sPrevTamer = entityTameable.getOwnerId();
            final Optional<UUID> prevTamer = asUUID(sPrevTamer);
            final Optional<UUID> newTamer = manipulator.owner().get();
            //Shouldn't this use the ImmutableDataCachingUtil?
            final ImmutableSpongeOptionalValue<UUID> prevValue = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, prevTamer);
            final ImmutableSpongeOptionalValue<UUID> newValue = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, newTamer);
            try {
                builder.replace(prevValue);
                entityTameable.setOwnerId(asString(newTamer));
                builder.success(newValue)
                    .result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            } catch (Exception e) {
                entityTameable.setOwnerId(sPrevTamer);
                builder.reject(newValue)
                    .result(DataTransactionResult.Type.ERROR);
                return builder.build();
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, TameableData manipulator, MergeFunction overlap) {
        //Health was unimplemented, is there something stopping implementation?
        return null;
    }

    @Override
    public Optional<ImmutableTameableData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableTameableData immutable) {
        //TODO: Health returns absent, investigate solutions.
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        //Fail to remove data, at this stage untameable tameables are not supported.
        //TODO: Does this negatively affect other DataHolders like configs?
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public TameableData create() {
        return new SpongeTameableData(null);
    }

    @Override
    public ImmutableTameableData createImmutable() {
        //TODO: Check ImmutableDataCachingUtil?
        return new ImmutableSpongeTameableData(null);
    }

    @Override
    public Optional<TameableData> createFrom(DataHolder dataHolder) {
        if(dataHolder instanceof EntityTameable) {
            final Optional<UUID> uuidOptional = asUUID((EntityTameable) dataHolder);
            return Optional.<TameableData>of(new SpongeTameableData(uuidOptional.orNull()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<TameableData> build(DataView container) throws InvalidDataException {
        //TODO: Why?
        return Optional.absent();
    }

    private static String asString(final Optional<UUID> uuidOptional) {
        if(uuidOptional.isPresent()) {
            UUIDTypeAdapter.fromUUID(uuidOptional.get());
        }
        return "";
    }

    private static Optional<UUID> asUUID(@Nullable final EntityTameable tameable) {
        if(tameable == null) {
            return Optional.absent();
        }
        return asUUID(tameable.getOwnerId());
    }

    private static Optional<UUID> asUUID(@Nullable final String sUUID) {
        if(sUUID == null) {
            return Optional.absent();
        }
        @Nullable UUID uuid;
        try {
            uuid = UUIDTypeAdapter.fromString(sUUID);
        } catch(final RuntimeException ignored) {
            uuid = null;
        }
        return Optional.fromNullable(uuid);
    }
}