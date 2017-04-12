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
package org.spongepowered.common.mixin.core.status;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.network.status.SpongeFavicon;
import org.spongepowered.common.text.SpongeTexts;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(ServerStatusResponse.class)
public abstract class MixinServerStatusResponse implements ClientPingServerEvent.Response {

    @Shadow @Nullable private ITextComponent description;
    @Shadow @Nullable private ServerStatusResponse.Players players;
    @Shadow private ServerStatusResponse.Version version;
    @Shadow @Nullable private String favicon;

    private Text descriptionText = Text.of();
    @Nullable private ServerStatusResponse.Players playerBackup;
    @Nullable private Favicon faviconHandle;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        setServerDescription(null);
    }

    @Override
    public Text getDescription() {
        return this.descriptionText;
    }

    @Override
    public void setDescription(Text description) {
        this.descriptionText = checkNotNull(description, "description");
        this.description = SpongeTexts.toComponent(description);
    }

    /**
     * @author minecrell - January 18th, 2015
     * @reason Use our Text API
     *
     * @param motd The message of the day to set
     */
    @Overwrite
    public void setServerDescription(@Nullable ITextComponent motd) {
        if (motd != null) {
            this.description = motd;
            this.descriptionText = SpongeTexts.toText(motd);
        } else {
            this.description = new TextComponentString("");
            this.descriptionText = Text.of();
        }
    }

    @Override
    public Optional<Players> getPlayers() {
        return Optional.ofNullable((Players) this.players);
    }

    @Override
    public void setHidePlayers(boolean hide) {
        if ((this.players == null) != hide) {
            if (hide) {
                this.playerBackup = this.players;
                this.players = null;
            } else {
                this.players = this.playerBackup;
                this.playerBackup = null;
            }
        }
    }

    @Override
    public MinecraftVersion getVersion() {
        return (MinecraftVersion) this.version;
    }

    @Override
    public Optional<Favicon> getFavicon() {
        return Optional.ofNullable(this.faviconHandle);
    }

    @Override
    public void setFavicon(@Nullable Favicon favicon) {
        this.faviconHandle = favicon;
        if (this.faviconHandle != null) {
            this.favicon = ((SpongeFavicon) this.faviconHandle).getEncoded();
        } else {
            this.favicon = null;
        }
    }

    /**
     * @author minecrell - January 18th, 2015
     * @reason Implements our Status API
     *
     * @param faviconBlob the blob of the favicon
     */
    @Overwrite
    public void setFavicon(@Nullable String faviconBlob) {
        if (faviconBlob == null) {
            this.favicon = null;
            this.faviconHandle = null;
        } else {
            try {
                this.faviconHandle = new SpongeFavicon(faviconBlob);
                this.favicon = faviconBlob;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
