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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.common.hooks.EventHooks;

public final class ForgeEventHooks implements EventHooks {

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

}
