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
package org.spongepowered.common.mixin.core.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandPardonIp;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListIPBans;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

@Mixin(CommandPardonIp.class)
public abstract class CommandPardonIpMixin {

    private InetAddress address;

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Instead of matching with the regex, attempt to parse the hostname
     *     with {@link InetAddress#getByName(String)} which supports all forms of
     *     IPs.
     *     Fixes MC-97885: https://bugs.mojang.com/browse/MC-97885
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;matches()Z", remap = false))
    private boolean impl$stringInetAddress(final Matcher matcher, final MinecraftServer server, final ICommandSender sender, final String[] args) {
        try {
            this.address = InetAddress.getByName(args[0]);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason If we have already parsed the IP, use the string representation
     *     of it instead of the input argument to have a consistent format.
     *     E.g. ::1 would be converted to 0:0:0:0:0:1
     */
    @Redirect(method = "execute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/UserListIPBans;removeEntry(Ljava/lang/Object;)V"))
    private void impl$removeBanEntryWithAddress(final UserListIPBans banList, Object address) {
        if (this.address != null) {
            address = this.address.getHostAddress();
            this.address = null;
        }

        banList.func_152684_c((String) address);
    }

}
