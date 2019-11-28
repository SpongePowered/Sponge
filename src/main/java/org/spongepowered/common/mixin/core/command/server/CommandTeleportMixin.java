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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.entity.EntityUtil;

import java.util.EnumSet;
import java.util.Set;

@Mixin(CommandTeleport.class)
public abstract class CommandTeleportMixin extends CommandBase {

    // This boolean is added in order to make minimal changes to 'execute'.
    // It is set to true if the events fired in 'func_189863_a' are not cancelled.
    // This allows us to prevent calling 'notifyCommandListener' if the event is cancelled.
    private static boolean impl$shouldNotifyCommandListener = false;

    /**
     * @author Aaron1011 - August 15, 2016
     * @reason Prevent 'notifyCommandListener' from being called the event is cancelled
     */
    @Overwrite
    @Override
    public void func_184881_a(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw new WrongUsageException("commands.teleport.usage", new Object[0]);
        }
        else
        {
            final Entity entity = func_184885_b(server, sender, args[0]);

            if (entity.field_70170_p != null)
            {
                // int i = 4096;
                final Vec3d vec3d = sender.func_174791_d();
                int j = 1;
                final CommandBase.CoordinateArg commandbase$coordinatearg = func_175770_a(vec3d.field_72450_a, args[j++], true);
                final CommandBase.CoordinateArg commandbase$coordinatearg1 = func_175767_a(vec3d.field_72448_b, args[j++], -4096, 4096, false);
                final CommandBase.CoordinateArg commandbase$coordinatearg2 = func_175770_a(vec3d.field_72449_c, args[j++], true);
                final Entity entity1 = sender.func_174793_f() == null ? entity : sender.func_174793_f();
                final CommandBase.CoordinateArg commandbase$coordinatearg3 = func_175770_a(args.length > j ? (double)entity1.field_70177_z : (double)entity.field_70177_z, args.length > j ? args[j] : "~", false);
                ++j;
                final CommandBase.CoordinateArg commandbase$coordinatearg4 = func_175770_a(args.length > j ? (double)entity1.field_70125_A : (double)entity.field_70125_A, args.length > j ? args[j] : "~", false);
                // Sponge start - check impl$shouldNotifyCommandListener before calling 'notifyCommandListener'

                // Guard against any possible re-entrance
                final boolean shouldNotify = impl$shouldNotifyCommandListener;

                doTeleport(entity, commandbase$coordinatearg, commandbase$coordinatearg1, commandbase$coordinatearg2, commandbase$coordinatearg3, commandbase$coordinatearg4);
                if (impl$shouldNotifyCommandListener) {
                    func_152373_a(sender, this, "commands.tp.success.coordinates", new Object[] {entity.func_70005_c_(), Double.valueOf(commandbase$coordinatearg.func_179628_a()), Double.valueOf(commandbase$coordinatearg1.func_179628_a()), Double.valueOf(commandbase$coordinatearg2.func_179628_a())});
                }
                impl$shouldNotifyCommandListener = shouldNotify;
                // Sponge end
            }
        }
    }

    /**
     * @author Aaron1011 - August 15, 2016
     * @reason Muliple modification points are needed, so an overwrite is easier
     */
    @Overwrite
    private static void doTeleport(final Entity p_189862_0_, final CommandBase.CoordinateArg p_189862_1_, final CommandBase.CoordinateArg p_189862_2_, final CommandBase.CoordinateArg p_189862_3_, final CommandBase.CoordinateArg p_189862_4_, final CommandBase.CoordinateArg p_189862_5_)
    {
        if (p_189862_0_ instanceof EntityPlayerMP)
        {
            final Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags>noneOf(SPacketPlayerPosLook.EnumFlags.class);
            float f = (float)p_189862_4_.func_179629_b();

            if (p_189862_4_.func_179630_c())
            {
                set.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
            }
            else
            {
                f = MathHelper.func_76142_g(f);
            }

            float f1 = (float)p_189862_5_.func_179629_b();

            if (p_189862_5_.func_179630_c())
            {
                set.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
            }
            else
            {
                f1 = MathHelper.func_76142_g(f1);
            }

            // Sponge start
            final EntityPlayerMP player = (EntityPlayerMP) p_189862_0_;
            final double x = p_189862_1_.func_179629_b();
            final double y = p_189862_2_.func_179629_b();
            final double z = p_189862_3_.func_179629_b();
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.COMMAND);
                final MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent(player, x, y, z, f, f1);
                if (event.isCancelled()) {
                    return;
                }

                p_189862_0_.func_184210_p();
                final Vector3d position = event.getToTransform().getPosition();
                ((EntityPlayerMP)p_189862_0_).field_71135_a.func_175089_a(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch(), set);
                p_189862_0_.func_70034_d((float) event.getToTransform().getYaw());
            }
            // Sponge end
        }
        else
        {
            final float f2 = (float)MathHelper.func_76138_g(p_189862_4_.func_179628_a());
            float f3 = (float)MathHelper.func_76138_g(p_189862_5_.func_179628_a());
            f3 = MathHelper.func_76131_a(f3, -90.0F, 90.0F);

            // Sponge start
            final double x = p_189862_1_.func_179628_a();
            final double y = p_189862_2_.func_179628_a();
            final double z = p_189862_3_.func_179628_a();
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.COMMAND);
                final MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent(p_189862_0_, x, y, z, f2, f3);
                if (event.isCancelled()) {
                    return;
                }

                final Vector3d position = event.getToTransform().getPosition();
                p_189862_0_.func_70012_b(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
                p_189862_0_.func_70034_d((float) event.getToTransform().getYaw());
            }
            // Sponge end
        }

        if (!(p_189862_0_ instanceof EntityLivingBase) || !((EntityLivingBase)p_189862_0_).func_184613_cA())
        {
            p_189862_0_.field_70181_x = 0.0D;
            p_189862_0_.field_70122_E = true;
        }

        // Sponge start - set 'impl$shouldNotifyCommandListener' to 'true' if we make it to the end of the method (the event wasn't cancelled)
        impl$shouldNotifyCommandListener = true;
        // Sponge end
    }

}
