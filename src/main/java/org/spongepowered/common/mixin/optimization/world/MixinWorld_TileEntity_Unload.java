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
package org.spongepowered.common.mixin.optimization.world;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.world.IMixinWorld;

import java.util.List;

@Mixin(value = World.class, priority = 1001)
public abstract class MixinWorld_TileEntity_Unload implements IMixinWorld {

    @Shadow private boolean processingLoadedTiles;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tickableTileEntities;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tileEntitiesToBeRemoved;

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lorg/spongepowered/common/event/tracking/CauseTracker;switchToPhase(Lorg/spongepowered/common/event/tracking/IPhaseState;Lorg/spongepowered/common/event/tracking/PhaseContext;)V"), remap = false)
    public void onUnloadTileEntitiesStart(CallbackInfo ci) {
        this.processingLoadedTiles = true;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", args = "log=true", target = "Ljava/util/List;isEmpty()Z", ordinal = 0))
    public boolean onUpdateEntitiesUnloadTiles(List<TileEntity> tileList) {
        for (Object tile : tileList)
        {
           SpongeImplHooks.onTileChunkUnload((TileEntity) tile);
        }

        // forge: faster "contains" makes this removal much more efficient
        java.util.Set<TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        remove.addAll(tileEntitiesToBeRemoved);
        this.tickableTileEntities.removeAll(remove);
        this.loadedTileEntityList.removeAll(remove);
        this.tileEntitiesToBeRemoved.clear();
        return true;
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lorg/spongepowered/common/event/tracking/CauseTracker;completePhase(Lorg/spongepowered/common/event/tracking/IPhaseState;)V"), remap = false)
    public void onUnloadTileEntitiesEnd(CallbackInfo ci) {
        this.processingLoadedTiles = false;
    }
}
