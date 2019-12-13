package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.Rotations;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ArmorStandEntityBodyRotationsProvider extends GenericMutableDataProvider<ArmorStandEntity, Map<BodyPart, Vector3d>> {

    public ArmorStandEntityBodyRotationsProvider() {
        super(Keys.BODY_ROTATIONS);
    }

    @Override
    protected Optional<Map<BodyPart, Vector3d>> getFrom(ArmorStandEntity dataHolder) {
        final Map<BodyPart, Vector3d> values = new HashMap<>();
        values.put(BodyParts.HEAD, VecHelper.toVector3d(dataHolder.getHeadRotation()));
        values.put(BodyParts.CHEST, VecHelper.toVector3d(dataHolder.getBodyRotation()));
        values.put(BodyParts.LEFT_ARM, VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getLeftArmRotation()));
        values.put(BodyParts.RIGHT_ARM, VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getRightArmRotation()));
        values.put(BodyParts.LEFT_LEG, VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getLeftLegRotation()));
        values.put(BodyParts.RIGHT_LEG, VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getRightLegRotation()));
        return Optional.of(values);
    }

    private static void apply(Map<BodyPart, Vector3d> value, BodyPart bodyPart, Consumer<Rotations> consumer) {
        final Vector3d vector = value.get(bodyPart);
        if (vector == null) {
            return;
        }
        consumer.accept(VecHelper.toRotation(vector));
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Map<BodyPart, Vector3d> value) {
        apply(value, BodyParts.HEAD, dataHolder::setHeadRotation);
        apply(value, BodyParts.CHEST, dataHolder::setBodyRotation);
        apply(value, BodyParts.LEFT_ARM, dataHolder::setLeftArmRotation);
        apply(value, BodyParts.RIGHT_ARM, dataHolder::setRightArmRotation);
        apply(value, BodyParts.LEFT_LEG, dataHolder::setLeftLegRotation);
        apply(value, BodyParts.RIGHT_LEG, dataHolder::setRightLegRotation);
        return true;
    }
}
