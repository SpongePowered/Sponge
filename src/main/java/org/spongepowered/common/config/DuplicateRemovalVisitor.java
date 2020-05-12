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

/**
 * Given a configuration node, and a node at the same position in the tree of a parent configuration, remove values from the child node that don't
 * change anything from the parent node.
 */
class DuplicateRemovalVisitor implements ConfigurationVisitor.Safe<ConfigurationNode, Void> {

    static DuplicateRemovalVisitor INSTANCE = new DuplicateRemovalVisitor();

    private DuplicateRemovalVisitor() {
    }

    @Override
    public ConfigurationNode newState() {
        throw new IllegalArgumentException("A parent configuration must be provided as the state object to properly remove duplicates");
    }

    private boolean isListElement(ConfigurationNode node) {
        return node.getParent() != null && node.getParent().isList();
    }

    @Override
    public void beginVisit(ConfigurationNode node, ConfigurationNode parent) {
        Preconditions.checkNotNull(parent, "A parent configuration must be provided!");
    }

    @Override
    public void enterNode(ConfigurationNode node, ConfigurationNode parent) {
    }

    @Override
    public void enterMappingNode(ConfigurationNode node, ConfigurationNode parent) {
    }

    @Override
    public void enterListNode(ConfigurationNode node, ConfigurationNode parent) {
        if (!isListElement(node) && Objects.equals(node.getValue(), parent.getNode(node.getPath()).getValue())) {
            node.setValue(null);
        }
    }

    @Override
    @SuppressWarnings("UnnecessaryUnboxing")
    public void enterScalarNode(ConfigurationNode node, ConfigurationNode parent) {
        // ignore list values
        if (isListElement(node)) {
            return;
        }

        // if the node already exists in the parent config, remove it
        ConfigurationNode parentNode = parent.getNode(node.getPath());
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
    public void exitMappingNode(ConfigurationNode node, ConfigurationNode parent) {
        // remove empty maps
        if (node.isEmpty() && !isListElement(node)) {
            node.setValue(null);
        }
    }

    @Override
    public void exitListNode(ConfigurationNode node, ConfigurationNode parentRoot) {
        ConfigurationNode parent = parentRoot.getNode(node.getPath());
        if (parent.isEmpty() && parent.getValue() == null) {
            if (node.isEmpty()) {
                node.setValue(null);
            }
        }
    }

    @Override
    public Void endVisit(ConfigurationNode parent) {
        return null;
    }

}
