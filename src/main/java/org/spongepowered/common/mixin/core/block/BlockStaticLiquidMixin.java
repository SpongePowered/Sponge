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
package org.spongepowered.common.mixin.core.block;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

import java.util.Collections;
import java.util.List;

@Mixin(BlockStaticLiquid.class)
public class BlockStaticLiquidMixin {

    @Redirect(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"
        )
    )
    private boolean impl$CheckEventsBeforeSpreadingFire(final World world, final BlockPos pos, final IBlockState blockState) {
        if (!ShouldFire.CHANGE_BLOCK_EVENT_PRE) { // If we're not throwing events... well..
            if (!((WorldBridge) world).bridge$isFake()) {
                return PhaseTracker.getInstance().setBlockState(((WorldServerBridge) world), pos, blockState, BlockChangeFlags.ALL);
            }
            return world.func_175656_a(pos, blockState);
        }

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(pos);
            frame.pushCause(this);
            final Vector3i target = VecHelper.toVector3i(pos);
            final Location<org.spongepowered.api.world.World> location = new Location<>((org.spongepowered.api.world.World) world, target);
            final List<Location<org.spongepowered.api.world.World>> locations = Collections.singletonList(location);
            final ChangeBlockEvent.Pre event = SpongeEventFactory.createChangeBlockEventPre(frame.getCurrentCause(), locations);
            if (!SpongeImpl.postEvent(event)) {
                return PhaseTracker.getInstance().setBlockState((WorldServerBridge) world, pos, blockState, BlockChangeFlags.ALL);
            }
            return false;
        }

    }
}
