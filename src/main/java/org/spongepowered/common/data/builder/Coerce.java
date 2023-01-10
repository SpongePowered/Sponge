/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
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
package org.spongepowered.common.data.builder;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for coercing unknown values to specific target types.
 */
public final class Coerce {

    private static final Pattern listPattern = Pattern.compile("^([\\(\\[\\{]?)(.+?)([\\)\\]\\}]?)$");

    private static final String[] listPairings = {"([{", ")]}"};

    /**
     * No subclasses for you.
     */
    private Coerce() {}

    /**
     * Gets the given object as a {@link String}.
     *
     * @param obj The object to translate
     * @return The string value, if available
     */
    public static Optional<String> asString(@Nullable Object obj) {
        if (obj instanceof String) {
            return Optional.of((String) obj);
        } else if (obj == null) {
            return Optional.empty();
        } else {
            return Optional.of(obj.toString());
        }
    }

    /**
     * Gets the given object as a {@link Boolean}.
     *
     * @param obj The object to translate
     * @return The boolean, if available
     */
    public static Optional<Boolean> asBoolean(@Nullable Object obj) {
        if (obj instanceof Boolean) {
            return Optional.of((Boolean) obj);
        } else if (obj instanceof Byte) {
            return Optional.of((Byte) obj != 0);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a {@link Integer}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The integer value, if available
     */
    public static Optional<Integer> asInteger(@Nullable Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).intValue());
        }

        try {
            return Optional.ofNullable(Integer.valueOf(obj.toString()));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }

        final String strObj = Coerce.sanitiseNumber(obj);
        final Integer iParsed = Ints.tryParse(strObj);
        if (iParsed == null) {
            final Double dParsed = Doubles.tryParse(strObj);
            // try parsing as double now
            return dParsed == null ? Optional.<Integer>empty() : Optional.of(dParsed.intValue());
        }
        return Optional.of(iParsed);
    }

    /**
     * Gets the given object as a {@link Double}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The double value, if available
     */
    public static Optional<Double> asDouble(@Nullable Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).doubleValue());
        }

        try {
            return Optional.ofNullable(Double.valueOf(obj.toString()));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }

        final String strObj = Coerce.sanitiseNumber(obj);
        final Double dParsed = Doubles.tryParse(strObj);
        // try parsing as double now
        return dParsed == null ? Optional.<Double>empty() : Optional.of(dParsed);
    }

    /**
     * Gets the given object as a {@link Float}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The float value, if available
     */
    public static Optional<Float> asFloat(@Nullable Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).floatValue());
        }

        try {
            return Optional.ofNullable(Float.valueOf(obj.toString()));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }

        final String strObj = Coerce.sanitiseNumber(obj);
        final Double dParsed = Doubles.tryParse(strObj);
        return dParsed == null ? Optional.<Float>empty() : Optional.of(dParsed.floatValue());
    }

    /**
     * Gets the given object as a {@link Short}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The short value, if available
     */
    public static Optional<Short> asShort(@Nullable Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).shortValue());
        }

        try {
            return Optional.ofNullable(Short.parseShort(Coerce.sanitiseNumber(obj)));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a {@link Byte}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The byte value, if available
     */
    public static Optional<Byte> asByte(@Nullable Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).byteValue());
        }

        try {
            return Optional.ofNullable(Byte.parseByte(Coerce.sanitiseNumber(obj)));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a {@link Long}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The long value, if available
     */
    public static Optional<Long> asLong(@Nullable Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).longValue());
        }

        try {
            return Optional.ofNullable(Long.parseLong(Coerce.sanitiseNumber(obj)));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a {@link Character}.
     *
     * @param obj The object to translate
     * @return The character, if available
     */
    public static Optional<Character> asChar(@Nullable Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        if (obj instanceof Character) {
            return Optional.of((Character) obj);
        }
        try {
            return Optional.of(obj.toString().charAt(0));
        } catch (Exception e) {
            // do nothing
        }
        return Optional.empty();
    }

    /**
     * Sanitise a string containing a common representation of a number to make
     * it parsable. Strips thousand-separating commas and trims later members
     * of a comma-separated list. For example the string "(9.5, 10.6, 33.2)"
     * will be sanitised to "9.5".
     *
     * @param obj Object to sanitise
     * @return Sanitised number-format string to parse
     */
    private static String sanitiseNumber(Object obj) {
        String string = obj.toString().trim();
        if (string.length() < 1) {
            return "0";
        }

        final Matcher candidate = Coerce.listPattern.matcher(string);
        if (Coerce.listBracketsMatch(candidate)) {
            string = candidate.group(2).trim();
        }

        final int decimal = string.indexOf('.');
        final int comma = string.indexOf(',', decimal);
        if (decimal > -1 && comma > -1) {
            return Coerce.sanitiseNumber(string.substring(0, comma));
        }

        if (string.indexOf('-', 1) != -1) {
            return "0";
        }

        return string.replace(",", "").split(" ", 0)[0];
    }

    private static boolean listBracketsMatch(Matcher candidate) {
        return candidate.matches() && Coerce.listPairings[0].indexOf(candidate.group(1)) == Coerce.listPairings[1].indexOf(candidate.group(3));
    }

}
