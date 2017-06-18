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

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.Iterables;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

public class DynamicChoicesValueParameter implements ValueParameter {

    private static final int CUTOFF = 5;

    private final Supplier<Iterable<String>> choiceSupplier;
    private final Function<String, ?> resultFunction;
    private final Tristate includeChoicesInUsage;

    public DynamicChoicesValueParameter(Supplier<Iterable<String>> choiceSupplier, Function<String, ?> resultFunction,
            Tristate includeChoicesInUsage) {
        this.choiceSupplier = choiceSupplier;
        this.resultFunction = resultFunction;
        this.includeChoicesInUsage = includeChoicesInUsage;
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        final String nextArg = args.next();
        return Optional.ofNullable(getValue(nextArg, args));
    }

    @Nullable
    public Object getValue(String nextArg, CommandArgs args) throws ArgumentParseException {
        Iterable<String> suppliedChoices = this.choiceSupplier.get();
        for (String choice : suppliedChoices) {
            if (choice.equalsIgnoreCase(nextArg)) {
                return this.resultFunction.apply(choice);
            }
        }

        throw args.createError(t("Argument was not a valid choice. Valid choices: %s", String.join(", ", suppliedChoices)));
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        final String nextArg = args.peek();
        return StreamSupport.stream(this.choiceSupplier.get().spliterator(), false).filter(x -> x.toLowerCase(Locale.ENGLISH)
                .startsWith(nextArg.toLowerCase(Locale.ENGLISH))).sorted().collect(Collectors.toList());
    }

    @Override
    public Text getUsage(Text key, Cause cause) {
        if (this.includeChoicesInUsage != Tristate.FALSE) {
            List<String> choices = StreamSupport.stream(this.choiceSupplier.get().spliterator(), false).sorted().collect(Collectors.toList());
            if (this.includeChoicesInUsage.asBoolean() || choices.size() < CUTOFF) {
                return Text.of("<", String.join("|", choices), ">");
            }
        }
        return key;
    }

    public Supplier<Iterable<String>> getChoiceSupplier() {
        return choiceSupplier;
    }

    public Function<String, ?> getResultFunction() {
        return resultFunction;
    }

    public Tristate getIncludeChoicesInUsage() {
        return this.includeChoicesInUsage;
    }

}
