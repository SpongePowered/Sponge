package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class EntityVelocityProvider extends GenericMutableDataProvider<Entity, Vector3d> {

    public EntityVelocityProvider() {
        super(Keys.VELOCITY);
    }

    @Override
    protected Optional<Vector3d> getFrom(Entity dataHolder) {
        return Optional.of(VecHelper.toVector3d(dataHolder.getMotion()));
    }

    @Override
    protected boolean set(Entity dataHolder, Vector3d value) {
        dataHolder.setMotion(VecHelper.toVec3d(value));
        return true;
    }
}
