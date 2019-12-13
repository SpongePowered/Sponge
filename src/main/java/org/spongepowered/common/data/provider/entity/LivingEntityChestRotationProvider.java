package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class LivingEntityChestRotationProvider extends GenericMutableDataProvider<LivingEntity, Vector3d> {

    public LivingEntityChestRotationProvider() {
        super(Keys.CHEST_ROTATION);
    }

    @Override
    protected Optional<Vector3d> getFrom(LivingEntity dataHolder) {
        final double yaw = dataHolder.rotationYaw;
        final double pitch = dataHolder.rotationPitch;
        return Optional.of(new Vector3d(pitch, yaw, 0));
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Vector3d value) {
        final float headYaw = (float) value.getY();
        final float pitch = (float) value.getX();
        dataHolder.setRotationYawHead(headYaw);
        dataHolder.rotationPitch = pitch;
        return true;
    }
}
