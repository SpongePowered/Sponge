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

import static org.spongepowered.common.util.NetworkUtil.LOCAL_ADDRESS;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.common.util.NetworkUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.IPBanList;

/**
 * Redirects all calls to the {@link BanService}.
 */
public class SpongeIPBanList extends IPBanList {

    public SpongeIPBanList(File bansFile) {
        super(bansFile);
    }

    private static BanService getService() {
        return Sponge.getServiceManager().provideUnchecked(BanService.class);
    }

    @Override
    protected boolean hasEntry(String entry) {
        if (entry.equals(LOCAL_ADDRESS)) { // Check for single player
            return false;
        }

        try {
            return getService().isBanned(InetAddress.getByName(entry));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    @Nullable
    public IPBanEntry getEntry(String obj) {
        if (obj.equals(LOCAL_ADDRESS)) { // Check for single player
            return null;
        }

        try {
            return (IPBanEntry) getService().getBanFor(InetAddress.getByName(obj)).orElse(null);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    public void removeEntry(String entry) {
        if (entry.equals(LOCAL_ADDRESS)) { // Check for single player
            return;
        }

        try {
            getService().pardon(InetAddress.getByName(entry));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    public String[] func_152685_a() {
        List<String> ips = new ArrayList<>();
        for (Ban.Ip ban : getService().getIpBans()) {
            ips.add(this.func_152707_c(new InetSocketAddress(ban.getAddress(), 0)));
        }
        return ips.toArray(new String[ips.size()]);
    }

    @Override
    public void addEntry(IPBanEntry entry) {
        getService().addBan((Ban) entry);
    }

    @Override
    public boolean func_152690_d() {
        return getService().getIpBans().isEmpty();
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Use InetSocketAddress#getHostString() where possible (instead of
     *     inspecting SocketAddress#toString()) to support IPv6 addresses
     */
    @Override
    public String func_152707_c(SocketAddress address) {
        return NetworkUtil.getHostString(address);
    }

}
