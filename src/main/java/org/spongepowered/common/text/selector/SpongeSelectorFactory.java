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
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.ArgumentHolder;
import org.spongepowered.api.text.selector.ArgumentHolder.Limit;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorFactory;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.mixin.core.command.EntitySelectorAccessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@NonnullByDefault
public class SpongeSelectorFactory implements SelectorFactory {

    public static final SpongeSelectorFactory INSTANCE = new SpongeSelectorFactory();

    private SpongeSelectorFactory() {
    }

    private static final Map<String, SelectorType> idToType;

    static {
        ImmutableMap.Builder<String, SelectorType> builder =
                ImmutableMap.builder();

        for (SelectorType type : Sponge.getRegistry().getAllOf(SelectorType.class)) {
            builder.put(type.getName(), type);
        }

        idToType = builder.build();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Function<K, V> methodAsFunction(final Method m, boolean isStatic) {
        if (isStatic) {
            return input -> {
                try {
                    return (V) m.invoke(null, input);
                } catch (IllegalAccessException e) {
                    SpongeImpl.getLogger().debug(m + " wasn't public", e);
                    return null;
                } catch (IllegalArgumentException e) {
                    SpongeImpl.getLogger()
                            .debug(m + " failed with paramter " + input, e);
                    return null;
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            };
        }
        return input -> {
            try {
                return (V) m.invoke(input);
            } catch (IllegalAccessException e) {
                SpongeImpl.getLogger().debug(m + " wasn't public", e);
                return null;
            } catch (IllegalArgumentException e) {
                SpongeImpl.getLogger()
                        .debug(m + " failed with paramter " + input, e);
                return null;
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        };
    }

    private final Map<String, ArgumentHolder.Limit<ArgumentType<Integer>>> scoreToTypeMap =
            Maps.newLinkedHashMap();
    private final Map<String, ArgumentType<?>> argumentLookupMap = Maps
            .newLinkedHashMap();

    @Override
    public Selector.Builder createBuilder() {
        return new SpongeSelectorBuilder();
    }

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
                rawMap = EntitySelectorAccessor.accessor$getArgumentMap(selector.substring(argListIndex + 1, selector.length() - 1));
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
                         new SpongeArgumentHolder.SpongeLimit<>(
                             min, max));
        }
        return this.scoreToTypeMap.get(name);
    }

    @Override
    public Optional<ArgumentType<?>> getArgumentType(String name) {
        if (name.startsWith("score_")) {
            String objective = name.replaceAll("^score_", "").replaceAll("_min$", "");
            Limit<ArgumentType<Integer>> limit = createScoreArgumentType(objective);
            if (name.endsWith("_min")) {
                return Optional.of(limit.minimum());
            } else {
                return Optional.of(limit.maximum());
            }
        }
        return Optional.ofNullable(this.argumentLookupMap.get(name));
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
            checkNotNull(converterKey, "converter key cannot be null");
            this.argumentLookupMap.put(key, new SpongeArgumentType<>(key,
                                                                     type, converterKey));
        }
        return (SpongeArgumentType<T>) this.argumentLookupMap.get(key);
    }

    public <T> SpongeArgumentType.Invertible<T> createInvertibleArgumentType(
            String key, Class<T> type) {
        return createInvertibleArgumentType(key, type, type.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> SpongeArgumentType.Invertible<T> createInvertibleArgumentType(
            String key, Class<T> type, String converterKey) {
        if (!this.argumentLookupMap.containsKey(key)) {
            checkNotNull(converterKey, "converter key cannot be null");
            this.argumentLookupMap.put(key,
                                       new SpongeArgumentType.Invertible<>(key, type,
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
        return new SpongeArgument<>(type, value);
    }

    @Override
    public <T> Argument.Invertible<T> createArgument(
            ArgumentType.Invertible<T> type, T value, boolean inverted) {
        return new SpongeArgument.Invertible<>(type, value, inverted);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, V> Set<Argument<T>> createArguments(
            ArgumentHolder<? extends ArgumentType<T>> type, V value) {
        Set<Argument<T>> set = Sets.newLinkedHashSet();
        if (type instanceof SpongeArgumentHolder.SpongeVector3) {
            Set<Function<V, T>> extractors =
                    ((SpongeArgumentHolder.SpongeVector3<V, T>) type)
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
            new HashMap<>(argumentMap.size());
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
        if (type instanceof ArgumentType.Invertible && !value.isEmpty() && value.charAt(0) == '!') {
            created =
                    createArgument((ArgumentType.Invertible<Object>) type,
                            type.convert(value.substring(1)), true);
        } else {
            created = createArgument(type, type.convert(value));
        }
        return created;
    }

    @Override
    public List<String> complete(String selector) {
        if (!selector.startsWith("@") || selector.contains("]")) {
            return ImmutableList.of();
        }
        Stream<String> choices;
        if (!selector.contains("[")) {
            // No arguments yet
            choices = Sponge.getRegistry().getAllOf(SelectorType.class).stream().map(type -> "@" + type.getId());
        } else {
            int keyStart = Math.max(selector.indexOf("["), selector.lastIndexOf(",")) + 1;
            int valueStart = selector.lastIndexOf("=") + 1;
            final String prefix = selector.substring(Math.max(keyStart, valueStart));
            if (keyStart > valueStart) {
                // Tab completing key
                choices = ArgumentTypes.values().stream().map(ArgumentType::getKey);
            } else {
                // Tab completing value
                Optional<ArgumentType<?>> type = ArgumentTypes.valueOf(selector.substring(keyStart, valueStart - 1));
                if (!type.isPresent()) {
                    return ImmutableList.of();
                }
                // TODO How to get all the values of an argument type?
                return ImmutableList.of();
            }
            choices = choices.map(input -> prefix + input);
        }
        return choices.filter(choice -> choice.startsWith(selector)).collect(ImmutableList.toImmutableList());
    }

}
