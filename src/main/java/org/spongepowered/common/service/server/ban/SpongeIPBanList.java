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

import static org.spongepowered.common.util.NetworkUtil.LOCAL_ADDRESS;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.util.BanUtil;
import org.spongepowered.common.util.NetworkUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

/**
 * Redirects all calls to the {@link BanService}.
 */
public final class SpongeIPBanList extends IpBanList {

    public SpongeIPBanList(final File file) {
        super(file);
    }

    @Override
    protected boolean contains(final String entry) {
        if (entry.equals(LOCAL_ADDRESS)) { // Check for single player
            return false;
        }

        try {
            return Sponge.server().serviceProvider().banService().banFor(InetAddress.getByName(entry)).join().isPresent();
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    public @Nullable IpBanListEntry get(final String obj) {
        if (obj.equals(LOCAL_ADDRESS)) { // Check for single player
            return null;
        }

        try {
            return Sponge.server().serviceProvider().banService().banFor(InetAddress.getByName(obj)).join()
                    .map(ban -> {
                        if (ban instanceof IpBanListEntry) {
                            return (IpBanListEntry) ban;
                        }
                        return new IpBanListEntry(BanUtil.addressToBanCompatibleString(ban.address()),
                                Date.from(ban.creationDate()),
                                ban.banSource().map(SpongeAdventure::legacySection).orElse(null),
                                ban.expirationDate().map(Date::from).orElse(null),
                                ban.reason().map(SpongeAdventure::legacySection).orElse(null));
                    })
                    .orElse(null);
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    public void remove(final String entry) {
        if (entry.equals(LOCAL_ADDRESS)) { // Check for single player
            return;
        }

        try {
            Sponge.server().serviceProvider().banService().pardon(InetAddress.getByName(entry)).join();
        } catch (final UnknownHostException e) {
            throw new IllegalArgumentException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    public String[] getUserList() {
        final List<String> ips = new ArrayList<>();
        for (final Ban.IP ban : Sponge.server().serviceProvider().banService().ipBans().join()) {
            ips.add(this.getIpFromAddress(new InetSocketAddress(ban.address(), 0)));
        }
        return ips.toArray(new String[0]);
    }

    @Override
    public void add(final IpBanListEntry entry) {
        Sponge.server().serviceProvider().banService().addBan((Ban) entry).join();
    }

    @Override
    public boolean isEmpty() {
        return Sponge.server().serviceProvider().banService().ipBans().join().isEmpty();
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Use InetSocketAddress#getHostString() where possible (instead of
     *     inspecting SocketAddress#toString()) to support IPv6 addresses
     */
    @Override
    public String getIpFromAddress(final SocketAddress address) {
        return NetworkUtil.getHostString(address);
    }
}
