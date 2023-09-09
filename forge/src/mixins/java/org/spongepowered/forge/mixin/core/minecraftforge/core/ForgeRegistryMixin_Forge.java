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

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.registry.SpongeRegistryEntry;
import org.spongepowered.common.registry.SpongeRegistryType;
import org.spongepowered.forge.bridge.minecraftforge.fml.ForgeRegistryBridge;

import java.util.Map;
import java.util.StringJoiner;

@Mixin(ForgeRegistry.class)
public abstract class ForgeRegistryMixin_Forge<V> implements ForgeRegistryBridge<V> {

    // @formatter:off
    @Shadow @Final private net.minecraft.resources.ResourceKey<Registry<V>> key;
    // @formatter:on

    private final Map<ResourceKey, RegistryBridge<V>> forge$parents = Maps.newHashMap();
    private boolean forge$warnedIfNoParent;

    @Inject(method = "add(ILnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;Ljava/lang/String;)I", at = @At("TAIL"))
    public void forge$writeToParent(final int id, final ResourceLocation key, final V value, final String owner, final CallbackInfoReturnable<Integer> cir) {
        final ResourceKey root = (ResourceKey) (Object) this.key.registry();
        final ResourceKey location = (ResourceKey) (Object) this.key.location();

        if (!this.forge$warnedIfNoParent && this.forge$parents.isEmpty()) {
            // We only care about minecraft namespaced registries, as that is what we've got parents for.
            if (location.namespace().equalsIgnoreCase("minecraft")) {
                SpongeCommon.logger().error(String.format(
                        "No parent registry found for %s, things might not work correctly!",
                        new StringJoiner("/").add(root.formatted()).add(location.formatted())
                ));
            }
            this.forge$warnedIfNoParent = true;
        }

        final SpongeRegistryEntry<V> entry = new SpongeRegistryEntry<>(new SpongeRegistryType<>(root, location),
                (ResourceKey) (Object) key, value);

        this.forge$parents.values().forEach(registry -> registry.bridge$register(entry));
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "setSlaveMap", at = @At("TAIL"))
    public void forge$establishParent(final ResourceLocation name, final Object obj, final CallbackInfo ci) {
        if (obj instanceof RegistryBridge) {
            this.forge$parents.put((ResourceKey) (Object) name, (RegistryBridge<V>) obj);
        }
    }

}
