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
package org.spongepowered.forge.hook;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.common.hooks.EventHooks;

public final class ForgeEventHooks implements EventHooks {

    @Override
    public void callItemDestroyedEvent(
        final Player player, final ItemStack stack, final InteractionHand hand
    ) {
        ForgeEventFactory.onPlayerDestroyItem(player, stack, InteractionHand.MAIN_HAND);
    }

    @Override
    public CriticalHitResult callCriticalHitEvent(
        final Player player, final Entity targetEntity, final boolean isCriticalAttack, final float v
    ) {
        final CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, targetEntity, isCriticalAttack, v + 1.0F);
        if (hitResult != null) {
            return new CriticalHitResult(true, hitResult.getDamageModifier() - 1.0F);
        }
        return new CriticalHitResult(false, v);
    }
}
