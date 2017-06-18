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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.ValueParameter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class LiteralValueParameter implements ValueParameter {

    private final Supplier<Iterable<String>> literalSupplier;
    private final Supplier<?> returnedValueSupplier;

    public LiteralValueParameter(Supplier<Iterable<String>> literalSupplier, Supplier<?> returnedValueSupplier) {
        this.literalSupplier = literalSupplier;
        this.returnedValueSupplier = returnedValueSupplier;
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        Iterable<String> supplier = this.literalSupplier.get();
        for (String literal : supplier) {
            String current = args.next();
            if (!current.equalsIgnoreCase(literal)) {
                throw args.createError(t("Argument %s did not match expected next argument %s", current, literal));
            }
        }

        return Optional.ofNullable(this.returnedValueSupplier.get());
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        for (String arg : this.literalSupplier.get()) {
            final Optional<String> next = args.nextIfPresent();
            if (!next.isPresent()) {
                break;
            } else if (args.hasNext()) {
                if (!next.get().equalsIgnoreCase(arg)) {
                    break;
                }
            } else {
                if (arg.toLowerCase().startsWith(next.get().toLowerCase())) { // Case-insensitive compare
                    return ImmutableList.of(arg);
                }
            }
        }
        return ImmutableList.of();
    }

    @Override
    public Text getUsage(Text key, Cause cause) {
        return Text.of(String.join(" ", this.literalSupplier.get()));
    }

    // For the resettable builder

    public Supplier<Iterable<String>> getLiteralSupplier() {
        return this.literalSupplier;
    }

    public Supplier<?> getReturnedValueSupplier() {
        return this.returnedValueSupplier;
    }
}
