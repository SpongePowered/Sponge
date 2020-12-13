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
package org.spongepowered.vanilla.mixin.core.server.integrated;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.vanilla.VanillaServer;

import java.io.File;
import java.net.Proxy;

@Mixin(IntegratedServer.class)
@Implements(@Interface(iface=VanillaServer.class, prefix="server$"))
public abstract class IntegratedServerMixin_Vanilla extends MinecraftServer implements MinecraftServerBridge  {

    @Shadow @Final private Minecraft mc;
    @Shadow @Final private WorldSettings worldSettings;

    @Shadow private boolean isGamePaused;

    public IntegratedServerMixin_Vanilla(File p_i50590_1_, Proxy p_i50590_2_, DataFixer dataFixerIn,
        Commands p_i50590_4_, YggdrasilAuthenticationService p_i50590_5_,
        MinecraftSessionService p_i50590_6_, GameProfileRepository p_i50590_7_,
        PlayerProfileCache p_i50590_8_, IChunkStatusListenerFactory p_i50590_9_,
        String p_i50590_10_) {
        super(p_i50590_1_, p_i50590_2_, dataFixerIn, p_i50590_4_, p_i50590_5_, p_i50590_6_, p_i50590_7_, p_i50590_8_, p_i50590_9_, p_i50590_10_);
    }

    @Override
    public void run() {
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.establishServerServices();
        lifecycle.establishServerFeatures();
        lifecycle.establishCommands();

        lifecycle.callStartingEngineEvent(cast());
        super.run();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void vanilla$callEngineStartedAndLoadedGame(final CallbackInfoReturnable<Boolean> cir) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.callStartedEngineEvent(cast());
    }

    @Override
    public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, JsonElement generatorOptions) {
        cast().getWorldManager().loadAllWorlds(saveName, worldNameIn, seed, type, generatorOptions, true, this.worldSettings, this.mc.gameSettings.difficulty);
    }

    private VanillaServer cast() {
        return (VanillaServer) this;
    }

    @Override
    public boolean bridge$performAutosaveChecks() {
        if (!this.isServerRunning()) {
            return false;
        }

        return !this.isGamePaused;
    }
}
