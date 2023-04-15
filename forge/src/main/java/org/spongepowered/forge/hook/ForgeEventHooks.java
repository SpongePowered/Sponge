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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.EventHooks;
import org.spongepowered.forge.launch.bridge.event.ForgeEventBridge_Forge;
import org.spongepowered.forge.launch.event.ForgeEventManager;

public final class ForgeEventHooks implements EventHooks {

    @Override
    public Tuple<InteractBlockEvent.Secondary, InteractionResult> callInteractBlockEventSecondary(
            final ServerPlayer player, final ServerLevel level, final ItemStack heldItem, final InteractionHand hand, final BlockHitResult blockHitResult) {
        final PlayerInteractEvent.RightClickBlock forgeEvent = new PlayerInteractEvent.RightClickBlock(player, hand, blockHitResult.getBlockPos(),
                blockHitResult);
        // Returning FAIL when cancelled is the behavior on SpongeVanilla, we match by default on SpongeForge but this can be changed by a mod
        forgeEvent.setCancellationResult(InteractionResult.FAIL);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final InteractBlockEvent.Secondary spongeEvent =
                    (InteractBlockEvent.Secondary) ((ForgeEventBridge_Forge) forgeEvent).bridge$createSpongeEvent(frame);

            ((ForgeEventManager) MinecraftForge.EVENT_BUS).postDual(spongeEvent, forgeEvent);

            return Tuple.of(spongeEvent, forgeEvent.getCancellationResult());
        }
    }

    @Override
    public ChangeEntityWorldEvent.Pre callChangeEntityWorldEventPre(final Entity entity, final ServerLevel toWorld) {
        final ChangeEntityWorldEvent.Pre pre = EventHooks.super.callChangeEntityWorldEventPre(entity, toWorld);
        if (pre.isCancelled()) {
            // Taken from ForgeHooks#onTravelToDimension
            // Revert variable back to true as it would have been set to false
            if (entity instanceof AbstractMinecartContainer)
            {
                ((AbstractMinecartContainer) entity).dropContentsWhenDead(true);
            }
        }
        return pre;
    }

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
        final CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, targetEntity, isCriticalAttack, v);
        if (hitResult != null) {
            return new CriticalHitResult(true, hitResult.getDamageModifier() - 1.0F);
        }
        return new CriticalHitResult(false, v);
    }
}
