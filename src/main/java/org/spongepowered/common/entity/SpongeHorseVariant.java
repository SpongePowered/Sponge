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

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.text.translation.SpongeTranslation;

@SuppressWarnings("deprecation")
public class SpongeHorseVariant extends SpongeEntityMeta implements org.spongepowered.api.data.type.HorseVariant {

    private final Class<? extends AbstractHorse> type;

    public SpongeHorseVariant(int variant, Class<? extends AbstractHorse> type, String name) {
        super(variant, name);
        this.type = type;
    }

    @Override
    public Translation getTranslation() {
        if (this.type == EntityHorse.class) {
            return new SpongeTranslation("entity.horse.name");
        } else if (this.type == EntityDonkey.class) {
            return new SpongeTranslation("entity.donkey.name");
        } else if (this.type == EntityMule.class) {
            return new SpongeTranslation("entity.mule.name");
        } else if (this.type == EntityZombieHorse.class) {
            return new SpongeTranslation("entity.zombiehorse.name");
        } else if (this.type == EntitySkeletonHorse.class) {
            return new SpongeTranslation("entity.skeletonhorse.name");
        }
        return new SpongeTranslation("entity.horse.name");
    }

}
