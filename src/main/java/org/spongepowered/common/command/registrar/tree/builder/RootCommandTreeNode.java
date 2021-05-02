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
package org.spongepowered.common.command.registrar.tree.builder;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.common.command.brigadier.tree.ForcedRedirectNode;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RootCommandTreeNode extends AbstractCommandTreeNode<CommandTreeNode.Root, RootCommandNode<SharedSuggestionProvider>>
        implements CommandTreeNode.Root {

    public @Nullable CommandNode<SharedSuggestionProvider> createArgumentTree(final CommandCause cause, final LiteralArgumentBuilder<SharedSuggestionProvider> rootBuilder) {
        if (this.getRequirement().test(cause)) {
            final Map<AbstractCommandTreeNode<?, ?>, CommandNode<SharedSuggestionProvider>> nodeToSuggestionProvider = new IdentityHashMap<>();
            // this is going to be iterated only.
            final Map<ForcedRedirectNode, AbstractCommandTreeNode<?, ?>> redirectsToApply = new LinkedHashMap<>();

            if (this.isExecutable()) {
                rootBuilder.executes(c -> 1);
            }

            final LiteralCommandNode<SharedSuggestionProvider> rootNode = rootBuilder.build();
            this.addChildNodesToTree(
                    cause,
                    rootNode,
                    nodeToSuggestionProvider,
                    redirectsToApply
            );

            // now create the redirects.
            redirectsToApply.forEach((node, key) -> node.setForcedRedirect(nodeToSuggestionProvider.get(key)));
            return rootNode;
        }
        return null;
    }

    @Override
    protected RootCommandNode<SharedSuggestionProvider> createElement(final String nodeKey) {
        // node key is ignored.
        return new RootCommandNode<>();
    }
}
