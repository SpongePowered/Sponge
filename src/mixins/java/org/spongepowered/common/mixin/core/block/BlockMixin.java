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

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.ReflectionUtil;
import org.spongepowered.math.vector.Vector3d;

@Mixin(value = Block.class)
public abstract class BlockMixin implements BlockBridge, TrackableBridge {

    private final boolean impl$isVanilla = this.getClass().getName().startsWith("net.minecraft.");
    private final boolean impl$hasCollideLogic = ReflectionUtil.isStepOnDeclared(this.getClass());
    private final boolean impl$hasCollideWithStateLogic = ReflectionUtil.isEntityInsideDeclared(this.getClass());

    /**
     * We captured the dye color when creating the Block.Properties.
     * As the Properties objects are discarded we transfer it over to the Block itself now.
     */

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpSpongeFields(final Block.Properties properties, final CallbackInfo ci) {
        ((DyeColorBlockBridge) this).bridge$setDyeColor(
            ((DyeColorBlockBridge) properties).bridge$getDyeColor().orElse(null));
    }


    @Inject(
        method = "popResource",
        at = @At(
            value = "NEW",
            target = "Lnet/minecraft/world/entity/item/ItemEntity;<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILSOFT,
        require = 0,
        expect = 0
    )
    private static void impl$throwConstructPreEvent(
        final Level level, final BlockPos pos, final ItemStack stack, final CallbackInfo ci,
        final float unused, final double xOffset, final double yOffset, final double zOffset
    ) {
        if (!ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            return;
        }
        final double xPos = (double) pos.getX() + xOffset;
        final double yPos = (double) pos.getY() + yOffset;
        final double zPos = (double) pos.getZ() + zOffset;
        // Go ahead and throw the construction event
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(level.getBlockState(pos));
            final ConstructEntityEvent.Pre eventPre = SpongeEventFactory.createConstructEntityEventPre(
                frame.currentCause(), ServerLocation.of((ServerWorld) level, xPos, yPos, zPos), new Vector3d(0, 0, 0),
                EntityTypes.ITEM.get()
            );
            SpongeCommon.post(eventPre);
            if (eventPre.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Override
    public boolean bridge$isVanilla() {
        return this.impl$isVanilla;
    }

    @Override
    public boolean bridge$hasCollideLogic() {
        return this.impl$hasCollideLogic;
    }

    @Override
    public boolean bridge$hasCollideWithStateLogic() {
        return this.impl$hasCollideWithStateLogic;
    }
}
