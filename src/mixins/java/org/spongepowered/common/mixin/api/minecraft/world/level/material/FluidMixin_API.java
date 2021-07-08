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
package org.spongepowered.common.mixin.api.minecraft.world.level.material;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.TagTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.common.util.TagUtil;

@Mixin(Fluid.class)
public abstract class FluidMixin_API implements FluidType {

    // @formatter:off
    @Shadow public abstract net.minecraft.world.level.block.state.StateDefinition<Fluid, net.minecraft.world.level.material.FluidState> shadow$getStateDefinition();
    @Shadow public abstract net.minecraft.world.level.material.FluidState shadow$defaultFluidState();
    // @formatter:on

    @Override
    public ImmutableList<FluidState> validStates() {
        return (ImmutableList) this.shadow$getStateDefinition().getPossibleStates();
    }

    @Override
    public FluidState defaultState() {
        return (FluidState) (Object) this.shadow$defaultFluidState();
    }

    @Override
    public Collection<StateProperty<?>> stateProperties() {
        return (Collection) this.shadow$getStateDefinition().getProperties();
    }

    @Override
    public Optional<StateProperty<?>> findStateProperty(final String name) {
        return Optional.ofNullable((StateProperty) this.shadow$getStateDefinition().getProperty(name));
    }

    @Override
    public TagType<FluidType> tagType() {
        return TagTypes.FLUID_TYPE.get();
    }

    @Override
    public Collection<Tag<FluidType>> tags() {
        return TagUtil.getAssociatedTags(this, RegistryTypes.FLUID_TYPE_TAGS);
    }
}
