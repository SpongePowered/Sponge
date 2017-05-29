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

import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.command.conversation.QuestionResult;

import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpongeQuestionResult implements QuestionResult {

    private final QuestionResultType type;
    private final Optional<Question> nextQuestion;

    /**
     * Constructs a new {@link SpongeQuestionResult}.
     *
     * @param type The type of question result
     * @param nextQuestion The next question, if applicable
     */
    SpongeQuestionResult(QuestionResultType type, @Nullable Question nextQuestion) {
        this.type = type;
        this.nextQuestion = Optional.ofNullable(nextQuestion);
    }

    @Override
    public QuestionResultType getType() {
        return this.type;
    }

    @Override
    public Optional<Question> getNextQuestion() {
        return this.nextQuestion;
    }

    @Override
    public Builder toBuilder() {
        return QuestionResult.builder().from(this);
    }

}
