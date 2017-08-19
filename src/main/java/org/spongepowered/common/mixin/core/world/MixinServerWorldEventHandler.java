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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinServerWorldEventHandler;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import javax.annotation.Nullable;

@Mixin(ServerWorldEventHandler.class)
public abstract class MixinServerWorldEventHandler implements IMixinServerWorldEventHandler {

    @Shadow @Final private WorldServer world;
    @Shadow @Final private MinecraftServer mcServer;

    @Redirect(method = "playSoundToAllNearExcept", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"), expect = 0, require = 0)
    private int getDimensionForPlayingSound(DimensionType dimensionType) {
        return ((IMixinWorldServer) this.world).getDimensionId();
    }

    @Redirect(method = "playEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"), expect = 0, require = 0)
    private int getDimensionForSoundEffects(DimensionType dimensionType) {
        return ((IMixinWorldServer) this.world).getDimensionId();
    }

    @Override
    public void playCustomSoundToAllNearExcept(@Nullable EntityPlayer player, String soundIn, SoundCategory category, double x, double y, double z,
            float volume, float pitch) {
        this.mcServer.getPlayerList().sendToAllNearExcept(player, x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D,
                ((IMixinWorldServer) this.world).getDimensionId(), new SPacketCustomSound(soundIn, category, x, y, z, volume, pitch));
    }

    @Inject(method = "onEntityRemoved", at = @At("RETURN"))
    public void onSpongeEntityRemoved(Entity entityIn, CallbackInfo ci) {
        ((IMixinEntity) entityIn).setActiveChunk(null);
    }
}
