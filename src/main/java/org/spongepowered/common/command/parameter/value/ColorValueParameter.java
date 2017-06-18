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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Color;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorValueParameter implements CatalogedValueParameter {

    private static final Pattern RGB_PATTERN = Pattern.compile("^[0-9,]+$");

    // TODO: Replace with catalog type if Color refactor goes ahead.
    private static final Map<String, Color> INBUILT_COLORS = ImmutableMap.<String, Color>builder()
            .put("black", Color.BLACK)
            .put("blue", Color.BLUE)
            .put("cyan", Color.CYAN)
            .put("darkcyan", Color.DARK_CYAN)
            .put("darkgreen", Color.DARK_GREEN)
            .put("darkmagenta", Color.DARK_MAGENTA)
            .put("gray", Color.GRAY)
            .put("green", Color.GREEN)
            .put("lime", Color.LIME)
            .put("magenta", Color.MAGENTA)
            .put("navy", Color.NAVY)
            .put("pink", Color.PINK)
            .put("purple", Color.PURPLE)
            .put("red", Color.RED)
            .put("white", Color.WHITE)
            .put("yellow", Color.YELLOW)
            .build();
    private static final List<String> INBUILT_COLOR_NAMES = INBUILT_COLORS.keySet().stream().sorted().collect(ImmutableList.toImmutableList());

    public static String findClosestColorName(String target) {
        int distance = Integer.MAX_VALUE;
        String closest = INBUILT_COLOR_NAMES.get(0);
        for (String name : INBUILT_COLOR_NAMES) {
            int currentDistance = StringUtils.getLevenshteinDistance(name, target);
            if (currentDistance < distance) {
                distance = currentDistance;
                closest = name;
            }
        }
        return closest;
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        // We'll only complete the inbuilt names.
        // TODO: When the Color refactor comes in, we can do this over the IDs of the colours.
        String arg = args.next().toLowerCase();
        return INBUILT_COLOR_NAMES.stream().filter(x -> arg.startsWith(x.toLowerCase())).sorted().collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return "sponge:color";
    }

    @Override
    public String getName() {
        return "Color parameter";
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        String rStr = args.next();

        // Check for hex format if allowed
        if (rStr.startsWith("0x") || rStr.startsWith("#")) {
            // Get the hex value without the prefix
            String value = rStr.substring(rStr.startsWith("0x") ? 2 : 1);
            int hex;
            try {
                hex = Integer.parseInt(value, 16);
            } catch (NumberFormatException e) {
                throw args.createError(t("Expected input %s to be hexadecimal, but it was not", rStr));
            }
            return Optional.of(Color.ofRgb(hex));
        }

        // Check whether the format matches
        if (RGB_PATTERN.matcher(rStr).matches()) {
            String gStr;
            String bStr;
            // Try for the comma-separated format
            if (rStr.contains(",")) {
                String[] split = rStr.split(",");
                if (split.length != 3) {
                    throw args.createError(t("Comma-separated color must have 3 elements, not %s", split.length));
                }

                rStr = split[0];
                gStr = split[1];
                bStr = split[2];
            } else {
                gStr = args.next();
                bStr = args.next();
            }

            int r = parseComponent(args, rStr, "r");
            int g = parseComponent(args, gStr, "g");
            int b = parseComponent(args, bStr, "b");
            return Optional.of(Color.of(new Vector3i(r, g, b)));
        }

        Color color = INBUILT_COLORS.get(rStr.toLowerCase());
        if (color == null) {
            throw args.createError(t("Unknown inbuilt color: %s Did you mean: %s?",
                            rStr, findClosestColorName(rStr)));
        }

        return Optional.of(color);
    }

    private static int parseComponent(CommandArgs args, String arg, String name) throws ArgumentParseException {
        try {
            int value = Integer.parseInt(arg);
            if (value < 0) {
                throw args.createError(t("Number %s for %s component is too small, it must be at least %s", value, name, 0));
            }
            if (value > 255) {
                throw args.createError(t("Number %s for %s component is too big, it must be at most %s", value, name, 255));
            }
            return value;
        } catch (NumberFormatException e) {
            throw args.createError(t("Expected input %s for %s component to be a number, but it was not", arg, name));
        }
    }
}
