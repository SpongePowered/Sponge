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
package org.spongepowered.common.test.gametest.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.common.command.brigadier.argument.ClientNativeArgumentParser;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNode;
import org.spongepowered.common.command.brigadier.tree.SpongeArgumentCommandNodeBuilder;
import org.spongepowered.common.command.parameter.SpongeParameterKey;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

/**
 * Checks that argument nodes carry the client-side argument type declared by
 * their parser. Without this, a value parameter that expects e.g. a player
 * arrives at the client as a plain string and neither the client nor the
 * server can complete it (SpongePowered/Sponge#4327, items B and C).
 */
@GameTestGroup("command")
public final class ClientArgumentTypeGameTests {

    @GameTest(maxTicks = 20)
    public void clientArgumentTypeReachesTheNode(final GameTestHelper helper) {
        helper.assertTrue(this.greedyTextNodeKeepsTheClientType(),
                "A remainingJoinedStrings argument node did not carry the greedy client argument type");
        helper.assertTrue(this.playerNodeKeepsTheClientType(),
                "A player argument node did not carry the player client argument type");
        helper.assertTrue(this.nativeCompleterIsNotForwarded(),
                "A native completion parser must not be sent to the server as a completer");
        helper.succeed();
    }

    private boolean greedyTextNodeKeepsTheClientType() {
        final ArgumentType<String> greedy = StringArgumentType.greedyString();
        return this.node(name -> ClientNativeArgumentParser.createIdentity(ResourceKey.of("test", name), greedy)).getType() == greedy;
    }

    private boolean playerNodeKeepsTheClientType() {
        final ArgumentType<?> player = EntityArgument.player();
        return this.node(name -> ClientNativeArgumentParser.createIdentity(ResourceKey.of("test", name), player)).getType() == player;
    }

    private boolean nativeCompleterIsNotForwarded() {
        final ArgumentType<String> greedy = StringArgumentType.greedyString();
        return this.node(name -> ClientNativeArgumentParser.createIdentity(ResourceKey.of("test", name), greedy)).getCustomSuggestions() == null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SpongeArgumentCommandNode<?> node(final java.util.function.Function<String, ClientNativeArgumentParser> parserFactory) {
        final String name = "argument";
        final SpongeParameterKey key = new SpongeParameterKey<>(name, Object.class);
        final ClientNativeArgumentParser parser = parserFactory.apply(name);
        return new SpongeArgumentCommandNodeBuilder(key, parser, parser, null, usage -> usage, name, false).build();
    }
}
