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
package org.spongepowered.common.mixin.core.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.CooldownTrackerServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.living.humanoid.player.CooldownEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CooldownTrackerServer.class)
public abstract class MixinCooldownTrackerServer extends MixinCooldownTracker {

    @Shadow @Final private EntityPlayerMP player;

    @Override
    protected boolean throwSetCooldownEvent(ItemType type, int ticks) {
        if (ticks == 0) {
            return true;
        }
        final CooldownEvent.Set event = SpongeEventFactory.createCooldownEventSet(Sponge.getCauseStackManager().getCurrentCause(),
                ticks, ticks, type, (Player) this.player, getCooldown(type));
        return !Sponge.getEventManager().post(event);
    }

    @Override
    protected void throwEndCooldownEvent(final ItemType type) {
        final CooldownEvent.End event = SpongeEventFactory.createCooldownEventEnd(Sponge.getCauseStackManager().getCurrentCause(),
                type, (Player) this.player);
        Sponge.getEventManager().post(event);
    }

}
