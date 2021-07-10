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

import net.minecraft.world.entity.AgableMob;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.BreedingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(AgableMob.class)
public abstract class AgableMobMixin extends MobMixin {

    @Inject(method = "setAge", at = @At("RETURN"))
    private void impl$callReadyToMateOnAgeUp(final int age, final CallbackInfo ci) {
        if (age == 0 && ShouldFire.BREEDING_EVENT_READY_TO_MATE) {
            if (((AgableMob) (Object) this) instanceof net.minecraft.world.entity.animal.Animal) {
                final BreedingEvent.ReadyToMate event = SpongeEventFactory.createBreedingEventReadyToMate(PhaseTracker.getCauseStackManager().currentCause(), (Animal) this);
                SpongeCommon.post(event);
            }
        }
    }

}
