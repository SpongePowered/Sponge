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

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeOperatorCriterion implements OperatorCriterion, DefaultedAdvancementCriterion {

    private final String name;
    private final Collection<AdvancementCriterion> criteria;

    @Nullable private Collection<AdvancementCriterion> recursiveChildrenCriteria;
    @Nullable private Collection<AdvancementCriterion> leafChildrenCriteria;

    SpongeOperatorCriterion(final String namePrefix, final Collection<AdvancementCriterion> criteria) {
        this.name = namePrefix + Arrays.toString(criteria.stream().map(AdvancementCriterion::getName).toArray(String[]::new));
        this.criteria = ImmutableSet.copyOf(criteria);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<FilteredTrigger<?>> getTrigger() {
        return Optional.empty();
    }

    private Collection<AdvancementCriterion> getAllChildrenCriteria0(final boolean onlyLeaves) {
        final ImmutableSet.Builder<AdvancementCriterion> criteria = ImmutableSet.builder();
        if (!onlyLeaves) {
            criteria.add(this);
        }
        for (final AdvancementCriterion criterion : this.criteria) {
            if (criterion instanceof OperatorCriterion) {
                criteria.addAll(((SpongeOperatorCriterion) criterion).getAllChildrenCriteria0(onlyLeaves));
            }
        }
        return criteria.build();
    }

    private Collection<AdvancementCriterion> getRecursiveChildren() {
        if (this.recursiveChildrenCriteria == null) {
            this.recursiveChildrenCriteria = getAllChildrenCriteria0(false);
        }
        return this.recursiveChildrenCriteria;
    }

    @Override
    public Collection<AdvancementCriterion> getCriteria() {
        return this.criteria;
    }

    @Override
    public Collection<AdvancementCriterion> getLeafCriteria() {
        if (this.leafChildrenCriteria == null) {
            this.leafChildrenCriteria = getAllChildrenCriteria0(true);
        }
        return this.leafChildrenCriteria;
    }

    @Override
    public Collection<AdvancementCriterion> find(final String name) {
        return getRecursiveChildren().stream()
                .filter(c -> c.getName().equals(name)).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Optional<AdvancementCriterion> findFirst(final String name) {
        return getRecursiveChildren().stream()
                .filter(c -> c.getName().equals(name)).findFirst();
    }
}
