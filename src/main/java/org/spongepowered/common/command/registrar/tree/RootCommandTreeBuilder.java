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
package org.spongepowered.common.command.registrar.tree;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Map;

public final class RootCommandTreeBuilder extends AbstractCommandTreeBuilder<CommandTreeBuilder.Basic, RootCommandNode<CommandSource>>
        implements CommandTreeBuilder.Basic {

    @Override
    protected RootCommandNode<CommandSource> createArgumentTree(final String nodeKey, final Command<CommandSource> command) {
        throw new IllegalStateException("RootCommandTreeBuilder must not be part of a tree (except as the root!)");
    }

    public Collection<CommandNode<CommandSource>> createArgumentTree(final Command<CommandSource> command) {
        // The node key is ignored here.
        final ImmutableList.Builder<CommandNode<CommandSource>> builder = ImmutableList.builder();
        this.getChildren().forEach((key, value) -> builder.add(value.createArgumentTree(key, command)));
        return builder.build();
    }

    public void addChildren(final Map<String, AbstractCommandTreeBuilder<?, ?>> children) {
        this.addChildrenInternal(children);
    }

}
