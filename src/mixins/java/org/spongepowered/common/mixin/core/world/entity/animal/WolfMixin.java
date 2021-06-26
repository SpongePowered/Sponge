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
package org.spongepowered.common.mixin.core.world.entity.animal;

import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.AggressiveEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.entity.AgableMobMixin;
import java.util.Random;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(net.minecraft.world.entity.animal.Wolf.class)
public abstract class WolfMixin extends AgableMobMixin implements AggressiveEntityBridge {

    // @formatter:off
    @Shadow public abstract void shadow$startPersistentAngerTimer();
    // @formatter:on

    @Override
    public boolean bridge$isAngry() {
        return ((NeutralMob) this).isAngry();
    }

    @Override
    public void bridge$setAngry(boolean angry) {
        this.shadow$startPersistentAngerTimer();
    }

    @Redirect(method = "mobInteract",
        at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0, remap = false))
    private int impl$ChangeRandomForTameEvent(Random rand, int bound, Player player, InteractionHand hand) {
        int random = rand.nextInt(bound);
        ItemStack stack = player.getItemInHand(hand);
        if (random == 0) {
            stack.shrink(1);
            try {
                PhaseTracker.getCauseStackManager().pushCause(ItemStackUtil.fromNative(stack).createSnapshot());
                PhaseTracker.getCauseStackManager().pushCause(player);
                if (!SpongeCommon.post(SpongeEventFactory.createTameEntityEvent(PhaseTracker.getCauseStackManager().currentCause(), (Wolf) this))) {
                    stack.grow(1);
                    return random;
                }
            } finally {
                PhaseTracker.getCauseStackManager().popCauses(2);
            }
        }
        return 1;
    }

}
