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
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.registry.type.advancement.AdvancementRegistryModule;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.Map;

import javax.annotation.Nullable;

public class SpongeAdvancementBuilder extends SpongeCatalogBuilder<Advancement, Advancement.Builder> implements Advancement.Builder {

    @Nullable private Advancement parent;
    private AdvancementCriterion criterion;
    @Nullable private DisplayInfo displayInfo;

    public SpongeAdvancementBuilder() {
        reset();
    }

    @Override
    public Advancement.Builder parent(@Nullable Advancement parent) {
        this.parent = parent;
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
    protected Advancement build(PluginContainer plugin, String id, Translation name) {
        final Tuple<Map<String, Criterion>, String[][]> result = SpongeCriterionHelper.toVanillaCriteriaData(this.criterion);
        final AdvancementRewards rewards = AdvancementRewards.field_192114_a;
        final ResourceLocation resourceLocation = new ResourceLocation(plugin.getId(), this.id);
        final net.minecraft.advancements.DisplayInfo displayInfo = this.displayInfo == null ? null :
                (net.minecraft.advancements.DisplayInfo) DisplayInfo.builder().from(this.displayInfo).build(); // Create a copy
        net.minecraft.advancements.Advancement parent = (net.minecraft.advancements.Advancement) this.parent;
        if (parent == null) {
            parent = AdvancementRegistryModule.DUMMY_ROOT_ADVANCEMENT; // Attach a dummy root until a tree is constructed
        }
        final Advancement advancement = (Advancement) new net.minecraft.advancements.Advancement(
                resourceLocation, parent, displayInfo, rewards, result.getFirst(), result.getSecond());
        ((AdvancementBridge) advancement).bridge$setCriterion(this.criterion);
        final String plainName = name.get();
        if (StringUtils.isNotEmpty(plainName)) {
            ((AdvancementBridge) advancement).bridge$setName(plainName);
        }
        return advancement;
    }

    @Override
    public Advancement.Builder reset() {
        this.criterion = AdvancementCriterion.EMPTY;
        this.displayInfo = null;
        this.parent = null;
        this.id = null;
        this.name = null;
        return this;
    }
}
