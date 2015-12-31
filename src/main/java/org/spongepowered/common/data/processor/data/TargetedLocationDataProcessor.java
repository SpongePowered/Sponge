package org.spongepowered.common.data.processor.data;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.ImmutableTargetedLocationData;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.manipulator.mutable.SpongeTargetedLocationData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.interfaces.IMixinEntityPlayer;

import java.util.Optional;

public class TargetedLocationDataProcessor extends AbstractSpongeDataProcessor<TargetedLocationData, ImmutableTargetedLocationData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityEnderEye || dataHolder instanceof EntityLiving;
    }

    @Override
    public Optional<TargetedLocationData> from(DataHolder dataHolder) {
        if(dataHolder instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) dataHolder;
            IMixinEntityPlayer mixinPlayer = (IMixinEntityPlayer) dataHolder;
            BlockPos pos = mixinPlayer.getCompassLocation();
            if(pos == null) pos = player.getEntityWorld().getSpawnPoint();
            final TargetedLocationData data = new SpongeTargetedLocationData(new Location<>((World) player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ()));
            return Optional.of(data);
        } else if(dataHolder instanceof EntityLiving) {
            final EntityLiving entity = (EntityLiving) dataHolder;
            final PathPoint path = entity.getNavigator().getPath().getFinalPathPoint();
            if(path == null) {
                return Optional.empty();
            }
            final TargetedLocationData data = new SpongeTargetedLocationData(new Location<>((World) entity.getEntityWorld(), path.xCoord, path.yCoord, path.zCoord));
            return Optional.of(data);
        } else if(dataHolder instanceof EntityEnderEye) {
            // TODO
        }
        return Optional.empty();
    }

    @Override
    public Optional<TargetedLocationData> createFrom(DataHolder dataHolder) {
        return null;
    }

    @Override
    public Optional<TargetedLocationData> fill(DataHolder dataHolder, TargetedLocationData manipulator, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<TargetedLocationData> fill(DataContainer container, TargetedLocationData targetedLocationData) {
        return null;
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, TargetedLocationData manipulator, MergeFunction function) {
        return null;
    }

    @Override
    public Optional<ImmutableTargetedLocationData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableTargetedLocationData immutable) {
        return null;
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return null;
    }
}
