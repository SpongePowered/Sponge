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
package org.spongepowered.common.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.animal.RideableHorse;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.List;

@SuppressWarnings("deprecation")
@Mixin(EntityHorse.class)
@Implements(@Interface(iface = RideableHorse.class, prefix = "rideableHorse$", unique = true))
public abstract class MixinEntityHorse extends MixinAbstractHorse implements RideableHorse {

    @Override
    public Translation getTranslation() {
        final HorseData horseData = getHorseData();
        final org.spongepowered.api.data.type.HorseVariant horseVariant = horseData.variant().get();
        return horseVariant.getTranslation();
    }

    @Override
    public Value<org.spongepowered.api.data.type.HorseVariant> variant() {
        return new SpongeValue<>(Keys.HORSE_VARIANT, DataConstants.Horse.DEFAULT_VARIANT);
    }

    @Override
    public Value<HorseStyle> style() {
        return new SpongeValue<>(Keys.HORSE_STYLE, DataConstants.Horse.DEFAULT_STYLE, HorseUtils.getHorseStyle((EntityHorse) (Object) this));
    }

    @Override
    public Value<HorseColor> color() {
        return new SpongeValue<>(Keys.HORSE_COLOR, DataConstants.Horse.DEFAULT_COLOR, HorseUtils.getHorseColor((EntityHorse) (Object) this));
    }

    @Override
    public HorseData getHorseData() {
        return new SpongeHorseData(HorseUtils.getHorseColor((EntityHorse) (Object) this), HorseUtils.getHorseStyle((EntityHorse) (Object) this),
            org.spongepowered.api.data.type.HorseVariants.HORSE);
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getHorseData());
    }
}
