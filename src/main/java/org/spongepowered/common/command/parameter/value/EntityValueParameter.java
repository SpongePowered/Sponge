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
package org.spongepowered.common.command.parameter.value;

import com.google.common.collect.Iterables;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.impl.PatternMatchingValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedSelectorParsers;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityValueParameter extends PatternMatchingValueParameter implements CatalogedValueParameter {

    private final String id;
    private final String name;

    public EntityValueParameter(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    protected Iterable<String> getChoices(Cause cause) {
        Set<Iterable<Entity>> worldEntities = Sponge.getServer().getWorlds().stream().map(World::getEntities).collect(Collectors.toSet());
        return StreamSupport.stream(Iterables.concat(worldEntities).spliterator(), false).map(input -> {
            if (input == null) {
                return null;
            }
            if (input instanceof Player) {
                return ((Player) input).getName();
            }
            return input.getUniqueId().toString();
        }).collect(Collectors.toList());
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        UUID uuid = UUID.fromString(choice);
        for (World world : Sponge.getServer().getWorlds()) {
            Optional<Entity> ret = world.getEntity(uuid);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        throw new IllegalArgumentException("Input value " + choice + " was not an entity");
    }

    @Override
    public Optional<?> parseSelector(Cause cause, String selector, CommandContext context, Function<Text, ArgumentParseException> errorFunction)
            throws ArgumentParseException {
        return CatalogedSelectorParsers.ENTITIES.parseSelector(cause, selector, context, errorFunction);
    }
}
