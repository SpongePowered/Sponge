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
import net.minecraft.entity.passive.HorseArmorType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.List;

@Mixin(EntityHorse.class)
public abstract class MixinEntityHorse extends MixinEntityAnimal implements Horse {

    @Shadow public abstract HorseArmorType shadow$getType();

    @Override
    public Translation getTranslation() {
        final HorseData horseData = getHorseData();
        final HorseVariant horseVariant = horseData.variant().get();
        return horseVariant.getTranslation();
    }

    @Override
    public HorseData getHorseData() {
        return new SpongeHorseData(HorseUtils.getHorseColor((EntityHorse) (Object) this),
                HorseUtils.getHorseStyle((EntityHorse) (Object) this),
                HorseUtils.getHorseVariant(this.shadow$getType()));
    }

    @Override
    public Value<HorseVariant> variant() {
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
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getHorseData());
    }
}
