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
import net.minecraft.entity.Entity;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.WrapperCommandSource;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(targets = IMixinCommandSender.EXECUTE_COMMAND_SENDER)
@NonnullByDefault
public abstract class MixinCommandExecuteAtSender implements ProxySource, IMixinCommandSource, IMixinCommandSender {

    @Shadow(aliases = {"field_174804_a", "val$entity", "a"}) @Final private Entity field_174804_a;
    @Shadow(aliases = {"field_189650_b", "val$sender", "b"}) @Final private ICommandSender field_189650_b;

    @Override
    public void sendMessage(Text message) {
        getOriginalSource().sendMessages(message);
    }

    @Override
    public void sendMessages(Text... messages) {
        getOriginalSource().sendMessages(messages);
    }

    @Override
    public void sendMessages(Iterable<Text> messages) {
        getOriginalSource().sendMessages(messages);
    }

    @Override
    public String getName() {
        return this.field_174804_a.getName();
    }

    @Override
    public String getIdentifier() {
        return getOriginalSource().getIdentifier();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of((CommandSource) this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return getOriginalSource().getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return getOriginalSource().getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return getOriginalSource().getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return getOriginalSource().hasPermission(contexts, permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getOriginalSource().hasPermission(permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return getOriginalSource().getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(Subject parent) {
        return getOriginalSource().isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return getOriginalSource().isChildOf(contexts, parent);
    }

    @Override
    public List<Subject> getParents() {
        return getOriginalSource().getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return getOriginalSource().getParents(contexts);
    }

    @Override
    public MessageChannel getMessageChannel() {
        return getOriginalSource().getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        getOriginalSource().setMessageChannel(channel);
    }

    @Override
    public Set<Context> getActiveContexts() {
        return getOriginalSource().getActiveContexts();
    }

    @Override
    public CommandSource getOriginalSource() {
        return WrapperCommandSource.of(this.field_189650_b);
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }

    @Override
    public ICommandSender asICommandSender() {
        return (ICommandSender) this;
    }

}
