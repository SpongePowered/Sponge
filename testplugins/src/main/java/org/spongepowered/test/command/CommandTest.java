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
package org.spongepowered.test.command;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.operator.Operator;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.command.selector.SelectorTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin("commandtest")
public final class CommandTest {

    private final PluginContainer plugin;
    private final Logger logger;

    @Inject
    public CommandTest(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerKey = Parameter.player().key("player")
                .usage(key -> "[any player]")
                .build();
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(playerKey)
                        .executor(context -> {
                            final ServerPlayer player = context.one(playerKey)
                                    .orElse(context.cause().root() instanceof ServerPlayer ? ((ServerPlayer) context.cause().root()) : null);
                            this.logger.info(player.name());
                            return CommandResult.success();
                        })
                        .build(),
                "getplayer");

        final Parameter.Value<String> playerParameterKey = Parameter.string().key("name").optional().build();
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(playerParameterKey)
                        .executor(context -> {
                            final Optional<String> result = context.one(playerParameterKey);
                            final Collection<GameProfile> collection;
                            if (result.isPresent()) {
                                // check to see if the string matches
                                collection = Sponge.game().server().userManager()
                                        .streamOfMatches(result.get().toLowerCase(Locale.ROOT))
                                        .collect(Collectors.toList());
                            } else {
                                collection = Sponge.game().server().userManager()
                                        .streamAll()
                                        .collect(Collectors.toList());
                            }
                            collection.forEach(x -> this.logger.info(
                                    "GameProfile - UUID: {}, Name - {}", x.uniqueId().toString(), x.name().orElse("---")));
                            return CommandResult.success();
                        })
                        .build(),
                "checkuser"
        );

        final Parameter.Key<String> testKey = Parameter.key("testKey", String.class);
        final Parameter.Key<Component> requiredKey = Parameter.key("requiredKey", Component.class);
        event.register(
                this.plugin,
                Command.builder()
                        .addFlag(Flag.builder().alias("f").alias("flag").build())
                        .addFlag(Flag.builder().alias("t").alias("text").setParameter(Parameter.string().key(testKey).build()).build())
                        .addParameter(Parameter.formattingCodeText().key(requiredKey).build())
                        .executor(context -> {
                            context.sendMessage(Identity.nil(), Component.text(context.flagInvocationCount("flag")));
                            context.sendMessage(Identity.nil(), Component.text(context.flagInvocationCount("t")));
                            context.all(testKey).forEach(x -> context.sendMessage(Identity.nil(), Component.text(x)));
                            context.sendMessage(Identity.nil(), context.requireOne(requiredKey));
                            return CommandResult.success();
                        })
                        .build(),
                "flagtest"
        );

        event.register(
                this.plugin,
                Command.builder()
                        .addFlag(Flag.builder().alias("t").alias("text").setParameter(Parameter.string().key(testKey).build()).build())
                        .addParameter(Parameter.formattingCodeText().key(requiredKey).build())
                        .executor(context -> {
                            context.sendMessage(Identity.nil(), Component.text("optional_test"));
                            context.sendMessage(Identity.nil(), Component.text(context.flagInvocationCount("t")));
                            context.all(testKey).forEach(x -> context.sendMessage(Identity.nil(), Component.text(x)));
                            context.sendMessage(Identity.nil(), context.requireOne(requiredKey));
                            return CommandResult.success();
                        })
                        .build(),
                "optionalflagtest"
        );

        event.register(
                this.plugin,
                Command.builder()
                        .executor(x -> {
                            x.sendMessage(Identity.nil(), Component.text().content("Click Me")
                                    .clickEvent(SpongeComponents.executeCallback(ctx -> ctx.sendMessage(Identity.nil(), Component.text("Hello"))))
                                    .build()
                            );
                            return CommandResult.success();
                        }).build(),
                "testCallback"
        );

        event.register(
                this.plugin,
                Command.builder()
                        .executor(x -> {
                            final Collection<Entity> collection = Selector.builder()
                                    .applySelectorType(SelectorTypes.ALL_ENTITIES.get())
                                    .addEntityType(EntityTypes.PLAYER.get(), false)
                                    .addGameMode(GameModes.CREATIVE.get())
                                    .limit(1)
                                    .includeSelf()
                                    .build()
                                    .select(x.cause());
                            for (final Entity entity : collection) {
                                x.sendMessage(Identity.nil(), Component.text(entity.toString()));
                            }
                            return CommandResult.success();
                        })
                        .build(),
                "testselector"
        );

        final Parameter.Key<ServerLocation> serverLocationKey = Parameter.key("serverLocation", ServerLocation.class);
        final Parameter.Value<ServerLocation> serverLocationParameter = Parameter.location().key(serverLocationKey).build();
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(serverLocationParameter)
                        .executor(x -> {
                            x.sendMessage(Identity.nil(), Component.text(x.requireOne(serverLocationKey).toString()));
                            return CommandResult.success();
                        })
                        .build(),
                "testlocation"
        );

        final Parameter.Key<ValueParameter<?>> commandParameterKey =
                Parameter.key("valueParameter", new TypeToken<ValueParameter<?>>() {});
        final TypeToken<ValueParameter<?>> typeToken = new TypeToken<ValueParameter<?>>() {};
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(serverLocationParameter)
                        .addParameter(
                                Parameter.registryElement(
                                        typeToken,
                                        commandContext -> Sponge.game().registries(),
                                        RegistryTypes.REGISTRY_KEYED_VALUE_PARAMETER,
                                        "sponge"
                                ).key(commandParameterKey).build())
                        .executor(x -> {
                            x.sendMessage(Identity.nil(), Component.text(x.requireOne(serverLocationKey).toString()));
                            x.sendMessage(Identity.nil(), Component.text(x.requireOne(commandParameterKey).toString()));
                            return CommandResult.success();
                        })
                        .build(),
                "testcatalogcompletion"
        );

        final Parameter.Key<TestEnum> enumParameterKey = Parameter.key("enum", TestEnum.class);
        final Parameter.Key<String> stringKey = Parameter.key("stringKey", String.class);
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(Parameter.enumValue(TestEnum.class).key(enumParameterKey).build())
                        .addParameter(Parameter.string().key(stringKey)
                                .completer((context, currentInput) ->
                                        Arrays.asList("bacon", "eggs", "spam").stream().map(CommandCompletion::of).collect(Collectors.toList()))
                                .build())
                        .executor(x -> {
                            x.sendMessage(Identity.nil(), Component.text(x.one(enumParameterKey).orElse(TestEnum.ONE).name()));
                            return CommandResult.success();
                        })
                        .build(),
                "testenum"
        );


        final Parameter.Key<ResourceKey> resourceKeyKey = Parameter.key("rk", ResourceKey.class);
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(Parameter.resourceKey().key(resourceKeyKey).build())
                        .executor(x -> {
                            x.sendMessage(Identity.nil(), Component.text(x.requireOne(resourceKeyKey).formatted()));
                            return CommandResult.success();
                        })
                        .build(),
                "testrk"
        );

        final Parameter.Key<User> userKey = Parameter.key("user", User.class);
        event.register(
                this.plugin,
                Command.builder()
                    .addParameter(Parameter.user().key(userKey).build())
                    .executor(context -> {
                        context.sendMessage(Identity.nil(), Component.text(context.requireOne(userKey).name()));
                        return CommandResult.success();
                    })
                    .build(),
                "getuser"
                );

        final Parameter.Key<String> stringLiteralKey = Parameter.key("literal", String.class);
        event.register(
                this.plugin,
                Command.builder()
                        .executor(context -> {
                            context.sendMessage(Identity.nil(), Component.text("Collected literals: " + String.join(", ", context.all(stringLiteralKey))));
                            return CommandResult.success();
                        })
                        .terminal(true)
                        .addParameter(
                                Parameter.firstOfBuilder(Parameter.literal(String.class, "1", "1").key(stringLiteralKey).build())
                                    .or(Parameter.literal(String.class, "2", "2").key(stringLiteralKey).build())
                                    .terminal()
                                    .build()
                        )
                        .addParameter(Parameter.seqBuilder(Parameter.literal(String.class, "3", "3").key(stringLiteralKey).build())
                                .then(Parameter.literal(String.class, "4", "4").key(stringLiteralKey).build())
                                .terminal()
                                .build())
                        .addParameter(Parameter.literal(String.class, "5", "5").optional().key(stringLiteralKey).build())
                        .addParameter(Parameter.literal(String.class, "6", "6").key(stringLiteralKey).build())
                        .build(),
                "testnesting");

        final Parameter.Value<SystemSubject> systemSubjectValue = Parameter.builder(SystemSubject.class)
                .key("systemsubject")
                .addParser(VariableValueParameters.literalBuilder(SystemSubject.class).literal(Collections.singleton("-"))
                        .returnValue(Sponge::systemSubject).build())
                .build();
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(Parameter.firstOf(
                                systemSubjectValue,
                                CommonParameters.PLAYER
                        ))
                        .addParameter(Parameter.remainingJoinedStrings().key(stringKey).build())
                        .executor(context -> {
                            final Audience audience;
                            final String name;
                            if (context.hasAny(systemSubjectValue)) {
                                audience = context.requireOne(systemSubjectValue);
                                name = "Console";
                            } else {
                                final ServerPlayer player = context.requireOne(CommonParameters.PLAYER);
                                name = player.name();
                                audience = player;
                            }
                            final String message = context.requireOne(stringKey);
                            context.sendMessage(Identity.nil(), Component.text("To " + name + "> " + message));
                            final Object root = context.cause().root();
                            final Identity identity = root instanceof ServerPlayer ? ((ServerPlayer) root).identity() : Identity.nil();
                            audience.sendMessage(identity, Component.text("From " + name + "> " + message));
                            return CommandResult.success();
                        })
                        .build(),
                    "testmessage"
                );

        final Command.Builder builder = Command.builder();

        final ValueCompleter stringValueCompleter = (c, s) -> s.isEmpty() ?
                Collections.singletonList(CommandCompletion.of("x")) :
                Arrays.asList(s, s + "bar", "foo_" + s).stream().map(CommandCompletion::of).collect(Collectors.toList());
