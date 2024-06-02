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
package org.spongepowered.common.mixin.core.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.living.player.CooldownEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Optional;

@Mixin(ServerItemCooldowns.class)
public abstract class ServerItemCooldownsMixin extends ItemCooldownsMixin {

    // @formmater:off
    @Shadow @Final private net.minecraft.server.level.ServerPlayer player;

    @Shadow protected abstract void shadow$onCooldownStarted(Item itemIn, int ticksIn);
    // @formmater:on

    @Override
    protected int impl$throwSetCooldownEvent(final ItemType type, final int ticks) {
        if (ticks == 0) {
            return 0;
        }
        final Optional<Ticks> beforeCooldown = ((CooldownTracker) this).cooldown(type);
        final CooldownEvent.Set event = SpongeEventFactory.createCooldownEventSet(PhaseTracker.getCauseStackManager().currentCause(),
                SpongeTicks.ticksOrInfinite(ticks), SpongeTicks.ticksOrInfinite(ticks), type, (ServerPlayer) this.player, beforeCooldown);

        if (Sponge.eventManager().post(event)) {
            this.shadow$onCooldownStarted((Item) type, beforeCooldown.map(x -> (int) x.ticks()).orElse(0));
            return Constants.Sponge.Entity.Player.ITEM_COOLDOWN_CANCELLED;
        } else {
            return SpongeTicks.toSaturatedIntOrInfinite(event.newCooldown());
        }
    }

    @Override
    protected void impl$throwEndCooldownEvent(final ItemType type) {
        final CooldownEvent.End event = SpongeEventFactory.createCooldownEventEnd(PhaseTracker.getCauseStackManager().currentCause(),
                type, (ServerPlayer) this.player);
        Sponge.eventManager().post(event);
    }

    @ModifyVariable(method = "onCooldownStarted", at = @At(value = "HEAD"), argsOnly = true)
    private int impl$modifyPacketOnInfiniteCooldown(int ticks) {
        if (ticks != Constants.TickConversions.INFINITE_TICKS) {
            return ticks;
        }
        return Integer.MAX_VALUE / 2; //StartTime + Ticks
    }

}
