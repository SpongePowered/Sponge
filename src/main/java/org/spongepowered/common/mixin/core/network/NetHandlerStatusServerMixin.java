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
package org.spongepowered.common.mixin.core.network;

import net.minecraft.network.NetHandlerStatusServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.network.status.SpongeStatusClient;
import org.spongepowered.common.network.status.SpongeStatusResponse;

@Mixin(NetHandlerStatusServer.class)
public abstract class NetHandlerStatusServerMixin {

    @Shadow @Final private static ITextComponent EXIT_MESSAGE;

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private NetworkManager networkManager;
    @Shadow private boolean handled;

    /**
     * @author Minecrell - January 18th, 2015
     * @reason Post the server status ping event for plugins.
     */
    @Overwrite
    public void processServerQuery(final CPacketServerQuery packetIn) {
        if (this.handled) {
            this.networkManager.func_150718_a(EXIT_MESSAGE);
        } else {
            this.handled = true;

            final ServerStatusResponse response = SpongeStatusResponse.post(this.server, new SpongeStatusClient(this.networkManager));
            if (response != null) {
                this.networkManager.func_179290_a(new SPacketServerInfo(response));
            } else {
                this.networkManager.func_150718_a(null);
            }
        }
    }

}
