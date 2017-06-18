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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedSelectorParser;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedSelectorParsers;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SpongeParameterBuilder implements Parameter.Builder {

    @Nullable private Text key;
    @Nullable private ValueParameter valueParameter;
    @Nullable private ValueParser parser;
    @Nullable private ValueCompleter completer;
    @Nullable private ValueUsage usage;
    private final List<ValueParameterModifier> modifiers = Lists.newArrayList();
    @Nullable private String permission;
    @Nullable private SelectorParser selectorParser;

    @Override
    public Parameter.Builder setKey(Text key) {
        Preconditions.checkNotNull(key);
        this.key = key;
        return this;
    }

    @Override
    public Parameter.Builder setParser(ValueParser parser) {
        Preconditions.checkNotNull(parser);
        if (parser instanceof ValueParameter) {
            this.valueParameter = (ValueParameter) parser;
            this.parser = null;
        } else {
            this.parser = parser;
            this.valueParameter = null;
        }
        return this;
    }

    @Override
    public Parameter.Builder setSuggestions(@Nullable ValueCompleter completer) {
        this.completer = completer;
        return this;
    }

    @Override
    public Parameter.Builder setUsage(@Nullable ValueUsage usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public Parameter.Builder modifier(ValueParameterModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    @Override
    public Parameter.Builder setSelectorParser(@Nullable SelectorParser selectorParser) {
        this.selectorParser = selectorParser;
        return this;
    }

    @Override
    public Parameter.Builder setRequiredPermission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public Parameter build() {
        Preconditions.checkState(this.key != null, "key must be set");
        Preconditions.checkState(this.valueParameter != null || this.parser != null, "Either PlayerValueParameter or onParse must be set");

        List<ValueParameterModifier> modifiersToUse = Lists.newArrayList(this.modifiers);
        if (this.permission != null) {
            modifiersToUse.add(0, new PermissionModifier(this.permission));
        }

        if (this.valueParameter != null && this.usage == null && this.completer == null) {
            return new SpongeParameter(this.key, modifiersToUse, this.valueParameter);
        }

        ValueParser parserToUse;
        ValueCompleter completerToUse;
        ValueUsage usageToUse;
        SelectorParser selectorParser;

        if (this.valueParameter != null) {
            parserToUse = this.valueParameter;
            completerToUse = this.completer == null ? this.valueParameter : this.completer;
            usageToUse = this.usage == null ? this.valueParameter : this.usage;
            selectorParser = this.selectorParser == null ? this.valueParameter : this.selectorParser;
        } else {
            parserToUse = this.parser;
            completerToUse = this.completer == null ? (cs, args, cec) -> new ArrayList<>() : this.completer;
            usageToUse = this.usage == null ? (t, cs) -> t : this.usage;
            selectorParser = this.selectorParser == null ? CatalogedSelectorParsers.NONE : this.selectorParser;
        }

        return new SpongeParameter(this.key, modifiersToUse, new SpongeValueParameter(parserToUse, completerToUse, usageToUse, selectorParser));
    }

    @Override
    public Parameter.Builder from(Parameter value) {
        if (!(value instanceof SpongeParameter)) {
            throw new IllegalArgumentException("value must be a SpongeParameter");
        }
        reset();

        ((SpongeParameter) value).populateBuilder(this);
        return this;
    }

    @Override
    public Parameter.Builder reset() {
        this.key = null;
        this.valueParameter = null;
        this.parser = null;
        this.completer = null;
        this.usage = null;
        this.modifiers.clear();
        this.permission = null;
        this.selectorParser = null;
        return this;
    }

    private static class SpongeValueParameter implements ValueParameter {

        private final ValueParser parser;
        private final ValueCompleter completer;
        private final ValueUsage usage;
        private final SelectorParser selectorParser;

        private SpongeValueParameter(
                ValueParser parser,
                ValueCompleter completer,
                ValueUsage usage,
                SelectorParser selectorParser) {
            this.parser = parser;
            this.completer = completer;
            this.usage = usage;
            this.selectorParser = selectorParser;
        }

        @Override
        public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
            return this.parser.getValue(cause, args, context);
        }

        @Override
        public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
            return this.completer.complete(cause, args, context);
        }

        @Override
        public Text getUsage(Text key, Cause cause) {
            return this.usage.getUsage(key, cause);
        }

        @Override
        public Optional<?> parseSelector(Cause cause, String selector, CommandContext context, Function<Text, ArgumentParseException> errorFunction)
                throws ArgumentParseException {
            return this.selectorParser.parseSelector(cause, selector, context, errorFunction);
        }
    }

    private static class PermissionModifier implements ValueParameterModifier {

        private final String permission;

        private PermissionModifier(String permission) {
            this.permission = permission;
        }

        @Override
        public void onParse(Text key, Cause cause, CommandArgs args, CommandContext context, ParsingContext parsingContext)
                throws ArgumentParseException {
            if (context.getSubject().map(x -> x.hasPermission(this.permission)).orElse(true)) {
                parsingContext.next();
            }
        }

        @Override
        public List<String> complete(Cause cause, CommandArgs args, CommandContext context, List<String> currentCompletions)
                throws ArgumentParseException {
            return Lists.newArrayList();
        }

        @Override
        public Text getUsage(Text key, Cause cause, Text currentUsage) {
            if (getSubject(cause).map(x -> x.hasPermission(this.permission)).orElse(true)) {
                return currentUsage;
            }

            return Text.EMPTY;
        }

        private Optional<Subject> getSubject(Cause cause) {
            EventContext context = cause.getContext();
            if (context.containsKey(EventContextKeys.COMMAND_PERMISSION_SUBJECT)) {
                return context.get(EventContextKeys.COMMAND_PERMISSION_SUBJECT);
            }

            if (context.containsKey(EventContextKeys.COMMAND_ENTITY)) {
                return context.get(EventContextKeys.COMMAND_ENTITY).map(x -> (Subject) x);
            }

            return cause.first(Subject.class);
        }
    }

}
