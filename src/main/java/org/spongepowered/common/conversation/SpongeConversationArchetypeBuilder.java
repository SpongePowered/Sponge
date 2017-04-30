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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.conversation.ConversationArchetype;
import org.spongepowered.api.conversation.ConversationArchetype.Builder;
import org.spongepowered.api.conversation.EndingHandler;
import org.spongepowered.api.conversation.ExternalChatHandler;
import org.spongepowered.api.conversation.ExternalChatHandlers;
import org.spongepowered.api.conversation.Question;
import org.spongepowered.api.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeConversationArchetypeBuilder implements ConversationArchetype.Builder {

    @Nullable private String id;
    private String exit = "exit";
    @Nullable private Question firstQuestion;
    private Set<EndingHandler> endingHandlers = new HashSet<>();
    private ExternalChatHandler defaultHandler = ExternalChatHandlers.deleteAll();
    @Nullable private Text startingMessage;
    @Nullable private Text title;
    @Nullable private Text padding;
    private boolean catchesOutput = true;
    private boolean allowCommands = false;

    @Override
    public Builder from(ConversationArchetype value) {
        this.id = value.getId();
        this.exit = value.getExitString();
        this.firstQuestion = value.getFirstQuestion();
        this.endingHandlers = value.getEndingHandlers();
        this.defaultHandler = value.getDefaultChatHandler();
        this.catchesOutput = value.catchesOutput();
        this.startingMessage = value.getStartingMessage().orElse(null);
        this.title = value.getTitle().orElse(null);
        this.padding = value.getTitle().orElse(null);
        return this;
    }

    @Override
    public Builder reset() {
        this.id = null;
        this.exit = "exit";
        this.firstQuestion = null;
        this.endingHandlers.clear();
        this.defaultHandler = ExternalChatHandlers.deleteAll();
        this.startingMessage = null;
        this.title = null;
        this.padding = null;
        this.catchesOutput = true;
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
        this.exit = exit;
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
    public Builder endingHandlers(List<EndingHandler> endingHandlers) {
        this.endingHandlers.addAll(checkNotNull(endingHandlers, "The ending handlers list cannot be null."));
        return this;
    }

    @Override
    public Builder clearEndingHandlers() {
        this.endingHandlers.clear();
        return this;
    }

    @Override
    public Builder defaultChatHandler(ExternalChatHandler externalChatHandler) {
        this.defaultHandler = checkNotNull(externalChatHandler, "The external chat handler cannot be null!");
        return this;
    }

    @Override
    public Builder catchesOutput(boolean catches) {
        this.catchesOutput = checkNotNull(catches, "The catches output boolean cannot be null!");
        return this;
    }

    @Override
    public Builder allowCommands(boolean allow) {
        this.allowCommands = checkNotNull(allow, "The allow commands boolean cannot be null!");
        return this;
    }

    @Override
    public ConversationArchetype build() {
        checkNotNull(this.id, "You must specify an id for this archetype!");
        checkNotNull(this.firstQuestion, "You must specify a proper first question!");
        return new SpongeConversationArchetype(this.firstQuestion, this.catchesOutput, this.allowCommands, this.defaultHandler,
            this.endingHandlers, this.startingMessage, this.id, this.exit, this.title, this.padding);
    }
}
