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

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.common.accessor.world.level.border.WorldBorderAccessor;

public final class PlayerOwnBorderListener implements BorderChangeListener {

    private final net.minecraft.server.level.ServerPlayer player;

    public PlayerOwnBorderListener(final net.minecraft.server.level.ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void onBorderSizeSet(final WorldBorder border, final double newSize) {
        this.sendBorderPacket(new ClientboundSetBorderPacket(border, ClientboundSetBorderPacket.Type.SET_SIZE));
    }

    @Override
    public void onBorderSizeLerping(final WorldBorder border, final double oldSize, final double newSize, final long time) {
        this.sendBorderPacket(new ClientboundSetBorderPacket(border, ClientboundSetBorderPacket.Type.LERP_SIZE));
    }

    @Override
    public void onBorderCenterSet(final WorldBorder border, final double x, final double z) {
        this.sendBorderPacket(new ClientboundSetBorderPacket(border, ClientboundSetBorderPacket.Type.SET_CENTER));
    }

    @Override
    public void onBorderSetWarningTime(final WorldBorder border, final int newTime) {
        this.sendBorderPacket(new ClientboundSetBorderPacket(border, ClientboundSetBorderPacket.Type.SET_WARNING_TIME));
    }

    @Override
    public void onBorderSetWarningBlocks(final WorldBorder border, final int newDistance) {
        this.sendBorderPacket(new ClientboundSetBorderPacket(border, ClientboundSetBorderPacket.Type.SET_WARNING_BLOCKS));
    }

    @Override
    public void onBorderSetDamagePerBlock(final WorldBorder border, final double newAmount) {
    }

    @Override
    public void onBorderSetDamageSafeZOne(final WorldBorder border, final double newSize) {
    }
    
    /**
     * This method is for cleaning up the player reference once they disconnect.
     */
    public void onPlayerDisconnect() {
        ((ServerPlayer) this.player).worldBorder().ifPresent(border -> ((WorldBorderAccessor) border).accessor$listeners().remove(this));
    }

    private void sendBorderPacket(final Packet<?> packet) {
        this.player.connection.send(packet);
    }
}
