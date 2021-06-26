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
package org.spongepowered.vanilla.mixin.core.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.vanilla.VanillaServer;
import org.spongepowered.vanilla.hooks.VanillaPacketHooks;

import java.net.Proxy;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Vanilla implements VanillaServer {

    // @formatter:off
    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract void shadow$detectBundledResources();
    @Shadow public abstract boolean shadow$isRunning();
    @Shadow protected abstract void loadLevel();
    // @formatter:on

    /**
     * Render localized/formatted chat components
     *
     * @param input original component
     */
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void impl$useTranslatingLogger(final Component input, final UUID sender, final CallbackInfo ci) {
        MinecraftServerMixin_Vanilla.LOGGER.info(input);
        ci.cancel();
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    private void vanilla$setPacketHooks(final Thread p_i232576_1_, final RegistryAccess.RegistryHolder p_i232576_2_, final LevelStorageSource.LevelStorageAccess p_i232576_3_,
        final WorldData p_i232576_4_, final PackRepository p_i232576_5_, final Proxy p_i232576_6_, final DataFixer p_i232576_7_,
        final ServerResources p_i232576_8_, final MinecraftSessionService p_i232576_9_, final GameProfileRepository p_i232576_10_,
        final GameProfileCache p_i232576_11_, final ChunkProgressListenerFactory p_i232576_12_, final CallbackInfo ci) {
        PlatformHooks.INSTANCE.setPacketHooks(new VanillaPacketHooks());
    }

    @Inject(method = "stopServer", at = @At(value = "HEAD"), cancellable = true)
    private void vanilla$callStoppingEngineEvent(final CallbackInfo ci) {
        SpongeBootstrap.lifecycle().callStoppingEngineEvent(this);
    }
}
