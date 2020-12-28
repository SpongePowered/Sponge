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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;

import java.util.Objects;

public final class SpongeFlagLiteralCommandNode extends SpongeLiteralCommandNode {

    private final Flag flag;
    private Command<CommandSource> executor;

    public SpongeFlagLiteralCommandNode(final LiteralArgumentBuilder<CommandSource> argumentBuilder, final Flag flag) {
        super(argumentBuilder);
        this.flag = flag;
    }

    @Override
    public void parse(final StringReader reader, final CommandContextBuilder<CommandSource> contextBuilder) throws CommandSyntaxException {
        super.parse(reader, contextBuilder);
        ((SpongeCommandContextBuilder) contextBuilder).addFlagInvocation(this.flag);
    }

    @Override
    public void forceExecutor(final Command<CommandSource> forcedExecutor) {
        this.executor = forcedExecutor;
    }

    @Override
    public Command<CommandSource> getCommand() {
        final Command<CommandSource> command = super.getCommand();
        if (command != null) {
            return command;
        }
        return this.executor;
    }

    // Circular references cause problems - this sidesteps the issue.
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeFlagLiteralCommandNode that = (SpongeFlagLiteralCommandNode) o;
        return Objects.equals(this.getName(), that.getName()) &&
                this.getRedirect() == that.getRedirect() && // reference equality is intended
                this.getChildren().size() == that.getChildren().size() &&
                this.getChildren().stream().map(CommandNode::getName).allMatch(x -> that.getChild(x) != null) && // make sure all children exist
                Objects.equals(this.flag, that.flag) &&
                Objects.equals(this.executor, that.executor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName(), this.getChildren().size(), this.flag, this.executor);
    }

}
