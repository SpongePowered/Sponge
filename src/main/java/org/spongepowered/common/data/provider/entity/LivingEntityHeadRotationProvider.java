package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class LivingEntityHeadRotationProvider extends GenericMutableDataProvider<LivingEntity, Vector3d> {

    public LivingEntityHeadRotationProvider() {
        super(Keys.HEAD_ROTATION);
    }

    @Override
    protected Optional<Vector3d> getFrom(LivingEntity dataHolder) {
        final double headYaw = dataHolder.getRotationYawHead();
        final double pitch = dataHolder.rotationPitch;
        return Optional.of(new Vector3d(pitch, headYaw, 0));
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Vector3d value) {
        final float yaw = (float) value.getY();
        final float pitch = (float) value.getX();
        dataHolder.rotationYaw = yaw;
        dataHolder.rotationPitch = pitch;
        return true;
    }
}
