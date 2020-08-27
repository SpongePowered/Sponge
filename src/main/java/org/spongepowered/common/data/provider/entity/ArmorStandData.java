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
package org.spongepowered.common.data.provider.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.Rotations;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.accessor.entity.item.ArmorStandEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ArmorStandData {

    private ArmorStandData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ArmorStandEntity.class)
                    .create(Keys.BODY_ROTATIONS)
                        .get(h -> {
                            final Map<BodyPart, Vector3d> values = new HashMap<>();
                            values.put(BodyParts.HEAD.get(), VecHelper.toVector3d(h.getHeadRotation()));
                            values.put(BodyParts.CHEST.get(), VecHelper.toVector3d(h.getBodyRotation()));
                            values.put(BodyParts.LEFT_ARM.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) h).accessor$getLeftArmRotation()));
                            values.put(BodyParts.RIGHT_ARM.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) h).accessor$getRightArmRotation()));
                            values.put(BodyParts.LEFT_LEG.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) h).accessor$getLeftLegRotation()));
                            values.put(BodyParts.RIGHT_LEG.get(), VecHelper.toVector3d(((ArmorStandEntityAccessor) h).accessor$getRightLegRotation()));
                            return values;
                        })
                        .set((h, v) -> {
                            apply(v, BodyParts.HEAD.get(), h::setHeadRotation);
                            apply(v, BodyParts.CHEST.get(), h::setBodyRotation);
                            apply(v, BodyParts.LEFT_ARM.get(), h::setLeftArmRotation);
                            apply(v, BodyParts.RIGHT_ARM.get(), h::setRightArmRotation);
                            apply(v, BodyParts.LEFT_LEG.get(), h::setLeftLegRotation);
                            apply(v, BodyParts.RIGHT_LEG.get(), h::setRightLegRotation);
                        })
                    .create(Keys.CHEST_ROTATION)
                        .get(h -> VecHelper.toVector3d(h.getBodyRotation()))
                        .set((h, v) -> h.setBodyRotation(VecHelper.toRotation(v)))
                    .create(Keys.HAS_ARMS)
                        .get(ArmorStandEntity::getShowArms)
                        .set((h, v) -> ((ArmorStandEntityAccessor) h).accessor$setShowArms(v))
                    .create(Keys.HAS_BASE_PLATE)
                        .get(h -> !h.hasNoBasePlate())
                        .set((h, v) -> ((ArmorStandEntityAccessor) h).accessor$setNoBasePlate(!v))
                    .create(Keys.HAS_MARKER)
                        .get(ArmorStandEntity::hasMarker)
                        .set((h, v) -> ((ArmorStandEntityAccessor) h).accessor$setMarker(v))
                    .create(Keys.HEAD_ROTATION)
                        .get(h -> VecHelper.toVector3d(h.getHeadRotation()))
                        .set((h, v) -> h.setHeadRotation(VecHelper.toRotation(v)))
                    .create(Keys.IS_PLACING_DISABLED)
                        .get(h -> Sponge.getRegistry().getCatalogRegistry()
                            .streamAllOf(EquipmentType.class)
                            .filter(t -> (Object) t instanceof EquipmentSlotType)
                            .collect(Collectors.toMap(t -> t, t -> ((ArmorStandEntityAccessor) h).accessor$isDisabled((EquipmentSlotType) (Object) t))))
                        .set((h, v) -> {
                            int chunk = 0;

                            int disabledSlots = ((ArmorStandEntityAccessor) h).accessor$getDisabledSlots();
                            // try and keep the all chunk empty
                            final int allChunk = disabledSlots & 0b1111_1111;
                            if (allChunk != 0) {
                                disabledSlots |= (allChunk << 16);
                                disabledSlots ^= 0b1111_1111;
                            }

                            if (v.get(EquipmentTypes.FEET.get())) chunk |= 1 << 1;
                            if (v.get(EquipmentTypes.LEGS.get())) chunk |= 1 << 2;
                            if (v.get(EquipmentTypes.CHEST.get())) chunk |= 1 << 3;
                            if (v.get(EquipmentTypes.HEAD.get())) chunk |= 1 << 4;

                            disabledSlots |= (chunk << 16);
                            ((ArmorStandEntityAccessor) h).accessor$setDisabledSlots(disabledSlots);
                        })
                    .create(Keys.IS_SMALL)
                        .get(ArmorStandEntity::isSmall)
                        .set((h, v) -> ((ArmorStandEntityAccessor) h).accessor$setSmall(v))
                    .create(Keys.IS_TAKING_DISABLED)
                        .get(h -> {
                            // include all chunk
                            final int disabled = ((ArmorStandEntityAccessor) h).accessor$getDisabledSlots();
                            final int resultantChunk = ((disabled >> 16) & 0b1111_1111) | (disabled & 0b1111_1111);

                            return ImmutableMap.of(
                                    EquipmentTypes.FEET.get(), (resultantChunk & (1 << 1)) != 0,
                                    EquipmentTypes.LEGS.get(), (resultantChunk & (1 << 2)) != 0,
                                    EquipmentTypes.CHEST.get(), (resultantChunk & (1 << 3)) != 0,
                                    EquipmentTypes.HEAD.get(), (resultantChunk & (1 << 4)) != 0);
                        })
                        .set((h, v) -> {
                            int chunk = 0;

                            int disabledSlots = ((ArmorStandEntityAccessor) h).accessor$getDisabledSlots();
                            // try and keep the all chunk empty
                            final int allChunk = disabledSlots & 0b1111_1111;
                            if (allChunk != 0) {
                                disabledSlots |= (allChunk << 16);
                                disabledSlots ^= 0b1111_1111;
                                ((ArmorStandEntityAccessor) h).accessor$setDisabledSlots(disabledSlots);
                            }

                            if (v.get(EquipmentTypes.FEET.get())) chunk |= 1 << 1;
                            if (v.get(EquipmentTypes.LEGS.get())) chunk |= 1 << 2;
                            if (v.get(EquipmentTypes.CHEST.get())) chunk |= 1 << 3;
                            if (v.get(EquipmentTypes.HEAD.get())) chunk |= 1 << 4;

                            disabledSlots |= (chunk << 8);
                            ((ArmorStandEntityAccessor) h).accessor$setDisabledSlots(disabledSlots);
                        })
                    .create(Keys.LEFT_ARM_ROTATION)
                        .get(h -> VecHelper.toVector3d(h.getLeftArmRotation()))
                        .set((h, v) -> h.setLeftArmRotation(VecHelper.toRotation(v)))
                    .create(Keys.LEFT_LEG_ROTATION)
                        .get(h -> VecHelper.toVector3d(h.getLeftLegRotation()))
                        .set((h, v) -> h.setLeftLegRotation(VecHelper.toRotation(v)))
                    .create(Keys.RIGHT_ARM_ROTATION)
                        .get(h -> VecHelper.toVector3d(h.getRightArmRotation()))
                        .set((h, v) -> h.setRightArmRotation(VecHelper.toRotation(v)))
                    .create(Keys.RIGHT_LEG_ROTATION)
                        .get(h -> VecHelper.toVector3d(h.getRightLegRotation()))
                        .set((h, v) -> h.setRightLegRotation(VecHelper.toRotation(v)));
    }
    // @formatter:on

    private static void apply(final Map<BodyPart, Vector3d> value, final BodyPart part, final Consumer<Rotations> consumer) {
        final Vector3d vec = value.get(part);
        if (vec == null) {
            return;
        }
        consumer.accept(VecHelper.toRotation(vec));
    }
}
