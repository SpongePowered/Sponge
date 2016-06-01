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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandTeleport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.EnumSet;
import java.util.Set;

@Mixin(CommandTeleport.class)
public abstract class MixinCommandTeleport extends CommandBase {

    /**
     * @author blood - May 31st, 2016
     * @author gabizou - May 31st, 2016 - Update to 1.9.4
     * @reason to fix LVT errors with SpongeForge
     *
     * @param sender The command source
     * @param args The command arguments
     */
    @Overwrite
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.tp.usage");
        }
        else
        {
            int i = 0;
            Entity entity;

            if (args.length != 2 && args.length != 4 && args.length != 6)
            {
                entity = getCommandSenderAsPlayer(sender);
            }
            else
            {
                entity = getEntity(server, sender, args[0]);
                i = 1;
            }

            if (args.length != 1 && args.length != 2)
            {
                if (args.length < i + 3)
                {
                    throw new WrongUsageException("commands.tp.usage");
                }
                else if (entity.worldObj != null)
                {
                    int lvt_5_2_ = i + 1;
                    CommandBase.CoordinateArg commandbase$coordinatearg = parseCoordinate(entity.posX, args[i], true);
                    CommandBase.CoordinateArg commandbase$coordinatearg1 = parseCoordinate(entity.posY, args[lvt_5_2_++], 0, 0, false);
                    CommandBase.CoordinateArg commandbase$coordinatearg2 = parseCoordinate(entity.posZ, args[lvt_5_2_++], true);
                    CommandBase.CoordinateArg commandbase$coordinatearg3 = parseCoordinate((double)entity.rotationYaw, args.length > lvt_5_2_ ? args[lvt_5_2_++] : "~", false);
                    CommandBase.CoordinateArg commandbase$coordinatearg4 = parseCoordinate((double)entity.rotationPitch, args.length > lvt_5_2_ ? args[lvt_5_2_] : "~", false);

                    if (entity instanceof EntityPlayerMP)
                    {
                        Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

                        if (commandbase$coordinatearg.isRelative())
                        {
                            set.add(SPacketPlayerPosLook.EnumFlags.X);
                        }

                        if (commandbase$coordinatearg1.isRelative())
                        {
                            set.add(SPacketPlayerPosLook.EnumFlags.Y);
                        }

                        if (commandbase$coordinatearg2.isRelative())
                        {
                            set.add(SPacketPlayerPosLook.EnumFlags.Z);
                        }

                        if (commandbase$coordinatearg4.isRelative())
                        {
                            set.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
                        }

                        if (commandbase$coordinatearg3.isRelative())
                        {
                            set.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
                        }

                        float f = (float)commandbase$coordinatearg3.getAmount();

                        if (!commandbase$coordinatearg3.isRelative())
                        {
                            f = MathHelper.wrapDegrees(f);
                        }

                        float f1 = (float)commandbase$coordinatearg4.getAmount();

                        if (!commandbase$coordinatearg4.isRelative())
                        {
                            f1 = MathHelper.wrapDegrees(f1);
                        }


                        // Sponge start
                        EntityPlayerMP player = (EntityPlayerMP) sender;
                        double x = commandbase$coordinatearg.getAmount();
                        double y = commandbase$coordinatearg1.getAmount();
                        double z = commandbase$coordinatearg2.getAmount();
                        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(player, x, y, z, f, f1);
                        if (event.isCancelled()) {
                            return;
                        }

                        entity.dismountRidingEntity();
                        Vector3d position = event.getToTransform().getPosition();
                        ((EntityPlayerMP)entity).connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch(), set);
                        entity.setRotationYawHead((float) event.getToTransform().getYaw());
                        // Sponge end
                    }
                    else
                    {
                        float f2 = (float)MathHelper.wrapDegrees(commandbase$coordinatearg3.getResult());
                        float f3 = (float)MathHelper.wrapDegrees(commandbase$coordinatearg4.getResult());

                        f3 = MathHelper.clamp_float(f3, -90.0F, 90.0F);

                        // Sponge start
                        double x = commandbase$coordinatearg.getResult();
                        double y = commandbase$coordinatearg1.getResult();
                        double z = commandbase$coordinatearg2.getResult();
                        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, x, y, z, f2, f3);
                        if (event.isCancelled()) {
                            return;
                        }

                        Vector3d position = event.getToTransform().getPosition();
                        entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                        entity.setRotationYawHead((float) event.getToTransform().getYaw());
                        // Sponge end
                    }

                    notifyCommandListener(sender, this, "commands.tp.success.coordinates", entity.getName(), commandbase$coordinatearg.getResult(), commandbase$coordinatearg1.getResult(), commandbase$coordinatearg2.getResult());
                }
            }
            else
            {
                Entity entity1 = getEntity(server, sender, args[args.length - 1]);

                if (entity1.worldObj != entity.worldObj)
                {
                    throw new CommandException("commands.tp.notSameDimension");
                }
                else
                {
                    entity.dismountRidingEntity();

                    if (entity instanceof EntityPlayerMP)
                    {
                        // Sponge start
                        EntityPlayerMP player = (EntityPlayerMP) sender;
                        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, entity1.posX, entity1.posY, entity1.posZ, entity1.rotationYaw, entity1.rotationPitch);
                        if (event.isCancelled()) {
                            return;
                        }

                        Vector3d position = event.getToTransform().getPosition();
                        player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                        // Sponge end
                    }
                    else
                    {
                        // Sponge Start - Events
                        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, entity1.posX, entity1.posY, entity1.posZ, entity1.rotationYaw, entity1.rotationPitch);
                        if (event.isCancelled()) {
                            return;
                        }

                        Vector3d position = event.getToTransform().getPosition();
                        entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                        // Sponge End
                    }

                    notifyCommandListener(sender, this, "commands.tp.success", entity.getName(), entity1.getName());
                }
            }
        }
    }
}
