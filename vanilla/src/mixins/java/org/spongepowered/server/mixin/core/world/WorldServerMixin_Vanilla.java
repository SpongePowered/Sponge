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
package org.spongepowered.server.mixin.core.world;

import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.world.WorldManager;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin_Vanilla extends WorldMixin_Vanilla implements WorldServerBridge {

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/DimensionType;getById(I)Lnet/minecraft/world/DimensionType;"))
    private static DimensionType vanilla$getDimensionType(int dimensionId) {
        return WorldManager.getDimensionType(dimensionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid dimension id: " + dimensionId));
    }

    @Redirect(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"))
    private int vanilla$onGetDimensionIdForWeather(DimensionType type) {
        return bridge$getDimensionId();
    }

    // Prevent wrong weather changes getting sent to players in other (unaffected) dimensions
    // This causes "phantom rain" on the client, sunny and rainy weather at the same time
    @Redirect(method = "updateWeather", require = 4, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/Packet;)V"))
    private void vanilla$onSendWeatherPacket(PlayerList manager, Packet<?> packet) {
        manager.sendPacketToAllPlayersInDimension(packet, bridge$getDimensionId());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int bridge$getDimensionId() {
        return ((WorldInfoBridge) this.worldInfo).bridge$getDimensionId();
    }


    @Override
    int vanillaImpl$updateRainTimeStart(final int newRainTime) {
        if (!((WorldBridge) this).bridge$isFake()) {
            if (this.worldInfo.getRainTime() - 1 != newRainTime) {
                this.bridge$setWeatherStartTime(this.getTotalWorldTime());
            }
        }
        return newRainTime;
    }

    @Override
    int vanillaImpl$updateThunderTimeStart(int newThunderTime) {
        if (!((WorldBridge) this).bridge$isFake()) {
            if (this.worldInfo.getThunderTime() - 1 != newThunderTime) {
                this.bridge$setWeatherStartTime(this.getTotalWorldTime());
            }
        }
        return newThunderTime;
    }
}
