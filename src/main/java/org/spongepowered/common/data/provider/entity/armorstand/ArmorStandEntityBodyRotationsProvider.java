/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.provider.entity.armorstand;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.Rotations;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.accessor.entity.item.ArmorStandEntityAccessor;
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
        values.put(BodyParts.HEAD.get(), VecHelper.toVector3d(dataHolder.getHeadRotation()));
        values.put(BodyParts.CHEST.get(), VecHelper.toVector3d(dataHolder.getBodyRotation()));
        values.put(BodyParts.LEFT_ARM.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getLeftArmRotation()));
        values.put(BodyParts.RIGHT_ARM.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getRightArmRotation()));
        values.put(BodyParts.LEFT_LEG.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getLeftLegRotation()));
        values.put(BodyParts.RIGHT_LEG.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) dataHolder).accessor$getRightLegRotation()));
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
        apply(value, BodyParts.HEAD.get(), dataHolder::setHeadRotation);
        apply(value, BodyParts.CHEST.get(), dataHolder::setBodyRotation);
        apply(value, BodyParts.LEFT_ARM.get(), dataHolder::setLeftArmRotation);
        apply(value, BodyParts.RIGHT_ARM.get(), dataHolder::setRightArmRotation);
        apply(value, BodyParts.LEFT_LEG.get(), dataHolder::setLeftLegRotation);
        apply(value, BodyParts.RIGHT_LEG.get(), dataHolder::setRightLegRotation);
        return true;
    }
}
