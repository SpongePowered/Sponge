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

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class DurationValueParameter implements CatalogedValueParameter {

    private static final Pattern DURATION_STRING =
            Pattern.compile("^((?<weeks>\\d+)w)?((?<days>\\d+)d)?((?<hours>\\d+)h)?"
                    + "((?<minutes>\\d+)m)?((?<seconds>\\d+)s)?((?<millis>\\d+)ms)?$", Pattern.CASE_INSENSITIVE);

    private static final Text ERROR_TEXT = t("The duration was not in one of the supported formats: seconds, D:HH:MM:SS, or []w[]d[]h[]m[]s[]ms");

    @Override
    public String getId() {
        return "sponge:duration";
    }

    @Override
    public String getName() {
        return "Duration Parameter";
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        return Lists.newArrayList();
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context)
            throws ArgumentParseException {
        String arg = args.next();
        if (arg.contains(":")) {
            // Split and test.
            try {
                List<Integer> li = Arrays.stream(arg.split(":", 4)).mapToInt(Integer::parseInt)
                        .boxed().collect(Collectors.toList());
                if (li.size() == 4) {
                    return Optional.of(Duration.ofSeconds(li.get(3))
                            .plusMinutes(li.get(2))
                            .plusHours(li.get(1))
                            .plusDays(li.get(0)));
                } else if (li.size() == 3) {
                    return Optional.of(Duration.ofSeconds(li.get(2))
                            .plusMinutes(li.get(1))
                            .plusHours(li.get(0)));
                }

                return Optional.of(Duration.ofSeconds(li.get(1))
                        .plusMinutes(li.get(0)));
            } catch (Exception e) {
                throw args.createError(ERROR_TEXT);
            }
        }

        // Did the Regex match?
        Matcher matcher = DURATION_STRING.matcher(arg);
        if (!arg.isEmpty() && matcher.find()) {
            return Optional.of(Duration.ofMillis(getFromGroup(matcher.group("millis")))
                    .plusSeconds(getFromGroup(matcher.group("seconds")))
                    .plusMinutes(getFromGroup(matcher.group("minutes")))
                    .plusHours(getFromGroup(matcher.group("hours")))
                    .plusDays(getFromGroup(matcher.group("days")))
                    .plusDays(7 * getFromGroup(matcher.group("weeks"))));
        }

        try {
            return Optional.of(Integer.parseInt(arg));
        } catch (Exception e) {
            throw args.createError(ERROR_TEXT);
        }
    }

    private int getFromGroup(@Nullable String groupContents) {
        if (groupContents == null) {
            return 0;
        }
        return Integer.parseInt(groupContents);
    }

}
