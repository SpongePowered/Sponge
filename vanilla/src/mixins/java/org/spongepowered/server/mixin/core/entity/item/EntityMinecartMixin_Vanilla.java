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
package org.spongepowered.server.mixin.core.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.common.bridge.entity.item.EntityMinecartBridge;
import org.spongepowered.common.util.Constants;

@Mixin(EntityMinecart.class)
public abstract class EntityMinecartMixin_Vanilla extends Entity {

    public EntityMinecartMixin_Vanilla(World worldIn) {
        super(worldIn);
    }

    @ModifyConstant(method = "moveDerailedMinecart",
        constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD, ordinal = 0))
    private double onAirX(double defaultValue) {
        return ((EntityMinecartBridge) this).bridge$getAirboneVelocityModifier().getX();
    }

    @ModifyConstant(method = "moveDerailedMinecart",
        constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD, ordinal = 1))
    private double onAirY(double defaultValue) {
        return ((EntityMinecartBridge) this).bridge$getAirboneVelocityModifier().getY();
    }

    @ModifyConstant(method = "moveDerailedMinecart",
        constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD, ordinal = 2))
    private double onAirZ(double defaultValue) {
        return ((EntityMinecartBridge) this).bridge$getAirboneVelocityModifier().getZ();
    }

}
