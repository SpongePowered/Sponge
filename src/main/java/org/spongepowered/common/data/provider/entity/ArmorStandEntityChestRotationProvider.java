package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class ArmorStandEntityChestRotationProvider extends GenericMutableDataProvider<ArmorStandEntity, Vector3d> {

    public ArmorStandEntityChestRotationProvider() {
        super(Keys.CHEST_ROTATION);
    }

    @Override
    protected Optional<Vector3d> getFrom(ArmorStandEntity dataHolder) {
        return Optional.of(VecHelper.toVector3d(dataHolder.getBodyRotation()));
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Vector3d value) {
        dataHolder.setBodyRotation(VecHelper.toRotation(value));
        return true;
    }
}
