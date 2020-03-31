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
package org.spongepowered.common.command.parameter.multi;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.common.command.brigadier.tree.SpongeCommandExecutorWrapper;

import java.util.List;
import java.util.function.Consumer;

public abstract class SpongeMultiParameter implements Parameter {

    private final List<Parameter> parameterCandidates;
    private final boolean isOptional;

    protected SpongeMultiParameter(final List<Parameter> parameterCandidates, final boolean isOptional) {
        this.parameterCandidates = ImmutableList.copyOf(parameterCandidates);
        this.isOptional = isOptional;
    }

    public List<Parameter> getParameterCandidates() {
        return this.parameterCandidates;
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    public abstract boolean createNode(SpongeCommandExecutorWrapper executorWrapper,
            Consumer<CommandNode<CommandSource>> buildNodeConsumer,
            Consumer<ArgumentBuilder<CommandSource, ?>> nodeCallback,
            List<CommandNode<CommandSource>> potentialOptionalRedirects,
            boolean isTermination);
}
