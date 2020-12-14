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
import com.mojang.datafixers.DataFixer;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.ServerPropertiesProvider;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.mixin.core.server.MinecraftServerMixin;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MinecraftServerMixin {

    public DedicatedServerMixin(final String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setServerOnGame(Thread p_i232601_1_, DynamicRegistries.Impl p_i232601_2_, SaveFormat.LevelSave p_i232601_3_,
            ResourcePackList p_i232601_4_, DataPackRegistries p_i232601_5_, IServerConfiguration p_i232601_6_, ServerPropertiesProvider p_i232601_7_,
            DataFixer p_i232601_8_, MinecraftSessionService p_i232601_9_, GameProfileRepository p_i232601_10_, PlayerProfileCache p_i232601_11_,
            IChunkStatusListenerFactory p_i232601_12_, CallbackInfo ci) {

        SpongeCommon.getGame().setServer(this);
        p_i232601_11_.load();
    }

    @Redirect(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerProfileCache;save()V"))
    private void onSave(final PlayerProfileCache cache) {
        ((PlayerProfileCacheBridge) cache).bridge$setCanSave(true);
        cache.save();
        ((PlayerProfileCacheBridge) cache).bridge$setCanSave(false);
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void impl$shutdownAsyncScheduler(final CallbackInfo ci) {
        SpongeCommon.getGame().getAsyncScheduler().close();
    }

    @Override
    public boolean bridge$performAutosaveChecks() {
        return this.shadow$isRunning();
    }
}
