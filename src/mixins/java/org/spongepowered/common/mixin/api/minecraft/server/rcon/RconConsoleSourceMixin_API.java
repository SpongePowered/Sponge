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
package org.spongepowered.common.mixin.api.minecraft.server.rcon;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.minecraft.server.rcon.RconConsoleSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.network.RconConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.server.rcon.thread.RconClientAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.server.rcon.RconConsoleSourceBridge;

import java.net.InetSocketAddress;

@Mixin(RconConsoleSource.class)
public abstract class RconConsoleSourceMixin_API implements RconConsoleSourceBridge, RconConnection {

    private static final String API$IDENTIFIER = "Recon";
    private static final Pointers API$POINTERS = Pointers.builder()
        .withStatic(Identity.NAME, RconConsoleSourceMixin_API.API$IDENTIFIER)
        .build();

    // @formatter:off
    @Shadow public abstract void shadow$sendSystemMessage(final net.minecraft.network.chat.Component $$0);
    // @formatter:on

    @Override
    public String identifier() {
        // RCon no longer has an identifier on the class, but it passes this to its CommandSource
        return RconConsoleSourceMixin_API.API$IDENTIFIER;
    }

    @Override
    public boolean isAuthorized() {
        return ((RconClientAccessor) this.bridge$getClient()).accessor$authed();
    }

    @Override
    public void setAuthorized(final boolean authorized) {
        ((RconClientAccessor) this.bridge$getClient()).accessor$authed(authorized);
    }

    @Override
    public InetSocketAddress address() {
        return ((RemoteConnection)this.bridge$getClient()).address();
    }

    @Override
    public InetSocketAddress virtualHost() {
        return ((RemoteConnection)this.bridge$getClient()).virtualHost();
    }

    @Override
    public void close() {
        ((RemoteConnection)this.bridge$getClient()).close();
    }

    // Audience

    @Override
    public void sendMessage(final @NonNull Identity identity, final @NonNull Component message, final @NonNull MessageType type) {
        this.shadow$sendSystemMessage(SpongeAdventure.asVanilla(message));
    }

    @Override
    public @NonNull Pointers pointers() {
        return RconConsoleSourceMixin_API.API$POINTERS;
    }

    @Override
    public boolean active() {
        return this.bridge$getClient().isRunning();
    }
}
