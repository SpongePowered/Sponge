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

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.conversation.AnswerHandler;
import org.spongepowered.api.command.conversation.PromptHandler;
import org.spongepowered.api.command.conversation.Question;
public class SpongeQuestion implements Question {

    private final String id;
    private final PromptHandler promptHandler;
    private final AnswerHandler handler;
    private final CommandElement arguments;
    private final InputTokenizer inputTokenizer;

    /**
     * Creates a new {@link Question} to be used within a conversation.
     *
     * @param id The id of the question
     * @param promptHandler The prompt handler for the question
     * @param handler The answer handler
     * @param arguments The arguments of the question
     * @param inputTokenizer The input tokenizer to use for parsing
     */
    SpongeQuestion(String id, PromptHandler promptHandler, AnswerHandler handler, CommandElement arguments, InputTokenizer inputTokenizer) {
        this.id = id;
        this.promptHandler = promptHandler;
        this.handler = handler;
        this.arguments = arguments;
        this.inputTokenizer = inputTokenizer;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public AnswerHandler getHandler() {
        return this.handler;
    }

    @Override
    public PromptHandler getPromptHandler() {
        return this.promptHandler;
    }

    @Override
    public CommandElement getArguments() {
        return this.arguments;
    }

    @Override
    public InputTokenizer getInputTokenizer() {
        return this.inputTokenizer;
    }

    @Override
    public Builder toBuilder() {
        return Question.builder().from(this);
    }

}
