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
package org.spongepowered.common.mixin.core.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.SignSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;

@Mixin(targets = IMixinCommandSender.SIGN_CLICK_SENDER)
public abstract class MixinSignCommandSender implements ICommandSender, IMixinCommandSender, SignSource, IMixinCommandSource {

    @Shadow private TileEntitySign field_174797_a;

    @Override
    public ICommandSender asICommandSender() {
        return this;
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }

    @Override
    public Player getInitiator() {
        return getEntity();
    }

    @Override
    public Sign getProvider() {
        return (Sign) field_174797_a;
    }

    @Override
    public Player getEntity() {
        return (Player) getCommandSenderEntity();
    }

}
