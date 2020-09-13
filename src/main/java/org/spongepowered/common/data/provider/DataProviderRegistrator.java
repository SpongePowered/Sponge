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

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.ImmutableDataProviderBuilder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.MutableDataProviderBuilder;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.copy.CopyHelper;

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
        public <K> MutableRegistration<T, K> create(final Supplier<? extends Key<? extends Value<K>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        /**
         * Creates a new {@link ImmutableRegistration} and registers it
         * @param key The key
         * @param <K> The key type
         * @return The registration
         */
        public <K> MutableRegistration<T, K> create(final Key<? extends Value<K>> key) {
            final MutableRegistration<T, K> registration = new MutableRegistration<>(this, key);
            this.register(registration);
            return registration;
        }

        @SuppressWarnings({"unchecked", "UnstableApiUsage"})
        protected <K, V extends Value<K>> MutableRegistrator<T> register(final MutableRegistration<T, K> registration) {
            this.builder.register(registration.build(target));
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
        public <K> ImmutableRegistration<T, K> create(final Supplier<? extends Key<? extends Value<K>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        /**
         * Creates a new {@link ImmutableRegistration} and registers it
         * @param key The key
         * @param <K> The key type
         * @return The registration
         */
        public <K> ImmutableRegistration<T, K> create(final Key<? extends Value<K>> key) {
            final ImmutableRegistration<T, K> registration = new ImmutableRegistration<>(this, key);
            this.register(registration);
            return registration;
        }

        @SuppressWarnings({"unchecked", "UnstableApiUsage"})
        protected <K, V> ImmutableRegistrator<T> register(final ImmutableRegistration<T, K> registration) {
            this.builder.register(registration.build(this.target));
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    private static class MutableRegistrationBase<H, E, R extends MutableRegistrationBase<H, E, R>> {

        private final Key<? extends Value<E>> key;
        @Nullable private BiFunction<H, E, Value<E>> constructValue;
        @Nullable private Function<H, E> get;
        @Nullable private BiFunction<H, E, Boolean> setAnd;
        @Nullable private BiConsumer<H, E> set;
        @Nullable private Function<H, Boolean> deleteAnd;
        @Nullable private Consumer<H> delete;
        @Nullable private Function<H, DataTransactionResult> deleteAndGet;
        @Nullable private Function<H, E> resetOnDelete;
        @Nullable private BiFunction<H, E, DataTransactionResult> setAndGet;
        @Nullable private Function<H, Boolean> supports;

        public MutableRegistrationBase(Key<? extends Value<E>> key) {
            this.key = key;
        }

        public R constructValue(final BiFunction<H, E, Value<E>> constructValue) {
            this.constructValue = constructValue;
            return (R) this;
        }

        public R get(final Function<H, E> get) {
            this.get = get;
            return (R) this;
        }

        public R set(final BiConsumer<H, E> set) {
            this.set = set;
            return (R) this;
        }

        public R setAnd(final BiFunction<H, E, Boolean> setAnd) {
            this.setAnd = setAnd;
            return (R) this;
        }

        public R delete(final Consumer<H> delete) {
            this.delete = delete;
            return (R) this;
        }

        public R deleteAnd(final Function<H, Boolean> deleteAnd) {
            this.deleteAnd = deleteAnd;
            return (R) this;
        }

        public R deleteAndGet(final Function<H, DataTransactionResult> deleteAndGet) {
            this.deleteAndGet = deleteAndGet;
            return (R) this;
        }

        public R resetOnDelete(final E value) {
            return this.resetOnDelete(CopyHelper.createSupplier(value));
        }

        public R resetOnDelete(final Supplier<E> resetOnDeleteTo) {
            return this.resetOnDelete(h -> resetOnDeleteTo.get());
        }

        public R resetOnDelete(final Function<H, E> resetOnDeleteTo) {
            this.resetOnDelete = resetOnDeleteTo;
            return (R) this;
        }

        public R setAndGet(final BiFunction<H, E, DataTransactionResult> setAndGet) {
            this.setAndGet = setAndGet;
            return (R) this;
        }

        public R supports(final Function<H, Boolean> supports) {
            this.supports = supports;
            return (R) this;
        }

        public DataProvider<?, ?> build(Class<H> target) {
            final MutableRegistrationBase<H, E, R> registration = this;
            return new GenericMutableDataProvider<H, E>(registration.key, target) {
                final boolean isBooleanKey = registration.key.getElementToken().getRawType() == Boolean.class;

                @Override
                protected Value<E> constructValue(final H dataHolder, final E element) {
                    if (registration.constructValue != null) {
                        return registration.constructValue.apply(dataHolder, element);
                    }
                    return super.constructValue(dataHolder, element);
                }

                @Override
                protected Optional<E> getFrom(final H dataHolder) {
                    if (this.isBooleanKey) {
                        return (Optional<E>) OptBool.of((Boolean) registration.get.apply(dataHolder));
                    }
                    return Optional.ofNullable(registration.get.apply(dataHolder));
                }

                @Override
                protected boolean set(final H dataHolder, final E value) {
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
                protected boolean delete(final H dataHolder) {
                    if (registration.deleteAnd != null) {
                        return registration.deleteAnd.apply(dataHolder);
                    }
                    if (registration.delete != null) {
                        registration.delete.accept(dataHolder);
                        return true;
                    }
                    if (registration.resetOnDelete != null) {
                        return this.set(dataHolder, registration.resetOnDelete.apply(dataHolder));
                    }
                    return super.delete(dataHolder);
                }

                @Override
                protected DataTransactionResult setAndGetResult(final H dataHolder, final E value) {
                    if (registration.setAndGet != null) {
                        return registration.setAndGet.apply(dataHolder, value);
                    }
                    return super.setAndGetResult(dataHolder, value);
                }

                @Override
                protected DataTransactionResult deleteAndGetResult(final H dataHolder) {
                    if (registration.deleteAndGet != null) {
                        return registration.deleteAndGet.apply(dataHolder);
                    }
                    if (registration.resetOnDelete != null) {
                        return setAndGetResult(dataHolder, registration.resetOnDelete.apply(dataHolder));
                    }
                    return super.deleteAndGetResult(dataHolder);
                }

                @Override
                protected boolean supports(final H dataHolder) {
                    if (registration.supports != null) {
                        return registration.supports.apply(dataHolder);
                    }
                    return super.supports(dataHolder);
                }
            };


        }

    }

    public static final class MutableRegistration<H, E> extends MutableRegistrationBase<H, E, MutableRegistration<H, E>> {

        private final MutableRegistrator<H> registrator;

        private MutableRegistration(final MutableRegistrator<H> registrator, final Key<? extends Value<E>> key) {
            super(key);
            this.registrator = registrator;
        }

        public <NE> MutableRegistration<H, NE> create(final Supplier<? extends Key<? extends Value<NE>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        public <NE> MutableRegistration<H, NE> create(final Key<? extends Value<NE>> key) {
            final MutableRegistration<H, NE> registration = new MutableRegistration<>(this.registrator, key);
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

    @SuppressWarnings("unchecked")
    private static class ImmutableRegistrationBase<H, E, R extends ImmutableRegistrationBase<H, E, R>> {
        private final Key<? extends Value<E>> key;
        @Nullable private BiFunction<H, E, Value<E>> constructValue;
        @Nullable private Function<H, E> get;
        @Nullable private BiFunction<H, E, H> set;
        @Nullable private Function<H, Boolean> supports;

        public ImmutableRegistrationBase(Key<? extends Value<E>> key) {
            this.key = key;
        }

        public R constructValue(final BiFunction<H, E, Value<E>> constructValue) {
            this.constructValue = constructValue;
            return (R) this;
        }

        public R get(final Function<H, E> get) {
            this.get = get;
            return (R) this;
        }

        public R set(final BiFunction<H, E, H> set) {
            this.set = set;
            return (R) this;
        }

        public R supports(final Function<H, Boolean> supports) {
            this.supports = supports;
            return (R) this;
        }

        public DataProvider<?, ?> build(Class<H> target) {
            final ImmutableRegistrationBase<H, E, R> registration = this;
            return new GenericImmutableDataProvider<H, E>(registration.key, target) {
                final boolean isBooleanKey = registration.key.getElementToken().getRawType() == Boolean.class;

                @Override
                protected Value<E> constructValue(final H dataHolder, final E element) {
                    if (registration.constructValue != null) {
                        return registration.constructValue.apply(dataHolder, element);
                    }
                    return super.constructValue(dataHolder, element);
                }

                @Override
                protected Optional<E> getFrom(final H dataHolder) {
                    if (this.isBooleanKey) {
                        return (Optional<E>) OptBool.of((Boolean) registration.get.apply(dataHolder));
                    }
                    return Optional.ofNullable(registration.get.apply(dataHolder));
                }

                @Override
                protected Optional<H> set(final H dataHolder, final E value) {
                    return Optional.ofNullable(registration.set.apply(dataHolder, value));
                }

                @Override
                protected boolean supports(final H dataHolder) {
                    if (registration.supports != null) {
                        return registration.supports.apply(dataHolder);
                    }
                    return super.supports(dataHolder);
                }
            };

        }

    }

    public static final class ImmutableRegistration<H, E> extends ImmutableRegistrationBase<H, E, ImmutableRegistration<H, E>> {

        private final ImmutableRegistrator<H> registrator;

        private ImmutableRegistration(final ImmutableRegistrator<H> registrator, final Key<? extends Value<E>> key) {
            super(key);
            this.registrator = registrator;
        }

        public <NE> ImmutableRegistration<H, NE> create(final Supplier<? extends Key<? extends Value<NE>>> suppliedKey) {
            return this.create(suppliedKey.get());
        }

        public <NE> ImmutableRegistration<H, NE> create(final Key<? extends Value<NE>> key) {
            final ImmutableRegistration<H, NE> registration = new ImmutableRegistration<>(this.registrator, key);
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

    public static class SpongeImmutableDataProviderBuilder<H extends DataHolder, V extends Value<E>, E, R extends ImmutableRegistrationBase<H, E, R>> implements ImmutableDataProviderBuilder<H, V, E> {

        private ImmutableRegistrationBase<H, E, R> registration;
        private TypeToken<H> holder;

        @Override
        public <NV extends Value<NE>, NE> ImmutableDataProviderBuilder<H, NV, NE> key(Key<NV> key) {
            this.registration = new ImmutableRegistrationBase(key);
            return (SpongeImmutableDataProviderBuilder) this;
        }

        @Override
        public  <NH extends H> ImmutableDataProviderBuilder<NH, V, E> dataHolder(TypeToken<NH> holder) {
            this.holder = (TypeToken) holder;
            return (SpongeImmutableDataProviderBuilder) this;
        }

        @Override
        public ImmutableDataProviderBuilder<H, V, E> get(Function<H, E> get) {
            this.registration.get(get);
            return this;
        }

        @Override
        public ImmutableDataProviderBuilder<H, V, E> set(BiFunction<H, E, H> set) {
            this.registration.set(set);
            return this;
        }

        @Override
        public ImmutableDataProviderBuilder<H, V, E> supports(Function<H, Boolean> supports) {
            this.registration.supports(supports);
            return this;
        }

        @Override
        public ImmutableDataProviderBuilder<H, V, E> reset() {
            this.registration = null;
            return this;
        }

        @Override
        public DataProvider<? extends Value<E>, E> build() {
            return this.registration.build((Class) this.holder.getRawType());
        }
    }

    public static class SpongeMutableDataProviderBuilder<H extends DataHolder.Mutable, V extends Value<E>, E, R extends MutableRegistrationBase<H, E, R>> implements MutableDataProviderBuilder<H, V, E> {

        private MutableRegistrationBase<H, E, R> registration;
        private TypeToken<H> holder;

        @Override
        public <NV extends Value<NE>, NE> MutableDataProviderBuilder<H, NV, NE> key(Key<NV> key) {
            this.registration = new MutableRegistrationBase(key);
            return (SpongeMutableDataProviderBuilder) this;
        }

        @Override
        public <NH extends H> MutableDataProviderBuilder<NH, V, E> dataHolder(TypeToken<NH> holder) {
            this.holder = (TypeToken) holder;
            return (SpongeMutableDataProviderBuilder) this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> get(Function<H, E> get) {
            this.registration.get(get);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> set(BiConsumer<H, E> set) {
            this.registration.set(set);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> setAnd(BiFunction<H, E, Boolean> setAnd) {
            this.registration.setAnd(setAnd);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> delete(Consumer<H> delete) {
            this.registration.delete(delete);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> deleteAnd(Function<H, Boolean> delete) {
            this.registration.deleteAnd(delete);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> deleteAndGet(Function<H, DataTransactionResult> delete) {
            this.registration.deleteAndGet(delete);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> resetOnDelete(Supplier<E> resetOnDeleteTo) {
            this.registration.resetOnDelete(resetOnDeleteTo);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> resetOnDelete(Function<H, E> resetOnDeleteTo) {
            this.registration.resetOnDelete(resetOnDeleteTo);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> setAndGet(BiFunction<H, E, DataTransactionResult> setAndGet) {
            this.registration.setAndGet(setAndGet);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> supports(Function<H, Boolean> supports) {
            this.registration.supports(supports);
            return this;
        }

        @Override
        public MutableDataProviderBuilder<H, V, E> reset() {
            this.registration = null;
            return this;
        }

        @Override
        public DataProvider<V, E> build() {
            return this.registration.build((Class) this.holder.getRawType());
        }
    }

}
