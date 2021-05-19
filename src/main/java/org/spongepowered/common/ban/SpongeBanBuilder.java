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
package org.spongepowered.common.ban;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.UserBanListEntry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.BanUtil;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;


public final class SpongeBanBuilder implements Ban.Builder {

    private GameProfile profile;
    private InetAddress address;
    private BanType banType;
    private @Nullable Component reason;
    private Instant start = Instant.now();
    private @Nullable Instant end;
    private @Nullable Component source;

    @Override
    public Ban.Builder profile(final GameProfile profile) {
        Objects.requireNonNull(profile, "Profile cannot be null!");
        if (this.banType != BanTypes.PROFILE.get()) {
            throw new IllegalStateException("Cannot set a GameProfile if the BanType is not BanTypes.PROFILE!");
        }
        this.profile = profile;
        return this;
    }

    @Override
    public Ban.Builder address(final InetAddress address) {
        Objects.requireNonNull(address, "Address cannot be null!");
        if (this.banType != BanTypes.IP.get()) {
            throw new IllegalStateException("Cannot set an InetAddress if the BanType is not BanTypes.IP!");
        }
        this.address = address;
        return this;
    }

    @Override
    public Ban.Builder type(final BanType type) {
        Objects.requireNonNull(type, "BanType cannot be null!");
        if (type == BanTypes.IP.get()) {
            this.profile = null;
        } else {
            this.address = null;
        }
        this.banType = type;
        return this;
    }

    @Override
    public Ban.Builder reason(final @Nullable Component reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public Ban.Builder startDate(final Instant instant) {
        Objects.requireNonNull(instant, "Start date cannot be null!");
        this.start = instant;
        return this;
    }

    @Override
    public Ban.Builder expirationDate(final @Nullable Instant instant) {
        this.end = instant;
        return this;
    }

    @Override
    public Ban.Builder source(final @Nullable Component source) {
        this.source = source;
        return this;
    }

    @Override
    public Ban build() {
        if (this.banType == null) {
            throw new IllegalStateException("BanType cannot be null!");
        }

        final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
        final String sourceName = this.source != null ? lcs.serialize(this.source) : null;
        final String reason = this.reason != null ? lcs.serialize(this.reason) : null;

        if (this.banType == BanTypes.PROFILE.get()) {
            if (this.profile == null) {
                throw new IllegalStateException("User cannot be null");
            }
            return (Ban) new UserBanListEntry(SpongeGameProfile.toMcProfile(this.profile.withoutProperties()),
                    Date.from(this.start), sourceName, this.toDate(this.end), reason);
        }
        if (this.address == null) {
            throw new IllegalStateException("Address cannot be null!");
        }

        return (Ban) new IpBanListEntry(BanUtil.addressToBanCompatibleString(this.address),
                Date.from(this.start), sourceName, this.toDate(this.end), reason);
    }

    private Date toDate(final Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    @Override
    public Ban.Builder from(final Ban ban) {
        this.reset();
        this.banType = ban.type();

        if (this.banType.equals(BanTypes.PROFILE.get())) {
            this.profile = ((Ban.Profile) ban).profile();
        } else {
            this.address = ((Ban.IP) ban).address();
        }

        this.reason = ban.reason().orElse(null);
        this.start = ban.creationDate();
        this.end = ban.expirationDate().orElse(null);
        this.source = ban.banSource().orElse(null);
        return this;
    }

    @Override
    public Ban.Builder reset() {
        this.profile = null;
        this.address = null;
        this.banType = null;
        this.reason = null;
        this.start = Instant.now();
        this.end = null;
        this.source = null;
        return this;
    }
}
