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
package org.spongepowered.common.mixin.core.entity.monster;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.monster.EndermanEntity;

@Mixin(EndermanEntity.class)
public abstract class EntityEndermanMixin extends EntityMobMixin {

    @Shadow @Nullable public abstract BlockState getHeldBlockState();

    @Shadow public abstract void setHeldBlockState(@Nullable BlockState state);

    @Redirect(method = "teleportTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntityEnderman;attemptTeleport(DDD)Z"))
    private boolean impl$CheckContextWithTeleport(EndermanEntity entityEnderman, double x, double y, double z) {
        if (entityEnderman.world.isRemote) {
            return entityEnderman.attemptTeleport(x, y, z);
        }
        
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.ENTITY_TELEPORT);
            return this.attemptTeleport(x, y, z);
        }
    }

    /**
     * @author gabizou - July 26th, 2018
     * @reason Due to vanilla logic, a block is removed *after* the held item is set,
     * so, when the block event gets cancelled, we don't have a chance to cancel the
     * enderman pickup.
     *
     * @param phaseContext The context, for whatever reason in the future
     */
    @Override
    public void bridge$onCancelledBlockChange(EntityTickContext phaseContext) {
        if (this.getHeldBlockState() != null) {
            this.setHeldBlockState(null);
        }
    }
}
