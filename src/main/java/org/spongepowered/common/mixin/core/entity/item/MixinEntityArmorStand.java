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

import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;

import java.util.List;

@Mixin(EntityArmorStand.class)
public abstract class MixinEntityArmorStand extends MixinEntityLivingBase implements ArmorStand {

    @Shadow public abstract boolean getShowArms();
    @Shadow public abstract boolean hasNoBasePlate();
    @Shadow public abstract boolean hasNoGravity();
    @Shadow protected abstract void setNoBasePlate(boolean p_175426_1_);
    @Shadow protected abstract void setNoGravity(boolean p_175425_1_);

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        manipulators.add(getBodyPartRotationalData());
    }

    @Override
    public boolean isSmall() {
        return (this.dataWatcher.getWatchableObjectByte(10) & 1) != 0;
    }

    @Override
    public void setSmall(boolean small) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(10);
        this.dataWatcher.updateObject(10, (byte) (small ? (b0 | 1) : (b0 & -2)));
    }

    @Override
    public boolean doesShowArms() {
        return this.getShowArms();
    }

    @Override
    public void setShowArms(boolean showArms) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(10);
        this.dataWatcher.updateObject(10, (byte) (showArms ? (b0 | 4) : (b0 & -5)));
    }

    @Override
    public boolean hasBasePlate() {
        return !this.hasNoBasePlate();
    }

    @Override
    public void setHasBasePlate(boolean baseplate) {
        this.setNoBasePlate(!baseplate);
    }

    @Override
    public boolean hasGravity() {
        return !this.hasNoGravity();
    }

    @Override
    public void setGravity(boolean gravity) {
        this.setNoGravity(!gravity);
    }
}
