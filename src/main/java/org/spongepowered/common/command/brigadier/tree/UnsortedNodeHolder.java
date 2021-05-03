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
package org.spongepowered.common.command.brigadier.tree;

import com.mojang.brigadier.tree.CommandNode;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;

public final class UnsortedNodeHolder {

    // used so we can have insertion order.
    private final List<CommandNode<CommandSourceStack>> standardChildren = new LinkedList<>();
    private final List<CommandNode<CommandSourceStack>> redirectingChildren = new LinkedList<>();

    private @Nullable List<CommandNode<CommandSourceStack>> cachedResult;

    public void add(final CommandNode<CommandSourceStack> node) {
        this.cachedResult = null;
        if (node.getRedirect() == null) {
            this.standardChildren.add(node);
        } else {
            this.redirectingChildren.add(node);
        }
    }

    public Collection<CommandNode<CommandSourceStack>> getChildren() {
        if (this.cachedResult == null) {
            final LinkedList<CommandNode<CommandSourceStack>> result = new LinkedList<>();
            result.addAll(this.standardChildren);
            result.addAll(this.redirectingChildren);
            this.cachedResult = Collections.unmodifiableList(result);
        }

        return this.cachedResult;
    }

    // Handles hidden nodes
    public Collection<CommandNode<CommandSourceStack>> getChildrenForSuggestions() {
        final ArrayList<CommandNode<CommandSourceStack>> nodes = new ArrayList<>();
        for (final CommandNode<CommandSourceStack> childNode : this.getChildren()) {
            if (childNode instanceof SpongeArgumentCommandNode && ((SpongeArgumentCommandNode<CommandSourceStack>) childNode).getParser().doesNotRead()) {
                final CommandNode<CommandSourceStack> redirected = childNode.getRedirect();
                if (redirected != null) {
                    // get the nodes from the redirect
                    if (redirected instanceof SpongeArgumentCommandNode) {
                        nodes.addAll(((SpongeArgumentCommandNode<CommandSourceStack>) redirected).getChildrenForSuggestions());
                    } else {
                        nodes.addAll(redirected.getChildren());
                    }
                } else {
                    nodes.addAll(((SpongeArgumentCommandNode<CommandSourceStack>) childNode).getChildrenForSuggestions());
                }
            } else {
                nodes.add(childNode);
            }
        }
        nodes.sort((first, second) -> {
            if (first.getRedirect() == null) {
                return second.getRedirect() == null ? 0 : -1;
            } else if (second.getRedirect() == null) {
                return 1;
            }

            // if both are redirects...
            if (first.getRedirect() == second) {
                // second goes first.
                return 1;
            } else if (second.getRedirect() == first) {
                // first goes first
                return -1;
            }
            return 0; // don't care.
        });
        return nodes;
    }

}
