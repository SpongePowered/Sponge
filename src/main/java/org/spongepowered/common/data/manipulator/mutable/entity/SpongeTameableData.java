package org.spongepowered.common.data.manipulator.mutable.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.OwnableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpongeTameableData extends AbstractData<TameableData, ImmutableTameableData> implements TameableData  {
    @Nullable private UUID owner;
    public SpongeTameableData(@Nullable UUID owner) {
        super(TameableData.class);
        this.owner = owner;
        registerStuff();
    }

    @Override
    public OptionalValue<UUID> owner() {
        return new SpongeOptionalValue<UUID>(Keys.TAMED_OWNER, Optional.fromNullable(owner));
    }

    @Override
    public TameableData copy() {
        return new SpongeTameableData(this.owner);
    }

    @Override
    public ImmutableTameableData asImmutable() {
        return new SpongeImmutableTameableData(this.owner);
    }

    @Override
    public int compareTo(TameableData o) {
        return ComparisonChain.start()
                .compare(owner, o.owner().get().orNull())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.TAMED_OWNER.getQuery(), this.owner);
    }

    public Optional<UUID> getOwner(){
        return Optional.fromNullable(owner);
    }

    public void setOwner(@Nullable UUID owner){
        this.owner = new UUID(owner.getMostSignificantBits(), owner.getLeastSignificantBits());
    }

    private void registerStuff() {
        registerFieldGetter(Keys.TAMED_OWNER, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return owner();
            }
        });
        registerFieldSetter(Keys.TAMED_OWNER, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setOwner((UUID) value);
            }
        });
        registerKeyValue(Keys.TAMED_OWNER, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return owner();
            }
        });
    }
}
