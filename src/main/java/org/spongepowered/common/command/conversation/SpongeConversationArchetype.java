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
package org.spongepowered.common.command.conversation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.conversation.Conversant;
import org.spongepowered.api.command.conversation.Conversation;
import org.spongepowered.api.command.conversation.ConversationArchetype;
import org.spongepowered.api.command.conversation.EndingHandler;
import org.spongepowered.api.command.conversation.ExternalChatHandler;
import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.service.pagination.PaginationCalculator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpongeConversationArchetype implements ConversationArchetype {

    private final Question question;
    private Set<EndingHandler> endingHandlers = new HashSet<>();
    private final ExternalChatHandler defaultChatHandler;
    private final String id;
    private final String exit;
    private final Optional<Text> banner;
    private final Optional<Text> title;
    private final Optional<Text> padding;
    private final Optional<Text> header;
    private final Optional<Text> startingMessage;
    private final Text commandUsageMessage;
    private boolean catchesOutput = true;
    private boolean allowCommands = false;

    /**
     * Creates the conversation archetype. Generally should only be called from
     * {@link SpongeConversationArchetypeBuilder}.
     *
     * @param firstQuestion The first question
     * @param catchesOutput Whether or not to catch conversant output
     * @param defaultHandler The default handler applied to conversants
     * @param endingHandlers The ending handlers for the conversation
     * @param startingMessage The message sent to conversants at the start of
     *     the conversation
     * @param id The id of the archetype
     * @param exit The exit keyword
     * @param title The title of the conversation
     * @param padding The padding for the title
     * @param header The header for after the banner
     */
    SpongeConversationArchetype(Question firstQuestion, boolean catchesOutput, boolean allowCommands, ExternalChatHandler defaultHandler,
            Set<EndingHandler> endingHandlers, @Nullable Text startingMessage, String id, String exit,
            @Nullable Text title, @Nullable Text padding, @Nullable Text header, Text commandUsageMessage) {
        this.question = firstQuestion;
        this.endingHandlers = endingHandlers;
        this.catchesOutput = catchesOutput;
        this.allowCommands = allowCommands;
        this.defaultChatHandler = defaultHandler;
        this.startingMessage = Optional.ofNullable(startingMessage);
        this.id = id.toLowerCase();
        this.exit = exit.toLowerCase();
        this.title = Optional.ofNullable(title);
        if (title != null && padding != null) {
            this.padding = Optional.of(padding);
            this.banner = Optional.of(new PaginationCalculator(10).center(title, padding));
        } else if (title != null) {
            this.padding = Optional.of(Text.of("="));
            this.banner = Optional.of(new PaginationCalculator(10).center(title, Text.of("=")));
        } else {
            this.padding = Optional.empty();
            this.banner = Optional.empty();
        }
        this.header = Optional.ofNullable(header);
        this.commandUsageMessage = commandUsageMessage;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<Text> getStartingMessage() {
        return this.startingMessage;
    }

    @Override
    public String getExitString() {
        return this.exit;
    }

    @Override
    public Question getFirstQuestion() {
        return this.question;
    }

    @Override
    public boolean catchesOutput() {
        return this.catchesOutput;
    }

    @Override
    public boolean allowsCommands() {
        return this.allowCommands;
    }

    @Override
    public ExternalChatHandler getDefaultChatHandler() {
        return this.defaultChatHandler;
    }

    @Override
    public ImmutableSet<EndingHandler> getEndingHandlers() {
        return ImmutableSet.copyOf(this.endingHandlers);
    }

    @Override
    public Optional<Text> getBanner() {
        return this.banner;
    }

    @Override
    public Optional<Text> getHeader() {
        return this.header;
    }

    @Override
    public Optional<Text> getTitle() {
        return this.title;
    }

    @Override
    public Optional<Text> getPadding() {
        return this.padding;
    }

    @Override
    public Text getNoCommandUsageMessage() {
        return this.commandUsageMessage;
    }

    @Override
    public Optional<Conversation> start(Object plugin, Conversant... conversants) {
        PluginContainer pluginContainer = Sponge.getPluginManager().fromInstance(checkNotNull(plugin, "Plugin object cannot be null!"))
                .orElseThrow(() -> new IllegalArgumentException("The provided plugin object is not a proper plugin instance!"));
        return Sponge.getConversationManager().start(this, pluginContainer, conversants);
    }

    @Override
    public Builder toBuilder() {
        return ConversationArchetype.builder().from(this);
    }

}
