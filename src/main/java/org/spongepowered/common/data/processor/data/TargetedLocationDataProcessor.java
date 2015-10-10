package org.spongepowered.common.data.processor.data;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathPoint;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableTargetedLocationData;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeTargetedLocationData;
import org.spongepowered.common.data.manipulator.mutable.SpongeTargetedLocationData;
import org.spongepowered.common.data.util.DataUtil;

import static com.google.common.base.Preconditions.checkNotNull;

public class TargetedLocationDataProcessor implements DataProcessor<TargetedLocationData, ImmutableTargetedLocationData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity;
    }

    @Override
    public Optional<TargetedLocationData> from(DataHolder dataHolder) {
        if(dataHolder instanceof EntityLiving) {
            final EntityLiving entity = (EntityLiving) dataHolder;
            final PathPoint path = entity.getNavigator().getPath().getFinalPathPoint();
            if(path == null) {
                return Optional.absent();
            }
            final TargetedLocationData data = new SpongeTargetedLocationData(new Location((World) entity.getEntityWorld(), path.xCoord, path.yCoord, path.zCoord));
            return Optional.of(data);
        }
        return Optional.absent();
    }

    @Override
    public Optional<TargetedLocationData> fill(DataHolder dataHolder, TargetedLocationData manipulator) {
        if(dataHolder instanceof EntityLiving) {
            final EntityLiving entity = (EntityLiving) dataHolder;
            final PathPoint path = entity.getNavigator().getPath().getFinalPathPoint();
            if(path == null) {
                return Optional.absent();
            }
            manipulator.set(manipulator.target().set(new Location((World) entity.getEntityWorld(), path.xCoord, path.yCoord, path.zCoord)));
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<TargetedLocationData> fill(DataHolder dataHolder, TargetedLocationData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final TargetedLocationData data = from(dataHolder).orNull();
            final TargetedLocationData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Location location = newData.target().get();
            return Optional.of(manipulator.set(Keys.TARGETED_LOCATION, location));
        }
        return Optional.absent();    }

    @Override
    public Optional<TargetedLocationData> fill(DataContainer container, TargetedLocationData targetedLocationData) {
        final Location location = DataUtil.getData(container, Keys.TARGETED_LOCATION, Location.class);
        return Optional.of(targetedLocationData.set(Keys.TARGETED_LOCATION, location));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, TargetedLocationData manipulator) {
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, TargetedLocationData manipulator, MergeFunction function) {
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableTargetedLocationData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableTargetedLocationData immutable) {
        if (key == Keys.TARGETED_LOCATION) {
            return Optional.<ImmutableTargetedLocationData>of(new ImmutableSpongeTargetedLocationData(immutable.target().get()));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof EntityLiving) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<TargetedLocationData> optional = from(dataHolder);
            if (optional.isPresent()) {
                try {
                    EntityLiving living = (EntityLiving) dataHolder;
                    living.getNavigator().clearPathEntity();
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue resetting the target location on an entity!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public TargetedLocationData create() {
        return null; // new SpongeTargetedLocationData() ?
    }

    @Override
    public ImmutableTargetedLocationData createImmutable() {
        return null; // new ImmutableSpongeTargetedLocationData() ?
    }

    @Override
    public Optional<TargetedLocationData> createFrom(DataHolder dataHolder) {
        return from(dataHolder); //something more?
    }

    @Override
    public Optional<TargetedLocationData> build(DataView container) throws InvalidDataException {
        final Location target = DataUtil.getData(container, Keys.TARGETED_LOCATION, Location.class);
        final TargetedLocationData data = new SpongeTargetedLocationData(target);
        return Optional.of(data);
    }
}
