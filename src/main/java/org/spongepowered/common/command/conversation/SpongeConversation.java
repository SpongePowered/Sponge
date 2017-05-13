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

import com.google.common.collect.MapMaker;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.conversation.CancellingHandler;
import org.spongepowered.api.command.conversation.Conversant;
import org.spongepowered.api.command.conversation.Conversation;
import org.spongepowered.api.command.conversation.ConversationArchetype;
import org.spongepowered.api.command.conversation.EndingHandler;
import org.spongepowered.api.command.conversation.ExternalChatHandler;
import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.conversation.ConversationEndCause;
import org.spongepowered.api.event.cause.conversation.ConversationEndType;
import org.spongepowered.api.event.cause.conversation.ConversationEndTypes;
import org.spongepowered.api.event.conversation.ConversationCloseEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class SpongeConversation implements Conversation {

    private final PluginContainer creator;
    private final ConversationArchetype archetype;

    private final ConcurrentMap<Conversant, ExternalChatHandler> externalChatHandlers = new MapMaker().weakKeys().initialCapacity(8).makeMap();

    private final DataContainer context = DataContainer.createNew();
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
        this.creator = creator;
        this.endingHandlers = new HashSet<>(archetype.getEndingHandlers());
        this.catchesOutput = archetype.catchesOutput();
        this.allowCommands = archetype.allowsCommands();
        this.currentQuestion = archetype.getFirstQuestion();
        conversants.forEach(c -> this.externalChatHandlers.put(c, archetype.getDefaultChatHandler()));

        Task.builder()
                .async()
                .interval(5, TimeUnit.MINUTES)
                .execute(() -> {
                    if (this.externalChatHandlers.size() == 0) {
                        SpongeImpl.getLogger().error("A " + creator.getId() + ":" + getId().toLowerCase(Locale.ENGLISH) +
                                " conversation has ended due to being empty for an extended period of time.");
                    }
                    end(ConversationEndTypes.TIMED_OUT, SpongeImpl.getImplementationCause());
                })
                .submit(creator);
    }

    @Override
    public DataContainer getContext() {
        return this.context;
    }

    @Override
    public CancellingHandler getCancellingHandler() {
        return null;
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
            synchronized (this.externalChatHandlers) {
                final Set<Conversant> conversants = this.externalChatHandlers.keySet();
                this.archetype.getBanner().ifPresent(b -> conversants.forEach(c -> c.sendThroughMessage(b)));
                this.archetype.getHeader().ifPresent(h -> conversants.forEach(c -> c.sendThroughMessage(h)));
                conversants.forEach(c -> c.sendThroughMessage(question.getPromptHandler().handle(this, this.context)));
            }
        }
    }

    @Override
    public boolean end(ConversationEndType endType, Cause cause) {
        cause = cause.with(NamedCause.of(endType.getName(), ConversationEndCause.builder().type(endType).build()));
        final ConversationCloseEvent.Ending endingEvent = SpongeEventFactory.createConversationCloseEventEnding(cause, this);

        if (SpongeImpl.postEvent(endingEvent)) {
            return false;
        }

        this.ended = true;

        for (EndingHandler e : this.endingHandlers) {
            e.handle(this, this.context, endType);
        }

        if (!endType.equals(ConversationEndTypes.TIMED_OUT)) {
            removeAllConversants();
        }

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
        if (this.currentQuestion != null) {
            this.archetype.getBanner().ifPresent(conversant::sendThroughMessage);
            this.archetype.getHeader().ifPresent(conversant::sendThroughMessage);
            conversant.sendThroughMessage(this.currentQuestion.getPromptHandler().handle(this, this.context));
        }
    }

    @Override
    public void removeConversant(Conversant conversant) {
        checkNotNull(conversant, "The conversant you input to remove cannot be null!");
        synchronized (this.externalChatHandlers) {
            if (this.externalChatHandlers.containsKey(conversant)) {
                this.externalChatHandlers.remove(conversant).finish(conversant);
                conversant.removeFromConversation();
            }
        }
    }

    @Override
    public void removeAllConversants() {
        this.externalChatHandlers.keySet().forEach(this::removeConversant);
    }

    @Override
    public void setChatHandler(Conversant conversant, ExternalChatHandler externalChatHandler) {
        checkNotNull(externalChatHandler, "The external chat handler you specify cannot be null!");
        synchronized (this.externalChatHandlers) {
            if (!this.externalChatHandlers.containsKey(conversant)) {
                SpongeImpl.getLogger().error("A conversant must be in the conversation for you to modify their external chat handler!");
                return;
            }
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
