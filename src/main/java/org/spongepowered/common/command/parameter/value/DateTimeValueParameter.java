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

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class DateTimeValueParameter implements CatalogedValueParameter {

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        return Lists.newArrayList();
    }

    @Override
    public String getId() {
        return "sponge:datetime";
    }

    @Override
    public String getName() {
        return "Date Time";
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        String arg = args.next();

        try {
            // Date and time
            return Optional.of(LocalDateTime.parse(arg));
        } catch (DateTimeParseException ex) {
            try {
                // Today, but a time.
                return Optional.of(LocalDateTime.of(LocalDate.now(), LocalTime.parse(arg)));
            } catch (DateTimeParseException ex2) {
                try {
                    // A date, at midnight
                    return Optional.of(LocalDateTime.of(LocalDate.parse(arg), LocalTime.MIDNIGHT));
                } catch (DateTimeParseException ex3) {
                    throw args.createError(t("Could not determine the date and time from the input."));
                }
            }
        }

    }
}
