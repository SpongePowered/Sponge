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

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.vanilla.VanillaServer;

import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin_Vanilla extends MinecraftServer implements MinecraftServerBridge, VanillaServer  {

    // @formatter:off
    @Shadow private boolean paused;
    // @formatter:on

    public IntegratedServerMixin_Vanilla(Thread p_i232576_1_, DynamicRegistries.Impl p_i232576_2_,
            SaveFormat.LevelSave p_i232576_3_, IServerConfiguration p_i232576_4_,
            ResourcePackList p_i232576_5_, Proxy p_i232576_6_, DataFixer p_i232576_7_,
            DataPackRegistries p_i232576_8_, MinecraftSessionService p_i232576_9_,
            GameProfileRepository p_i232576_10_, PlayerProfileCache p_i232576_11_,
            IChunkStatusListenerFactory p_i232576_12_) {
        super(p_i232576_1_, p_i232576_2_, p_i232576_3_, p_i232576_4_, p_i232576_5_, p_i232576_6_, p_i232576_7_, p_i232576_8_, p_i232576_9_,
                p_i232576_10_,
                p_i232576_11_, p_i232576_12_);
    }

    @Inject(method = "initServer", at = @At("HEAD"))
    private void vanilla$runEngineStartLifecycle(final CallbackInfoReturnable<Boolean> cir) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.establishServerServices();

        lifecycle.establishServerFeatures();
        lifecycle.establishCommands();

        lifecycle.establishEngineRegistries(this);
        lifecycle.callStartingEngineEvent(this);
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    private void vanilla$callStartedEngineAndLoadedGame(final CallbackInfoReturnable<Boolean> cir) {
        final SpongeLifecycle lifecycle = SpongeBootstrap.getLifecycle();
        lifecycle.callStartedEngineEvent(this);
    }

    @Override
    public void loadLevel() {
        this.getWorldManager().loadLevel();
    }

    @Override
    public boolean bridge$performAutosaveChecks() {
        if (!this.isRunning()) {
            return false;
        }

        return !this.paused;
    }
}
