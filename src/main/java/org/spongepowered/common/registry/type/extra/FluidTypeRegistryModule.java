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
package org.spongepowered.common.registry.type.extra;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.data.type.SpongeCommonFluidType;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(BlockTypeRegistryModule.class)
public final class FluidTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<FluidType> {

    public static FluidTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(FluidTypes.class)
    private final Map<String, FluidType> fluidTypeMap = new HashMap<>();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(FluidType extraCatalog) {

    }

    public void registerForge(FluidType fluidType) {
        checkNotNull(fluidType, "Someone is registering a null FluidType!");
        this.fluidTypeMap.put(fluidType.getId(), fluidType);
    }

    @Override
    public Optional<FluidType> getById(String id) {
        return Optional.ofNullable(this.fluidTypeMap.get(checkNotNull(id, "FluidType id cannot be null!").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<FluidType> getAll() {
        return ImmutableSet.copyOf(this.fluidTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        if (!this.fluidTypeMap.containsKey("water")) {
            this.fluidTypeMap.put("water", new SpongeCommonFluidType("water", BlockTypes.WATER));
        }
        if (!this.fluidTypeMap.containsKey("lava")) {
            this.fluidTypeMap.put("lava", new SpongeCommonFluidType("lava", BlockTypes.LAVA));
        }
    }

    FluidTypeRegistryModule() {
    }

    private static final class Holder {
        static final FluidTypeRegistryModule INSTANCE = new FluidTypeRegistryModule();
    }
}
