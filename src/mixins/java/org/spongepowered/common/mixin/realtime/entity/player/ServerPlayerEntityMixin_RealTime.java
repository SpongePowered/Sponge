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
package org.spongepowered.common.mixin.realtime.entity.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_RealTime extends PlayerEntityMixin_RealTime {

    // TODO: Minecraft 1.16 - Logic now occurs in Entity
//    @Redirect(
//        method = "processPortalCooldown",
//        at = @At(
//            value = "FIELD",
//            target = "portalCooldown",
//            opcode = Opcodes.PUTFIELD
//        ),
//        slice = @Slice(
//            from = @At(
//                value = "FIELD",
//                target = "Lnet/minecraft/entity/player/ServerPlayerEntity;isChangingDimension:Z",
//                opcode = Opcodes.GETFIELD
//            ),
//            to = @At("RETURN")
//        )
//    )
//    private void realTimeImpl$adjustForRealTimePortalCooldown(final ServerPlayerEntity self, final int modifier) {
//        if (((PlatformEntityBridge) (ServerPlayerEntity) (Object) this).bridge$isFakePlayer() || ((WorldBridge) this.level).bridge$isFake()) {
//            ((EntityAccessor) this).accessor$portalTime(modifier);
//            return;
//        }
//        final int ticks = (int) ((RealTimeTrackingBridge) this.level).realTimeBridge$getRealTimeTicks();
//        // The initially apparent function of timeUntilPortal is a cooldown for
//        // nether portals. However, there is a much more important use:
//        // preventing players from being immediately sent back to the other end
//        // of the portal. Since it only checks timeUntilPortal to determine
//        // whether the player was just in a portal, if timeUntilPortal gets set
//        // to 0, it assumes the player left and reentered the portal (see
//        // Entity.setPortal()). To prevent this, "snag" the value of
//        // timeUntilPortal at 1. If setPortal() does not reset it (the player
//        // exits the portal), modifier will become 0, indicating that it is
//        // OK to teleport the player.
//        ((EntityAccessor) this).accessor$portalTime(Math.max(modifier > 0 ? 1 : 0, ((EntityAccessor) this).accessor$portalTime() - ticks));
//    }
}
