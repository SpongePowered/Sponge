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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@NonnullByDefault
public class SpongeArgumentType<T> extends SpongeArgumentHolder<ArgumentType<T>> implements ArgumentType<T> {

    private static final Map<String, Function<String, ?>> converters = Maps.newHashMap();

    static {
        converters.put(String.class.getName(), Functions.<String>identity());
        converters.put(Team.class.getName(), new Function<String, Function<World, Team>>() {

            @Override
            public Function<World, Team> apply(final String input) {
                return new Function<World, Team>() {

                    @Override
                    public Team apply(World world) {
                        return world.getScoreboard().getTeam(input).get();
                    }

                };
            }

        });
        converters.put(Score.class.getName(), new Function<String, Function<World, Score>>() {

            @Override
            public Function<World, Score> apply(final String input) {
                return new Function<World, Score>() {

                    @Override
                    public Score apply(World world) {
                        Set<Score> scores = world.getScoreboard().getScores(Texts.of(input));
                        checkState(scores.size() == 1, "Scores for %s more than 1", input);
                        return scores.iterator().next();
                    }

                };
            }

        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<String, T> getConverter(final Class<T> type, String converterKey) {
        if (!converters.containsKey(converterKey)) {
            try {
                final Method valueOf = type.getMethod("valueOf", String.class);
                converters.put(converterKey, SpongeSelectorFactory.<String, T>methodAsFunction(valueOf, true));
            } catch (NoSuchMethodException ignored) {
                if (CatalogType.class.isAssignableFrom(type)) {
                    final Class<? extends CatalogType> type2 = type.asSubclass(CatalogType.class);
                    converters.put(converterKey, new Function<String, T>() {

                        @Override
                        public T apply(String input) {
                            // assume it exists for now
                            return (T) Sponge.getGame().getRegistry().getType(type2, input).get();
                        }

                    });
                } else {
                    throw new IllegalStateException("can't convert " + type);
                }
            } catch (SecurityException e) {
                Sponge.getLogger().catching(e);
            }
        }
        return (Function<String, T>) converters.get(converterKey);
    }

    private final String key;
    private final Function<String, T> converter;

    public SpongeArgumentType(String key, Class<T> type) {
        this(key, type, type.getName());
    }

    public SpongeArgumentType(String key, Class<T> type, String converterKey) {
        this(key, getConverter(type, converterKey));
    }

    public SpongeArgumentType(String key, Function<String, T> converter) {
        this.key = key;
        this.converter = checkNotNull(converter);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    protected T convert(String s) {
        return this.converter.apply(s);
    }

    public static class Invertible<T> extends SpongeArgumentType<T> implements ArgumentType.Invertible<T> {

        public Invertible(String key, Class<T> type) {
            super(key, type);
        }

        public Invertible(String key, Class<T> type, String converterKey) {
            super(key, getConverter(type, converterKey));
        }

        public Invertible(String key, Function<String, T> converter) {
            super(key, converter);
        }

    }
}
