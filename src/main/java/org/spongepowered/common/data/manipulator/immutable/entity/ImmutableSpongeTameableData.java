package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.base.Optional;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImmutableSpongeTameableData extends AbstractImmutableData<ImmutableTameableData, TameableData> implements ImmutableTameableData {
    @Nullable private final UUID tamer;

    public ImmutableSpongeTameableData(@Nullable UUID tamer) {
        super(ImmutableTameableData.class);
        this.tamer = tamer;
    }

    @Override
    public ImmutableOptionalValue<UUID> tamer(){
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeOptionalValue.class, Keys.TAMED_OWNER, Optional.fromNullable(this.tamer), Optional.absent());//TODO: fix
    }
}
