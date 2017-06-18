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
package org.spongepowered.common.command.parameter.flag;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.command.parameter.flag.UnknownFlagBehavior;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeFlags implements Flags {

    private final List<String> primaryFlags;
    private final Map<String, Parameter> flags;
    private final UnknownFlagBehavior shortUnknown;
    private final UnknownFlagBehavior longUnknown;
    private final boolean anchorFlags;

    SpongeFlags(List<String> primaryFlags, Map<String, Parameter> flags,
            UnknownFlagBehavior shortUnknown, UnknownFlagBehavior longUnknown, boolean anchorFlags) {
        this.primaryFlags = primaryFlags;
        this.flags = flags;
        this.shortUnknown = shortUnknown;
        this.longUnknown = longUnknown;
        this.anchorFlags = anchorFlags;
    }

    @Override
    public void parse(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (args.hasPrevious() && !this.anchorFlags || !args.hasNext() || !args.peek().startsWith("-")) {
            return; // Nothing to parse, move along.
        }

        // Avoiding APE
        CommandArgs.State tokenizedPreviousState = args.getState();
        CommandContext.State contextPreviousState = context.getState();
        String next = args.next();
        if (next.startsWith("--")) {
            parseLong(next, cause, args, context, tokenizedPreviousState, contextPreviousState);
        } else {
            parseShort(next, cause, args, context, tokenizedPreviousState, contextPreviousState);
        }
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        return Lists.newArrayList(); // This gets ignored
    }

    private void parseShort(String flag, Cause cause, CommandArgs args, CommandContext context, CommandArgs.State tokenizedPreviousState,
            CommandContext.State contextPreviousState) throws ArgumentParseException {
        char[] shortFlags = flag.substring(1).toLowerCase(Locale.ENGLISH).toCharArray();

        // -abc is parsed as -a -b -c
        // Note that if we have -abc [blah], a and b MUST NOT try to parse the next value. This is why we have the
        // PreventIteratorMovementCommandArgs class, which will throw an error in those scenarios.
        // -c is allowed to have a value.
        PreventIteratorMovementCommandArgs nonMoving = new PreventIteratorMovementCommandArgs(args);
        for (int i = 0; i < shortFlags.length; i++) {
            CommandArgs argsToUse = i == shortFlags.length - 1 ? args : nonMoving;
            Parameter param = this.flags.get(String.valueOf(shortFlags[i]));
            if (param == null) {
                this.shortUnknown.parse(cause, argsToUse, context, tokenizedPreviousState, contextPreviousState, String.valueOf(shortFlags[i]));
            } else {
                param.parse(cause, argsToUse, context);
            }
        }
    }

    private void parseLong(String flag, Cause cause, CommandArgs args, CommandContext context, CommandArgs.State tokenizedPreviousState,
            CommandContext.State contextPreviousState) throws ArgumentParseException {
        String longFlag = flag.substring(2).toLowerCase(Locale.ENGLISH);
        Parameter param = this.flags.get(longFlag);
        if (param == null) {
            this.longUnknown.parse(cause, args, context, tokenizedPreviousState, contextPreviousState, longFlag);
        } else {
            param.parse(cause, args, context);
        }
    }

    @Override
    public Text getUsage(Cause cause) {
        return Text.joinWith(CommandMessageFormatting.SPACE_TEXT, this.primaryFlags.stream()
                .map(this.flags::get).map(x -> x.getUsage(cause)).filter(x -> !x.isEmpty()).collect(Collectors.toList()));
    }

    @Override
    public boolean isAnchored() {
        return this.anchorFlags;
    }

    void populateBuilder(SpongeFlagsBuilder builder) {
        builder.updateFlags(this.primaryFlags, this.flags)
                .setUnknownShortFlagBehavior(this.shortUnknown)
                .setUnknownLongFlagBehavior(this.longUnknown)
                .setAnchorFlags(this.anchorFlags);
    }

    private class PreventIteratorMovementCommandArgs implements CommandArgs {

        private final CommandArgs args;

        PreventIteratorMovementCommandArgs(CommandArgs args) {
            this.args = args;
        }

        @Override
        public boolean hasNext() {
            return this.args.hasNext();
        }

        @Override
        public String next() throws ArgumentParseException {
            throw createValueError();
        }

        @Override
        public Optional<String> nextIfPresent() {
            return Optional.empty();
        }

        @Override
        public String peek() throws ArgumentParseException {
            return this.args.peek();
        }

        @Override
        public boolean hasPrevious() {
            return this.args.hasPrevious();
        }

        @Override
        public String previous() throws ArgumentParseException {
            throw createValueError();
        }

        @Override
        public List<String> getAll() {
            return this.args.getAll();
        }

        @Override
        public int getCurrentRawPosition() {
            return this.args.getCurrentRawPosition();
        }

        @Override
        public String getRaw() {
            return this.args.getRaw();
        }

        @Override
        public State getState() {
            return this.args.getState();
        }

        @Override
        public void setState(State state) {
            // noop
        }

        @Override
        public ArgumentParseException createError(Text message) {
            return this.args.createError(message);
        }

        @Override
        public ArgumentParseException createError(Text message, Throwable inner) {
            return this.args.createError(message, inner);
        }

        @Override
        public String rawArgsFromCurrentPosition() {
            return this.args.rawArgsFromCurrentPosition();
        }

        private ArgumentParseException createValueError() {
            return createError(t("Short flags that are not at the end of a group cannot have a value."));
        }
    }
}
