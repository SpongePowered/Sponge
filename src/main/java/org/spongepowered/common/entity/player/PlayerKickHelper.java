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
package org.spongepowered.common.entity.player;

import com.google.common.util.concurrent.Futures;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;


/**
 * Function to kick a player.
 */
public class PlayerKickHelper {
    private PlayerKickHelper() {
    }

    /**
     * A {@link IChatComponent}-friendly version of {@link NetHandlerPlayServer#kickPlayerFromServer(String)}.
     * This duplicates the code of that kick implementation pretty much exactly
     *
     * @param ply The player to kick
     * @param component The kick message
     */
    public static void kickPlayer(final EntityPlayerMP ply, final IChatComponent component) {
        ply.playerNetServerHandler.getNetworkManager().sendPacket(new S40PacketDisconnect(component),
                new GenericFutureListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        ply.playerNetServerHandler.getNetworkManager().closeChannel(component);
                    }
                });
        ply.playerNetServerHandler.getNetworkManager().disableAutoRead();;
        Futures.getUnchecked(MinecraftServer.getServer().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                ply.playerNetServerHandler.getNetworkManager().checkDisconnected();
            }
        }));

    }

}
