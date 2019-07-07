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
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.TreeLayout;
import org.spongepowered.api.advancement.TreeLayoutElement;

import java.util.Collection;
import java.util.Optional;

public final class SpongeTreeLayout implements TreeLayout {

    private final SpongeAdvancementTree tree;

    public SpongeTreeLayout(final SpongeAdvancementTree tree) {
        this.tree = tree;
    }

    @Override
    public AdvancementTree getTree() {
        return this.tree;
    }

    @Override
    public Collection<TreeLayoutElement> getElements() {
        final ImmutableSet.Builder<TreeLayoutElement> elements = ImmutableSet.builder();
        collectElements(this.tree.getRootAdvancement(), elements);
        return elements.build();
    }

    private static void collectElements(final Advancement advancement, final ImmutableSet.Builder<TreeLayoutElement> elements) {
        advancement.getDisplayInfo().ifPresent(displayInfo -> elements.add((TreeLayoutElement) displayInfo));
        advancement.getChildren().forEach(child -> collectElements(child, elements));
    }
    @Override
    public Optional<TreeLayoutElement> getElement(final Advancement advancement) {
        final Optional<AdvancementTree> tree = advancement.getTree();
        if (!tree.isPresent() || !advancement.getDisplayInfo().isPresent() || tree.get() != this.tree) {
            return Optional.empty();
        }
        return Optional.of((TreeLayoutElement) advancement.getDisplayInfo().get());
    }
}
