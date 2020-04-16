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
package org.spongepowered.server.mixin.core.server.dedicated;

import static org.spongepowered.common.SpongeImpl.MINECRAFT_VERSION;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.server.SpongeVanilla;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin_Vanilla extends MinecraftServer {

    @Shadow private PropertyManager settings;

    @Shadow public abstract String getMotd();

    @SuppressWarnings("NullableProblems") @com.google.inject.Inject private static SpongeVanilla vanilla$spongeVanilla;
    @SuppressWarnings("NullableProblems") @com.google.inject.Inject private static SpongeScheduler vanilla$scheduler;


    @SuppressWarnings("ConstantConditions")
    public DedicatedServerMixin_Vanilla() { // Ignored, ditched
        super(null, null, null, null, null, null, null);
    }

    @Inject(method = "init()Z",
        at = @At(
            value = "INVOKE_STRING",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V",
            args = "ldc=Loading properties",
            remap = false))
    private void vanilla$onServerLoad(CallbackInfoReturnable<Boolean> ci) throws Exception {
        vanilla$spongeVanilla.preInitialize();
    }

    @Inject(method = "init()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/DedicatedPlayerList;<init>(Lnet/minecraft/server/dedicated/DedicatedServer;)V",
            shift = At.Shift.BEFORE))
    private void vanilla$onServerInitialize(CallbackInfoReturnable<Boolean> ci) {
        if (this.getFolderName() == null) {
            this.setFolderName(this.settings.getStringProperty("level-name", "world"));
        }

        vanilla$spongeVanilla.initialize();
        ServerStatusResponse statusResponse = getServerStatusResponse();
        statusResponse.setServerDescription(new TextComponentString(this.getMotd()));
        statusResponse.setVersion(
                new ServerStatusResponse.Version(MINECRAFT_VERSION.getName(), MINECRAFT_VERSION.getProtocol()));
        this.applyServerIconToResponse(statusResponse);
    }

    @Inject(method = "init()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/PropertyManager;setProperty(Ljava/lang/String;Ljava/lang/Object;)V",
            ordinal = 2,
            shift = At.Shift.AFTER))
    private void vanilla$callServerAboutToStart(CallbackInfoReturnable<Boolean> ci) {
        vanilla$spongeVanilla.onServerAboutToStart();
    }

    @Inject(method = "init()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/DedicatedServer;loadAllWorlds(Ljava/lang/String;Ljava/lang/String;JLnet/minecraft/world/WorldType;Ljava/lang/String;)V",
            shift = At.Shift.AFTER))
    private void vanilla$callServerStarting(CallbackInfoReturnable<Boolean> ci) {
        vanilla$spongeVanilla.onServerStarting();
    }

    @Inject(method = "updateTimeLightAndEntities", at = @At("RETURN"))
    private void vanilla$onTick(CallbackInfo ci) {
        vanilla$scheduler.tickSyncScheduler();
    }

    @Inject(method = "systemExitNow", at = @At(value = "HEAD"))
    private void vanilla$callServerStopped(CallbackInfo ci) throws Exception {
        vanilla$spongeVanilla.onServerStopped();
    }

}
