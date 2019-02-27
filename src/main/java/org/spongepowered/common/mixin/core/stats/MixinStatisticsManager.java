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
package org.spongepowered.common.mixin.core.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatisticsManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.statistic.ChangeStatisticEvent;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StatisticsManager.class)
public abstract class MixinStatisticsManager {

    @Shadow @Final public Object2IntMap<Stat<?>> statsData;
    @Shadow public abstract int getValue(Stat<?> p_77444_1_);

    /**
     * @author Zidane - Feburary 26th, 2019 - Version 1.13
     *
     * @reason Fire event when changing statistic
     */
    @Overwrite
    public void setValue(EntityPlayer player, Stat<?> stat, int amount) {
        final int current = this.getValue(stat);

        Sponge.getCauseStackManager().pushCause(player);
        final ChangeStatisticEvent event = SpongeEventFactory.createChangeStatisticEvent(Sponge.getCauseStackManager().getCurrentCause(), current,
            current + amount, (Statistic) stat);
        if (Sponge.getEventManager().post(event)) {
            Sponge.getCauseStackManager().popCause();
            return;
        }

        Sponge.getCauseStackManager().popCause();
        this.statsData.put(stat, (int) event.getValue());
    }
}
