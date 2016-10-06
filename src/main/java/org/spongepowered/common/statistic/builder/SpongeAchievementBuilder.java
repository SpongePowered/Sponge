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

import net.minecraft.stats.StatList;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.statistic.achievement.Achievement.Builder;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.statistic.IMixinAchievement;

public class SpongeAchievementBuilder implements Builder {

    private String name;
    private Translation translation;
    private Translation description;
    private ItemStack icon;
    private Achievement parent;
    private Statistic sourceStatistic;
    private long targetValue = 1;

    @Override
    public Builder from(Achievement value) {
        reset();
        name(value.getName());
        translation(value.getTranslation());
        description(value.getDescription());
        value.getParent().ifPresent(this::parent);
        value.getSourceStatistic().ifPresent(this::sourceStatistic);
        value.getStatisticTargetValue().ifPresent(this::targetValue);
        return this;
    }

    @Override
    public Builder reset() {
        this.name = null;
        this.translation = null;
        this.description = null;
        this.parent = null;
        this.sourceStatistic = null;
        this.targetValue = 1;
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
    public Builder description(Translation description) {
        this.description = checkNotNull(description, "description");
        return this;
    }

    @Override
    public Builder icon(ItemStack item) {
        this.icon = checkNotNull(item, "item");
        return this;
    }

    @Override
    public Builder parent(Achievement parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public Builder sourceStatistic(Statistic sourceStatistic) {
        this.sourceStatistic = sourceStatistic;
        return this;
    }

    @Override
    public Builder targetValue(long value) {
        this.targetValue = value;
        return this;
    }

    @Override
    public Achievement buildAndRegister(String id) throws IllegalStateException {
        checkNotNull(id, "id");
        // Check input
        checkState(this.name != null, "name must be set");
        checkState(this.translation != null, "translation must be set");
        final String translationId = checkNotNull(this.translation.getId(), "translation's id");
        checkState(this.description != null, "description must be set");
        checkState(this.icon != null, "icon must be set");

        // Does it exist already?
        if (StatList.getOneShotStat(id) != null) {
            throw new IllegalStateException("An achievement/statistic with that id is already registered!");
        }
        // Need to check both since ids are handled differently in minecraft and
        // sponge
        if (SpongeImpl.getRegistry().getType(Achievement.class, id).isPresent()) {
            throw new IllegalStateException("An achievement with that id is already registered!");
        }

        // TODO: What about those?
        int row = 0;
        int column = 0;

        // Actual creation process
        final net.minecraft.stats.Achievement achieve =
                new net.minecraft.stats.Achievement(id, translationId, column, row, (net.minecraft.item.ItemStack) this.icon,
                        (net.minecraft.stats.Achievement) this.parent);
        final IMixinAchievement achievement = (IMixinAchievement) achieve;
        achievement.setName(this.name)
                .setTranslation(this.translation)
                .setDescription(this.description)
                .setSourceStatistic(this.sourceStatistic)
                .setTargetValue(this.targetValue);
        SpongeImpl.getRegistry().register(Achievement.class, achievement);
        return achievement;
    }

}
