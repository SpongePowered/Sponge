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
package org.spongepowered.common.mixin.core.network.rcon;

import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.network.rcon.RConThreadBase;
import net.minecraft.network.rcon.RConThreadClient;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.network.rcon.RconConnectionEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinRConConsoleSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Mixin(RConThreadClient.class)
public abstract class MixinRConThreadClient extends RConThreadBase implements RemoteConnection {

    @Shadow private boolean loggedIn;
    @Shadow private Socket clientSocket;

    private RConConsoleSource source;

    protected MixinRConThreadClient(IServer p_i45300_1_, String p_i45300_2_) {
        super(p_i45300_1_, p_i45300_2_);
    }

    private void initSource() {
        this.source = new RConConsoleSource(SpongeImpl.getServer());
        Object clientThread = this;
        ((IMixinRConConsoleSource) this.source).setConnection((RConThreadClient) clientThread);
    }

    @Override
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) this.clientSocket.getRemoteSocketAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return (InetSocketAddress) this.clientSocket.getLocalSocketAddress();
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "net.minecraft.network.rcon.IServer.handleRConCommand(Ljava/lang/String;)"
            + "Ljava/lang/String;"))
    public String commandExecutionHook(IServer server, String commandStr) {
        try {
            synchronized (this.clientSocket) {
                SpongeImpl.getServer().getCommandManager().executeCommand(this.source, commandStr);
                final String logContents = this.source.getLogContents();
                this.source.resetLog();
                return logContents;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "net.minecraft.network.rcon.RConThreadClient.sendResponse(IILjava/lang/String;)V",
            shift = At.Shift.BEFORE))
    public void rconLoginCallback(CallbackInfo ci) throws IOException {
        if (this.source == null) {
            initSource();
        }
        Sponge.getCauseStackManager().pushCause(this.source);
        RconConnectionEvent.Login event = SpongeEventFactory.createRconConnectionEventLogin(Sponge.getCauseStackManager().getCurrentCause(),
            (RconSource) this.source);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            this.loggedIn = false;
            throw new IOException("Cancelled login");
        }
    }

    @Inject(method = "closeSocket", at = @At("HEAD"))
    public void rconLogoutCallback(CallbackInfo ci){
        if (this.source == null) {
            initSource();
        }
        if (this.loggedIn) {
            Sponge.getCauseStackManager().pushCause(this.source);
            SpongeImpl.postEvent(SpongeEventFactory.createRconConnectionEventDisconnect(Sponge.getCauseStackManager().getCurrentCause(),
                (RconSource) this.source));
            Sponge.getCauseStackManager().popCause();
        }
    }
}
