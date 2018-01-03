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

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.FeedAnimalEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.mixin.core.entity.MixinEntityAgeable;

import java.util.Optional;

@Mixin(EntityAnimal.class)
public abstract class MixinEntityAnimal extends MixinEntityAgeable implements Animal {

    @Inject(method = "processInteract", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/EntityAnimal;consumeItemFromStack(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/passive/EntityAnimal;inLove:I"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/passive/EntityAnimal;setInLove(Lnet/minecraft/entity/player/EntityPlayer;)V"
                    )
            ), cancellable = true
    )
    private void onBreedFeed(EntityPlayer player, EnumHand hand, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack) {
        Sponge.getCauseStackManager().pushCause(player);
        FeedAnimalEvent.Love event = SpongeEventFactory.createFeedAnimalEventLove(Sponge.getCauseStackManager().getCurrentCause(),
                Optional.of(this.getLocation()), ((org.spongepowered.api.item.inventory.ItemStack) itemStack).createSnapshot(), this);
        if (SpongeImpl.postEvent(event)) {
            cir.setReturnValue(true);
        }
        Sponge.getCauseStackManager().popCause();
    }

    @Inject(method = "processInteract", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/EntityAnimal;consumeItemFromStack(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/passive/EntityAnimal;isChild()Z"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/passive/EntityAnimal;ageUp(IZ)V"
                    )
            ), cancellable = true
    )
    private void onAgeFeed(EntityPlayer player, EnumHand hand, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack) {
        Sponge.getCauseStackManager().pushCause(player);
        FeedAnimalEvent.Aging event = SpongeEventFactory.createFeedAnimalEventAging(Sponge.getCauseStackManager().getCurrentCause(),
                Optional.of(this.getLocation()), ((org.spongepowered.api.item.inventory.ItemStack) itemStack).createSnapshot(), this);
        if (SpongeImpl.postEvent(event)) {
            cir.setReturnValue(true);
        }
        Sponge.getCauseStackManager().popCause();
    }

}
