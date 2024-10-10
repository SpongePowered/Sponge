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
package org.spongepowered.common.mixin.core.world.entity.ai.goal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.api.entity.living.animal.horse.HorseLike;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.DismountTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(RunAroundLikeCrazyGoal.class)
public abstract class RunAroundLikeCrazyGoalMixin extends GoalMixin {

    // @formatter:off
    @Shadow @Final @Mutable private AbstractHorse horse;
    // @formatter:on

    /**
     * @author rexbut - December 16th, 2016
     * @author i509VCB - February 18th, 2020 - 1.14.4
     *
     * @reason - adjusted to support {@link DismountTypes}
     */
    @Overwrite
    public void tick() {
        if (!this.horse.isTamed() && this.horse.getRandom().nextInt(this.shadow$adjustedTickDelay(50)) == 0) {
            Entity entity = this.horse.getPassengers().get(0);

            if (entity == null) {
                return;
            }

            if (entity instanceof Player p) {
                int i = this.horse.getTemper();
                int j = this.horse.getMaxTemper();

                if (j > 0 && this.horse.getRandom().nextInt(j) < i) {
                    // Sponge start - Fire Tame Entity event
                    try (CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(entity);
                        if (SpongeCommon.post(SpongeEventFactory.createTameEntityEvent(frame.currentCause(), (HorseLike) this.horse))) {
                            return;
                        }
                    }
                    // Sponge end
                    this.horse.tameWithName(p);
                    return;
                }

                this.horse.modifyTemper(5);
            }

            // Sponge start - Throw an event before calling entity states
            // this.horseHost.ejectPassengers(); // Vanilla
            if (((EntityBridge) this.horse).bridge$removePassengers(DismountTypes.DERAIL.get())) {
                // Sponge end
                this.horse.makeMad();
                this.horse.level().broadcastEntityEvent(this.horse, (byte)6);
            }
        }
    }
}
