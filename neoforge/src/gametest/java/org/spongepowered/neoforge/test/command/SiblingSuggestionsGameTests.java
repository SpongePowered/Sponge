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
package org.spongepowered.neoforge.test.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.server.command.CommandHelper;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Checks that merging the client-bound command tree on NeoForge keeps a single
 * server-asked suggestion node per parent, like the vanilla path does
 * (SpongePowered/Sponge#4327, item A). Two siblings that both ask the server
 * make the client cancel the first request, so the suggestion window never
 * opens.
 */
@GameTestGroup("command")
public final class SiblingSuggestionsGameTests {

    @SuppressWarnings("unchecked")
    @GameTest(maxTicks = 20)
    public void mergeKeepsOneServerSuggestion(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final SuggestionProvider<CommandSourceStack> askServer = (SuggestionProvider<CommandSourceStack>) (SuggestionProvider<?>) SuggestionProviders.ASK_SERVER;

        final LiteralArgumentBuilder<CommandSourceStack> parent = LiteralArgumentBuilder.literal("siblingtest");
        parent.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("first", StringArgumentType.word())
                .suggests(askServer));
        parent.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("second", StringArgumentType.word())
                .suggests(askServer));
        final LiteralCommandNode<CommandSourceStack> sourceParent = parent.build();

        final RootCommandNode<CommandSourceStack> sourceRoot = new RootCommandNode<>();
        sourceRoot.addChild(sourceParent);
        final RootCommandNode<CommandSourceStack> resultRoot = new RootCommandNode<>();
        final Map<CommandNode<CommandSourceStack>, CommandNode<CommandSourceStack>> idMap = new IdentityHashMap<>();

        CommandHelper.mergeCommandNode(sourceRoot, resultRoot, idMap, source, this.noopCommand(),
                (Function<SuggestionProvider<CommandSourceStack>, SuggestionProvider<CommandSourceStack>>) provider -> provider);

        final CommandNode<CommandSourceStack> merged = resultRoot.getChild("siblingtest");
        helper.assertTrue(merged != null, "The test command was not merged into the result tree");
        int asking = 0;
        for (final CommandNode<CommandSourceStack> child : merged.getChildren()) {
            if (child instanceof ArgumentCommandNode
                    && ((ArgumentCommandNode<?, ?>) child).getCustomSuggestions() == SuggestionProviders.ASK_SERVER) {
                asking++;
            }
        }
        helper.assertTrue(asking == 1, "Expected exactly one child asking the server, found " + asking);
        helper.succeed();
    }

    private Command<CommandSourceStack> noopCommand() {
        return context -> 1;
    }
}