//        final ValueCompleter stringValueCompleter = null;

        final Parameter.Value<String> r_opt = Parameter.remainingJoinedStrings().key("r_def").optional().build();
        final Parameter.Value<String> r_req = Parameter.remainingJoinedStrings().key("r_req").completer(stringValueCompleter).build();
        final Parameter.Value<String> opt1 = Parameter.string().optional().key("opt1").build();
        final Parameter.Value<String> opt2 = Parameter.string().optional().key("opt2").build();
        final Parameter.Value<String> topt = Parameter.string().optional().key("topt").terminal().build();
        final Parameter.Value<String> req1 = Parameter.string().key("req1").completer(stringValueCompleter).build();
        final Parameter.Value<String> req2 = Parameter.string().key("req2").build();
        final Parameter.Value<Boolean> lit1 = Parameter.literal(Boolean.class, true, "lit1").key("lit1").build();
        final Parameter.Value<Boolean> lit2 = Parameter.literal(Boolean.class, true, "lit2").key("lit2").build();
        final Parameter optSeq_lit_req1 = Parameter.seqBuilder(lit1).then(req1).optional().build();
        final Parameter optSeq_lit_req2 = Parameter.seqBuilder(lit2).then(req2).optional().build();
        final Parameter seq_req_2 = Parameter.seqBuilder(req1).then(req2).build();
        final Parameter seq_opt_2 = Parameter.seqBuilder(opt1).then(opt2).build();
        final Parameter seq_opt_2_req = Parameter.seqBuilder(opt1).then(opt2).then(req1).build();

        // <req1>
        builder.addChild(Command.builder().executor(context -> CommandTest.printParameters(context, req1))
                        .addParameter(req1).build(),"required");

        // subcommand|<r_def>
        builder.addChild(Command.builder().executor(context -> CommandTest.printParameters(context, r_opt))
                        .addParameter(r_opt)
                        .addChild(Command.builder().executor(c -> CommandTest.printParameters(c, r_opt)).build(), "subcommand")
                        .build(),
                "optional_or_subcmd");


        // https://bugs.mojang.com/browse/MC-165562 usage does not show up after a space if there are no completions

        // [def1] <r_req>
        // TODO missing executed command when only providing a single value
        builder.addChild(Command.builder().executor(context -> CommandTest.printParameters(context, opt1, r_req))
                .addParameters(opt1, r_req).build(), "optional_r_required");

        // [opt1] [opt2] <r_req>
        // TODO missing executed command when only providing a single value
        builder.addChild(Command.builder().executor(context -> CommandTest.printParameters(context, opt1, opt2, r_req))
                .addParameters(opt1, opt2, r_req).build(), "optional_optional_required");

        // [opt1] [opt2]
        // TODO some redundancy in generated nodes because opt1 node can terminate early
        builder.addChild(Command.builder().executor(context -> CommandTest.printParameters(context, opt1, opt2))
                .addParameters(opt1, opt2).build(), "optional_optional");

        // [opt1] [literal <req1>]
        // TODO completion does not include req1 when opt1/literal is ambigous
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, opt1, lit1, req1))
                .addParameters(opt1, optSeq_lit_req1).build(), "optional_optsequence_literal_required");

        // [literal <req1>] [literal2 <req2>]
        // TODO sequences are not optional
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, lit1, req1, lit2, req2))
                .addParameters(optSeq_lit_req1, optSeq_lit_req2).build(), "opt_sequence_2_literal_required");

        // <<req1> <req2>>
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, req1, req2))
                .addParameters(seq_req_2).build(), "seq_required_required");

        // <[opt1] [opt2]>
        // TODO some redundancy in generated nodes because opt1 node can terminate early
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, opt1, opt2))
                .addParameters(seq_opt_2).build(), "seq_optional_optional");

        // <[opt1] [opt2] <req>>
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, opt1, opt2, req1))
                .addParameters(seq_opt_2_req).build(), "seq_optional_optional_required");

        // [opt1] <req> [opt2]
        // TODO some redundancy in generated nodes because req1 node can terminate early
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, opt1, req1, opt2))
                .addParameters(opt1, req1, opt2).build(), "optional_required_optional");

        // [opt1] [topt] !terminal
        // or
        // [opt1] [topt] <req1>
        builder.addChild(Command.builder().executor(c -> CommandTest.printParameters(c, opt1, topt, req1))
                .addParameters(opt1, topt, req1).build(), "optional_toptional_optional");

        event.register(this.plugin, builder.build(), "testcommand", "testcmd");

        // Adapted from https://github.com/SpongePowered/Sponge/issues/3238#issuecomment-750456173

        final Command.Parameterized firstSub = Command.builder()
                .addParameter(CommonParameters.BOOLEAN)
                .executor(c -> {
                    c.sendMessage(Identity.nil(), Component.text("first"));
                    return CommandResult.success();
                })
                .build();
        final Command.Parameterized secondSub = Command.builder()
                .addParameter(CommonParameters.BOOLEAN)
                .executor(c -> {
                    c.sendMessage(Identity.nil(), Component.text("second"));
                    return CommandResult.success();
                })
                .build();
        final Command.Parameterized parent = Command.builder()
                .executor(c -> {
                    c.sendMessage(Identity.nil(), Component.text("parent"));
                    return CommandResult.success();
                })
                .addParameters(CommonParameters.WORLD)
                .addParameters(Parameter.firstOf(
                        Parameter.subcommand(firstSub, "first"),
                        Parameter.subcommand(secondSub, "second")
                ))
                .terminal(true)
                .build();

        event.register(this.plugin, parent, "testterminal");

        // exceptions

        event.register(this.plugin, Command.builder()
                      .shortDescription(Component.text("test throwing execptions"))
                      .addChild(Command.builder()
                            .executor(ctx -> {
                                throw new CommandException(Component.text("Exit via exception"));
                            })
                            .build(), "exception")
                      .addChild(Command.builder()
                            .executor(ctx -> {
                                return CommandResult.error(Component.text("Exit via failed result"));
                            }).build(), "failedresult")
                      .build(), "testfailure");


        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(Parameter.enumValue(TestEnum.class).modifier(new ValueParameterModifier<TestEnum>() {
                            @Override
                            public @NotNull Optional<? extends TestEnum> modifyResult(final Parameter.@NotNull Key<? super TestEnum> parameterKey,
                                                                                      final ArgumentReader.@NotNull Immutable reader,
                                                                                      final CommandContext.@NotNull Builder context,
                                                                                      @Nullable final TestEnum value) throws ArgumentParseException {
                                if (value == TestEnum.THREE) {
                                    throw reader.createException(Component.text("Can't select three!"));
                                }
                                return Optional.ofNullable(value);
                            }

                            @Override
                            public List<CommandCompletion> modifyCompletion(@NotNull final CommandContext context,
                                                                            @NotNull final String currentInput,
                                                                            final List<CommandCompletion> completions) {
                                return completions.stream().filter(x -> !x.completion().equalsIgnoreCase(TestEnum.THREE.name())).collect(Collectors.toList());
                            }

                            @Override
                            public @Nullable Component modifyExceptionMessage(@Nullable final Component exceptionMessage) {
                                if (exceptionMessage == null) {
                                    return null;
                                }
                                return exceptionMessage.replaceText(builder -> {
                                    builder.match(", three").replacement("").once();
                                });
                            }
                        }).key(enumParameterKey).build())
                        .executor(x -> {
                            x.sendMessage(Identity.nil(), Component.text(x.one(enumParameterKey).orElse(TestEnum.ONE).name()));
                            return CommandResult.success();
                        })
                        .build(),
                "testenummodified"
        );

        final Parameter.Value<String> testString = Parameter.string().key("string").build();
        final Parameter.Value<Component> jsonTextParameter = Parameter.jsonText().key("text").build();
        event.register(
                this.plugin,
                Command.builder()
                    .addParameter(jsonTextParameter)
                    .addParameter(testString)
                    .executor(ctx -> {
                        ctx.sendMessage(Identity.nil(), ctx.requireOne(jsonTextParameter));
                        ctx.sendMessage(Identity.nil(), Component.text(ctx.requireOne(testString)));
                        return CommandResult.success();
                    })
                    .build(),
                "testcomponentjson"
        );

        final Parameter.Value<Integer> firstIntParameter = Parameter.integerNumber().key("int1").build();
        final Parameter.Value<Integer> secondIntParameter = Parameter.integerNumber().key("int2").build();
        final Parameter.Value<Operator> operatorParameter = Parameter.operator().key("operator").build();
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(firstIntParameter)
                        .addParameter(operatorParameter)
                        .addParameter(secondIntParameter)
                        .executor(ctx -> {
                            final int first = ctx.requireOne(firstIntParameter);
                            final int second = ctx.requireOne(secondIntParameter);
                            final Operator operator = ctx.requireOne(operatorParameter);
                            ctx.sendMessage(Identity.nil(), Component.text(first));
                            ctx.sendMessage(Identity.nil(),
                                    RegistryTypes.OPERATOR.get()
                                            .findValueKey(operator)
                                            .map(key -> Component.text(key.asString()))
                                            .orElse(Component.text("Not set"))
                            );
                            ctx.sendMessage(Identity.nil(), Component.text(second));
                            if (operator instanceof Operator.Simple) {
                                ctx.sendMessage(Identity.nil(), Component.text(((Operator.Simple) operator).apply(first, second)));
                            }
                            return CommandResult.success();
                        })
                        .build(),
                "testoperator");

        final Parameter.Value<Color> colorParameter = Parameter.color().key("color").build();
        event.register(
                this.plugin,
                Command.builder()
                        .addParameter(colorParameter)
                        .executor(ctx -> {
                            final Color color = ctx.requireOne(colorParameter);
                            final TextColor textColor = TextColor.color(color);
                            final String colorString = color.toString();
                            ctx.sendMessage(Identity.nil(), Component.text().color(textColor).content(colorString).build());
                            return CommandResult.success();
                        })
                        .build(),
                "textcolor"
        );
    }

    @Listener
    public void onRegisterRawSpongeCommand(final RegisterCommandEvent<Command.Raw> event) {
        event.register(this.plugin, new RawCommandTest(), "rawcommandtest");
        event.register(this.plugin, new ClientSuggestionsRawCommandTest(), "rawrecipescommandtest");
    }

    private static CommandResult printParameters(final CommandContext context, final Parameter.Value<?>... params) {
        for (final Parameter.Value<?> param : params) {
            final Object paramValue = context.one(param).map(Object::toString).orElse("missing");
            final String paramUsage = param.usage(context.cause());
            context.sendMessage(Identity.nil(), Component.text(paramUsage + ": " + paramValue));
        }
        // TODO usage starts with "command" - comes from SpogneParameterizedCommand#getCachedDispatcher
        context.executedCommand().ifPresent(cmd -> context.sendMessage(Identity.nil(), cmd.usage(context.cause()).color(NamedTextColor.GRAY)));
        return CommandResult.success();
    }

    public enum TestEnum {
        ONE,
        TWO,
        THREE
    }
}
