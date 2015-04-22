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
package org.spongepowered.common.text.selector;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.util.VecHelper;

public class SelectorResolver implements ICommandSender {

    private static final Function<CommandSource, String> GET_NAME =
            new Function<CommandSource, String>() {

                @Override
                public String apply(CommandSource input) {
                    return input.getName();
                }

            };

    private static Extent extentFromSource(CommandSource origin) {
        if (origin instanceof ICommandSender) {
            return (Extent) ((ICommandSender) origin).getEntityWorld();
        }
        return null;
    }

    private static Vec3 positionFromSource(CommandSource origin) {
        if (origin instanceof ICommandSender) {
            return ((ICommandSender) origin).getPositionVector();
        }
        return null;
    }

    private final World world;
    private final Vec3 position;
    private final Optional<CommandSource> original;
    private final Optional<ICommandSender> proxy;

    private SelectorResolver(Extent extent, Vec3 position,
            CommandSource original) {
        this.world = (World) extent;
        this.position = position;
        this.original = Optional.fromNullable(original);
        if (original instanceof ICommandSender) {
            this.proxy = Optional.of((ICommandSender) original);
        } else {
            this.proxy = Optional.absent();
        }
    }

    public SelectorResolver(Extent extent) {
        this(extent, null, null);
    }

    public SelectorResolver(Location location) {
        this(location.getExtent(), VecHelper.toVector(location.getPosition()),
                null);
    }

    public SelectorResolver(CommandSource origin) {
        this(extentFromSource(origin), positionFromSource(origin), origin);
    }

    @Override
    public String getCommandSenderName() {
        return original.transform(GET_NAME).or("SelectorResolver");
    }

    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText(getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent component) {
        if (proxy.isPresent()) {
            proxy.get().addChatMessage(component);
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        if (proxy.isPresent()) {
            return proxy.get().canCommandSenderUseCommand(permLevel,
                    commandName);
        }
        return permLevel == 1 && commandName.equals("@");
    }

    @Override
    public BlockPos getPosition() {
        return new BlockPos(this.position);
    }

    @Override
    public Vec3 getPositionVector() {
        return this.position;
    }

    @Override
    public World getEntityWorld() {
        return this.world;
    }

    @Override
    public Entity getCommandSenderEntity() {
        if (proxy.isPresent()) {
            return proxy.get().getCommandSenderEntity();
        }
        return null;
    }

    @Override
    public boolean sendCommandFeedback() {
        if (proxy.isPresent()) {
            return proxy.get().sendCommandFeedback();
        }
        return false;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {
        if (proxy.isPresent()) {
            proxy.get().setCommandStat(type, amount);
        }
    }

}
