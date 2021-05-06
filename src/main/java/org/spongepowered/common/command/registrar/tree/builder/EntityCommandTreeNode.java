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
import net.minecraft.commands.arguments.EntityArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

// TODO
public final class EntityCommandTreeNode
        extends ArgumentCommandTreeNode<CommandTreeNode.EntitySelection>
        implements CommandTreeNode.EntitySelection {

    private boolean playersOnly = false;
    private boolean oneOnly = false;

    public EntityCommandTreeNode(final @Nullable CommandTreeNodeType<EntitySelection> parameterType) {
        super(parameterType);
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        if (this.playersOnly) {
            if (this.oneOnly) {
                return EntityArgument.player();
            }
            return EntityArgument.players();
        } else {
            if (this.oneOnly) {
                return EntityArgument.entity();
            }
            return EntityArgument.entities();
        }
    }

    @Override
    public @NonNull EntitySelection playersOnly() {
        this.playersOnly = true;
        return this;
    }

    public boolean isPlayersOnly() {
        return this.playersOnly;
    }

    @Override
    public EntitySelection single() {
        this.oneOnly = true;
        return this;
    }
}
