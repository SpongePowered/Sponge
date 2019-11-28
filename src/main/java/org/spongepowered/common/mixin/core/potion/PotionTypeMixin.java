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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.registry.type.item.PotionTypeRegistryModule;

@Mixin(net.minecraft.potion.Potion.class)
public abstract class PotionTypeMixin {


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "registerPotionType",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/registry/RegistryNamespacedDefaultedByKey;register(ILjava/lang/Object;Ljava/lang/Object;)V"))
    private static void impl$registerForSponge(final DefaultedRegistry registry, final int id, final Object location,
        final Object potion) {
        final ResourceLocation resource = (ResourceLocation) location;
        final net.minecraft.potion.Potion mcPotion = (net.minecraft.potion.Potion) potion;

        PotionTypeRegistryModule.getInstance().registerFromGameData(resource.toString(), (PotionType) mcPotion);
        registry.func_177775_a(id, location, potion);
    }

}
