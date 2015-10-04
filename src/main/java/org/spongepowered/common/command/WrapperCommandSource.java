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

import net.minecraft.command.ICommandSender;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.LocatedSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.permission.base.SpongeSubject;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class WrapperCommandSource extends SpongeSubject implements CommandSource {

    final ICommandSender sender;
    private final MemorySubjectData data;

    private WrapperCommandSource(ICommandSender sender) {
        this.sender = sender;
        this.data = new MemorySubjectData(Sponge.getGame().getServiceManager().provide(PermissionService.class).get());

        // ICommandSenders have a *very* basic understanding of permissions, so
        // get what we can.
        this.data.setPermission(SubjectData.GLOBAL_CONTEXT, "minecraft.selector",
                Tristate.fromBoolean(this.sender.canCommandSenderUseCommand(1, "@")));
        this.data.setPermission(SubjectData.GLOBAL_CONTEXT, "minecraft.commandblock",
                Tristate.fromBoolean(this.sender.canCommandSenderUseCommand(2, "")));
        for (CommandMapping command : Sponge.getGame().getCommandDispatcher().getCommands()) {
            if (command.getCallable() instanceof MinecraftCommandWrapper) {
                MinecraftCommandWrapper wrapper = (MinecraftCommandWrapper) command.getCallable();
                this.data.setPermission(SubjectData.GLOBAL_CONTEXT, wrapper.getCommandPermission(),
                        Tristate.fromBoolean(wrapper.command.canCommandSenderUseCommand(sender)));
            }
        }
    }

    @Override
    public String getIdentifier() {
        return this.sender.getCommandSenderName();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of((CommandSource) this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        SpongePermissionService permission = (SpongePermissionService) Sponge.getGame().getServiceManager().provide(PermissionService.class).get();
        return permission.getSubjects(SpongePermissionService.SUBJECTS_SYSTEM);
    }

    @Override
    public MemorySubjectData getSubjectData() {
        return data;
    }

    @Override
    public String getName() {
        return this.sender.getCommandSenderName();
    }

    @Override
    public void sendMessage(Text... messages) {
        for (Text text : messages) {
            this.sender.addChatMessage(SpongeTexts.toComponent(text));
        }
    }

    @Override
    public void sendMessage(Iterable<Text> messages) {
        for (Text text : messages) {
            this.sender.addChatMessage(SpongeTexts.toComponent(text));
        }
    }

    @Override
    public MessageSink getMessageSink() {
        return Sponge.getGame().getServer().getBroadcastSink();
    }

    @Override
    public void setMessageSink(MessageSink sink) {
        // TODO Should I throw an exception here?
    }

    public static CommandSource of(ICommandSender sender) {
        if (sender instanceof IMixinCommandSender) {
            return ((IMixinCommandSender) sender).asCommandSource();
        }
        if (sender instanceof WrapperICommandSender) {
            return ((WrapperICommandSender) sender).source;
        }
        if (sender.getCommandSenderEntity() != null || !VecHelper.VEC3_ORIGIN.equals(sender.getPositionVector())) {
            return new Located(sender);
        }
        return new WrapperCommandSource(sender);
    }

    public static class Located extends WrapperCommandSource implements LocatedSource {

        Located(ICommandSender sender) {
            super(sender);
        }

        @Override
        public Location<World> getLocation() {
            return new Location<World>(getWorld(), VecHelper.toVector(this.sender.getPositionVector()));
        }

        @Override
        public World getWorld() {
            return (World) this.sender.getEntityWorld();
        }

    }

}
