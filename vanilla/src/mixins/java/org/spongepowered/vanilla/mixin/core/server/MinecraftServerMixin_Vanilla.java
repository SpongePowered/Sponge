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

import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SessionLockException;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.vanilla.VanillaServer;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Vanilla implements VanillaServer {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract Iterable<ServerWorld> shadow$getWorlds();
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract CustomServerBossInfoManager shadow$getCustomBossEvents();

    @Redirect(method = "loadWorlds",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;func_212504_a(Lnet/minecraft/world/server/ServerWorld;)V"))
    private void vanilla$onSaveHandlerBeingSetToPlayerList(final PlayerList playerList, final ServerWorld p_212504_1_) {
        playerList.func_212504_a(p_212504_1_);
        ((SpongeUserManager) this.getUserManager()).init();
    }

    @Inject(method = "stopServer", at = @At(value = "HEAD"), cancellable = true)
    private void vanilla$callStoppingEngineEvent(CallbackInfo ci) {
        SpongeBootstrap.getLifecycle().callStoppingEngineEvent(this);
    }

    /**
     * @author Zidane
     * @reason Per-world saving
     */
    @Overwrite
    public boolean save(boolean suppressLog, boolean flush, boolean forced) {
        for (ServerWorld serverworld : this.shadow$getWorlds()) {
            if (!suppressLog) {
                LOGGER.info("Saving chunks for world '{}'/{}", ((org.spongepowered.api.world.server.ServerWorld) serverworld).getKey(),
                    ((org.spongepowered.api.world.server.ServerWorld) serverworld).getDimension().getType().getKey());
            }

            try {
                serverworld.save((IProgressUpdate)null, flush, serverworld.disableLevelSaving && !forced);
            } catch (SessionLockException sessionlockexception) {
                LOGGER.warn(sessionlockexception.getMessage());
            }

            // Sponge Start - per-world world border
            serverworld.getWorldBorder().copyTo(serverworld.getWorldInfo());

            // TODO Minecraft 1.14 - Per-world boss events
            if (serverworld.getDimension().getType() == DimensionType.OVERWORLD) {
                serverworld.getWorldInfo().setCustomBossEvents(this.shadow$getCustomBossEvents().write());
            }

            // Sponge Start - Save our NBT compound with each world
            serverworld.getSaveHandler().saveWorldInfoWithPlayer(serverworld.getWorldInfo(), this.shadow$getPlayerList().getHostPlayerData());
        }

        return true;
    }
}
