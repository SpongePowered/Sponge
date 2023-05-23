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
package org.spongepowered.common.advancement;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTemplate;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.SpongeCriterionUtil;

import java.util.Map;

public final class SpongeAdvancementBuilder extends AbstractResourceKeyedBuilder<AdvancementTemplate, AdvancementTemplate.Builder> implements AdvancementTemplate.Builder.RootStep {

    private @Nullable Advancement parent;
    private AdvancementCriterion criterion;
    private @Nullable DisplayInfo displayInfo;
    private @Nullable ResourceLocation backgroundPath;
    private DataPack<AdvancementTemplate> pack = DataPacks.ADVANCEMENT;

    public SpongeAdvancementBuilder() {
        this.reset();
    }

    @Override
    public AdvancementTemplate.Builder parent(@Nullable Advancement parent) {
        this.parent = parent;
        this.backgroundPath = null;
        return this;
    }

    @Override
    public AdvancementTemplate.Builder.RootStep root() {
        this.parent = null;
        return this;
    }

    @Override
    public AdvancementTemplate.Builder background(ResourceKey backgroundPath) {
        this.backgroundPath = (ResourceLocation) (Object) backgroundPath;
        return this;
    }

    @Override
    public AdvancementTemplate.Builder criterion(AdvancementCriterion criterion) {
        checkNotNull(criterion, "criterion");
        this.criterion = criterion;
        return this;
    }

    @Override
    public AdvancementTemplate.Builder displayInfo(@Nullable DisplayInfo displayInfo) {
        this.displayInfo = displayInfo;
        return this;
    }

    @Override
    public AdvancementTemplate.Builder reset() {
        this.criterion = AdvancementCriterion.empty();
        this.displayInfo = null;
        this.parent = null;
        this.backgroundPath = null;
        this.pack = DataPacks.ADVANCEMENT;
        return this;
    }

    @Override
    public AdvancementTemplate build0() {
        final Tuple<Map<String, Criterion>, String[][]> result = SpongeCriterionUtil.toVanillaCriteriaData(this.criterion);
        final AdvancementRewards rewards = AdvancementRewards.EMPTY;
        final ResourceLocation resourceLocation = (ResourceLocation) (Object) key;

        final net.minecraft.advancements.DisplayInfo displayInfo = this.displayInfo == null ? null : new net.minecraft.advancements.DisplayInfo(
                ItemStackUtil.fromSnapshotToNative(this.displayInfo.icon()),
                SpongeAdventure.asVanilla(this.displayInfo.title()),
                SpongeAdventure.asVanilla(this.displayInfo.description()),
                this.backgroundPath,
                (FrameType) (Object) this.displayInfo.type(),
                this.displayInfo.doesShowToast(),
                this.displayInfo.doesAnnounceToChat(),
                this.displayInfo.isHidden());
        final net.minecraft.advancements.Advancement parent = (net.minecraft.advancements.Advancement) this.parent;
        final var advancement = new net.minecraft.advancements.Advancement(
                resourceLocation, parent, displayInfo, rewards, result.first(), result.second(), false);
        ((AdvancementBridge) advancement).bridge$setCriterion(this.criterion);
        return new SpongeAdvancementTemplate(this.key, advancement, this.pack);
    }
}
