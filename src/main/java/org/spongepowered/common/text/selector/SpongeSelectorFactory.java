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
package org.spongepowered.common.text.selector;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.command.PlayerSelector;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentHolder.Limit;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorBuilder;
import org.spongepowered.api.text.selector.SelectorFactory;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.Sponge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@NonnullByDefault
public class SpongeSelectorFactory implements SelectorFactory {

    private static final Map<String, SelectorType> idToType;

    static {
        ImmutableMap.Builder<String, SelectorType> builder =
                ImmutableMap.builder();

        for (SelectorType type : Sponge.getGame().getRegistry()
                .getAllOf(SelectorType.class)) {
            builder.put(type.getId(), type);
        }

        idToType = builder.build();
    }

    public static <K, V> Function<K, V> methodAsFunction(final Method m,
            boolean isStatic) {
        if (isStatic) {
            return new Function<K, V>() {

                @SuppressWarnings("unchecked")
                @Override
                public V apply(K input) {
                    try {
                        return (V) m.invoke(null, input);
                    } catch (IllegalAccessException e) {
                        Sponge.getLogger().debug(m + " wasn't public", e);
                        return null;
                    } catch (IllegalArgumentException e) {
                        Sponge.getLogger()
                                .debug(m + " failed with paramter " + input, e);
                        return null;
                    } catch (InvocationTargetException e) {
                        throw Throwables.propagate(e.getCause());
                    }
                }

            };
        } else {
            return new Function<K, V>() {

                @SuppressWarnings("unchecked")
                @Override
                public V apply(K input) {
                    try {
                        return (V) m.invoke(input);
                    } catch (IllegalAccessException e) {
                        Sponge.getLogger().debug(m + " wasn't public", e);
                        return null;
                    } catch (IllegalArgumentException e) {
                        Sponge.getLogger()
                                .debug(m + " failed with paramter " + input, e);
                        return null;
                    } catch (InvocationTargetException e) {
                        throw Throwables.propagate(e.getCause());
                    }
                }

            };
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> recast(Optional<?> source) {
        return (Optional<T>) source;
    }

    private final Map<String, ArgumentHolder.Limit<ArgumentType<Integer>>> scoreToTypeMap =
            Maps.newLinkedHashMap();
    private final Map<String, ArgumentType<?>> argumentLookupMap = Maps
            .newLinkedHashMap();

    @Override
    public SelectorBuilder createBuilder(SelectorType type) {
        return new SpongeSelectorBuilder(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Selector parseRawSelector(String selector) {
        checkArgument(selector.startsWith("@"), "Invalid selector %s",
                selector);
        // If multi-character types are possible, this handles it
        int argListIndex = selector.indexOf('[');
        if (argListIndex < 0) {
            argListIndex = selector.length();
        } else {
            int end = selector.indexOf(']');
            checkArgument(end > argListIndex && selector.charAt(end - 1) != ',',
                    "Invalid selector %s",
                    selector);
        }
        String typeStr = selector.substring(1, argListIndex);
        checkArgument(idToType.containsKey(typeStr), "No type known as '%s'",
                typeStr);
        SelectorType type = idToType.get(typeStr);
        try {
            Map<String, String> rawMap;
            if (argListIndex == selector.length()) {
                rawMap = ImmutableMap.of();
            } else {
                rawMap =
                        PlayerSelector.getArgumentMap(selector.substring(
                                argListIndex + 1, selector.length() - 1));
            }
            Map<ArgumentType<?>, Argument<?>> arguments =
                    parseArguments(rawMap);
            return new SpongeSelector(type, ImmutableMap.copyOf(arguments));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid selector " + selector,
                    e);
        }
    }

    @Override
    public Limit<ArgumentType<Integer>> createScoreArgumentType(String name) {
        if (!this.scoreToTypeMap.containsKey(name)) {
            SpongeArgumentType<Integer> min = createArgumentType(
                    "score_" + name + "_min", Integer.class,
                    Score.class.getName());
            SpongeArgumentType<Integer> max = createArgumentType(
                    "score_" + name, Integer.class,
                    Score.class.getName());
            this.scoreToTypeMap
                    .put(name,
                            new SpongeArgumentHolder.SpongeLimit<ArgumentType<Integer>>(
                                    min, max));
        }
        return this.scoreToTypeMap.get(name);
    }

    @Override
    public Optional<ArgumentType<?>> getArgumentType(String name) {
        return recast(Optional.fromNullable(this.argumentLookupMap.get(name)));
    }

    @Override
    public Collection<ArgumentType<?>> getArgumentTypes() {
        return this.argumentLookupMap.values();
    }

    @Override
    public SpongeArgumentType<String> createArgumentType(String key) {
        return createArgumentType(key, String.class);
    }

    @Override
    public <T> SpongeArgumentType<T> createArgumentType(String key,
            Class<T> type) {
        return createArgumentType(key, type, type.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> SpongeArgumentType<T> createArgumentType(String key,
            Class<T> type, String converterKey) {
        if (!this.argumentLookupMap.containsKey(key)) {
            this.argumentLookupMap.put(key, new SpongeArgumentType<T>(key,
                    type, converterKey));
        }
        return (SpongeArgumentType<T>) this.argumentLookupMap.get(key);
    }

    public SpongeArgumentType.Invertible<String> createInvertibleArgumentType(
            String key) {
        return createInvertibleArgumentType(key, String.class);
    }

    public <T> SpongeArgumentType.Invertible<T> createInvertibleArgumentType(
            String key, Class<T> type) {
        return createInvertibleArgumentType(key, type, type.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> SpongeArgumentType.Invertible<T> createInvertibleArgumentType(
            String key, Class<T> type, String converterKey) {
        if (!this.argumentLookupMap.containsKey(key)) {
            this.argumentLookupMap.put(key,
                    new SpongeArgumentType.Invertible<T>(key, type,
                            converterKey));
        }
        return (SpongeArgumentType.Invertible<T>) this.argumentLookupMap
                .get(key);
    }

    @Override
    public <T> Argument<T> createArgument(ArgumentType<T> type, T value) {
        if (type instanceof ArgumentType.Invertible) {
            return createArgument((ArgumentType.Invertible<T>) type, value,
                    false);
        }
        return new SpongeArgument<T>(type, value);
    }

    @Override
    public <T> Argument.Invertible<T> createArgument(
            ArgumentType.Invertible<T> type, T value, boolean inverted) {
        return new SpongeArgument.Invertible<T>(type, value, inverted);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, V> Set<Argument<T>> createArguments(
            ArgumentHolder<? extends ArgumentType<T>> type, V value) {
        Set<Argument<T>> set = Sets.newLinkedHashSet();
        if (type instanceof SpongeArgumentHolder.SpongeVector3) {
            Set<Function<V, T>> extractors =
                    ((SpongeArgumentHolder.SpongeVector3<V, T>) (Object) type)
                            .extractFunctions();
            Set<? extends ArgumentType<T>> types = type.getTypes();
            Iterator<Function<V, T>> extIter = extractors.iterator();
            Iterator<? extends ArgumentType<T>> typeIter = types.iterator();
            while (extIter.hasNext() && typeIter.hasNext()) {
                Function<V, T> extractor = extIter.next();
                ArgumentType<T> subtype = typeIter.next();
                set.add(createArgument(subtype, extractor.apply(value)));
            }
        }
        return set;
    }

    @Override
    public Argument<?> parseArgument(String argument)
            throws IllegalArgumentException {
        String[] argBits = argument.split("=");
        SpongeArgumentType<Object> type = getArgumentTypeWithChecks(argBits[0]);
        String value = argBits[1];
        return parseArgumentCreateShared(type, value);
    }

    public Map<ArgumentType<?>, Argument<?>> parseArguments(
            Map<String, String> argumentMap) {
        Map<ArgumentType<?>, Argument<?>> generated =
                new HashMap<ArgumentType<?>, Argument<?>>(argumentMap.size());
        for (Entry<String, String> argument : argumentMap.entrySet()) {
            String argKey = argument.getKey();
            SpongeArgumentType<Object> type = getArgumentTypeWithChecks(argKey);
            String value = argument.getValue();
            generated.put(type, parseArgumentCreateShared(type, value));
        }
        return generated;
    }

    private SpongeArgumentType<Object> getArgumentTypeWithChecks(String argKey) {
        Optional<ArgumentType<?>> type = ArgumentTypes.valueOf(argKey);
        if (!type.isPresent()) {
            throw new IllegalArgumentException("Invalid argument key " + argKey);
        }
        @SuppressWarnings("unchecked")
        ArgumentType<Object> unwrappedType = (ArgumentType<Object>) type.get();
        if (!(unwrappedType instanceof SpongeArgumentType)) {
            // TODO handle convert generally?
            throw new IllegalStateException("Cannot convert from string: "
                    + unwrappedType);
        }
        return (SpongeArgumentType<Object>) unwrappedType;
    }

    @SuppressWarnings("unchecked")
    private Argument<?> parseArgumentCreateShared(
            SpongeArgumentType<Object> type, String value) {
        Argument<?> created;
        if (type instanceof ArgumentType.Invertible && value.charAt(0) == '!') {
            created =
                    createArgument((ArgumentType.Invertible<Object>) type,
                            type.convert(value.substring(1)), true);
        } else {
            created = createArgument(type, type.convert(value));
        }
        return created;
    }

}
