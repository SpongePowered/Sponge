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

import com.google.common.base.Optional;
import net.minecraft.command.ICommandSender;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.ProxySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.command.WrapperCommandSource;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;

import java.util.List;
import java.util.Set;

@Mixin(targets = IMixinCommandSender.EXECUTE_COMMAND_SENDER)
@NonnullByDefault
public abstract class MixinCommandExecuteAtSender implements ProxySource, IMixinCommandSource, IMixinCommandSender {

    @Shadow(aliases = "val$entity") private net.minecraft.entity.Entity field_174804_a;
    @Shadow(aliases = "val$sender") private ICommandSender field_174802_b;

    @Override
    public void sendMessage(Text... messages) {
        getInitiator().sendMessage(messages);
    }

    @Override
    public void sendMessage(Iterable<Text> messages) {
        getInitiator().sendMessage(messages);
    }

    @Override
    public String getName() {
        return this.field_174804_a.getCommandSenderName();
    }

    @Override
    public String getIdentifier() {
        return getInitiator().getIdentifier();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of((CommandSource) this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return getInitiator().getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return getInitiator().getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return getInitiator().getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return getInitiator().hasPermission(contexts, permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return getInitiator().hasPermission(permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return getInitiator().getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(Subject parent) {
        return getInitiator().isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return getInitiator().isChildOf(contexts, parent);
    }

    @Override
    public List<Subject> getParents() {
        return getInitiator().getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return getInitiator().getParents(contexts);
    }

    @Override
    public void setMessageSink(MessageSink sink) {
        getInitiator().setMessageSink(sink);
    }

    @Override
    public MessageSink getMessageSink() {
        return getInitiator().getMessageSink();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return getInitiator().getActiveContexts();
    }

    @Override
    public CommandSource getInitiator() {
        return WrapperCommandSource.of(this.field_174802_b);
    }

    @Override
    public Subject getSubject() {
        return getInitiator();
    }

    @Override
    public Entity getEntity() {
        return (Entity) field_174804_a;
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
