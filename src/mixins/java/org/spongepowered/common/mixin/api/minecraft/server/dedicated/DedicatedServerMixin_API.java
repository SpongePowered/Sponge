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
package org.spongepowered.common.mixin.api.minecraft.server.dedicated;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.api.Server;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.minecraft.server.MinecraftServerMixin_API;

import java.net.InetSocketAddress;
import java.util.Optional;

@Mixin(DedicatedServer.class)
@Implements(@Interface(iface = Server.class, prefix = "server$"))
public abstract class DedicatedServerMixin_API extends MinecraftServerMixin_API implements Server {

    // @formatter:off
    @Shadow public abstract String shadow$getServerIp();
    @Shadow public abstract int shadow$getServerPort();
    // @formatter:on

    public DedicatedServerMixin_API(final String name) {
        super(name);
    }

    @Override
    public Optional<InetSocketAddress> boundAddress() {
        if (this.shadow$getServerIp() == null) {
            return Optional.empty();
        }
        return Optional.of(new InetSocketAddress(this.shadow$getServerIp(), this.shadow$getServerPort()));
    }

    @Intrinsic
    public boolean server$isDedicatedServer() {
        return true;
    }
}
