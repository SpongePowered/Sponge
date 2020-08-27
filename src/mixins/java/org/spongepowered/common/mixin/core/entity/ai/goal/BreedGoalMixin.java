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
package org.spongepowered.common.mixin.core.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.TristateResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;

import javax.annotation.Nullable;

@Mixin(BreedGoal.class)
public abstract class BreedGoalMixin {

    @Shadow @Final protected AnimalEntity animal;
    @Shadow protected AnimalEntity targetMate;

    @Shadow @Nullable private AnimalEntity shadow$getNearbyMate() {
        // Shadow implements
        return null;
    }

    private boolean impl$spawnEntityResult;

    @Nullable
    @Redirect(method = "shouldExecute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/BreedGoal;getNearbyMate()Lnet/minecraft/entity/passive/AnimalEntity;"))
    private AnimalEntity impl$callFindMateEvent(final BreedGoal entityAIMate) {
        AnimalEntity nearbyMate = this.shadow$getNearbyMate();
        if (nearbyMate == null) {
            return null;
        }

        if (ShouldFire.BREEDING_EVENT_FIND_MATE) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this.animal);
                final org.spongepowered.api.event.entity.BreedingEvent.FindMate event =
                    SpongeEventFactory.createBreedingEventFindMate(frame.getCurrentCause(), TristateResult.Result.DEFAULT,
                        TristateResult.Result.DEFAULT, (Animal) nearbyMate, true);
                if (SpongeCommon.postEvent(event) || event.getResult() == TristateResult.Result.DENY) {
                    nearbyMate = null;
                }
            }
        }

        return nearbyMate;
    }

    @Inject(method = "spawnBaby",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.AFTER,
            ordinal = 0),
        cancellable = true)
    private void impl$cancelSpawnResultIfMarked(final CallbackInfo ci) {
        if (!this.impl$spawnEntityResult) {
            ci.cancel();
        }
    }

    @Redirect(method = "spawnBaby()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;)Z",
            ordinal = 0))
    private boolean impl$throwBreedEvent(final World world, final Entity baby) {
        if (ShouldFire.BREEDING_EVENT_BREED) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                // TODO API 8 is removing this TargetXXXX nonsense so that is why I put the parents into the Cause
                frame.pushCause(this.animal);
                frame.pushCause(this.targetMate);
                final org.spongepowered.api.event.entity.BreedingEvent.Breed event =
                    SpongeEventFactory.createBreedingEventBreed(PhaseTracker.getCauseStackManager().getCurrentCause(), (Ageable) baby);
                this.impl$spawnEntityResult = !SpongeCommon.postEvent(event) && world.addEntity(baby);
            }
        } else {
            this.impl$spawnEntityResult = world.addEntity(baby);
        }
        return this.impl$spawnEntityResult;
    }



}
