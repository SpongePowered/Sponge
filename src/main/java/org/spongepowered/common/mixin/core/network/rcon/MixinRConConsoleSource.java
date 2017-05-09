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

import net.minecraft.command.ICommandSender;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.network.rcon.RConThreadClient;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinRConConsoleSource;
import org.spongepowered.common.interfaces.IMixinSubject;

@Mixin(RConConsoleSource.class)
public abstract class MixinRConConsoleSource implements ICommandSender, IMixinCommandSource, IMixinCommandSender, IMixinRConConsoleSource,
        RconSource, IMixinSubject {

    @Shadow @Final private StringBuffer buffer;

    private RConThreadClient clientThread;

    @Override
    public RemoteConnection getConnection() {
        return (RemoteConnection) this.clientThread;
    }

    @Override
    public void setConnection(RConThreadClient conn) {
        this.clientThread = conn;
    }

    @Override
    public void setLoggedIn(boolean loggedIn) {
        this.clientThread.loggedIn = loggedIn;
    }

    @Override
    public boolean getLoggedIn() {
        return this.clientThread.loggedIn;
    }

    /**
     * Add newlines between output lines for a command.
     *
     * @param component
     */
    @Inject(method = "sendMessage", at = @At("RETURN"))
    public void addNewlines(ITextComponent component, CallbackInfo ci) {
        this.buffer.append('\n');
    }

    @Override
    public String getIdentifier() {
        return getName();
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.TRUE;
    }

    @Override
    public ICommandSender asICommandSender() {
        return this;
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }
}
