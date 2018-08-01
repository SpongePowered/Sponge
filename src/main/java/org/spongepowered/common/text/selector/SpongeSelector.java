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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.ArgumentType;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NonnullByDefault
public class SpongeSelector implements Selector {

    protected final SelectorType type;
    protected final ImmutableMap<ArgumentType<?>, Argument<?>> arguments;

    private final String plain;

    public SpongeSelector(SelectorType type, ImmutableMap<ArgumentType<?>, Argument<?>> arguments) {
        this.type = checkNotNull(type, "type");
        this.arguments = checkNotNull(arguments, "arguments");
        this.plain = buildString();
    }

    @Override
    public SelectorType getType() {
        return this.type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(ArgumentType<T> type) {
        Argument<T> argument = (Argument<T>) this.arguments.get(type);
        return argument != null ? Optional.of(argument.getValue()) : Optional.<T>empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Argument<T>> getArgument(ArgumentType<T> type) {
        return Optional.ofNullable((Argument<T>) this.arguments.get(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Argument.Invertible<T>> getArgument(ArgumentType.Invertible<T> type) {
        return Optional.ofNullable((Argument.Invertible<T>) this.arguments.get(type));
    }

    @Override
    public List<Argument<?>> getArguments() {
        return this.arguments.values().asList();
    }

    @Override
    public boolean has(ArgumentType<?> type) {
        return this.arguments.containsKey(type);
    }

    @Override
    public boolean isInverted(ArgumentType.Invertible<?> type) {
        if (!has(type)) {
            return false;
        }
        return ((Argument.Invertible<?>) this.arguments.get(type)).isInverted();
    }

    @Override
    public ImmutableSet<Entity> resolve(CommandSource origin) {
        return new SelectorResolver(this, origin).resolve();
    }

    @Override
    public ImmutableSet<Entity> resolve(Extent... extents) {
        return resolve(ImmutableSet.copyOf(extents));
    }

    @Override
    public ImmutableSet<Entity> resolve(Collection<? extends Extent> extents) {
        return new SelectorResolver(this, extents).resolve();
    }

    @Override
    public ImmutableSet<Entity> resolve(Location<World> location) {
        Builder selector = Selector.builder().from(this);
        if (!this.has(ArgumentTypes.POSITION.x())) {
            selector.add(ArgumentTypes.POSITION.x(), location.getPosition().getFloorX());
        }
        if (!this.has(ArgumentTypes.POSITION.y())) {
            selector.add(ArgumentTypes.POSITION.y(), location.getPosition().getFloorY());
        }
        if (!this.has(ArgumentTypes.POSITION.z())) {
            selector.add(ArgumentTypes.POSITION.z(), location.getPosition().getFloorZ());
        }
        return new SelectorResolver(selector.build(), ImmutableSet.of(location.getExtent())).resolve();
    }

    @Override
    public String toPlain() {
        return this.plain;
    }

    @Override
    public Selector.Builder toBuilder() {
        return Selector.builder().from(this);
    }

    private String buildString() {
        StringBuilder result = new StringBuilder();

        result.append('@').append(this.type.getName());

        if (!this.arguments.isEmpty()) {
            Collection<Argument<?>> args = this.arguments.values();
            result.append(args.stream().map(Argument::toPlain).collect(Collectors.joining(",", "[", "]")));
        }

        return result.toString();
    }

}
