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
package org.spongepowered.common.command.brigadier.argument;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.ISuggestionProvider;

import java.util.List;
import java.util.function.Consumer;

/**
 * If the node to suggest is going to be complicated, use this
 * to specify it directly.
 */
public interface ComplexSuggestionNodeProvider {

    /**
     * Creates the nodes.
     */
    CommandNode<ISuggestionProvider> createSuggestions(final CommandNode<ISuggestionProvider> rootNode, final String key, final boolean isTerminal,
            final Consumer<List<CommandNode<ISuggestionProvider>>> commandNodeListConsumer,
            final Consumer<CommandNode<ISuggestionProvider>> mapInsertionConsumer, boolean allowCustomSuggestionsOnTheFirstElement);

}
