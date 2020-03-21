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
package org.spongepowered.common.mixin.core.entity.merchant.villager;

import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.CatalogKeyBridge;

@Mixin(VillagerProfession.class)
public abstract class VillagerProfessionMixin implements CatalogKeyBridge {

    private CatalogKey impl$key;

    @Redirect(
        method = "register(Ljava/lang/String;Lnet/minecraft/village/PointOfInterestType;Lcom/google/common/collect/ImmutableSet;Lcom/google/common/collect/ImmutableSet;)Lnet/minecraft/entity/merchant/villager/VillagerProfession;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/registry/Registry;register(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private static Object impl$setKey(final Registry<Object> registry, final ResourceLocation key, final Object profession) {
        ((CatalogKeyBridge) profession).bridge$setKey((CatalogKey) (Object) key);
        return Registry.register(registry, key, profession);
    }

    @Override
    public CatalogKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final CatalogKey key) {
        this.impl$key = key;
    }
}
