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

import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;

import java.util.List;

class SpongeParameter implements Parameter {

    private final Text key;
    private final List<ValueParameterModifier> modifiers;
    private final ValueParameter valueParameter;

    public SpongeParameter(Text key, List<ValueParameterModifier> modifiers, ValueParameter valueParameter) {
        this.key = key;
        this.modifiers = modifiers;
        this.valueParameter = valueParameter;
    }

    public Text getKey() {
        return this.key;
    }

    @Override
    public void parse(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        new SpongeParsingContext(this.key, cause, args, context, this.modifiers.listIterator(), this.valueParameter).next();
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        List<String> completions = this.valueParameter.complete(cause, args, context);
        for (ValueParameterModifier modifier : this.modifiers) {
            completions = modifier.complete(cause, args, context, completions);
        }

        return completions;
    }

    @Override
    public Text getUsage(Cause cause) {
        Text usage = Text.of(
                CommandMessageFormatting.LT_TEXT,
                this.valueParameter.getUsage(this.key, cause),
                CommandMessageFormatting.GT_TEXT);
        for (ValueParameterModifier modifier : this.modifiers) {
            usage = modifier.getUsage(this.key, cause, usage);
        }
        return usage;
    }

    void populateBuilder(SpongeParameterBuilder builder) {
        builder.modifiers(this.modifiers).setKey(this.key).setParser(this.valueParameter);
    }

}
