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
package org.spongepowered.forge.mixin.core.minecraftforge.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.registry.SpongeRegistryEntry;
import org.spongepowered.common.registry.SpongeRegistryType;
import org.spongepowered.forge.bridge.minecraftforge.fml.RegistryDelegate;

import java.util.Map;

@Mixin(ForgeRegistry.class)
public abstract class ForgeRegistryMixin<V extends IForgeRegistryEntry<V>> implements RegistryDelegate<V> {

    @Shadow @Final Map<ResourceLocation, ?> slaves;
    @Shadow @Final private net.minecraft.resources.ResourceKey<Registry<V>> key;
    private Parent<V> parent;

    @Inject(method = "add(ILnet/minecraftforge/registries/IForgeRegistryEntry;Ljava/lang/String;)I", at = @At("TAIL"))
    public void sponge$writeToParent(int id, V value, String owner, CallbackInfoReturnable<Integer> cir) {
        if(this.parent == null) {
            this.parent = this.slaves.entrySet()
                    .stream()
                    .filter(key -> key.getValue() instanceof RegistryDelegate.Parent)
                    .findFirst()
                    .map(entry -> (RegistryDelegate.Parent) entry.getValue())
                    .orElse(null);
        }

        final ResourceKey root = (ResourceKey) (Object) this.key.getRegistryName();
        final ResourceKey location = (ResourceKey) (Object) this.key.location();
        this.parent.bridge$register(new SpongeRegistryEntry<>(new SpongeRegistryType<>(root, location),
                (ResourceKey) (Object) value.getRegistryName(), value));
    }

}
