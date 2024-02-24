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
package org.spongepowered.common.util;


import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.Criterion;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.advancement.criterion.DefaultedAdvancementCriterion;
import org.spongepowered.common.advancement.criterion.SpongeAndCriterion;
import org.spongepowered.common.advancement.criterion.SpongeEmptyCriterion;
import org.spongepowered.common.advancement.criterion.SpongeOperatorCriterion;
import org.spongepowered.common.advancement.criterion.SpongeOrCriterion;
import org.spongepowered.common.advancement.criterion.SpongeScoreCriterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class SpongeCriterionUtil {

    public static AdvancementCriterion build(final Class<? extends OperatorCriterion> type,
            final Function<Set<AdvancementCriterion>, AdvancementCriterion> function,
            final AdvancementCriterion criterion, final Iterable<AdvancementCriterion> criteria) {
        Objects.requireNonNull(criteria, "criteria");
        final List<AdvancementCriterion> builder = new ArrayList<>();
        SpongeCriterionUtil.build(type, criterion, builder);
        for (final AdvancementCriterion criterion1 : criteria) {
            SpongeCriterionUtil.build(type, criterion1, builder);
        }
        return builder.isEmpty() ? new SpongeEmptyCriterion() : builder.size() == 1 ? builder.get(0) : function.apply(ImmutableSet.copyOf(builder));
    }

    private static void build(final Class<? extends OperatorCriterion> type, final AdvancementCriterion criterion, final List<AdvancementCriterion> criteria) {
        if (criterion instanceof SpongeEmptyCriterion) {
            return;
        }
        Objects.requireNonNull(criterion, "criterion");
        if (type.isInstance(criterion)) {
            criteria.addAll(((OperatorCriterion) criterion).criteria());
        } else {
            criteria.add(criterion);
        }
    }

    public static Tuple<Map<String, Criterion<?>>, List<List<String>>> toVanillaCriteriaData(final AdvancementCriterion criterion) {
        final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
        if (criterion instanceof SpongeEmptyCriterion) {
            return new Tuple<>(criteria, Collections.emptyList());
        }

        return new Tuple<>(criteria, SpongeCriterionUtil.collectCriteria(criterion, criteria));
    }

    private static List<List<String>> collectCriteria(final AdvancementCriterion criterion, final Map<String, Criterion<?>> criteria) {

        List<List<String>> requirements = new ArrayList<>();
        if (criterion instanceof SpongeAndCriterion) {
            ((SpongeOperatorCriterion) criterion).criteria().forEach(c -> requirements.addAll(SpongeCriterionUtil.collectCriteria(c, criteria)));
        } else if (criterion instanceof SpongeOrCriterion) {
            // OR List of AND Criteria of OR Criteria
            final List<List<List<String>>> andRequirementsList = ((SpongeOperatorCriterion) criterion).criteria().stream().map(c -> SpongeCriterionUtil
                    .collectCriteria(c, criteria)).toList();
            List<List<String>> finalList = new ArrayList<>();
            // For every AND Criteria
            for (List<List<String>> andRequirements : andRequirementsList) {
                if (finalList.isEmpty()) {
                    finalList.addAll(andRequirements); // Just take the first one in as is
                } else {
                    List<List<String>> workingList = new ArrayList<>();
                    for (List<String> andRequirement : andRequirements) {
                        for (List<String> prevAndRequirement : finalList) {
                            // For every AND requirement and FOR every previous AND requirement
                            // combine their OR requirements
                            // !! this can get very big very quickly !!
                            // TODO limit requirement count?
                            final List<String> newAndRequirement = new ArrayList<>(prevAndRequirement);
                            newAndRequirement.addAll(andRequirement);
                            workingList.add(newAndRequirement);
                        }
                    }

                    finalList = workingList;
                }
            }
            requirements.addAll(finalList);
        } else if (criterion instanceof final SpongeScoreCriterion scoreCriterion) {
            for (int i = 0; i < scoreCriterion.goal(); i++) {
                final DefaultedAdvancementCriterion internalCriterion = scoreCriterion.internalCriteria.get(i);
                criteria.put(internalCriterion.name(), ((Criterion) (Object) internalCriterion));
                requirements.add(Collections.singletonList(internalCriterion.name()));
            }
        } else {
            criteria.put(criterion.name(), (Criterion) (Object) criterion);
            requirements.add(Collections.singletonList(criterion.name()));
        }

        return requirements;
    }

    private SpongeCriterionUtil() {
    }
}
