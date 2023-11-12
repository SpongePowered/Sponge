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
package org.spongepowered.common.mixin.tracker.world.entity.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.tracker.world.entity.LivingEntityMixin_Tracker;

@Mixin(Player.class)
public abstract class PlayerMixin_Tracker extends LivingEntityMixin_Tracker {

    //@formatter:off
    @Shadow public abstract Component shadow$getDisplayName();
    @Shadow @Nullable public ItemEntity shadow$drop(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        return null; // SHADOWED
    }
    //@formatter:on

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    public void impl$callDestructEntityDeath(final DamageSource cause, final CallbackInfo ci) {
        if (this.shadow$isEffectiveAi()) {
            // Sponge start - Fire DestructEntityEvent.Death
            final DestructEntityEvent.Death event = SpongeCommonEventFactory.callDestructEntityEventDeath((Player) (Object) this, cause);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

}
