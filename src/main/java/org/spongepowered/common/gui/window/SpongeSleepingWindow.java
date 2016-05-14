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
package org.spongepowered.common.gui.window;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import org.spongepowered.api.gui.window.SleepingWindow;

public class SpongeSleepingWindow extends AbstractSpongeWindow implements SleepingWindow {

    @Override
    protected boolean show(EntityPlayerMP player) {
        player.playerNetServerHandler.sendPacket(new S0APacketUseBed(player, VIRTUAL_POS));
        player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        return true;
    }

    @Override
    protected void sendClose(EntityPlayerMP player) {
        player.playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 2));
    }

    @Override
    public boolean canDetectClientClose() {
        return true;
    }

    /**
     * Player clicked 'Leave Bed'.
     */
    public boolean onClientClose(EntityPlayerMP player) {
        sendClose(player); // Client doesn't close the GUI themselves
        onClosed(player);
        return true;
    }

    public static class Builder extends SpongeWindowBuilder<SleepingWindow, SleepingWindow.Builder> implements SleepingWindow.Builder {

        @Override
        public SleepingWindow build() {
            return new SpongeSleepingWindow();
        }
    }

}
