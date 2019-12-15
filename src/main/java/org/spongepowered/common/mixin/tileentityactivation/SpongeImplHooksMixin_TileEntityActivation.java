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

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.activation.ActivationCapabilityBridge;
import org.spongepowered.common.mixin.plugin.tileentityactivation.TileEntityActivation;

@Mixin(value = SpongeImplHooks.class)
public abstract class SpongeImplHooksMixin_TileEntityActivation {

    /**
     * @author blood - Minecraft 1.14.4
     * @reason Hook TileEntityActivation
     */
    @Overwrite(remap = false)
    public static boolean shouldTickTile(ITickableTileEntity tickableTileEntity) {
        final TileEntity tileEntity = (TileEntity) tickableTileEntity;
        final boolean canUpdate = TileEntityActivation.checkIfActive(tileEntity);

        if (!canUpdate) {
            ((ActivationCapabilityBridge) tileEntity).activation$incrementSpongeTicksExisted();
            ((ActivationCapabilityBridge) tileEntity).activation$inactiveTick();
            return false;
        }
        return true;
    }
}
