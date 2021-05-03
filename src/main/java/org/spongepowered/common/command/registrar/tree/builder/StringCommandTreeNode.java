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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

public final class StringCommandTreeNode extends ArgumentCommandTreeNode<CommandTreeNode.StringParser> implements CommandTreeNode.StringParser {

    private Types type = Types.PHRASE;

    public StringCommandTreeNode(final CommandTreeNodeType<@NonNull StringParser> parameterType) {
        super(parameterType);
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        switch (this.type) {
            case WORD:
                return StringArgumentType.word();
            case GREEDY:
                return StringArgumentType.greedyString();
            case PHRASE:
                return StringArgumentType.string();
        }
        throw new IllegalStateException("Incorrect argument type.");
    }

    @Override
    public StringParser word() {
        this.type = Types.WORD;
        return this;
    }

    @Override
    public StringParser greedy() {
        this.type = Types.GREEDY;
        return this;
    }

    /**
     * The behaviors available to the string parser.
     */
    public enum Types {

        /**
         * Will parse the next word
         */
        WORD,

        /**
         * Will parse the next word, or, if quoted, the quoted string
         */
        PHRASE,

        /**
         * Will parse the remainder of the argument string
         */
        GREEDY

    }
}
