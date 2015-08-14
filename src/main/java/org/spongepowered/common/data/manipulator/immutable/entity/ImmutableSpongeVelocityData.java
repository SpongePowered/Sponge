package org.spongepowered.common.data.manipulator.immutable.entity;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVelocityData;
import org.spongepowered.api.data.manipulator.mutable.entity.VelocityData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVelocityData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeVelocityData extends AbstractImmutableSingleData<Vector3d, ImmutableVelocityData, VelocityData> implements ImmutableVelocityData {

    public ImmutableSpongeVelocityData(Vector3d value) {
        super(ImmutableVelocityData.class, value, Keys.VELOCITY);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return velocity();
    }

    @Override
    public VelocityData asMutable() {
        return new SpongeVelocityData(this.value);
    }

    @Override
    public ImmutableValue<Vector3d> velocity() {
        return new ImmutableSpongeValue<Vector3d>(Keys.VELOCITY, new Vector3d(), this.value);
    }

    @Override
    public int compareTo(ImmutableVelocityData o) {
        return o.velocity().get().compareTo(this.value);
    }
}
