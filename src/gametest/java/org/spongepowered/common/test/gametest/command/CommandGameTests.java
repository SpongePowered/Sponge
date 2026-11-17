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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.CommandSourceStack;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
            final SuggestionBox client = CommandGameTests.clientBox(helper, player);

            final JsonObject commands = client.dumpReport().getAsJsonObject().getAsJsonObject("children");
            helper.assertTrue(commands != null && commands.has("help"), "No help command in the tree sent to the client: " + commands);
            helper.assertTrue(commands.has("minecraft:help"), "No namespaced help command in the tree sent to the client");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /**
     * Which parameters are sent with their choices and which leave the client to ask, for each way a parameter
     * with choices is built and each place it can sit in a command.
     */
    @GameTest(maxTicks = 1)
    void choicesSentToClient(final GameTestHelper helper) {
        CommandGameTests.registerChoices();
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final JsonObject subcommands = CommandGameTests.clientBox(helper, player).dumpReport().getAsJsonObject()
                .getAsJsonObject("children").getAsJsonObject(CommandGameTests.CHOICES).getAsJsonObject("children");
            // How each parameter of the subcommand is suggested for, in order of name rather than position
            final Map<String, List<String>> expected = Map.ofEntries(
                Map.entry("fixed", List.of("literal")),
                Map.entry("mode", List.of("literal")),
                Map.entry("exact", List.of("literal")),
                Map.entry("optional", List.of("literal")),
                Map.entry("flag", List.of("literal")),
                Map.entry("after", List.of("literal", "none")),
                Map.entry("before", List.of("literal", "none")),
                // Its choices are fixed, the alternative's are not
                Map.entry("either", List.of("ask_server", "literal")),
                // Each of these can suggest something other than its choices, too many of them, or one the client cannot match
                Map.entry("spaced", List.of("ask_server")),
                Map.entry("dynamic", List.of("ask_server")),
                Map.entry("many", List.of("ask_server")),
                Map.entry("completer", List.of("ask_server")),
                Map.entry("modified", List.of("ask_server")),
                Map.entry("parsers", List.of("ask_server"))
            );
            for (final Map.Entry<String, List<String>> subcommand : expected.entrySet()) {
                final JsonObject sent = subcommands.getAsJsonObject(subcommand.getKey());
                final List<String> kinds = CommandGameTests.arguments(sent).stream().map(CommandGameTests::suggestionKind).sorted().toList();
                helper.assertTrue(kinds.equals(subcommand.getValue()),
                    "'" + subcommand.getKey() + "' was sent to the client suggesting with " + kinds + ", not " + subcommand.getValue() + ": " + sent);
            }

            final Set<String> colours = CommandGameTests.literalValues(subcommands.getAsJsonObject("fixed"));
            helper.assertTrue(colours.equals(Set.of("red", "green")), "The wrong choices were sent to the client: " + colours);
            final Set<String> modes = CommandGameTests.literalValues(subcommands.getAsJsonObject("mode"));
            helper.assertTrue(modes.equals(Set.of("fast", "slow")), "The wrong enum choices were sent to the client: " + modes);
            final int most = CommandGameTests.literalValues(subcommands.getAsJsonObject("exact")).size();
            helper.assertTrue(most == 64, "Not every one of 64 choices was sent to the client: " + most);
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /** The box built from the packet has no server behind it, so what it suggests comes from the packet alone. */
    @GameTest(maxTicks = 1)
    void clientCompletesFixedChoices(final GameTestHelper helper) {
        CommandGameTests.registerChoices();
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final SuggestionBox client = CommandGameTests.clientBox(helper, player);
            final Map<String, Set<String>> expected = Map.of(
                "fixed ", Set.of("red", "green"),
                "fixed r", Set.of("red"),
                "mode ", Set.of("fast", "slow"),
                "mode S", Set.of("slow"),
                // Not what is already typed in full
                "exact choice6", Set.of("choice60", "choice61", "choice62", "choice63"),
                "optional g", Set.of("green"),
                "after 3 r", Set.of("red"),
                "before g", Set.of("green"),
                "flag --colour r", Set.of("red")
            );
            for (final Map.Entry<String, Set<String>> input : expected.entrySet()) {
                final List<String> suggestions = CommandGameTests.suggest(client, player, CommandGameTests.CHOICES + " " + input.getKey());
                helper.assertTrue(Set.copyOf(suggestions).equals(input.getValue()),
                    "The client completed '" + input.getKey() + "' on its own to " + suggestions + ", not " + input.getValue());
            }
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /** What the client is given the server still suggests when asked, as it does all the client is not given. */
    @GameTest(maxTicks = 1)
    void serverSuggestsChoices(final GameTestHelper helper) {
        CommandGameTests.registerChoices();
        final MinecraftServer server = helper.getLevel().getServer();
        final Map<String, String> expected = Map.ofEntries(
            Map.entry("fixed r", "red"),
            Map.entry("spaced li", "\"light blue\""),
            Map.entry("mode s", "slow"),
            Map.entry("dynamic a", "amber"),
            Map.entry("exact choice6", "choice63"),
            Map.entry("many choice6", "choice64"),
            Map.entry("completer t", "teal"),
            Map.entry("modified r", "red"),
            Map.entry("optional g", "green"),
            Map.entry("after 3 r", "red"),
            Map.entry("before g", "green"),
            Map.entry("either r", "red"),
            Map.entry("either a", "amber"),
            Map.entry("parsers b", "blue"),
            Map.entry("flag --colour r", "red")
        );
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final SuggestionBox box = server.getCommands().suggestionBox();
            for (final Map.Entry<String, String> input : expected.entrySet()) {
                final List<String> suggestions = CommandGameTests.suggest(box, player, CommandGameTests.CHOICES + " " + input.getKey());
                helper.assertTrue(suggestions.contains(input.getValue()),
                    "The server did not suggest " + input.getValue() + " to the player for '" + input.getKey() + "': " + suggestions);
            }
        } finally {
            server.getPlayerList().remove(player);
        }
        // Not a player, so not through a box
        for (final Map.Entry<String, String> input : expected.entrySet()) {
            final List<String> completions = Sponge.server().commandManager().complete(CommandGameTests.CHOICES + " " + input.getKey())
                .stream().map(CommandCompletion::completion).toList();
            helper.assertTrue(completions.contains(input.getValue()),
                "The server did not suggest " + input.getValue() + " to itself for '" + input.getKey() + "': " + completions);
        }
        helper.succeed();
    }

    /** Each command with a choice it takes, as it is completed, and with one it does not take. */
    @GameTest(maxTicks = 1)
    void choicesRun(final GameTestHelper helper) {
        CommandGameTests.registerChoices();
        final MinecraftServer server = helper.getLevel().getServer();
        // The choice the command ran with, or null where it must not run
        final String[][] expected = {
            {"fixed red", "red"},
            {"fixed purple", null},
            {"spaced \"light blue\"", "light blue"},
            {"spaced red", "red"},
            {"mode fast", "FAST"},
            {"mode FAST", "FAST"},
            {"mode medium", null},
            {"dynamic amber", "amber"},
            {"dynamic red", null},
            {"exact choice63", "choice63"},
            {"exact choice64", null},
            {"many choice64", "choice64"},
            {"completer red", "red"},
            {"completer teal", null},
            {"modified red", "red"},
            {"optional", "none"},
            {"optional green", "green"},
            {"after 3 red", "red"},
            {"after red", null},
            {"before green 3", "green"},
            {"either red", "red"},
            {"either amber", "amber"},
            {"either purple", null},
            {"parsers red", "red"},
            {"parsers blue", "blue"},
            {"flag", "none"},
            {"flag --colour red", "red"},
        };
        for (final String[] command : expected) {
            final String chosen = CommandGameTests.run(server.createCommandSourceStack(), CommandGameTests.CHOICES + " " + command[0]);
            helper.assertTrue(Objects.equals(chosen, command[1]), "'" + command[0] + "' ran with " + chosen + ", not " + command[1]);
        }

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            final String chosen = CommandGameTests.run(player.createCommandSourceStack(), CommandGameTests.CHOICES + " fixed green");
            helper.assertTrue("green".equals(chosen), "The player's command ran with " + chosen + ", not green");
        } finally {
            server.getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /** A command a player may not use is neither sent to them nor suggested for, while the server has all of it. */
    @GameTest(maxTicks = 1)
    void choicesHiddenFromPlayers(final GameTestHelper helper) {
        CommandGameTests.registerChoices();
        final MinecraftServer server = helper.getLevel().getServer();
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // The player is the cause of what they ask for, as when their packets are handled
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(player);
            final JsonObject commands = CommandGameTests.clientBox(helper, player).dumpReport().getAsJsonObject().getAsJsonObject("children");
            helper.assertTrue(commands.has(CommandGameTests.CHOICES), "The command any player may use was not sent to the client");
            helper.assertTrue(!commands.has(CommandGameTests.SERVER_ONLY), "A command the player may not use was sent to the client");

            final List<String> suggestions = CommandGameTests.suggest(server.getCommands().suggestionBox(), player, CommandGameTests.SERVER_ONLY + " r");
            helper.assertTrue(suggestions.isEmpty(), "Choices of a command the player may not use were suggested: " + suggestions);
            final String chosen = CommandGameTests.run(player.createCommandSourceStack(), CommandGameTests.SERVER_ONLY + " red");
            helper.assertTrue(chosen == null, "A command the player may not use ran with " + chosen);
        } finally {
            server.getPlayerList().remove(player);
        }

        final List<String> completions = Sponge.server().commandManager().complete(CommandGameTests.SERVER_ONLY + " r")
            .stream().map(CommandCompletion::completion).toList();
        helper.assertTrue(completions.contains("red"), "The server was not suggested the choices of its own command: " + completions);
        final String chosen = CommandGameTests.run(server.createCommandSourceStack(), CommandGameTests.SERVER_ONLY + " red");
        helper.assertTrue("red".equals(chosen), "The server's own command ran with " + chosen + ", not red");
        helper.succeed();
    }

    /** As a function is compiled: with no source, and no client to have completed anything. */
    @GameTest(maxTicks = 1)
    void functionsParseChoices(final GameTestHelper helper) throws CommandSyntaxException {
        CommandGameTests.registerChoices();
        final MinecraftServer server = helper.getLevel().getServer();
        final ParseContext context = new ParseContext.Simple(
            PermissionSet.ALL_PERMISSIONS, server.reloadableRegistries().lookup(), helper.getLevel().enabledFeatures(), ValidationWrapper.Provider.IMMEDIATE);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(Sponge.server().serviceProvider().permissionService()
                .newSubjectReference(PermissionService.SUBJECTS_FUNCTION, "sponge:gametest").resolve().join());
            for (final String line : List.of("fixed red", "spaced \"light blue\"", "mode fast", "dynamic amber", "after 3 red", "flag --colour red")) {
                server.getCommands().parseBox().parse(CommandGameTests.CHOICES + " " + line, context).unpack();
            }
            boolean parsed = true;
            try {
                server.getCommands().parseBox().parse(CommandGameTests.CHOICES + " fixed purple", context).unpack();
            } catch (final CommandSyntaxException e) {
                parsed = false;
            }
            helper.assertTrue(!parsed, "A function line with a choice the command does not take was parsed");
        }
        helper.succeed();
    }

    /**
     * A command, a subcommand, a parameter and a flag that each need a permission of their own: with none of them,
     * with each one alone and with all of them, a player is sent, suggested and may run only what they are permitted.
     */
    @GameTest(maxTicks = 1)
    void choicesFollowPermissions(final GameTestHelper helper) {
        CommandGameTests.registerChoices();
        final MinecraftServer server = helper.getLevel().getServer();
        final String guarded = CommandGameTests.GUARDED;
        final String permitted = CommandGameTests.PERMITTED;
        // What is looked at and how, what it shows without the permission in the last column and what it shows with it
        final String[][] expected = {
            {"sent", guarded, "absent", "[green, red]", CommandGameTests.COMMAND_PERMISSION},
            {"client", guarded + " r", "[]", "[red]", CommandGameTests.COMMAND_PERMISSION},
            {"server", guarded + " r", "[]", "[red]", CommandGameTests.COMMAND_PERMISSION},
            {"ran", guarded + " green", "null", "green", CommandGameTests.COMMAND_PERMISSION},

            {"sent", permitted + " open", "[green, red]", "[green, red]", null},
            {"ran", permitted + " open red", "red", "red", null},

            {"sent", permitted + " subcommand", "absent", "[green, red]", CommandGameTests.SUBCOMMAND_PERMISSION},
            {"client", permitted + " subcommand r", "[]", "[red]", CommandGameTests.SUBCOMMAND_PERMISSION},
            {"server", permitted + " subcommand r", "[]", "[red]", CommandGameTests.SUBCOMMAND_PERMISSION},
            {"ran", permitted + " subcommand red", "null", "red", CommandGameTests.SUBCOMMAND_PERMISSION},

            // The alternative needs no permission, and is there either way
            {"sent", permitted + " parameter", "[blue]", "[blue, green, red]", CommandGameTests.PARAMETER_PERMISSION},
            {"client", permitted + " parameter ", "[blue]", "[blue, green, red]", CommandGameTests.PARAMETER_PERMISSION},
            {"server", permitted + " parameter ", "[blue]", "[blue, green, red]", CommandGameTests.PARAMETER_PERMISSION},
            {"ran", permitted + " parameter red", "null", "red", CommandGameTests.PARAMETER_PERMISSION},
            {"ran", permitted + " parameter blue", "blue", "blue", null},

            {"sent", permitted + " flag", "[]", "[green, red]", CommandGameTests.FLAG_PERMISSION},
            {"client", permitted + " flag --colour g", "[]", "[green]", CommandGameTests.FLAG_PERMISSION},
            {"server", permitted + " flag --colour g", "[]", "[green]", CommandGameTests.FLAG_PERMISSION},
            {"ran", permitted + " flag --colour green", "null", "green", CommandGameTests.FLAG_PERMISSION},
            {"ran", permitted + " flag", "none", "none", null},
        };
        final List<Set<String>> grants = new ArrayList<>();
        grants.add(Set.of());
        CommandGameTests.PERMISSIONS.forEach(permission -> grants.add(Set.of(permission)));
        grants.add(Set.copyOf(CommandGameTests.PERMISSIONS));

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // The player is the cause of what they ask for, as when their packets are handled
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(player);
            for (final Set<String> granted : grants) {
                CommandGameTests.permit(player, granted);
                final SuggestionBox client = CommandGameTests.clientBox(helper, player);
                for (final String[] row : expected) {
                    final String seen = switch (row[0]) {
                        case "sent" -> CommandGameTests.sentChoices(client.dumpReport().getAsJsonObject(), row[1]);
                        case "client" -> CommandGameTests.suggest(client, player, row[1]).stream().sorted().toList().toString();
                        case "server" -> CommandGameTests.suggest(server.getCommands().suggestionBox(), player, row[1]).stream().sorted().toList().toString();
                        default -> String.valueOf(CommandGameTests.run(player.createCommandSourceStack(), row[1]));
                    };
                    final String wanted = row[4] == null || granted.contains(row[4]) ? row[3] : row[2];
                    helper.assertTrue(seen.equals(wanted),
                        "Permitted " + granted + ", '" + row[1] + "' " + row[0] + " " + seen + ", not " + wanted);
                }
            }
        } finally {
            CommandGameTests.permit(player, Set.of());
            server.getPlayerList().remove(player);
        }

        // Nothing is kept from the server itself
        for (final String[] row : expected) {
            if (row[0].equals("ran")) {
                final String chosen = String.valueOf(CommandGameTests.run(server.createCommandSourceStack(), row[1]));
                helper.assertTrue(chosen.equals(row[3]), "The server ran '" + row[1] + "' with " + chosen + ", not " + row[3]);
            }
        }
        helper.succeed();
    }

    private enum Mode { FAST, SLOW }

    private static final String PERMITTED = "gametestpermittedchoices";
    private static final String GUARDED = "gametestguardedchoices";
    private static final String COMMAND_PERMISSION = "sponge.gametest.choices.command";
    private static final String SUBCOMMAND_PERMISSION = "sponge.gametest.choices.subcommand";
    private static final String PARAMETER_PERMISSION = "sponge.gametest.choices.parameter";
    private static final String FLAG_PERMISSION = "sponge.gametest.choices.flag";
    private static final List<String> PERMISSIONS = List.of(
        CommandGameTests.COMMAND_PERMISSION, CommandGameTests.SUBCOMMAND_PERMISSION, CommandGameTests.PARAMETER_PERMISSION, CommandGameTests.FLAG_PERMISSION);

    /** Gives the player the permissions and denies them the rest of those the tests use, which left unset fall to their operator level. */
    private static void permit(final ServerPlayer player, final Set<String> granted) {
        for (final String permission : CommandGameTests.PERMISSIONS) {
            ((Subject) player).transientSubjectData()
                .setPermission(SubjectData.GLOBAL_CONTEXT, permission, granted.contains(permission) ? Tristate.TRUE : Tristate.FALSE).join();
        }
    }

    /** The choices sent with the parameters under the node the words lead to in a tree report, or that there is no such node. */
    private static String sentChoices(JsonObject node, final String path) {
        for (final String word : path.split(" ")) {
            node = node.has("children") ? node.getAsJsonObject("children").getAsJsonObject(word) : null;
            if (node == null) {
                return "absent";
            }
        }
        return CommandGameTests.arguments(node).stream()
            .filter(argument -> "literal".equals(CommandGameTests.suggestionKind(argument)))
            .flatMap(argument -> argument.getAsJsonObject("custom_suggestion").getAsJsonArray("values").asList().stream())
            .map(JsonElement::getAsString).sorted().toList().toString();
    }

    private static final String CHOICES = "gametestchoices";
    private static final String SERVER_ONLY = "gametestserverchoices";
    private static final AtomicReference<String> CHOSEN = new AtomicReference<>();
    private static boolean choicesRegistered;

    /** A subcommand for each way a parameter with choices is built or placed, registered once and left for the rest of the run. */
    private static void registerChoices() {
        if (CommandGameTests.choicesRegistered) {
            return;
        }
        CommandGameTests.choicesRegistered = true;
        final Parameter.Value<Integer> number = Parameter.integerNumber().key("number").build();
        final Parameter.Value<String> after = CommandGameTests.colour();
        final Parameter.Value<String> before = CommandGameTests.colour();
        final Parameter.Value<String> fixed = CommandGameTests.colour();
        final Parameter.Value<String> other = Parameter.choices(String.class, choice -> choice, () -> List.of("amber")).key("other").build();
        final Parameter.Value<String> flagged = CommandGameTests.colour();
        final Parameter.Value<String> serverOnly = CommandGameTests.colour();
        final Parameter.Value.Builder<String> parsers = Parameter.builder(String.class)
            .addParser(VariableValueParameters.staticChoicesBuilder(String.class).addChoice("red", "red").build())
            .addParser(VariableValueParameters.staticChoicesBuilder(String.class).addChoice("blue", "blue").build());

        final Command.Parameterized command = Command.builder()
            .addChild(CommandGameTests.choosing(Parameter.choices("red", "green")), "fixed")
            .addChild(CommandGameTests.choosing(Parameter.choices("red", "light blue")), "spaced")
            .addChild(CommandGameTests.choosing(Parameter.enumValue(Mode.class)), "mode")
            .addChild(CommandGameTests.choosing(Parameter.choices(String.class, choice -> choice, () -> List.of("amber"))), "dynamic")
            // As many as are sent to the client, and one more
            .addChild(CommandGameTests.choosing(Parameter.choices(CommandGameTests.numbered(64))), "exact")
            .addChild(CommandGameTests.choosing(Parameter.choices(CommandGameTests.numbered(65))), "many")
            .addChild(CommandGameTests.choosing(Parameter.choices("red").completer((context, input) -> List.of(CommandCompletion.of("teal")))), "completer")
            .addChild(CommandGameTests.choosing(Parameter.choices("red").modifier((key, reader, context, value) -> Optional.ofNullable(value))), "modified")
            .addChild(CommandGameTests.choosing(Parameter.choices("red", "green").optional()), "optional")
            .addChild(CommandGameTests.choosing(Parameter.seq(number, after), after), "after")
            .addChild(CommandGameTests.choosing(Parameter.seq(before, number), before), "before")
            .addChild(CommandGameTests.choosing(Parameter.firstOf(fixed, other), fixed, other), "either")
            .addChild(CommandGameTests.choosing(parsers), "parsers")
            .addChild(Command.builder().addFlag(Flag.of(flagged, "colour")).executor(CommandGameTests.reading(flagged)).build(), "flag")
            .build();
        final CommandRegistrar<Command.Parameterized> registrar = Sponge.server().commandManager().registrar(Command.Parameterized.class).orElseThrow();
        final PluginContainer plugin = Sponge.pluginManager().plugin("sponge_gametest").orElseThrow();
        registrar.register(plugin, command, CommandGameTests.CHOICES);

        final Parameter.Value<String> wholly = CommandGameTests.colour();
        final Parameter.Value<String> inSubcommand = CommandGameTests.colour();
        final Parameter.Value<String> needsPermission =
            Parameter.choices("red", "green").requiredPermission(CommandGameTests.PARAMETER_PERMISSION).key("choice").build();
        final Parameter.Value<String> needsNone = Parameter.choices("blue").key("other").build();
        final Parameter.Value<String> inFlag = CommandGameTests.colour();
        registrar.register(plugin, Command.builder().addParameter(wholly).permission(CommandGameTests.COMMAND_PERMISSION)
            .executor(CommandGameTests.reading(wholly)).build(), CommandGameTests.GUARDED);
        registrar.register(plugin, Command.builder()
            .addChild(CommandGameTests.choosing(Parameter.choices("red", "green")), "open")
            .addChild(Command.builder().addParameter(inSubcommand).permission(CommandGameTests.SUBCOMMAND_PERMISSION)
                .executor(CommandGameTests.reading(inSubcommand)).build(), "subcommand")
            .addChild(CommandGameTests.choosing(Parameter.firstOf(needsPermission, needsNone), needsPermission, needsNone), "parameter")
            .addChild(Command.builder()
                .addFlag(Flag.builder().alias("colour").setParameter(inFlag).setPermission(CommandGameTests.FLAG_PERMISSION).build())
                .executor(CommandGameTests.reading(inFlag)).build(), "flag")
            .build(), CommandGameTests.PERMITTED);
        registrar.register(plugin, Command.builder().addParameter(serverOnly).executor(CommandGameTests.reading(serverOnly))
            .executionRequirements(cause -> !(cause.root() instanceof Player)).build(), CommandGameTests.SERVER_ONLY);
    }

    private static Parameter.Value<String> colour() {
        return Parameter.choices("red", "green").key("choice").build();
    }

    private static String[] numbered(final int count) {
        return IntStream.range(0, count).mapToObj(i -> "choice" + i).toArray(String[]::new);
    }

    private static Command.Parameterized choosing(final Parameter.Value.Builder<?> choices) {
        final Parameter.Value<?> parameter = choices.key("choice").build();
        return CommandGameTests.choosing(parameter, parameter);
    }

    private static Command.Parameterized choosing(final Parameter parameters, final Parameter.Value<?>... read) {
        return Command.builder().addParameter(parameters).executor(CommandGameTests.reading(read)).build();
    }

    /** Records the first of the parameters to have a value, or that none had. */
    private static CommandExecutor reading(final Parameter.Value<?>... read) {
        return context -> {
            CommandGameTests.CHOSEN.set(Stream.of(read)
                .flatMap(parameter -> context.one(parameter).map(Object::toString).stream()).findFirst().orElse("none"));
            return CommandResult.success();
        };
    }

    /** What the command ran with, or null if it did not run. */
    private static @Nullable String run(final CommandSourceStack source, final String command) {
        CommandGameTests.CHOSEN.set(null);
        source.getServer().getCommands().performPrefixedCommand(source, command);
        return CommandGameTests.CHOSEN.get();
    }

    /** The tree as a client rebuilds it from the packet. */
    private static SuggestionBox clientBox(final GameTestHelper helper, final ServerPlayer player) {
        final ClientboundCommandsPacket packet = helper.getLevel().getServer().getCommands().suggestionBox().toPacket(player);
        final ByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundCommandsPacket.STREAM_CODEC.encode(buffer, packet);
        return SuggestionBox.fromPacket(ClientboundCommandsPacket.STREAM_CODEC.decode(buffer));
    }

    /** Every parameter under a node of the tree report. */
    private static List<JsonObject> arguments(final JsonObject node) {
        final List<JsonObject> arguments = new ArrayList<>();
        if ("argument".equals(node.get("type").getAsString())) {
            arguments.add(node);
        }
        if (node.has("children")) {
            node.getAsJsonObject("children").entrySet().forEach(child -> arguments.addAll(CommandGameTests.arguments(child.getValue().getAsJsonObject())));
        }
        return arguments;
    }

    /** What the client was told to suggest with for a parameter: literal, ask_server, or none if it was told nothing. */
    private static String suggestionKind(final JsonObject argument) {
        return argument.has("custom_suggestion")
            ? argument.getAsJsonObject("custom_suggestion").get("type").getAsString().replace("minecraft:", "") : "none";
    }

    /** The choices sent with the first parameter under a node that has any. */
    private static Set<String> literalValues(final JsonObject node) {
        return CommandGameTests.arguments(node).stream().filter(argument -> "literal".equals(CommandGameTests.suggestionKind(argument))).findFirst()
            .orElseThrow().getAsJsonObject("custom_suggestion").getAsJsonArray("values").asList().stream()
            .map(JsonElement::getAsString).collect(Collectors.toSet());
    }
}
