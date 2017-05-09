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
package org.spongepowered.common.world.border;

import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;

public final class PlayerBorderListener implements IBorderListener {

    private final int dimensionId;
    private final MinecraftServer server;

    public PlayerBorderListener(MinecraftServer server, int dimensionId) {
        this.server = server;
        this.dimensionId = dimensionId;
    }

    @Override
    public void onSizeChanged(WorldBorder border, double newSize) {
        this.server.getPlayerList()
                .sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_SIZE), this.dimensionId);
    }

    @Override
    public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time) {
        this.server.getPlayerList()
                .sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.LERP_SIZE), this.dimensionId);
    }

    @Override
    public void onCenterChanged(WorldBorder border, double x, double z) {
        this.server.getPlayerList()
                .sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_CENTER), this.dimensionId);
    }

    @Override
    public void onWarningTimeChanged(WorldBorder border, int newTime) {
        this.server.getPlayerList()
                .sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_TIME), this.dimensionId);
    }

    @Override
    public void onWarningDistanceChanged(WorldBorder border, int newDistance) {
        this.server.getPlayerList()
                .sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_BLOCKS),
                        this.dimensionId);
    }

    @Override
    public void onDamageAmountChanged(WorldBorder border, double newAmount) {
    }

    @Override
    public void onDamageBufferChanged(WorldBorder border, double newSize) {
    }

}
