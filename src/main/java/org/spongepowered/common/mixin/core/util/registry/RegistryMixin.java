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
package org.spongepowered.common.mixin.core.util.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;

import java.util.function.Supplier;

@Mixin(Registry.class)
public abstract class RegistryMixin {

    @Inject(method = "register(Ljava/lang/String;Lnet/minecraft/util/registry/MutableRegistry;Ljava/util/function/Supplier;)"
        + "Lnet/minecraft/util/registry/MutableRegistry;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void impl$registerRegistry(String registryName, Registry<Object> registry, Supplier<Object> supplier,
        CallbackInfoReturnable<Object> cir, ResourceLocation location) {
        final Object potentialCatalog = supplier.get();
        if (potentialCatalog instanceof CatalogType && registry instanceof SimpleRegistry) {
            // We don't care about non-catalog types and custom implementations of Registry
            SpongeImpl.getRegistry().getCatalogRegistry().registerRegistry((Class<CatalogType>) potentialCatalog.getClass(),
                (CatalogKey) (Object) location, (Registry<CatalogType>) (Object) registry);
        }
    }
}
