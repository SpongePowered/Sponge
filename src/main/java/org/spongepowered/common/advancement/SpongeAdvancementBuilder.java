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


import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeCriterionUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SpongeAdvancementBuilder implements Advancement.Builder.RootStep {

    private @Nullable ResourceLocation parent;
    private AdvancementCriterion criterion;
    private @Nullable DisplayInfo displayInfo;
    private @Nullable ResourceLocation backgroundPath;

    public SpongeAdvancementBuilder() {
        this.reset();
    }

    @Override
    public Advancement.Builder parent(final ResourceKey parent) {
        this.parent = (ResourceLocation) (Object) parent;
        this.backgroundPath = null;
        return this;
    }

    @Override
    public Advancement.Builder.RootStep root() {
        this.parent = null;
        return this;
    }

    @Override
    public Advancement.Builder background(final ResourceKey backgroundPath) {
        this.backgroundPath = (ResourceLocation) (Object) backgroundPath;
        return this;
    }

    @Override
    public Advancement.Builder criterion(final AdvancementCriterion criterion) {
        Objects.requireNonNull(criterion, "criterion");
        this.criterion = criterion;
        return this;
    }

    @Override
    public Advancement.Builder displayInfo(final @Nullable DisplayInfo displayInfo) {
        this.displayInfo = displayInfo;
        return this;
    }

    @Override
    public Advancement.Builder from(final Advancement value) {
        this.parent = (ResourceLocation) (Object) value.parent().orElse(null);
        this.criterion = value.criterion();
        this.displayInfo = value.displayInfo().orElse(null);
        this.backgroundPath = ((net.minecraft.advancements.Advancement) (Object) value).display()
            .map(net.minecraft.advancements.DisplayInfo::getBackground).map(Optional::get).orElse(null);
        return this;
    }

    @Override
    public Advancement.Builder reset() {
        this.criterion = AdvancementCriterion.empty();
        this.displayInfo = null;
        this.parent = null;
        this.backgroundPath = null;
        return this;
    }

    @Override
    public Advancement build() {
        final Tuple<Map<String, Criterion<?>>, List<List<String>>> result = SpongeCriterionUtil.toVanillaCriteriaData(this.criterion);
        final AdvancementRewards rewards = AdvancementRewards.EMPTY;

        var displayInfo = Optional.ofNullable(this.displayInfo).map(di -> new net.minecraft.advancements.DisplayInfo(
                ItemStackUtil.fromSnapshotToNative(di.icon()),
                SpongeAdventure.asVanilla(di.title()),
                SpongeAdventure.asVanilla(di.description()),
                Optional.ofNullable(this.backgroundPath),
                (net.minecraft.advancements.AdvancementType) (Object) di.type(),
                di.doesShowToast(),
                di.doesAnnounceToChat(),
                di.isHidden()));
        final var advancement = new net.minecraft.advancements.Advancement(Optional.ofNullable((this.parent)), displayInfo, rewards, result.first(), new AdvancementRequirements(result.second()), false);
        ((AdvancementBridge) (Object) advancement).bridge$setCriterion(this.criterion);
        return (Advancement) (Object) advancement;
    }
}
