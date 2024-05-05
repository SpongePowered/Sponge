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
package org.spongepowered.forge.mixin.tracker.world.level.block;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.RegistryBackedTrackableBridge;
import org.spongepowered.forge.mixin.tracker.world.level.block.state.BlockBehaviorMixin_Forge_Tracker;

@Mixin(Block.class)
public abstract class BlockMixin_Forge_Tracker extends BlockBehaviorMixin_Forge_Tracker implements RegistryBackedTrackableBridge<Block> {

    //@formatter:off
    @Shadow public abstract boolean isRandomlyTicking(BlockState $$0);
    @Shadow public abstract BlockState defaultBlockState();
    //@formatter:on

    @Override
    protected void forgeTracker$initializeTrackingState(CallbackInfoReturnable<ResourceLocation> cir) {
        // TODO Not the best check but the tracker options only matter during block ticks...
        if (this.isRandomlyTicking(this.defaultBlockState())) {
            this.bridge$refreshTrackerStates();
        }
    }
}
