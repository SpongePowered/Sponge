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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;

public interface SpongeNode {

    // Handles hidden nodes
    Collection<CommandNode<CommandSourceStack>> getChildrenForSuggestions();

    // will be implemented by the class
    Collection<? extends CommandNode<CommandSourceStack>> getRelevantNodes(final StringReader input);

    default Collection<? extends CommandNode<CommandSourceStack>> getRelevantNodesForSuggestions(final StringReader input) {
        final Collection<? extends CommandNode<CommandSourceStack>> result = this.getRelevantNodes(input);
        if (result.isEmpty()) {
            return result;
        }
        if (result.size() == 1 && result.iterator().next() instanceof LiteralCommandNode) {
            return result;
        }

        return this.getChildrenForSuggestions();
    }

    void forceExecutor(Command<CommandSourceStack> forcedExecutor);

    boolean canForceRedirect();

    void forceRedirect(CommandNode<CommandSourceStack> forcedRedirect);
}
