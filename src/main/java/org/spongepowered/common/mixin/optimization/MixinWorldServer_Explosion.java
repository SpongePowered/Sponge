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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.mixin.core.world.MixinWorld;

import java.util.Collections;
import java.util.List;

@Mixin(value = WorldServer.class, priority = 1111)
@SuppressWarnings("UnresolvedMixinReference") // MinecraftDev
public abstract class MixinWorldServer_Explosion extends MixinWorld {

    @Redirect(method = "triggerInternalExplosion", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/WorldServer;playerEntities:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<EntityPlayer> onGetPlayersForExplosionPacket(WorldServer self) {
        return Collections.emptyList();
    }


    @Redirect(method = "triggerInternalExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Explosion;doExplosionB(Z)V"))
    private void onCallExplosion(Explosion explosion, boolean spawnParticles) {
        explosion.doExplosionB(true); // Note that this is forced to be true.
        for (EntityPlayer playerEntity : this.playerEntities) {
            final Vec3d knockback = explosion.getPlayerKnockbackMap().get(playerEntity);
            if (knockback != null) {
                // In Vanilla, doExplosionB always updates the 'motion[xyz]' fields for every entity in range.
                // However, this field is completely ignored for players (since 'velocityChanged') is never set, and
                // a completely different value is sent through 'SPacketExplosion'.

                // To replicate this behavior, we manually send a velocity packet. It's critical that we don't simply
                // add to the 'motion[xyz]' fields, as that will end up using the value set by 'doExplosionB', which must be
                // ignored.
                ((EntityPlayerMP) playerEntity).connection.sendPacket(new SPacketEntityVelocity(playerEntity.getEntityId(), knockback.x, knockback.y, knockback.z));
            }
        }
    }
}
