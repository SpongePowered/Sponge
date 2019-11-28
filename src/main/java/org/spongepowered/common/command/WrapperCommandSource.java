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
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.service.permission.base.SpongeSubject;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

public class WrapperCommandSource extends SpongeSubject implements CommandSource {

    final ICommandSender sender;
    private final PermissionService service;
    private final MemorySubjectData data;

    private WrapperCommandSource(ICommandSender sender) {
        this.sender = sender;
        this.service = SpongeImpl.getGame().getServiceManager().provideUnchecked(PermissionService.class);
        this.data = new MemorySubjectData(this.service);
        CommandPermissions.populateMinecraftPermissions(sender, this.data);
    }

    @Override
    public PermissionService getService() {
        return this.service;
    }

    @Override
    public String getIdentifier() {
        return this.sender.getName();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of((CommandSource) this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.service.loadCollection(PermissionService.SUBJECTS_SYSTEM).join();
    }

    @Override
    public MemorySubjectData getSubjectData() {
        return this.data;
    }

    @Override
    public String getName() {
        return this.sender.getName();
    }

    @Override
    public void sendMessage(Text message) {
        checkNotNull(message, "message");
        this.sender.sendMessage(SpongeTexts.toComponent(message));
    }

    @Override
    public MessageChannel getMessageChannel() {
        return SpongeImpl.getGame().getServer().getBroadcastChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        // TODO Should I throw an exception here?
    }

    public static CommandSource of(ICommandSender sender) {
        if (sender instanceof CommandSenderBridge) {
            return ((CommandSenderBridge) sender).bridge$asCommandSource();
        }
        if (sender instanceof WrapperICommandSender) {
            return ((WrapperICommandSender) sender).source;
        }
        if (sender.getCommandSenderEntity() != null || !Vec3d.ZERO.equals(sender.getPositionVector())) {
            return new Located(sender);
        }
        return new WrapperCommandSource(sender);
    }

    public static class Located extends WrapperCommandSource implements Locatable {

        Located(ICommandSender sender) {
            super(sender);
        }

        @Override
        public Location<World> getLocation() {
            return new Location<>((World) this.sender.getEntityWorld(), VecHelper.toVector3d(this.sender.getPositionVector()));
        }

    }

}
