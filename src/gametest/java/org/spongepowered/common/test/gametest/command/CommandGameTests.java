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

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.ParseContext;
import net.minecraft.commands.ServerPlayerSuggestionContext;
import net.minecraft.commands.SuggestionBox;
import net.minecraft.commands.arguments.ValidationWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vanilla parses commands, suggests for them and sends their tree through
 * boxes that hold a converted copy of the command tree. Sponge puts its own
 * dispatcher behind those boxes, so each way into them is exercised here:
 * a command run by a source, one typed by a player, suggestions, a parse with
 * no source at all (as for a function being compiled) and the tree packet.
 *
 * <p>Several tests put a player in the level, hence the environment.</p>
 */
@GameTestGroup(value = "command", environment = NoMobSpawning.class)
public final class CommandGameTests {

    private static final BlockPos POS = new BlockPos(0, 1, 0);

    @GameTest
    void sourceCommandRuns(final GameTestHelper helper) {
        final AtomicBoolean executed = new AtomicBoolean();
        final EventListener<ExecuteCommandEvent.Post> listener = event -> {
            if (event.command().equals("setblock")) {
                executed.set(true);
            }
        };
        SpongeGameTests.listen(ExecuteCommandEvent.Post.class, listener);

        final BlockPos target = helper.absolutePos(CommandGameTests.POS);
        final MinecraftServer server = helper.getLevel().getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withLevel(helper.getLevel()),
            "setblock " + target.getX() + " " + target.getY() + " " + target.getZ() + " minecraft:gold_block");

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, CommandGameTests.POS);
            helper.assertTrue(executed.get(), "ExecuteCommandEvent.Post was not thrown for setblock");
            Sponge.eventManager().unregisterListeners(listener);
        });
    }

    @GameTest
    void playerCommandRuns(final GameTestHelper helper) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        final AtomicBoolean executed = new AtomicBoolean();
        final EventListener<ExecuteCommandEvent.Post> listener = event -> {
            if (event.command().equals("help") && event.cause().contains(player)) {
                executed.set(true);
            }
        };
        SpongeGameTests.listen(ExecuteCommandEvent.Post.class, listener);

        player.connection.handleChatCommand(new ServerboundChatCommandPacket("help"));

        helper.succeedWhen(() -> {
            helper.assertTrue(executed.get(), "ExecuteCommandEvent.Post was not thrown for the player's help command");
            Sponge.eventManager().unregisterListeners(listener);
            helper.getLevel().getServer().getPlayerList().remove(player);
        });
    }

    @GameTest(maxTicks = 1)
    void suggestionsForPlayer(final GameTestHelper helper) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final SuggestionBox box = helper.getLevel().getServer().getCommands().suggestionBox();
            final List<String> commands = CommandGameTests.suggest(box, player, "hel");
            helper.assertTrue(commands.contains("help"), "No help command among the suggestions for 'hel': " + commands);
            // Asked of a vanilla argument, which reads the online players from its suggestion context
            final List<String> names = CommandGameTests.suggest(box, player, "msg ");
            helper.assertTrue(names.contains(player.getGameProfile().name()), "The player is not among the suggestions for 'msg ': " + names);
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    private static List<String> suggest(final SuggestionBox box, final ServerPlayer player, final String input) {
        return box.parse(new StringReader(input), new ServerPlayerSuggestionContext(player)).computeSuggestions().join()
            .getList().stream().map(Suggestion::getText).toList();
    }

    /** The item is looked up in the registries of the context it is parsed with, there being no source to ask. */
    @GameTest(maxTicks = 1)
    void parsesWithoutSource(final GameTestHelper helper) throws CommandSyntaxException {
        final MinecraftServer server = helper.getLevel().getServer();
        final ParseContext context = new ParseContext.Simple(
            PermissionSet.ALL_PERMISSIONS, server.reloadableRegistries().lookup(), helper.getLevel().enabledFeatures(), ValidationWrapper.Provider.IMMEDIATE);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            // What compiling a function parses as
            frame.pushCause(Sponge.server().serviceProvider().permissionService()
                .newSubjectReference(PermissionService.SUBJECTS_FUNCTION, "sponge:gametest").resolve().join());
            server.getCommands().parseBox().parse("give @s minecraft:gold_block", context).unpack();
        }
        helper.succeed();
    }

    /** Encoded and decoded as it is on the way to a client, which rebuilds its own tree from it. */
    @GameTest(maxTicks = 1)
    void commandTreeReachesClient(final GameTestHelper helper) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final ClientboundCommandsPacket packet = helper.getLevel().getServer().getCommands().suggestionBox().toPacket(player);
            final ByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            ClientboundCommandsPacket.STREAM_CODEC.encode(buffer, packet);
            final SuggestionBox client = SuggestionBox.fromPacket(ClientboundCommandsPacket.STREAM_CODEC.decode(buffer));

            final JsonObject commands = client.dumpReport().getAsJsonObject().getAsJsonObject("children");
            helper.assertTrue(commands != null && commands.has("help"), "No help command in the tree sent to the client: " + commands);
            helper.assertTrue(commands.has("minecraft:help"), "No namespaced help command in the tree sent to the client");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }
}
