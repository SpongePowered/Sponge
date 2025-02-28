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
package org.spongepowered.common.mixin.core.resources;

import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.resources.RegistryDataLoader_LoaderBridge;

@Mixin(targets = "net/minecraft/resources/RegistryDataLoader$Loader")
public abstract class RegistryDataLoader_LoaderMixin implements RegistryDataLoader_LoaderBridge {

    private RegistryHolder impl$registryHolder;

    @Inject(method = "loadFromResources", at = @At("HEAD"))
    private void impl$onLoadFromResources(final ResourceManager $$0, final RegistryOps.RegistryInfoLookup $$1, final CallbackInfo ci) {
        this.impl$registryHolder = (RegistryHolder) $$0;
    }

    @Override
    public RegistryHolder bridge$registryHolder() {
        return this.impl$registryHolder;
    }
}
