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
package org.spongepowered.common.mixin.invalid.core.server.dedicated;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.mixin.core.server.MinecraftServerMixin;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServerMixin {

    @Shadow private boolean guiIsEnabled;

    @Shadow public abstract int getSpawnProtectionSize();

    /**
     * @author Zidane - April 20th, 2015
     * @reason At the time of writing, this turns off the default Minecraft Server GUI that exists in non-headless environment.
     * Reasoning: The GUI console can easily consume a sizable chunk of each CPU core (20% or more is common) on the computer being ran on and has
     * been proven to cause quite a bit of latency issues.
     */
    @Overwrite
    public void setGuiEnabled() {
        //MinecraftServerGui.createServerGui(this);
        this.guiIsEnabled = false;
    }

    @Inject(method = "systemExitNow", at = @At("HEAD"))
    private void postGameStoppingEvent(final CallbackInfo ci) {
        SpongeImpl.postShutdownEvents();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/PropertyManager;getIntProperty(Ljava/lang/String;I)I"))
    private int fixWrongDefaultDifficulty(final PropertyManager propertyManager, final String key, final int defaultValue) {
        if ("difficulty".equalsIgnoreCase(key)) {
            return propertyManager.getIntProperty(key, WorldInfo.DEFAULT_DIFFICULTY.getId());
        }

        return propertyManager.getIntProperty(key, defaultValue);
    }



    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerProfileCache;save()V"))
    private void onSave(final PlayerProfileCache cache) {
        ((PlayerProfileCacheBridge) this.getPlayerProfileCache()).bridge$setCanSave(true);
        this.getPlayerProfileCache().save();
        ((PlayerProfileCacheBridge) this.getPlayerProfileCache()).bridge$setCanSave(false);
    }


}
