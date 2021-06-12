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
package org.spongepowered.common.service.server.ban;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

/**
 * Redirects all calls to the {@link BanService}.
 */
public class SpongeUserBanList extends UserBanList {

    public SpongeUserBanList(final File file) {
        super(file);
    }

    @Override
    protected boolean contains(final com.mojang.authlib.GameProfile profile) {
        return Sponge.server().serviceProvider().banService().banFor(SpongeGameProfile.of(profile)).join().isPresent();
    }

    @Override
    public UserBanListEntry get(final com.mojang.authlib.GameProfile profile) {
        final Optional<Ban.Profile> ban = Sponge.server().serviceProvider().banService().banFor(SpongeGameProfile.of(profile)).join();
        return ban.map(x -> {
            if (x instanceof UserBanListEntry) {
                return (UserBanListEntry) x;
            } else {
                final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
                return new UserBanListEntry(SpongeGameProfile.toMcProfile(x.profile()),
                        Date.from(x.creationDate()),
                        x.banSource().map(lcs::serialize).orElse(null),
                        x.expirationDate().map(Date::from).orElse(null),
                        x.reason().map(lcs::serialize).orElse(null));
            }
        }).orElse(null);
    }

    @Override
    public String[] getUserList() {
        final List<String> names = new ArrayList<>();

        for (final Ban.Profile ban : Sponge.server().serviceProvider().banService().profileBans().join()) {
            ban.profile().name().ifPresent(names::add);
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public void add(final UserBanListEntry entry) {
        Sponge.server().serviceProvider().banService().addBan((Ban) entry).join();
    }

    @Override
    public boolean isEmpty() {
        return Sponge.server().serviceProvider().banService().profileBans().join().isEmpty();
    }

    @Override
    public void remove(final com.mojang.authlib.GameProfile entry) {
        Sponge.server().serviceProvider().banService().pardon(SpongeGameProfile.of(entry)).join();
    }

}
