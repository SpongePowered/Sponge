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

import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.Criterion;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class SpongeCriterionHelper {

    static AdvancementCriterion build(final Class<? extends OperatorCriterion> type, final Function<Set<AdvancementCriterion>, AdvancementCriterion> function,
            final AdvancementCriterion criterion, final Iterable<AdvancementCriterion> criteria) {
        checkNotNull(criteria, "criteria");
        final List<AdvancementCriterion> builder = new ArrayList<>();
        build(type, criterion, builder);
        for (final AdvancementCriterion criterion1 : criteria) {
            build(type, criterion1, builder);
        }
        return builder.isEmpty() ? SpongeEmptyCriterion.INSTANCE : builder.size() == 1 ? builder.get(0) : function.apply(ImmutableSet.copyOf(builder));
    }

    private static void build(final Class<? extends OperatorCriterion> type, final AdvancementCriterion criterion, final List<AdvancementCriterion> criteria) {
        if (criterion == SpongeEmptyCriterion.INSTANCE) {
            return;
        }
        checkNotNull(criterion, "criterion");
        if (type.isInstance(criterion)) {
            criteria.addAll(((OperatorCriterion) criterion).getCriteria());
        } else {
            criteria.add(criterion);
        }
    }

    static Tuple<Map<String, Criterion>, String[][]> toVanillaCriteriaData(final AdvancementCriterion criterion) {
        final Map<String, Criterion> criteria = new HashMap<>();
        if (criterion == SpongeEmptyCriterion.INSTANCE) {
            return new Tuple<>(criteria, new String[0][0]);
        }
        collectCriteria(criterion, criteria);
        final String[][] idsArray = new String[criteria.size()][];
        final Iterator<Criterion> it = criteria.values().iterator();
        for (int i = 0; i < criteria.size(); i++) {
            idsArray[i] = new String[] { ((DefaultedAdvancementCriterion) it.next()).getName() };
        }
        return new Tuple<>(criteria, idsArray);
    }

    private static void collectCriteria(final AdvancementCriterion criterion, final Map<String, Criterion> criteria) {
        if (criterion instanceof SpongeOperatorCriterion) {
            ((SpongeOperatorCriterion) criterion).getCriteria()
                    .forEach(c -> collectCriteria(c, criteria));
        } else if (criterion instanceof SpongeScoreCriterion) {
            final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
            final String name = criterion.getName();
            for (int i = 0; i < scoreCriterion.getGoal(); i++) {
                final String id = name + SpongeScoreCriterion.INTERNAL_SUFFIX_BASE + i;
                criteria.put(id, (Criterion) scoreCriterion.internalCriteria.get(i));
            }
        } else {
            criteria.put(criterion.getName(), (Criterion) criterion);
        }
    }

    private SpongeCriterionHelper() {
    }
}
