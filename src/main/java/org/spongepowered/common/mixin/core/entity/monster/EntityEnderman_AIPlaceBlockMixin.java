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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(EndermanEntity.PlaceBlockGoal.class)
public abstract class EntityEnderman_AIPlaceBlockMixin extends Goal {

    @Shadow @Final private EndermanEntity enderman;

    /**
     * @author gabizou - April 13th, 2018
     *  @reason - Due to Forge's changes, there's no clear redirect or injection
     *  point where Sponge can add the griefer checks. The original redirect aimed
     *  at the gamerule check, but this can suffice for now.
     * @author gabizou - July 26th, 2018
     * @reason Adds sanity check for calling a change block event pre
     *
     * @param entityEnderman The enderman doing griefing
     * @return The block state that can be placed, or null if the enderman can't grief
     */
    @Redirect(
        method = "shouldExecute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/monster/EntityEnderman;getHeldBlockState()Lnet/minecraft/block/state/IBlockState;"
        )
    )
    @Nullable
    private BlockState onCanGrief(final EndermanEntity entityEnderman) {
        final BlockState heldBlockState = entityEnderman.getHeldBlockState();
        return ((GrieferBridge) this.enderman).bridge$CanGrief() ? heldBlockState : null;
    }

    /**
     * @author gabizou - July 26th, 2018
     * @reason Makes enderman check for block changes before they can place their blocks.
     * This allows plugins to cancel the event regardless without issue.
     *
     * @param blockState The block state being placed
     * @param world The world
     * @param pos the position
     * @param toPlace The block being placed
     * @param old The old state
     * @param state The new state
     * @return True if the state is a full cube, and the event didnt get cancelled
     */
    @Redirect(method = "canPlaceBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;isFullCube()Z"))
    private boolean onUpdateCancel(final BlockState blockState, final World world, final BlockPos pos, final Block toPlace,
        final BlockState old, final BlockState state) {
        if (state.isFullCube()) {
            if (ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
                final Location<org.spongepowered.api.world.World> location =
                    new Location<org.spongepowered.api.world.World>((org.spongepowered.api.world.World) world, VecHelper.toVector3i(pos));
                final List<Location<org.spongepowered.api.world.World>> list = new ArrayList<>(1);
                list.add(location);
                final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
                final ChangeBlockEvent.Pre event = SpongeEventFactory.createChangeBlockEventPre(cause, list);
                return !SpongeImpl.postEvent(event);
            }
            return true;
        }
        return false;
    }
}
