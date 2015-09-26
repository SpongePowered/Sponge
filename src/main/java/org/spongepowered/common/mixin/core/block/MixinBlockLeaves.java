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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTransaction;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.DecayBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

@NonnullByDefault
@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends MixinBlock {

    @Inject(method = "updateTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockLeaves;destroy(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)V"), cancellable = true)
    public void callLeafDecay(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        Location<World> location =
            new Location<World>((World) worldIn, VecHelper.toVector(pos));
        BlockSnapshot blockOriginal = location.createSnapshot();
        BlockSnapshot blockReplacement = blockOriginal.withState(BlockTypes.AIR.getDefaultState());
        ImmutableList<BlockTransaction> transactions = new ImmutableList.Builder<BlockTransaction>().add(new BlockTransaction(blockOriginal, blockReplacement)).build();
        final DecayBlockEvent event = SpongeEventFactory.createDecayBlockEvent(Sponge.getGame(), Cause.of(worldIn), (World) worldIn, transactions);
        Sponge.getGame().getEventManager().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // TODO implement for tree type, decayable etc.


}
