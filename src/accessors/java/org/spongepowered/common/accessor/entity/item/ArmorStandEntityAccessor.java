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
package org.spongepowered.common.accessor.entity.item;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.Rotations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandEntityAccessor {

    @Accessor("disabledSlots") int accessor$disabledSlots();

    @Accessor("disabledSlots") void accessor$disabledSlots(final int disabledSlots);

    @Accessor("leftArmPose") Rotations accessor$leftArmPose();

    @Accessor("rightArmPose") Rotations accessor$rightArmPose();

    @Accessor("leftLegPose") Rotations accessor$leftLegPose();

    @Accessor("rightLegPose") Rotations accessor$rightLegPose();

    @Invoker("isDisabled") boolean invoker$isDisabled(final EquipmentSlotType slot);

    @Invoker("setSmall") void invoker$setSmall(final boolean small);

    @Invoker("setShowArms") void invoker$setShowArms(final boolean showArms);

    @Invoker("setNoBasePlate") void invoker$setNoBasePlate(final boolean noBasePlate);

    @Invoker("setMarker") void invoker$setMarker(final boolean marker);

}
