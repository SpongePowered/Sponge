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
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.util.SpongeCriterionUtil;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.Map;

public final class SpongeAdvancementBuilder extends SpongeCatalogBuilder<Advancement, Advancement.Builder> implements Advancement.Builder.RootStep {

    @Nullable private Advancement parent;
    private AdvancementCriterion criterion;
    @Nullable private DisplayInfo displayInfo;
    @Nullable private ResourceLocation backgroundPath;

    public SpongeAdvancementBuilder() {
        this.reset();
    }

    @Override
    public Advancement.Builder parent(@Nullable Advancement parent) {
        this.parent = parent;
        this.backgroundPath = null;
        return this;
    }

    @Override
    public Advancement.Builder.RootStep root() {
        this.parent = null;
        return this;
    }

    @Override
    public Advancement.Builder background(String backgroundPath) {
        this.backgroundPath = new ResourceLocation(backgroundPath);
        return this;
    }

    @Override
    public Advancement.Builder criterion(AdvancementCriterion criterion) {
        checkNotNull(criterion, "criterion");
        this.criterion = criterion;
        return this;
    }

    @Override
    public Advancement.Builder displayInfo(@Nullable DisplayInfo displayInfo) {
        this.displayInfo = displayInfo;
        return this;
    }

    @Override
    protected Advancement build(ResourceKey key) {
        final Tuple<Map<String, Criterion>, String[][]> result = SpongeCriterionUtil.toVanillaCriteriaData(this.criterion);
        final AdvancementRewards rewards = AdvancementRewards.EMPTY;
        final ResourceLocation resourceLocation = (ResourceLocation) (Object) key;

        final net.minecraft.advancements.DisplayInfo displayInfo = this.displayInfo == null ? null : new net.minecraft.advancements.DisplayInfo(
                ItemStackUtil.fromSnapshotToNative(this.displayInfo.getIcon()),
                SpongeAdventure.asVanilla(this.displayInfo.getTitle()),
                SpongeAdventure.asVanilla(this.displayInfo.getDescription()),
                this.backgroundPath,
                (FrameType) (Object) this.displayInfo.getType(),
                this.displayInfo.doesShowToast(),
                this.displayInfo.doesAnnounceToChat(),
                this.displayInfo.isHidden());
        final net.minecraft.advancements.Advancement parent = (net.minecraft.advancements.Advancement) this.parent;
        final Advancement advancement = (Advancement) new net.minecraft.advancements.Advancement(
                resourceLocation, parent, displayInfo, rewards, result.getFirst(), result.getSecond());
        ((AdvancementBridge) advancement).bridge$setCriterion(this.criterion);
        return advancement;
    }

    @Override
    public Advancement.Builder reset() {
        this.criterion = AdvancementCriterion.empty();
        this.displayInfo = null;
        this.parent = null;
        this.key = null;
        this.backgroundPath = null;
        return this;
    }
}
