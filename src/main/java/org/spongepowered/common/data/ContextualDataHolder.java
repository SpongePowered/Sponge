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
package org.spongepowered.common.data;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPerspective;
import org.spongepowered.api.data.DataPerspectiveResolver;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.CopyHelper;
import org.spongepowered.plugin.PluginContainer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class ContextualDataHolder {

    private final DataHolder dataHolder;
    private final Map<DataPerspective, PerspectiveContainer<?>> perspectives;

    public ContextualDataHolder(final DataHolder dataHolder) {
        this.dataHolder = dataHolder;

        this.perspectives = Maps.newHashMap();
    }

    private PerspectiveContainer<?> createDataPerception(final DataPerspective perspective) {
        return this.perspectives.computeIfAbsent(perspective, p -> {
            if (p instanceof final Entity entity) {
                return new EntityPerspectiveContainer(entity);
            } else if (p instanceof final Team team) {
                return new TeamPerspectiveContainer(team);
            } else if (p instanceof final World<?, ?> world) {
                return new WorldPerspectiveContainer(world);
            }
            throw new UnsupportedOperationException();
        });
    }

    public DataHolder.Mutable createDataPerception(final PluginContainer plugin, final DataPerspective perspective) {
        return new ContextualDataHolderProvider(plugin, this.createDataPerception(perspective));
    }

    public @Nullable ValueContainer get(final DataPerspective perspective) {
        return this.perspectives.get(perspective);
    }

    abstract class PerspectiveContainer<T extends DataPerspective> implements ValueContainer {

        protected final T perspective;

        private final Map<Key<?>, Map<PluginContainer, Object>> pluginValues;
        protected final Map<Key<?>, Object> activeValues;

        protected PerspectiveContainer(final T perspective) {
            this.perspective = perspective;

            this.pluginValues = Maps.newHashMap();
            this.activeValues = Maps.newHashMap();
        }

        <E> DataTransactionResult offer(final PluginContainer pluginContainer, final Key<? extends Value<E>> key, final E value) {
            final @Nullable DataPerspectiveResolver<Value<E>, E> resolver = SpongeDataManager.getDataPerspectiveResolverRegistry().get(key);
            if (resolver == null) {
                return DataTransactionResult.failResult(Value.immutableOf(key, value));
            }

            final Map<PluginContainer, E> map = (Map<PluginContainer, E>) this.pluginValues.computeIfAbsent(key, k -> Maps.newLinkedHashMap());
            if (Objects.equals(value, map.put(pluginContainer, value))) {
                return DataTransactionResult.successResult(Value.immutableOf(key, value));
            }

            final E mergedValue = resolver.merge(map.values());
            this.offer(key, resolver, mergedValue);
            return DataTransactionResult.successResult(Value.immutableOf(key, mergedValue));
        }

        protected abstract <E> void offer(final Key<? extends Value<E>> key, final DataPerspectiveResolver<Value<E>, E> resolver, final E value);

        @Override
        public <E> Optional<E> get(final Key<? extends Value<E>> key) {
            Objects.requireNonNull(key, "key");
            final @Nullable E value = (E) this.activeValues.get(key);
            if (value == null) {
                return ContextualDataHolder.this.dataHolder.get(key);
            }
            return Optional.of(CopyHelper.copy(value));
        }

        @Override
        public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
            Objects.requireNonNull(key, "key");
            final @Nullable E value = (E) this.activeValues.get(key);
            if (value == null) {
                return ContextualDataHolder.this.dataHolder.getValue(key);
            }
            return Optional.of(Value.genericMutableOf(key, CopyHelper.copy(value)));
        }

        @Override
        public boolean supports(Key<?> key) {
            return ContextualDataHolder.this.dataHolder.supports(key);
        }

        @Override
        public Set<Key<?>> getKeys() {
            return ContextualDataHolder.this.dataHolder.getKeys();
        }

        @Override
        public Set<Value.Immutable<?>> getValues() {
            return ContextualDataHolder.this.dataHolder.getValues();
        }
    }

    private abstract class NonEntityContainer<T extends DataPerspective> extends PerspectiveContainer<T> {

        protected NonEntityContainer(T perspective) {
            super(perspective);
        }

        protected abstract PerspectiveType type();

        protected <E> void offer(final Key<? extends Value<E>> key, final DataPerspectiveResolver<Value<E>, E> resolver, final E value) {
            if (Objects.equals(this.activeValues.put(key, value), value)) {
                return;
            }

            for (final DataPerspective perspective : this.perspective.perceives()) {
                if (!(perspective instanceof Entity entity)) {
                    continue;
                }

                final EntityPerspectiveContainer entityContainer = (EntityPerspectiveContainer) ContextualDataHolder.this.createDataPerception(entity);
                entityContainer.offer(this.type(), key, resolver, value);
            }
        }
    }

    private final class TeamPerspectiveContainer extends NonEntityContainer<Team> {

        public TeamPerspectiveContainer(final Team team) {
            super(team);
        }

        @Override
        protected PerspectiveType type() {
            return PerspectiveType.TEAM;
        }
    }

    private final class WorldPerspectiveContainer extends NonEntityContainer<World<?, ?>> {

        public WorldPerspectiveContainer(final World<?, ?> world) {
            super(world);
        }

        @Override
        protected PerspectiveType type() {
            return PerspectiveType.WORLD;
        }
    }

    private final class EntityPerspectiveContainer extends PerspectiveContainer<Entity> {

        private final Map<Key<?>, EnumMap<PerspectiveType, Object>> perspectiveValues;

        public EntityPerspectiveContainer(final Entity entity) {
            super(entity);

            this.perspectiveValues = Maps.newHashMap();
        }

        private <E> EnumMap<PerspectiveType, E> getValues(final Key<? extends Value<E>> key) {
            return (EnumMap<PerspectiveType, E>) this.perspectiveValues.computeIfAbsent(key, k -> Maps.newEnumMap(PerspectiveType.class));
        }

        @Override
        protected <E> void offer(final Key<? extends Value<E>> key, final DataPerspectiveResolver<Value<E>, E> resolver, final E value) {
            this.getValues(key).put(PerspectiveType.ENTITY, value);
            if (!Objects.equals(value, this.activeValues.put(key, value))) {
                resolver.apply(ContextualDataHolder.this.dataHolder, this.perspective, value);
            }
        }

        <E> void offer(final PerspectiveType type, final Key<? extends Value<E>> key, final DataPerspectiveResolver<Value<E>, E> resolver, final E value) {
            final EnumMap<PerspectiveType, E> values = this.getValues(key);
            values.put(type, value);
            this.updatePerspective(key, values, resolver);
        }

        private <E> void updatePerspective(final Key<? extends Value<E>> key, final EnumMap<PerspectiveType, E> values, final DataPerspectiveResolver<Value<E>, E> resolver) {
            for (final PerspectiveType type : PerspectiveType.values()) {
                final @Nullable E value = values.get(type);
                if (value == null) {
                    continue;
                }

                if (!Objects.equals(this.activeValues.put(key, value), value)) {
                    resolver.apply(ContextualDataHolder.this.dataHolder, this.perspective, value);
                }

                return;
            }
        }
    }

    private enum PerspectiveType {
        ENTITY,
        TEAM,
        WORLD
    }
}
