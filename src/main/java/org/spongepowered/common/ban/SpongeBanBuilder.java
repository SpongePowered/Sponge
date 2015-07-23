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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserListBansEntry;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanBuilder;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.util.command.CommandSource;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;

import javax.annotation.Nullable;

public class SpongeBanBuilder implements BanBuilder {

    private User user;
    private InetAddress address;
    private BanType banType;
    private Text.Literal reason;
    private Date start = new Date();
    private Date end;
    private CommandSource source;

    @Override
    public BanBuilder user(User user) {
        checkNotNull(user, "User cannot be null!");
        checkState(banType == BanType.USER_BAN, "Cannot set a User if the BanType is not BanType.USER_BAN!");
        this.user = user;
        return this;
    }

    @Override
    public BanBuilder address(InetAddress address) {
        checkNotNull(address, "Address cannot be null!");
        checkState(banType == BanType.IP_BAN, "Cannot set an InetAddress if the BanType is not BanType.IP_BAN!");
        this.address = address;
        return this;
    }

    @Override
    public BanBuilder type(BanType type) {
        checkNotNull(type, "BanType cannot be null!");
        if (type == BanType.IP_BAN) {
            this.user = null;
        } else {
            this.address = null;
        }
        this.banType = type;
        return this;
    }

    @Override
    public BanBuilder reason(Text.Literal reason) {
        checkNotNull(reason, "Reason cannot be null!");
        this.reason = reason;
        return this;
    }

    @Override
    public BanBuilder startDate(Date date) {
        checkNotNull(date, "Start date cannot be null!");
        this.start = date;
        return this;
    }

    @Override
    public BanBuilder expirationDate(@Nullable Date date) {
        this.end = date;
        return this;
    }

    @Override
    public BanBuilder source(@Nullable CommandSource source) {
        this.source = source;
        return this;
    }

    @Override
    public Ban build() {
        checkState(this.banType != null, "BanType cannot be null!");
        checkState(this.reason != null, "Reason cannot be null!");

        String sourceName = this.source != null ? this.source.getName() : null;

        if (this.banType == BanType.USER_BAN) {
            checkState(this.user != null, "User cannot be null!");
            return (Ban) new UserListBansEntry((GameProfile) this.user.getProfile(), this.start, sourceName, this.end, Texts.legacy().to(this.reason));
        } else {
            checkState(this.address != null, "Address cannot be null!");

            // This *should* be a static method, but apparently not...
            BanList ipBans = MinecraftServer.getServer().getConfigurationManager().getBannedIPs();
            return (Ban) new IPBanEntry(ipBans.addressToString(new InetSocketAddress(address, 0)), this.start, sourceName, this.end, Texts.legacy().to(this.reason));
        }
    }
}
