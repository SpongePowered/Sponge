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
package org.spongepowered.common.command;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.command.ICommandSender;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class SpongeProxySource implements ProxySource, CommandSourceBridge {

    final ICommandSender minecraft;
    private final CommandSource messageSource;
    private MessageChannel channel = MessageChannel.TO_ALL;
    private final Subject subjectDelegate;

    public SpongeProxySource(ICommandSender minecraft, CommandSource messageSource, Subject subjectDelegate) {
        this.minecraft = minecraft;
        this.messageSource = messageSource;
        this.subjectDelegate = subjectDelegate;
    }

    @Override
    public CommandSource getOriginalSource() {
        return this.messageSource;
    }

    @Override
    public String bridge$getIdentifier() {
        return this.subjectDelegate.getIdentifier();
    }

    @Override
    public ICommandSender bridge$asICommandSender() {
        return this.minecraft;
    }

    @Override
    public String getIdentifier() {
        return bridge$getIdentifier();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return this.subjectDelegate.getActiveContexts();
    }

    @Override
    public void sendMessage(Text message) {
        checkNotNull(message, "message");
        this.minecraft.func_145747_a(SpongeTexts.toComponent(message));
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.channel;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        checkNotNull(channel, "channel");
        this.channel = channel;
    }

    @Override
    public String getName() {
        return bridge$asICommandSender().func_70005_c_();
    }

    @Override
    public Locale getLocale() {
        return this.messageSource.getLocale();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of(this);
    }

    @Override
    public SubjectReference asSubjectReference() {
        return this.subjectDelegate.asSubjectReference();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.subjectDelegate.getContainingCollection();
    }

    @Override
    public boolean isSubjectDataPersisted() {
        return this.subjectDelegate.isSubjectDataPersisted();
    }

    @Override
    public SubjectData getSubjectData() {
        return this.subjectDelegate.getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return this.subjectDelegate.getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return this.subjectDelegate.hasPermission(contexts, permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.subjectDelegate.hasPermission(permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return this.subjectDelegate.getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(SubjectReference parent) {
        return this.subjectDelegate.isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
        return this.subjectDelegate.isChildOf(contexts, parent);
    }

    @Override
    public List<SubjectReference> getParents() {
        return this.subjectDelegate.getParents();
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        return this.subjectDelegate.getParents(contexts);
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return this.subjectDelegate.getOption(contexts, key);
    }

    @Override
    public Optional<String> getOption(String key) {
        return this.subjectDelegate.getOption(key);
    }

    public static final class Located extends SpongeProxySource implements Locatable {

        public Located(ICommandSender mixin, CommandSource messageSource, Subject subjectDelegate) {
            super(mixin, messageSource, subjectDelegate);
        }

        @Override
        public Location<World> getLocation() {
            return new Location<>((World) this.minecraft.func_130014_f_(), VecHelper.toVector3d(this.minecraft.func_174791_d()));
        }

        @Override
        public World getWorld() {
            return (World) this.minecraft.func_130014_f_();
        }
    }
}
