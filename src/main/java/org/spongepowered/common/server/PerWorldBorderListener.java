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
package org.spongepowered.common.server;

import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;

public final class PerWorldBorderListener implements IBorderListener {

    private final ServerWorld world;

    public PerWorldBorderListener(ServerWorld world) {
        this.world = world;
    }

    @Override
    public void onBorderSizeSet(final WorldBorder border, final double newSize) {
        this.sendWorldBorderActionPacket(border, SWorldBorderPacket.Action.SET_SIZE);
    }

    @Override
    public void onBorderSizeLerping(final WorldBorder border, final double oldSize, final double newSize, final long time) {
        this.sendWorldBorderActionPacket(border, SWorldBorderPacket.Action.LERP_SIZE);
    }

    @Override
    public void onBorderCenterSet(final WorldBorder border, final double x, final double z) {
        this.sendWorldBorderActionPacket(border, SWorldBorderPacket.Action.SET_CENTER);
    }

    @Override
    public void onBorderSetWarningTime(final WorldBorder border, final int newTime) {
        this.sendWorldBorderActionPacket(border, SWorldBorderPacket.Action.SET_WARNING_TIME);
    }

    @Override
    public void onBorderSetWarningBlocks(final WorldBorder border, final int newDistance) {
        this.sendWorldBorderActionPacket(border, SWorldBorderPacket.Action.SET_WARNING_BLOCKS);
    }

    @Override
    public void onBorderSetDamagePerBlock(final WorldBorder border, final double newAmount) {
    }

    @Override
    public void onBorderSetDamageSafeZOne(final WorldBorder border, final double newSize) {
    }

    private void sendWorldBorderActionPacket(final WorldBorder border, final SWorldBorderPacket.Action action) {
        final SWorldBorderPacket packet = new SWorldBorderPacket(border, action);
        this.world.players().forEach(player -> player.connection.send(packet));
    }
}
