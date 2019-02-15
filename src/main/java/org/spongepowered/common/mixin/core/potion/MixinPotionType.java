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
package org.spongepowered.common.mixin.core.potion;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.potion.IMixinPotion;
import org.spongepowered.common.registry.type.item.PotionTypeRegistryModule;

import java.util.List;

@Mixin(net.minecraft.potion.PotionType.class)
public abstract class MixinPotionType implements PotionType, IMixinPotion {

    @Shadow @Final private ImmutableList<net.minecraft.potion.PotionEffect> effects;

    private String spongeResourceID;

    @Override
    @SuppressWarnings("unchecked")
    public List<PotionEffect> getEffects() {
        return ((List) this.effects); // PotionEffect is mixed into
    }

    @Override
    public CatalogKey getKey() {
        return (CatalogKey) (Object) IRegistry.POTION.getKey((net.minecraft.potion.PotionType) (Object) this);
    }

    @Override
    public String getName() {
        return this.spongeResourceID;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/IRegistry;put("))
    private static void onPotionRegister(RegistryNamespacedDefaultedByKey registry, ResourceLocation resource, Object potion) {
        final net.minecraft.potion.PotionType mcPotion = (net.minecraft.potion.PotionType) potion;

        ((IMixinPotion) mcPotion).setId(resource);
        PotionTypeRegistryModule.getInstance().registerFromGameData(resource.toString(), (PotionType) mcPotion);
        registry.put(resource, potion);
    }
}
