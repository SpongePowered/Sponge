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
package org.spongepowered.common.mixin.api.mcp.network.rcon;

import net.minecraft.network.rcon.RConConsoleSource;
import org.spongepowered.api.command.source.RconSource;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.network.rcon.RConConsoleSourceBridge;
import org.spongepowered.common.bridge.network.rcon.RConThreadClientBridge;

@Mixin(RConConsoleSource.class)
@Implements(@Interface(iface = RconSource.class, prefix = "api$"))
public abstract class RConConsoleSourceMixin_API implements RconSource {

    @Shadow public abstract String shadow$getName();

    @Override
    public RemoteConnection getConnection() {
        return (RemoteConnection) ((RConConsoleSourceBridge) this).bridge$getClient();
    }

    @Override
    public void setLoggedIn(final boolean loggedIn) {
        ((RConThreadClientBridge) ((RConConsoleSourceBridge) this).bridge$getClient()).bridge$setLoggedIn(loggedIn);
    }

    @Override
    public boolean getLoggedIn() {
        return  ((RConThreadClientBridge) ((RConConsoleSourceBridge) this).bridge$getClient()).bridge$getLoggedIn();
    }

    @Override
    public String getIdentifier() {
        return ((CommandSourceBridge) this).bridge$getIdentifier();
    }

    @Intrinsic
    public String api$getName() {
        return shadow$getName();
    }
}
