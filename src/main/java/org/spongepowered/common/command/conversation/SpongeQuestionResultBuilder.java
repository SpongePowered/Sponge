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

import org.spongepowered.api.command.conversation.Question;
import org.spongepowered.api.command.conversation.QuestionResult;
import org.spongepowered.api.command.conversation.QuestionResult.Builder;
import org.spongepowered.api.command.conversation.QuestionResult.QuestionResultType;

import javax.annotation.Nullable;

public class SpongeQuestionResultBuilder implements QuestionResult.Builder {

    @Nullable private QuestionResultType type;
    @Nullable private Question nextQuestion;

    @Override
    public Builder from(QuestionResult value) {
        this.type = value.getType();
        this.nextQuestion = value.getNextQuestion().orElse(null);
        return this;
    }

    @Override
    public Builder reset() {
        this.type = null;
        this.nextQuestion = null;
        return this;
    }

    @Override
    public Builder type(QuestionResultType type) {
        this.type = checkNotNull(type, "The question result type cannot be null!");
        return this;
    }

    @Override
    public Builder nextQuestion(Question question) {
        this.nextQuestion = checkNotNull(question, "The question you specify cannot be null!");
        this.type = QuestionResultType.NEXT;
        return this;
    }

    @Override
    public Builder next() {
        this.type = QuestionResultType.NEXT;
        return this;
    }

    @Override
    public Builder end() {
        this.type = QuestionResultType.END;
        return this;
    }

    @Override
    public Builder repeat() {
        this.type = QuestionResultType.REPEAT;
        return this;
    }

    @Override
    public QuestionResult build() {
        checkNotNull(this.type, "The question result type cannot be null!");
        if (this.type.equals(QuestionResultType.NEXT)) {
            checkNotNull(this.nextQuestion, "The next question cannot be null if your result type is set to NEXT!");
        }
        return new SpongeQuestionResult(this.type, this.nextQuestion);
    }

}
