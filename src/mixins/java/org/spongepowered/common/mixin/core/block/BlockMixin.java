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

import co.aikar.timings.Timing;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import co.aikar.timings.sponge.SpongeTimings;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import org.spongepowered.common.util.ReflectionUtil;

@Mixin(value = Block.class)
public abstract class BlockMixin implements BlockBridge, TrackableBridge, TimingBridge {

    private final boolean impl$isVanilla = this.getClass().getName().startsWith("net.minecraft.");
    private final boolean impl$hasCollideLogic = ReflectionUtil.isStepOnDeclared(this.getClass());
    private final boolean impl$hasCollideWithStateLogic = ReflectionUtil.isEntityInsideDeclared(this.getClass());
    @Nullable private Timing impl$timing;

    /**
     * We captured the dye color when creating the Block.Properties.
     * As the Properties objects are discarded we transfer it over to the Block itself now.
     */

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpSpongeFields(final Block.Properties properties, final CallbackInfo ci) {
        ((DyeColorBlockBridge) this).bridge$setDyeColor(((DyeColorBlockBridge)properties).bridge$getDyeColor().orElse(null));
    }

/*
    @Inject(method = "spawnAsEntity",
            at = @At(value = "NEW", target = "net/minecraft/entity/item/ItemEntity"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT,
            require = 0,
            expect = 0
    )
    private static void impl$throwConstructPreEvent(
            final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack, final CallbackInfo ci,
            final float unused, final double xOffset, final double yOffset, final double zOffset) {
        if (!ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            return;
        }
        final double xPos = (double) pos.getX() + xOffset;
        final double yPos = (double) pos.getY() + yOffset;
        final double zPos = (double) pos.getZ() + zOffset;
        // Go ahead and throw the construction event
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(worldIn.getBlockState(pos));
            final ConstructEntityEvent.Pre eventPre = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), ServerLocation.of((ServerWorld) worldIn, xPos, yPos, zPos), new Vector3d(0, 0, 0), EntityTypes.ITEM.get());
            SpongeCommon.postEvent(eventPre);
            if (eventPre.isCancelled()) {
                ci.cancel();
            }
        }
    }
*/

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

    @Override
    public Timing bridge$timings() {
        if (this.impl$timing == null) {
            this.impl$timing = SpongeTimings.blockTiming((BlockType) this);
        }
        return this.impl$timing;
    }
}
