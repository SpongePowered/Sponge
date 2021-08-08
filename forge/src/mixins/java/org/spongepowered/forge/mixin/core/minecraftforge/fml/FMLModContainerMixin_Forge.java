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
package org.spongepowered.forge.mixin.core.minecraftforge.fml;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.forge.launch.event.ForgeEventManager;

@Mixin(value = FMLModContainer.class, remap = false)
public abstract class FMLModContainerMixin_Forge extends ModContainerMixin_Forge {

    private static final String forge$pluginModContainerName = "PluginModContainer";

    // @formatter:off
    @Shadow private Object modInstance;
    // @formatter:on

    // TODO Need to figure out a way to make constructMod public, this is a nasty hack
    @Inject(method = "constructMod", at = @At("HEAD"), cancellable = true)
    private void forge$skipConstructModIfNotThisClass(final CallbackInfo ci) {
        if (this.getClass().getSimpleName().equals(FMLModContainerMixin_Forge.forge$pluginModContainerName)) {
            ci.cancel();
        }
    }

    @Inject(method = "constructMod", at = @At("TAIL"))
    private void forge$registerModForSpongeListeners(final CallbackInfo ci) {
        if (this.modInstance != null) {
            ((ForgeEventManager) MinecraftForge.EVENT_BUS).registerListeners(this, this.modInstance);
        }
    }

    @Override
    public Object instance() {
        return this.modInstance;
    }
}
