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
package org.spongepowered.common.mixin.tracker.world.level.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.RegistryBackedTrackableBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.tracker.TrackerCategory;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin_Tracker implements RegistryBackedTrackableBridge<BlockEntityType<?>> {

    @Redirect(method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private static Object impl$initializeTrackerState(final Registry<Object> registry, final String key, final Object toRegister) {
        final Object registered = Registry.register(registry, key, toRegister);

        final RegistryBackedTrackableBridge<BlockEntityType<?>> trackableBridge = (RegistryBackedTrackableBridge<BlockEntityType<?>>) toRegister;
        trackableBridge.bridge$refreshTrackerStates();

        return registered;
    }

    @Override
    public TrackerCategory bridge$trackerCategory() {
        return SpongeGameConfigs.getTracker().get().blockEntity;
    }

    @Override
    public Registry<BlockEntityType<?>> bridge$trackerRegistryBacking() {
        return Registry.BLOCK_ENTITY_TYPE;
    }

    @Override
    public void bridge$saveTrackerConfig() {
        SpongeGameConfigs.getTracker().save();
    }
}
