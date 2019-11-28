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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.util.Constants;

@Mixin({ LivingEntity.class, FireballEntity.class, WitherSkullEntity.class, SmallFireballEntity.class })
public abstract class GrieferBridgeMixin implements GrieferBridge {

    private boolean griefer$canGrief = true;

    @Override
    public boolean bridge$CanGrief() {
        return this.griefer$canGrief;
    }

    @Override
    public void bridge$SetCanGrief(final boolean grief) {
        this.griefer$canGrief = grief;
        if (grief) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
            spongeData.func_74757_a(Constants.Sponge.Entity.CAN_GRIEF, true);
        } else {
            if (((DataCompoundHolder) this).data$hasSpongeCompound()) {
                ((DataCompoundHolder) this).data$getSpongeCompound().func_82580_o(Constants.Sponge.Entity.CAN_GRIEF);
            }
        }
    }

}
