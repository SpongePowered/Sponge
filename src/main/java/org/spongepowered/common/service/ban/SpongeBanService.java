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

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.common.interfaces.ban.IMixinBanList;
import org.spongepowered.common.interfaces.ban.IMixinBanLogic;
import org.spongepowered.common.interfaces.ban.IMixinUserListBans;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpongeBanService implements BanService {

    @Override
    public Collection<Ban> getBans() {
        List<Ban> bans = new ArrayList<Ban>();
        bans.addAll(this.getUserBans());
        bans.addAll(this.getIpBans());
        return bans;
    }

    @Override
    public Collection<Ban.User> getUserBans() {
        return (Collection) ((IMixinBanLogic) MinecraftServer.getServer().getConfigurationManager().getBannedPlayers()).getBans();
    }

    @Override
    public Collection<Ban.Ip> getIpBans() {
        return (Collection) ((IMixinBanLogic) MinecraftServer.getServer().getConfigurationManager().getBannedIPs()).getBans();
    }

    @Override
    public Collection<Ban.User> getBansFor(User user) {
        return (Collection) ((IMixinUserListBans) MinecraftServer.getServer().getConfigurationManager().getBannedPlayers()).getBans(user);
    }

    @Override
    public Collection<Ban.Ip> getBansFor(InetAddress address) {
        return (Collection) ((IMixinBanList) MinecraftServer.getServer().getConfigurationManager().getBannedIPs())
                .getBans(address);
    }

    @Override
    public boolean isBanned(User user) {
        return ((IMixinUserListBans) MinecraftServer.getServer().getConfigurationManager().getBannedPlayers()).isBanned(user);
    }

    @Override
    public boolean isBanned(InetAddress address) {
        return ((IMixinBanList) MinecraftServer.getServer().getConfigurationManager().getBannedIPs()).isBanned(address);
    }

    @Override
    public void pardon(User user) {
        ((IMixinUserListBans) MinecraftServer.getServer().getConfigurationManager().getBannedPlayers()).pardon(user);
    }

    @Override
    public void pardon(InetAddress address) {
        ((IMixinBanList) MinecraftServer.getServer().getConfigurationManager().getBannedIPs()).pardon(address);
    }

    @Override
    public void pardon(Ban ban) {
        ((IMixinBanLogic) this.getBanList(ban.getType())).pardon(ban);
    }

    @Override
    public void ban(Ban ban) {
        ((IMixinBanLogic) this.getBanList(ban.getType())).ban(ban);
    }

    @Override
    public boolean hasBan(Ban ban) {
        return ((IMixinBanLogic) this.getBanList(ban.getType())).hasBan(ban);
    }

    private UserList getBanList(BanType banType) {
        if (banType == BanType.USER_BAN) {
            return  MinecraftServer.getServer().getConfigurationManager().getBannedPlayers();
        }
        return MinecraftServer.getServer().getConfigurationManager().getBannedIPs();
    }
}
