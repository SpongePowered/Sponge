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
package org.spongepowered.common.entity;

import net.minecraft.entity.passive.HorseType;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.text.translation.SpongeTranslation;

public class SpongeHorseVariant extends SpongeEntityMeta implements HorseVariant {

    // This actually contains the horse variants, not armor types
    private HorseType horseType;

    public SpongeHorseVariant(int variant, HorseType type, String name) {
        super(variant, name);
        this.horseType = type;
    }

    public HorseType getType() {
        return this.horseType;
    }

    @Override
    public Translation getTranslation() {
        switch (this.type) {
            case 0:
            default:
                return new SpongeTranslation("entity.horse.name");
            case 1:
                return new SpongeTranslation("entity.donkey.name");
            case 2:
                return new SpongeTranslation("entity.mule.name");
            case 3:
                return new SpongeTranslation("entity.zombiehorse.name");
            case 4:
                return new SpongeTranslation("entity.skeletonhorse.name");
        }
    }

}
