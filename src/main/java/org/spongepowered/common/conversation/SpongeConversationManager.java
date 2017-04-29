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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.conversation.Conversant;
import org.spongepowered.api.conversation.Conversation;
import org.spongepowered.api.conversation.ConversationArchetype;
import org.spongepowered.api.conversation.ConversationManager;
import org.spongepowered.api.conversation.Question;
import org.spongepowered.api.conversation.QuestionResult;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.conversation.ConversationEndType;
import org.spongepowered.api.event.cause.conversation.ConversationEndTypes;
import org.spongepowered.api.event.conversation.ConversationOpenEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * An implementation of {@link ConversationManager} where all active conversations
 * are stored. Where processing and conversation creation occurs.
 */
@Singleton
public class SpongeConversationManager implements ConversationManager {

    private Multimap<String, Conversation> conversations = HashMultimap.create();
    private final Logger logger;

    @Inject
    public SpongeConversationManager(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Collection<Conversation> getConversation(PluginContainer plugin, String id) {
        return this.conversations.get(plugin.getId() + ":" + id.toLowerCase());
    }

    @Override
    public Collection<Conversation> getConversations() {
        return this.conversations.values();
    }

    @Override
    public boolean process(Conversant conversant, String message) {
        final Optional<Conversation> optionalConversation = conversant.getConversation();
        if (optionalConversation.isPresent()) {
            final Conversation conversation = optionalConversation.get();
            if (message.equalsIgnoreCase(conversation.getExitString())) {
                conversation.end(ConversationEndTypes.QUIT, Cause.of(NamedCause.source(conversant)));
            } else if (!conversation.hasEnded()) {
                final Optional<Question> optionalQuestion = conversation.getCurrentQuestion();
                if (!optionalQuestion.isPresent()) {
                    conversant.sendThroughMessage(Text.of("Your current conversation has no current question. Feel free to attempt to leave it."));
                } else {
                    final CommandContext context = new CommandContext();
                    final Question question = optionalQuestion.get();
                    try {
                        question.getArguments()
                            .parse(conversant, new CommandArgs(message, InputTokenizer.quotedStrings(false).tokenize(message, false)), context);
                        final QuestionResult questionResult = question.getHandler().handle(conversation, conversant, question, context);
                        switch (questionResult.getType()) {
                            case NEXT:
                                Optional<Question> nextQuestion = questionResult.getNextQuestion();
                                if (!nextQuestion.isPresent()) {
                                    this.logger.error(conversant.getName() + "'s next question was missing. Their conversation is ending.");
                                    conversant.sendThroughMessage(Text.of("Your next question is missing, ending conversation."));
                                    conversation.end(ConversationEndTypes.ERROR, Cause.of(NamedCause.source(conversation.getCreator())));
                                    return true;
                                }
                                conversation.setQuestion(nextQuestion.get());
                                break;
                            case REPEAT:
                                conversation.setQuestion(question);
                                break;
                            case END:
                                conversation.end(ConversationEndTypes.FINISHED, Cause.of(NamedCause.source(conversation.getCreator())));
                                break;
                            default:
                                // Should not occur
                                this.logger.error("For some reason the question result type did not match any.");
                                conversation.end(ConversationEndTypes.ERROR, Cause.of(NamedCause.source(SpongeImpl.getPlugin())));
                        }
                    } catch (ArgumentParseException e) {
                        conversant.sendThroughMessage(Text.of("Failed to parse your specified arguments. Restarting question."));
                        conversation.setQuestion(question);
                        this.logger.error(
                                "Failed to parse the arguments passed into {}'s conversation. Sending them the same question again.", conversant.getName(), e);
                        e.printStackTrace();
                    }
                }
            }
            return conversation.getArchetype().catchesOutput();
        }
        return false;
    }

    @Override
    public Optional<Conversation> start(ConversationArchetype archetype, PluginContainer plugin, Conversant... conversants) {
        final ConversationOpenEvent.Starting startingEvent = SpongeEventFactory
            .createConversationOpenEventStarting(Cause.of(NamedCause.source(plugin)), archetype, Sets.newHashSet(conversants));

        if (SpongeImpl.postEvent(startingEvent)) {
            return Optional.empty();
        }

        final Conversation conversation = new SpongeConversation(startingEvent.getArchetype(), startingEvent.getConversants(), plugin);

        for (Conversant c : startingEvent.getConversants()) {
            if (!c.isConversing()) {
                c.setConversation(conversation);
            } else {
                conversation.removeConversant(c);
            }
        }

        final String fullId = plugin.getId() + ":" + archetype.getId().toLowerCase();

        this.conversations.put(fullId, conversation);

        if (!conversation.getCurrentQuestion().isPresent()) {
            this.logger.error("A conversation with the id " + fullId + "'s first question was missing. The conversation is ending.");
            conversation.end(ConversationEndTypes.ERROR, Cause.of(NamedCause.source(conversation.getCreator())));
            for (Conversant conversant : conversation.getConversants()) {
                conversant.sendThroughMessage(Text.of("Your first question is missing, forcing the conversation to end."));
            }
            return Optional.empty();
        }

        final Optional<Text> startingMessage = conversation.getArchetype().getStartingMessage();

        // Send the initial message if one was set
        startingMessage.ifPresent(s -> conversation.getConversants().forEach(c -> c.sendThroughMessage(s)));

        conversation.setQuestion(archetype.getFirstQuestion());

        SpongeImpl.postEvent(SpongeEventFactory.createConversationOpenEventStarted(Cause.of(NamedCause.source(plugin)), conversation));

        return Optional.of(conversation);
    }

    @Override
    public void remove(Conversation conversation) {
        this.conversations.remove(conversation.getCreator().getId() + ":" + conversation.getId().toLowerCase(), conversation);
    }

    @Override
    public void endAll(ConversationEndType endType, Cause cause) {
        for (Conversation c : this.conversations.values()) {
            c.end(endType, cause);
        }
    }

}
