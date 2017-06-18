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
package org.spongepowered.common.command.parameter.value;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class ChoicesValueParameter implements ValueParameter {

    private static final int CUTOFF = 5;

    private final Map<String, Supplier<?>> choices;
    private final Tristate includeChoicesInUsage;

    public ChoicesValueParameter(Map<String, Supplier<?>> choices, Tristate includeChoicesInUsage) {
        this.choices = choices;
        this.includeChoicesInUsage = includeChoicesInUsage;
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        final String nextArg = args.next();
        return Optional.ofNullable(getValue(nextArg, args));
    }

    @Nullable
    public Object getValue(String nextArg, CommandArgs args) throws ArgumentParseException {
        return this.choices.entrySet().stream().filter(k -> k.getKey().equalsIgnoreCase(nextArg)).findFirst()
                .orElseThrow(() -> args.createError(t("Argument was not a valid choice. Valid choices: %s",
                        this.choices.keySet().stream().map(x -> x.toLowerCase(Locale.ENGLISH))
                                .sorted().collect(Collectors.joining(", ")))))
                .getValue().get();
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        final String nextArg = args.peek();
        return this.choices.keySet().stream().filter(x -> x.toLowerCase(Locale.ENGLISH)
                .startsWith(nextArg.toLowerCase(Locale.ENGLISH))).sorted().collect(Collectors.toList());
    }

    @Override
    public Text getUsage(Text key, Cause cause) {
        if (this.includeChoicesInUsage != Tristate.FALSE) {
            List<String> choices = this.choices.keySet().stream().sorted().collect(Collectors.toList());
            if (this.includeChoicesInUsage.asBoolean() || choices.size() < CUTOFF) {
                return Text.of("<", String.join("|", choices), ">");
            }
        }
        return key;
    }

    public Map<String, Supplier<?>> getChoices() {
        return this.choices;
    }

    public Tristate getIncludeChoicesInUsage() {
        return this.includeChoicesInUsage;
    }

}
