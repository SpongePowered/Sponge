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
package org.spongepowered.common.mixin.tileentityactivation;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.ActivationCapability;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;

@Mixin(Chunk.class)
public class ChunkMixin_TileEntityActivation {

    @Shadow @Final private World world;

    @Inject(method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", at = @At("RETURN"))
    private void tileActivationImpl$onAddTileEntityActivateCheck(
        final BlockPos pos, final TileEntity tileEntityIn, final CallbackInfo ci) {
        if (tileEntityIn.getWorld() == null) {
            tileEntityIn.setWorld(this.world);
        }
        if (!(tileEntityIn instanceof ITickable)) {
            return;
        }

        if (((WorldInfoBridge) this.world.getWorldInfo()).bridge$isValid()) {
            final ActivationCapability spongeTile = (ActivationCapability) tileEntityIn;
            final ChunkBridge spongeChunk = (ChunkBridge) this;
            if (spongeChunk.bridge$isPersistedChunk()) {
                // always activate TE's in persisted chunks
                spongeTile.activation$setDefaultActivationState(true);
                return;
            }
            final SpongeTileEntityType tileType = (SpongeTileEntityType) ((org.spongepowered.api.block.tileentity.TileEntity) tileEntityIn).getType();
            if (tileType == null) {
                return;
            }
            TileEntityActivation.initializeTileEntityActivationState(tileEntityIn);
            TileEntityActivation.addTileEntityToConfig(this.world, tileType);
        }
    }
}
