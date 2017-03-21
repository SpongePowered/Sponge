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

import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

@Mixin(AbstractHorse.class)
@SuppressWarnings("deprecation")
@Implements(@Interface(iface = Horse.class, prefix = "horse$", unique = true))
public abstract class MixinAbstractHorse extends MixinEntityAnimal implements Horse {

    @Override
    public HorseData getHorseData() {
        printDeprecatedHorseUsage("HorseData is not applicable to Horse, only Rideable Horse!");
        return new SpongeHorseData(DataConstants.Horse.DEFAULT_COLOR, DataConstants.Horse.DEFAULT_STYLE, DataConstants.Horse.DEFAULT_VARIANT);
    }

    @Override
    public Value<org.spongepowered.api.data.type.HorseVariant> variant() {
        printDeprecatedHorseUsage("HorseVariant is no longer applicable to all horses! HorseVariants cannot be changed!");
        return new SpongeValue<>(Keys.HORSE_VARIANT, DataConstants.Horse.DEFAULT_VARIANT);
    }

    @Override
    public Value<HorseStyle> style() {
        printDeprecatedHorseUsage("HorseStyle is only applicable to RideableHorses!");
        return new SpongeValue<>(Keys.HORSE_STYLE, DataConstants.Horse.DEFAULT_STYLE);
    }

    @Override
    public Value<HorseColor> color() {
        printDeprecatedHorseUsage("HorseColor is only applicable to RideableHorses!");
        return new SpongeValue<>(Keys.HORSE_COLOR, DataConstants.Horse.DEFAULT_COLOR);
    }

    @Unique
    protected void printDeprecatedHorseUsage(String specificSubHeader) {
        new PrettyPrinter(60).add("Deprecated Usage Detected").centre().hr()
                .add(specificSubHeader)
                .addWrapped(50,"Usage of these Horse related methods are no longer supported\n"
                               + "while they work technically for data retrieval, setting\n"
                               + "this unsupported data is no longer possible. Please \n"
                               + "notify the plugin developer using these methods!")
                .add(new UnsupportedOperationException("Deprecated Usage Detected"))
                .trace();
    }
}
