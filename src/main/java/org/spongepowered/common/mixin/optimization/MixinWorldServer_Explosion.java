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
package org.spongepowered.common.mixin.optimization;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.mixin.core.world.MixinWorld;

import java.util.Collections;
import java.util.List;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer_Explosion extends MixinWorld {

    @ModifyConstant(method = "newExplosion", constant = @Constant(intValue = 0, ordinal = 0))
    private int doExplosionWithParticles(int normallyFalse) {
        return 1;
    }

    @Redirect(method = "newExplosion", at = @At(value = "FIELD", target = "Lnet/minecraft/world/WorldServer;playerEntities:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<EntityPlayer> onGetPlayersForExplosionPacket(WorldServer self) {
        return Collections.emptyList();
    }


    @Inject(method = "newExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Explosion;doExplosionB(Z)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCallExpalosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking, CallbackInfoReturnable<Explosion> callbackInfo, Explosion explosion) {
        for (EntityPlayer playerEntity : this.playerEntities) {
            final Vec3d knockback = explosion.getPlayerKnockbackMap().get(playerEntity);
            if (knockback != null) {
                playerEntity.motionX += knockback.xCoord;
                playerEntity.motionY += knockback.yCoord;
                playerEntity.motionZ += knockback.zCoord;
            }
        }
    }
}
