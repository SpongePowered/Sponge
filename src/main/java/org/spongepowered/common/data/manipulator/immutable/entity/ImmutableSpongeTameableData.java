package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImmutableSpongeTameableData extends AbstractImmutableData<ImmutableTameableData, TameableData> implements ImmutableTameableData {
    @Nullable private final UUID owner;

    public ImmutableSpongeTameableData(@Nullable UUID owner) {
        super(ImmutableTameableData.class);
        this.owner = owner;
    }

    @Override
    public ImmutableOptionalValue<UUID> owner() {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeOptionalValue.class, Keys.TAMED_OWNER, Optional.fromNullable(this.owner), Optional.absent());//TODO: fix
    }

    @Override
    public ImmutableTameableData copy() {
        return this; //It's immutable, just return this, UUID is also immutable.
    }

    @Override
    public TameableData asMutable() {
        return new SpongeTameableData(owner);
    }

    @Override
    public int compareTo(ImmutableTameableData o) {
        return ComparisonChain.start()
                .compare(owner, o.owner().get().orNull())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.TAMED_OWNER.getQuery(), this.owner);
    }
}
