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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;


/**
 * Function to kick a player.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PlayerKickHelper {

    private PlayerKickHelper() {
    }

    /**
     * A {@link ITextComponent}-friendly version of {@link NetHandlerPlayServer#kickPlayerFromServer(String)}.
     * This duplicates the code of that kick implementation pretty much exactly
     *
     * @param ply The player to kick
     * @param component The kick message
     */
    public static void kickPlayer(final EntityPlayerMP ply, final ITextComponent component) {
        ply.connection.getNetworkManager().sendPacket(new SPacketDisconnect(component), future ->
                ply.connection.getNetworkManager().closeChannel(component));
        ply.connection.getNetworkManager().disableAutoRead();
        // fix this getServer.
        Futures.getUnchecked(ply.getServer().addScheduledTask(() -> ply.connection.getNetworkManager().checkDisconnected()));

    }

}
