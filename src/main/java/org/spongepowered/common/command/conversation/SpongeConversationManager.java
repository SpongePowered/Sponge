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
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.remainingRawJoinedStrings;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.conversation.Conversant;
import org.spongepowered.api.command.conversation.Conversation;
import org.spongepowered.api.command.conversation.ConversationArchetype;
import org.spongepowered.api.command.conversation.ConversationManager;
import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.command.conversation.QuestionResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.conversation.ConversationEndType;
import org.spongepowered.api.event.cause.conversation.ConversationEndTypes;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.conversation.ConversationOpenEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * An implementation of {@link ConversationManager} where all active conversations
 * are stored. Conversation creation and processing occurs here as well.
 */
@Singleton
public final class SpongeConversationManager implements ConversationManager {

    // Note this is synchronized
    private final Multimap<String, Conversation> conversations;
    private final Logger logger;

    /**
     * Creates the conversation manager, is injected with Guice.
     *
     * @param logger The logger for Sponge
     */
    @Inject
    public SpongeConversationManager(Logger logger) {
        this.logger = logger;
        this.conversations = Multimaps.synchronizedMultimap(HashMultimap.create());
    }

    @Override
    public Collection<Conversation> getConversation(PluginContainer plugin, String id) {
        checkNotNull(plugin, "The specified plugin container cannot be null!");
        checkNotNull(id, "The specified id cannot be null!");
        return this.conversations.get(plugin.getId() + ":" + id.toLowerCase());
    }

    @Override
    public Collection<Conversation> getConversations() {
        return Collections.unmodifiableCollection(this.conversations.values());
    }

    @Override
    public List<String> getSuggestions(Conversant conversant, String arguments, @Nullable Location<World> targetPosition) {
        final TabCompleteEvent.Conversation event = getInternalSuggestions(conversant, arguments, targetPosition);

        if (event != null && !Sponge.getEventManager().post(event)) {
            return ImmutableList.copyOf(event.getTabCompletions());
        }
        return ImmutableList.of();
    }

    @Nullable
    public TabCompleteEvent.Conversation getInternalSuggestions(Conversant conversant, String arguments, @Nullable Location<World> targetPosition) {
        Optional<Conversation> conversation = conversant.getConversation();
        if (!conversation.isPresent() || !conversation.get().getCurrentQuestion().isPresent()) {
            this.logger.warn("The conversant must be in a current conversation with a current question to get their tab completions");
            return null;
        }
        try {
            final Question question;
            question = conversation.get().getCurrentQuestion().get();
            CommandArgs args = new CommandArgs(arguments, question.getInputTokenizer().tokenize(arguments, true));
            CommandContext context = new CommandContext();
            if (targetPosition != null) {
                context.putArg(CommandContext.TARGET_BLOCK_ARG, targetPosition);
            }
            List<String> completions = question.getArguments().complete(conversant, args, context);
            return SpongeEventFactory.createTabCompleteEventConversation(Cause.source(conversant).build(), ImmutableList.copyOf(completions),
                    completions, conversation.get(), question, arguments, Optional.ofNullable(targetPosition), false);
        } catch (Exception e) {
            this.logger.error("There was a problem getting the tab completions for {}", conversant.getName(), e);
            return null;
        }
    }

    @Override
    public boolean process(Conversant conversant, String message) {
        checkNotNull(conversant, "The specified conversant cannot be null!");
        checkNotNull(message, "The specified message cannot be null!");
        final Optional<Conversation> optionalConversation = conversant.getConversation();
        if (optionalConversation.isPresent()) {
            final Conversation conversation = optionalConversation.get();
            if (conversation.getCancellingHandler().process(conversation, conversant, message, false)) {
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
                            .parse(conversant, new CommandArgs(message, question.getInputTokenizer().tokenize(message, false)), context);
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
                        this.logger.error("Failed to parse the arguments passed into {}'s conversation. Sending them the same question again.",
                                conversant.getName(), e);
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
        checkNotNull(plugin, "The specified plugin container cannot be null!");
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
        synchronized (this.conversations) {
            for (Conversation conversation : this.conversations.values()) {
                conversation.end(endType, cause);
            }
        }
    }

    public static CommandSpec getCommand() {
        return CommandSpec.builder()
                .description(Text.of("Allows you to add to a conversation with a command."))
                .arguments(optional(onlyOne(remainingRawJoinedStrings(Text.of("message")))))
                .executor((src, args) -> {
                    if (src instanceof Conversant) {
                        Conversant conversant = (Conversant) src;
                        if (!conversant.isConversing()) {
                            throw new CommandException(Text.of(TextColors.RED, "You must be in a conversation to use this command!"));
                        }
                        Optional<String> message = args.getOne("message");
                        if (message.isPresent() && !message.get().isEmpty()) {
                            Sponge.getConversationManager().process((Conversant) src, message.get());
                            return CommandResult.success();
                        } else {
                            throw new CommandException(Text.of(TextColors.RED, "You must specify a message for your conversation's current question."
                                    + "\nFor example: /conversation Spongie is cool"));
                        }
                    } else {
                        throw new CommandException(Text.of(TextColors.RED, "You must be a conversant to use this command."));
                    }
                })
                .build();
    }

}
