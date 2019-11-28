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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;

@Mixin(FallingBlock.class)
public class BlockFallingMixin {

    @Redirect(method = "checkFallable",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"
        )
    )
    private boolean impl$CheckIfAreaIsLoadedAndIfThrownEventIsntCancelled(final World world, final BlockPos pos, final BlockPos to) {
        if (world.isAreaLoaded(pos, to) && !((WorldBridge) world).bridge$isFake()) {

            final BlockPos actualPos = pos.add(32, 32, 32);
            final EntityType fallingBlock = EntityTypes.FALLING_BLOCK;
            final Vector3d position = new Vector3d(actualPos.getX() + 0.5D, actualPos.getY(), actualPos.getZ() + 0.5D);
            final BlockSnapshot snapshot = ((org.spongepowered.api.world.World) world).createSnapshot(actualPos.getX(), actualPos.getY(), actualPos.getZ());
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(snapshot);
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.FALLING_BLOCK);
                final Transform<org.spongepowered.api.world.World> worldTransform = new Transform<>((org.spongepowered.api.world.World) world, position);
                final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), fallingBlock, worldTransform);
                SpongeImpl.postEvent(event);
                return !event.isCancelled();
            }

        }
        return false;
    }

}
