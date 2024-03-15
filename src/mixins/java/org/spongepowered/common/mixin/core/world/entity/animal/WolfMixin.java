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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.AggressiveEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.entity.AgableMobMixin;


@Mixin(net.minecraft.world.entity.animal.Wolf.class)
public abstract class WolfMixin extends AgableMobMixin implements AggressiveEntityBridge {

    // @formatter:off
    @Shadow public abstract void shadow$startPersistentAngerTimer();
    @Shadow protected abstract void shadow$tryToTame(final Player $$0);
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;tryToTame(Lnet/minecraft/world/entity/player/Player;)V"))
    private void impl$onTame(final net.minecraft.world.entity.animal.Wolf instance, final Player player, final Player $$0, final InteractionHand $$1) {
        try {
            final ItemStack handStack = player.getItemInHand($$1);
            handStack.grow(1);
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(handStack);
            handStack.shrink(1);
            PhaseTracker.getCauseStackManager().pushCause(snapshot);
            this.shadow$tryToTame(player);
        } finally {
            PhaseTracker.getCauseStackManager().popCause();
        }
    }
    
    @Inject(method = "tryToTame",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;tame(Lnet/minecraft/world/entity/player/Player;)V"),
        cancellable = true)
    private void impl$onTame(final Player player, final CallbackInfo ci) {
        try {
            
            PhaseTracker.getCauseStackManager().pushCause(player);
            if (!SpongeCommon.post(SpongeEventFactory.createTameEntityEvent(PhaseTracker.getCauseStackManager().currentCause(), (Wolf) this))) {
                this.shadow$level().broadcastEntityEvent((net.minecraft.world.entity.animal.Wolf) (Object) this, (byte)6);
                ci.cancel();
            }
        } finally {
            PhaseTracker.getCauseStackManager().popCause();
        }
    }

}
