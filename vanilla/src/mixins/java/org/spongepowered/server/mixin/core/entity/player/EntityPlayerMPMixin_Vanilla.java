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
package org.spongepowered.server.mixin.core.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin_Vanilla extends EntityPlayerMixin_Vanilla {

    @Shadow public boolean invulnerableDimensionChange;
    @Shadow private Vec3d enteredNetherPosition;
    @Shadow public boolean queuedEndExit;
    @Shadow public NetHandlerPlayServer connection;
    @Shadow public boolean seenCredits;
    @Shadow @Final public MinecraftServer server;
    @Shadow public int lastExperience;
    @Shadow private float lastHealth;
    @Shadow private int lastFoodLevel;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void vanilla$initializeFieldsOnCopy(EntityPlayerMP oldPlayer, boolean respawnFromEnd, CallbackInfo ci) {
        this.vanilla$spawnChunkMap = ((EntityPlayerMPMixin_Vanilla) (Object) oldPlayer).vanilla$spawnChunkMap;
        this.vanilla$spawnForcedSet = ((EntityPlayerMPMixin_Vanilla) (Object) oldPlayer).vanilla$spawnForcedSet;

        if (((DataCompoundHolder) oldPlayer).data$hasRootCompound()) {
            final NBTTagCompound old = ((DataCompoundHolder) oldPlayer).data$getRootCompound();
            if (old.hasKey(Constants.Forge.PERSISTED_NBT_TAG)) {
                ((DataCompoundHolder) this).data$getRootCompound().setTag(Constants.Forge.PERSISTED_NBT_TAG, old.getCompoundTag(Constants.Forge.PERSISTED_NBT_TAG));
            }

        }
    }

    /**
     * @author Zidane
     * @reason Re-route dimension changes to common hook
     */
    @Overwrite
    @Nullable
    public Entity changeDimension(int toDimensionId) {
        if (this.world.isRemote || this.isDead) {
            return null;
        }

        final EntityPlayerMP player = (EntityPlayerMP) (Object) this;

        this.invulnerableDimensionChange = true;

        if (this.dimension == 0 && toDimensionId == -1) {
            this.enteredNetherPosition = new Vec3d(this.posX, this.posY, this.posZ);
        } else if (this.dimension != -1 && toDimensionId != 0) {
            this.enteredNetherPosition = null;
        }

        if (this.dimension == 1 && toDimensionId == 1) {
            this.world.removeEntity(player);

            if (!this.queuedEndExit) {
                this.queuedEndExit = true;
                this.connection.sendPacket(new SPacketChangeGameState(4, this.seenCredits ? 0.0F : 1.0F));
                this.seenCredits = true;
            }

            return player;
        }
        else {

            final WorldServer world = this.server.getWorld(toDimensionId);
            EntityUtil.transferPlayerToWorld(player, null, world, (TeleporterBridge) world.getDefaultTeleporter());

            this.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
            this.lastExperience = -1;
            this.lastHealth = -1.0F;
            this.lastFoodLevel = -1;
            return player;
        }
    }
}
