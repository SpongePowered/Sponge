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
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.MathHelper;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.EnumSet;
import java.util.Set;

@Mixin(CommandTeleport.class)
public abstract class MixinCommandTeleport extends CommandBase {

    /**
     * @author blood - May 31st, 2016
     * @reason to fix LVT errors with SpongeForge
     *     
     * @param sender The command source
     * @param args The command arguments
     */
    @Overwrite
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.tp.usage", new Object[0]);
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
                entity = getEntity(sender, args[0]);
                i = 1;
            }

            if (args.length != 1 && args.length != 2)
            {
                if (args.length < i + 3)
                {
                    throw new WrongUsageException("commands.tp.usage", new Object[0]);
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
                        Set<S08PacketPlayerPosLook.EnumFlags> set = EnumSet.<S08PacketPlayerPosLook.EnumFlags>noneOf(S08PacketPlayerPosLook.EnumFlags.class);

                        if (commandbase$coordinatearg.func_179630_c())
                        {
                            set.add(S08PacketPlayerPosLook.EnumFlags.X);
                        }

                        if (commandbase$coordinatearg1.func_179630_c())
                        {
                            set.add(S08PacketPlayerPosLook.EnumFlags.Y);
                        }

                        if (commandbase$coordinatearg2.func_179630_c())
                        {
                            set.add(S08PacketPlayerPosLook.EnumFlags.Z);
                        }

                        if (commandbase$coordinatearg4.func_179630_c())
                        {
                            set.add(S08PacketPlayerPosLook.EnumFlags.X_ROT);
                        }

                        if (commandbase$coordinatearg3.func_179630_c())
                        {
                            set.add(S08PacketPlayerPosLook.EnumFlags.Y_ROT);
                        }

                        float f = (float)commandbase$coordinatearg3.func_179629_b();

                        if (!commandbase$coordinatearg3.func_179630_c())
                        {
                            f = MathHelper.wrapAngleTo180_float(f);
                        }

                        float f1 = (float)commandbase$coordinatearg4.func_179629_b();

                        if (!commandbase$coordinatearg4.func_179630_c())
                        {
                            f1 = MathHelper.wrapAngleTo180_float(f1);
                        }

                        if (f1 > 90.0F || f1 < -90.0F)
                        {
                            f1 = MathHelper.wrapAngleTo180_float(180.0F - f1);
                            f = MathHelper.wrapAngleTo180_float(f + 180.0F);
                        }

                        // Sponge start
                        double x = commandbase$coordinatearg.func_179629_b();
                        double y = commandbase$coordinatearg1.func_179629_b();
                        double z = commandbase$coordinatearg2.func_179629_b();
                        DisplaceEntityEvent.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, x, y, z, f, f1);
                        if (event.isCancelled()) {
                            return;
                        }

                        entity.mountEntity((Entity)null);
                        Vector3d position = event.getToTransform().getPosition();
                        ((EntityPlayerMP)entity).playerNetServerHandler.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch(), set);
                        entity.setRotationYawHead((float) event.getToTransform().getYaw());
                        // Sponge end
                    }
                    else
                    {
                        float f2 = (float)MathHelper.wrapAngleTo180_double(commandbase$coordinatearg3.func_179628_a());
                        float f3 = (float)MathHelper.wrapAngleTo180_double(commandbase$coordinatearg4.func_179628_a());

                        if (f3 > 90.0F || f3 < -90.0F)
                        {
                            f3 = MathHelper.wrapAngleTo180_float(180.0F - f3);
                            f2 = MathHelper.wrapAngleTo180_float(f2 + 180.0F);
                        }

                        // Sponge start
                        double x = commandbase$coordinatearg.func_179628_a();
                        double y = commandbase$coordinatearg1.func_179628_a();
                        double z = commandbase$coordinatearg2.func_179628_a();
                        DisplaceEntityEvent.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, x, y, z, f2, f3);
                        if (event.isCancelled()) {
                            return;
                        }

                        Vector3d position = event.getToTransform().getPosition();
                        entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                        entity.setRotationYawHead((float) event.getToTransform().getYaw());
                        // Sponge end
                    }

                    notifyOperators(sender, this, "commands.tp.success.coordinates", new Object[] {entity.getName(), Double.valueOf(commandbase$coordinatearg.func_179628_a()), Double.valueOf(commandbase$coordinatearg1.func_179628_a()), Double.valueOf(commandbase$coordinatearg2.func_179628_a())});
                }
            }
            else
            {
                Entity entity1 = getEntity(sender, args[args.length - 1]);

                if (entity1.worldObj != entity.worldObj)
                {
                    throw new CommandException("commands.tp.notSameDimension", new Object[0]);
                }
                else
                {
                    entity.mountEntity((Entity)null);

                    if (entity instanceof EntityPlayerMP)
                    {
                        // Sponge start
                        DisplaceEntityEvent.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, entity1.posX, entity1.posY, entity1.posZ, entity1.rotationYaw, entity1.rotationPitch);
                        if (event.isCancelled()) {
                            return;
                        }

                        Vector3d position = event.getToTransform().getPosition();
                        ((EntityPlayerMP)entity).playerNetServerHandler.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                        // Sponge end
                    }
                    else
                    {
                        DisplaceEntityEvent.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, entity1.posX, entity1.posY, entity1.posZ, entity1.rotationYaw, entity1.rotationPitch);
                        if (event.isCancelled()) {
                            return;
                        }

                        Vector3d position = event.getToTransform().getPosition();
                        entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                    }

                    notifyOperators(sender, this, "commands.tp.success", new Object[] {entity.getName(), entity1.getName()});
                }
            }
        }
    }
}
