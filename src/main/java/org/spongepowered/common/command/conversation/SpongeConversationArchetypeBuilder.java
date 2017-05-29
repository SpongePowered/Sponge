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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.command.conversation.CancellingHandler;
import org.spongepowered.api.command.conversation.ConversationArchetype;
import org.spongepowered.api.command.conversation.ConversationArchetype.Builder;
import org.spongepowered.api.command.conversation.EndingHandler;
import org.spongepowered.api.command.conversation.ExternalChatHandlerType;
import org.spongepowered.api.command.conversation.ExternalChatHandlerTypes;
import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeConversationArchetypeBuilder implements ConversationArchetype.Builder {

    private static final Text defaultCommandUsageMethod = Text.of(TextColors.RED, "You must exit this conversation before you can use commands!");
    private static final CancellingHandler defaultCancellingHandler = (conversation, conversant, input, wasCommand) -> {
        if (wasCommand) {
            input = input.toLowerCase();
            if (input.equals("exit") || input.equals("quit") || input.equals("leave")) {
                return true;
            }
        }
        return false;
    };

    private Set<EndingHandler> endingHandlers = new HashSet<>();
    @Nullable private String id;
    @Nullable private Question firstQuestion;
    @Nullable private ExternalChatHandlerType defaultChatHandlerType;
    @Nullable private CancellingHandler cancellingHandler;
    @Nullable private Text startingMessage;
    @Nullable private Text title;
    @Nullable private Text padding;
    @Nullable private Text header;
    @Nullable private Text commandUsageMessage;
    private boolean catchesOutput = true;
    private boolean allowCommands = false;

    @Override
    public Builder from(ConversationArchetype value) {
        this.id = value.getId();
        this.firstQuestion = value.getFirstQuestion();
        this.endingHandlers = new HashSet<>(value.getEndingHandlers());
        this.defaultChatHandlerType = value.getDefaultChatHandlerType();
        this.cancellingHandler = value.getCancellingHandler();
        this.catchesOutput = value.catchesOutput();
        this.startingMessage = value.getStartingMessage().orElse(null);
        this.title = value.getTitle().orElse(null);
        this.padding = value.getTitle().orElse(null);
        this.header = value.getBanner().orElse(null);
        this.commandUsageMessage = value.getNoCommandUsageMessage();
        return this;
    }

    @Override
    public Builder reset() {
        this.endingHandlers.clear();
        this.id = null;
        this.firstQuestion = null;
        this.defaultChatHandlerType = null;
        this.startingMessage = null;
        this.title = null;
        this.padding = null;
        this.catchesOutput = true;
        this.commandUsageMessage = null;
        return this;
    }

    @Override
    public Builder id(String id) {
        checkNotNull(id, "The id you specify cannot be null!");
        checkArgument(!id.isEmpty(), "The id you specify cannot be empty.");
        this.id = id;
        return this;
    }

    @Override
    public Builder exitString(String exit) {
        checkNotNull(exit, "The exit string you specify cannot be null!");
        checkArgument(!exit.isEmpty(), "The exit string you specify cannot be empty.");
        return this;
    }

    @Override
    public Builder startingMessage(@Nullable Text startingMessage) {
        this.startingMessage = startingMessage;
        return this;
    }

    @Override
    public Builder padding(@Nullable Text padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public Builder title(@Nullable Text title) {
        this.title = title;
        return this;
    }

    @Override
    public Builder header(@Nullable Text header) {
        this.header = header;
        return this;
    }

    @Override
    public Builder firstQuestion(Question question) {
        this.firstQuestion = checkNotNull(question, "The first question cannot be null!");
        return this;
    }

    @Override
    public Builder endingHandler(EndingHandler endingHandler) {
        this.endingHandlers.add(checkNotNull(endingHandler, "The ending handler cannot be null!"));
        return this;
    }

    @Override
    public Builder endingHandlers(EndingHandler... endingHandlers) {
        this.endingHandlers.addAll(Arrays.asList(endingHandlers));
        return this;
    }

    @Override
    public Builder endingHandlers(Collection<EndingHandler> endingHandlers) {
        checkNotNull(endingHandlers, "The ending handlers collection cannot be null.");
        checkState(endingHandlers.size() > 0, "You must specify at least one ending handler.");
        this.endingHandlers.addAll(endingHandlers);
        return this;
    }

    @Override
    public Builder clearEndingHandlers() {
        this.endingHandlers.clear();
        return this;
    }

    @Override
    public Builder setCancellingHandler(CancellingHandler cancellingHandler) {
        this.cancellingHandler = checkNotNull(cancellingHandler, "The cancelling handler cannot be null!");
        return this;
    }

    @Override
    public Builder defaultChatHandlerType(ExternalChatHandlerType externalChatHandlerType) {
        this.defaultChatHandlerType = checkNotNull(externalChatHandlerType, "The external chat handler type cannot be null!");
        return this;
    }

    @Override
    public Builder catchesOutput(boolean catches) {
        this.catchesOutput = catches;
        return this;
    }

    @Override
    public Builder allowCommands(boolean allow) {
        this.allowCommands = allow;
        return this;
    }

    @Override
    public Builder noCommandUsageMessage(Text message) {
        this.commandUsageMessage = checkNotNull(message, "The no command usage method cannot be null!");
        return this;
    }

    @Override
    public ConversationArchetype build() {
        checkNotNull(this.id, "You must specify an id for this archetype!");
        checkNotNull(this.firstQuestion, "You must specify a proper first question!");
        if (this.commandUsageMessage == null) {
            this.commandUsageMessage = defaultCommandUsageMethod;
        }
        if (this.defaultChatHandlerType == null) {
            this.defaultChatHandlerType = ExternalChatHandlerTypes.DISCARD;
        }
        if (this.cancellingHandler == null) {
            this.cancellingHandler = defaultCancellingHandler;
        }
        return new SpongeConversationArchetype(this.firstQuestion, this.catchesOutput, this.allowCommands,
                this.defaultChatHandlerType, this.endingHandlers, this.startingMessage, this.id, this.title,
                this.padding, this.header, this.commandUsageMessage, this.cancellingHandler);
    }

}
