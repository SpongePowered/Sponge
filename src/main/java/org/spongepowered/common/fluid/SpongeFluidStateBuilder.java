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
package org.spongepowered.common.fluid;

import static net.minecraft.commands.arguments.blocks.BlockStateParser.ERROR_INVALID_VALUE;
import static org.spongepowered.common.util.DataUtil.checkDataExists;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Optional;

public final class SpongeFluidStateBuilder implements org.spongepowered.api.fluid.FluidState.Builder {

    private org.spongepowered.api.fluid.FluidState state = FluidTypes.EMPTY.get().defaultState();

    @Override
    public org.spongepowered.api.fluid.FluidState.@NonNull Builder fluid(final @NonNull FluidType fluidType) {
        this.state = Objects.requireNonNull(fluidType).defaultState();
        return this;
    }

    @Override
    public org.spongepowered.api.fluid.FluidState.@NonNull Builder fromString(final @NonNull String id) {
        this.state = this.parseString(id);
        return this;
    }

    @Override
    public <V> org.spongepowered.api.fluid.FluidState.@NonNull Builder add(final @NonNull Key<@NonNull ? extends Value<V>> key, final @NonNull V value) {
        Objects.requireNonNull(this.state, "The fluid type must be set first");
        Objects.requireNonNull(key, "The key must not be null");
        Objects.requireNonNull(key, "The value must not be null");
        this.state = this.state.with(key, value).orElse(this.state);
        return this;
    }

    @Override
    public org.spongepowered.api.fluid.FluidState.@NonNull Builder from(final org.spongepowered.api.fluid.@NonNull FluidState holder) {
        this.state = holder;
        return this;
    }

    @Override
    public org.spongepowered.api.fluid.@NonNull FluidState build() {
        Objects.requireNonNull(this.state, "There must be a FluidType specified.");
        return this.state;
    }

    @Override
    public org.spongepowered.api.fluid.FluidState.@NonNull Builder reset() {
        this.state = null;
        return this;
    }

    @Override
    public @NonNull Optional<org.spongepowered.api.fluid.FluidState> build(final @NonNull DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Fluids.FLUID_STATE)) {
            return Optional.empty();
        }
        checkDataExists(container, Constants.Fluids.FLUID_STATE);
        try {
            return container.getString(Constants.Fluids.FLUID_STATE).map(this::parseString);
        } catch (final Exception e) {
            throw new InvalidDataException("Could not retrieve a blockstate!", e);
        }
    }

    // The following is adapted from BlockStateParser

    private org.spongepowered.api.fluid.FluidState parseString(final String string) {
        final StringReader reader = new StringReader(string);
        try {
            final Fluid fluid = this.readFluid(reader);
            final FluidState state;
            if (reader.canRead() && reader.peek() == '[') {
                state = this.readProperties(reader, fluid);
            } else {
                state = fluid.defaultFluidState();
            }
            return (org.spongepowered.api.fluid.FluidState) (Object) state;
        } catch (final CommandSyntaxException e) {
            throw new IllegalArgumentException("The string " + string + " doe not parse into a valid FluidState", e);
        }
    }

    private Fluid readFluid(final StringReader reader) throws CommandSyntaxException {
        final int cursor = reader.getCursor();
        final ResourceLocation fluidKey = ResourceLocation.read(reader);
        final Registry<Fluid> fluidRegistry = SpongeCommon.vanillaRegistry(Registries.FLUID);
        return fluidRegistry.getOptional(fluidKey).orElseThrow(() -> {
            reader.setCursor(cursor);
            return BlockStateParser.ERROR_UNKNOWN_BLOCK.createWithContext(reader, fluidKey.toString());
        });
    }

    private net.minecraft.world.level.material.FluidState readProperties(final StringReader reader, final Fluid fluid) throws CommandSyntaxException {
        reader.skip();
        reader.skipWhitespace();

        FluidState state = fluid.defaultFluidState();
        final StateDefinition<?, ?> stateContainer = fluid.getStateDefinition();
        while(reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace();
            final int cursor = reader.getCursor();
            final String propertyKey = reader.readString();
            final Property<?> property = stateContainer.getProperty(propertyKey);
            if (property == null) {
                reader.setCursor(cursor);
                throw BlockStateParser.ERROR_UNKNOWN_PROPERTY.createWithContext(reader, fluid.toString(), propertyKey);
            }

            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() == '=') {
                reader.skip();
                reader.skipWhitespace();
                final int cursor1 = reader.getCursor();
                state = this.parseValue(state, reader, property, cursor1);
                reader.skipWhitespace();
                if (!reader.canRead()) {
                    continue;
                }

                if (reader.peek() == ',') {
                    reader.skip();
                    continue;
                }

                if (reader.peek() != ']') {
                    throw BlockStateParser.ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(reader);
                }
                break;
            }

            throw BlockStateParser.ERROR_EXPECTED_VALUE.createWithContext(reader, fluid.toString(), propertyKey);
        }

        if (reader.canRead()) {
            reader.skip();
        } else {
            throw BlockStateParser.ERROR_EXPECTED_END_OF_PROPERTIES.createWithContext(reader);
        }

        return (net.minecraft.world.level.material.FluidState) state;
    }

    private <T extends Comparable<T>> FluidState parseValue(
            final FluidState state, final StringReader reader, final Property<T> property, final int cursor) throws CommandSyntaxException {
        final Optional<T> propertyValue = property.getValue(reader.readString());
        if (propertyValue.isPresent()) {
            return state.setValue(property, propertyValue.get());
        } else {
            reader.setCursor(cursor);
            throw ERROR_INVALID_VALUE.createWithContext(reader, state.getType().toString(), property.getName(), cursor);
        }
    }

}
