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
package org.spongepowered.common.command.resolver;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.common.bridge.commands.arguments.CompletionsArgumentTypeBridge;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeNodePermissionCache;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeNode;
import org.spongepowered.common.command.brigadier.tree.SuggestionArgumentNode;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.util.CommandUtil;
import org.spongepowered.common.util.Constants;

import java.util.*;

public record SpongeSuggestionTreeResolver(
    CommandSourceStack source,
    Map<CommandNode<CommandSourceStack>, CommandNode<CommandSourceStack>> commandToSuggestion,
    Map<CommandNode<CommandSourceStack>, List<CommandNode<CommandSourceStack>>> playerNodes,
    SpongeCommandManager commandManager
) {

    /**
     * Replacement of Commands#fillUsableCommands.
     * There is no need to split this into injections to be compatible with mods because modded platforms never call fillUsableCommands.
     * Instead, they redirect fillUsableCommands to their own method, like we do.
     */
    public void fillSuggestions(final CommandNode<CommandSourceStack> rootCommandNode, final CommandNode<CommandSourceStack> rootSuggestion) {
        for (final CommandNode<CommandSourceStack> commandNode : SpongeSuggestionTreeResolver.getChildren(rootCommandNode)) {
            final List<CommandNode<CommandSourceStack>> existingNodes = this.playerNodes.get(commandNode);
            if (existingNodes != null) {
                if (!existingNodes.isEmpty()) {
                    boolean hasCustomSuggestionsAlready = CommandUtil.checkForCustomSuggestions(rootSuggestion);
                    for (final CommandNode<CommandSourceStack> node : existingNodes) {
                        // If we have custom suggestions, we need to limit it to one node, otherwise we trigger a bug
                        // in the client where it'll send more than one custom suggestion request - which is fine, except
                        // the client will then ignore all but one of them. This is a problem because we then end up with
                        // no suggestions - CompletableFuture.allOf(...) will contain an exception if a future is cancelled,
                        // meaning thenRun(...) does not run, which is how displaying the suggestions works...
                        //
                        // Because we don't control the client, we have to work around it here.
                        if (hasCustomSuggestionsAlready && node instanceof ArgumentCommandNode<CommandSourceStack, ?> argNode) {
                            if (CommandUtil.asksServer(argNode.getCustomSuggestions())) {
                                // Rebuild the node without the custom suggestions.
                                rootSuggestion.addChild(SpongeSuggestionTreeResolver.cloneArgumentWithoutSuggestions(argNode));
                                continue;
                            }
                        } else if (node instanceof ArgumentCommandNode && CommandUtil.asksServer(((ArgumentCommandNode<?, ?>) node).getCustomSuggestions())) {
                            hasCustomSuggestionsAlready = true; // no more custom suggestions
                        }
                        rootSuggestion.addChild(node);
                    }
                }
                // If empty, we have a node that won't resolve (even if not complex), so we ignore it.
                continue;
                // If we have already processed this node, and it appears in the suggestion node list, prevent a potentially costly
                // canUse check as we know we can already use it.
            }

            if (!this.commandToSuggestion.containsKey(commandNode) && !SpongeNodePermissionCache.canUse(
                rootCommandNode instanceof RootCommandNode, this.commandManager.getDispatcher(), commandNode, this.source)) {
                this.playerNodes.put(commandNode, Collections.emptyList());
                continue;
            }

            if (commandNode instanceof SpongeArgumentCommandNode<?> spongeCommandNode && spongeCommandNode.isComplex()) {
                final boolean hasCustomSuggestionsAlready = CommandUtil.checkForCustomSuggestions(rootSuggestion);
                final CommandNode<CommandSourceStack> finalCommandNode = spongeCommandNode.getComplexSuggestions(rootSuggestion, this.commandToSuggestion, this.playerNodes, !hasCustomSuggestionsAlready);
                if (!SpongeSuggestionTreeResolver.getChildren(commandNode).isEmpty()) {
                    this.fillSuggestions(commandNode, finalCommandNode);
                }
                continue;
            }

            ArgumentBuilder<CommandSourceStack, ?> builder;
            if (commandNode instanceof SpongeArgumentCommandNode<?> node) {
                builder = node.createBuilderForSuggestions(rootSuggestion, this.commandToSuggestion);
            } else if (commandNode instanceof ArgumentCommandNode<CommandSourceStack, ?> argNode && argNode.getType() instanceof CompletionsArgumentTypeBridge<?> argType) {
                final RequiredArgumentBuilder<CommandSourceStack, ?> r = RequiredArgumentBuilder.argument(argNode.getName(), argType.bridge$clientSideCompletionType());
                r.executes(argNode.getCommand())
                    .forward(argNode.getRedirect(), argNode.getRedirectModifier(), argNode.isFork())
                    .requires(argNode.getRequirement());
                if (!CommandUtil.checkForCustomSuggestions(rootSuggestion)) {
                    r.suggests(Constants.Command.ASK_SERVER);
                }
                builder = r;
            } else {
                builder = commandNode.createBuilder();
            }

            // From above.
            //
            // If we have custom suggestions, we need to limit it to one node, otherwise we trigger a bug
            // in the client where it'll send more than one custom suggestion request - which is fine, except
            // the client will then ignore all but one of them. This is a problem because we then end up with
            // no suggestions - CompletableFuture.allOf(...) will contain an exception if a future is cancelled,
            // meaning thenRun(...) does not run, which is how displaying the suggestions works...
            //
            // Because we don't control the client, we have to work around it here.
            if (builder instanceof RequiredArgumentBuilder r && r.getSuggestionsProvider() == Constants.Command.ASK_SERVER && CommandUtil.checkForCustomSuggestions(rootSuggestion)) {
                r.suggests(null);
            }

            if (builder.getRedirect() != null) {
                builder.redirect(this.commandToSuggestion.get(builder.getRedirect()));
            }

            final CommandNode<CommandSourceStack> suggestion = builder instanceof RequiredArgumentBuilder r ? new SuggestionArgumentNode(r) : builder.build();

            if (!this.commandToSuggestion.containsKey(commandNode)) {
                // done here because this check is applicable
                if (!this.playerNodes.containsKey(commandNode)) {
                    final List<CommandNode<CommandSourceStack>> children = new ArrayList<>();
                    children.add(suggestion);
                    this.playerNodes.put(commandNode, children);
                }

                // If the current root suggestion has already got a custom suggestion and this node has a custom suggestion,
                // we need to swap it out.
                if (commandNode instanceof ArgumentCommandNode<CommandSourceStack, ?> argNode && CommandUtil.checkForCustomSuggestions(rootSuggestion)) {
                    rootSuggestion.addChild(SpongeSuggestionTreeResolver.cloneArgumentWithoutSuggestions(argNode));
                } else {
                    rootSuggestion.addChild(suggestion);
                }
                this.commandToSuggestion.put(commandNode, suggestion);
            }

            if (!commandNode.getChildren().isEmpty()) {
                this.fillSuggestions(commandNode, suggestion);
            }
        }
    }

    private static Collection<CommandNode<CommandSourceStack>> getChildren(final CommandNode<CommandSourceStack> node) {
        if (node instanceof SpongeNode) {
            return ((SpongeNode) node).getChildrenForSuggestions();
        }
        return node.getChildren();
    }

    private static <S> ArgumentCommandNode<CommandSourceStack, S> cloneArgumentWithoutSuggestions(final ArgumentCommandNode<CommandSourceStack, S> toClone) {
        final RequiredArgumentBuilder<CommandSourceStack, S> builder = toClone.createBuilder();
        builder.suggests(null);
        for (final CommandNode<CommandSourceStack> node : toClone.getChildren()) {
            builder.then(node);
        }
        return new SuggestionArgumentNode<>(builder);
    }
}
