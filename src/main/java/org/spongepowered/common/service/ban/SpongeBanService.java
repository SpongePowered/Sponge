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
package org.spongepowered.common.service.ban;

import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.server.management.UserListIPBans;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.UserListUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * The default implementation of {@link BanService}.
 *
 * <p>Many of the methods here are copied from {@link UserListBans}
 * or {@link UserListIPBans}, while the original methods have been changed
 * to delegate to the registered {@link BanService}. This allows bans to
 * function normally when the default {@link BanService} has not been replaced,
 * while allowing plugin-provided {@link BanService}s to be used for all aspects
 * of Vanilla bans.</p>
 */
public class SpongeBanService implements BanService {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<Ban> getBans() {
        Collection<? extends Ban> bans = this.getProfileBans();
        bans.addAll((Collection) this.getIpBans());

        return (Collection<Ban>) bans;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Ban.Profile> getProfileBans() {
        this.getUserBanList().removeExpired();
        return new ArrayList<>((Collection<Ban.Profile>) (Object) this.getUserBanList().getEntries());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Ban.Ip> getIpBans() {
        this.getIPBanList().removeExpired();
        return new ArrayList<>((Collection<Ban.Ip>) (Object) this.getIPBanList().getEntries());
    }

    @Override
    public Optional<Ban.Profile> getBanFor(GameProfile profile) {
        UserListBans bans = this.getUserBanList();

        bans.removeExpired();
        return Optional.ofNullable((Ban.Profile) bans.getEntry((com.mojang.authlib.GameProfile) profile));
    }

    @Override
    public Optional<Ban.Ip> getBanFor(InetAddress address) {
        UserListIPBans bans = this.getIPBanList();

        bans.removeExpired();
        return Optional.ofNullable((Ban.Ip) bans.getBanEntry(new InetSocketAddress(address, 0)));
    }

    @Override
    public boolean isBanned(GameProfile profile) {
        UserListBans bans = this.getUserBanList();

        bans.removeExpired();
        return bans.getEntry((com.mojang.authlib.GameProfile) profile) != null;
    }

    @Override
    public boolean isBanned(InetAddress address) {
        UserListIPBans bans = this.getIPBanList();

        bans.removeExpired();
        return bans.getBanEntry(new InetSocketAddress(address, 0)) != null;
    }

    @Override
    public boolean pardon(GameProfile profile) {
        Optional<Ban.Profile> ban = getBanFor(profile);
        this.getUserBanList().removeExpired();
        return ban.isPresent() && removeBan(ban.get());
    }

    @Override
    public boolean pardon(InetAddress address) {
        Optional<Ban.Ip> ban = getBanFor(address);
        this.getIPBanList().removeExpired();
        return ban.isPresent() && removeBan(ban.get());
    }

    @Override
    public boolean removeBan(Ban ban) {
        if (!hasBan(ban)) {
            return false;
        }
        if (ban.getType().equals(BanTypes.PROFILE)) {
            User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate(((Ban.Profile) ban).getProfile());
            Sponge.getEventManager().post(SpongeEventFactory.createPardonUserEvent(Sponge.getCauseStackManager().getCurrentCause(), (Ban.Profile) ban, user));

            UserListUtils.removeEntry(this.getUserBanList(), ((Ban.Profile) ban).getProfile());
            return true;
        } else if (ban.getType().equals(BanTypes.IP)) {
            Sponge.getEventManager().post(SpongeEventFactory.createPardonIpEvent(Sponge.getCauseStackManager().getCurrentCause(), (Ban.Ip) ban));

            InetSocketAddress inetSocketAddress = new InetSocketAddress(((Ban.Ip) ban).getAddress(), 0);
            UserListUtils.removeEntry(this.getIPBanList(), this.getIPBanList().addressToString(inetSocketAddress));
            return true;
        }
        throw new IllegalArgumentException(String.format("Ban %s had unrecognized BanType %s!", ban, ban.getType()));
    }

    @Override
    public Optional<? extends Ban> addBan(Ban ban) {
        Optional<? extends Ban> prevBan;

        if (ban.getType().equals(BanTypes.PROFILE)) {
            prevBan = this.getBanFor(((Ban.Profile) ban).getProfile());

            User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate(((Ban.Profile) ban).getProfile());
            Sponge.getEventManager().post(SpongeEventFactory.createBanUserEvent(Sponge.getCauseStackManager().getCurrentCause(), (Ban.Profile) ban, user));

            UserListUtils.addEntry(this.getUserBanList(), (UserListEntry<?>) ban);
        } else if (ban.getType().equals(BanTypes.IP)) {
            prevBan = this.getBanFor(((Ban.Ip) ban).getAddress());

            Sponge.getEventManager().post(SpongeEventFactory.createBanIpEvent(Sponge.getCauseStackManager().getCurrentCause(), (Ban.Ip) ban));

            UserListUtils.addEntry(this.getIPBanList(), (UserListEntry<?>) ban);
        } else {
            throw new IllegalArgumentException(String.format("Ban %s had unrecognized BanType %s!", ban, ban.getType()));
        }
        return prevBan;
    }

    @Override
    public boolean hasBan(Ban ban) {
        if (ban.getType().equals(BanTypes.PROFILE)) {
            return this.isBanned(((Ban.Profile) ban).getProfile());
        } else if (ban.getType().equals(BanTypes.IP)) {
            return this.isBanned(((Ban.Ip) ban).getAddress());
        }
        throw new IllegalArgumentException(String.format("Ban %s had unrecognized BanType %s!", ban, ban.getType()));
    }

    private UserListBans getUserBanList() {
        return SpongeImpl.getServer().getPlayerList().getBannedPlayers();
    }

    private UserListIPBans getIPBanList() {
        return SpongeImpl.getServer().getPlayerList().getBannedIPs();
    }

}
