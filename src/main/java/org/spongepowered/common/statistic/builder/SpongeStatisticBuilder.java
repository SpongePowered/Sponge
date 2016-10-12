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
package org.spongepowered.common.statistic.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBasic;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.Statistic.Builder;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.statistic.IMixinStatistic;

public class SpongeStatisticBuilder implements Builder {

    private String name;
    private Translation translation;
    private StatisticFormat format;
    private StatisticGroup group;

    @Override
    public Builder from(Statistic value) {
        reset();
        name(value.getName());
        translation(value.getTranslation());
        value.getStatisticFormat().ifPresent(this::format);
        group(value.getGroup());
        return this;
    }

    @Override
    public Builder reset() {
        this.name = null;
        this.translation = null;
        this.format = null;
        this.group = null;
        return this;
    }

    @Override
    public Builder name(String name) {
        this.name = checkNotNull(name, "name");
        return this;
    }

    @Override
    public Builder translation(Translation translation) {
        this.translation = checkNotNull(translation, "translation");
        return this;
    }

    @Override
    public Builder format(StatisticFormat format) {
        this.format = checkNotNull(format, "format");
        return this;
    }

    @Override
    public Builder group(StatisticGroup group) {
        this.group = checkNotNull(group, "group");
        return this;
    }

    @Override
    public Statistic buildAndRegister(String id) throws IllegalStateException {
        checkNotNull(id, "id");
        // Check input
        checkState(this.name != null, "name must be set");
        checkState(this.translation != null, "translation must be set");
        final String translationId = checkNotNull(this.translation.getId(), "translation's id");
        checkState(this.group != null, "group must be set");
        StatisticFormat format = this.format;
        if (format == null) {
            format = checkNotNull(this.group.getDefaultStatisticFormat(), "group's default format");
        }

        // Does it exist already?
        if (StatList.getOneShotStat(id) != null) {
            throw new IllegalStateException("A statistic with that id is already registered!");
        }
        // Need to check both since ids are handled differently in minecraft and
        // sponge
        if (SpongeImpl.getRegistry().getType(Statistic.class, id).isPresent()) {
            throw new IllegalStateException("A statistic with that id is already registered!");
        }

        // Actual creation process
        final StatBasic stat = new StatBasic(id, new TextComponentTranslation(translationId), (IStatType) format);
        final IMixinStatistic statistic = (IMixinStatistic) stat;
        statistic.setName(this.name)
                .setTranslation(this.translation)
                .setStatisticGroup(this.group)
                .registerStat();
        SpongeImpl.getRegistry().register(Statistic.class, statistic);
        return statistic;
    }

}
