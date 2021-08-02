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
package org.spongepowered.common.mixin.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.bridge.commands.CommandsBridge;
import org.spongepowered.common.bridge.commands.arguments.CompletionsArgumentTypeBridge;
import org.spongepowered.common.command.brigadier.dispatcher.DelegatingCommandDispatcher;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeNodePermissionCache;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeNode;
import org.spongepowered.common.command.brigadier.tree.SuggestionArgumentNode;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.CommandUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(Commands.class)
public abstract class CommandsMixin implements CommandsBridge {

    // @formatter:off
    @Shadow public abstract CommandDispatcher<CommandSourceStack> shadow$getDispatcher();
    @Shadow private void shadow$fillUsableCommands(final CommandNode<CommandSourceStack> rootCommandSource,
            final CommandNode<SharedSuggestionProvider> rootSuggestion,
            final CommandSourceStack source,
            final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode) {
        throw new AssertionError("This shouldn't be callable");
    }
    // @formatter:on

    private CauseStackManager.StackFrame impl$initFrame = null;
    private final WeakHashMap<ServerPlayer, Map<CommandNode<CommandSourceStack>, List<CommandNode<SharedSuggestionProvider>>>> impl$playerNodeCache =
            new WeakHashMap<>();
    private SpongeCommandManager impl$commandManager;

