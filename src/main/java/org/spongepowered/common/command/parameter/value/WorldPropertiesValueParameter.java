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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.impl.PatternMatchingValueParameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WorldPropertiesValueParameter extends PatternMatchingValueParameter implements CatalogedValueParameter {

    private final CatalogTypeValueParameter<DimensionType> dimensionParameter;

    public WorldPropertiesValueParameter(CatalogTypeValueParameter<DimensionType> dimensionParameter) {
        this.dimensionParameter = dimensionParameter;
    }

    @Override
    public String getId() {
        return "sponge:world_properties";
    }

    @Override
    public String getName() {
        return "World Properties parameter";
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context)
            throws ArgumentParseException {
        final String next = args.peek();
        if (next.startsWith("#")) {
            String specifier = next.substring(1);
            if (specifier.equalsIgnoreCase("first")) {
                args.next();
                return Sponge.getServer().getAllWorldProperties().stream().filter(input -> input != null && input.isEnabled())
                        .findFirst().map(x -> (Object) x);
            } else if (specifier.equalsIgnoreCase("me") && context.getLocation().isPresent()) {
                args.next();
                return Optional.of(context.getLocation().get().getExtent().getProperties());
            } else {
                boolean firstOnly = false;
                if (specifier.endsWith(":first")) {
                    firstOnly = true;
                    specifier = specifier.substring(0, specifier.length() - 6);
                }
                args.next();

                @SuppressWarnings("unchecked")
                final DimensionType type = (DimensionType) (this.dimensionParameter.getValues(cause, args, specifier).iterator().next());
                Iterable<WorldProperties> ret = Sponge.getGame().getServer().getAllWorldProperties().stream().filter(input -> input != null
                        && input.isEnabled() && input.getDimensionType().equals(type)).collect(Collectors.toList());
                return Optional.of(firstOnly ? ret.iterator().next() : ret);
            }
        }

        return super.getValue(cause, args, context);
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) {
        Iterable<String> choices = getCompletionChoices(cause);
        final Optional<String> nextArg = args.nextIfPresent();
        if (nextArg.isPresent()) {
            choices = StreamSupport.stream(choices.spliterator(), false).filter(input -> getFormattedPattern(nextArg.get()).matcher(input).find())
                    .collect(Collectors.toList());
        }
        return ImmutableList.copyOf(choices);
    }

    private Iterable<String> getCompletionChoices(Cause cause) {
        return Iterables.concat(getChoices(cause), ImmutableSet.of("#first", "#me"),
                Sponge.getRegistry().getAllOf(DimensionType.class).stream().map(input2 -> "#" + input2.getId()).collect(Collectors.toList()));
    }

    @Override
    protected Iterable<String> getChoices(Cause cause) {
        return Sponge.getGame().getServer().getAllWorldProperties().stream()
                .map(WorldProperties::getWorldName)
                .collect(Collectors.toList());
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        Optional<WorldProperties> ret = Sponge.getGame().getServer().getWorldProperties(choice);
        if (!ret.isPresent()) {
            throw new IllegalArgumentException("Provided argument " + choice + " did not match a WorldProperties");
        }
        return ret.get();
    }
}
