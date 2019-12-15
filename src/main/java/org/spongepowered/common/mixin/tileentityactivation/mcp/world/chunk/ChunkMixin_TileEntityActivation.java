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
package org.spongepowered.common.mixin.tileentityactivation.mcp.world.chunk;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.bridge.activation.ActivationCapabilityBridge;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;

@Mixin(Chunk.class)
public abstract class ChunkMixin_TileEntityActivation {

    @Shadow public abstract World shadow$getWorld();

    @Inject(method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", at = @At("RETURN"))
    private void tileEntityActivation$onAddTileEntityActivateCheck(BlockPos pos, TileEntity tileEntity, CallbackInfo ci) {
        if (tileEntity.getWorld() == null) {
            return;
        }
        if (!(tileEntity instanceof ITickableTileEntity)) {
            return;
        }

        if (((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$isValid()) {
            final ActivationCapabilityBridge activationBridge = (ActivationCapabilityBridge) tileEntity;
            final ChunkBridge chunkBridge = (ChunkBridge) this;
            if (chunkBridge.bridge$isPersistedChunk()) {
                // always activate TE's in persisted chunks
                activationBridge.activation$setDefaultActivationState(true);
                return;
            }
            final SpongeTileEntityType tileType = (SpongeTileEntityType) ((org.spongepowered.api.block.entity.BlockEntity) tileEntity).getType();
            TileEntityActivation.initializeTileEntityActivationState(tileEntity);
            TileEntityActivation.addTileEntityToConfig(this.shadow$getWorld(), tileType);
        }
    }
}
