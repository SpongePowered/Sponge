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
package org.spongepowered.vanilla.mixin.core.world.entity.player;

import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.util.DamageEventUtil;

@Mixin(Player.class)
public class PlayerMixin_Vanilla_Attack_Impl {
    private DamageEventUtil.Attack<Player> attackImpl$attack;

    /**
     * Captures the crit multiplier as a function
     */
    @ModifyVariable(method = "attack", ordinal = 2,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSprinting()Z", ordinal = 1),
                           to = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDist:F")),
            at = @At(value = "JUMP", opcode = Opcodes.IFEQ)
    )
    public boolean attackImpl$critHook(final boolean isCritical) {
        // if (isCritical) damage *= 1.5F;
        if (isCritical) {
            final var bonusDamageFunc = DamageEventUtil.provideCriticalAttackFunction(this.attackImpl$attack.sourceEntity(), 1.5);
            this.attackImpl$attack.functions().add(bonusDamageFunc);
        }
        return isCritical;
    }
}
