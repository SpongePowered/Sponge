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
package org.spongepowered.common.config;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationVisitor;
import ninja.leaping.configurate.Types;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Given a configuration node, and a node at the same position in the tree of a parent configuration, remove values from the child node that don't
 * change anything from the parent node.
 *
 * This operates in configurations with a parent-child hierarchy like Sponge's, where there is one global configuration, plus world- and
 * dimension-specific configurations, each which only need to contain values that are different to the ones in their parent. By visiting each
 * configuration before it's saved, any unnecessary values can be removed.
 */
class DuplicateRemovalVisitor implements ConfigurationVisitor.Safe<AtomicReference<ConfigurationNode>, Void> {
    private static final DuplicateRemovalVisitor INSTANCE = new DuplicateRemovalVisitor();

    private DuplicateRemovalVisitor() {
    }

    public static void visit(ConfigurationNode child, ConfigurationNode parent) {
        child.visit(INSTANCE, new AtomicReference<>(parent));
    }

    @Override
    public AtomicReference<ConfigurationNode> newState() {
        throw new IllegalArgumentException("A parent configuration must be provided as the state object to properly remove duplicates");
    }

    private boolean isListElement(ConfigurationNode node) {
        return node.getParent() != null && node.getParent().isList();
    }

    @Override
    public void beginVisit(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
        Preconditions.checkNotNull(Preconditions.checkNotNull(parent, "parentRef").get(), "A parent configuration must be provided!");
    }

    @Override
    public void enterNode(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
        if (node.getParent() != null) { // exclude root nodes
            parent.set(parent.get().getNode(node.getKey()));
        }
    }

    @Override
    public void enterMappingNode(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
    }

    @Override
    public void enterListNode(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
        if (!isListElement(node) && Objects.equals(node.getValue(), parent.get().getValue())) {
            node.setValue(null);
        }
    }

    @Override
    @SuppressWarnings("UnnecessaryUnboxing")
    public void enterScalarNode(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
        ConfigurationNode parentNode = popParent(parent);
        // ignore list values
        if (isListElement(node)) {
            return;
        }

        // if the node already exists in the parent config, remove it
        if (Objects.equals(node.getValue(), parentNode.getValue())) {
            node.setValue(null);
            return;
        }

        // Fix double bug
        final Double nodeVal = node.getValue(Types::asDouble);
        if (nodeVal != null) {
            Double parentVal = parentNode.getValue(Types::asDouble);
            if (parentVal == null && nodeVal.doubleValue() == 0 || (parentVal != null && nodeVal.doubleValue() == parentVal.doubleValue())) {
                node.setValue(null);
            }
        }
    }

    @Override
    public void exitMappingNode(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
        popParent(parent);

        // remove empty maps
        if (node.isEmpty() && !isListElement(node)) {
            node.setValue(null);
        }
    }

    @Override
    public void exitListNode(ConfigurationNode node, AtomicReference<ConfigurationNode> parent) {
        ConfigurationNode parentNode = popParent(parent);
        if (parentNode.isEmpty() && node.isEmpty()) {
            node.setValue(null);
        }
    }

    @Override
    public Void endVisit(AtomicReference<ConfigurationNode> parent) {
        return null;
    }

    private ConfigurationNode popParent(AtomicReference<ConfigurationNode> parentRef) {
        ConfigurationNode parent = parentRef.get();
        if (parent.getParent() != null) {
            parentRef.set(parent.getParent());
        }
        return parent;
    }
}
