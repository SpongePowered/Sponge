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
package org.spongepowered.common.mixin.optimization.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.optimization.OptimizedMapDataBridge;
import org.spongepowered.common.mixin.core.world.storage.MapStorageAccessor;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_MapOptimization {

    // Sponge re-uses the same MapStorage for all worlds, so we only
    // need to tick it once per server tick
    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void mapOptimization$onEndTickMapOptimization(CallbackInfo ci) {

        final List<WorldSavedData> data;

        // Mods, such as TwilightForest, may add themselves to this list when ticking which will cause a CME.
        if (!SpongeImplHooks.isVanilla()) {
            data = new ArrayList<>(
                ((MapStorageAccessor) (WorldManager.getWorldByDimensionId(0).orElse(null).func_175693_T())).getLoadedDataList());
        }
        else {
            data = ((MapStorageAccessor) (WorldManager.getWorldByDimensionId(0).orElse(null).func_175693_T())).getLoadedDataList();
        }

        data
            .stream()
            .filter(wsd -> wsd instanceof MapData)
            .forEach(wsd -> ((OptimizedMapDataBridge) wsd).mapOptimizationBridge$tickMap());
    }
}
