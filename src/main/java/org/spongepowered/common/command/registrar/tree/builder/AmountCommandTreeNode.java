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
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

public final class AmountCommandTreeNode extends ArgumentCommandTreeNode<CommandTreeNode.Amount> implements CommandTreeNode.Amount {

    private final ArgumentType<?> ifSingle;
    private final ArgumentType<?> ifMultiple;
    private boolean single;

    public AmountCommandTreeNode(final CommandTreeNodeType<Amount> parameterType,
            final ArgumentType<?> ifSingle,
            final ArgumentType<?> ifMultiple) {
        super(parameterType);
        this.ifSingle = ifSingle;
        this.ifMultiple = ifMultiple;
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        if (this.single) {
            return this.ifSingle;
        } else {
            return this.ifMultiple;
        }
    }

    @Override
    public CommandTreeNode.Amount single() {
        this.single = true;
        return this;
    }

}
