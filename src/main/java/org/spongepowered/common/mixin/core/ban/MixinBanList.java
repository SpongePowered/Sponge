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
package org.spongepowered.common.mixin.core.ban;

import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListBansEntry;
import org.spongepowered.api.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.ban.IMixinBanList;
import org.spongepowered.common.interfaces.ban.IMixinBanLogic;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

@Mixin(BanList.class)
public abstract class MixinBanList extends UserList implements IMixinBanList {

    @Shadow public abstract String addressToString(SocketAddress address);

    // Despite the name, this is actually a list of IP bans

    public MixinBanList(File saveFile) {
        super(saveFile);
    }

    @Override
    public Collection<IPBanEntry> getBans(InetAddress address) {
        return (Collection) this.getEntries().get(this.inetToString(address));
    }

    @Override
    public boolean isBanned(InetAddress address) {
        return this.values.containsKey(this.inetToString(address));
    }

    @Override
    public void pardon(InetAddress address){
        for (IPBanEntry entry: this.getBans(address)) {
            this.removeEntry(entry.getValue());
        }
    }

    private String inetToString(InetAddress address) {
        return this.addressToString(new InetSocketAddress(address, 0));
    }

}
