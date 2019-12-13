package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LivingEntityBodyRotationsProvider extends GenericMutableDataProvider<LivingEntity, Map<BodyPart, Vector3d>> {

    public LivingEntityBodyRotationsProvider() {
        super(Keys.BODY_ROTATIONS);
    }

    @Override
    protected Optional<Map<BodyPart, Vector3d>> getFrom(LivingEntity dataHolder) {
        final double headYaw = dataHolder.getRotationYawHead();
        final double pitch = dataHolder.rotationPitch;
        final double yaw = dataHolder.rotationYaw;

        final Map<BodyPart, Vector3d> values = new HashMap<>();
        values.put(BodyParts.HEAD, new Vector3d(pitch, headYaw, 0));
        values.put(BodyParts.CHEST, new Vector3d(pitch, yaw, 0));
        return Optional.of(values);
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Map<BodyPart, Vector3d> value) {
        final Vector3d headRotation = value.get(BodyParts.HEAD);
        final Vector3d bodyRotation = value.get(BodyParts.CHEST);

        if (bodyRotation != null) {
            dataHolder.rotationYaw = (float) bodyRotation.getY();
            dataHolder.rotationPitch = (float) bodyRotation.getX();
        }
        if (headRotation != null) {
            dataHolder.rotationYawHead = (float) headRotation.getY();
            dataHolder.rotationPitch = (float) headRotation.getX();
        }

        return true;
    }
}
