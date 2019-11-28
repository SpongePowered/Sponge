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
package org.spongepowered.common.statistic;

import net.minecraft.item.Item;
import net.minecraft.stats.StatCrafting;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

import javax.annotation.Nullable;

public final class SpongeBlockStatistic extends StatCrafting implements BlockStatistic, SpongeStatistic {

    @Nullable private StatisticType statisticType;

    public SpongeBlockStatistic(String statId, String itemName, ITextComponent statName, Item item) {
        super(statId, itemName, statName, item);
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(this.statId);
    }

    @Override
    public ItemType getItemType() {
        return ItemTypeRegistryModule.getInstance().getById(
            this.statId.substring(this.statId.lastIndexOf('.') + 1)).get();
    }

    @Override
    public Optional<Criterion> getCriterion() {
        return Optional.ofNullable((Criterion) getCriteria());
    }

    @Override
    public String getName() {
        return getStatName().getUnformattedText();
    }

    @Override
    public StatisticType getType() {
        if (this.statisticType == null) {
            this.statisticType = Sponge.getRegistry().getType(StatisticType.class, getId().substring(0, getId().indexOf("."))).get();
        }
        return this.statisticType;
    }
}
