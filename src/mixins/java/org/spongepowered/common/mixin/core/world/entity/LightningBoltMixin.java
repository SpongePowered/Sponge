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
package org.spongepowered.common.mixin.core.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.entity.ExpireEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends EntityMixin {

    //@formatter:off
    @Shadow private boolean visualOnly;
    //@formatter:on

    @Shadow private int life;

    @SuppressWarnings("unchecked")
    @Redirect(
        method = "tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
        )
    )
    private List<Entity> impl$ThrowEventAndProcess(final Level level, final Entity thisEntity, final AABB boundingBox, final Predicate<Entity> predicate) {
        if (this.removed || this.level.isClientSide) {
            return Collections.emptyList();
        }
        final List<Entity> originalEntities = level.getEntities(thisEntity, boundingBox, predicate);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final LightningEvent.Strike strike = SpongeEventFactory.createLightningEventStrike(frame.currentCause(), (List<org.spongepowered.api.entity.Entity>) (List<?>) originalEntities);
            Sponge.eventManager().post(strike);
            if (strike.isCancelled()) {
                return Collections.emptyList();
            }
        }
        return originalEntities;
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;remove()V"))
    private void impl$ThrowLightningPost(final CallbackInfo ci) {
        if (this.level.isClientSide || this.life >= 0) {
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final LightningEvent.Post strike = SpongeEventFactory.createLightningEventPost(frame.currentCause());
            Sponge.eventManager().post(strike);
            final ExpireEntityEvent event = SpongeEventFactory.createExpireEntityEvent(frame.currentCause(), (org.spongepowered.api.entity.Entity) (Object) this);
            Sponge.eventManager().post(event);
        }
    }

}
