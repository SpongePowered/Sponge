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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.conversation.Conversant;
import org.spongepowered.api.conversation.Conversation;
import org.spongepowered.api.conversation.ConversationArchetype;
import org.spongepowered.api.conversation.EndingHandler;
import org.spongepowered.api.conversation.ExternalChatHandler;
import org.spongepowered.api.conversation.Question;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.conversation.ConversationEndCause;
import org.spongepowered.api.event.cause.conversation.ConversationEndType;
import org.spongepowered.api.event.conversation.ConversationCloseEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

public class SpongeConversation implements Conversation {

    private final PluginContainer creator;
    private final ConversationArchetype archetype;

    private final Map<Conversant, ExternalChatHandler> externalChatHandlers = new ConcurrentHashMap<>();
    private final DataContainer context = new MemoryDataContainer();
    private final Set<EndingHandler> endingHandlers;
    @Nullable private Question currentQuestion;
    private boolean catchesOutput;
    private boolean allowCommands;

    private boolean ended;

    /**
     * Creates a new conversation.
     *
     * @param archetype The archetype for the question
     * @param conversants All conversants in the conversation
     * @param creator Who created the conversation
     */
    SpongeConversation(ConversationArchetype archetype, Collection<Conversant> conversants, PluginContainer creator) {
        this.archetype = archetype;
        conversants.forEach(c -> addConversant(c, archetype.getDefaultChatHandler()));
        this.creator = creator;
        this.endingHandlers = new HashSet<>(archetype.getEndingHandlers());
        this.catchesOutput = archetype.catchesOutput();
        this.allowCommands = archetype.allowsCommands();
        this.currentQuestion = archetype.getFirstQuestion();
    }

    @Override
    public DataContainer getContext() {
        return this.context;
    }

    @Override
    public PluginContainer getCreator() {
        return this.creator;
    }

    @Override
    public ConversationArchetype getArchetype() {
        return this.archetype;
    }

    @Override
    public Set<Conversant> getConversants() {
        return this.externalChatHandlers.keySet();
    }

    @Override
    public Optional<Question> getCurrentQuestion() {
        return Optional.ofNullable(this.currentQuestion);
    }

    @Override
    public void setQuestion(@Nullable Question question) {
        this.currentQuestion = question;
        if (question != null) {
            this.archetype.getHeader().ifPresent(text -> getConversants().forEach(c -> c.sendThroughMessage(text)));
            getConversants().forEach(c -> c.sendThroughMessage(question.getPrompt()));
        }
    }

    @Override
    public boolean end(ConversationEndType endType, Cause cause) {
        cause = cause.with(NamedCause.of(endType.getName(), ConversationEndCause.builder().type(endType).build()));
        final ConversationCloseEvent.Ending endingEvent = SpongeEventFactory.createConversationCloseEventEnding(cause, this);

        if (SpongeImpl.postEvent(endingEvent) || endingEvent.isCancelled()) {
            return false;
        }

        this.ended = true;

        for (EndingHandler e : this.endingHandlers) {
            e.handle(this, this.context, endType);
        }

        removeAllConversants();

        this.currentQuestion = null;

        Sponge.getConversationManager().remove(this);

        final ConversationCloseEvent.Ended endedEvent = SpongeEventFactory.createConversationCloseEventEnded(cause, this);
        SpongeImpl.postEvent(endedEvent);

        return true;
    }

    @Override
    public void addConversant(Conversant conversant, ExternalChatHandler externalChatHandler) {
        this.externalChatHandlers.put(checkNotNull(conversant, "The conversant you specify cannot be null!"),
            checkNotNull(externalChatHandler, "The external chat handler you specify for this conversant cannot be null!"));
    }

    @Override
    public void removeConversant(Conversant conversant) {
        this.externalChatHandlers.remove(conversant).finish(conversant);
        conversant.removeFromConversation();
    }

    @Override
    public void removeAllConversants() {
        this.externalChatHandlers.keySet().forEach(this::removeConversant);
    }

    @Override
    public void setChatHandler(Conversant conversant, ExternalChatHandler externalChatHandler) {
        checkNotNull(externalChatHandler, "The external chat handler you specify cannot be null!");
        if (this.externalChatHandlers.keySet().contains(conversant)) {
            this.externalChatHandlers.put(conversant, externalChatHandler);
        }
    }

    @Override
    public Optional<ExternalChatHandler> getChatHandler(Conversant conversant) {
        return Optional.ofNullable(this.externalChatHandlers.get(conversant));
    }

    @Override
    public boolean hasEnded() {
        return this.ended;
    }

    @Override
    public void catchOutput(boolean catches) {
        this.catchesOutput = catches;
    }

    @Override
    public boolean catchesOutput() {
        return this.catchesOutput;
    }

    @Override
    public void allowCommands(boolean allow) {
        // TODO Stop their commands if not allowed
        this.allowCommands = allow;
    }

    @Override
    public boolean allowsCommands() {
        return this.allowCommands;
    }

    @Override
    public Set<EndingHandler> getEndingHandlers() {
        return this.endingHandlers;
    }

    @Override
    public void addEndingHandler(EndingHandler endingHandler) {
        this.endingHandlers.add(checkNotNull(endingHandler, "The ending handler you add cannot be null!"));
    }

    @Override
    public boolean removeEndingHandler(EndingHandler endingHandler) {
        return this.endingHandlers.remove(endingHandler);
    }

}
