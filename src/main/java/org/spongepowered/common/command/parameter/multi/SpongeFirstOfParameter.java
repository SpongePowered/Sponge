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

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.common.command.brigadier.SpongeParameterTranslator;
import org.spongepowered.common.command.brigadier.tree.SpongeCommandExecutorWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpongeFirstOfParameter extends SpongeMultiParameter {

    protected SpongeFirstOfParameter(final List<Parameter> parameterCandidates, final boolean isOptional) {
        super(parameterCandidates, isOptional);
    }

    @Override
    public boolean createNode(
            final SpongeCommandExecutorWrapper executorWrapper,
            final Consumer<CommandNode<CommandSource>> builtNodeConsumer,
            final Consumer<ArgumentBuilder<CommandSource, ?>> nodeCallback,
            final List<CommandNode<CommandSource>> potentialOptionalRedirects,
            final boolean isTermination) {

        // If we have a termination, this is easy!
        // If not, it's a little more complicated. But it's all handled by `nodeCallback` anyway.
        boolean first = true;

        // This will grab the first built node, that is, the last in a tree list.
        final Grabber grabber = new Grabber(builtNodeConsumer);

        // The redirector redirects to the grabbed node - which will continue execution to the node after this.
        final Consumer<ArgumentBuilder<CommandSource, ?>> redirector = node -> node.redirect(grabber.grabbed);

        for (final Parameter parameter : this.getParameterCandidates()) {
            if (parameter instanceof SpongeSequenceParameter) {
                final SpongeSequenceParameter sequenceParameter = ((SpongeSequenceParameter) parameter);
                final boolean grab = first && !sequenceParameter.endsWithSubcommand();
                ((SpongeSequenceParameter) parameter).createNode(
                        executorWrapper,
                        grab ? grabber : builtNodeConsumer,
                        first && isTermination ? nodeCallback : redirector, // latter is used for merging command paths back together.
                        potentialOptionalRedirects,
                        isTermination);
            } else {
                final List<Parameter> parameters = new ArrayList<>();
                parameters.add(parameter);
                SpongeParameterTranslator.createNode(
                    parameters.listIterator(),
                    executorWrapper,
                    first && !(parameter instanceof Parameter.Subcommand) ? grabber : builtNodeConsumer,
                    first && isTermination ? nodeCallback : redirector, // latter is used for merging command paths back together.

                    // we don't want to redirect to secondary branches, so we hand a copy of the list, BUT we will want there to be
                    // redirects within the branch, and to later nodes.
                    first ? potentialOptionalRedirects : new ArrayList<>(potentialOptionalRedirects),
                    isTermination);
            }

            if (first && grabber.isValid()) {
                first = false;
            }
        }

        return this.getParameterCandidates().stream().anyMatch(Parameter::isOptional);
    }

    private static class Grabber implements Consumer<CommandNode<CommandSource>> {

        private final Consumer<CommandNode<CommandSource>> original;
        private CommandNode<CommandSource> grabbed;

        public Grabber(final Consumer<CommandNode<CommandSource>> original) {
            this.original = original;
        }

        @Override
        public void accept(final CommandNode<CommandSource> commandSourceCommandNode) {
            this.grabbed = commandSourceCommandNode;
            this.original.accept(commandSourceCommandNode);
        }

        public boolean isValid() {
            return this.original != null;
        }
    }
}
