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
package org.spongepowered.common.mixin.api.minecraft.network.protocol.status;

import static com.google.common.base.Preconditions.checkNotNull;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.protocol.status.ServerStatusBridge;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(ServerStatus.class)
public abstract class ServerStatusMixin_API implements ClientPingServerEvent.Response {

    // @formatter:off
    @Shadow @Nullable private net.minecraft.network.chat.Component description;
    @Shadow @Nullable private ServerStatus.Players players;
    @Shadow private ServerStatus.Version version;
    // @formatter:on


    @Override
    public Component description() {
        return ((ServerStatusBridge) this).bridge$getDescription();
    }

    @Override
    public void setDescription(final Component description) {
        ((ServerStatusBridge) this).bridge$setDescription(checkNotNull(description, "description"));
        this.description = SpongeAdventure.asVanilla(description);
    }

    @Override
    public Optional<org.spongepowered.api.event.server.ClientPingServerEvent.Response.Players> players() {
        return Optional.ofNullable((org.spongepowered.api.event.server.ClientPingServerEvent.Response.Players) this.players);
    }

    @Override
    public void setHidePlayers(final boolean hide) {
        if ((this.players == null) != hide) {
            if (hide) {
                ((ServerStatusBridge) this).bridge$setPlayerBackup(this.players);
                this.players = null;
            } else {
                this.players = ((ServerStatusBridge) this).bridge$getPlayerBackup();
                ((ServerStatusBridge) this).bridge$setPlayerBackup(null);
            }
        }
    }

    @Override
    public MinecraftVersion version() {
        return (MinecraftVersion) this.version;
    }

    @Override
    public Optional<Favicon> favicon() {
        return Optional.ofNullable(((ServerStatusBridge) this).bridge$getFavicon());
    }

    @Override
    public void setFavicon(@Nullable final Favicon favicon) {
        ((ServerStatusBridge) this).setFavicon(favicon);
    }

}
