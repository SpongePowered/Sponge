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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class UnsortedNodeHolder {

    // used so we can have insertion order.
    private final List<CommandNode<CommandSource>> standardChildren = new LinkedList<>();
    private final List<CommandNode<CommandSource>> redirectingChildren = new LinkedList<>();

    @Nullable private ImmutableList<CommandNode<CommandSource>> cachedResult;

    public void add(final CommandNode<CommandSource> node) {
        this.cachedResult = null;
        if (node.getRedirect() == null) {
            this.standardChildren.add(node);
        } else {
            this.redirectingChildren.add(node);
        }
    }

    public Collection<CommandNode<CommandSource>> getChildren() {
        if (this.cachedResult == null) {
            this.cachedResult = ImmutableList.<CommandNode<CommandSource>>builder()
                    .addAll(this.standardChildren)
                    .addAll(this.redirectingChildren)
                    .build();
        }

        return this.cachedResult;
    }

}
