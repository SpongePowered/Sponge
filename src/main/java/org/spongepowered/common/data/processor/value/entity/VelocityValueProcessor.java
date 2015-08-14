package org.spongepowered.common.data.processor.value.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Function;
import com.google.common.base.Optional;
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

public class VelocityValueProcessor implements ValueProcessor<Vector3d, Value<Vector3d>> {

    @Override
    public Key<? extends BaseValue<Vector3d>> getKey() {
        return Keys.VELOCITY;
    }

    @Override
    public Optional<Vector3d> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof Entity) {
            return Optional.of(new Vector3d(((Entity) container).motionX, ((Entity) container).motionY, ((Entity) container).motionZ));
        }
        return Optional.absent();
    }

    @Override
    public Optional<Value<Vector3d>> getApiValueFromContainer(ValueContainer<?> container) {
        if (container instanceof Entity) {
            final double x = ((Entity) container).motionX;
            final double y = ((Entity) container).motionY;
            final double z = ((Entity) container).motionZ;
            return Optional.<Value<Vector3d>>of(new SpongeValue<Vector3d>(Keys.VELOCITY, new Vector3d(), new Vector3d(x, y, z)));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof Entity;
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container, Function<Vector3d, Vector3d> function) {
        if (container instanceof Entity) {
            final Vector3d old = getValueFromContainer(container).get();
            final ImmutableValue<Vector3d> oldValue = new ImmutableSpongeValue<Vector3d>(Keys.VELOCITY, new Vector3d(), old);
            final Vector3d newVec = checkNotNull(checkNotNull(function, "function").apply(old), "The function returned a null value!");
            final ImmutableValue<Vector3d> newVal = new ImmutableSpongeValue<Vector3d>(Keys.VELOCITY, new Vector3d(), newVec);
            try {
                ((Entity) container).motionX = newVec.getX();
                ((Entity) container).motionY = newVec.getY();
                ((Entity) container).motionZ = newVec.getZ();
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newVal);
            }
            return DataTransactionBuilder.successReplaceResult(newVal, oldValue);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        return offerToStore(container, ((Vector3d) value.get()));
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Vector3d value) {
        final ImmutableValue<Vector3d> newValue = new ImmutableSpongeValue<Vector3d>(Keys.VELOCITY, new Vector3d(), value);
        if (container instanceof Entity) {
            final Vector3d old = getValueFromContainer(container).get();
            final ImmutableValue<Vector3d> oldValue = new ImmutableSpongeValue<Vector3d>(Keys.VELOCITY, new Vector3d(), old);
            try {
                ((Entity) container).motionX = value.getX();
                ((Entity) container).motionY = value.getY();
                ((Entity) container).motionZ = value.getZ();
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
