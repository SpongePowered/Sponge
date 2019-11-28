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
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.Constants;

@Mixin(CommandSummon.class)
public abstract class CommandSummonMixin extends CommandBase {

    @Redirect(method = "execute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;readWorldEntityPos(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;DDDZ)Lnet/minecraft/entity/Entity;"))
    private Entity impl$throwConstructEvent(final NBTTagCompound nbt, final World world, final double x, final double y, final double z, final boolean b,
        final MinecraftServer server, final ICommandSender sender, final String[] args) {
        if ("Minecart".equals(nbt.func_74779_i(Constants.Entity.ENTITY_TYPE_ID))) {
            nbt.func_74778_a(Constants.Entity.ENTITY_TYPE_ID,
                    EntityMinecart.Type.values()[nbt.func_74762_e(Constants.Entity.Minecart.MINECART_TYPE)].func_184954_b());
            nbt.func_82580_o(Constants.Entity.Minecart.MINECART_TYPE);
        }
        final Class<? extends Entity> entityClass = SpongeImplHooks.getEntityClass(new ResourceLocation(nbt.func_74779_i(Constants.Entity.ENTITY_TYPE_ID)));
        if (entityClass == null) {
            return null;
        }
        final EntityType type = EntityTypeRegistryModule.getInstance().getForClass(entityClass);
        if (type == null) {
            return null;
        }

        final Transform<org.spongepowered.api.world.World> transform = new Transform<>(((org.spongepowered.api.world.World) world), new Vector3d(x, y, z));
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), type, transform);
            SpongeImpl.postEvent(event);
            return event.isCancelled() ? null : AnvilChunkLoader.func_186054_a(nbt, world, x, y, z, b);
        }
    }

    @Inject(method = "execute", at = @At(value = "NEW", args = "class=net/minecraft/entity/effect/EntityLightningBolt"), cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$checkLightning(final MinecraftServer server, final ICommandSender sender, final String[] args, final CallbackInfo ci, final String s,
        final BlockPos blockpos, final Vec3d vec3d, final double x, final double y, final double z, final World world) {
        final Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) world, new Vector3d(x, y, z));
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(),
                    EntityTypes.LIGHTNING, transform);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            } else {
                final LightningEvent.Pre lightningPre = SpongeEventFactory.createLightningEventPre(frame.getCurrentCause());
                if (SpongeImpl.postEvent(lightningPre)) {
                    ci.cancel();
                }
            }
        }
    }
}
