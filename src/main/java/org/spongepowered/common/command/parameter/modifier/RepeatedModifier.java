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
package org.spongepowered.common.command.parameter.modifier;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.format.CommandMessageFormats;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.ParsingContext;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;

public class RepeatedModifier implements ValueParameterModifier {

    private final int numberOfRepetitions;

    public RepeatedModifier(int numberOfRepetitions) {
        Preconditions.checkArgument(numberOfRepetitions > 0, "The number of time to repeat an element must be positive.");
        this.numberOfRepetitions = numberOfRepetitions;
    }

    @Override
    public void onParse(Text key, Cause cause, CommandArgs args, CommandContext context, ParsingContext parsingContext)
            throws ArgumentParseException {
        for (int count = 0; count < this.numberOfRepetitions; count++) {
            parsingContext.next();
        }

    }

    @Override
    public Text getUsage(Text key, Cause cause, Text currentUsage) {
        Text.Builder repeatedBuilder = Text.builder();
        for (int i = 0; i < this.numberOfRepetitions; i++) {
            if (i > 0) {
                repeatedBuilder.append(CommandMessageFormatting.SPACE_TEXT);
            }

            repeatedBuilder.append(currentUsage);
        }

        return repeatedBuilder.build();
    }

    public int getNumberOfRepetitions() {
        return this.numberOfRepetitions;
    }
}
