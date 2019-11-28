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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.common.mixin.core.world.border.WorldBorderAccessor;

public class PlayerOwnBorderListener implements IBorderListener {

    private ServerPlayerEntity player;

    public PlayerOwnBorderListener(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void func_177694_a(WorldBorder border, double newSize) {
        sendBorderPacket(new SWorldBorderPacket(border, SWorldBorderPacket.Action.SET_SIZE));
    }

    @Override
    public void func_177692_a(WorldBorder border, double oldSize, double newSize, long time) {
        sendBorderPacket(new SWorldBorderPacket(border, SWorldBorderPacket.Action.LERP_SIZE));
    }

    @Override
    public void func_177693_a(WorldBorder border, double x, double z) {
        sendBorderPacket(new SWorldBorderPacket(border, SWorldBorderPacket.Action.SET_CENTER));
    }

    @Override
    public void func_177691_a(WorldBorder border, int newTime) {
        sendBorderPacket(new SWorldBorderPacket(border, SWorldBorderPacket.Action.SET_WARNING_TIME));
    }

    @Override
    public void func_177690_b(WorldBorder border, int newDistance) {
        sendBorderPacket(new SWorldBorderPacket(border, SWorldBorderPacket.Action.SET_WARNING_BLOCKS));
    }

    @Override
    public void func_177696_b(WorldBorder border, double newAmount) {
    }

    @Override
    public void func_177695_c(WorldBorder border, double newSize) {
    }
    
    /**
     * This method is for cleaning up the player reference once they disconnect.
     */
    public void onPlayerDisconnect() {
        ((Player) this.player).getWorldBorder().ifPresent(border -> ((WorldBorderAccessor) border).accessor$getListeners().remove(this));
    }

    private void sendBorderPacket(IPacket<?> packet) {
        this.player.field_71135_a.func_147359_a(packet);
    }
}
