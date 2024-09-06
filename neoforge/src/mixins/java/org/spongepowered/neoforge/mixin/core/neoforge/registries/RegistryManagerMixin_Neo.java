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
package org.spongepowered.neoforge.mixin.core.neoforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.RegistrySnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(RegistryManager.class)
public class RegistryManagerMixin_Neo {

    @SuppressWarnings("UnstableApiUsage")
    @Inject(method = "takeSnapshot", at = @At("RETURN"))
    private static void forge$dontSendSpongeDataToClient(RegistryManager.SnapshotType snapshotType, CallbackInfoReturnable<Map<ResourceLocation, RegistrySnapshot>> cir) {
        if (snapshotType != RegistryManager.SnapshotType.SYNC_TO_CLIENT) {
            return;
        }

        // TODO NeoForge
        /*for (RegistrySnapshot snapshot : cir.getReturnValue().values()) {
            snapshot.ids.keySet().removeIf(loc -> loc.getNamespace().equals("sponge"));
        }*/
    }
}
