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

import com.google.common.collect.Maps;
import net.minecraft.world.GameType;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

@NonnullByDefault
public class SpongeArgumentType<T> extends SpongeArgumentHolder<ArgumentType<T>> implements ArgumentType<T> {

    private static final Map<String, Function<String, ?>> converters = Maps.newHashMap();

    static {
        converters.put(String.class.getName(), Function.<String>identity());
        converters.put(EntityType.class.getName(),
                       (Function<String, EntityType>) input -> EntityTypeRegistryModule.getInstance().getById(input.toLowerCase()).orElse(null));
        converters.put(GameMode.class.getName(), input -> {
            try {
                int i = Integer.parseInt(input);
                return GameType.func_185329_a(i, GameType.NOT_SET);
            } catch (NumberFormatException e) {
                return GameType.func_185328_a(input, GameType.NOT_SET);
            }
        });
    }

    @SuppressWarnings("unchecked")
    static <T> Function<String, T> getConverter(final Class<T> type, String converterKey) {
        if (!converters.containsKey(converterKey)) {
            try {
                final Method valueOf = type.getMethod("valueOf", String.class);
                converters.put(converterKey, SpongeSelectorFactory.<String, T>methodAsFunction(valueOf, true));
            } catch (NoSuchMethodException ignored) {
                if (CatalogType.class.isAssignableFrom(type)) {
                    final Class<? extends CatalogType> type2 = type.asSubclass(CatalogType.class);
                    converters.put(converterKey, (Function<String, T>) input -> {
                        // assume it exists for now
                        return (T) SpongeImpl.getGame().getRegistry().getType(type2, input).get();
                    });
                } else {
                    throw new IllegalStateException("can't convert " + type);
                }
            } catch (SecurityException e) {
                SpongeImpl.getLogger().catching(e);
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
        this.key = checkNotNull(key);
        this.converter = checkNotNull(converter);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArgumentType && ((ArgumentType<?>) obj).getKey().equals(getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
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
