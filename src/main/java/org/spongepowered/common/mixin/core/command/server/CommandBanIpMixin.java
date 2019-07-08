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
import net.minecraft.command.server.CommandBanIp;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

@Mixin(CommandBanIp.class)
public abstract class CommandBanIpMixin {

    @Shadow protected abstract void banIp(MinecraftServer server, ICommandSender sender, String ipAddress, @Nullable String banReason);

    private InetAddress address;

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Instead of matching with the regex, attempt to parse the hostname
     *     with {@link InetAddress#getByName(String)} which supports all forms of
     *     IPs.
     *     Fixes MC-97885: https://bugs.mojang.com/browse/MC-97885
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;matches()Z", remap = false))
    private boolean impl$TryParseWithInetAddress(final Matcher matcher, final MinecraftServer server, final ICommandSender sender, final String[] args) {
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
            target = "Lnet/minecraft/command/server/CommandBanIp;banIp(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;Ljava/lang/String;Ljava/lang/String;)V",
            ordinal = 0))
    private void impl$useHostAddress(final CommandBanIp command, final MinecraftServer server, final ICommandSender sender, String address, final String reason) {
        if (this.address != null) {
            address = this.address.getHostAddress();
            this.address = null;
        }

        banIp(server, sender, address, reason);
    }


}
