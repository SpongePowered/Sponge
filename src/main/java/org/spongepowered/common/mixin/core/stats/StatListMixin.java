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

import net.minecraft.item.Item;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.statistic.SpongeBlockStatistic;
import org.spongepowered.common.statistic.SpongeEntityStatistic;

@Mixin(StatList.class)
public class StatListMixin {

    @Redirect(method = "initMiningStats", at = @At(value = "NEW", args = "class=net/minecraft/stats/StatCrafting"))
    private static StatCrafting createBlockStat(final String statId, final String itemName, final ITextComponent statName, final Item item) {
        return new SpongeBlockStatistic(statId, itemName, statName, item);
    }

    @Redirect(method = "getStatKillEntity", at = @At(value = "NEW", args = "class=net/minecraft/stats/StatBase"))
    private static Stat createKillEntityStat(final String statId, final ITextComponent statName) {
        final String entityId = statId.substring(statId.lastIndexOf(".") + 1);
        return new SpongeEntityStatistic(statId, statName, entityId);
    }

    @Redirect(method = "getStatEntityKilledBy", at = @At(value = "NEW", args = "class=net/minecraft/stats/StatBase"))
    private static Stat createKilledByEntityStat(final String statId, final ITextComponent statName) {
        final String entityId = statId.substring(statId.lastIndexOf(".") + 1);
        return new SpongeEntityStatistic(statId, statName, entityId);
    }

}
