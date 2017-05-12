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

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.conversation.AnswerHandler;
import org.spongepowered.api.command.conversation.PromptHandler;
import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.command.conversation.Question.Builder;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

public class SpongeQuestionBuilder implements Question.Builder {

    @Nullable private String id;
    @Nullable private PromptHandler promptHandler;
    @Nullable private AnswerHandler answerHandler;
    @Nullable private InputTokenizer inputTokenizer;
    @Nullable private CommandElement arguments;
    
    @Override
    public Builder from(Question value) {
        this.id = value.getId();
        this.promptHandler = value.getPromptHandler();
        this.answerHandler = value.getHandler();
        this.arguments = value.getArguments();
        this.inputTokenizer = value.getInputTokenizer();
        return this;
    }

    @Override
    public Builder reset() {
        this.id = null;
        this.promptHandler = null;
        this.answerHandler = null;
        this.inputTokenizer = null;
        this.arguments = null;
        return this;
    }

    @Override
    public Builder id(String id) {
        this.id = checkNotNull(id, "The id for this question cannot be null!");
        return this;
    }

    @Override
    public Builder prompt(PromptHandler promptHandler) {
        this.promptHandler = checkNotNull(promptHandler, "The prompt handler cannot be null!");
        return this;
    }

    @Override
    public Builder handler(AnswerHandler answerHandler) {
        this.answerHandler = checkNotNull(answerHandler, "The answer handler for this question cannot be null!");
        return this;
    }

    @Override
    public Builder argument(CommandElement element) {
        this.arguments = checkNotNull(element, "The argument you specify cannot be null!");
        return this;
    }

    @Override
    public Builder arguments(CommandElement... elements) {
        this.arguments = GenericArguments.seq(checkNotNull(elements, "The arguments you specify cannot be null!"));
        return this;
    }

    @Override
    public Builder inputTokenizer(InputTokenizer inputTokenizer) {
        this.inputTokenizer = checkNotNull(inputTokenizer, "The input tokenizer cannot be null!");
        return this;
    }

    @Override
    public Question build() {
        checkNotNull(this.id, "The id for this question cannot be null!");
        checkNotNull(this.promptHandler, "The prompt handler cannot be null!");
        checkNotNull(this.answerHandler, "The answer handler for this question cannot be null!");
        if (this.arguments == null) {
            this.arguments = GenericArguments.remainingJoinedStrings(Text.of("answer"));
        }
        if (this.inputTokenizer == null) {
            this.inputTokenizer = InputTokenizer.quotedStrings(false);
        }
        return new SpongeQuestion(this.id, this.promptHandler, this.answerHandler, this.arguments, this.inputTokenizer);
    }

}
