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

import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.players.IpBanListAccessor;
import org.spongepowered.common.accessor.server.players.StoredUserListAccessor;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.UserListUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

/**
 * The default implementation of {@link BanService}.
 *
 * <p>Many of the methods here are copied from {@link UserBanList}
 * or {@link IpBanList}, while the original methods have been changed
 * to delegate to the registered {@link BanService}. This allows bans to
 * function normally when the default {@link BanService} has not been replaced,
 * while allowing plugin-provided {@link BanService}s to be used for all aspects
 * of Vanilla bans.</p>
 */
@Singleton
public final class SpongeBanService implements BanService {

    @Override
    public CompletableFuture<Collection<? extends Ban>> getBans() {
        final Collection<Ban.Profile> bans = this.getProfileBans().join();
        final Collection<Ban.IP> ipBans = this.getIpBans().join();
        final Collection<Ban> list = new ArrayList<>(bans.size() + ipBans.size());
        list.addAll(bans);
        list.addAll(ipBans);
        return CompletableFuture.completedFuture(Collections.unmodifiableCollection(list));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Collection<Ban.Profile>> getProfileBans() {
        final StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry> accessor =
            (StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry>) this.getUserBanList();
        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(Collections.unmodifiableCollection(new ArrayList<>((Collection<Ban.Profile>) (Object) accessor.accessor$map().values())));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Collection<Ban.IP>> getIpBans() {
        final StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry> accessor = ((StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry>) this.getIPBanList());
        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(Collections.unmodifiableCollection(new ArrayList<>((Collection<Ban.IP>) (Object) accessor.accessor$map().values())));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Optional<Ban.Profile>> getBanFor(final GameProfile profile) {
        final StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry> accessor =
            (StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry>) this.getUserBanList();
        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(Optional.ofNullable((Ban.Profile) accessor.accessor$map().get(accessor.invoker$getKeyForUser(SpongeGameProfile.toMcProfile(profile)))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Optional<Ban.IP>> getBanFor(final InetAddress address) {
        final StoredUserListAccessor<String, IpBanListEntry> accessor = ((StoredUserListAccessor<String, IpBanListEntry>) this.getIPBanList());

        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(Optional.ofNullable((Ban.IP) accessor.accessor$map().get(accessor.invoker$getKeyForUser(((IpBanListAccessor) accessor).invoker$getIpFromAddress(new InetSocketAddress(address, 0))))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Boolean> isBanned(final GameProfile profile) {
        final StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry> accessor =
            (StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry>) this.getUserBanList();

        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(accessor.accessor$map().containsKey(accessor.invoker$getKeyForUser(SpongeGameProfile.toMcProfile(profile))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Boolean> isBanned(final InetAddress address) {
        final StoredUserListAccessor<String, IpBanListEntry> accessor = ((StoredUserListAccessor<String, IpBanListEntry>) this.getIPBanList());

        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(accessor.accessor$map().containsKey(accessor.invoker$getKeyForUser(((IpBanListAccessor) accessor).invoker$getIpFromAddress(new InetSocketAddress(address, 0)))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Boolean> pardon(final GameProfile profile) {
        final CompletableFuture<Optional<Ban.Profile>> ban = this.getBanFor(profile);
        final StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry> accessor =
            (StoredUserListAccessor<com.mojang.authlib.GameProfile, UserBanListEntry>) this.getUserBanList();
        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(ban.join().isPresent() && this.removeBan(ban.join().get()).join());
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Boolean> pardon(final InetAddress address) {
        final CompletableFuture<Optional<Ban.IP>> ban = this.getBanFor(address);
        final StoredUserListAccessor<String, IpBanListEntry> accessor = ((StoredUserListAccessor<String, IpBanListEntry>) this.getIPBanList());
        accessor.invoker$removeExpired();
        return CompletableFuture.completedFuture(ban.join().isPresent() && this.removeBan(ban.join().get()).join());
    }

    @Override
    public CompletableFuture<Boolean> removeBan(final Ban ban) {
        if (!this.hasBan(ban).join()) {
            return CompletableFuture.completedFuture(false);
        }
        if (ban.getType().equals(BanTypes.PROFILE.get())) {
            final User user = Sponge.getServer().getUserManager().getOrCreate(((Ban.Profile) ban).getProfile());
            Sponge.getEventManager().post(SpongeEventFactory.createPardonUserEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (Ban.Profile) ban, user));

            UserListUtil.removeEntry(this.getUserBanList(), SpongeGameProfile.toMcProfile(((Ban.Profile) ban).getProfile()));
            return CompletableFuture.completedFuture(true);
        } else if (ban.getType().equals(BanTypes.IP.get())) {
            Sponge.getEventManager().post(SpongeEventFactory.createPardonIpEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (Ban.IP) ban));

            final InetSocketAddress inetSocketAddress = new InetSocketAddress(((Ban.IP) ban).getAddress(), 0);
            UserListUtil.removeEntry(this.getIPBanList(), ((IpBanListAccessor) this.getIPBanList()).invoker$getIpFromAddress(inetSocketAddress));
            return CompletableFuture.completedFuture(true);
        }
        throw new IllegalArgumentException(String.format("Ban %s had unrecognized BanType %s!", ban, ban.getType()));
    }

    @Override
    public CompletableFuture<Optional<? extends Ban>> addBan(final Ban ban) {
        final Ban prevBan;

        if (ban.getType().equals(BanTypes.PROFILE.get())) {

            final User user = Sponge.getServer().getUserManager().getOrCreate(((Ban.Profile) ban).getProfile());
            Sponge.getEventManager().post(SpongeEventFactory.createBanUserEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (Ban.Profile) ban, user));

            prevBan = (Ban) UserListUtil.addEntry(this.getUserBanList(), (StoredUserEntry<?>) ban);
        } else if (ban.getType().equals(BanTypes.IP.get())) {

            Sponge.getEventManager().post(SpongeEventFactory.createBanIpEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (Ban.IP) ban));

            prevBan = (Ban) UserListUtil.addEntry(this.getIPBanList(), (StoredUserEntry<?>) ban);
        } else {
            throw new IllegalArgumentException(String.format("Ban %s had unrecognized BanType %s!", ban, ban.getType()));
        }
        return CompletableFuture.completedFuture(Optional.ofNullable(prevBan));
    }

    @Override
    public CompletableFuture<Boolean> hasBan(final Ban ban) {
        if (ban.getType().equals(BanTypes.PROFILE.get())) {
            return this.isBanned(((Ban.Profile) ban).getProfile());
        } else if (ban.getType().equals(BanTypes.IP.get())) {
            return this.isBanned(((Ban.IP) ban).getAddress());
        }
        throw new IllegalArgumentException(String.format("Ban %s had unrecognized BanType %s!", ban, ban.getType()));
    }

    private UserBanList getUserBanList() {
        return SpongeCommon.getServer().getPlayerList().getBans();
    }

    private IpBanList getIPBanList() {
        return SpongeCommon.getServer().getPlayerList().getIpBans();
    }

}
