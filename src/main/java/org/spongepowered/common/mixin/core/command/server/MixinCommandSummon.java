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
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

@Mixin(CommandSummon.class)
public abstract class MixinCommandSummon extends CommandBase {

    private static final String LIGHTNINGBOLT_CLASS = "class=net/minecraft/entity/effect/EntityLightningBolt";
    private static final String ENTITY_LIST_CREATE_FROM_NBT =
            "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;readWorldEntityPos(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;DDDZ)Lnet/minecraft/entity/Entity;";

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = ENTITY_LIST_CREATE_FROM_NBT))
    private Entity onAttemptSpawnEntity(NBTTagCompound nbt, World world, double x, double y, double z, boolean b, MinecraftServer server, ICommandSender sender, String[] args) {
        if ("Minecart".equals(nbt.getString(NbtDataUtil.ENTITY_TYPE_ID))) {
            nbt.setString(NbtDataUtil.ENTITY_TYPE_ID,
                    EntityMinecart.Type.values()[nbt.getInteger(NbtDataUtil.MINECART_TYPE)].getName());
            nbt.removeTag(NbtDataUtil.MINECART_TYPE);
        }
        Class<? extends Entity> entityClass = SpongeImplHooks.getEntityClass(new ResourceLocation(nbt.getString(NbtDataUtil.ENTITY_TYPE_ID)));
        if (entityClass == null) {
            return null;
        }
        EntityType type = EntityTypeRegistryModule.getInstance().getForClass(entityClass);
        if (type == null) {
            return null;
        }

        Transform<org.spongepowered.api.world.World> transform = new Transform<>(((org.spongepowered.api.world.World) world), new Vector3d(x, y, z));
        Object frame = Sponge.getCauseStackManager().pushCauseFrame();
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PLACEMENT);
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(), type, transform);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCauseFrame(frame);
        return event.isCancelled() ? null : AnvilChunkLoader.readWorldEntityPos(nbt, world, x, y, z, b);
    }

    @Inject(method = "execute", at = @At(value = "NEW", args = LIGHTNINGBOLT_CLASS), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onProcess(MinecraftServer server, ICommandSender sender, String args[], CallbackInfo ci, String s, BlockPos blockpos, Vec3d vec3d, double x, double y, double z, World world) {
        Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, new Vector3d(x, y, z));
        Object frame = Sponge.getCauseStackManager().pushCauseFrame();
        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.PLACEMENT);
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(),
                EntityTypes.LIGHTNING, transform);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCauseFrame(frame);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
