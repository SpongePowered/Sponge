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
package org.spongepowered.common.mixin.core.server.rcon.thread;

import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.NetworkDataOutputStream;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.server.query.QueryServerEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Mixin(QueryThreadGs4.class)
public abstract class QueryThreadGs4Mixin {

    // @formatter:off
    @Shadow @Final private NetworkDataOutputStream rulesResponse;
    @Shadow @Final private ServerInterface serverInterface;
    @Shadow @Final private String serverName;
    @Shadow @Final private String worldName;
    @Shadow @Final private int maxPlayers;
    @Shadow @Final private int serverPort;
    @Shadow private String hostIp;

    @Shadow protected abstract void shadow$sendTo(byte[] param0, DatagramPacket param1) throws IOException;
    @Shadow protected abstract byte[] shadow$getIdentBytes(SocketAddress param0);
    // @formatter:on

    @Redirect(method = "processPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/rcon/thread/QueryThreadGs4;sendTo([BLjava/net/DatagramPacket;)V"))
    public void impl$basicSendTo(QueryThreadGs4 query, byte[] param0, DatagramPacket datagramPacket) throws IOException {
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        final QueryServerEvent.Basic event = SpongeEventFactory.createQueryServerEventBasic(currentCause,
                (InetSocketAddress) datagramPacket.getSocketAddress(),
                "SMP",
                this.worldName,
                this.serverName,
                this.maxPlayers,
                this.serverInterface.getPlayerCount());

        Sponge.eventManager().post(event);

        NetworkDataOutputStream var3 = new NetworkDataOutputStream(1460);
        var3.write(0);
        var3.writeBytes(this.shadow$getIdentBytes(event.address()));
        var3.writeString(event.motd());
        var3.writeString(event.gameType());
        var3.writeString(event.map());
        var3.writeString(Integer.toString(event.playerCount()));
        var3.writeString(Integer.toString(event.maxPlayerCount()));
        var3.writeShort((short)this.serverPort);
        var3.writeString(this.hostIp);
        this.shadow$sendTo(var3.toByteArray(), datagramPacket);
    }

    @Inject(method = "buildRuleResponse", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/rcon/NetworkDataOutputStream;reset()V"))
    public void impl$basicSendTo(DatagramPacket datagramPacket, CallbackInfoReturnable<byte[]> cir) throws IOException {

        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        final QueryServerEvent.Full event = SpongeEventFactory.createQueryServerEventFull(currentCause,
                (InetSocketAddress) datagramPacket.getSocketAddress(),
                new LinkedHashMap<>(),
                "MINECRAFT",
                "SMP",
                this.worldName,
                this.serverName,
                Arrays.asList(this.serverInterface.getPlayerNames()),
                this.serverInterface.getPluginNames(),
                this.serverInterface.getServerVersion(),
                this.maxPlayers,
                this.serverInterface.getPlayerCount());
        Sponge.eventManager().post(event);

        this.rulesResponse.reset();
        this.rulesResponse.write(0);
        this.rulesResponse.writeBytes(this.shadow$getIdentBytes(event.address()));
        this.rulesResponse.writeString("splitnum");
        this.rulesResponse.write(128);
        this.rulesResponse.write(0);
        this.rulesResponse.writeString("hostname");
        this.rulesResponse.writeString(event.motd());
        this.rulesResponse.writeString("gametype");
        this.rulesResponse.writeString(event.gameType());
        this.rulesResponse.writeString("game_id");
        this.rulesResponse.writeString(event.gameId());
        this.rulesResponse.writeString("version");
        this.rulesResponse.writeString(event.version());
        this.rulesResponse.writeString("plugins");
        this.rulesResponse.writeString(event.plugins());
        this.rulesResponse.writeString("map");
        this.rulesResponse.writeString(event.map());
        this.rulesResponse.writeString("numplayers");
        this.rulesResponse.writeString("" + event.playerCount());
        this.rulesResponse.writeString("maxplayers");
        this.rulesResponse.writeString("" + event.maxPlayerCount());
        this.rulesResponse.writeString("hostport");
        this.rulesResponse.writeString("" + this.serverPort);
        this.rulesResponse.writeString("hostip");
        this.rulesResponse.writeString(this.hostIp);
        this.rulesResponse.write(0);
        this.rulesResponse.write(1);
        this.rulesResponse.writeString("player_");
        this.rulesResponse.write(0);
        final List<String> var3 = event.players();

        for(String var4 : var3) {
            this.rulesResponse.writeString(var4);
        }

        this.rulesResponse.write(0);

        cir.setReturnValue(this.rulesResponse.toByteArray());
    }
}
