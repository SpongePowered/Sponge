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
package org.spongepowered.common.mixin.api.minecraft.world.level.dimension;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.holder.SpongeDataHolder;

import java.util.Objects;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin_API implements WorldType, SpongeDataHolder {

    @Nullable private Context api$context;

    @Override
    public Context context() {
        if (this.api$context == null) {
            final var location = Objects.requireNonNull(this.api$location(), "DimensionType is not registered: " + this);
            this.api$context = new Context(Context.DIMENSION_KEY, location.getPath());
        }

        return this.api$context;
    }

    @Nullable
    private ResourceLocation api$location() {
        final Registry<DimensionType> registry = SpongeCommon.vanillaRegistry(Registries.DIMENSION_TYPE);
        return registry.getKey((DimensionType) (Object) this);
    }

}
