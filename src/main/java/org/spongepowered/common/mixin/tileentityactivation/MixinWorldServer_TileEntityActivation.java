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

import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.mixin.core.world.MixinWorld;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;

@Mixin(value = WorldServer.class, priority = 1300)
public abstract class MixinWorldServer_TileEntityActivation extends MixinWorld {

    @Override
    public void spongeTileEntityActivation() {
        TileEntityActivation.activateTileEntities((WorldServer) (Object) this);
    }

    /**
     * Injects into Sponge added injection for updating tile entities, since we need to
     * declare the tile entity to be updateable based on the activation range.
     *
     * @param tile The tile ticking
     * @param ci The callback
     */
    @Inject(method = "updateTileEntity", at = @At("HEAD"), cancellable = true, remap = false)
    public void onUpdateTileEntityHead(ITickable tile, CallbackInfo ci) {
        final net.minecraft.tileentity.TileEntity tileEntity = (net.minecraft.tileentity.TileEntity) tile;
        final boolean canUpdate = TileEntityActivation.checkIfActive(tileEntity);

        if (!canUpdate) {
            ((IModData_Activation) tileEntity).incrementSpongeTicksExisted();
            ((IModData_Activation) tileEntity).inactiveTick();
            ci.cancel();
        }
    }
}
