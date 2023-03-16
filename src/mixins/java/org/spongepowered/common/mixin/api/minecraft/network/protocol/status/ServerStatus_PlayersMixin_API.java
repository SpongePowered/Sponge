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
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.api.network.status.StatusResponse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerStatus.Players.class)
public abstract class ServerStatus_PlayersMixin_API implements StatusResponse.Players {

    // @formatter:off
    @Shadow @Final private int online;
    @Shadow @Final private int max;
    @Shadow @Final private List<GameProfile> sample;
    // @formatter:on

    @Override
    public int online() {
        return this.online;
    }

    @Override
    public int max() {
        return this.max;
    }

    @Override
    public List<org.spongepowered.api.profile.GameProfile> profiles() {
        final List<org.spongepowered.api.profile.GameProfile> profiles = new ArrayList<>();
        this.sample.forEach(profile -> profiles.add(SpongeGameProfile.of(profile)));
        return profiles;
    }

}
