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
package org.spongepowered.common.mixin.api.mcp.potion;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(net.minecraft.potion.Potion.class)
public abstract class PotionTypeMixin_API implements PotionType {

    @Shadow @Final public static DefaultedRegistry<ResourceLocation, net.minecraft.potion.Potion> REGISTRY;
    @Shadow @Final private ImmutableList<net.minecraft.potion.EffectInstance> effects;

    @Nullable private String spongeResourceID;

    @Override
    @SuppressWarnings("unchecked")
    public List<PotionEffect> getEffects() {
        return ((List) this.effects); // PotionEffect is mixed into
    }

    @Override
    public String getId() {
        if (this.spongeResourceID == null) {
            this.spongeResourceID = REGISTRY.getKey((net.minecraft.potion.Potion) (Object) this).toString();
        }
        return this.spongeResourceID;
    }

    @Override
    public String getName() {
        return this.getId();
    }

}
