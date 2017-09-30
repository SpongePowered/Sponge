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
package org.spongepowered.common.mixin.realtime.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.mixin.realtime.IMixinRealTimeTicking;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends Entity {

    private static final String ENTITY_PLAYER_MP_PORTAL_COOLDOWN_FIELD = "Lnet/minecraft/entity/player/EntityPlayerMP;timeUntilPortal:I";

    public MixinEntityPlayerMP(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "decrementTimeUntilPortal", at = @At(value = "FIELD", target = ENTITY_PLAYER_MP_PORTAL_COOLDOWN_FIELD, opcode = Opcodes.PUTFIELD, ordinal = 0))
    public void fixupPortalCooldown(EntityPlayerMP self, int modifier) {
        int ticks = (int) ((IMixinRealTimeTicking) self.getEntityWorld()).getRealTimeTicks();
        this.timeUntilPortal = Math.max(0, this.timeUntilPortal - ticks);
    }

}
