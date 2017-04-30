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
package org.spongepowered.common.conversation;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.conversation.Conversant;
import org.spongepowered.api.conversation.Conversation;
import org.spongepowered.api.conversation.ConversationArchetype;
import org.spongepowered.api.conversation.EndingHandler;
import org.spongepowered.api.conversation.ExternalChatHandler;
import org.spongepowered.api.conversation.Question;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.service.pagination.PaginationCalculator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpongeConversationArchetype implements ConversationArchetype {

    private final Question question;
    private Set<EndingHandler> endingHandlers = new HashSet<>();
    private final ExternalChatHandler defaultChatHandler;
    private final String id;
    private final String exit;
    @Nullable private final Text header;
    @Nullable private final Text title;
    @Nullable private final Text padding;
    @Nullable private final Text startingMessage;
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
     */
    SpongeConversationArchetype(Question firstQuestion, boolean catchesOutput, boolean allowCommands, ExternalChatHandler defaultHandler,
        Set<EndingHandler> endingHandlers, @Nullable Text startingMessage, String id, String exit, @Nullable Text title, @Nullable Text padding) {
        this.question = firstQuestion;
        this.endingHandlers = endingHandlers;
        this.catchesOutput = catchesOutput;
        this.allowCommands = allowCommands;
        this.defaultChatHandler = defaultHandler;
        this.startingMessage = startingMessage;
        this.id = id.toLowerCase();
        this.exit = exit.toLowerCase();
        this.title = title;
        this.padding = padding;
        if (title != null && padding != null) {
            this.header = new PaginationCalculator(10).center(title, padding);
        } else if (title != null) {
            this.header = new PaginationCalculator(10).center(title, Text.of("="));
        } else {
            this.header = null;
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<Text> getStartingMessage() {
        return Optional.ofNullable(this.startingMessage);
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
    public Optional<Text> getHeader() {
        return Optional.ofNullable(this.header);
    }

    @Override
    public Optional<Text> getTitle() {
        return Optional.ofNullable(this.title);
    }

    @Override
    public Optional<Text> getPadding() {
        return Optional.ofNullable(this.padding);
    }

    @Override
    public Optional<Conversation> start(PluginContainer plugin, Conversant... conversants) {
        return Sponge.getConversationManager().start(this, plugin, conversants);
    }

    @Override
    public Builder toBuilder() {
        return ConversationArchetype.builder().from(this);
    }
}
