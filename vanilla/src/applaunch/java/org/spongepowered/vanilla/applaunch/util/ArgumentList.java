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
package org.spongepowered.vanilla.applaunch.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/*
 * A class that attempts to parse command line arguments into key value pairs to allow addition and editing.
 * Can not use JOptSimple as that doesn't parse out the values for keys unless the spec says it has a value.
 */
public class ArgumentList {

    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Supplier<String[]>> entries = new ArrayList<>();
    private final Map<String, EntryValue> values = new HashMap<>();

    public static ArgumentList from(final String... args) {
        final ArgumentList ret = new ArgumentList();

        boolean ended = false;
        for (int x = 0; x < args.length; x++) {
            if (!ended) {
                if ("--".equals(args[x])) { // '--' by itself means there are no more arguments
                    ended = true;
                } else if ("-".equals(args[x])) {
                    ret.addRaw(args[x]);
                } else if (args[x].startsWith("-")) {
                    final int idx = args[x].indexOf('=');
                    final String key = idx == -1 ? args[x] : args[x].substring(0, idx);
                    final String value = idx == -1 ? null : idx == args[x].length() - 1 ? "" : args[x].substring(idx + 1);

                    if (idx == -1 && x + 1 < args.length && !args[x + 1].startsWith("-")) { //Not in --key=value, so try and grab the next argument.
                        ret.addArg(true, key, args[x + 1]); //Assume that if the next value is a "argument" then don't use it as a value.
                        x++; // This isn't perfect, but the best we can do without knowing all of the spec.
                    } else {
                        ret.addArg(false, key, value);
                    }
                } else {
                    ret.addRaw(args[x]);
                }
            } else {
                ret.addRaw(args[x]);
            }
        }
        return ret;
    }

    public void addRaw(final String arg) {
        this.entries.add(() -> new String[]{arg});
    }

    public void addArg(final boolean split, final String raw, final String value) {
        final int idx = raw.startsWith("--") ? 2 : 1;
        final String prefix = raw.substring(0, idx);
        final String key = raw.substring(idx);
        final EntryValue entry = new EntryValue(split, prefix, key, value);
        if (this.values.containsKey(key)) {
            ArgumentList.LOGGER.debug("Duplicate entries for " + key + " Unindexable");
        } else {
            this.values.put(key, entry);
        }
        this.entries.add(entry);
    }

    public String[] getArguments() {
        return this.entries.stream()
            .flatMap(e -> Arrays.stream(e.get()))
            .toArray(String[]::new);
    }

    public boolean hasValue(final String key) {
        return this.getOrDefault(key, null) != null;
    }

    public String get(final String key) {
        final EntryValue ent = this.values.get(key);
        return ent == null ? null : ent.getValue();
    }

    public String getOrDefault(final String key, final String value) {
        final EntryValue ent = this.values.get(key);
        return ent == null ? value : ent.getValue() == null ? value : ent.getValue();
    }

    public void put(final String key, final String value) {
        EntryValue entry = this.values.get(key);
        if (entry == null) {
            entry = new EntryValue(true, "--", key, value);
            this.values.put(key, entry);
            this.entries.add(entry);
        } else {
            entry.setValue(value);
        }
    }

    public void putLazy(final String key, final String value) {
        final EntryValue ent = this.values.get(key);
        if (ent == null) {
            this.addArg(true, "--" + key, value);
        } else if (ent.getValue() == null) {
            ent.setValue(value);
        }
    }

    public String remove(final String key) {
        final EntryValue ent = this.values.remove(key);
        if (ent == null) {
            return null;
        }
        this.entries.remove(ent);
        return ent.getValue();
    }

    private static class EntryValue implements Supplier<String[]> {

        private final String prefix;
        private final String key;
        private final boolean split;
        private String value;

        public EntryValue(final boolean split, final String prefix, final String key, final String value) {
            this.split = split;
            this.prefix = prefix;
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        @Override
        public String[] get() {
            if (this.getValue() == null) {
                return new String[]{this.prefix + this.getKey()};
            }
            if (this.split) {
                return new String[]{this.prefix + this.getKey(), this.getValue()};
            }
            return new String[]{this.prefix + this.getKey() + '=' + this.getValue()};
        }

        @Override
        public String toString() {
            return String.join(", ", this.get());
        }
    }
}
