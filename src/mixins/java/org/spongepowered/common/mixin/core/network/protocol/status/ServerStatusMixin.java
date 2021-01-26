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
package org.spongepowered.common.mixin.core.network.protocol.status;

import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ServerStatusResponseBridge;
import org.spongepowered.common.network.status.SpongeFavicon;

import java.io.IOException;

import javax.annotation.Nullable;

@Mixin(ServerStatus.class)
public abstract class ServerStatusMixin implements ServerStatusResponseBridge {

    @Shadow @Nullable private net.minecraft.network.chat.Component description;
    @Shadow @Nullable private String favicon;

    private Component impl$descriptionText = Component.empty();
    @Nullable private ServerStatus.Players impl$playerBackup;
    @Nullable private Favicon impl$faviconHandle;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$initializeDescriptionText(final CallbackInfo ci) {
        this.setDescription(null);
    }

    /**
     * @author minecrell - January 18th, 2015
     * @reason Use our Text API
     *
     * @param motd The message of the day to set
     */
    @Overwrite
    public void setDescription(@Nullable final net.minecraft.network.chat.Component motd) {
        if (motd != null) {
            this.description = motd;
            this.impl$descriptionText = SpongeAdventure.asAdventure(motd);
        } else {
            this.description = new TextComponent("");
            this.impl$descriptionText = Component.empty();
        }
    }

    /**
     * @author minecrell - January 18th, 2015
     * @reason Implements our Status API
     *
     * @param faviconBlob the blob of the favicon
     */
    @Overwrite
    public void setFavicon(@Nullable final String faviconBlob) {
        if (faviconBlob == null) {
            this.favicon = null;
            this.impl$faviconHandle = null;
        } else {
            try {
                this.impl$faviconHandle = new SpongeFavicon(faviconBlob);
                this.favicon = faviconBlob;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Component bridge$getDescription() {
        return this.impl$descriptionText;
    }

    @Override
    public void bridge$setDescription(@Nullable final Component text) {
        this.impl$descriptionText = text == null ? Component.empty() : text;
    }

    @Override
    public Favicon bridge$getFavicon() {
        return this.impl$faviconHandle;
    }

    @Override
    public void setFavicon(@Nullable final Favicon favicon) {
        this.impl$faviconHandle = favicon;
        if (favicon != null) {
            this.favicon = ((SpongeFavicon) favicon).getEncoded();
        } else {
            this.favicon = null;
        }
    }

    @Override
    public ServerStatus.Players bridge$getPlayerBackup() {
        return this.impl$playerBackup;
    }

    @Override
    public void bridge$setPlayerBackup(final ServerStatus.Players players) {
        this.impl$playerBackup = players;
    }
}
