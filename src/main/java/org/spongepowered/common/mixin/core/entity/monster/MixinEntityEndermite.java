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

import net.minecraft.entity.monster.EntityEndermite;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.ExpirableData;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.entity.living.monster.Endermite;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ExpireEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpirableData;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.List;

@Mixin(EntityEndermite.class)
public abstract class MixinEntityEndermite extends MixinEntityMob implements Endermite {

    @Shadow public int lifetime;

    @Override
    public ExpirableData getExpirableData() {
        return new SpongeExpirableData(this.lifetime, 2400);
    }

    @Override
    public BoundedValue.Mutable<Integer> expireTicks() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPIRATION_TICKS)
                .minimum(0)
                .maximum(2400)
                .defaultValue(0)
                .value(this.lifetime)
                .build();
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getExpirableData());
    }

    @Inject(method = "onLivingUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/monster/EntityEndermite;setDead()V"))
    private void fireExpireEventLifetime(CallbackInfo ci) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            ExpireEntityEvent event = SpongeEventFactory.createExpireEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this);
            SpongeImpl.postEvent(event);
        }
    }
}
