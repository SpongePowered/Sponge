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

import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.TristateResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;

import javax.annotation.Nullable;
import net.minecraft.world.entity.ai.goal.BreedGoal;

@Mixin(BreedGoal.class)
public abstract class BreedGoalMixin {

    // @formatter:off
    @Shadow @Final protected net.minecraft.world.entity.animal.Animal animal;
    @Shadow protected net.minecraft.world.entity.animal.Animal partner;

    @Shadow @Nullable private net.minecraft.world.entity.animal.Animal shadow$getFreePartner() {
        // Shadow implements
        return null;
    }
    // @formatter:on

    @Nullable
    @Redirect(method = "canUse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/goal/BreedGoal;getFreePartner()Lnet/minecraft/world/entity/animal/Animal;"))
    private net.minecraft.world.entity.animal.Animal impl$callFindMateEvent(final BreedGoal entityAIMate) {
        net.minecraft.world.entity.animal.Animal nearbyMate = this.shadow$getFreePartner();
        if (nearbyMate == null) {
            return null;
        }

        if (ShouldFire.BREEDING_EVENT_FIND_MATE) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this.animal);
                final org.spongepowered.api.event.entity.BreedingEvent.FindMate event =
                    SpongeEventFactory.createBreedingEventFindMate(frame.currentCause(), TristateResult.Result.DEFAULT,
                        TristateResult.Result.DEFAULT, (Animal) nearbyMate, true);
                if (SpongeCommon.post(event) || event.result() == TristateResult.Result.DENY) {
                    nearbyMate = null;
                }
            }
        }

        return nearbyMate;
    }

}
