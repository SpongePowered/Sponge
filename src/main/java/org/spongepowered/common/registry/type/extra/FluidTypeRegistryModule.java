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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.data.type.SpongeCommonFluidType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;

@RegisterCatalog(FluidTypes.class)
@RegistrationDependency(BlockTypeRegistryModule.class)
public final class FluidTypeRegistryModule extends AbstractCatalogRegistryModule<FluidType> implements SpongeAdditionalCatalogRegistryModule<FluidType> {

    public static FluidTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }


    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(FluidType extraCatalog) {

    }

    public void registerForge(FluidType fluidType) {
        checkNotNull(fluidType, "Someone is registering a null FluidType!");
        this.map.put(fluidType.getKey(), fluidType);
    }

    @Override
    public void registerDefaults() {
        final CatalogKey water = CatalogKey.minecraft("water");
        if (!this.map.containsKey(water)) {
            register(water, new SpongeCommonFluidType("water", BlockTypes.WATER));
        }
        final CatalogKey lava = CatalogKey.minecraft("lava");
        if (!this.map.containsKey(lava)) {
            register(lava, new SpongeCommonFluidType("lava", BlockTypes.LAVA));
        }
    }

    FluidTypeRegistryModule() {
    }

    private static final class Holder {
        static final FluidTypeRegistryModule INSTANCE = new FluidTypeRegistryModule();
    }
}
