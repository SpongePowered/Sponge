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
package org.spongepowered.test.commandtest;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
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
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin("commandtest")
public final class CommandTestPlugin {

    private final PluginContainer plugin;
    private final Logger logger;

    @Inject
    public CommandTestPlugin(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerKey = Parameter.playerOrSource().setKey("player")
                .setUsage(key -> "[any player]")
                .build();
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(playerKey)
                        .setExecutor(context -> {
                            final ServerPlayer player = context.requireOne(playerKey);
                            this.logger.info(player.getName());
                            return CommandResult.success();
                        })
                        .build(),
                "getplayer");

        final Parameter.Value<String> playerParameterKey = Parameter.string().setKey("name").optional().build();
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(playerParameterKey)
                        .setExecutor(context -> {
                            final Optional<String> result = context.getOne(playerParameterKey);
                            final Collection<GameProfile> collection;
                            if (result.isPresent()) {
                                // check to see if the string matches
                                collection = Sponge.getGame().getServer().getUserManager()
                                        .streamOfMatches(result.get().toLowerCase(Locale.ROOT))
                                        .collect(Collectors.toList());
                            } else {
                                collection = Sponge.getGame().getServer().getUserManager()
                                        .streamAll()
                                        .collect(Collectors.toList());
                            }
                            collection.forEach(x -> this.logger.info(
                                    "GameProfile - UUID: {}, Name - {}", x.getUniqueId().toString(), x.getName().orElse("---")));
                            return CommandResult.success();
                        })
                        .build(),
                "checkuser"
        );

        final Parameter.Key<String> testKey = Parameter.key("testKey", TypeToken.of(String.class));
        final Parameter.Key<Component> requiredKey = Parameter.key("requiredKey", TypeToken.of(Component.class));
        event.register(
                this.plugin,
                Command.builder()
                        .flag(Flag.builder().alias("f").alias("flag").build())
                        .flag(Flag.builder().alias("t").alias("text").setParameter(Parameter.string().setKey(testKey).build()).build())
                        .parameter(Parameter.formattingCodeText().setKey(requiredKey).build())
                        .setExecutor(context -> {
                            context.sendMessage(Component.text(context.getFlagInvocationCount("flag")));
                            context.sendMessage(Component.text(context.getFlagInvocationCount("t")));
                            context.getAll(testKey).forEach(x -> context.sendMessage(Component.text(x)));
                            context.sendMessage(context.requireOne(requiredKey));
                            return CommandResult.success();
                        })
                        .build(),
                "flagtest"
        );

        event.register(
                this.plugin,
                Command.builder()
                        .setExecutor(x -> {
                            x.sendMessage(Component.text().content("Click Me")
                                    .clickEvent(SpongeComponents.executeCallback(ctx -> ctx.sendMessage(Component.text("Hello"))))
                                    .build()
                            );
                            return CommandResult.success();
                        }).build(),
                "testCallback"
        );

        event.register(
                this.plugin,
                Command.builder()
                        .setExecutor(x -> {
                            final Collection<Entity> collection = Selector.builder()
                                    .applySelectorType(SelectorTypes.ALL_ENTITIES.get())
                                    .entityType(EntityTypes.PLAYER.get(), false)
                                    .gameMode(GameModes.CREATIVE.get())
                                    .setLimit(1)
                                    .includeSelf()
                                    .build()
                                    .select(x.getCause());
                            for (final Entity entity : collection) {
                                x.sendMessage(Component.text(entity.toString()));
                            }
                            return CommandResult.success();
                        })
                        .build(),
                "testselector"
        );

        final Parameter.Key<ServerLocation> serverLocationKey = Parameter.key("serverLocation", TypeToken.of(ServerLocation.class));
        final Parameter.Value<ServerLocation> serverLocationParmeter = Parameter.location().setKey(serverLocationKey).build();
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(serverLocationParmeter)
                        .setExecutor(x -> {
                            x.sendMessage(Component.text(x.requireOne(serverLocationKey).toString()));
                            return CommandResult.success();
                        })
                        .build(),
                "testlocation"
        );

        final Parameter.Key<CatalogedValueParameter<?>> commandParameterKey =
                Parameter.key("valueParameter", new TypeToken<CatalogedValueParameter<?>>() {});
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(serverLocationParmeter)
                        .parameter(
                                Parameter.catalogedElement((Class<CatalogedValueParameter<?>>) (Class) CatalogedValueParameter.class)
                                        .setKey(commandParameterKey)
                                        .build())
                        .setExecutor(x -> {
                            x.sendMessage(Component.text(x.requireOne(serverLocationKey).toString()));
                            x.sendMessage(Component.text(x.requireOne(commandParameterKey).getKey().toString()));
                            return CommandResult.success();
                        })
                        .build(),
                "testcatalogcompletion"
        );

        final Parameter.Key<TestEnum> enumParameterKey = Parameter.key("enum", TypeToken.of(TestEnum.class));
        final Parameter.Key<String> stringKey = Parameter.key("stringKey", TypeToken.of(String.class));
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(Parameter.enumValue(TestEnum.class).orDefault(TestEnum.ONE).setKey(enumParameterKey).build())
                        .parameter(Parameter.string().setKey(stringKey).setSuggestions((context, currentInput) -> ImmutableList.of("bacon", "eggs", "spam")).build())
                        .setExecutor(x -> {
                            x.sendMessage(Component.text(x.requireOne(enumParameterKey).name()));
                            return CommandResult.success();
                        })
                        .build(),
                "testenum"
        );


        final Parameter.Key<ResourceKey> resourceKeyKey = Parameter.key("rk", TypeToken.of(ResourceKey.class));
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(Parameter.resourceKey().setKey(resourceKeyKey).build())
                        .setExecutor(x -> {
                            x.sendMessage(Component.text(x.requireOne(resourceKeyKey).getFormatted()));
                            return CommandResult.success();
                        })
                        .build(),
                "testrk"
        );

        final Parameter.Key<User> userKey = Parameter.key("user", TypeToken.of(User.class));
        event.register(
                this.plugin,
                Command.builder()
                    .parameter(Parameter.user().setKey(userKey).build())
                    .setExecutor(context -> {
                        context.sendMessage(Component.text(context.requireOne(userKey).getName()));
                        return CommandResult.success();
                    })
                    .build(),
                "getuser"
                );

        final Parameter.Key<String> stringLiteralKey = Parameter.key("literal", TypeToken.of(String.class));
        event.register(
                this.plugin,
                Command.builder()
                        .setExecutor(context -> {
                            context.sendMessage(Component.text("Collected literals: " + String.join(", ", context.getAll(stringLiteralKey))));
                            return CommandResult.success();
                        })
                        .setTerminal(true)
                        .parameter(
                                Parameter.firstOfBuilder(Parameter.literal(String.class, "1", "1").setKey(stringLiteralKey).build())
                                    .or(Parameter.literal(String.class, "2", "2").setKey(stringLiteralKey).build())
                                    .terminal()
                                    .build()
                        )
                        .parameter(Parameter.seqBuilder(Parameter.literal(String.class, "3", "3").setKey(stringLiteralKey).build())
                                .then(Parameter.literal(String.class, "4", "4").setKey(stringLiteralKey).build())
                                .terminal()
                                .build())
                        .parameter(Parameter.literal(String.class, "5", "5").optional().setKey(stringLiteralKey).build())
                        .parameter(Parameter.literal(String.class, "6", "6").setKey(stringLiteralKey).build())
                        .build(),
                "testnesting");


        event.register(this.plugin,
                Command.builder().setExecutor(context -> CommandResult.success())
                        .parameter(Parameter.remainingJoinedStrings().orDefault("default").setKey("defaulted").build())
                        .child(Command.builder().setExecutor(c -> CommandResult.success()).build(), "subcommand")
                        .build(),
                "testoptionaldefaultwithsubcommand");
    }

    @Listener
    public void onRegisterRawSpongeCommand(final RegisterCommandEvent<Command.Raw> event) {
        event.register(this.plugin, new RawCommandTest(), "rawcommandtest");
    }

    public enum TestEnum {
        ONE,
        TWO,
        THREE
    }
}
