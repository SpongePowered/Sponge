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
package org.spongepowered.common.mixin.core.network.rcon;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.command.ICommandSender;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.network.rcon.RConThreadClient;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.network.rcon.RConConsoleSourceBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;

import javax.annotation.Nullable;

@Mixin(RConConsoleSource.class)
public abstract class RConConsoleSourceMixin implements ICommandSender, CommandSourceBridge, CommandSenderBridge, RConConsoleSourceBridge,
    SubjectBridge {

    @Shadow @Final private StringBuffer buffer;

    @Nullable private RConThreadClient impl$clientThread;

    @Override
    public void bridge$setConnection(final RConThreadClient conn) {
        this.impl$clientThread = conn;
    }

    @Override
    public RConThreadClient bridge$getClient() {
        return checkNotNull(this.impl$clientThread, "RCon Client is null");
    }

    /**
     * Add newlines between output lines for a command
     * @param component text coming in
     */
    @Inject(method = "sendMessage", at = @At("RETURN"))
    private void impl$addNewlines(final ITextComponent component, final CallbackInfo ci) {
        this.buffer.append('\n');
    }

    @Override
    public String bridge$getIdentifier() {
        return func_70005_c_();
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.TRUE;
    }

    @Override
    public ICommandSender bridge$asICommandSender() {
        return this;
    }

    @Override
    public CommandSource bridge$asCommandSource() {
        return (CommandSource) this;
    }
}
