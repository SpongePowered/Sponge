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
import org.spongepowered.api.advancement.criteria.AndCriterion;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class SpongeCriterionHelper {

    static AdvancementCriterion build(Class<? extends OperatorCriterion> type, Function<Set<AdvancementCriterion>, AdvancementCriterion> function,
            AdvancementCriterion criterion, Iterable<AdvancementCriterion> criteria) {
        checkNotNull(criteria, "criteria");
        final List<AdvancementCriterion> builder = new ArrayList<>();
        build(type, criterion, builder);
        for (AdvancementCriterion criterion1 : criteria) {
            build(type, criterion1, builder);
        }
        return builder.isEmpty() ? SpongeEmptyCriterion.INSTANCE : builder.size() == 1 ? builder.get(0) : function.apply(ImmutableSet.copyOf(builder));
    }

    private static void build(Class<? extends OperatorCriterion> type, AdvancementCriterion criterion, List<AdvancementCriterion> criteria) {
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

    public static AdvancementCriterion toCriterion(Map<String, Criterion> criteria, String[][] requirements) {
        final Map<String, AdvancementCriterion> criteriaMap = new HashMap<>();
        for (Map.Entry<String, Criterion> entry : criteria.entrySet()) {
            String name = entry.getKey();
            ((IMixinCriterion) entry.getValue()).setName(name);
            final int index = name.indexOf(SpongeScoreCriterion.SUFFIX_BASE);
            if (index != -1) {
                final int scoreIndex = Integer.parseInt(name.substring(index + SpongeScoreCriterion.SUFFIX_BASE.length()));
                name = name.substring(0, index);
                final SpongeScoreCriterion criterion = (SpongeScoreCriterion) criteriaMap.computeIfAbsent(name,
                        name1 -> new SpongeScoreCriterion(name1, new ArrayList<>()));
                while (criterion.internalCriteria.size() < scoreIndex) {
                    criterion.internalCriteria.add(null);
                }
                criterion.internalCriteria.set(scoreIndex, (AdvancementCriterion) entry.getValue());
                criteriaMap.put(name, criterion);
            } else {
                criteriaMap.put(name, (AdvancementCriterion) entry.getValue());
            }
        }
        // Fill up missing criteria in score criteria
        for (AdvancementCriterion criterion : criteriaMap.values()) {
            if (criterion instanceof SpongeScoreCriterion) {
                final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
                for (int i = 0; i < scoreCriterion.getGoal(); i++) {
                    if (scoreCriterion.internalCriteria.get(i) == null) {
                        final Criterion internalCriterion = new Criterion();
                        final String name = criterion.getName() + SpongeScoreCriterion.SUFFIX_BASE + i;
                        ((IMixinCriterion) internalCriterion).setName(name);
                        scoreCriterion.internalCriteria.set(i, (AdvancementCriterion) internalCriterion);
                        criteria.put(name, internalCriterion);
                    }
                }
            }
        }
        final List<AdvancementCriterion> andCriteria = new ArrayList<>();
        for (String[] array : requirements) {
            final Set<AdvancementCriterion> orCriteria = new HashSet<>();
            for (String name : array) {
                final int index = name.indexOf(SpongeScoreCriterion.SUFFIX_BASE);
                if (index != -1) {
                    name = name.substring(0, index);
                }
                final AdvancementCriterion criterion = (AdvancementCriterion) criteria.get(name);
                checkNotNull(criterion, name);
                orCriteria.add((AdvancementCriterion) criteria.get(name));
            }
            andCriteria.add(OrCriterion.of(orCriteria));
        }
        return AndCriterion.of(andCriteria);
    }

    static Tuple<Map<String, Criterion>, String[][]> toVanillaCriteriaData(AdvancementCriterion criterion) {
        final Map<String, Criterion> criteria = new HashMap<>();
        if (criterion == SpongeEmptyCriterion.INSTANCE) {
            return new Tuple<>(criteria, new String[0][0]);
        }
        collectCriteria(criterion, criteria);
        final String[][] idsArray = new String[criteria.size()][];
        final Iterator<Criterion> it = criteria.values().iterator();
        for (int i = 0; i < criteria.size(); i++) {
            idsArray[i] = new String[] { ((ICriterion) it.next()).getName() };
        }
        return new Tuple<>(criteria, idsArray);
    }

    private static void collectCriteria(AdvancementCriterion criterion, Map<String, Criterion> criteria) {
        if (criterion instanceof SpongeOperatorCriterion) {
            ((SpongeOperatorCriterion) criterion).getCriteria()
                    .forEach(c -> collectCriteria(c, criteria));
        } else if (criterion instanceof SpongeScoreCriterion) {
            final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
            final String name = criterion.getName();
            for (int i = 0; i < scoreCriterion.getGoal(); i++) {
                final String id = name + SpongeScoreCriterion.SUFFIX_BASE + i;
                criteria.put(id, (Criterion) scoreCriterion.internalCriteria.get(i));
            }
        } else {
            criteria.put(criterion.getName(), (Criterion) criterion);
        }
    }

    private SpongeCriterionHelper() {
    }
}
