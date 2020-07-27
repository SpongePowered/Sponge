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
package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.OptBool;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class DataProviderRegistrator {

    protected final DataProviderRegistratorBuilder builder;

    public DataProviderRegistrator(final DataProviderRegistratorBuilder builder) {
        this.builder = builder;
    }

    /**
     * Creates a new {@link MutableRegistrator}
     * @return The registrator
     */
    public <T> MutableRegistrator<T> asMutable(final Class<T> target) {
        return new MutableRegistrator<>(this.builder, target);
    }

    /**
     * Creates a new {@link ImmutableRegistrator}
     * @return The registrator
     */
    public <T> ImmutableRegistrator<T> asImmutable(final Class<T> target) {
        return new ImmutableRegistrator<>(this.builder, target);
    }

    public static final class MutableRegistrator<T> extends DataProviderRegistrator {

        private final Class<T> target;

        public MutableRegistrator(final DataProviderRegistratorBuilder builder, final Class<T> target) {
            super(builder);
            this.target = target;
        }

        /**
         * Creates a new {@link ImmutableRegistration} and registers it
         * @param suppliedKey The key supplier
         * @param <K> The key type
         * @return The registration
         */
        public <K> MutableRegistration<K, T> create(final Supplier<? extends Key<? extends Value<K>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        /**
         * Creates a new {@link ImmutableRegistration} and registers it
         * @param key The key
         * @param <K> The key type
         * @return The registration
         */
        public <K> MutableRegistration<K, T> create(final Key<? extends Value<K>> key) {
            final MutableRegistration<K, T> registration = new MutableRegistration<>(this, key);
            this.register(registration);
            return registration;
        }

        @SuppressWarnings({"unchecked", "UnstableApiUsage"})
        protected <K> MutableRegistrator<T> register(final MutableRegistration<K, T> registration) {
            this.builder.register(
                    new GenericMutableDataProvider<T, K>(registration.key, this.target) {
                        final boolean isBooleanKey = registration.key.getElementToken().getRawType() == Boolean.class;

                        @Override
                        protected Value<K> constructValue(final T dataHolder, final K element) {
                            if (registration.constructValue != null) {
                                return registration.constructValue.apply(dataHolder, element);
                            }
                            return super.constructValue(dataHolder, element);
                        }

                        @Override
                        protected Optional<K> getFrom(final T dataHolder) {
                            if (this.isBooleanKey) {
                                return (Optional<K>) OptBool.of((Boolean) registration.get.apply(dataHolder));
                            }
                            return Optional.ofNullable(registration.get.apply(dataHolder));
                        }

                        @Override
                        protected boolean set(final T dataHolder, final K value) {
                            if (registration.setAnd != null) {
                                return registration.setAnd.apply(dataHolder, value);
                            }
                            if (registration.set != null) {
                                registration.set.accept(dataHolder, value);
                                return true;
                            }
                            return super.set(dataHolder, value);
                        }

                        @Override
                        protected boolean delete(final T dataHolder) {
                            if (registration.deleteAnd != null) {
                                return registration.deleteAnd.apply(dataHolder);
                            }
                            if (registration.delete != null) {
                                registration.delete.accept(dataHolder);
                                return true;
                            }
                            return super.delete(dataHolder);
                        }

                        @Override
                        protected DataTransactionResult setAndGetResult(final T dataHolder, final K value) {
                            if (registration.setAndGet != null) {
                                return registration.setAndGet.apply(dataHolder, value);
                            }
                            return super.setAndGetResult(dataHolder, value);
                        }

                        @Override
                        protected DataTransactionResult deleteAndGetResult(final T dataHolder) {
                            if (registration.deleteAndGet != null) {
                                return registration.deleteAndGet.apply(dataHolder);
                            }
                            return super.deleteAndGetResult(dataHolder);
                        }

                        @Override
                        protected boolean supports(final T dataHolder) {
                            if (registration.supports != null) {
                                registration.supports.apply(dataHolder);
                            }
                            return super.supports(dataHolder);
                        }
                    }
            );
            return this;
        }
    }

    public static final class ImmutableRegistrator<T> extends DataProviderRegistrator {

        private final Class<T> target;

        public ImmutableRegistrator(final DataProviderRegistratorBuilder builder, final Class<T> target) {
            super(builder);
            this.target = target;
        }

        /**
         * Creates a new {@link ImmutableRegistration} and registers it
         * @param suppliedKey The key supplier
         * @param <K> The key type
         * @return The registration
         */
        public <K> ImmutableRegistration<K, T> create(final Supplier<? extends Key<? extends Value<K>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        /**
         * Creates a new {@link ImmutableRegistration} and registers it
         * @param key The key
         * @param <K> The key type
         * @return The registration
         */
        public <K> ImmutableRegistration<K, T> create(final Key<? extends Value<K>> key) {
            final ImmutableRegistration<K, T> registration = new ImmutableRegistration<>(this, key);
            this.register(registration);
            return registration;
        }

        @SuppressWarnings({"unchecked", "UnstableApiUsage"})
        protected <K> ImmutableRegistrator<T> register(final ImmutableRegistration<K, T> registration) {
            this.builder.register(
                    new GenericImmutableDataProvider<T, K>(registration.key, this.target) {
                        final boolean isBooleanKey = registration.key.getElementToken().getRawType() == Boolean.class;

                        @Override
                        protected Value<K> constructValue(final T dataHolder, final K element) {
                            if (registration.constructValue != null) {
                                return registration.constructValue.apply(dataHolder, element);
                            }
                            return super.constructValue(dataHolder, element);
                        }

                        @Override
                        protected Optional<K> getFrom(final T dataHolder) {
                            if (this.isBooleanKey) {
                                return (Optional<K>) OptBool.of((Boolean) registration.get.apply(dataHolder));
                            }
                            return Optional.ofNullable(registration.get.apply(dataHolder));
                        }

                        @Override
                        protected Optional<T> set(final T dataHolder, final K value) {
                            if (this.isBooleanKey) {
                                return (Optional<T>) OptBool.of((Boolean) registration.set.apply(dataHolder, value));
                            }
                            return Optional.ofNullable(registration.set.apply(dataHolder, value));
                        }

                        @Override
                        protected boolean supports(final T dataHolder) {
                            if (registration.supports != null) {
                                registration.supports.apply(dataHolder);
                            }
                            return super.supports(dataHolder);
                        }
                    }
            );
            return this;
        }
    }

    public static final class MutableRegistration<K, T> {

        private final MutableRegistrator<T> registrator;
        private final Key<? extends Value<K>> key;
        @Nullable private BiFunction<T, K, Value<K>> constructValue;
        @Nullable private Function<T, K> get;
        @Nullable private BiFunction<T, K, Boolean> setAnd;
        @Nullable private BiConsumer<T, K> set;
        @Nullable private Function<T, Boolean> deleteAnd;
        @Nullable private Consumer<T> delete;
        @Nullable private Function<T, DataTransactionResult> deleteAndGet;
        @Nullable private BiFunction<T, K, DataTransactionResult> setAndGet;
        @Nullable private Function<T, Boolean> supports;

        private MutableRegistration(final MutableRegistrator<T> registrator, final Key<? extends Value<K>> key) {
            this.registrator = registrator;
            this.key = key;
        }

        public MutableRegistration<K, T> constructValue(final BiFunction<T, K, Value<K>> constructValue) {
            this.constructValue = constructValue;
            return this;
        }

        public MutableRegistration<K, T> get(final Function<T, K> get) {
            this.get = get;
            return this;
        }

        public MutableRegistration<K, T> set(final BiConsumer<T, K> set) {
            this.set = set;
            return this;
        }

        public MutableRegistration<K, T> setAnd(final BiFunction<T, K, Boolean> setAnd) {
            this.setAnd = setAnd;
            return this;
        }

        public MutableRegistration<K, T> delete(final Consumer<T> delete) {
            this.delete = delete;
            return this;
        }

        public MutableRegistration<K, T> deleteAnd(final Function<T, Boolean> deleteAnd) {
            this.deleteAnd = deleteAnd;
            return this;
        }

        public MutableRegistration<K, T> deleteAndGet(final Function<T, DataTransactionResult> deleteAndGet) {
            this.deleteAndGet = deleteAndGet;
            return this;
        }

        public MutableRegistration<K, T> setAndGet(final BiFunction<T, K, DataTransactionResult> setAndGet) {
            this.setAndGet = setAndGet;
            return this;
        }

        public MutableRegistration<K, T> supports(final Function<T, Boolean> supports) {
            this.supports = supports;
            return this;
        }

        public <NK> MutableRegistration<NK, T> create(final Supplier<? extends Key<? extends Value<NK>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        public <NK> MutableRegistration<NK, T> create(final Key<? extends Value<NK>> key) {
            final MutableRegistration<NK, T> registration = new MutableRegistration<>(this.registrator, key);
            this.registrator.register(registration);
            return registration;
        }

        /**
         * Creates a new {@link MutableRegistrator}
         * @return The registrator
         */
        public <NT> MutableRegistrator<NT> asMutable(final Class<NT> target) {
            return new MutableRegistrator<>(this.registrator.builder, target);
        }

        /**
         * Creates a new {@link ImmutableRegistrator}
         * @return The registrator
         */
        public <NT> ImmutableRegistrator<NT> asImmutable(final Class<NT> target) {
            return new ImmutableRegistrator<>(this.registrator.builder, target);
        }
    }

    public static final class ImmutableRegistration<K, T> {

        private final ImmutableRegistrator<T> registrator;
        private final Key<? extends Value<K>> key;
        @Nullable private BiFunction<T, K, Value<K>> constructValue;
        @Nullable private Function<T, K> get;
        @Nullable private BiFunction<T, K, T> set;
        @Nullable private Function<T, Boolean> supports;

        private ImmutableRegistration(final ImmutableRegistrator<T> registrator, final Key<? extends Value<K>> key) {
            this.registrator = registrator;
            this.key = key;
        }

        public ImmutableRegistration<K, T> constructValue(final BiFunction<T, K, Value<K>> constructValue) {
            this.constructValue = constructValue;
            return this;
        }

        public ImmutableRegistration<K, T> get(final Function<T, K> get) {
            this.get = get;
            return this;
        }

        public ImmutableRegistration<K, T> set(final BiFunction<T, K, T> set) {
            this.set = set;
            return this;
        }

        public ImmutableRegistration<K, T> supports(final Function<T, Boolean> supports) {
            this.supports = supports;
            return this;
        }

        public <NK> ImmutableRegistration<NK, T> create(final Supplier<? extends Key<? extends Value<NK>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        public <NK> ImmutableRegistration<NK, T> create(final Key<? extends Value<NK>> key) {
            final ImmutableRegistration<NK, T> registration = new ImmutableRegistration<>(this.registrator, key);
            this.registrator.register(registration);
            return registration;
        }

        /**
         * Creates a new {@link MutableRegistrator}
         * @return The registrator
         */
        public <NT> MutableRegistrator<NT> asMutable(final Class<NT> target) {
            return new MutableRegistrator<>(this.registrator.builder, target);
        }

        /**
         * Creates a new {@link ImmutableRegistrator}
         * @return The registrator
         */
        public <NT> ImmutableRegistrator<NT> asImmutable(final Class<NT> target) {
            return new ImmutableRegistrator<>(this.registrator.builder, target);
        }
    }
}
