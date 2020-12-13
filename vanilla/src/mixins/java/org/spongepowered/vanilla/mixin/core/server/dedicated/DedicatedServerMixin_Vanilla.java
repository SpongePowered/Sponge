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
package org.spongepowered.vanilla.mixin.core.server.dedicated;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.vanilla.VanillaServer;

import java.io.File;
import java.net.Proxy;

@Mixin(DedicatedServer.class)
@Implements(@Interface(iface = VanillaServer.class, prefix = "server$"))
public abstract class DedicatedServerMixin_Vanilla extends MinecraftServer {

    public DedicatedServerMixin_Vanilla(final File p_i50590_1_, final Proxy p_i50590_2_, final DataFixer dataFixerIn,
        final Commands p_i50590_4_, final YggdrasilAuthenticationService p_i50590_5_,
        final MinecraftSessionService p_i50590_6_, final GameProfileRepository p_i50590_7_,
        final PlayerProfileCache p_i50590_8_, final IChunkStatusListenerFactory p_i50590_9_,
        final String p_i50590_10_) {
        super(p_i50590_1_, p_i50590_2_, dataFixerIn, p_i50590_4_, p_i50590_5_, p_i50590_6_, p_i50590_7_, p_i50590_8_, p_i50590_9_, p_i50590_10_);
    }

    @Override
    public void run() {
        // Save config now that registries have been initialized
        ConfigHandle.setSaveSuppressed(false);

        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.establishServerServices();

        lifecycle.establishRegistries();
        lifecycle.establishDataProviders();

        lifecycle.establishServerFeatures();
        lifecycle.establishCommands();

        lifecycle.callStartingEngineEvent(cast());

        lifecycle.establishDataPackRegistries();
        super.run();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void vanilla$callStartedEngineAndLoadedGame(final CallbackInfoReturnable<Boolean> cir) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.callStartedEngineEvent(cast());

        lifecycle.callLoadedGameEvent();
    }

    @Override
    protected void loadAllWorlds(final String saveName, final String worldNameIn, final long seed, final WorldType type, final JsonElement generatorOptions) {
        cast().getWorldManager().loadAllWorlds(saveName, worldNameIn, seed, type, generatorOptions, false, null, Difficulty.NORMAL);
    }

    private VanillaServer cast() {
        return (VanillaServer) this;
    }
}
