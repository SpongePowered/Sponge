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

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.network.protocol.status.ServerStatus;

@Mixin(ServerStatus.Players.class)
public abstract class ServerStatus_PlayersMixin_API implements ClientPingServerEvent.Response.Players {

    // @formatter:off
    @Shadow @Final @Mutable private int numPlayers;
    @Shadow @Final @Mutable private int maxPlayers;
    // @formatter:on

    @Nullable private List<org.spongepowered.api.profile.GameProfile> profiles;

    @Override
    public int online() {
        return this.numPlayers;
    }

    @Override
    public void setOnline(final int online) {
        this.numPlayers = online;
    }

    @Override
    public int max() {
        return this.maxPlayers;
    }

    @Override
    public void setMax(final int max) {
        this.maxPlayers = max;
    }

    @Override
    public List<org.spongepowered.api.profile.GameProfile> profiles() {
        if (this.profiles == null) {
            this.profiles = new ArrayList<>();
        }
        return this.profiles;
    }

    /**
     * @author minecrell - January 18th, 2015
     * @reason Use our game profile objects in the collection
     * instead of vanilla.
     *
     * @return The profiles
     */
    @Overwrite
    public GameProfile[] getSample() {
        if (this.profiles == null) {
            return new GameProfile[0];
        }

        // TODO: When serializing, Minecraft calls this method frequently (it doesn't store the result).
        // Maybe we should cache this until the list is modified or patch the serialization?
        return this.profiles.stream()
                .map(SpongeGameProfile::toMcProfile)
                .toArray(GameProfile[]::new);
    }

    /**
     * @author minecrell - January 18th, 2015
     * @reason Use our own field
     *
     * @param playersIn The players to set
     */
    @Overwrite
    public void setSample(final GameProfile[] playersIn) {
        if (this.profiles == null) {
            this.profiles = new ArrayList<>(playersIn.length);
        } else {
            this.profiles.clear();
        }
        for (final GameProfile profile : playersIn) {
            this.profiles.add(SpongeGameProfile.of(profile));
        }
    }
}
