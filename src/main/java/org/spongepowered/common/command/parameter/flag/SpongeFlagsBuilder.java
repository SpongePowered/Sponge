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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.command.parameter.flag.UnknownFlagBehavior;
import org.spongepowered.api.command.parameter.flag.UnknownFlagBehaviors;
import org.spongepowered.api.command.parameter.managed.ParsingContext;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.parameter.SpongeSequenceParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpongeFlagsBuilder implements Flags.Builder {

    private final static ValueParser MARK_TRUE = (source, args, context) -> Optional.of(true);

    private final List<String> primaryFlags = Lists.newArrayList();
    private final Map<String, Parameter> flags = Maps.newHashMap();
    private UnknownFlagBehavior shortUnknown = UnknownFlagBehaviors.ERROR;
    private UnknownFlagBehavior longUnknown = UnknownFlagBehaviors.ERROR;
    private boolean anchorFlags = false;

    @Override
    public Flags.Builder flag(String primaryIdentifier, String... secondaryIdentifiers) {
        Preconditions.checkNotNull(primaryIdentifier, "primaryIdentifier");
        List<String> aliases = storeAliases(primaryIdentifier, secondaryIdentifiers);
        Text usage = Text.of(CommandMessageFormatting.LEFT_SQUARE, getFlag(primaryIdentifier), CommandMessageFormatting.RIGHT_SQUARE);
        Parameter markTrue = Parameter.builder().setKey(primaryIdentifier.toLowerCase()).setParser(MARK_TRUE).setUsage((source, current) -> usage)
                .build();
        this.primaryFlags.add(primaryIdentifier.toLowerCase());
        for (String alias : aliases) {
            this.flags.put(alias, markTrue);
        }
        return this;
    }

    @Override
    public Flags.Builder permissionFlag(String flagPermission, String primaryIdentifier, String... secondaryIdentifiers) {
        Preconditions.checkNotNull(primaryIdentifier, "primaryIdentifier");
        List<String> aliases = storeAliases(primaryIdentifier, secondaryIdentifiers);
        final String first = getFlag(primaryIdentifier.toLowerCase());
        Text usage = Text.of(CommandMessageFormatting.LEFT_SQUARE, first, CommandMessageFormatting.RIGHT_SQUARE);
        Parameter perm = Parameter.builder().setKey(primaryIdentifier.toLowerCase()).setParser(MARK_TRUE)
                .modifiers(new PermissionModifier(first, flagPermission, usage)).build();
        this.primaryFlags.add(primaryIdentifier.toLowerCase());
        for (String alias : aliases) {
            this.flags.put(alias, perm);
        }
        return this;
    }

    @Override
    public Flags.Builder valueFlag(Parameter value, String primaryIdentifier, String... secondaryIdentifiers) {
        Preconditions.checkNotNull(primaryIdentifier, "primaryIdentifier");
        List<String> aliases = storeAliases(primaryIdentifier, secondaryIdentifiers);
        this.primaryFlags.add(primaryIdentifier.toLowerCase());
        Parameter element = new UsageWrapper(
                new SpongeSequenceParameter(
                    Lists.newArrayList(Parameter.builder().setKey(primaryIdentifier.toLowerCase()).setParser(MARK_TRUE)
                            .setUsage((source, current) -> Text.EMPTY).build(), value), false, false));
        for (String alias : aliases) {
            this.flags.put(alias, element);
        }
        return this;
    }

    @Override
    public Flags.Builder setUnknownLongFlagBehavior(UnknownFlagBehavior behavior) {
        Preconditions.checkNotNull(behavior);
        this.longUnknown = behavior;
        return this;
    }

    @Override
    public Flags.Builder setUnknownShortFlagBehavior(UnknownFlagBehavior behavior) {
        Preconditions.checkNotNull(behavior);
        this.shortUnknown = behavior;
        return this;
    }

    @Override
    public Flags.Builder setAnchorFlags(boolean anchorFlags) {
        this.anchorFlags = anchorFlags;
        return this;
    }

    @Override
    public Flags build() {
        return new SpongeFlags(
                ImmutableList.copyOf(this.primaryFlags),
                ImmutableMap.copyOf(this.flags),
                this.shortUnknown,
                this.longUnknown,
                this.anchorFlags
        );
    }

    @Override
    public Flags.Builder from(Flags value) {
        if (!(value instanceof SpongeFlags)) {
            throw new IllegalArgumentException("value must be a SpongeFlags object");
        }

        ((SpongeFlags) value).populateBuilder(this);

        return this;
    }

    // For use in from
    SpongeFlagsBuilder updateFlags(List<String> primaryFlags, Map<String, Parameter> flags) {
        this.primaryFlags.clear();
        this.primaryFlags.addAll(primaryFlags);
        this.flags.clear();
        this.flags.putAll(flags);
        return this;
    }

    @Override
    public Flags.Builder reset() {
        this.flags.clear();
        this.shortUnknown = UnknownFlagBehaviors.ERROR;
        this.longUnknown = UnknownFlagBehaviors.ERROR;
        this.anchorFlags = false;
        return this;
    }

    private String getFlag(String flag) {
        if (flag.length() == 1) {
            return "-" + flag;
        } else {
            return "--" + flag;
        }
    }

    private List<String> storeAliases(String primary, String[] secondary) {
        Preconditions.checkNotNull(primary);

        List<String> aliases = Lists.newArrayList(primary);

        // Put the flag aliases in
        aliases.addAll(Arrays.asList(secondary));

        return aliases;
    }

    private static class PermissionModifier implements ValueParameterModifier {

        private final String flag;
        private final String flagPermission;
        private final Text usage;

        PermissionModifier(String flag, String flagPermission, Text usage) {
            this.flag = flag;
            this.flagPermission = flagPermission;
            this.usage = usage;
        }

        @Override
        public void onParse(Text key, Cause cause, CommandArgs args, CommandContext context, ParsingContext parsingContext)
                throws ArgumentParseException {
            if (context.getSubject().map(x -> x.hasPermission(this.flagPermission)).orElse(true)) {
                parsingContext.next();
            } else {
                throw args.createError(t("You do not have permission to use the flag %s", this.flag));
            }
        }

        @Override
        public Text getUsage(Text key, Cause cause, Text currentUsage) {
            if (!Command.getSubjectFromCause(cause).map(x -> x.hasPermission(this.flagPermission)).orElse(true)) {
                return Text.EMPTY;
            }

            return usage;
        }
    }

    private static class UsageWrapper implements Parameter {

        private final Parameter wrapped;

        UsageWrapper(Parameter wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void parse(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
            this.wrapped.parse(cause, args, context);
        }

        @Override
        public List<String> complete(Cause cause, CommandArgs args, CommandContext context)
                throws ArgumentParseException {
            return this.wrapped.complete(cause, args, context);
        }

        @Override
        public Text getUsage(Cause cause) {
            Text usage = this.wrapped.getUsage(cause);
            if (usage.isEmpty() || usage.toPlain().matches("^\\[.*]$")) {
                return usage;
            }

            return Text.of(CommandMessageFormatting.LEFT_SQUARE, usage, CommandMessageFormatting.RIGHT_SQUARE);
        }
    }
}
