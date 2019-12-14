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
package org.spongepowered.common.mixin.api.mcp.entity.passive;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.mutable.DyeableData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeShearedData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;
import Mutable;
import java.util.Collection;
import net.minecraft.entity.passive.SheepEntity;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin_API extends AnimalEntityMixin_API implements Sheep {

    @Shadow public abstract net.minecraft.item.DyeColor getFleeceColor();
    @Shadow public abstract boolean getSheared();

    @Override
    public DyeableData getDyeData() {
        return new SpongeDyeableData((DyeColor) (Object) this.getFleeceColor());
    }

    @Override
    public Mutable<DyeColor> color() {
        return new SpongeValue<>(Keys.DYE_COLOR, Constants.Catalog.DEFAULT_SHEEP_COLOR, (DyeColor) (Object) this.getFleeceColor());
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super org.spongepowered.api.data.DataManipulator.Mutable<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getDyeData());
        manipulators.add(new SpongeShearedData(this.getSheared()));
    }

}
