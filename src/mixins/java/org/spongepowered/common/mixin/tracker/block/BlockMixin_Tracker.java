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
package org.spongepowered.common.mixin.tracker.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.block.TrackedBlockBridge;
import org.spongepowered.common.launch.Launcher;

@Mixin(Block.class)
public abstract class BlockMixin_Tracker implements TrackedBlockBridge {

    // @formatter:off
    @Shadow public static void shadow$spawnDrops(final BlockState state, final World worldIn, final BlockPos pos) {
        throw new IllegalStateException("untransformed shadow");
    }
    // @formatter:on
    private boolean tracker$hasNeighborLogicOverridden = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tracker$initializeTrackerOptimizations(final Block.Properties properties, final CallbackInfo ci) {
        // neighborChanged
        try {
            final String mapping = Launcher.getInstance().isDeveloperEnvironment() ? "neighborChanged" : "func_220069_a";
            final Class<?>[] argTypes = {net.minecraft.block.BlockState.class, net.minecraft.world.World.class, BlockPos.class, Block.class, BlockPos.class, boolean.class};
            final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            this.tracker$hasNeighborLogicOverridden = !clazz.equals(Block.class);
        } catch (final Throwable e) {
            if (e instanceof NoClassDefFoundError) {
                // fall back to checking if class equals Block.
                // Fixes https://github.com/SpongePowered/SpongeForge/issues/2770
                //noinspection EqualsBetweenInconvertibleTypes
                this.tracker$hasNeighborLogicOverridden = !this.getClass().equals(Block.class);
            }
        }
    }

    @Override
    public boolean bridge$overridesNeighborNotificationLogic() {
        return this.tracker$hasNeighborLogicOverridden;
    }
}
