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
package org.spongepowered.common.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.AggressiveBridge;
import org.spongepowered.common.mixin.core.entity.EntityAgeableMixin;

import java.util.Random;

@Mixin(EntityWolf.class)
public abstract class EntityWolfMixin extends EntityAgeableMixin implements AggressiveBridge {

    @Shadow public abstract boolean shadow$isAngry();
    @Shadow public abstract void shadow$setAngry(boolean angry);

    @Override
    public boolean bridge$isAngry() {
        return this.shadow$isAngry();
    }

    @Override
    public void bridge$setAngry(boolean angry) {
        this.shadow$setAngry(angry);
    }

    @Redirect(method = "processInteract",
        at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0, remap = false))
    private int impl$ChangeRandomForTameEvent(Random rand, int bound, EntityPlayer player, EnumHand hand) {
        int random = rand.nextInt(bound);
        ItemStack stack = player.func_184586_b(hand);
        if (random == 0) {
            stack.func_190918_g(1);
            try {
                Sponge.getCauseStackManager().pushCause(((org.spongepowered.api.item.inventory.ItemStack) stack).createSnapshot());
                Sponge.getCauseStackManager().pushCause(player);
                if (!SpongeImpl.postEvent(SpongeEventFactory.createTameEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), (Wolf) this))) {
                    stack.func_190917_f(1);
                    return random;
                }
            } finally {
                Sponge.getCauseStackManager().popCauses(2);
            }
        }
        return 1;
    }

}
