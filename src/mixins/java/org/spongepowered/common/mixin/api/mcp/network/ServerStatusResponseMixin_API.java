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
package org.spongepowered.common.mixin.api.mcp.network;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.network.ServerStatusResponseBridge;
import org.spongepowered.common.network.status.SpongeFavicon;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(ServerStatusResponse.class)
public abstract class ServerStatusResponseMixin_API implements ClientPingServerEvent.Response {

    @Shadow @Nullable private ITextComponent description;
    @Shadow @Nullable private ServerStatusResponse.Players players;
    @Shadow private ServerStatusResponse.Version version;
    @Shadow @Nullable private String favicon;


    @Override
    public Text getDescription() {
        return ((ServerStatusResponseBridge) this).bridge$getDescription();
    }

    @Override
    public void setDescription(final Text description) {
        ((ServerStatusResponseBridge) this).bridge$setDescription(checkNotNull(description, "description"));
        this.description = SpongeTexts.toComponent(description);
    }

    @Override
    public Optional<org.spongepowered.api.event.server.ClientPingServerEvent.Response.Players> getPlayers() {
        return Optional.ofNullable((org.spongepowered.api.event.server.ClientPingServerEvent.Response.Players) this.players);
    }

    @Override
    public void setHidePlayers(final boolean hide) {
        if ((this.players == null) != hide) {
            if (hide) {
                ((ServerStatusResponseBridge) this).bridge$setPlayerBackup(this.players);
                this.players = null;
            } else {
                this.players = ((ServerStatusResponseBridge) this).bridge$getPlayerBackup();
                ((ServerStatusResponseBridge) this).bridge$setPlayerBackup(null);
            }
        }
    }

    @Override
    public MinecraftVersion getVersion() {
        return (MinecraftVersion) this.version;
    }

    @Override
    public Optional<Favicon> getFavicon() {
        return Optional.ofNullable(((ServerStatusResponseBridge) this).bridge$getFavicon());
    }

    @Override
    public void setFavicon(@Nullable final Favicon favicon) {
        ((ServerStatusResponseBridge) this).setFavicon(favicon);
    }

}
