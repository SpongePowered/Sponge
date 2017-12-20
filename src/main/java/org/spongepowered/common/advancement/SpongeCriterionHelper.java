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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        final List<AdvancementCriterion> orCriteria = new ArrayList<>();
        for (String[] array : requirements) {
            final Set<AdvancementCriterion> andCriteria = new HashSet<>();
            for (String name : array) {
                final int index = name.indexOf(SpongeScoreCriterion.SUFFIX_BASE);
                if (index != -1) {
                    name = name.substring(0, index);
                }
                AdvancementCriterion criterion = (AdvancementCriterion) criteria.get(name);
                checkNotNull(criterion, name);
                andCriteria.add((AdvancementCriterion) criteria.get(name));
            }
            orCriteria.add(AndCriterion.of(andCriteria));
        }
        return OrCriterion.of(orCriteria);
    }

    // If the following can be simplified, be my guest
    // The goal is to reduce the AdvancementCriterion structure with
    // unlimited OR and AND operators into a simple one:
    //
    // String[][]
    // The first layer represents the OR operator and the second one
    // the AND operator

    static Tuple<Map<String, Criterion>, String[][]> toVanillaCriteriaData(AdvancementCriterion criterion) {
        final Map<String, Criterion> criteria = new HashMap<>();
        if (criterion == SpongeEmptyCriterion.INSTANCE) {
            return new Tuple<>(criteria, new String[0][0]);
        }
        final Wrapper wrapper = toWrapper(criterion, criteria);

        final List<Wrapper> list;
        if (wrapper instanceof And) {
            list = merge((And) wrapper);
        } else if (wrapper instanceof Or) {
            list = merge((Or) wrapper);
        } else {
            return new Tuple<>(criteria, new String[][] {{ ((One) wrapper).id }});
        }

        final List<List<String>> ids = new ArrayList<>();
        for (Wrapper wrapper1 : list) {
            if (wrapper1 instanceof Or) {
                throw new IllegalStateException("Unexpected Or.");
            } else if (wrapper1 instanceof And) {
                ids.add(((And) wrapper1).wrappers.stream().map(wrapper2 -> ((One) wrapper2).id).collect(Collectors.toList()));
            } else {
                ids.add(Collections.singletonList(((One) wrapper1).id));
            }
        }
        final String[][] idsArray = new String[ids.size()][];
        for (int i = 0; i < ids.size(); i++) {
            final List<String> entries = ids.get(i);
            idsArray[i] = entries.toArray(new String[entries.size()]);
        }
        return new Tuple<>(criteria, idsArray);
    }

    private static List<Wrapper> merge(Or or) {
        final List<Wrapper> stack = new ArrayList<>();
        for (Wrapper wrapper : or.wrappers) {
            if (wrapper instanceof Or) {
                throw new IllegalStateException("Or cannot be directly nested into Or.");
            } else if (wrapper instanceof And) {
                stack.addAll(merge((And) wrapper));
            } else {
                stack.add(wrapper);
            }
        }
        return stack;
    }

    private static List<Wrapper> merge(And and) {
        final List<Wrapper> stack = new ArrayList<>();
        for (Wrapper wrapper : and.wrappers) {
            if (wrapper instanceof Or) {
                final List<Wrapper> wrappers = merge((Or) wrapper);
                if (stack.isEmpty()) {
                    stack.addAll(wrappers);
                } else {
                    final List<Wrapper> temp = new ArrayList<>(stack);
                    stack.clear();
                    for (Wrapper tempWrapper : temp) {
                        //noinspection Convert2streamapi
                        for (Wrapper criterion1 : wrappers) {
                            stack.add(tempWrapper.and(criterion1));
                        }
                    }
                }
            } else if (wrapper instanceof And) {
                throw new IllegalStateException("And cannot be directly nested into And.");
            } else {
                if (stack.isEmpty()) {
                    stack.add(wrapper);
                } else {
                    final List<Wrapper> temp = new ArrayList<>(stack);
                    stack.clear();
                    //noinspection Convert2streamapi
                    for (Wrapper tempWrapper : temp) {
                        stack.add(tempWrapper.and(wrapper));
                    }
                }
            }
        }
        return stack;
    }

    private static Wrapper toWrapper(AdvancementCriterion criterion, Map<String, Criterion> criteria) {
        if (criterion instanceof SpongeAndCriterion) {
            return new And(((SpongeAndCriterion) criterion).getCriteria().stream()
                    .map(c -> toWrapper(c, criteria)).collect(Collectors.toList()));
        } else if (criterion instanceof SpongeOrCriterion) {
            return new Or(((SpongeOrCriterion) criterion).getCriteria().stream()
                    .map(c -> toWrapper(c, criteria)).collect(Collectors.toList()));
        } else if (criterion instanceof SpongeScoreCriterion) {
            final SpongeScoreCriterion scoreCriterion = (SpongeScoreCriterion) criterion;
            final List<Wrapper> wrappers = new ArrayList<>();
            final String name = criterion.getName();
            for (int i = 0; i < scoreCriterion.getGoal(); i++) {
                final String id = name + SpongeScoreCriterion.SUFFIX_BASE + i;
                criteria.put(id, (Criterion) scoreCriterion.internalCriteria.get(i));
                wrappers.add(new One(id));
            }
            return new And(wrappers);
        } else {
            criteria.put(criterion.getName(), (Criterion) criterion);
            return new One(criterion.getName());
        }
    }

    private static abstract class Wrapper {

        public static Wrapper EMPTY = new Wrapper() {};

        Wrapper and(Wrapper wrapper) {
            if (this == EMPTY && wrapper == EMPTY) {
                return EMPTY;
            } else if (wrapper == EMPTY) {
                return this;
            } else if (this == EMPTY) {
                return wrapper;
            }
            final List<Wrapper> wrappers = new ArrayList<>();
            if (this instanceof And) {
                wrappers.addAll(((And) this).wrappers);
            } else {
                wrappers.add(this);
            }
            if (wrapper instanceof And) {
                wrappers.addAll(((And) wrapper).wrappers);
            } else {
                wrappers.add(wrapper);
            }
            return new And(wrappers);
        }
    }

    private static class One extends Wrapper {

        final String id;

        private One(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return this.id;
        }
    }

    private static abstract class Multi extends Wrapper {

        final List<Wrapper> wrappers;

        private Multi(List<Wrapper> wrappers) {
            this.wrappers = wrappers;
        }

        @Override
        public String toString() {
            return Arrays.toString(this.wrappers.toArray());
        }
    }

    private static class And extends Multi {

        private And(List<Wrapper> wrappers) {
            super(wrappers);
        }
    }

    private static class Or extends Multi {

        private Or(List<Wrapper> wrappers) {
            super(wrappers);
        }
    }

    private SpongeCriterionHelper() {
    }
}
