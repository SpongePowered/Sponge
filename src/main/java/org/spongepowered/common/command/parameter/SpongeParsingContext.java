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
package org.spongepowered.common.command.parameter;

import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ParsingContext;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ListIterator;
import java.util.Optional;

public class SpongeParsingContext implements ParsingContext {

    private final Text key;
    private final Cause cause;
    private final CommandArgs args;
    private final CommandContext context;
    private final ListIterator<ValueParameterModifier> modifierListIterator;
    private final ValueParameter valueParameter;

    SpongeParsingContext(Text key, Cause cause, CommandArgs args, CommandContext context,
            ListIterator<ValueParameterModifier> modifierListIterator, ValueParameter valueParameter) {
        this.key = key;
        this.cause = cause;
        this.args = args;
        this.context = context;
        this.modifierListIterator = modifierListIterator;
        this.valueParameter = valueParameter;
    }

    @Override
    public void next() throws ArgumentParseException {
        if (this.modifierListIterator.hasNext()) {
            try {
                this.modifierListIterator.next().onParse(this.key, this.cause, this.args, this.context, this);
            } finally {
                this.modifierListIterator.previous();
            }
        } else {
            if (this.args.hasNext() && this.args.peek().startsWith("@")) {
                CommandArgs.State argsState = this.args.getState();
                CommandContext.State contextState = this.context.getState();
                Optional<?> result = this.valueParameter.parseSelector(this.cause, this.args.next(), this.context, args::createError);
                if (result.isPresent()) {
                    this.context.putEntry(this.key, result.get());
                    return;
                }

                this.args.setState(argsState);
                this.context.setState(contextState);
            }
            this.context.putEntry(this.key, this.valueParameter.getValue(this.cause, this.args, this.context).orElseThrow(() ->
                    this.args.createError(Text.of(TextColors.RED, "Could not parse result for ", this.key))));
        }
    }
}
