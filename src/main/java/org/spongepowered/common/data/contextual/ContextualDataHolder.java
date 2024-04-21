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
package org.spongepowered.common.data.contextual;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPerspective;
import org.spongepowered.api.data.DataPerspectiveResolver;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.world.World;
import org.spongepowered.plugin.PluginContainer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public final class ContextualDataHolder implements ContextualData {

    private final DataHolder dataHolder;

    private final Map<DataPerspective, PerspectiveContainer<?, ?>> perspectives;

    public ContextualDataHolder(final DataHolder dataHolder) {
        this.dataHolder = dataHolder;

        this.perspectives = Maps.newHashMap();
    }

    public PerspectiveContainer<?, ?> createDataPerception(final DataPerspective perspective) {
        return this.perspectives.computeIfAbsent(perspective, p -> {
            if (p instanceof final Entity entity) {
                return new EntityPerspectiveContainer(this, entity);
            } else if (p instanceof final Team team) {
                return new TeamPerspectiveContainer(this, team);
            } else if (p instanceof final World<?, ?> world) {
                return new WorldPerspectiveContainer(this, world);
            }
            throw new UnsupportedOperationException();
        });
    }

    public DataHolder.Mutable createDataPerception(final PluginContainer plugin, final DataPerspective perspective) {
        return new ContextualDataProvider(this.createDataPerception(perspective), plugin);
    }

    public @Nullable ValueContainer get(final DataPerspective perspective) {
        return this.perspectives.get(perspective);
    }

    private static final class EntityPerspectiveContainer extends PerspectiveContainer<ContextualDataHolder, Entity> {

        private final Map<Key<?>, EnumMap<PerspectiveType, Object>> perspectiveValues;

        private EntityPerspectiveContainer(final ContextualDataHolder holder, final Entity entity) {
            super(PerspectiveType.ENTITY, holder, entity);

            this.perspectiveValues = Maps.newHashMap();
        }

        private <E> EnumMap<PerspectiveType, E> getValues(final Key<? extends Value<E>> key) {
            return (EnumMap<PerspectiveType, E>) this.perspectiveValues.computeIfAbsent(key, k -> Maps.newEnumMap(PerspectiveType.class));
        }

        @Override
        protected <E> void offer(final PerspectiveType perspectiveType, final DataPerspectiveResolver<Value<E>, E> resolver, final E value) {
            final EnumMap<PerspectiveType, E> values = this.getValues(resolver.key());
            values.put(perspectiveType, value);
            this.updatePerspective(values, resolver);
        }

        private <E> void updatePerspective(final EnumMap<PerspectiveType, E> values, final DataPerspectiveResolver<Value<E>, E> resolver) {
            for (final PerspectiveType type : PerspectiveType.values()) {
                final @Nullable E value = values.get(type);
                if (value == null) {
                    continue;
                }

                if (!Objects.equals(this.activeValues.put(resolver.key(), value), value)) {
                    resolver.apply(this.holder.dataHolder, this.perspective, value);
                }

                return;
            }
        }
    }

    private static abstract class NonEntityContainer<P extends DataPerspective> extends PerspectiveContainer<ContextualDataHolder, P> {

        protected NonEntityContainer(final PerspectiveType perspectiveType, final ContextualDataHolder holder, final P perspective) {
            super(perspectiveType, holder, perspective);
        }

        @Override
        protected <E> void offer(final PerspectiveType perspectiveType, final DataPerspectiveResolver<Value<E>, E> resolver, final E value) {
            if (Objects.equals(this.activeValues.put(resolver.key(), value), value)) {
                return;
            }

            for (final DataPerspective perspective : this.perspective.perceives()) {
                this.holder.createDataPerception(perspective).offer(perspectiveType, this.perspective, resolver, value);
            }
        }
    }

    private static final class TeamPerspectiveContainer extends NonEntityContainer<Team> {

        private TeamPerspectiveContainer(final ContextualDataHolder holder, final Team team) {
            super(PerspectiveType.TEAM, holder, team);
        }
    }

    private static final class WorldPerspectiveContainer extends NonEntityContainer<World<?, ?>> {

        private WorldPerspectiveContainer(final ContextualDataHolder holder, final World<?, ?> world) {
            super(PerspectiveType.WORLD, holder, world);
        }
    }
}
