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
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandTeleport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import java.util.Set;

@Mixin(CommandTeleport.class)
public abstract class MixinCommandTeleport extends CommandBase {

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;setPlayerLocation(DDDFFLjava/util/Set;)V"), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPlayerTeleport(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci, int i, Entity entity, int lvt_5_2_, CoordinateArg commandbase$coordinatearg, CoordinateArg commandbase$coordinatearg1, CoordinateArg commandbase$coordinatearg2, CoordinateArg commandbase$coordinatearg3, CoordinateArg commandbase$coordinatearg4, Set<SPacketPlayerPosLook.EnumFlags> set, float f, float f1) {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        double x = commandbase$coordinatearg.getAmount();
        double y = commandbase$coordinatearg1.getAmount();
        double z = commandbase$coordinatearg2.getAmount();
        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(player, x, y, z, f, f1);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            Vector3d position = event.getToTransform().getPosition();
            player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch(), set);
            entity.setRotationYawHead((float) event.getToTransform().getYaw());
            notifyCommandListener(sender, this, "commands.tp.success.coordinates", new Object[] {entity.getName(), position.getX(), position.getY(), position.getZ()});
            ci.cancel();
        }
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;setPlayerLocation(DDDFF)V"), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPlayerTeleport2(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci, int i, Entity entity, Entity entity1) {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, entity1.posX, entity1.posY, entity1.posZ, entity1.rotationYaw, entity1.rotationPitch);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            Vector3d position = event.getToTransform().getPosition();
            player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
            notifyCommandListener(sender, this, "commands.tp.success", new Object[] {entity.getName(), entity1.getName()});
            ci.cancel();
        }
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setLocationAndAngles(DDDFF)V", ordinal = 0), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onEntityTeleport(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci, int i, Entity entity, int lvt_5_2_, CoordinateArg commandbase$coordinatearg, CoordinateArg commandbase$coordinatearg1, CoordinateArg commandbase$coordinatearg2, CoordinateArg commandbase$coordinatearg3, CoordinateArg commandbase$coordinatearg4, float f2, float f3) {
        double x = commandbase$coordinatearg.getResult();
        double y = commandbase$coordinatearg1.getResult();
        double z = commandbase$coordinatearg2.getResult();
        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, x, y, z, f2, f3);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            Vector3d position = event.getToTransform().getPosition();
            entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
            entity.setRotationYawHead((float) event.getToTransform().getYaw());
            notifyCommandListener(sender, this, "commands.tp.success.coordinates", new Object[] {entity.getName(), position.getX(), position.getY(), position.getZ()});
            ci.cancel();
        }
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setLocationAndAngles(DDDFF)V", ordinal = 1), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onEntityTeleport2(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci, int i, Entity entity, Entity entity1) {
        MoveEntityEvent.Position.Teleport event = SpongeCommonEventFactory.handleDisplaceEntityTeleportEvent(entity, entity1.posX, entity1.posY, entity1.posZ, entity1.rotationYaw, entity1.rotationPitch);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            Vector3d position = event.getToTransform().getPosition();
            entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
            notifyCommandListener(sender, this, "commands.tp.success", new Object[] {entity.getName(), entity1.getName()});
            ci.cancel();
        }
    }
}
