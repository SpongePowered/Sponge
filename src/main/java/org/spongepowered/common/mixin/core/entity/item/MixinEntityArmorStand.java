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
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;

@Mixin(EntityArmorStand.class)
@Implements(@Interface(iface = ArmorStand.class, prefix = "armor$"))
public abstract class MixinEntityArmorStand extends MixinEntityLivingBase {

    @Shadow public abstract boolean isSmall();
    @Shadow public abstract boolean getShowArms();
    @Shadow public abstract boolean hasNoBasePlate();
    @Shadow public abstract boolean hasNoGravity();
    @Shadow protected abstract void setNoBasePlate(boolean p_175426_1_);
    @Shadow protected abstract void setNoGravity(boolean p_175425_1_);
    @Shadow public abstract void setSmall(boolean p_175420_1_);
    @Shadow public abstract void setShowArms(boolean p_175413_1_);

    @Intrinsic
    public boolean armor$isSmall() {
        return this.isSmall();
    }

    @Intrinsic
    public void armor$setSmall(boolean small) {
        this.setSmall(small);
    }

    public boolean armor$doesShowArms() {
        return this.getShowArms();
    }

    @Intrinsic
    public void armor$setShowArms(boolean showArms) {
        this.setShowArms(showArms);
    }

    public boolean armor$hasBasePlate() {
        return !this.hasNoBasePlate();
    }

    public void armor$setHasBasePlate(boolean baseplate) {
        this.setNoBasePlate(!baseplate);
    }

    public boolean armor$hasGravity() {
        return !this.hasNoGravity();
    }

    public void armor$setGravity(boolean gravity) {
        this.setNoGravity(!gravity);
    }
}
