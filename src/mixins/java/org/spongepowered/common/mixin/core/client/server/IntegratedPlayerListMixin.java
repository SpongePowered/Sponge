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
package org.spongepowered.common.mixin.core.client.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.client.server.IntegratedPlayerListBridge;
import org.spongepowered.common.mixin.core.server.players.PlayerListMixin;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

@Mixin(IntegratedPlayerList.class)
public abstract class IntegratedPlayerListMixin extends PlayerListMixin implements IntegratedPlayerListBridge {

    // @formatter:off
    @Shadow public abstract Component shadow$canPlayerLogin(SocketAddress param0, GameProfile param1);
    // @formatter:on

    public CompletableFuture<Component> bridge$canPlayerLoginClient(final SocketAddress param0, final com.mojang.authlib.GameProfile param1) {
        final Component component = this.shadow$canPlayerLogin(param0, param1);
        if (component == null) {
            return this.impl$canPlayerLoginServer(param0, param1);
        }
        return CompletableFuture.completedFuture(component);
    }

    @Redirect(method = "canPlayerLogin", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)"
                    + "Lnet/minecraft/network/chat/Component;"))
    private Component impl$redirectCanPlayerLoginSuperCall(final PlayerList playerList, final SocketAddress param0, final GameProfile param1) {
        return null; // we'll handle it above which we'll indicate by returning null.
    }

}
