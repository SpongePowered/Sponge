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
package org.spongepowered.server.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin_Vanilla implements DataCompoundHolder {

    @Shadow public World world;
    @Shadow public boolean isDead;
    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Nullable private NBTTagCompound vanilla$customEntityData;

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("RETURN"), remap = false)
    private void vanillaImpl$pushConstructPost(World world, CallbackInfo ci) {
        Sponge.getCauseStackManager().pushCause(world);
        SpongeImpl.postEvent(SpongeEventFactory.createConstructEntityEventPost(Sponge.getCauseStackManager().getCurrentCause(),
            ((org.spongepowered.api.entity.Entity) this), ((org.spongepowered.api.entity.Entity) this).getType(), ((org.spongepowered.api.entity.Entity) this).getTransform()));
        Sponge.getCauseStackManager().popCause();
    }

    @Override
    public final NBTTagCompound data$getRootCompound() {
        if (this.vanilla$customEntityData == null) {
            this.vanilla$customEntityData = new NBTTagCompound();
        }
        return this.vanilla$customEntityData;
    }

    @Override
    public boolean data$hasRootCompound() {
        return this.vanilla$customEntityData != null;
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    private void vanilla$preReadFromNBTInject(NBTTagCompound tagCompound, CallbackInfo ci) {
        if (tagCompound.hasKey(Constants.Forge.FORGE_DATA)) {
            this.vanilla$customEntityData = tagCompound.getCompoundTag(Constants.Forge.FORGE_DATA);
        }
    }

    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeEntityToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    private void vanilla$preWriteToNBTInject(NBTTagCompound tagCompound, CallbackInfoReturnable<NBTTagCompound> ci) {
        if (this.vanilla$customEntityData != null) {
            tagCompound.setTag(Constants.Forge.FORGE_DATA, this.vanilla$customEntityData);
        }
    }

    /**
     * @author Zidane
     * @reason Re-route dimension changes to common hook
     */
    @Nullable
    @Overwrite
    public net.minecraft.entity.Entity changeDimension(int toDimensionId) {
        if (!this.world.isRemote && !this.isDead) {

            final WorldServer world = this.getServer().getWorld(toDimensionId);
            return EntityUtil.transferEntityToWorld((Entity) (Object) this, null, world, (TeleporterBridge) world.getDefaultTeleporter(), true);
        }

        return null;
    }
}
