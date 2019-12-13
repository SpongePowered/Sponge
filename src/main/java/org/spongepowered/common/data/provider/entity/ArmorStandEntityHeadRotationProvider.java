package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class ArmorStandEntityHeadRotationProvider extends GenericMutableDataProvider<ArmorStandEntity, Vector3d> {

    public ArmorStandEntityHeadRotationProvider() {
        super(Keys.HEAD_ROTATION);
    }

    @Override
    protected Optional<Vector3d> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(VecHelper.toVector3d(dataHolder.getHeadRotation()));
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Vector3d value) {
        dataHolder.setHeadRotation(VecHelper.toRotation(value));
        return true;
    }
}
