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
package org.spongepowered.vanilla.mixin.core.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.client.MinecraftBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.vanilla.client.VanillaClient;
import org.spongepowered.vanilla.util.WindowUtils;

import java.io.IOException;
import java.io.InputStream;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin_Vanilla implements MinecraftBridge, VanillaClient {

    @Override
    public ClientType bridge$getClientType() {
        return ClientType.SPONGE_VANILLA;
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void vanilla$establishRegistriesAndStartingEngine(CallbackInfo ci) {
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();

        lifecycle.establishClientRegistries(this);
        lifecycle.callStartingEngineEvent(this);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/VanillaPackResources;getResource(Lnet/minecraft/server/packs/PackType;Lnet/minecraft/resources/ResourceLocation;)Ljava/io/InputStream;"))
    private InputStream vanilla$skipLoadingVanillaIcon(final VanillaPackResources vanillaPackResources, final PackType param0, final ResourceLocation param1) {
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setIcon(Ljava/io/InputStream;Ljava/io/InputStream;)V"))
    private void vanilla$useSpongeIcon(final Window window, final InputStream param0, final InputStream param1) {
        try (final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("spongie_icon.png")) {
            WindowUtils.setWindowIcon(window.getWindow(), stream);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
