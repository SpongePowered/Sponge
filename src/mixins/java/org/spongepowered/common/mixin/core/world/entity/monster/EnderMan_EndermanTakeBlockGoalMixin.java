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
package org.spongepowered.common.mixin.core.world.entity.monster;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;

import javax.annotation.Nullable;

@Mixin(targets = "net/minecraft/world/entity/monster/EnderMan$EndermanTakeBlockGoal")
public abstract class EnderMan_EndermanTakeBlockGoalMixin extends Goal {

    // @formatter:off
    @Shadow @Final private EnderMan enderman; //enderman
    // @formatter:on

    /**
     * @author gabizou - April 13th, 2018
     *  @reason - Due to Forge's changes, there's no clear redirect or injection
     *  point where Sponge can add the griefer checks. The original redirect aimed
     *  at the gamerule check, but this can suffice for now.
     *
     * @param entityEnderman The enderman trying to pick up the block
     * @return The held blockstate, if can grief, or air state if they cannot (the pickup checks if the block state is not null only)
     */
    @Redirect(
        method = "canUse()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/EnderMan;getCarriedBlock()Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    @Nullable
    private BlockState onCanGrief(final EnderMan entityEnderman) {
        final BlockState heldBlockState = entityEnderman.getCarriedBlock();
        return ((GrieferBridge) this.enderman).bridge$canGrief() ? heldBlockState : Blocks.AIR.defaultBlockState();
    }
}
