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

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.math.Rotations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface EntityArmorStandAccessor {

    @Accessor("DEFAULT_HEAD_ROTATION")
    static Rotations accessor$getDefaultHeadRotation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DEFAULT_BODY_ROTATION")
    static Rotations accessor$getDefaultBodyRotation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DEFAULT_LEFTARM_ROTATION")
    static Rotations accessor$getDefaultLeftarmRotation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DEFAULT_RIGHTARM_ROTATION")
    static Rotations accessor$getDefaultRightarmRotation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DEFAULT_LEFTLEG_ROTATION")
    static Rotations accessor$getDefaultLeftlegRotation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DEFAULT_RIGHTLEG_ROTATION")
    static Rotations accessor$getDefaultRightlegRotation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("STATUS")
    static DataParameter<Byte> accessor$getStatusParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("HEAD_ROTATION")
    static DataParameter<Rotations> accessor$getHeadRotationParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("BODY_ROTATION")
    static DataParameter<Rotations> accessor$getBodyRotationParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("LEFT_ARM_ROTATION")
    static DataParameter<Rotations> accessor$getLeftArmRotationParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("RIGHT_ARM_ROTATION")
    static DataParameter<Rotations> accessor$getRightArmRotationParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("LEFT_LEG_ROTATION")
    static DataParameter<Rotations> accessor$getLeftLegRotationParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("RIGHT_LEG_ROTATION")
    static DataParameter<Rotations> accessor$getRightLegRotationParameter() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    
    @Accessor("headRotation") Rotations accessor$getheadRotation();
    @Accessor("bodyRotation") Rotations accessor$getbodyRotation();
    @Accessor("leftArmRotation") Rotations accessor$getleftArmRotation();
    @Accessor("rightArmRotation") Rotations accessor$getrightArmRotation();
    @Accessor("leftLegRotation") Rotations accessor$getleftLegRotation();
    @Accessor("rightLegRotation") Rotations accessor$getrightLegRotation();

    @Accessor("disabledSlots") int accessor$getDisabledSlots();
    @Accessor("disabledSlots") void accessor$setDisabledSlots(int disabledSlots);

    @Invoker("setShowArms") void accessor$setShowArms(boolean showArms);
    @Invoker("setSmall") void accessor$setSmall(boolean small);
    @Invoker("setNoBasePlate") void accessor$setNoBasePlate(boolean noBasePlate);
    @Invoker("setMarker") void accessor$setMarker(boolean marker);

}
