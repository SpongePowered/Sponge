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
package org.spongepowered.common.mixin.core.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.world.storage.WorldServerMultiAdapterWorldInfo;

@NonnullByDefault
@Mixin(WorldServerMulti.class)
public abstract class MixinWorldServerMulti extends WorldServer {

    public MixinWorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
        super(server, saveHandlerIn, info, dimensionId, profilerIn);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;addListener"
            + "(Lnet/minecraft/world/border/IBorderListener;)V"))
    public void nullRouteBindToDelegateWorldBorder(WorldBorder invoker, IBorderListener listener) {
    }

    private static WorldInfo realWorldInfo;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/storage/ISaveHandler;Lnet/minecraft/world/storage/WorldInfo;ILnet/minecraft/profiler/Profiler;)V"))
    private static ISaveHandler unwrapSaveHandler(ISaveHandler wrappedSaveHandler) {
        if (wrappedSaveHandler instanceof WorldServerMultiAdapterWorldInfo) {
            realWorldInfo = ((WorldServerMultiAdapterWorldInfo) wrappedSaveHandler).getRealWorldInfo();
            return ((WorldServerMultiAdapterWorldInfo) wrappedSaveHandler).getProxySaveHandler();
        } else {
            return wrappedSaveHandler;
        }
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/storage/ISaveHandler;Lnet/minecraft/world/storage/WorldInfo;ILnet/minecraft/profiler/Profiler;)V"))
    private static WorldInfo replaceWorldInfo(WorldInfo derivedInfo) {
        if (realWorldInfo != null) {
            return realWorldInfo;
        } else {
            return derivedInfo;
        }
    }

    /**
     * @author bloodmc
     * @reason Uses our own save handler instead of delegating to
     * the "parent" world since multi-world support changes the
     * structure.
     *
     * @throws MinecraftException An exception
     */
    @Override
    @Overwrite
    protected void saveLevel() throws MinecraftException {
        // this.perWorldStorage.saveAllData();
        // we handle all saving including perWorldStorage in WorldServer.saveLevel. This needs to be disabled since we
        // use a seperate save handler for each world. Each world folder needs to generate a corresponding
        // level.dat for plugins that require it such as MultiVerse.
        super.saveLevel();
    }

    /**
     * @author Zidane - June 5th 2016
     * @reason For Sponge multi-world, each world gets its own WorldBorder
     */
    @Inject(method = "init", at = @At("RETURN"))
    public void onInit(CallbackInfoReturnable<World> cir) {
        this.getWorldBorder().setCenter(this.worldInfo.getBorderCenterX(), this.worldInfo.getBorderCenterZ());
        this.getWorldBorder().setDamageAmount(this.worldInfo.getBorderDamagePerBlock());
        this.getWorldBorder().setDamageBuffer(this.worldInfo.getBorderSafeZone());
        this.getWorldBorder().setWarningDistance(this.worldInfo.getBorderWarningDistance());
        this.getWorldBorder().setWarningTime(this.worldInfo.getBorderWarningTime());

        if (this.worldInfo.getBorderLerpTime() > 0L) {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize(), this.worldInfo.getBorderLerpTarget(), this.worldInfo.getBorderLerpTime());
        }
        else {
            this.getWorldBorder().setTransition(this.worldInfo.getBorderSize());
        }
    }
}
