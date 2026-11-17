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
package org.spongepowered.common.command.brigadier.dispatcher;

import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParseContext;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * What vanilla's parse and suggestion boxes hold in place of their own copy
 * of the command tree: every call goes to the Sponge dispatcher, which works
 * on a {@link CommandSourceStack} rather than on the box's context.
 *
 * <p>The node, parse result and usage types are only the Sponge dispatcher's
 * with another type parameter, hence the unchecked casts.</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpongeBoxDispatcher<C extends ParseContext> extends CommandDispatcher<C> {

    private final DelegatingCommandDispatcher delegate;
    private final boolean suggestions;

    public SpongeBoxDispatcher(final DelegatingCommandDispatcher delegate, final boolean suggestions) {
        this.delegate = delegate;
        this.suggestions = suggestions;
    }

    public PermissionService permissionService() {
        return this.delegate.permissionService();
    }

    public void permissionService(final PermissionService permissionService) {
        this.delegate.permissionService(permissionService);
    }

    private CommandSourceStack source(final C context) {
        if (context instanceof final CommandSourceStack source) {
            return source;
        }
        if (context instanceof final SuggestionContext suggestion && suggestion.entity() instanceof final ServerPlayer player) {
            return player.createCommandSourceStack();
        }
        // Nothing to run as, such as a function being compiled: the context still decides what parses
        final CommandSourceStack source = new CommandSourceStack(
            CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, context.permissions(), CommonComponents.EMPTY, null);
        final CommandSourceStackBridge bridge = (CommandSourceStackBridge) source;
        bridge.bridge$parseContext(context);
        bridge.bridge$permissionService(this.delegate.permissionService());
        return source;
    }

    @Override
    public ParseResults<C> parse(final String command, final C context) {
        return (ParseResults) this.delegate.spongeDispatcher().parse(command, this.source(context), this.suggestions);
    }

    @Override
    public ParseResults<C> parse(final StringReader command, final C context) {
        return (ParseResults) this.delegate.spongeDispatcher().parse(command, this.source(context), this.suggestions);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<C> parse) {
        return this.delegate.getCompletionSuggestions((ParseResults) parse);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(final ParseResults<C> parse, final int cursor) {
        return this.delegate.getCompletionSuggestions((ParseResults) parse, cursor);
    }

    @Override
    public Map<CommandNode<C>, String> getSmartUsage(final CommandNode<C> node, final C context) {
        return (Map) this.delegate.getSmartUsage((CommandNode) node, this.source(context));
    }

    @Override
    public String[] getAllUsage(final CommandNode<C> node, final C context, final boolean restricted) {
        return this.delegate.getAllUsage((CommandNode) node, this.source(context), restricted);
    }

    @Override
    public RootCommandNode<C> getRoot() {
        return (RootCommandNode) this.delegate.getRoot();
    }

    @Override
    public Collection<String> getPath(final CommandNode<C> target) {
        return this.delegate.getPath((CommandNode) target);
    }

    @Override
    public CommandNode<C> findNode(final Collection<String> path) {
        return (CommandNode) this.delegate.findNode(path);
    }

    @Override
    public void findAmbiguities(final AmbiguityConsumer<C> consumer) {
        // As for the Sponge dispatcher: this produces bad information for Sponge
    }
}
