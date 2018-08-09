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

import com.google.common.base.CaseFormat;
import net.minecraft.item.Item;
import net.minecraft.stats.StatCrafting;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

import javax.annotation.Nullable;

public final class SpongeBlockStatistic extends StatCrafting implements BlockStatistic, TypedSpongeStatistic {

    private String spongeId;
    private CatalogKey key;

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
    public CatalogKey getKey() {
        if (this.key ==  null) {
            this.key = CatalogKey.resolve(TypedSpongeStatistic.super.getId());
        }
        return this.key;
    }

    @Override
    public String getName() {
        return getStatName().getUnformattedText();
    }

    @Nullable
    @Override
    public String getSpongeId() {
        return this.spongeId;
    }

    @Override
    public void setSpongeId(String id) {
        this.spongeId = id;
        this.key = CatalogKey.resolve(id);
    }

    @Override
    public String getMinecraftId() {
        return this.statId;
    }

}
