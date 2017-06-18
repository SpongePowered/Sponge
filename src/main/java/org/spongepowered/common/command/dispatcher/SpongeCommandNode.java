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
package org.spongepowered.common.command.dispatcher;

import com.google.common.collect.Maps;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.dispatcher.CommandNode;
import org.spongepowered.api.command.dispatcher.Dispatcher;

import java.util.Map;

import javax.annotation.Nullable;

public class SpongeCommandNode implements CommandNode {

    private final CommandMapping commandMapping;
    @Nullable private Map<String, CommandNode> subnodes = null;

    public SpongeCommandNode(CommandMapping commandMapping) {
        this.commandMapping = commandMapping;
    }

    @Override
    public CommandMapping getCommandMapping() {
        return this.commandMapping;
    }

    @Override
    public Map<String, ? extends CommandNode> getSubcommands() {
        if (this.subnodes == null) {
            this.subnodes = Maps.newHashMap();
            Command command = this.commandMapping.getCommand();
            if (command instanceof Dispatcher) {
                Dispatcher dispatcher = ((Dispatcher) command);
                for (String alias : dispatcher.getPrimaryAliases()) {
                    dispatcher.getCommandNode(alias)
                            .ifPresent(mapping -> this.subnodes.put(mapping.getCommandMapping().getPrimaryAlias(), mapping));
                }
            }
        }

        return this.subnodes;
    }
}
