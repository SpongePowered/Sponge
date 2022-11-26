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
package org.spongepowered.common.mixin.core.client.gui.screens.worldselection;

import com.mojang.serialization.DynamicOps;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.server.SpongeWorldManager;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Redirect(method = "lambda$loadWorldStem$1",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryOps;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/resources/RegistryOps;"))
    private static <T> RegistryOps<T> impl$captureBootstrapOps(final DynamicOps<T> $$0, final HolderLookup.Provider $$1) {
        final RegistryOps<T> ops = RegistryOps.create($$0, $$1);
        SpongeWorldManager.bootstrapOps = (RegistryOps<Tag>) ops;
        return ops;
    }


}
