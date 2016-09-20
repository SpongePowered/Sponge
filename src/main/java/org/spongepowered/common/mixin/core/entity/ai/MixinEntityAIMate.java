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
package org.spongepowered.common.mixin.core.entity.ai;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.passive.EntityAnimal;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.BreedEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;

@Mixin(EntityAIMate.class)
public abstract class MixinEntityAIMate {

    private static final String
        METHOD_INVOKE_ASSIGN =
        "net/minecraft/entity/passive/EntityAnimal.createChild(Lnet/minecraft/entity/EntityAgeable;)Lnet/minecraft/entity/EntityAgeable;";

    @Shadow @Final private EntityAnimal theAnimal;
    @Shadow private EntityAnimal targetMate;

    @Inject(method = "spawnBaby()V", at = @At(value = "INVOKE_ASSIGN", target = METHOD_INVOKE_ASSIGN, shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callBreedEvent(CallbackInfo ci, EntityAgeable entityageable) {
        final BreedEntityEvent.Breed event = SpongeEventFactory.createBreedEntityEventBreed(Cause.of(NamedCause.source(this.theAnimal)),
                Optional.empty(), (Ageable)entityageable, (Ageable)this.targetMate);
        SpongeImpl.postEvent(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

}
