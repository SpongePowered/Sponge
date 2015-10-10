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
package org.spongepowered.common.mixin.core.entity.ai.attributes;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.BaseAttribute;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.attribute.Attribute;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(RangedAttribute.class)
@Implements(@Interface(iface = Attribute.class, prefix = "attribute$"))
public abstract class MixinRangedAttribute extends BaseAttribute implements Attribute {

    @Shadow private double minimumValue;
    @Shadow private double maximumValue;

    protected MixinRangedAttribute(IAttribute p_i45892_1_, String unlocalizedNameIn, double defaultValueIn) {
        super(p_i45892_1_, unlocalizedNameIn, defaultValueIn);
    }

    public double attribute$getMinimum() {
        return this.maximumValue;
    }

    public double attribute$getMaximum() {
        return this.maximumValue;
    }

    public double attribute$getDefaultValue() {
        return this.getDefaultValue();
    }

    public Predicate<DataHolder> attribute$getTargets() {
        return input -> {
            switch (this.getAttributeUnlocalizedName()) {
                case "horse.jumpStrength":
                    return input instanceof EntityHorse;
                case "zombie.spawnReinforcements":
                    return input instanceof EntityZombie;
                default:
                    return input instanceof EntityLivingBase;
            }
        };
    }

    public String attribute$getId() {
        return this.getAttributeUnlocalizedName();
    }

    public String attribute$getName() {
        return this.getAttributeUnlocalizedName();
    }
}
