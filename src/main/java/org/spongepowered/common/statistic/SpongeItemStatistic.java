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
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatCrafting;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import javax.annotation.Nullable;
import java.util.Optional;

public class SpongeItemStatistic extends StatCrafting implements ItemStatistic, TypedSpongeStatistic {

    private String spongeId;
    private CatalogKey key;
    @Nullable private Translation translation;

    public SpongeItemStatistic(String statId, String itemName, ITextComponent statName, Item item) {
        super(statId, itemName, statName, item);
    }

    @Override
    public Translation getTranslation() {
        if (this.translation == null) {
            String args = new ItemStack((Item) getItemType()).getDisplayName();
            int end = "stat.".length() + this.statId.substring("stat.".length()).indexOf('.');
            this.translation = new SpongeTranslation(this.statId.substring(0, end), args);
        }
        return this.translation;
    }

    @Override
    public ItemType getItemType() {
        return ItemTypeRegistryModule.getInstance().get(
                CatalogKey.resolve(this.statId.substring(this.statId.lastIndexOf('.') + 1))).get();
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