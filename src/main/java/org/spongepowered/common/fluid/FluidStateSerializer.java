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

import net.minecraft.fluid.Fluid;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.state.StateMatcher;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.util.Tuple;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FluidStateSerializer {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Function<Map.Entry<IProperty<?>, Comparable<?>>, String> MAP_ENTRY_TO_STRING = p_apply_1_ -> {
        if (p_apply_1_ == null) {
            return "<NULL>";
        } else {
            final IProperty iproperty = p_apply_1_.getKey();
            return iproperty.getName() + "=" + iproperty.getName(p_apply_1_.getValue());
        }
    };

    public static String serialize(final FluidState state) {
        final StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Registry.FLUID.getKey((Fluid) state.getType()).toString());
        if (!((net.minecraft.block.BlockState) state).getValues().isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append(
                    ((net.minecraft.fluid.IFluidState) state).getValues()
                            .entrySet()
                            .stream()
                            .map(FluidStateSerializer.MAP_ENTRY_TO_STRING)
                            .collect(Collectors.joining(","))
            );
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    public static Optional<FluidState> deserialize(final String string) {
        final String state = Objects.requireNonNull(string, "Id cannot be null!").toLowerCase(Locale.ENGLISH);
        try {
            return Optional.of(new SpongeFluidStateBuilder().fromString(state).build());
        } catch (final Exception ex) {
            return Optional.empty();
        }
    }

}
