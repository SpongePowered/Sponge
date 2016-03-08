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
import net.minecraft.command.server.CommandSummon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

@Mixin(CommandSummon.class)
public abstract class MixinCommandSummon extends CommandBase {

    private static final String LIGHTNINGBOLT_CLASS = "class=net/minecraft/entity/effect/EntityLightningBolt";
    private static final String WORLD_SPAWN_ENTITY = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z";
    private static final String ENTITY_LIST_CREATE_FROM_NBT =
            "Lnet/minecraft/entity/EntityList;createEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;";

    @Redirect(method = "processCommand", at = @At(value = "INVOKE", target = ENTITY_LIST_CREATE_FROM_NBT))
    private Entity onAttemptSpawnEntity(NBTTagCompound nbt, World world, ICommandSender sender, String[] args) {
        if ("Minecart".equals(nbt.getString(NbtDataUtil.ENTITY_TYPE_ID))) {
            nbt.setString(NbtDataUtil.ENTITY_TYPE_ID,
                    EntityMinecart.EnumMinecartType.byNetworkID(nbt.getInteger(NbtDataUtil.MINECART_TYPE)).getName());
            nbt.removeTag(NbtDataUtil.MINECART_TYPE);
        }
        Class<? extends Entity> entityClass = EntityList.stringToClassMapping.get(nbt.getString(NbtDataUtil.ENTITY_TYPE_ID));
        if (entityClass == null) {
            return null;
        }
        EntityType type = EntityTypeRegistryModule.getInstance().getForClass(entityClass);
        if (type == null) {
            return null;
        }
        Vec3 vec3 = sender.getPositionVector();

        double x = vec3.xCoord;
        double y = vec3.yCoord;
        double z = vec3.zCoord;

        try {
            if (args.length >= 4) {
                x = parseDouble(x, args[1], true);
                y = parseDouble(y, args[2], false);
                z = parseDouble(z, args[3], true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Transform<org.spongepowered.api.world.World> transform = new Transform<>(((org.spongepowered.api.world.World) world), new Vector3d(x, y, z));
        SpawnCause cause = getSpawnCause(sender);
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), type, transform);
        SpongeImpl.postEvent(event);
        return event.isCancelled() ? null : EntityList.createEntityFromNBT(nbt, world);
    }

    @Redirect(method = "processCommand", at = @At(value = "INVOKE", target = WORLD_SPAWN_ENTITY))
    private boolean onSpawnEntity(World world, Entity entity, ICommandSender sender, String[] args) {
        return ((org.spongepowered.api.world.World) world).spawnEntity((org.spongepowered.api.entity.Entity) entity,
                Cause.of(NamedCause.source(getSpawnCause(sender))));
    }

    @Inject(method = "processCommand", at = @At(value = "NEW", args = LIGHTNINGBOLT_CLASS), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onProcess(ICommandSender commandSender, String[] args, CallbackInfo callbackInfo, String unused, BlockPos position, Vec3 vector,
            double x, double y, double z, World target) {
        Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) target, new Vector3d(x, y, z));

        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(getSpawnCause(commandSender))),
                EntityTypes.LIGHTNING, transform);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    private static SpawnCause getSpawnCause(ICommandSender commandSender) {
        if (commandSender instanceof Entity) {
            return EntitySpawnCause.builder()
                    .entity((org.spongepowered.api.entity.Entity) commandSender)
                    .type(SpawnTypes.PLACEMENT)
                    .build();
        } else if (commandSender instanceof TileEntity) {
            return BlockSpawnCause.builder()
                    .block(((TileEntity) commandSender).getLocation().createSnapshot())
                    .type(SpawnTypes.PLACEMENT)
                    .build();
        } else {
            return SpawnCause.builder()
                    .type(SpawnTypes.PLACEMENT)
                    .build();
        }
    }
}
