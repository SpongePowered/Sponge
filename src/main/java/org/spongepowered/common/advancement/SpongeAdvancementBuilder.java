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

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;

import java.util.Map;

import javax.annotation.Nullable;

public class SpongeAdvancementBuilder implements Advancement.Builder {

    private static final Criterion dummyCriterion = new Criterion(new ImpossibleTrigger.Instance());
    private static final String dummyCriterionName = "dummy_root_criterion";
    private static final Map<String, Criterion> dummyCriteria = ImmutableMap.of(dummyCriterionName, dummyCriterion);
    private static final String[][] dummyRequirements = {{ dummyCriterionName }};

    public static final net.minecraft.advancements.Advancement DUMMY_ROOT_ADVANCEMENT = new net.minecraft.advancements.Advancement(
            new ResourceLocation("sponge", "dummy_root"), null, null,
            AdvancementRewards.EMPTY, dummyCriteria, dummyRequirements);

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
    public Advancement build(String id) {
        checkNotNull(id, "id");
        final PluginContainer plugin = Sponge.getCauseStackManager().getCurrentCause().first(PluginContainer.class).get();
        final Tuple<Map<String, Criterion>, String[][]> result = SpongeCriterionHelper.toVanillaCriteriaData(this.criterion);
        final AdvancementRewards rewards = AdvancementRewards.EMPTY;
        final ResourceLocation resourceLocation = new ResourceLocation(plugin.getId(), id);
        final net.minecraft.advancements.DisplayInfo displayInfo = this.displayInfo == null ? null :
                (net.minecraft.advancements.DisplayInfo) DisplayInfo.builder().from(this.displayInfo).build(); // Create a copy
        net.minecraft.advancements.Advancement parent = (net.minecraft.advancements.Advancement) this.parent;
        if (parent == null) {
            parent = DUMMY_ROOT_ADVANCEMENT; // Attach a dummy root until a tree is constructed
        }
        final Advancement advancement = (Advancement) new net.minecraft.advancements.Advancement(
                resourceLocation, parent, displayInfo, rewards, result.getFirst(), result.getSecond());
        ((IMixinAdvancement) advancement).setCriterion(this.criterion);
        return advancement;
    }

    @Override
    public Advancement.Builder from(Advancement value) {
        this.displayInfo = value.getDisplayInfo().orElse(null);
        this.criterion = value.getCriterion();
        this.parent = value.getParent().orElse(null);
        return this;
    }

    @Override
    public Advancement.Builder reset() {
        this.criterion = AdvancementCriterion.EMPTY;
        this.displayInfo = null;
        this.parent = null;
        return this;
    }
}
