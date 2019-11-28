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
package org.spongepowered.common.mixin.api.command;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

@Mixin(value = {EntityPlayerMP.class, TileEntityCommandBlock.class, EntityMinecartCommandBlock.class, MinecraftServer.class, RConConsoleSource.class},
        targets = "net/minecraft/tileentity/TileEntitySign$1", priority = 899) // Allows for other implementations of CommandSource to override/write
public abstract class CommandSourceMixin_API implements CommandSource {

    private MessageChannel channel = MessageChannel.TO_ALL;

    @Override
    public void sendMessage(final Text message) {
        checkNotNull(message, "message");
        ((CommandSourceBridge) this).bridge$asICommandSender().func_145747_a(SpongeTexts.toComponent(message));
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.channel;
    }

    @Override
    public void setMessageChannel(final MessageChannel channel) {
        this.channel = checkNotNull(channel, "channel");
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        if (this instanceof User && !((User) this).isOnline()) {
            return Optional.empty();
        }
        return Optional.<CommandSource>of(this);
    }

}
