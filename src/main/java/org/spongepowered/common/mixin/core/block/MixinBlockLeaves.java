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

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.LeafDecayEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

@NonnullByDefault
@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends MixinBlock {

    @Inject(method = "updateTick", at = @At(value = "INVOKE", target = "net/minecraft/block/BlockLeaves.destroy (Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callLeafDecay(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci, byte b0, int i, int j, int k, int l, byte b1, int i1, int j1, int k1) {
        Location block = new Location((Extent) worldIn, VecHelper.toVector(pos));
        BlockSnapshot postChange = block.getSnapshot();
        postChange.setBlockState(BlockTypes.AIR.getDefaultState());
        final LeafDecayEvent event = SpongeEventFactory.createLeafDecay(Sponge.getGame(), null, block, postChange); //TODO Fix null cause
        Sponge.getGame().getEventManager().post(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }
}
