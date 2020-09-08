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
package org.spongepowered.common.mixin.core.server.dedicated;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.ServerPropertiesProvider;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.mixin.core.server.MinecraftServerMixin;

import java.io.File;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServerMixin {

    public DedicatedServerMixin(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setServerOnGame(final File p_i50720_1_, final ServerPropertiesProvider p_i50720_2_, final DataFixer dataFixerIn,
        final YggdrasilAuthenticationService p_i50720_4_, final MinecraftSessionService p_i50720_5_, final GameProfileRepository p_i50720_6_,
        final PlayerProfileCache p_i50720_7_, final IChunkStatusListenerFactory p_i50720_8_, final String p_i50720_9_, final CallbackInfo ci) {

        SpongeCommon.getGame().setServer(this);
        p_i50720_7_.load();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerProfileCache;save()V"))
    private void onSave(final PlayerProfileCache cache) {
        ((PlayerProfileCacheBridge) cache).bridge$setCanSave(true);
        cache.save();
        ((PlayerProfileCacheBridge) cache).bridge$setCanSave(false);
    }

}
