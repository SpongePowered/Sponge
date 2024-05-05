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
package org.spongepowered.test.chat;

import com.google.inject.Inject;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.ChatTypeTemplate;
import org.spongepowered.api.adventure.ChatTypes;
import org.spongepowered.api.adventure.ResolveOperations;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.data.GetValue;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Plugin("chattest")
public class ChatTest implements LoadableModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final BossBar INFO_BAR = BossBar.bossBar(Component.translatable("chattest.bars.info"), 1f, BossBar.Color.PINK,
                                                      BossBar.Overlay.PROGRESS);
    public static final ResourceKey INVERTED_CHAT_ORDER = ResourceKey.of("chattest", "inverted");

    private final Game game;
    private final PluginContainer container;
    private boolean barVisible;

    @Inject
    ChatTest(final Game game, final PluginContainer container) {
        this.game = game;
        this.container = container;
    }

    @Listener
    private void constructed(final ConstructPluginEvent event) {
        // Register localization keys
        final TranslationRegistry lang = TranslationRegistry.create(ResourceKey.of(this.container, "translations"));
        Arrays.asList(Locales.EN_US, new Locale("en", "UD")).forEach(it ->
                lang.registerAll(it, ResourceBundle.getBundle("org.spongepowered.test.chat.messages", it), false));
        GlobalTranslator.translator().addSource(lang);
    }

    @Listener
    private void onServerStarted(final StartedEngineEvent<Server> event)
    {
        // TODO register this earlier - static context?
        final ChatTypeTemplate template = ChatTypeTemplate.builder().translationKey("%s by <%s>")
                .addContent().addSender()
                .key(INVERTED_CHAT_ORDER)
                .build();
        Sponge.server().dataPackManager().save(template);
    }

    @Override
    public void enable(final CommandContext ctx) {
        this.game.eventManager().registerListeners(this.container, new Listeners());
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        // /togglebossbar
        event.register(this.container, Command.builder()
                .permission("chattest.togglebossbar")
                .executor(ctx -> {
                    if (this.barVisible) {
                        this.game.server().hideBossBar(ChatTest.INFO_BAR);
                    } else {
                        this.game.server().showBossBar(ChatTest.INFO_BAR);
                    }
                    this.barVisible = !this.barVisible;
                    return CommandResult.success();
                })
                .build(), "togglebossbar");

        event.register(this.container, Command.builder()
                      .permission("chattest.sendbook")
                      .executor(ctx -> {
                          ctx.cause().audience().openBook(Book.builder()
                                                                        .title(Component.text("A story"))
                                                                        .author(Component.text("You"))
                                                                        .pages(Component.translatable("chattest.book.1"),
                                                                               Component.translatable("chattest.book.2")));
                          return CommandResult.success();
                      }).build(), "sendbook");

        event.register(this.container, Command.builder()
                .permission("chattest.giveitem")
                .executor(ctx -> {
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to use this command!")));

                    final ItemStack itemStack = ItemStack.builder().itemType(ItemTypes.PAPER)
                            .add(Keys.CUSTOM_NAME, Component.translatable("chattest.item.name"))
                            .add(Keys.LORE, Collections.singletonList(Component.translatable("chattest.item.lore"))).build();

                    player.inventory().offer(itemStack);
                    return CommandResult.success();
                }).build(), "giveitem");

        final Parameter.Value<ServerPlayer> targetArg = Parameter.player().key("target").build();
        final Parameter.Value<Component> messageArg = Parameter.jsonText().key("message").build();

        event.register(this.container, Command.builder()
        .permission("chatttest.tell-resolve")
            .addParameters(targetArg, messageArg)
        .executor(ctx -> {
            final ServerPlayer target = ctx.requireOne(targetArg);
            final Component message = ctx.requireOne(messageArg);
            final Component resolvedMessage = SpongeComponents.resolve(message, ctx.cause(), target, ResolveOperations.CONTEXTUAL_COMPONENTS);
            target.sendMessage(ctx.cause().first(Identified.class).map(Identified::identity).orElse(Identity.nil()), resolvedMessage);
            return CommandResult.success();
        })
        .build(), "tellresolve");

        event.register(this.container, Command.builder().addParameters(messageArg).executor(ctx -> {
            ctx.sendMessage(Component.text("here it comes..."));
            if (ctx.cause().audience() instanceof final ServerPlayer player) {
                player.simulateChat(ctx.requireOne(messageArg), event.cause());
            }
            return CommandResult.success();
        }).build(), "simulatechat");
    }

    static class Listeners {

        @Listener
        private void onLogin(final ServerSideConnectionEvent.Join event) {
            event.player().sendMessage(Component.translatable("chattest.response"));
        }

        @Listener(order = Order.LAST)
        private void onChat(final PlayerChatEvent event, final @Root ServerPlayer player, @GetValue("HEALTH") final double health) {
            ChatTest.LOGGER.info(Component.translatable("chattest.response.chat",
                                                                              event.message(),
                                                                              player.require(Keys.DISPLAY_NAME)
                                                                                      .decorate(TextDecoration.BOLD)
                                                                                      .colorIfAbsent(NamedTextColor.AQUA))
                                                               .color(NamedTextColor.DARK_AQUA));
            ChatTest.LOGGER.info("Player has health of {}", health);

            if (event instanceof PlayerChatEvent.Decorate) {
                event.setMessage(event.message().color(NamedTextColor.GREEN));
            } else if (event instanceof final PlayerChatEvent.Submit submitEvent) {

                submitEvent.setChatType(ChatTypes.key(INVERTED_CHAT_ORDER));
                final Optional<Component> optPlayerName = event.player().flatMap(p -> p.get(Keys.DISPLAY_NAME));
                final TextComponent name = Component.text("Prefix", NamedTextColor.RED)
                        .append(Component.text(" | ", NamedTextColor.GOLD))
                        .append(optPlayerName.orElse(Component.text("N/A")).color(NamedTextColor.DARK_GREEN));
                submitEvent.setSender(name);
            }
        }
    }
}
