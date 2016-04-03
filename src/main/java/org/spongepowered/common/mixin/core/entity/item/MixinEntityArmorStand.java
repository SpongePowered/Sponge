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
package org.spongepowered.common.mixin.core.entity.item;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Rotations;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBodyPartRotationalData;
import org.spongepowered.common.data.processor.multi.entity.ArmorStandBodyPartRotationalDataProcessor;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;
import org.spongepowered.common.util.VecHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(EntityArmorStand.class)
public abstract class MixinEntityArmorStand extends MixinEntityLivingBase implements ArmorStand {

    @Shadow public Rotations leftArmRotation;
    @Shadow public Rotations rightArmRotation;
    @Shadow public Rotations leftLegRotation;
    @Shadow public Rotations rightLegRotation;
    @Shadow public abstract Rotations shadow$getHeadRotation();
    @Shadow public abstract Rotations getBodyRotation();

    @Override
    public BodyPartRotationalData getBodyPartRotationalData() {
        Map<Key<?>, Object> values = Maps.newHashMapWithExpectedSize(6);

        values.put(Keys.HEAD_ROTATION, VecHelper.toVector(this.shadow$getHeadRotation()));
        values.put(Keys.CHEST_ROTATION, VecHelper.toVector(this.getBodyRotation()));
        values.put(Keys.LEFT_ARM_ROTATION, VecHelper.toVector(this.leftArmRotation));
        values.put(Keys.RIGHT_ARM_ROTATION, VecHelper.toVector(this.rightArmRotation));
        values.put(Keys.LEFT_LEG_ROTATION, VecHelper.toVector(this.leftLegRotation));
        values.put(Keys.RIGHT_LEG_ROTATION, VecHelper.toVector(this.rightLegRotation));
        Collection<BodyPart> bodyParts = Sponge.getRegistry().getAllOf(BodyPart.class);
        Collection<Vector3d> rotations = Arrays.asList(values.values().toArray(new Vector3d[values.values().size()]));
        values.put(Keys.BODY_ROTATIONS, ArmorStandBodyPartRotationalDataProcessor.zipCollections(bodyParts, rotations));
        return new SpongeBodyPartRotationalData();
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getBodyPartRotationalData());
    }

}