    // We prepare our own dispatcher and commands manager, to redirect registrations to our system
    @Redirect(method = "<init>", at = @At(
            value = "NEW",
            args = "class=com/mojang/brigadier/CommandDispatcher",
            remap = false
    ))
    private CommandDispatcher<CommandSourceStack> impl$useSpongeDispatcher() {
        final SpongeCommandManager manager = Launch.instance().lifecycle().platformInjector().getInstance(SpongeCommandManager.class);
        manager.init();
        this.impl$commandManager = manager;
        return new DelegatingCommandDispatcher(manager.getBrigadierRegistrar());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/commands/AdvancementCommands;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
    private void impl$setupStackFrameOnInit(final CommandDispatcher<CommandSourceStack> dispatcher) {
        this.impl$initFrame = PhaseTracker.getCauseStackManager().pushCauseFrame();
        this.impl$initFrame.pushCause(Launch.instance().minecraftPlugin());
        AdvancementCommands.register(dispatcher);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$tellDispatcherCommandsAreRegistered(final CallbackInfo ci) {
        this.impl$initFrame.popCause();
        PhaseTracker.getCauseStackManager().popCauseFrame(this.impl$initFrame);
        this.impl$initFrame = null;
    }

    /*
     * Hides nodes that we have marked as "hidden"
     */
    @Redirect(method = "fillUsableCommands",
            slice = @Slice(
                    from = @At("HEAD"),
                    to = @At(value = "INVOKE", remap = false, target = "Ljava/util/Iterator;hasNext()Z")
            ),
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;getChildren()Ljava/util/Collection;", remap = false))
    private Collection<CommandNode<CommandSourceStack>> impl$handleHiddenChildrenAndEnsureUnsortedLoop(final CommandNode<CommandSourceStack> commandNode) {
        return this.impl$getChildrenFromNode(commandNode);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Redirect(method = "fillUsableCommands",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/brigadier/tree/CommandNode;createBuilder()Lcom/mojang/brigadier/builder/ArgumentBuilder;",
                    remap = false))
    private ArgumentBuilder<?, ?> impl$createArgumentBuilder(
            final CommandNode<CommandSourceStack> commandNode,
            final CommandNode<CommandSourceStack> rootCommandSource,
            final CommandNode<SharedSuggestionProvider> rootSuggestion,
            final CommandSourceStack source,
            final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode) {
        if (commandNode instanceof SpongeArgumentCommandNode) {
            return ((SpongeArgumentCommandNode<?>) commandNode).createBuilderForSuggestions(rootSuggestion, commandNodeToSuggestionNode);
        }

        if (commandNode instanceof ArgumentCommandNode && ((ArgumentCommandNode<?, ?>) commandNode).getType() instanceof CompletionsArgumentTypeBridge<?>) {
            final ArgumentCommandNode<?, ?> argNode = (ArgumentCommandNode<?, ?>) commandNode;
            final RequiredArgumentBuilder<?, ?> builder = RequiredArgumentBuilder.argument(argNode.getName(),
                    ((CompletionsArgumentTypeBridge<?>) ((ArgumentCommandNode<?, ?>) commandNode).getType()).bridge$clientSideCompletionType());
            builder.executes((Command) argNode.getCommand())
                    .forward(argNode.getRedirect(), argNode.getRedirectModifier(), argNode.isFork())
                    .requires(argNode.getRequirement());
            if (!CommandUtil.checkForCustomSuggestions(rootSuggestion)) {
                builder.suggests((SuggestionProvider) SuggestionProviders.ASK_SERVER);
            }
            return builder;
        }
        return commandNode.createBuilder();
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "fillUsableCommands", at =
        @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private <K, V> V impl$preventPutIntoMapIfNodeIsComplex(final Map<K, V> map,
            final K key,
            final V value,
            final CommandNode<CommandSourceStack> rootCommandSource,
            final CommandNode<SharedSuggestionProvider> rootSuggestion,
            final CommandSourceStack source,
            final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode) {
        if (!map.containsKey(key)) {
            // done here because this check is applicable
            final ServerPlayer e = (ServerPlayer) source.getEntity();
            final Map<CommandNode<CommandSourceStack>, List<CommandNode<SharedSuggestionProvider>>> playerNodes = this.impl$playerNodeCache.get(e);
            if (!playerNodes.containsKey(key)) {
                final List<CommandNode<SharedSuggestionProvider>> children = new ArrayList<>();
                children.add((CommandNode<SharedSuggestionProvider>) value);
                playerNodes.put((CommandNode<CommandSourceStack>) key, children);
            }

            // If the current root suggestion has already got a custom suggestion and this node has a custom suggestion,
            // we need to swap it out.
            if (value instanceof ArgumentCommandNode && CommandUtil.checkForCustomSuggestions(rootSuggestion)) {
                rootSuggestion.addChild(this.impl$cloneArgumentCommandNodeWithoutSuggestions((ArgumentCommandNode<SharedSuggestionProvider, ?>) value));
            } else {
                rootSuggestion.addChild((CommandNode<SharedSuggestionProvider>) value);
            }
            return map.put(key, value);
        }
        return null; // it's ignored anyway.
    }

    @Redirect(method = "fillUsableCommands",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/brigadier/tree/CommandNode;addChild(Lcom/mojang/brigadier/tree/CommandNode;)V",
                    remap = false))
    private void impl$preventAddChild(final CommandNode<SharedSuggestionProvider> root, final CommandNode<SharedSuggestionProvider> newChild) {
        // no-op, we did this above.
    }

    @Redirect(method = "fillUsableCommands",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$testPermissionAndPreventRecalculationWhenSendingNodes(
            final CommandNode<CommandSourceStack> commandNode,
            final Object source,
            final CommandNode<CommandSourceStack> rootCommandNode,
            final CommandNode<SharedSuggestionProvider> rootSuggestion,
            final CommandSourceStack sourceButTyped,
            final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode
    ) {
        if (SpongeNodePermissionCache.canUse(
                rootCommandNode instanceof RootCommandNode, this.impl$commandManager.getDispatcher(), commandNode, sourceButTyped)) {

            boolean shouldContinue = true;
            if (commandNode instanceof SpongeArgumentCommandNode && ((SpongeArgumentCommandNode<?>) commandNode).isComplex()) {
                shouldContinue = false;
                final ServerPlayer e = (ServerPlayer) sourceButTyped.getEntity();
                final boolean hasCustomSuggestionsAlready = CommandUtil.checkForCustomSuggestions(rootSuggestion);
                final CommandNode<SharedSuggestionProvider> finalCommandNode = ((SpongeArgumentCommandNode<?>) commandNode).getComplexSuggestions(
                        rootSuggestion,
                        commandNodeToSuggestionNode,
                        this.impl$playerNodeCache.get(e),
                        !hasCustomSuggestionsAlready
                );
                if (!this.impl$getChildrenFromNode(commandNode).isEmpty()) {
                    this.shadow$fillUsableCommands(commandNode, finalCommandNode, sourceButTyped, commandNodeToSuggestionNode);
                }
            }

            if (shouldContinue) {
                // If we've already created the node, then just attach what we already have.
                final ServerPlayer e = (ServerPlayer) sourceButTyped.getEntity();
                final List<CommandNode<SharedSuggestionProvider>> suggestionProviderCommandNode = this.impl$playerNodeCache.get(e).get(commandNode);
                if (suggestionProviderCommandNode != null) {
                    shouldContinue = false;
                    boolean hasCustomSuggestionsAlready = CommandUtil.checkForCustomSuggestions(rootSuggestion);
                    for (final CommandNode<SharedSuggestionProvider> node : suggestionProviderCommandNode) {
                        // If we have custom suggestions, we need to limit it to one node, otherwise we trigger a bug
                        // in the client where it'll send more than one custom suggestion request - which is fine, except
                        // the client will then ignore all but one of them. This is a problem because we then end up with
                        // no suggestions - CompletableFuture.allOf(...) will contain an exception if a future is cancelled,
                        // meaning thenRun(...) does not run, which is how displaying the suggestions works...
                        //
                        // Because we don't control the client, we have to work around it here.
                        if (hasCustomSuggestionsAlready && node instanceof ArgumentCommandNode) {
                            final ArgumentCommandNode<SharedSuggestionProvider, ?> argNode = (ArgumentCommandNode<SharedSuggestionProvider, ?>) node;
                            if (argNode.getCustomSuggestions() != null) {
                                // Rebuild the node without the custom suggestions.
                                rootSuggestion.addChild(this.impl$cloneArgumentCommandNodeWithoutSuggestions(argNode));
                                continue;
                            }
                        } else if (node instanceof ArgumentCommandNode && ((ArgumentCommandNode<?, ?>) node).getCustomSuggestions() != null) {
                            hasCustomSuggestionsAlready = true; // no more custom suggestions
                        }
                        rootSuggestion.addChild(node);
                    }
                }
            }
            return shouldContinue;
        }
        return false;
    }

    @Redirect(method = "fillUsableCommands", at = @At(value = "INVOKE",
            target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;suggests(Lcom/mojang/brigadier/suggestion/SuggestionProvider;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;", remap = false))
    private RequiredArgumentBuilder<SharedSuggestionProvider, ?> impl$dontAskServerIfSiblingAlreadyDoes(
            final RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredArgumentBuilder,
            final SuggestionProvider<SharedSuggestionProvider> provider,
            final CommandNode<CommandSourceStack> rootCommandNode,
            final CommandNode<SharedSuggestionProvider> rootSuggestion,
            final CommandSourceStack sourceButTyped,
            final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode
    ) {
        // From above.
        //
        // If we have custom suggestions, we need to limit it to one node, otherwise we trigger a bug
        // in the client where it'll send more than one custom suggestion request - which is fine, except
        // the client will then ignore all but one of them. This is a problem because we then end up with
        // no suggestions - CompletableFuture.allOf(...) will contain an exception if a future is cancelled,
        // meaning thenRun(...) does not run, which is how displaying the suggestions works...
        //
        // Because we don't control the client, we have to work around it here.
        if (provider != SuggestionProviders.ASK_SERVER || !CommandUtil.checkForCustomSuggestions(rootSuggestion)) {
            requiredArgumentBuilder.suggests(provider);
        }
        return requiredArgumentBuilder;
    }

    @Redirect(method = "sendCommands", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;fillUsableCommands(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Map;)V"))
    private void impl$addNonBrigSuggestions(
            final Commands commands,
            final CommandNode<CommandSourceStack> p_197052_1_,
            final CommandNode<SharedSuggestionProvider> p_197052_2_,
            final CommandSourceStack p_197052_3_,
            final Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> p_197052_4_,
            final ServerPlayer playerEntity) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SUBJECT, (Subject) playerEntity);
            final CommandCause sourceToUse = ((CommandSourceStackBridge) p_197052_3_).bridge$withCurrentCause();
            try {
                this.impl$playerNodeCache.put(playerEntity, new IdentityHashMap<>());
                // We use this because the redirects should be a 1:1 mapping (which is what this map is for).
                final IdentityHashMap<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> idMap = new IdentityHashMap<>(p_197052_4_);
                this.shadow$fillUsableCommands(p_197052_1_, p_197052_2_, (CommandSourceStack) sourceToUse, idMap);
            } finally {
                this.impl$playerNodeCache.remove(playerEntity);
            }
            for (final CommandNode<SharedSuggestionProvider> node : this.impl$commandManager.getNonBrigadierSuggestions(sourceToUse)) {
                p_197052_2_.addChild(node);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Redirect(method = "fillUsableCommands",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/ArgumentBuilder;build()Lcom/mojang/brigadier/tree/CommandNode;", remap = false))
    private CommandNode<SharedSuggestionProvider> impl$createSpongeArgumentNode(final ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder) {
        if (argumentBuilder instanceof RequiredArgumentBuilder) {
            return new SuggestionArgumentNode<>((RequiredArgumentBuilder<SharedSuggestionProvider, ?>) argumentBuilder);
        }
        return argumentBuilder.build();
    }

    @Override
    public SpongeCommandManager bridge$commandManager() {
        return this.impl$commandManager;
    }

    private Collection<CommandNode<CommandSourceStack>> impl$getChildrenFromNode(final CommandNode<CommandSourceStack> parentNode) {
        final Collection<CommandNode<CommandSourceStack>> nodes;
        if (parentNode instanceof SpongeNode) {
            nodes = ((SpongeNode) parentNode).getChildrenForSuggestions();
        } else {
            nodes = parentNode.getChildren();
        }
        return nodes;
    }

    private ArgumentCommandNode<SharedSuggestionProvider, ?> impl$cloneArgumentCommandNodeWithoutSuggestions(final ArgumentCommandNode<SharedSuggestionProvider, ?> toClone) {
        final RequiredArgumentBuilder<SharedSuggestionProvider, ?> builder = toClone.createBuilder();
        builder.suggests(null);
        for (final CommandNode<SharedSuggestionProvider> node : toClone.getChildren()) {
            builder.then(node);
        }
        return new SuggestionArgumentNode<>(builder);
    }

}
