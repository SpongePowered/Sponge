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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContextBuilder;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeRootCommandNode;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// For use on the Brigadier dispatcher
public final class SpongeCommandDispatcher extends CommandDispatcher<CommandSource> {

    public SpongeCommandDispatcher() {
        super(new SpongeRootCommandNode());
    }

    public LiteralCommandNode<CommandSource> register(final LiteralCommandNode<CommandSource> command) {
        this.getRoot().addChild(command);
        return command;
    }

    @Override
    public ParseResults<CommandSource> parse(final String command, final CommandSource source) {
        final SpongeCommandContextBuilder builder = new SpongeCommandContextBuilder(this, source, this.getRoot(), 0);
        return this.parseNodes(true, this.getRoot(), new SpongeStringReader(command), builder);
    }

    @Override
    public ParseResults<CommandSource> parse(final StringReader command, final CommandSource source) {
        final SpongeCommandContextBuilder builder = new SpongeCommandContextBuilder(this, source, this.getRoot(), command.getCursor());
        final SpongeStringReader reader = new SpongeStringReader(command);
        return this.parseNodes(true, this.getRoot(), reader, builder);
    }

    @Override
    public int execute(final StringReader input, final CommandSource source) throws CommandSyntaxException {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final CommandSourceBridge sourceBridge = (CommandSourceBridge) source;
            frame.pushCause(sourceBridge.bridge$getICommandSource());
            frame.addContext(EventContextKeys.COMMAND, input.getString());
            return ((SpongeCommandManager) SpongeCommon.getGame().getCommandManager()).process(sourceBridge.bridge$asCommandCause(), input.getRemaining()).getResult();
        } catch (final CommandException e) {
            throw new net.minecraft.command.CommandException(SpongeAdventure.asVanilla(e.getText()));
        }
    }

    private ParseResults<CommandSource> parseNodes(
            final boolean isRoot,
            final CommandNode<CommandSource> node,
            final SpongeStringReader originalReader,
            final SpongeCommandContextBuilder contextSoFar) {

        final CommandSource source = contextSoFar.getSource();
        // Sponge Start
        Map<CommandNode<CommandSource>, CommandSyntaxException> errors = null;
        List<ParseResults<CommandSource>> potentials = null;
        // Sponge End
        final int cursor = originalReader.getCursor();

        for (final CommandNode<CommandSource> child : node.getRelevantNodes(originalReader)) {
            // Sponge Start
            // If we've got a potential result, don't try a default.
            if (child instanceof SpongeArgumentCommandNode && ((SpongeArgumentCommandNode<?>) child).getParser().doesNotRead()
                    && potentials != null) {
                continue;
            }
            // We need to do a little more scaffolding for permissions
            // if (!child.canUse(source)) {
            if (!SpongeNodePermissionCache.canUse(isRoot, this, child, source)) {
            // Sponge End
                continue;
            }
            // Sponge Start
            final SpongeCommandContextBuilder context = contextSoFar.copy();
            final SpongeStringReader reader = new SpongeStringReader(originalReader);
            // Sponge End
            try {
                try {
                     child.parse(reader, context);
                } catch (final RuntimeException ex) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, ex.getMessage());
                }
                // Sponge Start: if we didn't consume anything we don't want Brig to complain at us.
                if (reader.getCursor() == cursor) {
                    reader.unskipWhitespace();
                } else if (reader.canRead()) {
                // Sponge End
                    if (reader.peek() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader);
                    }
                }
            } catch (final CommandSyntaxException ex) {
                if (errors == null) {
                    errors = new LinkedHashMap<>();
                }
                errors.put(child, ex);
                reader.setCursor(cursor);
                continue;
            }

            context.withCommand(child.getCommand());
            // Sponge Start: if we have a node that can parse an empty, we
            // must let it do so.
            if (reader.canRead(child.getRedirect() == null ? 2 : 1)
                || child.getChildren().stream().anyMatch(x -> x instanceof SpongeArgumentCommandNode &&
                    ((SpongeArgumentCommandNode<?>) x).getParser().doesNotRead())) {
            // Sponge End
                reader.skip();
                // Sponge Start: redirect is now in a local variable as we use it a fair bit
                final CommandNode<CommandSource> redirect = child.getRedirect();
                if (redirect != null) {
                    final SpongeCommandContextBuilder childContext =
                            new SpongeCommandContextBuilder(this, source, child.getRedirect(), reader.getCursor());
                    final ParseResults<CommandSource> parse = this.parseNodes(redirect instanceof RootCommandNode,
                            child.getRedirect(),
                            reader,
                            childContext);
                    // Sponge End
                    context.withChild(parse.getContext());
                    return new ParseResults<>(context, parse.getReader(), parse.getExceptions());
                } else {
                    final ParseResults<CommandSource> parse = this.parseNodes(false, child, reader, context);
                    if (potentials == null) {
                        potentials = new ArrayList<>(1);
                    }
                    potentials.add(parse);
                }
            } else {
                if (potentials == null) {
                    potentials = new ArrayList<>(1);
                }
                potentials.add(new ParseResults<>(context, reader, Collections.emptyMap()));
            }
        }

        if (potentials != null) {
            if (potentials.size() > 1) {
                potentials.sort((a, b) -> {
                    if (!a.getReader().canRead() && b.getReader().canRead()) {
                        return -1;
                    }
                    if (a.getReader().canRead() && !b.getReader().canRead()) {
                        return 1;
                    }
                    if (a.getExceptions().isEmpty() && !b.getExceptions().isEmpty()) {
                        return -1;
                    }
                    if (!a.getExceptions().isEmpty() && b.getExceptions().isEmpty()) {
                        return 1;
                    }
                    return 0;
                });
            }
            return potentials.get(0);
        }

        return new ParseResults<>(contextSoFar, originalReader, errors == null ? Collections.emptyMap() : errors);
    }

}
