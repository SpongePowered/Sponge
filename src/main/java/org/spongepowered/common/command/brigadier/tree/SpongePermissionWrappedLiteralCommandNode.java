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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Used as a marker to indicate that this root node is not Sponge native.
 */
public final class SpongePermissionWrappedLiteralCommandNode extends SpongeLiteralCommandNode {

    @Nullable private Command<CommandSourceStack> executor;

    public SpongePermissionWrappedLiteralCommandNode(
            final LiteralArgumentBuilder<CommandSourceStack> builder) {
        super(builder);

        for (final CommandNode<CommandSourceStack> argument : builder.getArguments()) {
            this.addChild(argument);
        }
    }

    @Override
    public void forceExecutor(final Command<CommandSourceStack> forcedExecutor) {
        this.executor = forcedExecutor;
    }

    @Override
    public Command<CommandSourceStack> getCommand() {
        final Command<CommandSourceStack> command = super.getCommand();
        if (command != null) {
            return command;
        }
        return this.executor;
    }

}
